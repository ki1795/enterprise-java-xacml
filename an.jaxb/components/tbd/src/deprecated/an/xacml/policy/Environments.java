package deprecated.an.xacml.policy;

import an.xacml.evaluator.TargetsMatcher;

public class Environments extends TargetsMatcher {
    private Environment[] environments;
    public Environments(Environment[] environments) {
        if (environments == null || environments.length < 1) {
            throw new IllegalArgumentException("environments should not be null or" +
                    " its length should not less than 1.");
        }
        matches = environments;
        this.environments = environments;
    }

    public Environment[] getEnvironments() {
        return environments;
    }
}