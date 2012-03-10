package an.xacml.engine.evaluator;

import java.lang.reflect.Array;

import deprecated.an.xacml.policy.AttributeValue;
import oasis.names.tc.xacml._2_0.policy.schema.os.AttributeValueType;
import an.xacml.Constants;
import an.xacml.IndeterminateException;
import an.xacml.engine.ctx.EvaluationContext;

public class AttributeValueEvaluator implements Evaluator {

    private AttributeValueType attributeValue;

    public AttributeValueEvaluator(Object attributeValue) {
        this.attributeValue = (AttributeValueType)attributeValue;
    }

    @Override
    /**
     * Evaluate AttributeValueType object, including its inner expression if PDP support inner expression and set the
     * evaluation result back to the AttributeValueType object. And then return itself.
     * 
     * FIXME If an AttributeValueType including an inner expression, should it be mapped to a Java object by JAXB? or
     * just as String representation in its content?
     * FIXME Seems we should not support the inner expression? Or we need lanuch another context to evaluate the child
     * expression? - We may provide the ability at the point (PEP) to launch another evaluation with the same context.
     */
    public Object evaluate(EvaluationContext ctx) throws IndeterminateException {
        // First evaluate child expression.
        if (supportInnerExpression() && this.childExp != null) {
            Object result = childExp.evaluate(ctx);
            AttributeValue finalResult = null;
            if (result.getClass().isArray()) {
                // We expected a single AttributeValue instance.
                if (Array.getLength(result) == 1) {
                    finalResult = ((AttributeValue[])result)[0];
                }
                // If multiple AttributeValue instances are returned, we will throw an IndeterminateException.
                else {
                    throw new IndeterminateException("The child expression returned more than one AttributeValue.",
                            Constants.STATUS_SYNTAXERROR);
                }
            }
            else {
                finalResult = (AttributeValue)result;
            }
            // If the child expression's data type does not match the parents' one, we will throw an exception.
            if (!finalResult.getDataType().equals(this.dataType)) {
                throw new IndeterminateException("The child expression's data type '" + finalResult.getDataType() + 
                        "' doesn't match the parents' data type '" + dataType + "'.", Constants.STATUS_SYNTAXERROR);
            }
            return finalResult;
        }
        // If no child expression, we will return this.
        else {
            return this;
        }
        // TODO Auto-generated method stub
        return null;
    }

    private boolean supportInnerExpression() {
        /*
        XACMLElement root = getRootElement();
        if (root instanceof AbstractPolicy) {
            PDP pdp = ((AbstractPolicy)root).getOwnerPDP();
            if (pdp != null) {
                return pdp.supportInnerExpression();
            }
        }
        // If we are in response, or we are not in PDP, then PEP should in charge of evaluate it.*/
        // TODO
        return false;
    }
}
