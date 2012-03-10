package an.xacml.engine.evaluator;

import an.xacml.IndeterminateException;
import an.xacml.engine.ctx.EvaluationContext;

/**
 * Target element, PolicySet, Policy and Rule should use this interface to evaluate if it's match the request or not.
 */
public interface Matcher {
    /**
     * Return true if matched.
     * @param request The passed in request context, implementation may retrieve attribute from it.
     * @return
     * @throws IndeterminateException
     */
    public boolean match(EvaluationContext ctx) throws IndeterminateException;
}