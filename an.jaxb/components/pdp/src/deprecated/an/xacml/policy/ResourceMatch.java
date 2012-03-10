package deprecated.an.xacml.policy;

import java.net.URI;

import an.xacml.Expression;
import an.xacml.engine.evaluator.TargetMatchMatcher;

public class ResourceMatch extends TargetMatchMatcher {
    public ResourceMatch(URI matchId, AttributeValue value, Expression designatorOrSelector) {
        this.matchId = matchId;
        this.attributeValue = value;
        this.designatorOrSelector = designatorOrSelector;
    }
}