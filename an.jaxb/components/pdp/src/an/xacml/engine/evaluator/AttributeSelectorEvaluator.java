package an.xacml.engine.evaluator;

import static an.xacml.Constants.SUPPORTED_XPATH_VERSIONS;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import oasis.names.tc.xacml._2_0.context.schema.os.MissingAttributeDetailType;
import oasis.names.tc.xacml._2_0.context.schema.os.RequestType;
import oasis.names.tc.xacml._2_0.policy.schema.os.AttributeSelectorType;
import oasis.names.tc.xacml._2_0.policy.schema.os.AttributeValueType;
import oasis.names.tc.xacml._2_0.policy.schema.os.DefaultsType;
import oasis.names.tc.xacml._2_0.policy.schema.os.EngineObjectFactory;
import oasis.names.tc.xacml._2_0.policy.schema.os.PolicyType;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import an.config.ConfigurationException;
import an.xacml.Constants;
import an.xacml.IndeterminateException;
import an.xacml.engine.ctx.AttributeRetriever;
import an.xacml.engine.ctx.AttributeRetrieverRegistry;
import an.xacml.engine.ctx.EvaluationContext;
import an.xml.XMLDataTypeMappingException;

public class AttributeSelectorEvaluator implements Evaluator {

    private AttributeSelectorType attrSelector;
    private String dataType;
    private String contextPath;
    private boolean mustBePresent;

    private static Binder<Node> binder;
    
    private static XPathFactory xpathFactory = XPathFactory.newInstance();
    private XPath xpath = xpathFactory.newXPath();

    public final static String GET_POLICY_DEFAULTS = "getPolicyDefaults";
    public final static String GET_POLICYSET_DEFAULTS = "getPolicySetDefaults";

    private static JAXBException initializeException;
    static {
        JAXBContext jaxb;
        try {
            jaxb = JAXBContext.newInstance(RequestType.class);
            binder = jaxb.createBinder();
        }
        catch (JAXBException e) {
            initializeException = e;
        }
    }

    public AttributeSelectorEvaluator(Object selector) throws JAXBException {
        if (initializeException != null) {
            throw initializeException;
        }

        this.attrSelector = (AttributeSelectorType)selector;
        initialize();
    }

    private void initialize() {
        this.dataType = attrSelector.getDataType();
        this.contextPath = attrSelector.getRequestContextPath();
        this.mustBePresent = attrSelector.isMustBePresent();
    }

    @Override
    public Object evaluate(EvaluationContext ctx) throws IndeterminateException {
        // 1st - getAttributeValuesFromRequest
        List<AttributeValueType> result = getAttributeValuesFromRequest(ctx, contextPath, dataType);
        if (result != null && result.size() > 0) {
            return result;
        }
        // 2nd - getAttributeValuesFromAttributeRetriever
        result = getAttributeValuesFromAttributeRetriever(ctx, contextPath, dataType);

        // Must be present?
        if ((result == null || result.size() == 0) && ctx.getPDP().supportMustBePresent() && mustBePresent) {
            // throw an IndeterminateException
            IndeterminateException ex = new IndeterminateException(
                    "The required attribute is missing : " + contextPath, Constants.STATUS_MISSINGATTRIBUTE);
            // Return an IndeterminateException with an array of MissingAttributeDetail. This array object will be
            // finally put into a Status object that includes in a Response.
            MissingAttributeDetailType missing = new MissingAttributeDetailType();
            missing.setAttributeId(this.contextPath);
            missing.setDataType(this.dataType);
            List<MissingAttributeDetailType> missingAttrs = new ArrayList<MissingAttributeDetailType>();
            missingAttrs.add(missing);

            ex.setAttachedObject(missingAttrs);
            throw ex;
        }

        return result;
    }

