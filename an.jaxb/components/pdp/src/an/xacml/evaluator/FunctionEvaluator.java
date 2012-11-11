package an.xacml.evaluator;

import static an.xacml.Constants.TYPE_STRING;
import oasis.names.tc.xacml._2_0.policy.schema.os.EngineObjectFactory;
import oasis.names.tc.xacml._2_0.policy.schema.os.FunctionType;
import an.xacml.Constants;
import an.xacml.engine.IndeterminateException;
import an.xacml.engine.impl.EvaluationContext;
import an.xml.XMLDataTypeMappingException;

public class FunctionEvaluator implements Evaluator {

    private FunctionType function;
    private String functionId;

    public FunctionEvaluator(Object func) {
        this.function = (FunctionType)func;
        this.functionId = function.getFunctionId();
    }

    /**
     * We only need return a function id to parent element, parent element will evaluate the rest elements and use the
     * result as parameters of the built-in function, and then evaluate the built-in function.
     */
    @Override
    public Object evaluate(EvaluationContext ctx) throws IndeterminateException {
        try {
            return EngineObjectFactory.createAttributeValue(TYPE_STRING, functionId);
        }
        catch (XMLDataTypeMappingException e) {
            throw new IndeterminateException("Error occurs when initialize an AttributeValue object", e, 
                    Constants.STATUS_SYNTAXERROR);
        }
    }
}
