package an.xacml.engine.evaluator;

import static an.xacml.engine.ctx.FunctionRegistry.getInstance;
import static oasis.names.tc.xacml._2_0.policy.schema.os.AttributeValueType.TRUE;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import oasis.names.tc.xacml._2_0.policy.schema.os.ActionMatchType;
import oasis.names.tc.xacml._2_0.policy.schema.os.AttributeDesignatorType;
import oasis.names.tc.xacml._2_0.policy.schema.os.AttributeSelectorType;
import oasis.names.tc.xacml._2_0.policy.schema.os.AttributeValueType;
import oasis.names.tc.xacml._2_0.policy.schema.os.EnvironmentMatchType;
import oasis.names.tc.xacml._2_0.policy.schema.os.ResourceMatchType;
import oasis.names.tc.xacml._2_0.policy.schema.os.SubjectMatchType;
import an.xacml.Constants;
import an.xacml.IndeterminateException;
import an.xacml.engine.ctx.EvaluationContext;
import an.xacml.engine.ctx.FunctionRegistry;
import an.xacml.function.BuiltInFunction;

/**
 * The matcher for ActionMatch, EnvironmentMatch, ResourceMatch and SubjectMatch. It will invoke the function provided
 * by the element, which execute the actual matching function.
 */
public class TargetMatchMatcher implements Matcher {

    private Object element;

    protected static final String MATCH_ID = "getMatchId";
    protected static final String ATTRIBUTE_VALUE = "getAttributeValue";
    protected static final String ATTRIBUTE_SELECTOR = "getAttributeSelector";

    protected static final String SUBJECT_ATTRIBUTE_DESIGNATOR = "getSubjectAttributeDesignator";
    protected static final String ACTION_ATTRIBUTE_DESIGNATOR = "getActionAttributeDesignator";
    protected static final String ENVIRONMENT_ATTRIBUTE_DESIGNATOR = "getEnvironmentAttributeDesignator";
    protected static final String RESOURCE_ATTRIBUTE_DESIGNATOR = "getResourceAttributeDesignator";

    public TargetMatchMatcher(Object targetMatch) {
        this.element = targetMatch;
    }

    /**
     * The method get a built-in function and invoke it by passing attribute value and result of an expression (a 
     * designator or a attribute selector) as its parameters, and then return a boolean result indicate if the match
     * is successful or failed. If any error occurs, an IndeterminateException will be thrown (XACML 2.0 requires).
     */
    public boolean match(EvaluationContext ctx) throws IndeterminateException {
        try {
            Object param1 = evaluateAttributeValue(ctx);
            // The expression evaluate result should be an AttributeValue[]
            AttributeValueType[] param2 = (AttributeValueType[])evaluateDesignatorOrSelector(ctx);

            FunctionRegistry funcReg = getInstance();
            BuiltInFunction func = funcReg.lookup(getMatchId());
            if (param2 != null) {
                // The <Match> element requires to match each of attributes returned from designator or selector,
                // if at least one matched, it will return true.
                for (AttributeValueType attrVal : param2) {
                    if (compareAttributeValue((AttributeValueType)func.invoke(ctx, new Object[] {param1, attrVal}), TRUE)) {
                        return true;
                    }
                }
            }
            return false;
        }
        catch (IndeterminateException ex) {
            throw ex;
        }
        catch (Exception t) {
            if (t instanceof InvocationTargetException) {
                Throwable targetT = ((InvocationTargetException)t).getTargetException();
                if (targetT instanceof IndeterminateException) {
                    throw (IndeterminateException)targetT;
                }
            }
            throw new IndeterminateException("The match operation failed due to error: ", t, Constants.STATUS_SYNTAXERROR);
        }
    }

    /**
     * Get the matchId (functionId). The referenced function must return a boolean result.
     * @return The matchId which referencing the function.
     * @throws Exception 
     */
    private String getMatchId() throws Exception {
        return (String)get(MATCH_ID);
    }

    private AttributeValueType getAttributeValue() throws Exception {
        return (AttributeValueType)get(ATTRIBUTE_VALUE);
    }

    private AttributeSelectorType getAttributeSelector() throws Exception {
        return (AttributeSelectorType)get(ATTRIBUTE_SELECTOR);
    }

    private AttributeDesignatorType getAttributeDesignator() throws Exception {
        String methodName = null;
        if (element instanceof SubjectMatchType) {
            methodName = SUBJECT_ATTRIBUTE_DESIGNATOR;
        }
        else if (element instanceof ActionMatchType) {
            methodName = ACTION_ATTRIBUTE_DESIGNATOR;
        }
        else if (element instanceof ResourceMatchType) {
            methodName = RESOURCE_ATTRIBUTE_DESIGNATOR;
        }
        else if (element instanceof EnvironmentMatchType) {
            methodName = ENVIRONMENT_ATTRIBUTE_DESIGNATOR;
        }
        return (AttributeDesignatorType)get(methodName);
    }

    private Object get(String methodName) throws Exception {
        // FIXME should we avoid reflection to improve performance?
        Class<?> claz = element.getClass();
        Method method = claz.getMethod(methodName);
        return method.invoke(element);
    }

    private boolean compareAttributeValue(AttributeValueType a, AttributeValueType b)
            throws EvaluatorRegistryException {
        if (a == b) {
            return true;
        }

        Comparator comparator = EvaluatorFactory.getInstance().getComparator(a);
        if (comparator.compare(a, b) == 0) {
            return true;
        }

        return false;
    }

    private Object evaluateAttributeValue(EvaluationContext ctx) throws Exception {
        AttributeValueType attrValue = getAttributeValue();
        return EvaluatorFactory.getInstance().getEvaluator(attrValue).evaluate(ctx);
    }

    private Object evaluateDesignatorOrSelector(EvaluationContext ctx) throws Exception {
        AttributeDesignatorType designator = getAttributeDesignator();
        AttributeSelectorType selector = getAttributeSelector();
        Object evaluatable = (designator == null ? selector : designator); 

        return EvaluatorFactory.getInstance().getEvaluator(evaluatable).evaluate(ctx);
    }
}