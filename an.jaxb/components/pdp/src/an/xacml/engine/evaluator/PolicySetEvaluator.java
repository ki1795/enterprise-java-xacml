package an.xacml.engine.evaluator;

import static an.xacml.engine.evaluator.PolicyEvaluator.appendPolicyObligationsToResult;
import static deprecated.an.xacml.context.Decision.Deny;
import static deprecated.an.xacml.context.Decision.Permit;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._2_0.policy.schema.os.CombinerParametersType;
import oasis.names.tc.xacml._2_0.policy.schema.os.IdReferenceType;
import oasis.names.tc.xacml._2_0.policy.schema.os.ObligationsType;
import oasis.names.tc.xacml._2_0.policy.schema.os.PolicyCombinerParametersType;
import oasis.names.tc.xacml._2_0.policy.schema.os.PolicySetCombinerParametersType;
import oasis.names.tc.xacml._2_0.policy.schema.os.PolicySetType;
import oasis.names.tc.xacml._2_0.policy.schema.os.PolicyType;
import oasis.names.tc.xacml._2_0.policy.schema.os.RuleCombinerParametersType;
import oasis.names.tc.xacml._2_0.policy.schema.os.RuleType;
import oasis.names.tc.xacml._2_0.policy.schema.os.TargetType;
import oasis.names.tc.xacml._2_0.policy.schema.os.VariableDefinitionType;

import deprecated.an.xacml.context.Result;
import deprecated.an.xacml.policy.AbstractPolicy;
import deprecated.an.xacml.policy.IdReference;
import an.xacml.Constants;
import an.xacml.IndeterminateException;
import an.xacml.PolicySyntaxException;
import an.xacml.XACMLElement;
import an.xacml.engine.ctx.EvaluationContext;
import an.xacml.engine.ctx.FunctionRegistry;
import an.xacml.function.BuiltInFunction;

public class PolicySetEvaluator implements Evaluator {

    private PolicySetType policySet;

    // following fields are extracted from policy property while constructing the evaluator.
    private TargetType target;
    // this might include PolicyType or PolicySetType. Also include the resolved policies from IdReferenceType.
    private List<Object> policies = new ArrayList<Object>();
    private String policyCombiningAlgId;
    private CombinerParametersType combinerParameters;
    private List<PolicyCombinerParametersType> policyCombinerParameters = new ArrayList<PolicyCombinerParametersType>();
    private List<PolicySetCombinerParametersType> policySetCombinerParameters = new ArrayList<PolicySetCombinerParametersType>();
    private ObligationsType obligations;

    public PolicySetEvaluator(Object policySet) {
        this.policySet = (PolicySetType)policySet;
        initialize();
    }

    @Override
    public Object evaluate(EvaluationContext ctx) throws IndeterminateException {
        try {
            ctx.setCurrentEvaluatingPolicy(policySet);

            if (target.match(ctx)) {
                // We don't call the method from PolicySet's constructor, because referenced policies may not be loaded while
                // constructing the policySet.
                mergePolicies();
                if (allPolicies == null || allPolicies.length == 0) {
                    return Result.NOTAPPLICABLE;
                }

                // Get rule-combine-alg function from function registry, and then pass rules, combinerParams and 
                // RuleCombinerParams to it, get the EvaluationResult
                FunctionRegistry functionReg = FunctionRegistry.getInstance();
                BuiltInFunction policyCombAlg = functionReg.lookup(policyCombiningAlgId);

                Result policyResult =(Result)policyCombAlg.invoke(ctx, new Object[] {
                        allPolicies, combinerParameters, policyCombinerParameters, policySetCombinerParameters});
                // Retrieve the corresponding Obligations by Effect in EvaluationResult, and set it to EvaluationResult.
                if ((policyResult.getDecision() == Permit || policyResult.getDecision() == Deny) && obligations != null) {
                    if (policyResult == Result.PERMIT || policyResult == Result.DENY) {
                        policyResult = new Result(policyResult);
                    }
                    appendPolicyObligationsToResult(policyResult, obligations, ctx);
                }
                return policyResult;
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
            throw new IndeterminateException("Error occurs while evaluating PolicySet.", t,
                    Constants.STATUS_SYNTAXERROR);
        }
    }

    /**
     * Extract elements from policySet
     */
    private void initialize() {
        /*
        private List<Object> policies = new ArrayList<Object>();
        private CombinerParametersType combinerParameters;
        private List<PolicyCombinerParametersType> policyCombinerParameters = new ArrayList<PolicyCombinerParametersType>();
        private List<PolicySetCombinerParametersType> policySetCombinerParameters = new ArrayList<PolicySetCombinerParametersType>();
        */
        target = policySet.getTarget();
        policyCombiningAlgId = policySet.getPolicyCombiningAlgId();
        obligations = policySet.getObligations();

        List<JAXBElement<?>> list = policySet.getPolicySetOrPolicyOrPolicySetIdReference();
        for (JAXBElement<?> jaxbElem : list) {
            Object o = jaxbElem.getValue();
            // extract rules
            if (o instanceof PolicyType) {
                
            }
            else if (o instanceof PolicySetType) {
                
            }
            else if (o instanceof IdReferenceType) {
                // TODO resolve policy or policySet and add them to the policy list.
            }
            // extract anb merge parameters
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
            else if (o instanceof PolicyCombinerParametersType) {
                // TODO make it as a common method then we can use it in PolicySetCombinerParameters
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
            else if (o instanceof PolicySetCombinerParametersType) {
                // TODO
            }
        }
    }

    /**
     * Merge all policies to an array, and this array will be used for evaluation.
     * @throws PolicySyntaxException 
     */
    private synchronized void mergePolicies() throws PolicySyntaxException {
        if (allPolicies == null && all != null && all.size() > 0) {
            allPolicies = new AbstractPolicy[all.size()];
            int index = 0;
            for (XACMLElement elem : all) {
                if (elem instanceof AbstractPolicy) {
                    allPolicies[index ++] = (AbstractPolicy)elem;
                }
                else {
                    allPolicies[index ++] = ((IdReference)elem).getPolicy();
                }
            }
        }
    }

}
