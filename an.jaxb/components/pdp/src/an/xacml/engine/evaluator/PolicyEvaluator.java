package an.xacml.engine.evaluator;

import static deprecated.an.xacml.context.Decision.Deny;
import static deprecated.an.xacml.context.Decision.Permit;

import java.lang.reflect.InvocationTargetException;

import deprecated.an.xacml.context.Result;
import oasis.names.tc.xacml._2_0.policy.schema.os.PolicyType;
import an.xacml.Constants;
import an.xacml.IndeterminateException;
import an.xacml.engine.ctx.EvaluationContext;
import an.xacml.engine.ctx.FunctionRegistry;
import an.xacml.function.BuiltInFunction;

public class PolicyEvaluator implements Evaluator {

    private PolicyType policy;

    /**
     * This constructor is required for getting the corresponding evaluator from the factory class. 
     * @param policy The passed in policy which will be evaluated.
     */
    public PolicyEvaluator(Object policy) {
        this.policy = (PolicyType)policy;
    }

    @Override
    public Object evaluate(EvaluationContext ctx) throws IndeterminateException {
        try {
            ctx.setCurrentEvaluatingPolicy(policy);

            if (target.match(ctx) && rules != null && rules.length > 0) {
                // Get rule-combine-alg function from function registry, and then pass rules, combinerParams and 
                // RuleCombinerParams to it, get the EvaluationResult
                FunctionRegistry functionReg = FunctionRegistry.getInstance();
                BuiltInFunction ruleCombAlg = functionReg.lookup(ruleCombiningAlgId);

                Result ruleResult =(Result)ruleCombAlg.invoke(ctx,
                        new Object[] {rules, combinerParameters, ruleCombinerParameters});
                // Retrieve the corresponding Obligations by Effect in EvaluationResult, and set it to EvaluationResult.
                if ((ruleResult.getDecision() == Permit || ruleResult.getDecision() == Deny) && obligations != null) {
                    // Clone the result
                    ruleResult = new Result(ruleResult);
                    appendPolicyObligationsToResult(ruleResult, obligations, ctx, supportInnerExpression());
                }
                return ruleResult;
            }
            // NotApplicable
            return Result.NOTAPPLICABLE;
        }
        catch (IndeterminateException ex) {
            throw ex;
        }
        catch (Throwable t) {
            if (t instanceof InvocationTargetException) {
                Throwable targetT = ((InvocationTargetException)t).getTargetException();
                if (targetT instanceof IndeterminateException) {
                    throw (IndeterminateException)targetT;
                }
            }
            throw new IndeterminateException("Error occurs while evaluating Policy.", t, Constants.STATUS_SYNTAXERROR);
        }
    }
}
