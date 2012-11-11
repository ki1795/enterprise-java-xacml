package an.xacml.evaluator;

import oasis.names.tc.xacml._2_0.policy.schema.os.AttributeValueType;
import an.xacml.engine.IndeterminateException;
import an.xacml.engine.impl.EvaluationContext;

public class AttributeValueEvaluator implements Evaluator {

    private AttributeValueType attributeValue;

    public AttributeValueEvaluator(Object attributeValue) {
        this.attributeValue = (AttributeValueType)attributeValue;
    }

    @Override
    /**
     * Evaluate AttributeValueType object, NOT evaluate its inner expression (it's responsible of PEP). The actual
     * value of AttributeValueType has been determined while it's construct, so this evaluate method just need to
     * return itself.
     * 
     * We provided the ability at the point (PEP) to launch another evaluation with the same context.
     */
    public Object evaluate(EvaluationContext ctx) throws IndeterminateException {
        return attributeValue;
    }
}
