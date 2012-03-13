package an.xacml.engine.evaluator;

import an.xacml.IndeterminateException;
import an.xacml.engine.ctx.EvaluationContext;

public class IdReferenceEvaluator implements Evaluator {

    @Override
    public Object evaluate(EvaluationContext ctx) throws IndeterminateException {
        // TODO resolve the referenced policy or policySet and then set it back to IdReferenceType
        return null;
    }

}
