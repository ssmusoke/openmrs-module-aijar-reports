package org.openmrs.module.aijarreports.library;

import org.openmrs.*;
import org.openmrs.api.PatientSetService;
import org.openmrs.module.aijarreports.common.Period;
import org.openmrs.module.aijarreports.definition.cohort.definition.*;
import org.openmrs.module.aijarreports.definition.data.converter.PatientIdentifierConverter;
import org.openmrs.module.aijarreports.definition.data.definition.ObsForPersonInPeriodDataDefinition;
import org.openmrs.module.aijarreports.metadata.HIVMetadata;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.*;
import org.openmrs.module.reporting.common.*;
import org.openmrs.module.reporting.data.ConvertedDataDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.*;
import org.openmrs.module.reporting.data.encounter.definition.ConvertedEncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.*;
import org.openmrs.module.reporting.data.person.definition.ObsForPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredAddressDataDefinition;
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

    public Parameter getOnDateParameter() {
        return new Parameter("onDate", "On Date", Date.class);
    }

    // Data Converters
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

    // Convenience converter methods

    public DataConverter getIdentifierCollectionConverter() {
        CollectionConverter collectionConverter = new CollectionConverter(new PatientIdentifierConverter(), true, null);
        return new ChainedConverter(collectionConverter, new ObjectFormatter(" "));
    }

    public CohortDefinition convert(CohortDefinition cd, Map<String, String> renamedParameters) {
        return new MappedParametersCohortDefinition(cd, renamedParameters);
    }

    public EncounterQuery convert(EncounterQuery query, Map<String, String> renamedParameters) {
        return new MappedParametersEncounterQuery(query, renamedParameters);
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

    protected <T extends DataDefinition> void addAndConvertMappings(T copyFrom, ConvertedDataDefinition<T> copyTo, Map<String, String> renamedParameters, DataConverter converter) {
        copyTo.setDefinitionToConvert(ParameterizableUtil.copyAndMap(copyFrom, copyTo, renamedParameters));
        if (converter != null) {
            copyTo.setConverters(Arrays.asList(converter));
        }
    }

    // Composition Cohorts

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

    // Patient Data Definitions


    //    public PatientDataDefinition getMostRecentObsByEndDate(Concept question) {
    //        return getMostRecentObsByEndDate(question);
    //    }

    public PatientDataDefinition getObsByEndDate(Concept question, DataConverter converter, TimeQualifier timeQualifier) {
        ObsForPersonDataDefinition def = new ObsForPersonDataDefinition();
        def.setWhich(timeQualifier);
        def.setQuestion(question);
        def.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(def, ObjectUtil.toMap("onOrBefore=endDate"), converter);
    }

    public PatientDataDefinition getObsByEndDate(Concept question, DataConverter converter, String olderThan, TimeQualifier timeQualifier) {
        ObsForPersonDataDefinition def = new ObsForPersonDataDefinition();
        def.setWhich(timeQualifier);
        def.setQuestion(question);
        def.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(def, ObjectUtil.toMap("onOrBefore=endDate-" + olderThan), converter);
    }

    public PatientDataDefinition getObsDuringPeriod(Concept question, DataConverter converter,TimeQualifier timeQualifier) {
        ObsForPersonDataDefinition def = new ObsForPersonDataDefinition();
        def.setWhich(timeQualifier);
        def.setQuestion(question);
        def.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        def.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        return convert(def, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=endDate"), converter);
    }

    public PatientDataDefinition getObsDuringPeriod(Concept question, DataConverter converter, String olderThan, TimeQualifier timeQualifier) {
        ObsForPersonDataDefinition def = new ObsForPersonDataDefinition();
        def.setWhich(timeQualifier);
        def.setQuestion(question);
        def.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        def.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        return convert(def, ObjectUtil.toMap("onOrAfter=startDate-" + olderThan + ",onOrBefore=endDate-" + olderThan), converter);
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

    public PatientDataDefinition getLastEncounterOfTypeByEndDate(EncounterType type, DataConverter converter) {
        EncountersForPatientDataDefinition def = new EncountersForPatientDataDefinition();
        def.setWhich(TimeQualifier.LAST);
        def.setTypes(Arrays.asList(type));
        def.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(def, ObjectUtil.toMap("onOrBefore=endDate"), converter);
    }

    public PatientDataDefinition getPreferredAddress(String property) {
        PreferredAddressDataDefinition d = new PreferredAddressDataDefinition();
        PropertyConverter converter = new PropertyConverter(PersonAddress.class, property);
        return convert(d, converter);
    }

    public PatientDataDefinition getFirstObsByEndDate(Concept question, List<EncounterType> encounterTypes, DataConverter converter) {
        ObsForPersonDataDefinition def = new ObsForPersonDataDefinition();
        def.setWhich(TimeQualifier.FIRST);
        def.setQuestion(question);
        def.setEncounterTypeList(encounterTypes);
        def.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(def, ObjectUtil.toMap("onOrBefore=endDate"), converter);
    }

    public PatientDataDefinition getFirstObBetweenDates(Concept question, List<EncounterType> encounterTypes, DataConverter converter) {
        ObsForPersonDataDefinition def = new ObsForPersonDataDefinition();
        def.setWhich(TimeQualifier.FIRST);
        def.setQuestion(question);
        def.setEncounterTypeList(encounterTypes);
        def.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        def.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        return convert(def, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=endDate"), converter);
    }

    public PatientDataDefinition getMostRecentObsByEndDate(Concept question, List<EncounterType> encounterTypes, DataConverter converter) {
        ObsForPersonDataDefinition def = new ObsForPersonDataDefinition();
        def.setWhich(TimeQualifier.LAST);
        def.setQuestion(question);
        def.setEncounterTypeList(encounterTypes);
        def.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(def, ObjectUtil.toMap("onOrBefore=endDate"), converter);
    }

    public PatientDataDefinition getAllObsByEndDate(Concept question, List<EncounterType> encounterTypes, DataConverter converter) {
        ObsForPersonDataDefinition def = new ObsForPersonDataDefinition();
        def.setQuestion(question);
        def.setEncounterTypeList(encounterTypes);
        def.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(def, ObjectUtil.toMap("onOrBefore=endDate"), converter);
    }

    public PatientDataDefinition getCodedObsPresentByEndDate(Concept question, Concept answer, List<EncounterType> encounterTypes) {
        ObsForPersonDataDefinition def = new ObsForPersonDataDefinition();
        def.setQuestion(question);
        def.setEncounterTypeList(encounterTypes);
        def.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(def, ObjectUtil.toMap("onOrBefore=endDate"), getObsValueCodedPresentConverter(answer));
    }


    public PatientDataDefinition getObsValueDuringPeriod(Concept question, List<EncounterType> encounterTypes, DataConverter converter) {
        ObsForPersonInPeriodDataDefinition def = new ObsForPersonInPeriodDataDefinition();
        def.setQuestion(question);
        def.setEncounterTypes(encounterTypes);
        def.setWhichEncounter(TimeQualifier.FIRST);
        def.addParameter(new Parameter("startDate", "Start Date", Date.class));
        return convert(def, ObjectUtil.toMap("startDate=startDate"), converter);
    }

    public PatientDataDefinition getObsValueDuringPeriod(Concept question, List<EncounterType> encounterTypes, List<Concept> answers, DataConverter converter) {
        ObsForPersonInPeriodDataDefinition def = new ObsForPersonInPeriodDataDefinition();
        def.setAnswers(answers);
        def.setQuestion(question);
        def.setEncounterTypes(encounterTypes);
        def.setWhichEncounter(TimeQualifier.FIRST);
        def.addParameter(new Parameter("startDate", "Start Date", Date.class));
        return convert(def, ObjectUtil.toMap("startDate=startDate"), converter);
    }

    // Cohorts Definitions

    public CohortDefinition getPatientsWhoseObsValueDateIsOnSpecifiedDate(Concept dateConcept, List<EncounterType> types) {
        DateObsCohortDefinition cd = new DateObsCohortDefinition();
        cd.setTimeModifier(PatientSetService.TimeModifier.ANY);
        cd.setQuestion(dateConcept);
        cd.setEncounterTypeList(types);
        cd.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return cd;
    }

    public CohortDefinition getAnyEncounterOfTypesWithinMonthsByEndDate(List<EncounterType> types, int numMonths) {
        EncounterCohortDefinition cd = new EncounterCohortDefinition();
        cd.setEncounterTypeList(types);
        cd.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        return convert(cd, ObjectUtil.toMap("onOrAfter=endDate-" + numMonths + "m+1d"));
    }

    public CohortDefinition getAnyEncounterOfTypesByEndOfPreviousDate(List<EncounterType> types) {
        EncounterCohortDefinition cd = new EncounterCohortDefinition();
        cd.setEncounterTypeList(types);
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(cd, ObjectUtil.toMap("onOrBefore=startDate-1d"));
    }

    public CohortDefinition getAnyEncounterOfTypesBetweenDates(List<EncounterType> types) {
        EncounterCohortDefinition cd = new EncounterCohortDefinition();
        cd.setEncounterTypeList(types);
        cd.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(cd, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=endDate"));
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
        return cd;
    }

    public CohortDefinition getAnyEncounterOfType(List<EncounterType> types) {
        InEncounterCohortDefinition cd = new InEncounterCohortDefinition();
        cd.setEncounterTypes(types);
        cd.addParameter(getStartDateParameter());
        cd.addParameter(getEndDateParameter());
        return convert(cd, ObjectUtil.toMap("startDate=startDate,endDate=endDate"));
    }

    public CohortDefinition getObsWithEncounters(Concept question, List<EncounterType> types) {
        ObsWithEncountersCohortDefinition cd = new ObsWithEncountersCohortDefinition();
        cd.setEncounterTypes(types);
        cd.setQuestion(question);
        cd.setWhichEncounter(TimeQualifier.FIRST);
        cd.addParameter(getStartDateParameter());
        cd.addParameter(getEndDateParameter());
        return convert(cd, ObjectUtil.toMap("startDate=startDate,endDate=endDate"));
    }

    public CohortDefinition getObsWithEncounters(Concept question, List<EncounterType> types, List<Concept> answers) {
        ObsWithEncountersCohortDefinition cd = new ObsWithEncountersCohortDefinition();
        cd.setEncounterTypes(types);
        cd.setQuestion(question);
        cd.setWhichEncounter(TimeQualifier.FIRST);
        cd.setAnswers(answers);
        cd.addParameter(getStartDateParameter());
        cd.addParameter(getEndDateParameter());
        return convert(cd, ObjectUtil.toMap("startDate=startDate,endDate=endDate"));
    }

    public CohortDefinition getPatientsInPeriod(List<EncounterType> types, Period period) {
        PatientsInPeriodCohortDefinition cd = new PatientsInPeriodCohortDefinition();
        cd.setEncounterTypes(types);
        cd.setWhichEncounter(TimeQualifier.FIRST);
        cd.setPeriod(period);
        cd.addParameter(getStartDateParameter());
        return convert(cd, ObjectUtil.toMap("startDate=startDate"));
    }

    public CohortDefinition getPatientsInPeriod(List<EncounterType> types, Concept question, Period period) {
        PatientsInPeriodCohortDefinition cd = new PatientsInPeriodCohortDefinition();
        cd.setEncounterTypes(types);
        cd.setQuestion(question);
        cd.setIncludeObs(true);
        cd.setWhichEncounter(TimeQualifier.FIRST);
        cd.setPeriod(period);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        return convert(cd, ObjectUtil.toMap("startDate=startDate"));
    }

    public CohortDefinition getPatientsInPeriod(List<EncounterType> types, Concept question, List<Concept> answers, Period period) {
        PatientsInPeriodCohortDefinition cd = new PatientsInPeriodCohortDefinition();
        cd.setEncounterTypes(types);
        cd.setQuestion(question);
        cd.setIncludeObs(true);
        cd.setWhichEncounter(TimeQualifier.FIRST);
        cd.setPeriod(period);
        cd.setAnswers(answers);
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        return convert(cd, ObjectUtil.toMap("startDate=startDate"));
    }

    public CohortDefinition getPatientsWithIdentifierOfType(PatientIdentifierType... types) {
        PatientIdentifierCohortDefinition cd = new PatientIdentifierCohortDefinition();
        for (PatientIdentifierType type : types) {
            cd.addTypeToMatch(type);
        }
        return cd;
    }

    public CohortDefinition getPatientsWithConcept(Concept question, PatientSetService.TimeModifier timeModifier) {
        CodedObsCohortDefinition cd = new CodedObsCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(question);
        return cd;
    }

    public CohortDefinition getPatientsWithCodedObs(Concept question, List<EncounterType> restrictToTypes, List<Concept> codedValues, PatientSetService.TimeModifier timeModifier) {
        CodedObsCohortDefinition cd = new CodedObsCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(question);
        cd.setEncounterTypeList(restrictToTypes);
        cd.setOperator(SetComparator.IN);
        cd.setValueList(codedValues);
        return cd;
    }

    public CohortDefinition getPatientsWithCodedObsDuringPeriod(Concept question, List<EncounterType> restrictToTypes, PatientSetService.TimeModifier timeModifier) {
        CodedObsCohortDefinition cd = new CodedObsCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(question);
        cd.setEncounterTypeList(restrictToTypes);
        cd.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(cd, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=endDate"));
    }

    public CohortDefinition getPatientsWithCodedObsDuringPeriod(Concept question, List<EncounterType> restrictToTypes, String olderThan, PatientSetService.TimeModifier timeModifier) {
        CodedObsCohortDefinition cd = new CodedObsCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(question);
        cd.setEncounterTypeList(restrictToTypes);
        cd.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(cd, ObjectUtil.toMap("onOrAfter=startDate-" + olderThan + ",onOrBefore=endDate-" + olderThan));
    }

    public CohortDefinition getPatientsWithCodedObsDuringPeriod(Concept question, List<EncounterType> restrictToTypes, List<Concept> codedValues, PatientSetService.TimeModifier timeModifier) {
        CodedObsCohortDefinition cd = new CodedObsCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(question);
        cd.setEncounterTypeList(restrictToTypes);
        cd.setOperator(SetComparator.IN);
        cd.setValueList(codedValues);
        cd.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(cd, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=endDate"));
    }

    public CohortDefinition getPatientsWithCodedObsDuringPeriod(Concept question, List<EncounterType> restrictToTypes, List<Concept> codedValues, String olderThan, PatientSetService.TimeModifier timeModifier) {
        CodedObsCohortDefinition cd = new CodedObsCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(question);
        cd.setEncounterTypeList(restrictToTypes);
        cd.setOperator(SetComparator.IN);
        cd.setValueList(codedValues);
        cd.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(cd, ObjectUtil.toMap("onOrAfter=startDate-" + olderThan + ",onOrBefore=endDate-" + olderThan));
    }

    public CohortDefinition getPatientsWithCodedObsByEndOfPreviousDate(Concept question, List<EncounterType> restrictToTypes, PatientSetService.TimeModifier timeModifier) {
        CodedObsCohortDefinition cd = new CodedObsCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(question);
        cd.setEncounterTypeList(restrictToTypes);
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(cd, ObjectUtil.toMap("onOrBefore=startDate-1d"));
    }

    public CohortDefinition getPatientsWithCodedObsByEndOfPreviousDate(Concept question, List<EncounterType> restrictToTypes, String olderThan, PatientSetService.TimeModifier timeModifier) {
        CodedObsCohortDefinition cd = new CodedObsCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(question);
        cd.setEncounterTypeList(restrictToTypes);
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(cd, ObjectUtil.toMap("onOrBefore=startDate-" + olderThan));
    }

    public CohortDefinition getPatientsWithCodedObsByEndOfPreviousDate(Concept question, List<EncounterType> restrictToTypes, List<Concept> codedValues, PatientSetService.TimeModifier timeModifier) {
        CodedObsCohortDefinition cd = new CodedObsCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(question);
        cd.setEncounterTypeList(restrictToTypes);
        cd.setOperator(SetComparator.IN);
        cd.setValueList(codedValues);
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(cd, ObjectUtil.toMap("onOrBefore=startDate-1d"));
    }

    public CohortDefinition getPatientsWithCodedObsByEndOfPreviousDate(Concept question, List<EncounterType> restrictToTypes, List<Concept> codedValues, String olderThan, PatientSetService.TimeModifier timeModifier) {
        CodedObsCohortDefinition cd = new CodedObsCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(question);
        cd.setEncounterTypeList(restrictToTypes);
        cd.setOperator(SetComparator.IN);
        cd.setValueList(codedValues);
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(cd, ObjectUtil.toMap("onOrBefore=startDate-" + olderThan));
    }

    public CohortDefinition getPatientsWithNumericObs(Concept question, List<EncounterType> restrictToTypes, RangeComparator operator, Double value, PatientSetService.TimeModifier timeModifier) {
        NumericObsCohortDefinition cd = new NumericObsCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(question);
        cd.setEncounterTypeList(restrictToTypes);
        cd.setOperator1(operator);
        cd.setValue1(value);
        return cd;
    }

    public CohortDefinition getPatientsWithNumericObsDuringPeriod(Concept question, List<EncounterType> restrictToTypes, RangeComparator operator, Double value, PatientSetService.TimeModifier timeModifier) {
        NumericObsCohortDefinition cd = new NumericObsCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(question);
        cd.setEncounterTypeList(restrictToTypes);
        cd.setOperator1(operator);
        cd.setValue1(value);
        cd.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(cd, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=endDate"));
    }

    public CohortDefinition getPatientsWithNumericObsDuringPeriod(Concept question, List<EncounterType> restrictToTypes, RangeComparator operator, Double value, String olderThan, PatientSetService.TimeModifier timeModifier) {
        NumericObsCohortDefinition cd = new NumericObsCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(question);
        cd.setEncounterTypeList(restrictToTypes);
        cd.setOperator1(operator);
        cd.setValue1(value);
        cd.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(cd, ObjectUtil.toMap("onOrAfter=startDate-" + olderThan + ",onOrBefore=endDate-" + olderThan));
    }

    public CohortDefinition getPatientsWithNumericObsDuringPeriod(Concept question, List<EncounterType> restrictToTypes, PatientSetService.TimeModifier timeModifier) {
        NumericObsCohortDefinition cd = new NumericObsCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(question);
        cd.setEncounterTypeList(restrictToTypes);
        cd.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(cd, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=endDate"));
    }

    public CohortDefinition getPatientsWithNumericObsDuringPeriod(Concept question, List<EncounterType> restrictToTypes, String olderThan, PatientSetService.TimeModifier timeModifier) {
        NumericObsCohortDefinition cd = new NumericObsCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(question);
        cd.setEncounterTypeList(restrictToTypes);
        cd.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(cd, ObjectUtil.toMap("onOrAfter=startDate-" + olderThan + ",onOrBefore=endDate-" + olderThan));
    }

    public CohortDefinition getPatientsWithNumericObsByEndOfPreviousDate(Concept question, List<EncounterType> restrictToTypes, RangeComparator operator, Double value, PatientSetService.TimeModifier timeModifier) {
        NumericObsCohortDefinition cd = new NumericObsCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(question);
        cd.setEncounterTypeList(restrictToTypes);
        cd.setOperator1(operator);
        cd.setValue1(value);
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(cd, ObjectUtil.toMap("onOrBefore=startDate-1d"));
    }

    public CohortDefinition getPatientsWithNumericObsByEndOfPreviousDate(Concept question, List<EncounterType> restrictToTypes, RangeComparator operator, Double value, String olderThan, PatientSetService.TimeModifier timeModifier) {
        NumericObsCohortDefinition cd = new NumericObsCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(question);
        cd.setEncounterTypeList(restrictToTypes);
        cd.setOperator1(operator);
        cd.setValue1(value);
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(cd, ObjectUtil.toMap("onOrBefore=startDate-" + olderThan));
    }

    public CohortDefinition getPatientsWithNumericObsByEndOfPreviousDate(Concept question, List<EncounterType> restrictToTypes, PatientSetService.TimeModifier timeModifier) {
        NumericObsCohortDefinition cd = new NumericObsCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(question);
        cd.setEncounterTypeList(restrictToTypes);
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(cd, ObjectUtil.toMap("onOrBefore=startDate-1d"));
    }

    public CohortDefinition getPatientsWithNumericObsByEndOfPreviousDate(Concept question, List<EncounterType> restrictToTypes, String olderThan, PatientSetService.TimeModifier timeModifier) {
        NumericObsCohortDefinition cd = new NumericObsCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(question);
        cd.setEncounterTypeList(restrictToTypes);
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(cd, ObjectUtil.toMap("onOrBefore=startDate-" + olderThan));
    }


    public CohortDefinition getPatientsWhoseMostRecentCodedObsInValuesByEndDate(Concept question, List<EncounterType> types, Concept... values) {
        CodedObsCohortDefinition cd = new CodedObsCohortDefinition();
        cd.setTimeModifier(PatientSetService.TimeModifier.MAX);
        cd.setQuestion(question);
        cd.setEncounterTypeList(types);
        cd.setOperator(SetComparator.IN);
        cd.setValueList(Arrays.asList(values));
        cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        return convert(cd, ObjectUtil.toMap("onOrBefore=endDate"));
    }

    public CohortDefinition getPatientsWhoseObs(Concept dateConcept, List<EncounterType> types) {
        DateObsCohortDefinition cd = new DateObsCohortDefinition();
        cd.setTimeModifier(PatientSetService.TimeModifier.ANY);
        cd.setQuestion(dateConcept);
        cd.setEncounterTypeList(types);
        return cd;
    }

    public CohortDefinition getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(Concept dateConcept, List<EncounterType> types, PatientSetService.TimeModifier timeModifier) {
        DateObsCohortDefinition cd = new DateObsCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(dateConcept);
        cd.setEncounterTypeList(types);
        cd.setOperator1(RangeComparator.GREATER_EQUAL);
        cd.addParameter(new Parameter("value1", "value1", Date.class));
        cd.setOperator2(RangeComparator.LESS_EQUAL);
        cd.addParameter(new Parameter("value2", "value2", Date.class));
        return convert(cd, ObjectUtil.toMap("value1=startDate,value2=endDate"));
    }

    public CohortDefinition getPatientsWhoseObsValueDateIsByEndDate(Concept dateConcept, List<EncounterType> types, PatientSetService.TimeModifier timeModifier) {
        DateObsCohortDefinition cd = new DateObsCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(dateConcept);
        cd.setEncounterTypeList(types);
        cd.setOperator1(RangeComparator.LESS_EQUAL);
        cd.addParameter(new Parameter("value1", "value1", Date.class));
        return convert(cd, ObjectUtil.toMap("value1=endDate"));
    }

    public CohortDefinition getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(Concept dateConcept, List<EncounterType> types, String olderThan, PatientSetService.TimeModifier timeModifier) {
        DateObsCohortDefinition cd = new DateObsCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(dateConcept);
        cd.setEncounterTypeList(types);
        cd.setOperator1(RangeComparator.GREATER_EQUAL);
        cd.addParameter(new Parameter("value1", "value1", Date.class));
        cd.setOperator2(RangeComparator.LESS_EQUAL);
        cd.addParameter(new Parameter("value2", "value2", Date.class));
        return convert(cd, ObjectUtil.toMap("value1=startDate-" + olderThan + ",value2=endDate-" + olderThan));
    }


    public CohortDefinition getPatientsWhoseMostRecentObsDateIsBetweenValuesByEndDate(Concept dateConcept, List<EncounterType> types, PatientSetService.TimeModifier timeModifier, String olderThan, String onOrPriorTo) {
        DateObsCohortDefinition cd = new DateObsCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(dateConcept);
        cd.setEncounterTypeList(types);
        cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        Map<String, String> params = ObjectUtil.toMap("onOrBefore=endDate");
        if (olderThan != null) {
            cd.setOperator1(RangeComparator.LESS_THAN);
            cd.addParameter(new Parameter("value1", "value1", Date.class));
            params.put("value1", "endDate-" + olderThan);
        }
        if (onOrPriorTo != null) {
            cd.setOperator2(RangeComparator.GREATER_EQUAL);
            cd.addParameter(new Parameter("value2", "value2", Date.class));
            params.put("value2", "endDate-" + onOrPriorTo);
        }
        return convert(cd, params);
    }

    public CohortDefinition getLastVisitInTheQuarter(Concept question, PatientSetService.TimeModifier timeModifier) {
        HavingVisitCohortDefinition cd = new HavingVisitCohortDefinition();
        cd.setTimeModifier(timeModifier);
        cd.setQuestion(question);
        cd.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return convert(cd, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=endDate"));
    }

    public CohortDefinition getLostToFollowUp() {
        LostPatientsCohortDefinition cd = new LostPatientsCohortDefinition();
        cd.setMinimumDays(90);
        cd.addParameter(new Parameter("endDate", "Ending", Date.class));
        return convert(cd, ObjectUtil.toMap("endDate=endDate"));
    }

    public CohortDefinition getLost() {
        LostPatientsCohortDefinition cd = new LostPatientsCohortDefinition();
        cd.setMinimumDays(7);
        cd.setMaximumDays(89);
        cd.addParameter(new Parameter("endDate", "Ending", Date.class));
        return convert(cd, ObjectUtil.toMap("endDate=endDate"));
    }

    public CohortDefinition getTextBasedObs(Concept question) {
        TextObsCohortDefinition cd = new TextObsCohortDefinition();
        cd.setTimeModifier(PatientSetService.TimeModifier.ANY);
        cd.setQuestion(question);
        return cd;
    }


}
