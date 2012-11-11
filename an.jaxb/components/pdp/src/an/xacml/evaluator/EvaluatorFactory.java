package an.xacml.evaluator;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import oasis.names.tc.xacml._2_0.policy.schema.os.ActionMatchType;
import oasis.names.tc.xacml._2_0.policy.schema.os.ActionType;
import oasis.names.tc.xacml._2_0.policy.schema.os.ActionsType;
import oasis.names.tc.xacml._2_0.policy.schema.os.ApplyType;
import oasis.names.tc.xacml._2_0.policy.schema.os.AttributeAssignmentType;
import oasis.names.tc.xacml._2_0.policy.schema.os.AttributeDesignatorType;
import oasis.names.tc.xacml._2_0.policy.schema.os.AttributeSelectorType;
import oasis.names.tc.xacml._2_0.policy.schema.os.AttributeValueType;
import oasis.names.tc.xacml._2_0.policy.schema.os.ConditionType;
import oasis.names.tc.xacml._2_0.policy.schema.os.EnvironmentMatchType;
import oasis.names.tc.xacml._2_0.policy.schema.os.EnvironmentType;
import oasis.names.tc.xacml._2_0.policy.schema.os.EnvironmentsType;
import oasis.names.tc.xacml._2_0.policy.schema.os.FunctionType;
import oasis.names.tc.xacml._2_0.policy.schema.os.IdReferenceType;
import oasis.names.tc.xacml._2_0.policy.schema.os.PolicySetType;
import oasis.names.tc.xacml._2_0.policy.schema.os.PolicyType;
import oasis.names.tc.xacml._2_0.policy.schema.os.ResourceMatchType;
import oasis.names.tc.xacml._2_0.policy.schema.os.ResourceType;
import oasis.names.tc.xacml._2_0.policy.schema.os.ResourcesType;
import oasis.names.tc.xacml._2_0.policy.schema.os.RuleType;
import oasis.names.tc.xacml._2_0.policy.schema.os.SubjectAttributeDesignatorType;
import oasis.names.tc.xacml._2_0.policy.schema.os.SubjectMatchType;
import oasis.names.tc.xacml._2_0.policy.schema.os.SubjectType;
import oasis.names.tc.xacml._2_0.policy.schema.os.SubjectsType;
import oasis.names.tc.xacml._2_0.policy.schema.os.TargetType;
import oasis.names.tc.xacml._2_0.policy.schema.os.VariableDefinitionType;
import oasis.names.tc.xacml._2_0.policy.schema.os.VariableReferenceType;

//FIXME which object should we add an equals method on?
/**
 * We register evaluator and matcher for XACML elements to this factory class.
 */
public class EvaluatorFactory {

    // Evaluator registration
    private Map<Class<?>, Class<?>> evaluatorReg = new HashMap<Class<?>, Class<?>>();
    // Matcher registration
    private Map<Class<?>, Class<?>> matcherReg = new HashMap<Class<?>, Class<?>>();
    // Comparator registration
    private Map<Class<?>, Class<?>> comparatorReg = new HashMap<Class<?>, Class<?>>();

    private static EvaluatorFactory instance = new EvaluatorFactory();

