package an.xacml.evaluator;

public class EvaluatorRegistryException extends Exception {
    private static final long serialVersionUID = -2848789682024179091L;

    public EvaluatorRegistryException(String msg, Throwable t) {
        super(msg, t);
    }

    public EvaluatorRegistryException(String msg) {
        super(msg);
    }
}
