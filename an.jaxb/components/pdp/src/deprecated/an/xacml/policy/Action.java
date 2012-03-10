package deprecated.an.xacml.policy;

import an.xacml.engine.evaluator.TargetMatcher;

public class Action extends TargetMatcher {
    private ActionMatch[] actionMatches;

    public Action(ActionMatch[] actionMatches) {
        if (actionMatches == null || actionMatches.length < 1) {
            throw new IllegalArgumentException("actionMatches should not be null or" +
                    " its length should not less than 1.");
        }
        matches = actionMatches;
        this.actionMatches = actionMatches;
    }

    public ActionMatch[] getActionMatches() {
        return actionMatches;
    }
}