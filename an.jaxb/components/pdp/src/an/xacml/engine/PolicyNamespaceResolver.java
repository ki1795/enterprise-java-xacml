package an.xacml.engine;

import java.util.Map;

/**
 * Policy or PolicySet may have some extra namespaces in nested XML nodes. They are not defined in XACML standard, but
 * they are required for AttributeSelector to select correct nodes from request context. PolicyType and PolicySetType
 * are implemented this interface to populate their namespaces on. So that AttributeSelector can use them while
 * evaluating policies.
 */
public interface PolicyNamespaceResolver {

    /**
     * Return the map of namespace and its actual value.
     * @return
     * @throws Exception
     */
    public Map<String, String> getNamespaceMappings() throws Exception;
}
