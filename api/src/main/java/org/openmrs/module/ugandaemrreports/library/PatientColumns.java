package org.openmrs.module.ugandaemrreports.library;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.patient.definition.EncountersForPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PreferredIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.ObsForPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;

import java.util.Date;
import java.util.List;

/**
 */
public class PatientColumns {

    public static ObsForPersonDataDefinition createObsForPersonData(Concept question, List<EncounterType> encounterTypes, TimeQualifier timeQualifier) {
        ObsForPersonDataDefinition def = new ObsForPersonDataDefinition();
        if (encounterTypes != null) {
            def.setEncounterTypeList(encounterTypes);
        }
        if (timeQualifier != null) {
            def.setWhich(timeQualifier);
        }
        def.setQuestion(question);
        return def;
    }

    public static ObsForPersonDataDefinition createObsForPersonData(Concept question, List<EncounterType> encounterTypes, String parameter, TimeQualifier timeQualifier) {
        ObsForPersonDataDefinition def = createObsForPersonData(question, encounterTypes, timeQualifier);
        def.addParameter(new Parameter(parameter, parameter, Date.class));
        return def;
    }

    public static ObsForPersonDataDefinition createObsForPersonData(Concept question, List<EncounterType> encounterTypes, List<String> parameters, TimeQualifier timeQualifier) {
        ObsForPersonDataDefinition def = createObsForPersonData(question, encounterTypes, timeQualifier);
        if (parameters != null) {
            for (String p : parameters) {
                def.addParameter(new Parameter(p, p, Date.class));
            }
        }
        return def;
    }

    public static PersonAttributeDataDefinition createAttributeForPersonData(String name, PersonAttributeType personAttributeType) {
        PersonAttributeDataDefinition def = new PersonAttributeDataDefinition();
        def.setName(name);
        def.setPersonAttributeType(personAttributeType);
        return def;
    }

    public static PreferredIdentifierDataDefinition createIdentifierForPersonData(String name, PatientIdentifierType identifierType) {
        PreferredIdentifierDataDefinition def = new PreferredIdentifierDataDefinition();
        def.setName(name);
        if (identifierType != null) {
            def.setIdentifierType(identifierType);
        }
        return def;
    }

    public static EncountersForPatientDataDefinition createEncountersForPatientDataDefinition(List<EncounterType> encounterTypes) {
        EncountersForPatientDataDefinition encountersForPatientDataDefinition = new EncountersForPatientDataDefinition();
        encountersForPatientDataDefinition.setTypes(encounterTypes);
        return encountersForPatientDataDefinition;
    }

    public static EncountersForPatientDataDefinition createEncountersForPatientDataDefinition(List<EncounterType> encounterTypes, String parameter) {
        EncountersForPatientDataDefinition encountersForPatientDataDefinition = createEncountersForPatientDataDefinition(encounterTypes);
        encountersForPatientDataDefinition.addParameter(new Parameter(parameter, parameter, Date.class));
        return encountersForPatientDataDefinition;
    }

    public static EncountersForPatientDataDefinition createEncountersForPatientDataDefinition(List<EncounterType> encounterTypes, List<String> parameters) {
        EncountersForPatientDataDefinition encountersForPatientDataDefinition = createEncountersForPatientDataDefinition(encounterTypes);
        if (parameters != null) {
            for (String p : parameters) {
                encountersForPatientDataDefinition.addParameter(new Parameter(p, p, Date.class));
            }
        }
        return encountersForPatientDataDefinition;
    }

}
