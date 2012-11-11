package deprecated.an.xacml.adapter.file.policy;

import org.w3c.dom.Element;

import deprecated.an.xacml.policy.Action;

import an.xacml.XACMLElement;

public class FileAdapterAction extends FileAdapterTargetElement {
    public static final String ELEMENT_NAME = "Action";
    public FileAdapterAction(Element elem) throws Exception {
        initializeTargetElement(elem, Action.class);
    }

    public FileAdapterAction(XACMLElement elem) throws Exception {
        if (elem.getElementName() == null) {
            elem.setElementName(ELEMENT_NAME);
        }
        initializeTargetElement(elem);
    }
}