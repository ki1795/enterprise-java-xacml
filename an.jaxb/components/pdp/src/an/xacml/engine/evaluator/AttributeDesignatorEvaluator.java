package an.xacml.engine.evaluator;

import static an.xacml.Constants.PREFIX_ACTION;
import static an.xacml.Constants.PREFIX_ENVIRONMENT;
import static an.xacml.Constants.PREFIX_RESOURCE;
import static an.xacml.Constants.PREFIX_SUBJECT;
import static an.xacml.engine.ctx.AttributeRetriever.ACTION;
import static an.xacml.engine.ctx.AttributeRetriever.ANY;
import static an.xacml.engine.ctx.AttributeRetriever.ENVIRONMENT;
import static an.xacml.engine.ctx.AttributeRetriever.RESOURCE;
import static an.xacml.engine.ctx.AttributeRetriever.SUBJECT;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import oasis.names.tc.xacml._2_0.context.schema.os.AttributeType;
import oasis.names.tc.xacml._2_0.context.schema.os.MissingAttributeDetailType;
import oasis.names.tc.xacml._2_0.context.schema.os.SubjectType;
import oasis.names.tc.xacml._2_0.policy.schema.os.AttributeDesignatorType;
import oasis.names.tc.xacml._2_0.policy.schema.os.AttributeValueType;
import oasis.names.tc.xacml._2_0.policy.schema.os.EngineObjectFactory;
import oasis.names.tc.xacml._2_0.policy.schema.os.SubjectAttributeDesignatorType;
import an.xacml.Constants;
import an.xacml.IndeterminateException;
import an.xacml.engine.ctx.AttributeRetriever;
import an.xacml.engine.ctx.AttributeRetrieverRegistry;
import an.xacml.engine.ctx.EvaluationContext;
import an.xml.XMLGeneralException;

public class AttributeDesignatorEvaluator implements Evaluator {

    private AttributeDesignatorType attrDesignator;

    private String attrId;
    private String dataType;
    private String issuer;
    private String subjectCategory;
    private boolean mustBePresent;

    public static final String GET_ATTRIBUTE = "getAttribute";

    public AttributeDesignatorEvaluator(Object attrDesignator) {
        this.attrDesignator = (AttributeDesignatorType)attrDesignator;
        initialize();
    }

    private void initialize() {
        attrId = attrDesignator.getAttributeId();
        dataType = attrDesignator.getDataType();
        issuer = attrDesignator.getIssuer();
        mustBePresent = attrDesignator.isMustBePresent();

        if (attrDesignator instanceof SubjectAttributeDesignatorType) {
            subjectCategory = ((SubjectAttributeDesignatorType)attrDesignator).getSubjectCategory();
        }
    }

    @Override
    public Object evaluate(EvaluationContext ctx) throws IndeterminateException {
        try {
            // 1st - getFromRequest
            List<AttributeValueType> result = getAttributeValuesFromRequest(ctx, attrId, dataType, issuer, subjectCategory);
            if (result != null && result.size() > 0) {
                return result;
            }

            // 2nd - Trying to get dateTime from context.
            result = new ArrayList<AttributeValueType>();
            result.add(ctx.getDateTimeAttribute(attrId));
            if (result != null && result.size() > 0) {
                return result;
            }

            // 3rd - If no attribute got, we will try to get from attribute retrievers.
            result = getAttributeValuesFromAttributeRetriever(ctx, attrId, dataType, issuer, subjectCategory);

            // 4th - Must be present? if yes and no attribute got, throw an IndeterminateException.
            if ((result == null || result.size() == 0) && ctx.getPDP().supportMustBePresent() && mustBePresent) {
                // throw an IndeterminateException
                IndeterminateException ex = new IndeterminateException(
                        "The required attribute is missing : " + attrId, Constants.STATUS_MISSINGATTRIBUTE);
                // Return an IndeterminateException with an array of MissingAttributeDetail. This array object will be
                // finally put into a Status object that includes in a Response.
                MissingAttributeDetailType missing = new MissingAttributeDetailType();
                missing.setAttributeId(this.attrId);
                missing.setDataType(this.dataType);
                missing.setIssuer(this.issuer);
                List<MissingAttributeDetailType> missingAttrs = new ArrayList<MissingAttributeDetailType>();
                missingAttrs.add(missing);

                ex.setAttachedObject(missingAttrs);
                throw ex;
            }

            return result;
        }
        catch (IndeterminateException ex) {
            throw ex;
        }
        // Code should not run to here, because the datatype and value are get from an existing Attribute object, it 
        // should has already passed type check. However, we still want place code here in case there are errors.
        catch (Exception t) {
            throw new IndeterminateException("There is error occurs during retrieve attributes from request.", t,
                    Constants.STATUS_SYNTAXERROR);
        }
    }

