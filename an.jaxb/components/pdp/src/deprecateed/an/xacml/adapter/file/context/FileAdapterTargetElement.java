package deprecateed.an.xacml.adapter.file.context;

import org.w3c.dom.Element;

import deprecated.an.xacml.context.Attribute;
import deprecated.an.xacml.context.TargetElement;
import deprecateed.an.xacml.adapter.DataAdapter;

import an.xml.XMLElement;

public abstract class FileAdapterTargetElement extends AbstractFileAdapterContextElement {
    protected Attribute[] extractAttributes() {
        XMLElement[] children = getChildElements();
        Attribute[] attrs = new Attribute[children.length];
        for (int i = 0; i < children.length; i ++) {
            attrs[i] = (Attribute)((DataAdapter)children[i]).getEngineElement();
        }
        return attrs;
    }

    protected void populateAttributes(TargetElement targetElem) throws Exception {
        Attribute[] allAttrs = targetElem.getAllAttributes();
        for (int i = 0; i < allAttrs.length; i ++) {
            xmlElement.appendChild((Element)new FileAdapterAttribute(allAttrs[i]).getDataStoreObject());
        }
    }
}