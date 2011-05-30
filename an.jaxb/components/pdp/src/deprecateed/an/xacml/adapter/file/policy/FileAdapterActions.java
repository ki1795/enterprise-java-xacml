package deprecateed.an.xacml.adapter.file.policy;

import org.w3c.dom.Element;

import deprecated.an.xacml.policy.Actions;

import an.xacml.XACMLElement;

public class FileAdapterActions extends FileAdapterTargetElements {
    public static final String ELEMENT_NAME = "Actions";
    public FileAdapterActions(Element elem) throws Exception {
        initializeTargetElement(elem, Actions.class);
    }

    public FileAdapterActions(XACMLElement engineElem) throws Exception {
        if (engineElem.getElementName() == null) {
            engineElem.setElementName(ELEMENT_NAME);
        }
        initializeTargetElement(engineElem);
    }
}