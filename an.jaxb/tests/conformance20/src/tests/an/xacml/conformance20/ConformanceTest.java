package tests.an.xacml.conformance20;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static tests.an.xacml.util.TestUtil.getFileFromClassPath;

import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import an.config.ConfigElement;
import an.config.Configuration;
import an.log.LogFactory;
import an.log.Logger;
import an.xacml.engine.CacheManager;
import an.xml.XMLGeneralException;

public class ConformanceTest {
    public static final String CONFIG_SUFFIX = ".properties";

    private XACMLConformanceTestSuite initSuite(String suiteName) throws Exception {
        // Load test config file
        String configFileName = suiteName + CONFIG_SUFFIX;
        configFileName = getFileFromClassPath(configFileName);
        Properties testProps = new Properties();
        testProps.load(new FileInputStream(configFileName));
        XACMLConformanceTestSuite suite = new XACMLConformanceTestSuite(suiteName, testProps);
        suite.initialize();
        return suite;
    }

    private Map<String, Object> runSuite(String suiteName) throws Exception {
        System.out.println("===========================================");
        System.out.println("Start running suite '" + suiteName + "' ...");

        try {
            return initSuite(suiteName).runSuite(System.out);
        }
        finally {
            System.out.println(suiteName + " runs done.");
        }
    }

    private void confirmResultAllTruth(Map<String, Object> result) {
        for (String each : result.keySet()) {
            Object actualResult = result.get(each);
            if (!(actualResult instanceof Boolean) || !(Boolean)actualResult) {
                fail("Test case - '" + each + "' : Expecting true but got '" + actualResult + "'");
            }
        }
    }

    @BeforeClass public static void initialize() throws Exception {
        String configFileName = getFileFromClassPath("conformance20.xml");
        Configuration config = new Configuration(configFileName);
        LogFactory.initialize(
                (ConfigElement)config.getConfigurationElement().getSingleXMLElementByType(Logger.ELEMTYPE_LOG));
    }

    @Test public void suiteAttributeReferences() throws Exception {
        XACMLConformanceTestSuite suite = initSuite("AttributeReferences");
        XACMLConformanceTestCase[] allCases = suite.getAllTestCases();
        for (XACMLConformanceTestCase testCase : allCases) {
            if (testCase.getTestCaseName().equals("IIA004")) {
                int policyNumber = CacheManager.getInstance(testCase.pdp).getCachedPolicyNumber();
                assertTrue("Test case - '" + testCase.getTestCaseName() + "' : Expecting no policy loaded but got " +
                        policyNumber, policyNumber == 0);
            }
        }

        System.out.println("===========================================");
        System.out.println("Start running suite '" + suite.getTestSuiteName() + "' ...");

        try {
            Map<String, Object> result = suite.runSuite(System.out);

            for (String each : result.keySet()) {
                Object actualResult = result.get(each);
                if (!each.equals("IIA004")) {
                    if (each.equals("IIA005")) {
                        if (actualResult instanceof XMLGeneralException) {
                            String msg = ((XMLGeneralException)actualResult).getMessage();
                            String expected = "Error occurs during parse XML file";
                            if (msg.indexOf(expected) < 0) {
                                fail("Test case - '" + each +
                                        "' : Expecting message '" + expected + "' but got '" + actualResult + "'");
                            }
                        }
                        else {
                            fail("Test case - '" + each + "' : Expecting XMLGeneralException but got '" + actualResult + "'");
                        }
                    }
                    else {
                        if (!(actualResult instanceof Boolean) || !(Boolean)actualResult) {
                            fail("Test case - '" + each + "' : Expecting true but got '" + actualResult + "'");
                        }
                    }
                }
            }
        }
        finally {
            System.out.println(suite.getTestSuiteName() + " runs done.");
        }
    }

    @Test public void suiteTargetMatching() throws Exception {
        confirmResultAllTruth(runSuite("TargetMatching"));
    }

    @Test public void suiteFunctionGeneralApplyTests() throws Exception {
        confirmResultAllTruth(runSuite("FunctionGeneralApplyTests"));
    }

    @Test public void suiteFunctionArithmeticFunctions() throws Exception {
        confirmResultAllTruth(runSuite("FunctionArithmeticFunctions"));
    }

    @Test public void suiteFunctionArithmeticConversionFunctions() throws Exception {
        confirmResultAllTruth(runSuite("FunctionArithmeticConversionFunctions"));
    }

    @Test public void suiteFunctionEqualityFunctions() throws Exception {
        confirmResultAllTruth(runSuite("FunctionEqualityFunctions"));
    }

    @Test public void suiteFunctionStringRegMatchFunctions() throws Exception {
        confirmResultAllTruth(runSuite("FunctionStringRegMatchFunctions"));
    }

    @Test public void suiteFunctionComparisonFunctions1() throws Exception {
        confirmResultAllTruth(runSuite("FunctionComparisonFunctions1"));
    }

    @Test public void suiteFunctionSpecialMatchingFunctions() throws Exception {
        confirmResultAllTruth(runSuite("FunctionSpecialMatchingFunctions"));
    }

    @Test public void suiteFunctionLogicalFunctions() throws Exception {
        confirmResultAllTruth(runSuite("FunctionLogicalFunctions"));
    }

    @Test public void suiteFunctionStringNormalizationFunctions() throws Exception {
        confirmResultAllTruth(runSuite("FunctionStringNormalizationFunctions"));
    }

    @Test public void suiteFunctionDurationFunctions() throws Exception {
        confirmResultAllTruth(runSuite("FunctionDurationFunctions"));
    }

    @Test public void suiteFunctionComparisonFunctions2() throws Exception {
        confirmResultAllTruth(runSuite("FunctionComparisonFunctions2"));
    }

    @Test public void suiteFunctionBagFunctions() throws Exception {
        confirmResultAllTruth(runSuite("FunctionBagFunctions"));
    }

    @Test public void suiteFunctionHighOrderBagFunctions() throws Exception {
        confirmResultAllTruth(runSuite("FunctionHighOrderBagFunctions"));
    }

    @Test public void suiteFunctionSetFunctions() throws Exception {
        confirmResultAllTruth(runSuite("FunctionSetFunctions"));
    }

    @Test public void suiteFunctionDurationEqualsFunctions() throws Exception {
        confirmResultAllTruth(runSuite("FunctionDurationEqualsFunctions"));
    }

    @Test public void suiteCombiningAlgorithms() throws Exception {
        confirmResultAllTruth(runSuite("CombiningAlgorithms"));
    }

    @Test public void suiteSchemaComponents() throws Exception {
        confirmResultAllTruth(runSuite("SchemaComponents"));
    }

    /*=====================================
     * Following are all optional features
     =====================================*/
    @Test public void suiteObligations() throws Exception {
        confirmResultAllTruth(runSuite("Obligations"));
    }

    @Test public void suiteHierarchicalResources() throws Exception {
        // TODO we now don't support hierarchical resource, so we don't run this suite.
    }

    @Test public void suiteAttributeSelectors() throws Exception {
        confirmResultAllTruth(runSuite("AttributeSelectors"));
    }

    @Test public void suiteNonMandatoryFunctions() throws Exception {
        confirmResultAllTruth(runSuite("NonMandatoryFunctions"));
    }
}