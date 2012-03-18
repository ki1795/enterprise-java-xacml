package an.xacml.engine.ctx;

import static an.xacml.Constants.ATTR_DATE;
import static an.xacml.Constants.ATTR_DATETIME;
import static an.xacml.Constants.ATTR_TIME;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import oasis.names.tc.xacml._2_0.context.schema.os.RequestType;
import oasis.names.tc.xacml._2_0.policy.schema.os.AttributeValueType;
import oasis.names.tc.xacml._2_0.policy.schema.os.EngineObjectFactory;
import oasis.names.tc.xacml._2_0.policy.schema.os.VariableDefinitionType;
import an.xacml.Constants;
import an.xacml.engine.PDP;
import an.xml.XMLDataTypeRegistry;

public class EvaluationContext {
    // current PDP we evaluating policies inside.
    private PDP pdp;
    private RequestType request;
    private Object policy;
    private AttributeValueType time;
    private AttributeValueType date;
    private AttributeValueType dateTime;

    /**
     * Variable definition is scoped by policy. When we start to evaluate a policy, we define the variables on context for others
     * reference, and then we release them after the policy evaluate done. Since policySet may include child polices, we need to
     * distinguish all variables among different policies (scope/namespace).
     */
    private Map<Object, Map<String, VariableDefinitionType>> variableDefs = 
            new HashMap<Object, Map<String, VariableDefinitionType>>();

    public EvaluationContext(RequestType request) throws EvaluationContextException {
        this.request = request;
        try {
            mergeRequestTargetElements();
            XMLGregorianCalendar current = XMLDataTypeRegistry.getDatatypeFactory().newXMLGregorianCalendar();
            time = EngineObjectFactory.createAttributeValue(Constants.TYPE_TIME, current);
            date = EngineObjectFactory.createAttributeValue(Constants.TYPE_DATE, current);
            dateTime = EngineObjectFactory.createAttributeValue(Constants.TYPE_DATETIME, current);
        }
        catch (Exception e) {
            throw new EvaluationContextException("Cannot initialize evaluation context due to error :", e);
        }
    }

    private void mergeRequestTargetElements() {
        List<Object> merged = request.getMergedTargetElements();
        merged.addAll(request.getSubject());
        merged.addAll(request.getResource());

        if (request.getAction() != null) {
            merged.add(request.getAction());
        }
        if (request.getEnvironment() != null) {
            merged.add(request.getEnvironment());
        }
    }

    public void setPDP(PDP pdp) {
        this.pdp = pdp;
    }

    public PDP getPDP() {
        return this.pdp;
    }

    /**
     * FIXME should it be the root evaluating element? or should we add another field to keep PDP (we may need PDP
     * configuration information while evaluating polices) information?
     * @param policy
     */
    public Object setCurrentEvaluatingPolicy(Object policy) {
        Object old = this.policy;
        this.policy = policy;
        return old;
    }

    public Object getCurrentEvaluatingPolicy() {
        return policy;
    }

    public RequestType getRequest() {
        return request;
    }

    public void defineVariables(Map<String, VariableDefinitionType> variables) {
        Object policy = getCurrentEvaluatingPolicy();

        Map<String, VariableDefinitionType> vars = variableDefs.get(policy);
        if (vars == null) {
            vars = new HashMap<String, VariableDefinitionType>();
            variableDefs.put(policy, vars);
        }
        // merge the variables. but actually we only define variables once while we evaluating a policy.
        vars.putAll(variables);
    }

    public void releaseVariables() {
        variableDefs.remove(getCurrentEvaluatingPolicy());
    }

    public Map<String, VariableDefinitionType> getVariables() {
    	return variableDefs.get(getCurrentEvaluatingPolicy());
    }

    /**
     * Get the corresponding date, time or datetime from the context.
     * @param attrId
     * @return
     */
    public AttributeValueType getDateTimeAttribute(String attrId) {
        if (ATTR_TIME.equals(attrId)) {
            return time;
        }
        else if (ATTR_DATE.equals(attrId)) {
            return date;
        }
        else if (ATTR_DATETIME.equals(attrId)) {
            return dateTime;
        }
        return null;
    }
}