    private EvaluatorFactory() {
        // register evaluators/matchers for XACML policy & context
        evaluatorReg.put(AttributeValueType.class, AttributeValueEvaluator.class);
        evaluatorReg.put(AttributeAssignmentType.class, AttributeValueEvaluator.class);
        evaluatorReg.put(PolicyType.class, PolicyEvaluator.class);
        evaluatorReg.put(PolicySetType.class, PolicySetEvaluator.class);
        evaluatorReg.put(IdReferenceType.class, IdReferenceEvaluator.class);
        evaluatorReg.put(RuleType.class, RuleEvaluatorAndMatcher.class);
        evaluatorReg.put(ConditionType.class, ConditionEvaluator.class);
        evaluatorReg.put(AttributeDesignatorType.class, AttributeDesignatorEvaluator.class);
        evaluatorReg.put(SubjectAttributeDesignatorType.class, ConditionEvaluator.class);
        evaluatorReg.put(AttributeSelectorType.class, AttributeSelectorEvaluator.class);
        evaluatorReg.put(ApplyType.class, ApplyEvaluator.class);
        evaluatorReg.put(FunctionType.class, FunctionEvaluator.class);
        evaluatorReg.put(VariableDefinitionType.class, VariableDefinitionEvaluator.class);
        evaluatorReg.put(VariableReferenceType.class, VariableReferenceEvaluator.class);

        matcherReg.put(PolicyType.class, PolicyMatcher.class);
        matcherReg.put(PolicySetType.class, PolicyMatcher.class);
        matcherReg.put(RuleType.class, RuleEvaluatorAndMatcher.class);
        matcherReg.put(ActionType.class, TargetMatcher.class);
        matcherReg.put(EnvironmentType.class, TargetMatcher.class);
        matcherReg.put(ResourceType.class, TargetMatcher.class);
        matcherReg.put(SubjectType.class, TargetMatcher.class);
        matcherReg.put(TargetType.class, TargetMatcher.class);
        matcherReg.put(ActionsType.class, TargetsMatcher.class);
        matcherReg.put(EnvironmentsType.class, TargetsMatcher.class);
        matcherReg.put(ResourcesType.class, TargetsMatcher.class);
        matcherReg.put(SubjectsType.class, TargetsMatcher.class);
        matcherReg.put(ActionMatchType.class, TargetMatchMatcher.class);
        matcherReg.put(EnvironmentMatchType.class, TargetMatchMatcher.class);
        matcherReg.put(ResourceMatchType.class, TargetMatchMatcher.class);
        matcherReg.put(SubjectMatchType.class, TargetMatchMatcher.class);
        // TODO

        comparatorReg.put(AttributeValueType.class, AttributeValueComparator.class);
    }

    public static EvaluatorFactory getInstance() {
        return instance;
    }

    public Evaluator getEvaluator(Object evaluatable) throws EvaluatorRegistryException {
        Class<?> evalClass = evaluatorReg.get(evaluatable.getClass());
        if (evalClass == null) {
            throw new EvaluatorRegistryException("No evaluator registered for '" +
                    evaluatable.getClass().getSimpleName() + "'");
        }

        try {
            Constructor<?> evalC = evalClass.getConstructor(Object.class);
            return (Evaluator)evalC.newInstance(evaluatable);
        }
        catch (Exception e) {
            throw new EvaluatorRegistryException("Failed to get evaluator for '" +
                    evaluatable.getClass().getSimpleName() + "'", e);
        }
    }

    public Matcher getMatcher(Object matchable) throws EvaluatorRegistryException {
        Class<?> matchClass = matcherReg.get(matchable.getClass());
        if (matchClass == null) {
            throw new EvaluatorRegistryException("No matcher registered for '" +
                    matchable.getClass().getSimpleName() + "'");
        }

        try {
            Constructor<?> matchC = matchClass.getConstructor(Object.class);
            return (Matcher)matchC.newInstance(matchable);
        }
        catch (Exception e) {
            throw new EvaluatorRegistryException("Failed to get matcher for '" + matchable, e);
        }
    }

    public Comparator getComparator(Object comparable) throws EvaluatorRegistryException {
        Class<?> comparatorClass = comparatorReg.get(comparable.getClass());
        if (comparatorClass == null) {
            throw new EvaluatorRegistryException("No comparator registered for '" +
                    comparable.getClass().getSimpleName() + "'");
        }

        try {
            Constructor<?> comparatorC = comparatorClass.getConstructor(Object.class);
            return (Comparator)comparatorC.newInstance(comparable);
        }
        catch (Exception e) {
            throw new EvaluatorRegistryException("Failed to get comparator for '" + comparable, e);
        }
    }
}