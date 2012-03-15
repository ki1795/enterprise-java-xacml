package an.xacml.engine.evaluator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import deprecated.an.xacml.policy.AbstractPolicy;
import deprecated.an.xacml.policy.Version;
import oasis.names.tc.xacml._2_0.policy.schema.os.IdReferenceType;
import an.xacml.IndeterminateException;
import an.xacml.PolicySyntaxException;
import an.xacml.engine.PDP;
import an.xacml.engine.PolicyResolver;
import an.xacml.engine.PolicyResolverRegistry;
import an.xacml.engine.ctx.EvaluationContext;

public class IdReferenceEvaluator implements Evaluator {

    private IdReferenceType idRef;
    private String id;
    private String version;
    private String earliestVersion;
    private String latestVersion;

    // Seems JAXB does NOT check the format of VersionMatchType, so we do the validation by ourselves.
    public static final String VERSIONMATCH_PATTERN = "((\\d+|\\*)\\.)*(\\d+|\\*|\\+)";
    public static final String GET_VERSION = "getVersion";

    public IdReferenceEvaluator(Object idRef) {
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
        // TODO resolve the referenced policy or policySet and then set it back to IdReferenceType
        PDP pdp = ctx.getPDP();
        if (pdp != null) {
            PolicyResolverRegistry reg = PolicyResolverRegistry.getInstance(pdp);
            PolicyResolver[] resolvers = reg.getAllPolicyResolvers();
            for (PolicyResolver resolver : resolvers) {
                if (resolver.isPolicySupported(id)) {
                    Object resolved = resolver.resolvePolicy(id);
                    if (resolved != null && validateResolvedPolicy(resolved)) {
                        resolved.setParentElement(getParentElement());
                        policy = resolved;
                        break;
                    }
                    else {
                        logger.warn("The policy resolved by " + resolver.getClass().getSimpleName() +
                                (resolved == null ?
                                 " is null." : " doesn't match the IdReference. Will try next resolver."));
                    }
                }
            }
        }
        if (policy == null) {
            throw new PolicySyntaxException("Could not resolve policy using the given id: " + id);
        }

        if (!id.equals(policy.getId())) {
            throw new PolicySyntaxException("The referenced policy's ID does not match the expected.");
        }

        if (!validateResolvedPolicy(policy)) {
            throw new PolicySyntaxException("The referenced policy's version does not match the expected.");
        }
        return null;
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
