package deprecateed.an.xacml.adapter.file.context;

import org.w3c.dom.Element;

import deprecated.an.xacml.context.Action;
import deprecated.an.xacml.context.TargetElement;

import an.xacml.PolicySyntaxException;
import an.xacml.XACMLElement;

public class FileAdapterAction extends FileAdapterTargetElement {
    /**
    <xs:element name="Action" type="xacml-context:ActionType"/>
    <xs:complexType name="ActionType">
        <xs:sequence>
            <xs:element ref="xacml-context:Attribute" minOccurs="0" maxOccurs="unbounded"/>
        </xs:sequence>
    </xs:complexType>
     */
	public static final String ELEMENT_NAME = "Action";
    public FileAdapterAction(Element elem) throws PolicySyntaxException {
        initialize(elem);
        engineElem = new Action(extractAttributes());
        engineElem.setElementName(elem.getLocalName());
    }

    public FileAdapterAction(XACMLElement engineElem) throws Exception {
        this.engineElem = engineElem;

        if (this.engineElem.getElementName() == null) {
            this.engineElem.setElementName(ELEMENT_NAME);
        }
        xmlElement = createContextElement();
        populateAttributes((TargetElement)engineElem);
    }
}