package an.xacml.engine.evaluator;

import static oasis.names.tc.xacml._2_0.context.schema.os.DecisionType.DENY;
import static oasis.names.tc.xacml._2_0.context.schema.os.DecisionType.PERMIT;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oasis.names.tc.xacml._2_0.context.schema.os.ResultType;
import oasis.names.tc.xacml._2_0.policy.schema.os.CombinerParametersType;
import oasis.names.tc.xacml._2_0.policy.schema.os.ObligationType;
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
    private Map<String, VariableDefinitionType> variableDefs = new HashMap<String, VariableDefinitionType>();
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
        Object previousPolicy = null;
        try {
            previousPolicy = ctx.setCurrentEvaluatingPolicy(policy);
            // check the ruleCombinerParameters is valid or not
            validateRuleCombinerParameters();
            // update the variable definitions to the context.
            ctx.defineVariables(variableDefs);

            if (EvaluatorFactory.getInstance().getMatcher(target).match(ctx) && rules.size() > 0) {
                // Get rule-combine-alg function from function registry, and then pass rules, combinerParams and 
                // RuleCombinerParams to it, get the EvaluationResult
                FunctionRegistry functionReg = FunctionRegistry.getInstance();
                BuiltInFunction ruleCombAlg = functionReg.lookup(ruleCombiningAlgId);

                ResultType ruleResult =(ResultType)ruleCombAlg.invoke(ctx,
                        new Object[] {rules.toArray(new RuleType[0]), combinerParameters,
                                      ruleCombinerParameters.toArray(new RuleCombinerParametersType[0])});
                // Retrieve the corresponding Obligations by Effect in EvaluationResult, and set it to EvaluationResult.
                if ((ruleResult.getDecision() == PERMIT || ruleResult.getDecision() == DENY) && obligations != null) {
                    // Clone the result
                    ruleResult = new ResultType(ruleResult);
                    appendPolicyObligationsToResult(ruleResult, obligations);
                }
                return ruleResult;
            }
            // NotApplicable
            return ResultType.NOTAPPLICABLE;
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
        finally {
            // release the variableDefinition from current policy scope
            ctx.releaseVariables();
            // set the current evaluating policy back to the up level policy.
            ctx.setCurrentEvaluatingPolicy(previousPolicy);
        }
    }

    private void validateRuleCombinerParameters() throws IndeterminateException {
        for (RuleCombinerParametersType ruleParams : ruleCombinerParameters) {
            for (RuleType rule : rules) {
                if (ruleParams.getRuleIdRef().equals(rule.getRuleId())) {
                    continue;
                }
            }
            throw new IndeterminateException("The RuleCombinerParameters doesn't have a matched rule id : " +
                                             ruleParams.getRuleIdRef(), Constants.STATUS_SYNTAXERROR);
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
            // extract rules
        	if (o instanceof RuleType) {
        		rules.add((RuleType)o);
        	}
        	// extract and merge parameters
        	else if (o instanceof CombinerParametersType) {
        		if (combinerParameters == null) {
        			combinerParameters = (CombinerParametersType)o;
        		}
        		// merge all combiner parameters to a single one.
        		else {
        			combinerParameters.getCombinerParameter().addAll(((CombinerParametersType)o).getCombinerParameter());
        		}
        	}
        	// extract and merge rule parameters according to rule id
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
        		    // we will check the rule id that the ruleCombinerParameters specified is match an existing rule or not in
        		    // evaluate method
        			ruleCombinerParameters.add((RuleCombinerParametersType)o);
        		}
        	}
        	else if (o instanceof VariableDefinitionType) {
        	    VariableDefinitionType varDef = (VariableDefinitionType)o;
        		variableDefs.put(varDef.getVariableId(), varDef);
        	}
        }
    }

    protected static void appendPolicyObligationsToResult(ResultType result, ObligationsType obls) {

        List<ObligationType> list = obls.getObligation();
        String decision = result.getDecision().value();
        if (result.getObligations() == null) {
            result.setObligations(new ObligationsType());
        }
        List<ObligationType> resultObls = result.getObligations().getObligation();

        for (ObligationType obl : list) {
            // It's should be ok we use "==" not use equals
            if (obl.getFulfillOn().value() == decision) {
                // add a clone one.
                resultObls.add(new ObligationType(obl));
            }
        }
    }
}
