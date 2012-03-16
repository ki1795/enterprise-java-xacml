package an.xacml.engine.evaluator;

import static an.xacml.engine.evaluator.PolicyEvaluator.appendPolicyObligationsToResult;
import static oasis.names.tc.xacml._2_0.context.schema.os.DecisionType.DENY;
import static oasis.names.tc.xacml._2_0.context.schema.os.DecisionType.PERMIT;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._2_0.context.schema.os.ResultType;
import oasis.names.tc.xacml._2_0.policy.schema.os.CombinerParametersType;
import oasis.names.tc.xacml._2_0.policy.schema.os.IdReferenceType;
import oasis.names.tc.xacml._2_0.policy.schema.os.ObligationsType;
import oasis.names.tc.xacml._2_0.policy.schema.os.PolicyCombinerParametersType;
import oasis.names.tc.xacml._2_0.policy.schema.os.PolicySetCombinerParametersType;
import oasis.names.tc.xacml._2_0.policy.schema.os.PolicySetType;
import oasis.names.tc.xacml._2_0.policy.schema.os.PolicyType;
import oasis.names.tc.xacml._2_0.policy.schema.os.TargetType;
import an.xacml.Constants;
import an.xacml.IndeterminateException;
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
    }

    @Override
    public Object evaluate(EvaluationContext ctx) throws IndeterminateException {
        Object previousPolicySet = null;
        try {
            previousPolicySet = ctx.setCurrentEvaluatingPolicy(policySet);
            initialize(ctx);

            // validate policy and policySet combiner parameters
            validatePolicyAndPolicySetCombinerParameters();

            if (EvaluatorFactory.getInstance().getMatcher(target).match(ctx)) {
                if (policies.size() == 0) {
                    return ResultType.NOTAPPLICABLE;
                }

                // Get rule-combine-alg function from function registry, and then pass rules, combinerParams and 
                // RuleCombinerParams to it, get the EvaluationResult
                FunctionRegistry functionReg = FunctionRegistry.getInstance();
                BuiltInFunction policyCombAlg = functionReg.lookup(policyCombiningAlgId);

                ResultType policyResult = (ResultType)policyCombAlg.invoke(ctx, new Object[] {
                        policies.toArray(), combinerParameters,
                        policyCombinerParameters.toArray(new PolicyCombinerParametersType[0]),
                        policySetCombinerParameters.toArray(new PolicySetCombinerParametersType[0])});
                // Retrieve the corresponding Obligations by Effect in EvaluationResult, and set it to EvaluationResult.
                if ((policyResult.getDecision() == PERMIT || policyResult.getDecision() == DENY) && obligations != null) {
                    if (policyResult == ResultType.PERMIT || policyResult == ResultType.DENY) {
                        policyResult = new ResultType(policyResult);
                    }
                    appendPolicyObligationsToResult(policyResult, obligations);
                }
                return policyResult;
            }
            // NotApplicable
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
            throw new IndeterminateException("Error occurs while evaluating PolicySet.", t,
                    Constants.STATUS_SYNTAXERROR);
        }
        finally {
            ctx.setCurrentEvaluatingPolicy(previousPolicySet);
        }
    }

    private void validatePolicyAndPolicySetCombinerParameters() throws IndeterminateException {
        policy:
        for (PolicyCombinerParametersType policyParams : policyCombinerParameters) {
            for (Object policy : policies) {
                if (policy instanceof PolicyType) {
                    if (policyParams.getPolicyIdRef().equals(((PolicyType)policy).getPolicyId())) {
                        continue policy;
                    }
                }
            }
            throw new IndeterminateException("The PolicyCombinerParameters doesn't have a matched policy id : " +
                    policyParams.getPolicyIdRef(), Constants.STATUS_SYNTAXERROR);
        }

        policySet:
        for (PolicySetCombinerParametersType policySetParams : policySetCombinerParameters) {
            for (Object policySet : policies) {
                if (policySet instanceof PolicySetType) {
                    if (policySetParams.getPolicySetIdRef().equals(((PolicySetType)policySet).getPolicySetId())) {
                        continue policySet;
                    }
                }
            }
            throw new IndeterminateException("The PolicySetCombinerParameters doesn't have a matched policySet id : " +
                    policySetParams.getPolicySetIdRef(), Constants.STATUS_SYNTAXERROR);
        }
    }

    /**
     * Extract elements from policySet
     * @throws EvaluatorRegistryException 
     * @throws IndeterminateException 
     */
    private void initialize(EvaluationContext ctx) throws IndeterminateException, EvaluatorRegistryException {
        target = policySet.getTarget();
        policyCombiningAlgId = policySet.getPolicyCombiningAlgId();
        obligations = policySet.getObligations();

        List<JAXBElement<?>> list = policySet.getPolicySetOrPolicyOrPolicySetIdReference();
        for (JAXBElement<?> jaxbElem : list) {
            Object o = jaxbElem.getValue();
            // extract policies & policySets
            if (o instanceof PolicyType || o instanceof PolicySetType) {
                policies.add(o);
            }
            else if (o instanceof IdReferenceType) {
                // resolve policy or policySet and add them to the policy list.
                Object ref = ((IdReferenceType)o).getPolicy();
                if (ref == null) {
                    // evaluate the IdReferenceType to resolve the referenced policy. The resolved policy will be populated to
                    // the evaluating IdReferenceType instance.
                    ref = EvaluatorFactory.getInstance().getEvaluator(o).evaluate(ctx);
                }
                policies.add(ref);
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
            // extract and merge policy parameters according to policy id
            else if (o instanceof PolicyCombinerParametersType) {
                boolean merged = false;
                for (PolicyCombinerParametersType policyParam : policyCombinerParameters) {
                    // if there is existing policy combiner parameters with same policy id in the list, we will merge them
                    // together.
                    if (((PolicyCombinerParametersType)o).getPolicyIdRef().equals(policyParam.getPolicyIdRef())) {
                        policyParam.getCombinerParameter().addAll(((PolicyCombinerParametersType)o).getCombinerParameter());
                        merged = true;
                        break;
                    }
                }
                // add as a new parameter
                if (!merged) {
                    policyCombinerParameters.add((PolicyCombinerParametersType)o);
                }
            }
            else if (o instanceof PolicySetCombinerParametersType) {
                boolean merged = false;
                for (PolicySetCombinerParametersType policySetParam : policySetCombinerParameters) {
                    // if there is existing policy combiner parameters with same policy id in the list, we will merge them
                    // together.
                    if (((PolicySetCombinerParametersType)o).getPolicySetIdRef().equals(policySetParam.getPolicySetIdRef())) {
                        policySetParam.getCombinerParameter().addAll(((PolicySetCombinerParametersType)o).getCombinerParameter());
                        merged = true;
                        break;
                    }
                }
                // add as a new parameter
                if (!merged) {
                    policySetCombinerParameters.add((PolicySetCombinerParametersType)o);
                }
            }
        }
    }
}
