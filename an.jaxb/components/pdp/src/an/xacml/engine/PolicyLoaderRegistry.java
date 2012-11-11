package an.xacml.engine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Map;

import an.config.ConfigElement;
import an.config.ConfigurationException;
import an.xacml.PDP;

/**
 * If "an.xacml.engine.PolicyLoader" is configured for a custom PolicyLoader 
 * in configuration file or in VM argument, the helper will create an instance 
 * of the configured one. The custom PolicyLoader should implements PolicyLoader
 * interface, and it should have a constructor with following signature,
 * 
 * CustomPolicyLoader(CustomConfigElement config)
 */
public abstract class PolicyLoaderRegistry {
    private static Map<PDP, PolicyLoader> plRegistry = new Hashtable<PDP, PolicyLoader>();
    private static Map<PolicyLoader, PDP> pdpRegistry = new Hashtable<PolicyLoader, PDP>();
    /**
     * We support custom policy loaders. The custom policy loader should implement interface PolicyLoader, and its
     * implementation class should be defined by this attribute.
     */
    public static final String ATTR_DATASTORE_CLASSNAME = "an.xacml.engine.PolicyLoader";

    /**
     * Each PDP has a unique policy loader.
     * @param pdp
     * @return
     * @throws ConfigurationException
     */
    public static synchronized PolicyLoader getDataStore(PDP pdp) throws ConfigurationException {
        PolicyLoader ds = (PolicyLoader)plRegistry.get(pdp);

        if (ds == null) {
            ConfigElement config = pdp.getDataStoreConfig();
            String dsClassName = (String)config.getAttributeValueByName(ATTR_DATASTORE_CLASSNAME);

            try {
                Class<?> dsClass = Class.forName(dsClassName);
                Constructor<?> dsCons = dsClass.getDeclaredConstructor(new Class[]{config.getClass()});
                ds = (PolicyLoader)dsCons.newInstance(config);
                plRegistry.put(pdp, ds);
                pdpRegistry.put(ds, pdp);
            }
            catch (Exception e) {
                if (e instanceof InvocationTargetException) {
                    Throwable cfEx = ((InvocationTargetException)e).getCause();
                    if (cfEx instanceof ConfigurationException) {
                        throw (ConfigurationException)cfEx;
                    }
                }
                throw new ConfigurationException("Error occurs when initialize the DataStore.", e);
            }
        }
        return ds;
    }

    public static PDP getPDP(PolicyLoader ds) {
        return pdpRegistry.get(ds);
    }

    public static synchronized void removeDataStore(PDP pdp) {
        PolicyLoader ds = plRegistry.remove(pdp);
        pdpRegistry.remove(ds);
    }
}