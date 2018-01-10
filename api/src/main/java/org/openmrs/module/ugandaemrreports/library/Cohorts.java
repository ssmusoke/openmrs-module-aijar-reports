package org.openmrs.module.ugandaemrreports.library;

import com.google.common.base.Joiner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.*;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata;
import org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Cohorts {

    public Log log = LogFactory.getLog(getClass());

    public static SqlCohortDefinition getPatientsWhoEnrolledInCareInYear() {
        SqlCohortDefinition patientsStartedCareInYear = new SqlCohortDefinition("select e.patient_id from encounter e inner join encounter_type et on(e.encounter_type = et.encounter_type_id and et.uuid = '8d5b27bc-c2cc-11de-8d13-0010c6dffd0f' and YEAR(:startDate) = YEAR(e.encounter_datetime) and e.voided = 0)");
        patientsStartedCareInYear.addParameter(new Parameter("startDate", "startDate", Date.class));
        return patientsStartedCareInYear;
    }

    public static SqlCohortDefinition getPatientsWhoEnrolledInCareUntilDate() {
        SqlCohortDefinition patientsStartedCareByDate = new SqlCohortDefinition("select e.patient_id from encounter e INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id where e.voided = false and et.uuid = '8d5b27bc-c2cc-11de-8d13-0010c6dffd0f' AND (TO_DAYS(e.encounter_datetime) < TO_DAYS(:endDate))");

        patientsStartedCareByDate.addParameter(new Parameter("endDate", "endDate", Date.class));
        return patientsStartedCareByDate;
    }

    public static SqlCohortDefinition getPatientHavingARTDuringMonth() {
        SqlCohortDefinition patientsStartedCareInYear = new SqlCohortDefinition("select o.person_id from obs o where o.voided = false and o.concept_id in (99161) and (EXTRACT(YEAR_MONTH FROM :startDate) = EXTRACT(YEAR_MONTH FROM o.value_datetime) OR EXTRACT(YEAR_MONTH FROM :startDate) = EXTRACT(YEAR_MONTH FROM o.obs_datetime)) group by o.person_id");
        patientsStartedCareInYear.addParameter(new Parameter("startDate", "startDate", Date.class));
        return patientsStartedCareInYear;
    }

    public static SqlCohortDefinition getPatientsWithAtLeastOneMissedAppointmentAfterArtStartDate() {
        SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition("select e.patient_id from encounter e inner join obs oi on(oi.concept_id = 99161 and oi.person_id = e.patient_id and e.encounter_datetime  BETWEEN oi.value_datetime and DATE_ADD(oi.value_datetime, INTERVAL 1 YEAR)) where e.encounter_id not in (select o.encounter_id from obs o where o.concept_id = 90069 group by o.encounter_id ) group by e.patient_id");
        return sqlCohortDefinition;
    }

    public static SqlCohortDefinition getPatientsWithAtLeastOneScheduledVisitAfterArtStartDate() {
        SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition("select e.patient_id from encounter e inner join obs oi on(oi.concept_id = 99161 and oi.person_id = e.patient_id and e.encounter_datetime  BETWEEN oi.value_datetime and DATE_ADD(oi.value_datetime, INTERVAL 1 YEAR)) where e.encounter_id in (select o.encounter_id from obs o where o.concept_id = 90069 group by o.encounter_id ) group by e.patient_id");
        return sqlCohortDefinition;
    }

    public static SqlCohortDefinition getPatientHavingARTBeforeMonth() {
        SqlCohortDefinition patientsStartedCareInYear = new SqlCohortDefinition("select o.person_id from obs o where o.voided = false and o.concept_id in (99161) and (EXTRACT(YEAR_MONTH FROM :startDate) < EXTRACT(YEAR_MONTH FROM o.value_datetime) OR EXTRACT(YEAR_MONTH FROM :startDate) < EXTRACT(YEAR_MONTH FROM o.obs_datetime)) group by o.person_id");
        patientsStartedCareInYear.addParameter(new Parameter("startDate", "startDate", Date.class));
        return patientsStartedCareInYear;
    }

    public static SqlCohortDefinition getMissedAppointments() {
        SqlCohortDefinition cohortDefinition = new SqlCohortDefinition("select person_id from (select person_id,MAX(value_datetime) as 'return_date' from obs where concept_id = 5096 and value_datetime BETWEEN ':date1' AND ':date2' group by person_id) A inner join  (select patient_id,MAX(encounter_datetime) as 'encounter_date' from encounter e where encounter_datetime <= ':date3' group by patient_id) B on (A.person_id = B.patient_id) where DATEDIFF(B.encounter_date,A.return_date) between 8 and 89");
        cohortDefinition.addParameter(new Parameter("date1", "date1", Date.class));
        cohortDefinition.addParameter(new Parameter("date2", "date2", Date.class));
        cohortDefinition.addParameter(new Parameter("date3", "date3", Date.class));
        return cohortDefinition;

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

    public static CodedObsCohortDefinition createCodedObsCohortDefinition(Concept question, Concept value,
                                                                          SetComparator setComparator,
                                                                          BaseObsCohortDefinition.TimeModifier timeModifier) {
        CodedObsCohortDefinition obsCohortDefinition = new CodedObsCohortDefinition();

        if (question != null) {
            obsCohortDefinition.setQuestion(question);
        }
        if (setComparator != null) {
            obsCohortDefinition.setOperator(setComparator);
        }
        if (timeModifier != null) {
            obsCohortDefinition.setTimeModifier(timeModifier);
        }

        List<Concept> valueList = new ArrayList<Concept>();
        if (value != null) {
            valueList.add(value);
            obsCohortDefinition.setValueList(valueList);
        }
        return obsCohortDefinition;
    }

    public static CodedObsCohortDefinition createCodedObsCohortDefinition(String name, Concept question, Concept value,
                                                                          SetComparator setComparator,
                                                                          BaseObsCohortDefinition.TimeModifier timeModifier) {
        CodedObsCohortDefinition obsCohortDefinition = createCodedObsCohortDefinition(question, value, setComparator,
                timeModifier);
        obsCohortDefinition.setName(name);
        return obsCohortDefinition;
    }

    public static CodedObsCohortDefinition createCodedObsCohortDefinition(String name, String parameterName,
                                                                          Concept question, Concept value,
                                                                          SetComparator setComparator,
                                                                          BaseObsCohortDefinition.TimeModifier timeModifier) {
        CodedObsCohortDefinition obsCohortDefinition = createCodedObsCohortDefinition(name, question, value, setComparator,
                timeModifier);
        if (parameterName != null) {
            obsCohortDefinition.addParameter(new Parameter(parameterName, parameterName, Date.class));
        }
        return obsCohortDefinition;
    }

    public static CodedObsCohortDefinition createCodedObsCohortDefinition(String name, List<String> parameterNames,
                                                                          Concept question, Concept value,
                                                                          SetComparator setComparator,
                                                                          BaseObsCohortDefinition.TimeModifier timeModifier) {
        CodedObsCohortDefinition obsCohortDefinition = createCodedObsCohortDefinition(name, question, value, setComparator,
                timeModifier);
        if (parameterNames != null) {
            for (String p : parameterNames) {
                obsCohortDefinition.addParameter(new Parameter(p, p, Date.class));
            }
        }
        return obsCohortDefinition;
    }

    public static GenderCohortDefinition createFemaleCohortDefinition(String name) {
        GenderCohortDefinition femaleDefinition = new GenderCohortDefinition();
        femaleDefinition.setName(name);
        femaleDefinition.setFemaleIncluded(true);
        return femaleDefinition;
    }

    public static GenderCohortDefinition createMaleCohortDefinition(String name) {
        GenderCohortDefinition maleDefinition = new GenderCohortDefinition();
        maleDefinition.setName(name);
        maleDefinition.setMaleIncluded(true);
        return maleDefinition;
    }

    public static AgeCohortDefinition createOver15AgeCohort(String name) {
        AgeCohortDefinition over15Cohort = new AgeCohortDefinition();
        over15Cohort.setName(name);
        over15Cohort.setMinAge(new Integer(15));
        over15Cohort.addParameter(new Parameter("effectiveDate", "endDate", Date.class));
        return over15Cohort;
    }

    public static AgeCohortDefinition createUnder15AgeCohort(String name) {
        AgeCohortDefinition under15Cohort = new AgeCohortDefinition();
        under15Cohort.setName(name);
        under15Cohort.setMaxAge(new Integer(14));
        under15Cohort.addParameter(new Parameter("effectiveDate", "endDate", Date.class));
        return under15Cohort;
    }

    public static AgeCohortDefinition createUnderAgeCohort(String name, int age) {
        AgeCohortDefinition underAgeCohort = new AgeCohortDefinition();
        underAgeCohort.setName(name);
        underAgeCohort.setMaxAge(new Integer(age));
        underAgeCohort.addParameter(new Parameter("effectiveDate", "endDate", Date.class));
        return underAgeCohort;
    }

    public static AgeCohortDefinition createUnderAgeCohort(String name, int age, DurationUnit unit) {
        AgeCohortDefinition underAgeCohort = new AgeCohortDefinition();
        underAgeCohort.setName(name);
        underAgeCohort.setMaxAge(new Integer(age));
        underAgeCohort.setMaxAgeUnit(unit);
        underAgeCohort.addParameter(new Parameter("effectiveDate", "endDate", Date.class));
        return underAgeCohort;
    }

    public static AgeCohortDefinition createAboveAgeCohort(String name, int age, DurationUnit unit) {
        AgeCohortDefinition aboveAgeCohort = new AgeCohortDefinition();
        aboveAgeCohort.setName(name);
        aboveAgeCohort.setMinAge(new Integer(age));
        aboveAgeCohort.setMinAgeUnit(unit);
        aboveAgeCohort.addParameter(new Parameter("effectiveDate", "endDate", Date.class));
        return aboveAgeCohort;
    }

    public static AgeCohortDefinition createUnder3AgeCohort(String name) {
        AgeCohortDefinition under3Cohort = new AgeCohortDefinition();
        under3Cohort.setName(name);
        under3Cohort.setMaxAge(new Integer(2));
        under3Cohort.addParameter(new Parameter("effectiveDate", "endDate", Date.class));
        return under3Cohort;
    }

    public static AgeCohortDefinition create3to5AgeCohort(String name) {
        AgeCohortDefinition threeTo5Cohort = new AgeCohortDefinition();
        threeTo5Cohort.setName(name);
        threeTo5Cohort.setMaxAge(new Integer(4));
        threeTo5Cohort.setMinAge(new Integer(3));
        threeTo5Cohort.addParameter(new Parameter("effectiveDate", "endDate", Date.class));
        return threeTo5Cohort;
    }

    public static AgeCohortDefinition createUnder18monthsCohort(String name) {
        AgeCohortDefinition at18monthsOfAge = new AgeCohortDefinition();
        at18monthsOfAge.setName(name);
        at18monthsOfAge.setMinAge(10);
        at18monthsOfAge.setMinAgeUnit(DurationUnit.MONTHS);
        at18monthsOfAge.setMaxAge(17);
        at18monthsOfAge.setMaxAgeUnit(DurationUnit.MONTHS);
        at18monthsOfAge.addParameter(new Parameter("effectiveDate", "endDate", Date.class));
        return at18monthsOfAge;
    }

    public static AgeCohortDefinition createInfantUnder18months(String name) {
        AgeCohortDefinition at18monthsOfAge = new AgeCohortDefinition();
        at18monthsOfAge.setName(name);
        at18monthsOfAge.setMinAge(2);
        at18monthsOfAge.setMinAgeUnit(DurationUnit.WEEKS);
        at18monthsOfAge.setMaxAge(17);
        at18monthsOfAge.setMaxAgeUnit(DurationUnit.MONTHS);
        //at18monthsOfAge.addParameter(new Parameter("effectiveDate", "endDate", Date.class));
        return at18monthsOfAge;
    }

    public static AgeCohortDefinition createXtoYAgeCohort(String name, int minAge, int maxAge) {
        AgeCohortDefinition xToYCohort = new AgeCohortDefinition();
        xToYCohort.setName(name);
        xToYCohort.setMaxAge(new Integer(maxAge));
        xToYCohort.setMinAge(new Integer(minAge));
        xToYCohort.addParameter(new Parameter("effectiveDate", "endDate", Date.class));
        return xToYCohort;
    }

    public static AgeCohortDefinition createOver5AgeCohort(String name) {
        AgeCohortDefinition over5Cohort = new AgeCohortDefinition();
        over5Cohort.setName(name);
        over5Cohort.setMinAge(new Integer(5));
        over5Cohort.addParameter(new Parameter("effectiveDate", "endDate", Date.class));
        return over5Cohort;
    }

    public static AgeCohortDefinition createOverXAgeCohort(String name, int minAge) {
        AgeCohortDefinition overXCohort = new AgeCohortDefinition();
        overXCohort.setName(name);
        overXCohort.setMinAge(new Integer(minAge));
        overXCohort.addParameter(new Parameter("effectiveDate", "endDate", Date.class));
        return overXCohort;
    }

    public static AgeCohortDefinition createUnder5YearsAgeCohort(String name) {
        AgeCohortDefinition under5YearsCohort = new AgeCohortDefinition();
        under5YearsCohort.setName(name);
        under5YearsCohort.setMaxAge(new Integer(5));
        under5YearsCohort.setMaxAgeUnit(DurationUnit.YEARS);
        under5YearsCohort.addParameter(new Parameter("effectiveDate", "endDate", Date.class));
        return under5YearsCohort;
    }


    public static EncounterCohortDefinition createEncounterParameterizedByDate(String name, String parameterName,
                                                                               List<EncounterType> encounters) {
        EncounterCohortDefinition encounter = createEncounterParameterizedByDate(name, parameterName);
        encounter.setEncounterTypeList(encounters);
        return encounter;
    }

    public static EncounterCohortDefinition createEncounterParameterizedByDate(String name, List<String> parameterNames,
                                                                               List<EncounterType> encounters) {
        EncounterCohortDefinition encounter = createEncounterParameterizedByDate(name, parameterNames);
        encounter.setEncounterTypeList(encounters);
        return encounter;
    }

    public static EncounterCohortDefinition createEncounterParameterizedByDate(String name, List<String> parameterNames,
                                                                               EncounterType encounterType) {
        List<EncounterType> encounters = new ArrayList<EncounterType>();
        encounters.add(encounterType);

        EncounterCohortDefinition encounter = createEncounterParameterizedByDate(name, parameterNames);
        encounter.setEncounterTypeList(encounters);
        return encounter;
    }

    public static EncounterCohortDefinition createEncounterParameterizedByDate(String name, String parameterName) {
        EncounterCohortDefinition encounter = new EncounterCohortDefinition();
        encounter.setName(name);
        encounter.addParameter(new Parameter(parameterName, parameterName, Date.class));
        return encounter;
    }

    public static EncounterCohortDefinition createEncounterParameterizedByDate(String name, List<String> parameterNames) {
        EncounterCohortDefinition encounter = new EncounterCohortDefinition();
        encounter.setName(name);
        if (parameterNames != null) {
            for (String p : parameterNames) {
                encounter.addParameter(new Parameter(p, p, Date.class));
            }
        }
        return encounter;
    }

    public static EncounterCohortDefinition createEncounterBasedOnForms(String name, String parameterName, List<Form> forms) {
        EncounterCohortDefinition encounter = createEncounterParameterizedByDate(name, parameterName);
        encounter.setFormList(forms);
        return encounter;
    }

    public static EncounterCohortDefinition createEncounterBasedOnForms(String name, List<String> parameterNames,
                                                                        List<Form> forms) {
        EncounterCohortDefinition encounter = createEncounterParameterizedByDate(name, parameterNames);
        encounter.setFormList(forms);
        return encounter;
    }

    public static EncounterCohortDefinition createEncounterBasedOnForms(String name, List<Form> forms) {
        EncounterCohortDefinition encounter = new EncounterCohortDefinition();
        encounter.setName(name);
        encounter.setFormList(forms);
        return encounter;
    }


    public static NumericObsCohortDefinition createNumericObsCohortDefinition(String name, Concept question, double value,
                                                                              RangeComparator setComparator,
                                                                              BaseObsCohortDefinition.TimeModifier timeModifier) {

        NumericObsCohortDefinition obsCohortDefinition = new NumericObsCohortDefinition();

        obsCohortDefinition.setName(name);

        if (question != null)
            obsCohortDefinition.setQuestion(question);

        if (setComparator != null)
            obsCohortDefinition.setOperator1(setComparator);

        if (timeModifier != null)
            obsCohortDefinition.setTimeModifier(timeModifier);

        if (value != 0) {
            obsCohortDefinition.setValue1(value);
        }

        return obsCohortDefinition;
    }

    public static NumericObsCohortDefinition createNumericObsCohortDefinition(String name, String parameterName,
                                                                              Concept question, double value,
                                                                              RangeComparator setComparator,
                                                                              BaseObsCohortDefinition.TimeModifier timeModifier) {

        NumericObsCohortDefinition obsCohortDefinition = createNumericObsCohortDefinition(parameterName, question, value,
                setComparator, timeModifier);

        if (parameterName != null) {
            obsCohortDefinition.addParameter(new Parameter(parameterName, parameterName, Date.class));
        }

        return obsCohortDefinition;
    }

    public static NumericObsCohortDefinition createNumericObsCohortDefinition(String name, List<String> parameterNames,
                                                                              Concept question, double value,
                                                                              RangeComparator setComparator,
                                                                              BaseObsCohortDefinition.TimeModifier timeModifier) {

        NumericObsCohortDefinition obsCohortDefinition = createNumericObsCohortDefinition(name, question, value,
                setComparator, timeModifier);

        if (parameterNames != null) {
            for (String p : parameterNames) {
                obsCohortDefinition.addParameter(new Parameter(p, p, Date.class));
            }
        }

        return obsCohortDefinition;
    }

    public static CohortDefinition genderAndHasAncEncounter(boolean female, boolean male, String uuid) {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();

        GenderCohortDefinition gender = new GenderCohortDefinition();
        gender.setName("gender female " + female + " and male " + male);
        gender.setFemaleIncluded(female);
        gender.setMaleIncluded(male);

        EncounterCohortDefinition encounter = new EncounterCohortDefinition();
        encounter.setName("Has encounter");
        encounter.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        encounter.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        encounter.addEncounterType(Context.getEncounterService().getEncounterTypeByUuid(uuid));

        cd.setName("Is specific gender and has " + Context.getEncounterService().getEncounterTypeByUuid(uuid).getName() + " encounter");
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.addSearch("gender", ReportUtils.map(gender));
        cd.addSearch("encounter", ReportUtils.map(encounter, "onOrAfter=${startDate},onOrBefore=${endDate}"));
        cd.setCompositionString("gender AND encounter");

        return cd;
    }

    /**
     * Clients with a Return Visit Date
     *
     * @return
     */
    public static CohortDefinition patientWithAppoinment() {
        DateObsCohortDefinition cd = new DateObsCohortDefinition();
        cd.setName("Has an appointment date");
        cd.setQuestion(Dictionary.getConcept(Metadata.Concept.RETURN_VISIT_DATE));
        List<String> encounterTypes = Arrays.asList(Metadata.EncounterType.ANC_ENCOUNTER, Metadata.EncounterType.ART_ENCOUNTER_PAGE, Metadata.EncounterType.EID_ENCOUNTER_PAGE);
        cd.setEncounterTypeList(Dictionary.getEncounterTypeList(Joiner.on(",").join(encounterTypes)));
        cd.setTimeModifier(BaseObsCohortDefinition.TimeModifier.ANY);
        cd.setOperator1(RangeComparator.GREATER_EQUAL);
        cd.setOperator2(RangeComparator.LESS_EQUAL);
        cd.addParameter(new Parameter("value1", "Before Date", Date.class));
        cd.addParameter(new Parameter("value2", "After Date", Date.class));
        return cd;
    }


}
