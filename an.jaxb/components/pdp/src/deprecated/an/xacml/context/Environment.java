package deprecated.an.xacml.context;

public class Environment extends TargetElement {
    public Environment(Attribute[] attrs) {
        populateAttributes(attrs);
        generateHashCode();
    }
}