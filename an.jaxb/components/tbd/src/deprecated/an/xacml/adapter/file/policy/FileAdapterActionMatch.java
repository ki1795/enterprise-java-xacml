package deprecated.an.xacml.adapter.file.policy;

import org.w3c.dom.Element;

import deprecated.an.xacml.policy.ActionMatch;

import an.xacml.XACMLElement;

public class FileAdapterActionMatch extends FileAdapterTargetElementMatch {
    public static final String ELEMENT_NAME = "ActionMatch";
    public FileAdapterActionMatch(Element elem) throws Exception {
        initializeTargetElement(elem, ActionMatch.class);
    }

    public FileAdapterActionMatch(XACMLElement engineElem) throws Exception {
        if (engineElem.getElementName() == null) {
            engineElem.setElementName(ELEMENT_NAME);
        }
        initializeTargetElement(engineElem);
    }
}