package an.xacml.engine.evaluator;

import java.util.Map;

import oasis.names.tc.xacml._2_0.policy.schema.os.VariableDefinitionType;
import oasis.names.tc.xacml._2_0.policy.schema.os.VariableReferenceType;
import an.xacml.Constants;
import an.xacml.IndeterminateException;
import an.xacml.engine.ctx.EvaluationContext;

public class VariableReferenceEvaluator implements Evaluator {

    private VariableReferenceType vRef;
    private String vId;

    public VariableReferenceEvaluator(Object vRef) {
        this.vRef = (VariableReferenceType)vRef;
        this.vId = this.vRef.getVariableId();
    }

    @Override
    public Object evaluate(EvaluationContext ctx) throws IndeterminateException {
        Map<String, VariableDefinitionType> vDefs = ctx.getVariables();
        if (vDefs != null) {
            VariableDefinitionType vDef = vDefs.get(vId);
            if (vDef != null) {
                try {
                    return EvaluatorFactory.getInstance().getEvaluator(vDef).evaluate(ctx);
                }
                catch (EvaluatorRegistryException e) {
                    throw new IndeterminateException("Error occurs while get evaluator of VariableDefinition.", e,
                            Constants.STATUS_SYNTAXERROR);
                }
                catch (Exception ex) {
                    throw new IndeterminateException("Error occurs while evaluating VariableReference.", ex,
                            Constants.STATUS_SYNTAXERROR);
                }
            }
        }
        throw new IndeterminateException("No VariableDefinition found in context for variable reference : " + vId + ".",
                Constants.STATUS_SYNTAXERROR);
    }
}
