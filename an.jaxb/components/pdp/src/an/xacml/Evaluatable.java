package an.xacml;

import deprecated.an.xacml.context.Result;
import an.xacml.engine.ctx.EvaluationContext;

/**
 * @deprecated
 * All evaluatable, PolicySet, Policy and Rule, must implement the interface.
 */
public interface Evaluatable {

    /**
     * PolicySet, Policy, and Rule, should implement this interface.
     * @param request The passed in request context, implementation may retrieve attribute from it.
     * @return The evaluate result.
     */
    public Result evaluate(EvaluationContext ctx) throws IndeterminateException;
}