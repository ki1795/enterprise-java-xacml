package deprecated.an.xacml.policy;

import an.xacml.evaluator.TargetsMatcher;

public class Subjects extends TargetsMatcher {
    private Subject[] subjects;
    public Subjects(Subject[] subjects) {
        if (subjects == null || subjects.length < 1) {
            throw new IllegalArgumentException("subjects should not be null or" +
                    " its length should not less than 1.");
        }
        matches = subjects;
        this.subjects = subjects;
    }

    public Subject[] getSubjects() {
        return subjects;
    }
}