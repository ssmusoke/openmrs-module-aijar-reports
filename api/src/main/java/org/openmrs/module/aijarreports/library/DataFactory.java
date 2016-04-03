package org.openmrs.module.aijarreports.library;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.api.PatientSetService;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.DateObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.MappedParametersCohortDefinition;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.ConvertedDataDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.ChainedConverter;
import org.openmrs.module.reporting.data.converter.CollectionConverter;
import org.openmrs.module.reporting.data.converter.CollectionElementConverter;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.DataSetRowConverter;
import org.openmrs.module.reporting.data.converter.ListConverter;
import org.openmrs.module.reporting.data.converter.NullValueConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.converter.PropertyConverter;
import org.openmrs.module.reporting.data.encounter.definition.ConvertedEncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PersonToPatientDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.query.encounter.definition.EncounterQuery;
import org.openmrs.module.reporting.query.encounter.definition.MappedParametersEncounterQuery;
import org.springframework.stereotype.Component;

@Component
public class DataFactory {

	public Parameter getStartDateParameter() {
		return new Parameter("startDate", "Start Date", Date.class);
	}

	public Parameter getEndDateParameter() {
		return new Parameter("endDate", "End Date", Date.class);
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

	public DataConverter getProgramEnrollmentDateConverter() {
		return new PropertyConverter(PatientProgram.class, "dateEnrolled");
	}

	public DataConverter getStateNameConverter() {
		return new ChainedConverter(new PropertyConverter(PatientState.class, "state.concept"), getObjectFormatter());
	}

	public DataConverter getStateStartDateConverter() {
		return new PropertyConverter(PatientState.class, "startDate");
	}

	public DataConverter getStateLocationConverter() {
		return new ChainedConverter(new PropertyConverter(PatientState.class, "patientProgram.location"), new ObjectFormatter());
	}

	public DataConverter getStateProgramEnrollmentDateConverter() {
		return new PropertyConverter(PatientState.class, "patientProgram.dateEnrolled");
	}

	public DataConverter getStateNameAndDateFormatter() {
		return new ObjectFormatter("{patientProgram.program}: {state} (since {startDate|yyyy-MM-dd})");
	}

	public DataConverter getActiveStatesAsStringConverter() {
		ChainedConverter converter = new ChainedConverter();
		converter.addConverter(new CollectionConverter(getStateNameAndDateFormatter(), false, null));
		converter.addConverter(new ObjectFormatter("; "));
		return converter;
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

	// Convenience methods

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
}
