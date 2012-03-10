package an.xacml.engine.evaluator;

import oasis.names.tc.xacml._2_0.policy.schema.os.PolicyType;
import an.xacml.IndeterminateException;
import an.xacml.engine.ctx.EvaluationContext;

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
        // TODO Auto-generated method stub
        return null;
    }
}
