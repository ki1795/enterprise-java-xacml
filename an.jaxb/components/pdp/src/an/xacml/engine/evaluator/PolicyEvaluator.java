package an.xacml.engine.evaluator;

import static oasis.names.tc.xacml._2_0.context.schema.os.DecisionType.DENY;
import static oasis.names.tc.xacml._2_0.context.schema.os.DecisionType.PERMIT;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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
            // update the variable definitions to the context.
            Map<String, VariableDefinitionType> ctxVarDefs = ctx.getVariableDefinitions();
            for (VariableDefinitionType varDef : variableDefs) {
                ctxVarDefs.put(varDef.getVariableId(), varDef);
            }

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
                    appendPolicyObligationsToResult(ruleResult, obligations, ctx);
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

    protected static void appendPolicyObligationsToResult(
            ResultType result, ObligationsType obls, EvaluationContext ctx) {

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
