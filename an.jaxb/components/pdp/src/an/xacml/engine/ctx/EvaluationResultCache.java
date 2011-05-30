package an.xacml.engine.ctx;

import an.config.ConfigElement;
import an.xacml.engine.Cache;

public class EvaluationResultCache extends Cache {
    public EvaluationResultCache(ConfigElement config) {
        super(config);
    }

    public void invalidateAll() {
        removeAll();
    }
}