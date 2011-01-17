package an.example.dems;

import an.config.ConfigElement;
import an.xacml.context.Request;
import an.xacml.context.Response;
import an.xacml.engine.ContextFactory;
import an.xacml.engine.ContextFactoryHelper;
import an.xacml.engine.ContextHandler;
import an.xacml.engine.DefaultContextHandler;

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