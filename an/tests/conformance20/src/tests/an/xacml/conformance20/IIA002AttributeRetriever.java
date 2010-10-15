package tests.an.xacml.conformance20;

import java.net.URI;
import java.util.Map;

import org.w3c.dom.Element;

import an.config.ConfigElement;
import an.xacml.IndeterminateException;
import an.xacml.engine.AttributeRetriever;
import an.xacml.engine.EvaluationContext;
import an.xacml.policy.AttributeValue;
import an.xml.XMLDataTypeMappingException;

public class IIA002AttributeRetriever implements AttributeRetriever {
    public IIA002AttributeRetriever(ConfigElement config) {}

    public int getType() {return 0;}

    public boolean isAttributeSupported(URI attrId, URI dataType) {
        if (attrId.toString().equals("urn:oasis:names:tc:xacml:1.0:example:attribute:role")) {
            return true;
        }
        return false;
    }

    public AttributeValue[] retrieveAttributeValues(EvaluationContext context, URI attrId, URI dataType, String issuer,
            URI subjCategory) throws IndeterminateException {
        try {
            if (attrId.toString().equals("urn:oasis:names:tc:xacml:1.0:example:attribute:role")) {
                return new AttributeValue[] {AttributeValue.getInstance(dataType, "Physician")};
            }
            return null;
        } catch (XMLDataTypeMappingException e) {
            throw new IndeterminateException("Error occurs while retrieving attribute : " + attrId, e);
        }
    }

    public AttributeValue[] retrieveAttributeValues(EvaluationContext context, String requestCtxPath, URI dataType,
            Element request, Map<String, String> additionalNSMappings) throws IndeterminateException {
        return null;
    }
}