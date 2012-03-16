package an.xacml.engine.evaluator;

import java.lang.reflect.InvocationTargetException;

import oasis.names.tc.xacml._2_0.policy.schema.os.AttributeValueType;
import oasis.names.tc.xacml._2_0.policy.schema.os.ConditionType;
import an.xacml.Constants;
import an.xacml.IndeterminateException;
import an.xacml.engine.ctx.EvaluationContext;

public class ConditionEvaluator implements Evaluator {

    private ConditionType condition;
    private Object expression;

    public ConditionEvaluator(Object condition) {
        this.condition = (ConditionType)condition;
        this.expression = this.condition.getExpression().getValue();
    }

    /**
     * Condition evaluation result will be boolean value TRUE or FALSE, not like other evaluatable - return an AttributeValue.
     */
    @Override
    public Object evaluate(EvaluationContext ctx) throws IndeterminateException {
        try {
            Object result = null;
            Object condVal = EvaluatorFactory.getInstance().getEvaluator(expression).evaluate(ctx);
            if (condVal != null && condVal instanceof AttributeValueType) {
                result = ((AttributeValueType)condVal).getValue();
                if (result != null && result instanceof Boolean) {
                    return (Boolean)result;
                }
            }
            throw new IndeterminateException("Expect a Boolean value, but got a " +
                    (result == null ? " null value." : result.getClass().getSimpleName() + " value : " + result),
                    Constants.STATUS_PROCESSINGERROR);
        }
        catch (IndeterminateException iEx) {
            throw iEx;
        }
        catch (Exception t) {
            if (t instanceof InvocationTargetException) {
                Throwable targetT = ((InvocationTargetException)t).getTargetException();
                if (targetT instanceof IndeterminateException) {
                    throw (IndeterminateException)targetT;
                }
            }
            throw new IndeterminateException("Error occurs while evaluating Condition.", t, Constants.STATUS_SYNTAXERROR);
        }
    }

}
