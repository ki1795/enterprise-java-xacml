package an.xacml.engine.evaluator;

import static deprecated.an.xacml.context.Decision.Deny;
import static deprecated.an.xacml.context.Decision.Permit;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import deprecated.an.xacml.context.Result;
import oasis.names.tc.xacml._2_0.policy.schema.os.CombinerParametersType;
import oasis.names.tc.xacml._2_0.policy.schema.os.ObligationsType;
import oasis.names.tc.xacml._2_0.policy.schema.os.PolicyType;
import oasis.names.tc.xacml._2_0.policy.schema.os.RuleCombinerParametersType;
import oasis.names.tc.xacml._2_0.policy.schema.os.RuleType;
import oasis.names.tc.xacml._2_0.policy.schema.os.TargetType;
import oasis.names.tc.xacml._2_0.policy.schema.os.VariableDefinitionType;
import an.xacml.Constants;
import an.xacml.IndeterminateException;
import an.xacml.engine.ctx.EvaluationContext;
import an.xacml.engine.ctx.FunctionRegistry;
import an.xacml.function.BuiltInFunction;

public class PolicyEvaluator implements Evaluator {

    private PolicyType policy;

    // following fields are extracted from policy property while constructing the evaluator.
    private TargetType target;
    private List<RuleType> rules = new ArrayList<RuleType>();
    private String ruleCombiningAlgId;
    private CombinerParametersType combinerParameters;
    private List<RuleCombinerParametersType> ruleCombinerParameters = new ArrayList<RuleCombinerParametersType>();
    private List<VariableDefinitionType> variableDefs = new ArrayList<VariableDefinitionType>();
    private ObligationsType obligations;

    /**
     * This constructor is required for getting the corresponding evaluator from the factory class. 
     * @param policy The passed in policy which will be evaluated.
     */
    public PolicyEvaluator(Object policy) {
        this.policy = (PolicyType)policy;
        initialize();
    }

    @Override
    public Object evaluate(EvaluationContext ctx) throws IndeterminateException {
        try {
            ctx.setCurrentEvaluatingPolicy(policy);
            // TODO update the variable definitions to the context.

            //policy.get
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

    /**
     * Extract policy child elements and pre-process them.
     */
    private void initialize() {
        target = policy.getTarget();
        combinerParameters = policy.getCombinerParameters();
        ruleCombiningAlgId = policy.getRuleCombiningAlgId();
        obligations = policy.getObligations();

        List<Object> list = policy.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition();
        for (Object o : list) {
        	if (o instanceof RuleType) {
        		rules.add((RuleType)o);
        	}
        	else if (o instanceof CombinerParametersType) {
        		if (combinerParameters == null) {
        			combinerParameters = (CombinerParametersType)o;
        		}
        		// merge all combiner parameters to a single one.
        		else {
        			combinerParameters.getCombinerParameter().addAll(((CombinerParametersType)o).getCombinerParameter());
        		}
        	}
        	else if (o instanceof RuleCombinerParametersType) {
        		boolean merged = false;
        		for (RuleCombinerParametersType ruleParam : ruleCombinerParameters) {
        			// if there is existing rule combiner parameters with same rule id in the list, we will merge them
        			// together.
        			if (((RuleCombinerParametersType)o).getRuleIdRef().equals(ruleParam.getRuleIdRef())) {
        				ruleParam.getCombinerParameter().addAll(((RuleCombinerParametersType)o).getCombinerParameter());
        				merged = true;
        				break;
        			}
        		}
        		// add as a new parameter
        		if (!merged) {
        			ruleCombinerParameters.add((RuleCombinerParametersType)o);
        		}
        	}
        	else if (o instanceof VariableDefinitionType) {
        		variableDefs.add((VariableDefinitionType)o);
        	}
        }
    }
}
