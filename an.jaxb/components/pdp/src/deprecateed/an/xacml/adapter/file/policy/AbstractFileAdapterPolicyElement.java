package deprecateed.an.xacml.adapter.file.policy;

import static deprecateed.an.xacml.adapter.file.XMLFileDataAdapterRegistry.getPolicyDataAdapterClassByXMLType;
import deprecateed.an.xacml.adapter.DataAdapter;
import deprecateed.an.xacml.adapter.file.AbstractFileAdapterElement;

public abstract class AbstractFileAdapterPolicyElement extends AbstractFileAdapterElement implements DataAdapter {
    @Override
    protected Class<?> getElementClass(String elemType) {
    	Class<?> elemClz = getPolicyDataAdapterClassByXMLType(elemType);
        // If type is null, it should be a primitive XML type, we get the corresponding Java type from DataTypeRegistry.
        if (elemClz == null) {
        	getElementClassFromSystem(elemType);
        }
        return elemClz;
    }
}