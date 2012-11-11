package an.xacml.evaluator;

import an.xacml.engine.IndeterminateException;
import an.xacml.engine.impl.EvaluationContext;

public interface Evaluator {

    /**
     * TODO
     * @param ctx
     * @return
     * @throws IndeterminateException
     */
    public Object evaluate(EvaluationContext ctx) throws IndeterminateException;
}
