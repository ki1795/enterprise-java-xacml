package an.xacml.engine.evaluator;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.xml.bind.JAXBElement;

import oasis.names.tc.xacml._2_0.policy.schema.os.ApplyType;
import an.xacml.Constants;
import an.xacml.IndeterminateException;
import an.xacml.engine.ctx.EvaluationContext;
import an.xacml.engine.ctx.FunctionRegistry;
import an.xacml.function.BuiltInFunction;
import an.xacml.function.LogicalFunction;

public class ApplyEvaluator implements Evaluator {

    private ApplyType apply;
    private String functionId;
    private List<JAXBElement<?>> expressions;

    public ApplyEvaluator(Object apply) {
        this.apply = (ApplyType)apply;
        functionId = this.apply.getFunctionId();
        expressions = this.apply.getExpression();
    }

    @Override
    public Object evaluate(EvaluationContext ctx) throws IndeterminateException {
        try {
            FunctionRegistry functionReg = FunctionRegistry.getInstance();
            BuiltInFunction function = functionReg.lookup(functionId);

            Object[] params = new Object[expressions.size()];
            // If function is logical function, we won't evaluate the expression, we let function itself to evaluate
            // it as need. This is a standard specified behavior.
            if (isLogicalFunction(function)) {
                params = expressions.toArray();
            }
            else {
                for (int i = 0; i < expressions.size(); i ++) {
                    params[i] = EvaluatorFactory.getInstance().getEvaluator(expressions.get(i)).evaluate(ctx);
                }
            }

            try {
                return function.invoke(ctx, params);
            }
            catch (InvocationTargetException functionInvEx) {
                // All indeterminate exception throws from function should be processing error, ...
                Throwable t = functionInvEx.getTargetException();
                if (t instanceof IndeterminateException) {
                    // ... except it has already had an definitely status code.
                    if (((IndeterminateException)t).getStatusCode().equals(Constants.STATUS_UNKNOWNERROR)) {
                        ((IndeterminateException)t).setStatusCode(Constants.STATUS_PROCESSINGERROR);
                    }
                    throw (IndeterminateException)t;
                }
                throw functionInvEx;
            }
        }
        catch (IndeterminateException ex) {
            throw ex;
        }
        catch (Exception t) {
            throw new IndeterminateException("Error occurs while evaluating Apply element.", t, 
                    Constants.STATUS_PROCESSINGERROR);
        }
    }

    private static boolean isLogicalFunction(BuiltInFunction func) {
        if (func.getAttribute(LogicalFunction.class) != null) {
            return true;
        }
        return false;
    }
}