    public List<AttributeValueType> getAttributeValuesFromAttributeRetriever(
            EvaluationContext ctx, String attrId, String dataType, String issuer, String subjCategory)
    throws XMLGeneralException, IndeterminateException {
        // First trying to get attributes from request
        List<AttributeValueType> result = new ArrayList<AttributeValueType>();
        // Trying to get attribute from attribute retrievers.
        AttributeRetrieverRegistry reg = AttributeRetrieverRegistry.getInstance(ctx.getPDP());

        int type = getPossibleTypeOfAttributeId(attrId.toString());
        List<AttributeRetriever> attrRetrs = reg.getAttributeRetrieversByType(type);
        for (AttributeRetriever attrRetr : attrRetrs) {
            if (attrRetr.isAttributeSupported(attrId, dataType)) {
                result = attrRetr.retrieveAttributeValues(ctx, attrId, dataType, issuer, subjCategory);
                if (result != null && result.size() > 0) {
                    return result;
                }
            }
        }

        if (type != ANY) {
            attrRetrs = reg.getAllAttributeRetrievers();
            for (AttributeRetriever attrRetr : attrRetrs) {
                if (attrRetr.isAttributeSupported(attrId, dataType)) {
                    result = attrRetr.retrieveAttributeValues(ctx, attrId, dataType, issuer, subjCategory);
                    if (result != null && result.size() > 0) {
                        return result;
                    }
                }
            }
        }

        return result;
    }

    private List<AttributeValueType> getAttributeValuesFromRequest(
            EvaluationContext ctx, String attrId, String dataType, String issuer, String subjCategory)
    throws IndeterminateException {
        try {
            List<AttributeValueType> result = new ArrayList<AttributeValueType>();

            List<Object> allElements = new ArrayList<Object>();
            if (subjCategory == null) {
                // get non-subject attribute.
                allElements.addAll(ctx.getRequest().getMergedTargetElements());
            }
            else {
                // if going to get a subject attribute.
                allElements.addAll(ctx.getRequest().getSubject());
            }

            for (Object elem : allElements) {
                // If this is for subject attribute designator, the search scope is restricted to same category.
                if (subjCategory == null || subjCategory.equals(((SubjectType)elem).getSubjectCategory())) {
                    List<AttributeType> attrs = getAttributeById(attrId, elem);
                    for (AttributeType attr : attrs) {
                        // The designate attribute should not be null
                        if (attr != null && 
                            // If the designated data type is null, we don't need to match it
                            (attr.getDataType() == null || dataType.equals(attr.getDataType())) &&
                            // If the issuer is required for attribute designator, we need do matching with designated 
                            // attribute.
                            (issuer == null || issuer.equals(attr.getIssuer()))) {
                            List<oasis.names.tc.xacml._2_0.context.schema.os.AttributeValueType> values = attr.getAttributeValue();
                            for (oasis.names.tc.xacml._2_0.context.schema.os.AttributeValueType cValue : values) {
                                result.add(EngineObjectFactory.createAttributeValue(
                                      // Multiple XML elements will be parsed as a single String value when the datatype is String
                                      attr.getDataType(), cValue.getContent().get(0)));
                            }
                        }
                    }
                }
            }

            return result;
        }
        // Code should not run to here, because the datatype and value are get from an existing Attribute object, it 
        // should has already passed type check. However, we still want place code here in case there are errors.
        catch (Exception t) {
            throw new IndeterminateException("There is error occurs during retrieve attributes from request.", t,
                    Constants.STATUS_SYNTAXERROR);
        }
    }

    @SuppressWarnings("unchecked")
    private List<AttributeType> getAttributeById(String attrId, Object target) throws IllegalArgumentException,
    IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {
        Method getAttr = target.getClass().getMethod(GET_ATTRIBUTE);
        return (List<AttributeType>)getAttr.invoke(target);
    }

    private static int getPossibleTypeOfAttributeId(String attrId) {
        if (attrId.startsWith(PREFIX_SUBJECT)) {
            return SUBJECT;
        }
        if (attrId.startsWith(PREFIX_ACTION)) {
            return ACTION;
        }
        if (attrId.startsWith(PREFIX_RESOURCE)) {
            return RESOURCE;
        }
        if (attrId.startsWith(PREFIX_ENVIRONMENT)) {
            return ENVIRONMENT;
        }
        return ANY;
    }
}
