package oasis.names.tc.xacml._2_0.policy.schema.os;

import static an.xml.XMLDataTypeRegistry.getJavaType;
import static an.xml.XMLDataTypeRegistry.getTypedValue;
import an.xacml.Constants;
import an.xml.XMLDataTypeMappingException;

/**
 * The EngineObjectFactory is designed for construct engine objects (only responsible for objects in policy schema
 * scope) which we don't want user construct them from default constructors - for performance consideration or other
 * reasons.
 * 
 * Currently we only have AttributeValueType need to be taken care.
 */
public class EngineObjectFactory {

    /**
     * Create the AttributeValueType instance from a String value and a given XML type. This method will check the
     * conversion from the String value to the Java typed value.
     * @param xmlType
     * @param value
     * @return
     * @throws XMLDataTypeMappingException Throws if the String value can not be converted to the Java typed value.
     */
    public static AttributeValueType createAttributeValue(String xmlType, String value) throws XMLDataTypeMappingException {
        checkNull(xmlType, value);

        // To not create a new instance for TRUE and FALSE - performance consideration
        if (xmlType.equals(Constants.TYPE_BOOLEAN) && value != null) {
            if (value.equalsIgnoreCase("true")) {
                return AttributeValueType.TRUE;
            }
            else if (value.equalsIgnoreCase("false")) {
                return AttributeValueType.FALSE;
            }
        }

        // Trying to convert to Java type and return the new instance.
        return new AttributeValueType(xmlType, getTypedValue(getJavaType(xmlType), value));
    }

    /**
     * Create the AttributeValueType instance from a typed value and a given XML type. This method will check the
     * if the typed value's type is match the given XML type.
     * @param xmlType
     * @param value
     * @return
     * @throws XMLDataTypeMappingException Throws if the typed value's type doesn't match the given XML type.
     */
    public static AttributeValueType createAttributeValue(String xmlType, Object value) throws XMLDataTypeMappingException {
        checkNull(xmlType, value);

        // To not create a new instance for TRUE and FALSE - performance consideration
        if (xmlType.equals(Constants.TYPE_BOOLEAN) && value != null && value instanceof Boolean) {
            if (value.equals(Boolean.TRUE)) {
                return AttributeValueType.TRUE;
            }
            else if (value.equals(Boolean.FALSE)) {
                return AttributeValueType.FALSE;
            }
        }

        Class<?> expectedType = getJavaType(xmlType);
        Class<?> actualType = value.getClass();
        if (!expectedType.isAssignableFrom(actualType)) {
            throw new XMLDataTypeMappingException(
                    "The attribute value's type '" + actualType.getName() +
                    "' doesn't match the XMLSchema data type '" + expectedType.getName() + "'.");
        }
        return new AttributeValueType(xmlType, value);
    }

    private static void checkNull(String xmlType, Object value) throws XMLDataTypeMappingException {
        if (xmlType == null) {
            throw new XMLDataTypeMappingException("The given XML type is NULL.");
        }
        if (value == null) {
            throw new XMLDataTypeMappingException("The given value is NULL.");
        }
    }
}
