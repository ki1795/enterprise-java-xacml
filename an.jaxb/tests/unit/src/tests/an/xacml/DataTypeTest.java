package tests.an.xacml;

import static an.xml.XMLDataTypeRegistry.XMLTypeAsQName;
import static an.xml.XMLDataTypeRegistry.XML_ANYURI;
import static an.xml.XMLDataTypeRegistry.XML_BOOLEAN;
import static an.xml.XMLDataTypeRegistry.XML_DOUBLE;
import static an.xml.XMLDataTypeRegistry.XML_INT;
import static an.xml.XMLDataTypeRegistry.XML_INTEGER;
import static an.xml.XMLDataTypeRegistry.XML_LONG;
import static an.xml.XMLDataTypeRegistry.XML_STRING;
import static an.xml.XMLDataTypeRegistry.getJavaType;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static javax.xml.datatype.DatatypeConstants.DATE;
import static javax.xml.datatype.DatatypeConstants.DATETIME;
import static javax.xml.datatype.DatatypeConstants.TIME;
import static org.junit.Assert.assertTrue;

import javax.naming.ldap.LdapName;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.junit.Test;

import an.datatype.base64Binary;
import an.datatype.dnsName;
import an.datatype.hexBinary;
import an.datatype.ipAddress;
import an.datatype.rfc822Name;
import an.xacml.Constants;
import an.xacml.XACMLElement;
import an.xml.XMLDataTypeMappingException;

public class DataTypeTest {
    @Test public void testDataTypeRegGetJavaTypeByXML() throws XMLDataTypeMappingException {
        assertTrue("Mapping " + XML_STRING + " fail.", getJavaType(new QName(W3C_XML_SCHEMA_NS_URI, XML_STRING)).equals(java.lang.String.class));
        assertTrue("Mapping " + XML_BOOLEAN + " fail.", getJavaType(new QName(W3C_XML_SCHEMA_NS_URI, XML_BOOLEAN)).equals(Boolean.class));
        assertTrue("Mapping " + XML_INTEGER + " fail.", getJavaType(new QName(W3C_XML_SCHEMA_NS_URI, XML_INTEGER)).equals(java.math.BigInteger.class));
        assertTrue("Mapping " + XML_DOUBLE + " fail.", getJavaType(new QName(W3C_XML_SCHEMA_NS_URI, XML_DOUBLE)).equals(java.math.BigDecimal.class));
        assertTrue("Mapping " + XML_INT + " fail.", getJavaType(new QName(W3C_XML_SCHEMA_NS_URI, XML_INT)).equals(Integer.class));
        assertTrue("Mapping " + XML_LONG + " fail.", getJavaType(new QName(W3C_XML_SCHEMA_NS_URI, XML_LONG)).equals(Long.class));
        assertTrue("Mapping " + XML_ANYURI + " fail.", getJavaType(new QName(W3C_XML_SCHEMA_NS_URI, XML_ANYURI)).equals(java.net.URI.class));
        assertTrue("Mapping " + XACMLElement.XML_HEXBINARY + " fail.", getJavaType(new QName(W3C_XML_SCHEMA_NS_URI, XACMLElement.XML_HEXBINARY)).equals(hexBinary.class));
        assertTrue("Mapping " + XACMLElement.XML_BASE64BINARY + " fail.", getJavaType(new QName(W3C_XML_SCHEMA_NS_URI, XACMLElement.XML_BASE64BINARY)).equals(base64Binary.class));
        assertTrue("Mapping " + Constants.TYPE_X500NAME + " fail.", getJavaType(Constants.TYPE_X500NAME.toString()).equals(LdapName.class));
        assertTrue("Mapping " + Constants.TYPE_RFC822NAME + " fail.", getJavaType(Constants.TYPE_RFC822NAME.toString()).equals(rfc822Name.class));
        assertTrue("Mapping " + Constants.TYPE_IPADDRESS + " fail.", getJavaType(Constants.TYPE_IPADDRESS.toString()).equals(ipAddress.class));
        assertTrue("Mapping " + Constants.TYPE_DNSNAME + " fail.", getJavaType(Constants.TYPE_DNSNAME.toString()).equals(dnsName.class));
        // TODO add test of dayTimeDuration & yearMonthDuration
        assertTrue("Mapping " + TIME + " fail.", getJavaType(TIME).equals(XMLGregorianCalendar.class));
        assertTrue("Mapping " + DATE + " fail.", getJavaType(DATE).equals(XMLGregorianCalendar.class));
        assertTrue("Mapping " + DATETIME + " fail.", getJavaType(DATETIME).equals(XMLGregorianCalendar.class));
    }

    @Test public void testXMLTypeAsQNameNULL() {
        assertTrue("XMLTypeAsQName(null) returns not NULL.", XMLTypeAsQName(null) == null);
    }

    @Test public void testXMLTypeAsQNameSharp() {
        assertTrue("XMLTypeAsQName(\"ns#local\") returns " + XMLTypeAsQName("ns#local"),
                XMLTypeAsQName("ns#local").equals(new QName("ns", "local")));
    }

    @Test public void testXMLTypeAsQNameNULLNameSpace() {
        assertTrue("XMLTypeAsQName(\"local\") returns " + XMLTypeAsQName("local"),
                XMLTypeAsQName("local").equals(new QName("local")));
    }
}