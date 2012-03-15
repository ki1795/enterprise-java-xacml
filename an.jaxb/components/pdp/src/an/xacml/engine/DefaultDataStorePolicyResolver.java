package an.xacml.engine;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import oasis.names.tc.xacml._2_0.policy.schema.os.PolicySetType;
import oasis.names.tc.xacml._2_0.policy.schema.os.PolicyType;
import an.config.ConfigElement;

public class DefaultDataStorePolicyResolver implements PolicyResolver {
    private Map<String, Object> policies = new Hashtable<String, Object>();

    public DefaultDataStorePolicyResolver(ConfigElement config) {}

    public boolean isPolicySupported(String policyId) {
        return policies.containsKey(policyId);
    }

    public List<Object> resolveAllPolicies() {
        List<Object> result = new ArrayList<Object>();
        result.addAll(policies.values());
        return result;
    }

    public Object resolvePolicy(String policyId) {
        return policies.get(policyId);
    }

    protected void setPolicies(Object[] policies) {
        this.policies.clear();
        if (policies != null && policies.length > 0) {
            for (Object policy : policies) {
                if (policy instanceof PolicyType) {
                    this.policies.put(((PolicyType)policy).getPolicyId(), policy);
                }
                else if (policy instanceof PolicySetType) {
                    this.policies.put(((PolicySetType)policy).getPolicySetId(), policy);
                }
            }
        }
    }
/*
    public void update(AbstractPolicy[] toBeUpdated, URI[] toBeDeleted) {
        // TODO Should update together with PolicyCache
    }*/
}