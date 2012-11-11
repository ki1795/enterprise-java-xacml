package an.xacml.engine;

import java.util.List;

/**
 * Load policies from underlying data store.
 */
public interface PolicyLoader {

    /**
     * Load policies from underlying data store.
     * @return List of policy objects
     */
    public List<Object> load() throws Exception;
}
