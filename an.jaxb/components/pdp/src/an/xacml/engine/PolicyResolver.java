package an.xacml.engine;

import java.util.List;

/**
 * This interface defined a policy resolver that can retrieve a policy by given id, or can return all policies this
 * resolver supported.
 */
public interface PolicyResolver {
    /**
     * Return true if the policy identified by policyId is supported by this PolicyResolver. Otherwise return false.
     * @param policyId
     * @return
     */
    public boolean isPolicySupported(String policyId);
    /**
     * Resolve a single policy. If current resolver doesn't support the policy identified by policyId, this method
     * should return null.
     * @param policyId
     * @return
     */
    public Object resolvePolicy(String policyId);
    /**
     * Return all supported policies by this PolicyResolver. The implementation should have cache mechanism to improve
     * the performance.
     * @return
     */
    public List<Object> resolveAllPolicies();
}