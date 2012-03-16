package an.xacml.engine.evaluator;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import oasis.names.tc.xacml._2_0.policy.schema.os.ActionType;
import oasis.names.tc.xacml._2_0.policy.schema.os.EnvironmentType;
import oasis.names.tc.xacml._2_0.policy.schema.os.ResourceType;
import oasis.names.tc.xacml._2_0.policy.schema.os.SubjectType;
import oasis.names.tc.xacml._2_0.policy.schema.os.TargetType;
import an.xacml.Constants;
import an.xacml.IndeterminateException;
import an.xacml.engine.ctx.EvaluationContext;

/**
 * TargetMatcher (Conjunctive) provide the implementation for all single Target element,
 * if all child matchable elements are matched, the match method will return true,
 * otherwise it will return false.
 */
public class TargetMatcher implements Matcher {

    private Object target;

    protected static final String GET_SUBJECT_MATCH = "getSubjectMatch";
    protected static final String GET_ACTION_MATCH = "getActionMatch";
    protected static final String GET_RESOURCE_MATCH = "getResourceMatch";
    protected static final String GET_ENVIRONMENT_MATCH = "getEnvironmentMatch";

    protected static final String GET_SUBJECTS = "getSubjects";
    protected static final String GET_ACTIONS = "getActions";
    protected static final String GET_RESOURCES = "getResources";
    protected static final String GET_ENVIRONMENTS = "getEnvironments";

    public TargetMatcher(Object target) {
        this.target = target;
    }

    public boolean match(EvaluationContext ctx) throws IndeterminateException {
        try {
            List<?> matches = getMatch();
            for (Object match : matches) {
                if (!EvaluatorFactory.getInstance().getMatcher(match).match(ctx)) {
                    return false;
                }
            }
            return true;
        }
        catch (IndeterminateException e) {
            throw e;
        }
        catch (Exception e) {
            // TODO invocationTarget exception
            throw new IndeterminateException("The match operation failed due to error: ", e,
                    Constants.STATUS_PROCESSINGERROR);
        }
    }

    private List<?> getMatch() throws Exception {
        String methodName = null;
        if (target instanceof SubjectType) {
            methodName = GET_SUBJECT_MATCH;
        }
        else if (target instanceof ActionType) {
            methodName = GET_ACTION_MATCH;
        }
        else if (target instanceof ResourceType) {
            methodName = GET_RESOURCE_MATCH;
        }
        else if (target instanceof EnvironmentType) {
            methodName = GET_ENVIRONMENT_MATCH;
        }
        // TargetType is also a Conjunctive match, so we use this matcher
        else if (target instanceof TargetType) {
            List<Object> matches = new ArrayList<Object>();

            addTargetsToList(matches, GET_SUBJECTS);
            addTargetsToList(matches, GET_RESOURCES);
            addTargetsToList(matches, GET_ACTIONS);
            addTargetsToList(matches, GET_ENVIRONMENTS);

            return matches;
        }
        return (List<?>)get(methodName);
    }

    private Object get(String methodName) throws Exception {
        // FIXME should we avoid reflection to improve performance?
        Class<?> claz = target.getClass();
        Method method = claz.getMethod(methodName);
        return method.invoke(target);
    }

    private void addTargetsToList(List<Object> matches, String methodName) throws Exception {
        Object subjects = get(methodName);
        if (subjects != null) {
            matches.add(subjects);
        }
    }
}