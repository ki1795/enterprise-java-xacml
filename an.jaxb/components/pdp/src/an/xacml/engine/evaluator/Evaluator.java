package an.xacml.engine.evaluator;

import an.xacml.IndeterminateException;
import an.xacml.engine.ctx.EvaluationContext;

public interface Evaluator {

    /**
     * TODO
     * @param ctx
     * @return
     * @throws IndeterminateException
     */
    public Object evaluate(EvaluationContext ctx) throws IndeterminateException;
}
