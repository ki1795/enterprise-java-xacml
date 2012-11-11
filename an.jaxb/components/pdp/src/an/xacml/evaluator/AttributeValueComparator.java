package an.xacml.evaluator;

import java.util.List;

import oasis.names.tc.xacml._2_0.policy.schema.os.AttributeValueType;

public class AttributeValueComparator implements Comparator {

    public AttributeValueComparator(Object a) {}

    @Override
    /**
     * We only compare equality of 2 AttributeType objects, if they are not equal, return -1.
     */
    public int compare(Object a, Object b) {
        if (a == b) {
            return 0;
        }
        // not null
        if (a != null && b != null && 
            // compare between same type
            a.getClass() == AttributeValueType.class &&
            b.getClass() == AttributeValueType.class &&
            // first compare data type. data type should not be null because XML schema has defined it's required.
            ((AttributeValueType)a).getDataType().equals(((AttributeValueType)b).getDataType())) {
            // compare content
            List<Object> aContent = ((AttributeValueType)a).getContent();
            List<Object> bContent = ((AttributeValueType)b).getContent();

            if (aContent.size() == bContent.size()) {
                for (int i = 0; i < aContent.size(); i ++) {
                    if (!aContent.get(i).equals(bContent.get(i))) {
                        return -1;
                    }
                }
                return 0;
            }
            // FIXME compare other attributes?
        }
        return -1;
    }
}
