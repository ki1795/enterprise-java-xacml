package an.xacml.evaluator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import oasis.names.tc.xacml._2_0.policy.schema.os.IdReferenceType;
import oasis.names.tc.xacml._2_0.policy.schema.os.PolicyType;
import an.log.LogFactory;
import an.log.Logger;
import an.xacml.Constants;
import an.xacml.PDP;
import an.xacml.engine.IndeterminateException;
import an.xacml.engine.PolicyResolver;
import an.xacml.engine.PolicyResolverRegistry;
import an.xacml.engine.impl.EvaluationContext;

public class IdReferenceEvaluator implements Evaluator {

    private IdReferenceType idRef;
    private String id;
    private String version;
    private String earliestVersion;
    private String latestVersion;
    private Logger logger;

    // Seems JAXB does NOT check the format of VersionMatchType, so we do the validation by ourselves.
    public static final String VERSIONMATCH_PATTERN = "((\\d+|\\*)\\.)*(\\d+|\\*|\\+)";
    public static final String GET_VERSION = "getVersion";
    public static final String GET_POLICY_ID = "getPolicyId";
    public static final String GET_POLICYSET_ID = "getPolicySetId";

    public IdReferenceEvaluator(Object idRef) {
        logger = LogFactory.getLogger();
        this.idRef = (IdReferenceType)idRef;

        id = this.idRef.getValue();
        version = validateVersionMatch(this.idRef.getVersion());
        earliestVersion = validateVersionMatch(this.idRef.getEarliestVersion());
        latestVersion = validateVersionMatch(this.idRef.getLatestVersion());
    }

    private String validateVersionMatch(String pattern) {
        if (pattern.matches(VERSIONMATCH_PATTERN)) {
            return convertVersionMatchToJavaRE(pattern);
        }
        else {
            throw new IllegalArgumentException("The given pattern \"" + pattern + 
                    "\" doesn't match the version match format");
        }
    }

    private String convertVersionMatchToJavaRE(String versionMatch) {
        String plus = "\\.\\+", plusRep = "(.\\\\d+)*";
        String dot = "\\.", dotRep = "\\\\.";
        String ast = "\\*", astRep = "\\\\d";

        // replace all "*" with "\d"
        String phase1 = versionMatch.replaceAll(ast, astRep);
        // replace all ".+" with "(.\d+)*"
        String phase2 = phase1.replaceAll(plus, plusRep);
        // replace all "." with "\\.", include the "." in "(.\d+)*"
        return phase2.replaceAll(dot, dotRep);
    }

    @Override
    public Object evaluate(EvaluationContext ctx) throws IndeterminateException {
        try {
            // resolve the referenced policy or policySet and then set it back to IdReferenceType
            PDP pdp = ctx.getPDP();
            if (pdp != null) {
                PolicyResolverRegistry reg = PolicyResolverRegistry.getInstance(pdp);
                PolicyResolver[] resolvers = reg.getAllPolicyResolvers();
                for (PolicyResolver resolver : resolvers) {
                    if (resolver.isResolvable(id)) {
                        Object resolved = resolver.resolve(id);
                        if (resolved != null && validateResolvedPolicy(resolved)) {
                            idRef.setPolicy(resolved);
                            break;
                        }
                        else {
                            logger.warn("The policy resolved by " + resolver.getClass().getSimpleName() +
                                    (resolved == null ? " is null." : " doesn't match the IdReference. Will try next resolver."));
                        }
                    }
                }
            }
            if (idRef.getPolicy() == null) {
                throw new IndeterminateException("Could not resolve policy using the given id: " + id, Constants.STATUS_SYNTAXERROR);
            }
    
            if (!id.equals(getPolicyOrPolicySetId(idRef.getPolicy()))) {
                throw new IndeterminateException(
                        "The referenced policy's ID does not match the expected.", Constants.STATUS_SYNTAXERROR);
            }

            return idRef.getPolicy();
        }
        catch (IndeterminateException iEx) {
            throw iEx;
        }
        catch (Exception t) {
            if (t instanceof InvocationTargetException) {
                Throwable targetT = ((InvocationTargetException)t).getTargetException();
                if (targetT instanceof IndeterminateException) {
                    throw (IndeterminateException)targetT;
                }
            }
            throw new IndeterminateException("Error occurs while evaluating Policy.", t, Constants.STATUS_SYNTAXERROR);
        }
    }

    private String getPolicyOrPolicySetId(Object policy) throws SecurityException, NoSuchMethodException, 
    IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        String getIdMethodName = null;
        if (policy instanceof PolicyType) {
            getIdMethodName = GET_POLICY_ID;
        }
        else {
            getIdMethodName = GET_POLICYSET_ID;
        }
        Method getIdMethod = policy.getClass().getMethod(getIdMethodName);
        return (String)getIdMethod.invoke(policy);
    }

    private boolean validateResolvedPolicy(Object policy) throws Exception {
        Method getVersion = policy.getClass().getMethod(GET_VERSION);
        String policyVersion = (String)getVersion.invoke(policy);

        // FIXME should we check the policy version between earliest and latest?
        if ((version != null && !policyVersion.matches(version)) ||
            (earliestVersion != null && !policyVersion.matches(earliestVersion)) ||
            (latestVersion != null && !policyVersion.matches(latestVersion))) {
            return false;
        }
        return true;
    }
}
