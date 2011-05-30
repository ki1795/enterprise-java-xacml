package tests.an.xacml.indexing;

import static deprecateed.an.xacml.adapter.file.XACMLParser.parseRequest;
import static org.junit.Assert.assertTrue;
import static tests.an.xacml.util.TestUtil.getFileFromClassPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import deprecated.an.xacml.context.Request;
import deprecated.an.xacml.policy.AbstractPolicy;
import deprecateed.an.xacml.adapter.file.XMLFileDataStore;

import an.config.ConfigElement;
import an.config.Configuration;
import an.config.ConfigurationException;
import an.control.OperationFailedException;
import an.log.LogFactory;
import an.log.LogInitializationException;
import an.log.Logger;
import an.xacml.IndeterminateException;
import an.xacml.engine.CacheManager;
import an.xacml.engine.PDP;
import an.xacml.engine.PDPInitializeException;
import an.xacml.engine.ctx.EvaluationContext;
import an.xml.XMLGeneralException;

/**
 * This class is intend to test if the retrieved cacheables from CacheManager are the right ones.
 */
public class IndexingTest {
    private final static String CONFIG_FILE = "pdp.xml";
    private final static String REQUEST_PATTERN = ".*Request.xml";
    private static PDP pdp;
    private static AbstractPolicy[] allPolicies;
    private static String contextPath;
    private static Logger logger;

    private String[] loadRequestFiles() throws Exception {
        System.out.print("Loading request file list ... ");
        ArrayList<String> requestFiles = new ArrayList<String>();
        File dir = new File(contextPath);
        if (dir.exists() && dir.isDirectory()) {
            File[] reqsInDir = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.matches(REQUEST_PATTERN) && !name.equals("IIA005Request.xml");
                }
            });

            for (File reqFile : reqsInDir) {
                requestFiles.add(reqFile.getAbsolutePath());
            }
        }
        System.out.println("done.");
        return requestFiles.toArray(new String[0]);
    }

    private void tryEachRequest(Request request, String fileName) throws Exception {
        System.out.print("Trying request: '" + fileName + "' ... result is: ");
        System.out.flush();
        CacheManager cacheMgr = CacheManager.getInstance(pdp);

        AbstractPolicy[] potentialMatchedPolicies = cacheMgr.getPoliciesByRequest(request);
        logger.debug("\tGot " + potentialMatchedPolicies.length + " potential matched policies.");
        for (AbstractPolicy policy : potentialMatchedPolicies) {
            boolean actualMatch = false;
            IndeterminateException intEx = null;
            try {
                actualMatch = policy.match(new EvaluationContext(request));
            }
            catch (IndeterminateException ex) {
                intEx = ex;
            }
            logger.debug("\t\t - " + policy.getId() + " : actualMatch = " + (intEx == null ? actualMatch : intEx.getMessage()));
        }

        AbstractPolicy[] potentialNotMatchedPolicies = computePotentialNotMatchedPolicies(potentialMatchedPolicies);
        logger.debug("\tGot " + potentialNotMatchedPolicies.length + " potential not matched policies.");
        if (potentialNotMatchedPolicies.length > 0) {
            boolean allMatched = false;
            IndeterminateException intEx = null;
            AbstractPolicy currentPolicy = null;
            for (AbstractPolicy policy : potentialNotMatchedPolicies) {
                boolean matched = false;
                try {
                    matched = policy.match(new EvaluationContext(request));
                    allMatched = allMatched || matched;

                    logger.debug("\t\t - " + policy.getId() + " : actualMatch = " + (intEx == null ? matched :
                        "Indeterminate (" + intEx.getMessage() + ")"));
                    if (matched) {
                        currentPolicy = policy;
                        break;
                    }
                }
                catch (IndeterminateException ex) {
                    intEx = ex;
                }
            }

            if (intEx == null) {
                System.out.println(allMatched ? "failed" : "succeeded");
                assertTrue("Expected NotApplicable, but got 1 matched, request '" + fileName + "', policy '" +
                        currentPolicy + "'", !allMatched);
            }
            else {
                // log the IndeterminateException.
                logger.warn("Got an IndeterminateException instead of a NotApplicable, this should be ok.", intEx);
                System.out.println("succeeded(" + intEx + ")");
            }
        }
        else {
            System.out.println("succeeded(0 not matched policy)");
        }
    }

    private AbstractPolicy[] computePotentialNotMatchedPolicies(AbstractPolicy[] matchedPolicies) {
        ArrayList<AbstractPolicy> result = new ArrayList<AbstractPolicy>();
        Set<AbstractPolicy> matchedSet = new HashSet<AbstractPolicy>();
        if (matchedPolicies != null && matchedPolicies.length > 0) {
            for (AbstractPolicy policy : matchedPolicies) {
                matchedSet.add(policy);
            }
        }
        for (AbstractPolicy policy : allPolicies) {
            if (!matchedSet.contains(policy)) {
                result.add(policy);
            }
        }
        return result.toArray(new AbstractPolicy[0]);
    }

    @BeforeClass public static void setup()
    throws FileNotFoundException, ConfigurationException, LogInitializationException,
    XMLGeneralException, PDPInitializeException, OperationFailedException {
        // load configuration file from class path
        ConfigElement config = new Configuration(getFileFromClassPath(CONFIG_FILE)).getConfigurationElement();
        LogFactory.initialize((ConfigElement)config.getSingleXMLElementByType(Logger.ELEMTYPE_LOG));
        logger = LogFactory.getLogger();

        ConfigElement[] pdpConfigs = (ConfigElement[])config.getXMLElementsByName(PDP.ELEM_PDP);
        if (pdpConfigs.length != 1) {
            throw new ConfigurationException("Expected 1 PDP configuration, but got " + pdpConfigs.length);
        }
        // start PDP, load all policies
        pdp = PDP.getInstance(pdpConfigs[0]);
        pdp.start();

        allPolicies = CacheManager.getInstance(pdp).getAllPolicies();
        contextPath = (String)pdp.getDataStoreConfig().getAttributeValueByName(XMLFileDataStore.ATTR_POLICY_PATH);
    }

    @Test public void testIndexMatching() throws Exception {
        String[] reqFileList = loadRequestFiles();
        System.out.print("Parsing requests ... ");
        Request[] reqs = new Request[reqFileList.length];
        for (int i = 0; i < reqFileList.length; i ++) {
            reqs[i] = parseRequest(new FileInputStream(reqFileList[i]));
        }
        System.out.println("done.");
        long start = System.currentTimeMillis();
        for (int i = 0; i < reqFileList.length; i ++) {
            tryEachRequest(reqs[i], reqFileList[i]);
        }
        System.out.println("Processed " + reqFileList.length + " requests in " +
                (System.currentTimeMillis() - start) / 1000 + " seconds.");
    }

    @AfterClass public static void tearDown() {
        // stop PDP
        pdp.shutdownForce();
        LogFactory.shutdown();
    }
}