package an.xacml.engine.ctx;

import deprecated.an.xacml.context.Request;
import deprecated.an.xacml.context.Response;
import an.config.ConfigElement;

/**
 * The default implementation of ContextFacroty, if "an.xacml.engine.ContextFactory"
 * is not configured, this defult ContextFactory will be used.
 */
public class DefaultContextFactory implements ContextFactory {
    private ContextHandler ctxHandler;

    public DefaultContextFactory(ConfigElement config) {}

    public synchronized ContextHandler getContextHandler() {
        if (ctxHandler == null) {
            ctxHandler = new DefaultContextHandler(ContextFactoryHelper.getPDP(this));
        }
        return ctxHandler;
    }

    public Request createRequestFromCtx(Object reqCtx) {
        // We directly cast the application request context to XACML request.
        return (Request)reqCtx;
    }

    public Object createResponseCtx(Response response) {
        // We directly return XACML response as application response.
        return response;
    }
}