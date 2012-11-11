package an.xacml.evaluator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import oasis.names.tc.xacml._2_0.policy.schema.os.ActionsType;
import oasis.names.tc.xacml._2_0.policy.schema.os.EnvironmentsType;
import oasis.names.tc.xacml._2_0.policy.schema.os.ResourcesType;
import oasis.names.tc.xacml._2_0.policy.schema.os.SubjectsType;
import an.xacml.Constants;
import an.xacml.engine.IndeterminateException;
import an.xacml.engine.impl.EvaluationContext;

/**
 * Targets (Disjunctive) provides a default implementation for a set of matchables,
 * if any of matchables is matched, the match method will return true, otherwise,
 * will return false.
 */
public class TargetsMatcher implements Matcher {

    private Object targets;

    protected static final String GET_SUBJECT = "getSubject";
    protected static final String GET_ACTION = "getAction";
    protected static final String GET_RESOURCE = "getResource";
    protected static final String GET_ENVIRONMENT = "getEnvironment";

    public TargetsMatcher(Object targets) {
        this.targets = targets;
    }

    public boolean match(EvaluationContext ctx) throws IndeterminateException {
        List<?> targetElems;
        try {
            targetElems = getTargetElements();
            for (Object elem : targetElems) {
                if (EvaluatorFactory.getInstance().getMatcher(elem).match(ctx)) {
                    return true;
                }
            }
            return false;
        }
        catch (IndeterminateException ie) {
            throw ie;
        }
        catch (Exception t) {
            if (t instanceof InvocationTargetException) {
                Throwable targetT = ((InvocationTargetException)t).getTargetException();
                if (targetT instanceof IndeterminateException) {
                    throw (IndeterminateException)targetT;
                }
            }
            throw new IndeterminateException("The match operation failed due to error: ", t, Constants.STATUS_SYNTAXERROR);
        }
    }

    private List<?> getTargetElements() throws Exception {
        String methodName = null;
        if (targets instanceof SubjectsType) {
            methodName = GET_SUBJECT;
        }
        else if (targets instanceof ActionsType) {
            methodName = GET_ACTION;
        }
        else if (targets instanceof ResourcesType) {
            methodName = GET_RESOURCE;
        }
        else if (targets instanceof EnvironmentsType) {
            methodName = GET_ENVIRONMENT;
        }
        return (List<?>)get(methodName);
    }

    private Object get(String methodName) throws Exception {
        // FIXME should we avoid reflection to improve performance?
        Class<?> claz = targets.getClass();
        Method method = claz.getMethod(methodName);
        return method.invoke(targets);
    }
}