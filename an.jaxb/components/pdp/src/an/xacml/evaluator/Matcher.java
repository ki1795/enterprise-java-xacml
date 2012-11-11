package an.xacml.evaluator;

import an.xacml.engine.IndeterminateException;
import an.xacml.engine.impl.EvaluationContext;

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