package org.openmrs.module.aijarreports.library;

import org.openmrs.*;
import org.openmrs.api.PatientSetService;
import org.openmrs.module.aijarreports.definition.cohort.definition.InAgeRangeAtCohortDefinition;
import org.openmrs.module.aijarreports.definition.cohort.definition.InEncounterCohortDefinition;
import org.openmrs.module.aijarreports.definition.cohort.definition.ObsWithEncountersCohortDefinition;
import org.openmrs.module.aijarreports.metadata.HIVMetadata;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.*;
import org.openmrs.module.reporting.common.Age;
import org.openmrs.module.reporting.common.BooleanOperator;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.ConvertedDataDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.*;
import org.openmrs.module.reporting.data.encounter.definition.ConvertedEncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.*;
import org.openmrs.module.reporting.data.person.definition.ObsForPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.query.encounter.definition.EncounterQuery;
import org.openmrs.module.reporting.query.encounter.definition.MappedParametersEncounterQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class DataFactory {

    @Autowired
    HIVMetadata hivMetadata;

    public Parameter getStartDateParameter() {
        return ReportingConstants.START_DATE_PARAMETER;
    }

    public Parameter getEndDateParameter() {
        return ReportingConstants.END_DATE_PARAMETER;
    }

    public CohortDefinition getPatientsWhoseObsValueDateIsOnSpecifiedDate(Concept dateConcept, List<EncounterType> types) {
        DateObsCohortDefinition cd = new DateObsCohortDefinition();
        cd.setTimeModifier(PatientSetService.TimeModifier.ANY);
        cd.setQuestion(dateConcept);
        cd.setEncounterTypeList(types);
        cd.addParameter(new Parameter("onDate", "onDate", Date.class));
        return convert(cd, ObjectUtil.toMap("onDate=onDate"));
    }

    public PatientDataDefinition getAllIdentifiersOfType(PatientIdentifierType pit, DataConverter converter) {
        PatientIdentifierDataDefinition def = new PatientIdentifierDataDefinition();
        def.setTypes(Arrays.asList(pit));
        return new ConvertedPatientDataDefinition(def, converter);
    }

    public PatientDataDefinition getFirstEncounterOfTypeByEndDate(EncounterType type, DataConverter converter) {
        EncountersForPatientDataDefinition def = new EncountersForPatientDataDefinition();
        def.setWhich(TimeQualifier.FIRST);
        def.setTypes(Arrays.asList(type));
        def.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(def, ObjectUtil.toMap("onOrBefore=endDate"), converter);
    }

    // Converters

	/*public DataConverter getIdentifierCollectionConverter() {
        CollectionConverter collectionConverter = new CollectionConverter(new PatientIdentifierConverter(), true, null);
		return new ChainedConverter(collectionConverter, new ObjectFormatter(" "));
	}*/

    public DataConverter getIdentifierConverter() {
        return new PropertyConverter(PatientIdentifier.class, "identifier");
    }

    public DataConverter getEncounterDatetimeConverter() {
        return new PropertyConverter(Encounter.class, "encounterDatetime");
    }

    public DataConverter getEncounterLocationNameConverter() {
        return new ChainedConverter(new PropertyConverter(Encounter.class, "location"), new ObjectFormatter());
    }

    public DataConverter getEncounterTypeNameConverter() {
        return new ChainedConverter(new PropertyConverter(Encounter.class, "type"), new ObjectFormatter());
    }

    public DataConverter getObsDatetimeConverter() {
        return new PropertyConverter(Obs.class, "obsDatetime");
    }

    public DataConverter getObsValueNumericConverter() {
        return new PropertyConverter(Obs.class, "valueNumeric");
    }

    public DataConverter getObsValueDatetimeConverter() {
        return new PropertyConverter(Obs.class, "valueDatetime");
    }

    public DataConverter getObsValueDatetimeCollectionConverter() {
        ChainedConverter itemConverter = new ChainedConverter(getObsValueDatetimeConverter(), getObjectFormatter());
        CollectionConverter collectionConverter = new CollectionConverter(itemConverter, true, null);
        return new ChainedConverter(collectionConverter, new ObjectFormatter(" "));
    }

    public DataConverter getObsValueCodedConverter() {
        return new PropertyConverter(Obs.class, "valueCoded");
    }

    public DataConverter getObsValueCodedNameConverter() {
        return new ChainedConverter(getObsValueCodedConverter(), new ObjectFormatter());
    }

    public DataConverter getObsValueTextConverter() {
        return new PropertyConverter(Obs.class, "valueText");
    }

    public DataConverter getObsValueCodedPresentConverter(Concept valueCoded) {
        ChainedConverter converter = new ChainedConverter();
        converter.addConverter(new CollectionConverter(getObsValueCodedConverter(), false, null));
        converter.addConverter(new CollectionElementConverter(valueCoded, "true", ""));
        return converter;
    }

    public DataConverter getObjectFormatter() {
        return new ObjectFormatter();
    }

    public DataConverter getListItemConverter(Integer index, DataConverter... converters) {
        ChainedConverter ret = new ChainedConverter();
        ret.addConverter(new ListConverter(index, Object.class));
        for (DataConverter converter : converters) {
            ret.addConverter(converter);
        }
        return ret;
    }

    public DataConverter getLastListItemConverter(DataConverter... converters) {
        ChainedConverter ret = new ChainedConverter();
        ret.addConverter(new ListConverter(TimeQualifier.LAST, 1, Object.class));
        for (DataConverter converter : converters) {
            ret.addConverter(converter);
        }
        return ret;
    }

    public DataConverter getDataSetItemConverter(Integer index, String columnName, Object nullReplacement) {
        ChainedConverter ret = new ChainedConverter();
        ret.addConverter(new ListConverter(index, DataSetRow.class));
        ret.addConverter(new DataSetRowConverter(columnName));
        if (nullReplacement != null) {
            ret.addConverter(new NullValueConverter(nullReplacement));
        }
        return ret;
    }

    public DataConverter getLastDataSetItemConverter(String columnName, Object nullReplacement) {
        ChainedConverter ret = new ChainedConverter();
        ret.addConverter(new ListConverter(TimeQualifier.LAST, 1, DataSetRow.class));
        ret.addConverter(new DataSetRowConverter(columnName));
        if (nullReplacement != null) {
            ret.addConverter(new NullValueConverter(nullReplacement));
        }
        return ret;
    }

    public CompositionCohortDefinition getPatientsInAny(CohortDefinition... elements) {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.initializeFromQueries(BooleanOperator.OR, elements);
        return cd;
    }

    public CompositionCohortDefinition getPatientsNotIn(CohortDefinition... elements) {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.initializeFromQueries(BooleanOperator.NOT, elements);
        return cd;
    }

    public CompositionCohortDefinition createPatientComposition(Object... elements) {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.initializeFromElements(elements);
        return cd;
    }

    // Convenience methods

    public PatientDataDefinition getMostRecentObsByEndDate(Concept question) {
        return getMostRecentObsByEndDate(question, null);
    }

    public PatientDataDefinition getMostRecentObsByEndDate(Concept question, DataConverter converter) {
        ObsForPersonDataDefinition def = new ObsForPersonDataDefinition();
        def.setWhich(TimeQualifier.LAST);
        def.setQuestion(question);
        def.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(def, ObjectUtil.toMap("onOrBefore=endDate"), converter);
    }

    public PatientDataDefinition convert(PatientDataDefinition pdd, Map<String, String> renamedParameters, DataConverter converter) {
        ConvertedPatientDataDefinition convertedDefinition = new ConvertedPatientDataDefinition();
        addAndConvertMappings(pdd, convertedDefinition, renamedParameters, converter);
        return convertedDefinition;
    }

    public PatientDataDefinition convert(PatientDataDefinition pdd, DataConverter converter) {
        return convert(pdd, null, converter);
    }

    public PatientDataDefinition convert(PersonDataDefinition pdd, Map<String, String> renamedParameters, DataConverter converter) {
        return convert(new PersonToPatientDataDefinition(pdd), renamedParameters, converter);
    }

    public PatientDataDefinition convert(PersonDataDefinition pdd, DataConverter converter) {
        return convert(pdd, null, converter);
    }

    public EncounterDataDefinition convert(EncounterDataDefinition pdd, Map<String, String> renamedParameters, DataConverter converter) {
        ConvertedEncounterDataDefinition convertedDefinition = new ConvertedEncounterDataDefinition();
        addAndConvertMappings(pdd, convertedDefinition, renamedParameters, converter);
        return convertedDefinition;
    }

    public EncounterDataDefinition convert(EncounterDataDefinition pdd, DataConverter converter) {
        return convert(pdd, null, converter);
    }

    public EncounterQuery convert(EncounterQuery query, Map<String, String> renamedParameters) {
        return new MappedParametersEncounterQuery(query, renamedParameters);
    }

    public CohortDefinition convert(CohortDefinition cd, Map<String, String> renamedParameters) {
        return new MappedParametersCohortDefinition(cd, renamedParameters);
    }

    protected <T extends DataDefinition> void addAndConvertMappings(T copyFrom, ConvertedDataDefinition<T> copyTo, Map<String, String> renamedParameters, DataConverter converter) {
        copyTo.setDefinitionToConvert(ParameterizableUtil.copyAndMap(copyFrom, copyTo, renamedParameters));
        if (converter != null) {
            copyTo.setConverters(Arrays.asList(converter));
        }
    }

    // Cohorts
    public CohortDefinition getAnyEncounterOfTypesWithinMonthsByEndDate(List<EncounterType> types, int numMonths) {
        EncounterCohortDefinition cd = new EncounterCohortDefinition();
        cd.setEncounterTypeList(types);
        cd.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        return convert(cd, ObjectUtil.toMap("onOrAfter=endDate-" + numMonths + "m+1d"));
    }

    public CohortDefinition getAnyEncounterOfTypesWithinMonthsBeforeEndDate(List<EncounterType> types, int numMonths) {
        EncounterCohortDefinition cd = new EncounterCohortDefinition();
        cd.setEncounterTypeList(types);
        cd.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        return convert(cd, ObjectUtil.toMap("onOrAfter=endDate-" + numMonths + "m+1d"));
    }

    public CompositionCohortDefinition getPatientsInAll(CohortDefinition... elements) {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.initializeFromQueries(BooleanOperator.AND, elements);
        return cd;
    }

    public CohortDefinition getPatientsWhoStartedStateWhenInAgeRangeAtLocationByEndDate(Integer minAge, Age.Unit minAgeUnit, Integer maxAge, Age.Unit maxAgeUnit) {
        InAgeRangeAtCohortDefinition cd = new InAgeRangeAtCohortDefinition();
        cd.setMinAge(minAge);
        cd.setMinAgeUnit(minAgeUnit);
        cd.setMaxAge(maxAge);
        cd.setMaxAgeUnit(maxAgeUnit);
        //        return convert(cd, ObjectUtil.toMap("startedOnOrBefore=endDate"));
        return cd;
    }

    public CohortDefinition getAnyEncounterOfType(List<EncounterType> types) {
        InEncounterCohortDefinition cd = new InEncounterCohortDefinition();
        cd.setEncounterTypes(types);
        cd.addParameter(new Parameter("startYear", "Start Year", Integer.class));
        cd.addParameter(new Parameter("startMonth", "Start Month", Integer.class));
        cd.addParameter(new Parameter("monthsBefore", "Month Before", Integer.class));
        return convert(cd, ObjectUtil.toMap("startYear=startYear,startMonth=startMonth,monthsBefore=monthsBefore"));
    }

    public CohortDefinition getObsWithEncounters(Concept question, List<EncounterType> types) {
        ObsWithEncountersCohortDefinition cd = new ObsWithEncountersCohortDefinition();
        cd.setEncounterTypes(types);
        cd.setQuestion(question);
        cd.setWhichEncounter(TimeQualifier.FIRST);
        cd.addParameter(new Parameter("startYear", "Start Year", Integer.class));
        cd.addParameter(new Parameter("startMonth", "Start Month", Integer.class));
        cd.addParameter(new Parameter("monthsBefore", "Month Before", Integer.class));
        return convert(cd, ObjectUtil.toMap("startYear=startYear,startMonth=startMonth,monthsBefore=monthsBefore"));
    }

    public CohortDefinition getObsWithEncounters(Concept question, List<EncounterType> types, List<Concept> answers) {
        ObsWithEncountersCohortDefinition cd = new ObsWithEncountersCohortDefinition();
        cd.setEncounterTypes(types);
        cd.setQuestion(question);
        cd.setWhichEncounter(TimeQualifier.FIRST);
        cd.setAnswers(answers);
        cd.addParameter(new Parameter("startYear", "Start Year", Integer.class));
        cd.addParameter(new Parameter("startMonth", "Start Month", Integer.class));
        cd.addParameter(new Parameter("monthsBefore", "Month Before", Integer.class));
        return convert(cd, ObjectUtil.toMap("startYear=startYear,startMonth=startMonth,monthsBefore=monthsBefore"));
    }

    public CohortDefinition getPatients(Concept question) {
        CodedObsCohortDefinition cd = new CodedObsCohortDefinition();
        cd.setTimeModifier(PatientSetService.TimeModifier.ANY);
        cd.setQuestion(question);
        //        cd.setEncounterTypeList(encounterTypes);
        return cd;
    }

    public CohortDefinition getPatientsWithIdentifierOfType(PatientIdentifierType... types) {
        PatientIdentifierCohortDefinition cd = new PatientIdentifierCohortDefinition();
        for (PatientIdentifierType type : types) {
            cd.addTypeToMatch(type);
        }
        return cd;
    }

}
