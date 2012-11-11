package an.xacml.engine;

public class PolicyLoaderException extends Exception {
    private static final long serialVersionUID = 6800881404085919595L;

    public PolicyLoaderException(String message) {
        super(message);
    }

    public PolicyLoaderException(String message, Throwable t) {
        super(message, t);
    }
}
