package an.xacml.evaluator;

import static oasis.names.tc.xacml._2_0.policy.schema.os.EffectType.PERMIT;

import java.lang.reflect.InvocationTargetException;

import oasis.names.tc.xacml._2_0.context.schema.os.ResultType;
import oasis.names.tc.xacml._2_0.policy.schema.os.ConditionType;
import oasis.names.tc.xacml._2_0.policy.schema.os.EffectType;
import oasis.names.tc.xacml._2_0.policy.schema.os.RuleType;
import oasis.names.tc.xacml._2_0.policy.schema.os.TargetType;
import an.xacml.Constants;
import an.xacml.engine.IndeterminateException;
import an.xacml.engine.impl.EvaluationContext;

public class RuleEvaluatorAndMatcher implements Evaluator, Matcher {

    private RuleType rule;
    private TargetType target;
    private ConditionType condition;
    private EffectType effect;

    public RuleEvaluatorAndMatcher(Object rule) {
        this.rule = (RuleType)rule;
        initialize();
    }

    @Override
    public Object evaluate(EvaluationContext ctx) throws IndeterminateException {
        try {
            if (target == null || EvaluatorFactory.getInstance().getMatcher(target).match(ctx)) {
                if (condition == null || (Boolean)EvaluatorFactory.getInstance().getEvaluator(condition).evaluate(ctx)) {
                    // return the rule's Effect to EvaluationResult
                    if (effect == PERMIT) {
                        return ResultType.PERMIT;
                    }
                    else {
                        return ResultType.DENY;
                    }
                }
                // Condition evaluate to "false", return a NotApplicable.
            }
            // return NotApplicable with a non-parent status-code. Parent policy or policySet should wrap the statusCode
            // as a child to their own EvaluationResult.
            return ResultType.NOTAPPLICABLE;
        }
        catch (IndeterminateException ex) {
            throw ex;
        }
        catch (Exception t) {
            if (t instanceof InvocationTargetException) {
                Throwable targetT = ((InvocationTargetException)t).getTargetException();
                if (targetT instanceof IndeterminateException) {
                    throw (IndeterminateException)targetT;
                }
            }
            throw new IndeterminateException("Error occurs while evaluating Rule.", t, Constants.STATUS_SYNTAXERROR);
        }
    }

    @Override
    public boolean match(EvaluationContext ctx) throws IndeterminateException {
        try {
            return EvaluatorFactory.getInstance().getMatcher(target).match(ctx);
        }
        catch (IndeterminateException e) {
            throw e;
        }
        catch (Exception t) {
            if (t instanceof InvocationTargetException) {
                Throwable targetT = ((InvocationTargetException)t).getTargetException();
                if (targetT instanceof IndeterminateException) {
                    throw (IndeterminateException)targetT;
                }
            }
            throw new IndeterminateException("The match operation failed due to error: ", t, Constants.STATUS_SYNTAXERROR);
        }
    }

    private void initialize() {
        target = rule.getTarget();
        condition = rule.getCondition();
        effect = rule.getEffect();
    }
}
