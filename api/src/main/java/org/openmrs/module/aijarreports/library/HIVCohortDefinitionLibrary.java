package org.openmrs.module.aijarreports.library;

import org.openmrs.module.aijarreports.common.Period;
import org.openmrs.module.aijarreports.metadata.HIVMetadata;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by carapai on 12/05/2016.
 */
@Component
public class HIVCohortDefinitionLibrary extends BaseDefinitionLibrary<CohortDefinition> {

    @Autowired
    private DataFactory df;

    @Autowired
    private HIVMetadata hivMetadata;

    @Override
    public Class<? super CohortDefinition> getDefinitionType() {
        return CohortDefinition.class;
    }

    @Override
    public String getKeyPrefix() {
        return "aijar.cohort.hiv.";
    }


    public CohortDefinition getEverEnrolledInCare() {
        return df.getPatientsWithIdentifierOfType(hivMetadata.getHIVIdentifier());
    }

    public CohortDefinition getYearlyPatientsOnCare() {
        return df.getPatientsInPeriod(hivMetadata.getARTSummaryPageEncounterType(), Period.YEARLY);
    }

    public CohortDefinition getEnrolledInCareByEndOfPreviousDate(){
        return df.getAnyEncounterOfTypesByEndOfPreviousDate(hivMetadata.getARTSummaryPageEncounterType());
    }

    public CohortDefinition getEnrolledInCareBetweenDates(){
        return df.getAnyEncounterOfTypesBetweenDates(hivMetadata.getARTSummaryPageEncounterType());
    }
}
