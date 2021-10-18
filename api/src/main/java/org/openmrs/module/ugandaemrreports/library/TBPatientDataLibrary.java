package org.openmrs.module.ugandaemrreports.library;

import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.metadata.TBMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



/**
 * Patient data elements for the TB program
 */
@Component
public class TBPatientDataLibrary extends BaseDefinitionLibrary<PatientDataDefinition> {
    @Autowired
    private DataFactory df;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private TBMetadata tbMetadata;

    @Override
    public Class<? super PatientDataDefinition> getDefinitionType() {
        return PatientDataDefinition.class;
    }

    @Override
    public String getKeyPrefix() {
        return "ugemr.tbpatientdata.";
    }

    public PatientDataDefinition getPatientType() {
        return df.getObsByEndDate(tbMetadata.getTypeOfPatient(), tbMetadata.getTBFollowupEncounterType(), TimeQualifier.LAST, df.getObsDatetimeConverter());
    }

    public PatientDataDefinition getTBStatus() {
        return df.getObsByEndDate(tbMetadata.getTBStatus(), tbMetadata.getTBFollowupEncounterType(), TimeQualifier.LAST, df.getObsDatetimeConverter());
    }


}
