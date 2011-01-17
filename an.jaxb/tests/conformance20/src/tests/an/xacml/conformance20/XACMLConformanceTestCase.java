package tests.an.xacml.conformance20;

import static an.xacml.adapter.file.XACMLParser.parseRequest;
import static an.xacml.adapter.file.XACMLParser.parseResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

import an.config.ConfigElement;
import an.config.Configuration;
import an.log.LogFactory;
import an.log.Logger;
import an.xacml.adapter.file.XACMLParser;
import an.xacml.adapter.file.XMLFileDataStore;
import an.xacml.context.Request;
import an.xacml.context.Response;
import an.xacml.engine.PDP;

public class XACMLConformanceTestCase {
    public static final String TESTCASE_PLACEHOLDER = "@TESTCASE@";
    public static final String REQUEST_SUFFIX = "Request.xml";
    public static final String RESPONSE_SUFFIX = "Response.xml";
    private Configuration config;
    PDP pdp;
    private Request request;
    private Response expectedResponse;
    private String contextDir;
    private String testCaseName;
    private Logger logger;

    public XACMLConformanceTestCase(String caseName, String configStream) throws Exception {
    	logger = LogFactory.getLogger();
        testCaseName = caseName;
        String configString = configStream.replaceAll(TESTCASE_PLACEHOLDER, caseName);
        config = new Configuration(new ByteArrayInputStream(configString.getBytes()));
        ConfigElement rootConfig = config.getConfigurationElement();
        ConfigElement pdpConfig = (ConfigElement)rootConfig.getXMLElementsByName(PDP.ELEM_PDP)[0];
        pdp = PDP.getInstance(pdpConfig);
        contextDir = (String)pdp.getDataStoreConfig().getAttributeValueByName(XMLFileDataStore.ATTR_POLICY_PATH);
    }

    public void initialize() throws Exception {
        pdp.start();
        request = parseRequest(new FileInputStream(contextDir + "/" + testCaseName + REQUEST_SUFFIX));
        expectedResponse = parseResponse(new FileInputStream(contextDir + "/" + testCaseName + RESPONSE_SUFFIX));
    }

    public void cleanup() throws Exception {
        pdp.shutdownForce();
        pdp = null;
        config = null;
        request = null;
        expectedResponse = null;
    }

    public String getTestCaseName() {
        return testCaseName;
    }

    public boolean runTestCase() throws Exception {
    	if (logger.isDebugEnabled()) {
    		ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
    		XACMLParser.dumpRequest(request, tempOut);
    		logger.debug("Dump request: " + tempOut.toString());
    	}

    	Response actualResponse = (Response)pdp.handleRequest(request);
    	if (logger.isDebugEnabled()) {
    		ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
    		XACMLParser.dumpResponse(actualResponse, tempOut);
    		logger.debug("Dump actual response: " + tempOut.toString());
    	}

    	// Compare the result with expected one
        if (actualResponse.equals(expectedResponse)) {
            return true;
        }
        return false;
    }

    public boolean runTestCase(int times) throws Exception {
        // Evaluate current request
        long evalStart = System.currentTimeMillis();
        Response actualResponse = null;
        for (int i = 0; i < times; i ++) {
            actualResponse = (Response)pdp.handleRequest(request);
        }
        System.out.println("Evaluation takes " + (System.currentTimeMillis() - evalStart) + " million seconds for " +
                (times <= 0 ? 1 : times) + " times w/o any cache.");

        // Compare the result with expected one
        if (actualResponse.equals(expectedResponse)) {
            return true;
        }
        return false;
    }
}