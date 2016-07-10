package org.openmrs.module.aijarreports.library;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.reporting.query.encounter.evaluator.SqlEncounterQueryEvaluator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

public class Cohorts {

    public Log log = LogFactory.getLog(getClass());

    public static SqlCohortDefinition getPatientsWhoEnrolledInCareInYear() {
        SqlCohortDefinition patientsStartedCareInYear = new SqlCohortDefinition("select e.patient_id from encounter e where e.voided = false and e.encounter_type = '8d5b27bc-c2cc-11de-8d13-0010c6dffd0f' and YEAR(:startDate) = YEAR(e.encounter_datetime)");
        patientsStartedCareInYear.addParameter(new Parameter("startDate", "startDate", Date.class));
        return patientsStartedCareInYear;
    }

    public static SqlCohortDefinition getPatientsWithObsDuringQuarter(Concept concept, Integer periodToAdd, String period) {
        String encounterQuery = makeEncounterQuery(periodToAdd, period);

        String dateString = "DATE_ADD(:startDate,INTERVAL " + periodToAdd + " " + period + ")";
        String currentPeriod = period + "(" + dateString + ")";
        String currentYear = "YEAR(" + dateString + ")";

        String workingPeriod = period + "(e.encounter_datetime)";
        String workingYear = "YEAR(e.encounter_datetime)";

        SqlCohortDefinition patientsStartedCareInYear = new SqlCohortDefinition("select o.person_id from obs o where concept_id = " + concept.getId() + " and o.encounter_id in (" + encounterQuery + ")");
        patientsStartedCareInYear.addParameter(new Parameter("startDate", "startDate", Date.class));
        return patientsStartedCareInYear;
    }

    private static String makeEncounterQuery(Integer periodToAdd, String period) {
        String dateString = "DATE_ADD(:startDate,INTERVAL " + periodToAdd + " " + period + ")";
        String currentPeriod = period + "(" + dateString + ")";
        String currentYear = "YEAR(" + dateString + ")";

        String workingPeriod = period + "(e.encounter_datetime)";
        String workingYear = "YEAR(e.encounter_datetime)";

        String condition = currentYear + " = " + workingYear + " and " + currentPeriod + " = " + workingPeriod;

        String query = "select e.encounter_id from encounter e where e.voided = false and " + condition + " group by e.patient_id";

        return query;
    }
}
