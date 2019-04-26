package org.openmrs.module.ugandaemrreports.library;

import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reportingcompatibility.service.ReportService.TimeModifier;
import org.openmrs.module.ugandaemrreports.common.Enums;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.metadata.TBMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 */
@Component
public class TBCohortDefinitionLibrary extends BaseDefinitionLibrary<CohortDefinition> {

    @Autowired
    private DataFactory df;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private TBMetadata tbMetadata;

    @Override
    public Class<? super CohortDefinition> getDefinitionType() {
        return CohortDefinition.class;
    }

    @Override
    public String getKeyPrefix() {
        return "ugemr.cohort.tb.";
    }


    public CohortDefinition getEverEnrolledInTBCare() {
        return df.getPatientsWithIdentifierOfType(tbMetadata.getTBIdentifier());
    }

    public CohortDefinition getNewAndRelapsedPatientsDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getTypeOfPatient(),tbMetadata.getTBFormEncounterType(),tbMetadata.getNewAndRelapsedTBPatients(), BaseObsCohortDefinition.TimeModifier.ANY);
    }
}