    private List<AttributeValueType> getAttributeValuesFromRequest(EvaluationContext ctx, String requestCtxPath, String dataType)
    throws IndeterminateException {
        List<AttributeValueType> result = new ArrayList<AttributeValueType>();
        NodeList nList = null;

        try {
            Object root = ctx.getCurrentEvaluatingPolicy();
            if (root != null) {
                isPolicyXPathVersionSupported(root);
                nList = (NodeList)xpath.evaluate(
                        requestCtxPath, getXmlNodeFromRequest(ctx), XPathConstants.NODESET);

                for (int i = 0; i < nList.getLength(); i ++) {
                    Node node = nList.item(i);
                    short nodeType = node.getNodeType();
                    if (nodeType == Node.TEXT_NODE || nodeType == Node.ATTRIBUTE_NODE || 
                        nodeType == Node.PROCESSING_INSTRUCTION_NODE || nodeType == Node.COMMENT_NODE) {
                        result.add(EngineObjectFactory.createAttributeValue(dataType, node.getNodeValue()));
                    }
                    else {
                        // throw an IndeterminateException of syntax-error
                        throw new IndeterminateException("The node selected by specfied XPath expression is not one of "
                        + "following - a text node, an attribute node, a processing instruction node or a comment node.", 
                        Constants.STATUS_SYNTAXERROR);
                    }
                }

                return result;
            }
            else {
                throw new IndeterminateException("The root element is NULL, or is NOT a Policy or PolicySet object.",
                        Constants.STATUS_SYNTAXERROR);
            }
        }
        catch (IndeterminateException ie) {
            throw ie;
        }
        catch (XPathExpressionException xe) {
            // throw an IndeterminateException of syntax-error
            throw new IndeterminateException("Error occurs when evaluating xpath expression against request", xe, 
                    Constants.STATUS_PROCESSINGERROR);
        }
        catch (XMLDataTypeMappingException dmEx) {
            // throw an IndeterminateException of syntax-error
            throw new IndeterminateException("Error occurs when initialize an AttributeValue object", dmEx, 
                    Constants.STATUS_SYNTAXERROR);
        }
        catch (Exception t) {
            throw new IndeterminateException("Error occurs while evaluating AttributeSelector.", t, 
                    Constants.STATUS_SYNTAXERROR);
        }
    }

    public static boolean isPolicyXPathVersionSupported(Object policy) throws IndeterminateException {
        String name = null;
        if (policy instanceof PolicyType) {
            name = GET_POLICY_DEFAULTS;
        }
        else {
            name = GET_POLICYSET_DEFAULTS;
        }

        Method getPolicyDefaults;
        try {
            getPolicyDefaults = policy.getClass().getMethod(name);
            DefaultsType defaults = (DefaultsType)getPolicyDefaults.invoke(policy);

            if (defaults != null) {
                String xpathVer = defaults.getXPathVersion();
                if (xpathVer != null) {
                    for (int i = 0; i < SUPPORTED_XPATH_VERSIONS.length; i ++) {
                        if (xpathVer.equals(SUPPORTED_XPATH_VERSIONS[i])) {
                            return true;
                        }
                    }
                }
            }
            else {
                throw new IndeterminateException("The required XPathVersion is not configured in parent Policy or " +
                        "PolicySet element.", Constants.STATUS_SYNTAXERROR);
            }

            return false;
        }
        catch (IndeterminateException iEx) {
            throw iEx;
        }
        catch (Exception e) {
            throw new IndeterminateException(
                    "Error occurs while getting Defaults from policy or policySet.", e, Constants.STATUS_SYNTAXERROR);
        }
    }

    private Node getXmlNodeFromRequest(EvaluationContext ctx) throws ConfigurationException, JAXBException {
        Node reqNode = ctx.getRequest().getXmlNode();
        if (reqNode == null) {
            reqNode = binder.getXMLNode(ctx.getRequest());
            ctx.getRequest().setXmlNode(reqNode);
        }

        return reqNode;
    }

    public List<AttributeValueType> getAttributeValuesFromAttributeRetriever(
            EvaluationContext ctx, String requestCtxPath, String dataType) throws IndeterminateException {
        List<AttributeValueType> result = new ArrayList<AttributeValueType>();
        try {
            AttributeRetrieverRegistry reg = AttributeRetrieverRegistry.getInstance(ctx.getPDP());
            Map<String, String> mappings = new HashMap<String, String>();

            /* FIXME where can we get these namespaces as we are currently using JAXB instead of XML parser
            // Additional Namespace mappings.
            mappings.putAll(policy.getPolicyNamespaceMappings());
            if (additionalNSMappings != null) {
                mappings.putAll(additionalNSMappings);
            }
            */

            List<AttributeRetriever> attrRetrs = reg.getAllAttributeRetrievers();
            for (AttributeRetriever attrRetr : attrRetrs) {
                result = attrRetr.retrieveAttributeValues(
                        ctx, requestCtxPath, dataType, ctx.getRequest().getXmlNode(), mappings);
                if (result != null && result.size() > 0) {
                    return result;
                }
            }

            return result;
        }
        catch (IndeterminateException intEx) {
            throw intEx;
        }
        catch (Exception e) {
            throw new IndeterminateException("There is error occurs during retrieve attributes from request.", e,
                    Constants.STATUS_SYNTAXERROR);
        }
    }
}
