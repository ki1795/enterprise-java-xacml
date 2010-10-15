package an.example.dems;

import static an.example.dems.DEMSStatus.KEY_RUN_STATUS;
import static an.example.dems.DEMSStatus.STATUS_RUN_INITIALIZED;
import static an.example.dems.DEMSStatus.STATUS_RUN_NOTRUN;
import static an.example.dems.DEMSStatus.STATUS_RUN_RUNING;
import an.config.ConfigElement;
import an.config.Configuration;
import an.control.AbstractMonitorableAndControllable;
import an.control.OperationFailedException;
import an.log.LogFactory;
import an.log.Logger;
import an.util.CommandLineArguments;
import an.util.InvalidCommandLineArgumentException;
import an.xacml.engine.PDP;

/**
 * The main program.
 */
public class DEMS extends AbstractMonitorableAndControllable {
    public static final String PRODUCT = "*Distributed Entitlement Management System*";
    public static final String VERSION = "<1.0>";
    public static final String TOKEN_CONFIG = "configFile";
    public static final String[] REQUIRED_ARGS = {TOKEN_CONFIG};
    public static DEMS demsMain;

    private ConfigElement config;
    private PDP[] pdps;

    /**
     * Don't allow to instantial from outside.
     * @param config
     * @throws Throwable
     */
    private DEMS(Configuration config) throws Throwable {
        this.config = config.getConfigurationElement();
        status = new DEMSStatus();
        initialize();
    }

    /**
     * For now, we only have PDP in system, so we initialize all PDPs.
     * @throws Throwable
     */
    protected void initialize() throws Throwable {
        try {
            ConfigElement logConfig = (ConfigElement)config.getSingleXMLElementByType(Logger.ELEMTYPE_LOG);
            LogFactory.initialize(logConfig);

            ConfigElement[] pdpConfigs = (ConfigElement[])config.getXMLElementsByName(PDP.ELEM_PDP);
            pdps = new PDP[pdpConfigs.length];
            for (int i = 0; i < pdpConfigs.length; i ++) {
                pdps[i] = PDP.getInstance(pdpConfigs[i]);
            }
            status.updateProperty(KEY_RUN_STATUS, STATUS_RUN_INITIALIZED);
        }
        catch (Throwable t) {
            LogFactory.shutdown();
            throw t;
        }
    }

    public synchronized void shutdown() {
        System.out.println("Shutting down " + PRODUCT + " gracefully ... ");

        Object current = status.getProperty(KEY_RUN_STATUS);
        if (!current.equals(STATUS_RUN_NOTRUN)) {
            // Shutdown all PDPs
            for (int i = 0; i < pdps.length; i ++) {
                pdps[i].shutdown();
            }
            LogFactory.shutdown();
            // May shutdown other services in the future.
            notifyAll();
            status.updateProperty(KEY_RUN_STATUS, STATUS_RUN_NOTRUN);
            System.out.println(PRODUCT + " has been shutdown.");
        }
        else {
            System.out.println(PRODUCT + " has already been shutdown, you can't shutdown it again.");
        }
    }

    public synchronized void shutdownForce() {
        System.out.println("Shutting down " + PRODUCT + " immediately ... ");

        Object current = status.getProperty(KEY_RUN_STATUS);
        if (!current.equals(STATUS_RUN_NOTRUN)) {
            // Force shutdown all PDPs
            for (int i = 0; i < pdps.length; i ++) {
                pdps[i].shutdownForce();
            }
            LogFactory.shutdown();
            // May force shutdown other services in the future.
            notifyAll();
            status.updateProperty(KEY_RUN_STATUS, STATUS_RUN_NOTRUN);
            System.out.println(PRODUCT + " has been shutdown.");
        }
        else {
            System.out.println(PRODUCT + " has already been shutdown, you can't shutdown it again.");
        }
    }

    public synchronized void start() throws OperationFailedException {
        System.out.println("Starting " + PRODUCT + " ... ");

        Object current = status.getProperty(KEY_RUN_STATUS);
        if (current.equals(STATUS_RUN_INITIALIZED)) {
            // Start all PDPs
            for (int i = 0; i < pdps.length; i ++) {
                pdps[i].start();
            }
            // May start other services in the future.
            status.updateProperty(KEY_RUN_STATUS, STATUS_RUN_RUNING);
            System.out.println(PRODUCT + " " + VERSION + " has been started.");
        }
        else {
            System.out.println("Current status is " + current + ", you can't start it unless it is " + 
                    STATUS_RUN_INITIALIZED + ".");
        }
    }

    public synchronized void waiting() throws InterruptedException {
        wait();
    }

    public static void main(String[] args) {
        CommandLineArguments cmdLine = new CommandLineArguments(REQUIRED_ARGS, null);
        try {
            cmdLine.parse(args);

            String configFile = cmdLine.getArgumentValueByToken(TOKEN_CONFIG);
            if (configFile != null) {
                demsMain = new DEMS(new Configuration(configFile));
                demsMain.start();
                demsMain.waiting();
            }
            else {
                throw new InvalidCommandLineArgumentException(
                        "There is no configuratio file specified in command line arguments.");
            }
        }
        catch (Throwable t) {
            System.err.println("Error occurs when initialize system, will exit now.");
            t.printStackTrace(System.err);

            if (demsMain != null) {
                demsMain.shutdownForce();
            }
        }
    }
}