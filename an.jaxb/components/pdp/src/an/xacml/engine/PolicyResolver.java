package an.xacml.engine;

import java.util.List;

/**
 * This interface defined a policy resolver that can retrieve a policy by given id, or can return all policies this
 * resolver supported.
 */
public interface PolicyResolver {
    /**
     * Return true if the policy identified by policyId is resolvable by this PolicyResolver. Otherwise return false.
     * @param policyId
     * @return
     */
    public boolean isResolvable(String policyId);
    /**
     * Resolve a single policy. If current resolver doesn't support the policy identified by policyId, this method
     * will return null.
     * @param policyId
     * @return
     */
    public Object resolve(String policyId);
    /**
     * Return all resolvable policies by this PolicyResolver. The implementation should have cache mechanism to improve
     * the performance.
     * @return
     */
    public List<Object> resolveAll();
}