package an.example.dems;

import deprecated.an.xacml.context.Request;
import deprecated.an.xacml.context.Response;
import an.config.ConfigElement;
import an.xacml.engine.ctx.ContextFactory;
import an.xacml.engine.ctx.ContextFactoryHelper;
import an.xacml.engine.ctx.ContextHandler;
import an.xacml.engine.ctx.DefaultContextHandler;

public class HTTPContextFactory implements ContextFactory {
    private ContextHandler ctxHandler;

    public HTTPContextFactory(ConfigElement config) {}

    public Request createRequestFromCtx(Object reqCtx) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object createResponseCtx(Response response) {
        // TODO Auto-generated method stub
        return null;
    }

    public synchronized ContextHandler getContextHandler() {
        if (ctxHandler == null) {
            // We use default context handler
            ctxHandler = new DefaultContextHandler(ContextFactoryHelper.getPDP(this));
        }
        return ctxHandler;
    }
}