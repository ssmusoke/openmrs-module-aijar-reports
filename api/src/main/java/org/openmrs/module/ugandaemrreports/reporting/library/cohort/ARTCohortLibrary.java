package org.openmrs.module.ugandaemrreports.reporting.library.cohort;

import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata;
import org.openmrs.module.ugandaemrreports.reporting.utils.CoreUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;

/**
 * ART Library
 */
@Component
public class ARTCohortLibrary {

    /**
     * Patients who on in Care
     *
     * @return the cohort definition
     */
    public CohortDefinition enrolledInCareOnOrBefore() {
        EncounterCohortDefinition cd = new EncounterCohortDefinition();
        cd.setEncounterTypeList(Arrays.asList(CoreUtils.getEncounterType(Metadata.EncounterType.ART_SUMMARY_PAGE)));
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return cd;
    }
}
