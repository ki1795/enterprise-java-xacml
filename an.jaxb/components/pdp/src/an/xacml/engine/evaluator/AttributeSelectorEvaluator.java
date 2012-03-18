package an.xacml.engine.evaluator;

import static an.xacml.Constants.SUPPORTED_XPATH_VERSIONS;
import static an.xacml.Constants.PREFIX_ACTION;
import static an.xacml.Constants.PREFIX_ENVIRONMENT;
import static an.xacml.Constants.PREFIX_RESOURCE;
import static an.xacml.Constants.PREFIX_SUBJECT;
import static an.xacml.Constants.SUPPORTED_XPATH_VERSIONS;
import static an.xacml.engine.ctx.AttributeRetriever.ACTION;
import static an.xacml.engine.ctx.AttributeRetriever.ANY;
import static an.xacml.engine.ctx.AttributeRetriever.ENVIRONMENT;
import static an.xacml.engine.ctx.AttributeRetriever.RESOURCE;
import static an.xacml.engine.ctx.AttributeRetriever.SUBJECT;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import deprecated.an.xacml.policy.AbstractPolicy;
import deprecated.an.xacml.policy.AttributeValue;
import an.xacml.Constants;
import an.xacml.IndeterminateException;
import an.xacml.engine.ctx.AttributeRetriever;
import an.xacml.engine.ctx.AttributeRetrieverRegistry;
import an.xacml.engine.ctx.EvaluationContext;
import an.xml.XMLDataTypeMappingException;

public class AttributeSelectorEvaluator implements Evaluator {

    private static XPathFactory xpathFactory = XPathFactory.newInstance();
    private XPath xpath = xpathFactory.newXPath();

    @Override
    public Object evaluate(EvaluationContext ctx) throws IndeterminateException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * TODO should be moved to AttributeSelectorEvaluator class
     */
    private AttributeValue[] getAttributeValuesFromRequest(String requestCtxPath, URI dataType)
    throws IndeterminateException {
        Vector<AttributeValue> result = new Vector<AttributeValue>();
        NodeList nList = null;

        try {
            AbstractPolicy root = getCurrentEvaluatingPolicy();
            if (root != null) {
                isPolicyXPathVersionSupported(root);
                nList = (NodeList)xpath.evaluate(
                        requestCtxPath, getRequest().getRootNode(), XPathConstants.NODESET);

                for (int i = 0; i < nList.getLength(); i ++) {
                    Node node = nList.item(i);
                    short nodeType = node.getNodeType();
                    if (nodeType == Node.TEXT_NODE || nodeType == Node.ATTRIBUTE_NODE || 
                        nodeType == Node.PROCESSING_INSTRUCTION_NODE || nodeType == Node.COMMENT_NODE) {
                        result.add(AttributeValue.getInstance(dataType, node.getNodeValue()));
                    }
                    else {
                        // throw an IndeterminateException of syntax-error
                        throw new IndeterminateException("The node selected by specfied XPath expression is not one of "
                        + "following - a text node, an attribute node, a processing instruction node or a comment node.", 
                        Constants.STATUS_SYNTAXERROR);
                    }
                }

                return result.toArray(new AttributeValue[0]);
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

    /**
     * TODO should be moved to AttributeSelectorEvaluator class
     */
    public static boolean isPolicyXPathVersionSupported(AbstractPolicy policy) throws IndeterminateException {
        URI xpathVer = policy.getXPathVersion();
        if (xpathVer != null) {
            String strVal = xpathVer.toString();
            for (int i = 0; i < SUPPORTED_XPATH_VERSIONS.length; i ++) {
                if (strVal.equals(SUPPORTED_XPATH_VERSIONS[i])) {
                    return true;
                }
            }
        }
        else {
            throw new IndeterminateException("The required XPathVersion is not configured in parent Policy or " +
                    "PolicySet element.", Constants.STATUS_SYNTAXERROR);
        }

        return false;
    }

    /**
     * TODO should be moved to AttributeSelectorEvaluator class
     */
    public AttributeValue[] getAttributeValues(String requestCtxPath, URI dataType) throws IndeterminateException {
        try {
            // First trying to get attributes from request
            AttributeValue[] result = getAttributeValuesFromRequest(requestCtxPath, dataType);
            // If no attribute got, we will try to get from attribute retrievers.
            if (result == null || result.length == 0) {
                AttributeRetrieverRegistry reg = AttributeRetrieverRegistry.getInstance(policy.getOwnerPDP());
                Map<String, String> mappings = new HashMap<String, String>();
                // Additional Namespace mappings.
                mappings.putAll(policy.getPolicyNamespaceMappings());
                if (additionalNSMappings != null) {
                    mappings.putAll(additionalNSMappings);
                }

                AttributeRetriever[] attrRetrs = reg.getAllAttributeRetrievers();
                for (AttributeRetriever attrRetr : attrRetrs) {
                    result = attrRetr.retrieveAttributeValues(
                            this, requestCtxPath, dataType, getRequest().getRootNode(), mappings);
                    if (result != null && result.length > 0) {
                        return result;
                    }
                }
            }
            return result != null ? result : new AttributeValue[0];
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
