package an.xacml;


/**
 * This class represent the Indeterminate error during the matching or evaluating process.
 */
public class IndeterminateException extends Exception {
    private static final long serialVersionUID = -4903662220704205651L;
    /**
     * The status code indicate the cause of the exception.
     */
    private String statusCode = Constants.STATUS_UNKNOWNERROR;
    /**
     * The caller could attach an object that include all detailed information about the exception, the exception 
     * receiver then could retrieve and process it with the final desicion.
     */
    private Object attachedObject;

    public IndeterminateException(String message) {
        super(message);
        // The default attached object is self.
        attachedObject = this;
    }

    public IndeterminateException(String message, Throwable t) {
        super(message, t);
        // The default attached object is self.
        attachedObject = this;
    }

    public IndeterminateException(String message, String status) {
        super(message);
        statusCode = status;
        // The default attached object is self.
        attachedObject = this;
    }

    public IndeterminateException(String message, Throwable t, String status) {
        super(message, t);
        statusCode = status;
        // The default attached object is self.
        attachedObject = this;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }
    
    public void setAttachedObject(Object o) {
        attachedObject = o;
    }

    public Object getAttachedObject() {
        return attachedObject;
    }
}