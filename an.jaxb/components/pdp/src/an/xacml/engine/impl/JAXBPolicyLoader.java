package an.xacml.engine.impl;

import static an.xml.XMLParserWrapper.getNamespaceMappings;
import static an.xml.XMLParserWrapper.getNodeXMLText;
import static an.xml.XMLParserWrapper.parse;
import static an.xml.XMLParserWrapper.verifySchemaFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import oasis.names.tc.xacml._2_0.policy.schema.os.PolicySetType;
import oasis.names.tc.xacml._2_0.policy.schema.os.PolicyType;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import an.config.ConfigElement;
import an.log.LogFactory;
import an.log.Logger;
import an.xacml.engine.PolicyLoader;
import an.xacml.engine.PolicyLoaderException;
import an.xml.XMLParserWrapper;

/**
 * The default implementation of DataStore, if "an.xacml.engine.DataStore"
 * is not configured, this defult DataStore will be used.
 */
public class JAXBPolicyLoader implements PolicyLoader {
    private String path;
    private String pattern;
    private Logger logger;
    private Map<String, File> policyFilesByID = new Hashtable<String, File>();

    private Unmarshaller jaxbUnmarshaller;
    private Binder<Node> jaxbBinder;

    public static final String ATTR_POLICY_PATH = "path";
    public static final String ATTR_FILENAME_PATTERN = "pattern";

    public JAXBPolicyLoader(ConfigElement config) throws Exception {
        logger = LogFactory.getLogger();
        loadConfigurations(config);
        initialize();
    }

    protected void initialize() throws Exception {
        SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        // Retrieve the schema file from given path or classpath, and verify if it exists.
        Schema sch = sf.newSchema(new StreamSource(verifySchemaFile(XMLParserWrapper.getPolicyDefaultSchema())));
        JAXBContext ctx = JAXBContext.newInstance(PolicyType.class);
        jaxbUnmarshaller = ctx.createUnmarshaller();
        jaxbBinder = ctx.createBinder();
        // enable XML schema validation
        jaxbUnmarshaller.setSchema(sch);
    }

    protected void loadConfigurations(ConfigElement config) {
        path = (String)config.getAttributeValueByName(ATTR_POLICY_PATH);
        pattern = (String)config.getAttributeValueByName(ATTR_FILENAME_PATTERN);
    }

    /**
     * Load all policies from a specific path, return a list of policy.
     * @throws PolicyLoaderException 
     */
    public List<Object> load() throws PolicyLoaderException {
            List<Object> policies = new ArrayList<Object>();
            policyFilesByID.clear();
            File dir = new File(path);

            if (dir.isDirectory()) {
                File[] policyFiles = dir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        if (pattern == null || pattern.length() == 0) {
                            return true;
                        }
                        else {
                            return name.matches(pattern);
                        }
                    }
                });

                System.out.println("Loading policies from '" + dir.toString() + "' ...");

                int actualPolicyNum = 0;
                boolean warn = false;
                boolean error = false;

                System.gc();
                long memBegin = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                long begin = System.currentTimeMillis();

                for (int i = 0; i < policyFiles.length; i ++) {
                    InputStream in = null;
                    try {
                        in = new FileInputStream(policyFiles[i]);
                        JAXBElement<?> p = (JAXBElement<?>)jaxbUnmarshaller.unmarshal(in);
                        Object root = p.getValue();

                        // check if there are duplicated policy IDs in different files
                        String policyId = getPolicyOrPolicySetId(root);
                        if (policyFilesByID.get(policyId) != null) {
                            throw new PolicyLoaderException("The policy loaded from '" +
                                    policyFiles[i] + "' with ID <" + policyId + "> already exists.");
                        }

                        // load the policy file using XML parser to get additional namespaces for future use
                        populateNamespaces(root, policyFiles[i]);

                        policies.add(root);
                        // This is used to build up a ID->File index
                        policyFilesByID.put(policyId, policyFiles[i]);
                        // 10 policies one dot, 1000 policies one line
                        actualPolicyNum ++;

                        if (actualPolicyNum % 10 == 0) {
                            System.out.print(".");
                        }
                        if (actualPolicyNum % 1000 == 0 && i < policyFiles.length - 1) {
                            System.out.println();
                        }

                        // Check if loaded policy is correct
                        if (logger.isDebugEnabled()) {
                            try {
                                // Log the XML document we loaded
                                logger.debug("Dump policy loaded from '" + policyFiles[i] + "': " +
                                        getNodeXMLText(jaxbBinder.getXMLNode(root)));
                            }
                            catch (Exception debugEx) {
                                logger.debug("Dump policy failed due to: ", debugEx);
                            }
                        }
                    }
                    catch (Exception e) {
                        logger.error("Error occurs when parsing policy file : " + policyFiles[i], e);
                        error = true;
                    }
                    finally {
                        try {
                            in.close();
                        }
                        catch (Exception ex) {}
                    }
                }

                // Print some hints
                if (actualPolicyNum > 0) {
                    long end = System.currentTimeMillis();
                    System.gc();
                    long memEnd = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

                    System.out.println("\n" + actualPolicyNum + " policies loaded. " +
                            "Time elapsed " + (end - begin) / 1000 + " second. " +
                            "Memory used " + (memEnd - memBegin) / 1024 / 1024 + " MB.");
                    if (error || warn) {
                        System.out.println("There are " + (error ? "errors" : "warnings") +
                                " occur while loading policies, please check log file for details.");
                    }
                }
            }
            return policies;
    }

    /**
     * Get policy id if parsed Object is a Policy, or get policySet id if parsed Object is a PolicySet.
     * There should not have multiple policies or policySets in a single file.
     * @param element
     * @return policy id
     */
    private String getPolicyOrPolicySetId(Object policy) throws PolicyLoaderException {
        if (policy instanceof PolicyType) {
            return ((PolicyType)policy).getPolicyId();
        }

        if (policy instanceof PolicySetType) {
            return ((PolicySetType)policy).getPolicySetId();
        }

        throw new PolicyLoaderException("Invalid policy type: " + policy.getClass().getSimpleName());
    }

    /**
     * Get any extra namespaces from policy XML file. The extra namespaces might be referenced by any nested XML nodes,
     * and these nodes could be selected by AttributeSelector.
     * @param policy
     * @param policyFile
     */
    private void populateNamespaces(Object policy, File policyFile) throws Exception {
        InputStream in = null;
        try {
            in = new FileInputStream(policyFile);
            Element elem = parse(in);
            Map<String, String> nsMappings = getNamespaceMappings(elem);
            if (policy instanceof PolicyType) {
                ((PolicyType)policy).setNamespaceMappings(nsMappings);
            }
            else if (policy instanceof PolicySetType) {
                ((PolicySetType)policy).setNamespaceMappings(nsMappings);
            }
        }
        finally {
            try {
                in.close();
            }
            catch (Exception ioEx) {}
        }
    }
}