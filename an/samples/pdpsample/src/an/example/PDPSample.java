package an.example;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.String;
import java.util.Scanner;

import an.config.ConfigElement;
import an.config.Configuration;
import an.log.LogFactory;
import an.log.Logger;
import an.util.CommandLineArguments;
import an.util.InvalidCommandLineArgumentException;
import an.xacml.adapter.file.XACMLParser;
import an.xacml.adapter.file.XMLFileDataStore;
import an.xacml.context.Request;
import an.xacml.context.Response;
import an.xacml.context.Result;
import an.xacml.engine.EvaluationContext;
import an.xacml.engine.PDP;
import an.xacml.policy.AbstractPolicy;

import java.io.*;

//import java.util.*;
import org.dom4j.*;
import org.dom4j.io.*;

import produceXML.requestXMLvector;
import produceXML.readXML;

public class PDPSample {
    private PDP pdp;
    private Logger logger;
    private String policyDir;
    public static final String TOKEN_CONFIG = "configFile";
    public static final String REQUEST_FILE = "request";
    public static final String[] REQUIRED_ARGS = {TOKEN_CONFIG, REQUEST_FILE};

    public PDPSample(Configuration config) throws Exception {
        ConfigElement rootConfig = config.getConfigurationElement();
        ConfigElement logConfig = (ConfigElement)rootConfig.getSingleXMLElementByType(Logger.ELEMTYPE_LOG);
        LogFactory.initialize(logConfig);
        logger = LogFactory.getLogger();
        ConfigElement pdpConfig = (ConfigElement)rootConfig.getXMLElementsByName(PDP.ELEM_PDP)[0];
        pdp = PDP.getInstance(pdpConfig);
        // Get the policy path that in configuration file. We will use it to parse policies one by one.
        policyDir = (String)pdp.getDataStoreConfig().getAttributeValueByName(XMLFileDataStore.ATTR_POLICY_PATH);
    }

    public void start() throws Exception {
        pdp.start();
    }

    public void refresh() throws Exception {
        pdp.reloadPolicies();
    }

    public void cleanup() {
        pdp.shutdownForce();
        pdp = null;
    }

    public void evaluateRequestAgainstPDP(Request request) throws Exception {
        if (logger.isDebugEnabled()) {
            ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
            XACMLParser.dumpRequest(request, tempOut);
            logger.debug("Dump request: " + tempOut.toString());
        }
        // Let PDP use its indexing mechanism to select corresponding poilicies and evaluate
        Response actualResponse = (Response)pdp.handleRequest(request);
        if (logger.isDebugEnabled()) {
            ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
            XACMLParser.dumpResponse(actualResponse, tempOut);
			
            String tempOutString= tempOut.toString(); //
            logger.debug("Dump actual response: " + tempOutString);
			
			InputStream is = new ByteArrayInputStream(tempOutString.getBytes());
			readXML result = new readXML(is);
        }
    }

    public void evaluateRequestAgainstPolicy(Request request, AbstractPolicy policy) throws Exception {
        // We send the request directly to policy.
        Result evalResult = policy.evaluate(new EvaluationContext(request));
        Response actualResponse = new Response(new Result[] {evalResult});
        if (logger.isDebugEnabled()) {
            ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
            XACMLParser.dumpResponse(actualResponse, tempOut);
            logger.debug("Dump actual response: " + tempOut.toString());
        }
    }
        
    
    public static void main(String[] args) {
        CommandLineArguments cmdLine = new CommandLineArguments(REQUIRED_ARGS, null);
        PDPSample sample = null;
        
        System.out.println("讀入的引數: ");
        for(int i=0; i<args.length; i++){
        	System.out.println(args[i] + " ");
        }
        System.out.println(" ");
            
        try {
            cmdLine.parse(args);

            // Get config file from command line arguments.
            String configFile = cmdLine.getArgumentValueByToken(TOKEN_CONFIG);
            String reqFile = cmdLine.getArgumentValueByToken(REQUEST_FILE);
            if (configFile != null) {
                sample = new PDPSample(new Configuration(configFile));
                sample.start();
                
                //while(true){
                	requestXMLvector req = new requestXMLvector();
                	req.buildXML(reqFile);
                //System.out.println(reqFile);

                // Load request from file.
                	Request request = XACMLParser.parseRequest(new FileInputStream(reqFile));

                // Evaluate the request against PDP which may hold 100, 1000 or 10000 policies. Let PDP retrieve
                // the matchable policies and evaluate them
                	sample.evaluateRequestAgainstPDP( request );
                //}

//                sample.refresh();
//                System.out.println(sample.policyDir);
                // List all policies under policy data store directory, then send the request to each policy one by one.
//                File dir = new File(sample.policyDir);
//                File[] policyFiles = dir.listFiles();
//                for (File policyFile : policyFiles) {
//                    if (policyFile.isFile() && policyFile.getName().endsWith(".xml")) {
//                        sample.evaluateRequestAgainstPolicy(request, XACMLParser.parsePolicy(new FileInputStream(policyFile)));
//                    }
//                	}
            }
            else {
                throw new InvalidCommandLineArgumentException(
                        "There is no configuratio file specified in command line arguments.");
            }
        }
        catch (Throwable t) {
            System.err.println("Error occurs when initialize system, will exit now.");
            t.printStackTrace(System.err);

            if (sample != null) {
                sample.cleanup();
            }
        }
    }
}