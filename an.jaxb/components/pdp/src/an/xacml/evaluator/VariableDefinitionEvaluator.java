package an.xacml.evaluator;

import oasis.names.tc.xacml._2_0.policy.schema.os.VariableDefinitionType;
import an.xacml.Constants;
import an.xacml.engine.IndeterminateException;
import an.xacml.engine.impl.EvaluationContext;

public class VariableDefinitionEvaluator implements Evaluator {

    private VariableDefinitionType vDef;
    private Object expression;

    public VariableDefinitionEvaluator(Object vDef) {
        this.vDef = (VariableDefinitionType)vDef;

        this.expression = (this.vDef.getExpression() == null ? null : this.vDef.getExpression().getValue());
    }

    @Override
    public Object evaluate(EvaluationContext ctx) throws IndeterminateException {
        try {
            return EvaluatorFactory.getInstance().getEvaluator(expression).evaluate(ctx);
        }
        catch (EvaluatorRegistryException e) {
            throw new IndeterminateException("Error occurs while get evaluator of inner expression.", e,
                    Constants.STATUS_SYNTAXERROR);
        }
        catch (Exception ex) {
            throw new IndeterminateException("Error occurs while evaluating VariableDefinition.", ex,
                    Constants.STATUS_SYNTAXERROR);
        }
    }
}
