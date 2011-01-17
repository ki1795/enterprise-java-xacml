package tests.an.xacml.conformance20;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class XACMLConformanceTestSuite {
    public static final String CONFIG_KEY_TESTCASES = "TestCases";
    public static final String CONFIG_KEY_BEFORE = "BeforeSuite";
    public static final String CONFIG_KEY_AFTER = "AfterSuite";
    public static final String CONFIG_KEY_DEFAULTCONFIG = "DefaultConfig";
    public static final String TESTCASES_DELIMITER = ",";

    private Properties testProps;
    private String suiteName;
    private String beforeSuite;
    private String afterSuite;
    private String defaultConfigStream;
    private XACMLConformanceTestCase[] testCases;

    public XACMLConformanceTestSuite(String suiteName, Properties testProps) {
        this.testProps = testProps;
        this.suiteName = suiteName;
        beforeSuite = testProps.getProperty(CONFIG_KEY_BEFORE);
        afterSuite = testProps.getProperty(CONFIG_KEY_AFTER);
        defaultConfigStream = testProps.getProperty(CONFIG_KEY_DEFAULTCONFIG, "");
    }

    public void initialize() throws Exception {
        String[] testCaseNames = testProps.getProperty(CONFIG_KEY_TESTCASES, "").split(TESTCASES_DELIMITER);
        testCases = new XACMLConformanceTestCase[testCaseNames.length];
        for (int i = 0; i < testCaseNames.length; i ++) {
            String each = testCaseNames[i];
            String config = testProps.getProperty(each);
            if (config == null) {
                config = defaultConfigStream;
            }
            testCases[i] = new XACMLConformanceTestCase(each, config);
        }
    }

    public XACMLConformanceTestCase[] getAllTestCases() {
        return testCases;
    }

    public String getTestSuiteName() {
        return suiteName;
    }

    public Map<String, Object> runSuite(PrintStream out) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            beforeSuite(beforeSuite, suiteName);
            for (XACMLConformanceTestCase eachCase : testCases) {
                String caseName = eachCase.getTestCaseName();
                try {
                    eachCase.initialize();
                    if (out != null) {
                        out.print("===> Running test case '" + caseName + "' ... result : ");
                    }
                    boolean caseResult = eachCase.runTestCase();
                    if (out != null) {
                        out.println(caseResult);
                        System.out.println("-------------------------------------------");
                    }
                    result.put(caseName, caseResult);
                }
                catch (Exception ex) {
                    result.put(caseName, ex);
                    if (out != null) {
                        out.println(ex.getClass().getSimpleName() + " - " + ex.getMessage());
                        System.out.println("-------------------------------------------");
                    }
                }
                finally {
                    eachCase.cleanup();
                }
            }
            return result;
        }
        finally {
            afterSuite(afterSuite, suiteName);
        }
    }

    private void beforeSuite(String beforeClass, String suiteName) throws Exception {
        if (beforeClass != null && beforeClass.length() > 0) {
            Class<?> clazz = Class.forName(beforeClass);
            Method method = clazz.getMethod("before" + suiteName);
            method.invoke(null);
        }
    }

    private void afterSuite(String afterClass, String suiteName) throws Exception {
        if (afterClass != null && afterClass.length() > 0) {
            Class<?> clazz = Class.forName(afterClass);
            Method method = clazz.getMethod("after" + suiteName);
            method.invoke(null);
        }
    }
}