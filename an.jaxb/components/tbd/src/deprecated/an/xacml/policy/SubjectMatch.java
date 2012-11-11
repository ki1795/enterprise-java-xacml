package deprecated.an.xacml.policy;

import java.net.URI;

import an.xacml.Expression;
import an.xacml.evaluator.TargetMatchMatcher;

public class SubjectMatch extends TargetMatchMatcher {
    public SubjectMatch(URI matchId, AttributeValue value, Expression designatorOrSelector) {
        this.matchId = matchId;
        this.attributeValue = value;
        this.designatorOrSelector = designatorOrSelector;
    }
}