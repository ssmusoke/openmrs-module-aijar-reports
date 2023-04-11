package org.openmrs.module.ugandaemrreports.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

public class CareFragmentController {


    protected final Log log = LogFactory.getLog(getClass());

    @Autowired
    EncounterService encounterService;

    @Autowired
    HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    EvaluationContext evaluationContext;

    HqlQueryBuilder q = null;
    String dateSpliter = "=:";

    public void controller(FragmentModel model, @SpringBean("patientService") PatientService patientService,
                           @RequestHeader HttpHeaders headers) {

        if (headers != null) {
            headers.forEach((key, value) -> {
                log.info(String.format("================Header '%s' = %s", key, value));
            });
        }

        evaluationContext=new EvaluationContext();

        //Computing Reporting period basing on the Current Date on the Server/Pc.
        model.addAttribute("quarter", getReportingPeriod().split(dateSpliter)[0]);
        model.addAttribute("sdate", getReportingPeriod().split(dateSpliter)[1]);
        model.addAttribute("edate", getReportingPeriod().split(dateSpliter)[2]);

        //Automatically generated basing on the server/pc date.
        String startDate = (getReportingPeriod().split(dateSpliter)[1]).trim();
        String endDate = (getReportingPeriod().split(dateSpliter)[2]).trim();

        AdministrationService administrationService = Context.getAdministrationService();

        //Monthly enrollments
        //Query returning the number of client whose enrollment date fall within the same month => Month:Number Enrolled
        q = new HqlQueryBuilder();
        q.select("monthname(encounter_datetime) as 'Month',count(month(encounter_datetime)) as 'Number_Enrolled_In_A_Month'");
        q.from(Encounter.class);
        q.where(String.format("encounter_type =(select et.encounter_type_id from encounter_type et where et.uuid='%s') and encounter_datetime between '%s' and '%s'", "8d5b27bc-c2cc-11de-8d13-0010c6dffd0f", startDate, endDate));
        q.groupBy("month(encounter_datetime)");

        log.info(q.toString() + " Query to return enrollments");

        List<List<Object>> monthly_enrollments_in_period = administrationService.executeSQL(q.toString(), true);
        log.info(monthly_enrollments_in_period.size() + "=============== Size of records returned============");

        //add returned record to a model attribute for further processing on the UI
        model.addAttribute("monthly_enrollments_in_period", monthly_enrollments_in_period);

        //Total enrolled in Care in a specified period=>TX_New in a period
        //select count(*) from encounter e where e.encounter_type =(select et.encounter_type_id from encounter_type et where et.uuid='8d5b27bc-c2cc-11de-8d13-0010c6dffd0f') and e.encounter_datetime between '2022-04-01' and '2022-07-31';
        q = new HqlQueryBuilder();
        q.select("count(*) as 'Total_Enrolled_In_A_Period'");
        q.from(Encounter.class);
        q.where(String.format("encounter_type =(select et.encounter_type_id from encounter_type et where et.uuid='8d5b27bc-c2cc-11de-8d13-0010c6dffd0f') and encounter_datetime between '%s' and '%s'", startDate, endDate));

        log.info(q.toString() + " Query to return total enrollments in a certain period");

        List<List<Object>> t_enrolled = administrationService.executeSQL(q.toString(), true);

        model.addAttribute("total_enrolled_in_a_period", t_enrolled.get(0).get(0));
        log.info("===========:" + t_enrolled.get(0).get(0) + ":total_enrolled_in_a_period==============");


        //Count all patient available and add to a model attribute(B)
        long total_patients_system = 0;

        if (patientService != null) {
            //total_patients_system = patientService.getAllPatients().stream().filter(c -> c.getDead() != true).count();
            total_patients_system = patientService.getAllPatients().size();
            model.addAttribute("total_patients_system", total_patients_system);
            log.info("===========:" + total_patients_system + ":total_patients_system==============");

        } else {
            log.info("===========:getting records from database total_patients_system_database==============");
            q = new HqlQueryBuilder();
            q.select("count(*) as 'total_patients_system'");
            q.from(Encounter.class);
            q.where(String.format("encounter_type =(select et.encounter_type_id from encounter_type et where et.uuid='8d5b27bc-c2cc-11de-8d13-0010c6dffd0f')"));

            /*before
                q.select("count(*) as 'total_patients_system'");
                q.from(Patient.class);
            */
            //q.where(String.format("add where close to eliminate deceased and lost to follow up"));

            log.info(q.toString() + " getting records from database total_patients_system");

            List<List<Object>> t_enrolled_in_system = administrationService.executeSQL(q.toString(), true);

            total_patients_system = (long) t_enrolled_in_system.get(0).get(0);
            model.addAttribute("total_patients_system", t_enrolled_in_system);
        }


        //Total Enrolled in Care- all clients with summary page(A)
        //select count(*) from encounter e where e.encounter_type =(select et.encounter_type_id from encounter_type et where et.uuid='8d5b27bc-c2cc-11de-8d13-0010c6dffd0f') and e.encounter_datetime between '2022-04-01' and '2022-07-31';
        q = new HqlQueryBuilder();
        q.select("count(distinct patient_id) as 'total_enrolled_tn_a_care'");
        q.from(Encounter.class);
        q.where(String.format("encounter_type =(select et.encounter_type_id from encounter_type et where et.uuid='8d5b27bc-c2cc-11de-8d13-0010c6dffd0f')"));

        log.info(q.toString() + " Query to return total enrollments in a care");

        List<List<Object>> t_enrolled_in_care = administrationService.executeSQL(q.toString(), true);

        long total_enrolled_in_a_care = (long) t_enrolled_in_care.get(0).get(0);
        model.addAttribute("total_enrolled_in_a_care", total_enrolled_in_a_care);
        log.info("===========:" + total_enrolled_in_a_care + ":total_enrolled_in_a_care==============");


        //Not Active in Care
        //remove transferred out,deceased
        long not_active_in_care = total_patients_system - total_enrolled_in_a_care;
        model.addAttribute("not_active_in_care", not_active_in_care);


        //Tx New in a period
        //select count(*) as 'Tx_NEW' from (select person_id,max(value_datetime) from obs
        // where concept_id=99161 and value_datetime between '%s' and '%s'  group by person_id) tx_new
        q = new HqlQueryBuilder();
        q.select(String.format("count(*) as 'tx_new_new_on_art' from (select max(value_datetime) from %s where concept_id=%s and value_datetime between '%s' and '%s'  group by person_id) tx_new_new_on_art", "obs", 99161, startDate, endDate));

        log.info(q.toString() + " New on ART in a specified period");

        List<List<Object>> tx_new_new_on_art = administrationService.executeSQL(q.toString(), true);

        long tx_new_new_on_art1 = (long) tx_new_new_on_art.get(0).get(0);
        model.addAttribute("tx_new_new_on_art_period", tx_new_new_on_art1);
        log.info("===========:" + tx_new_new_on_art1 + ":tx_new_new_on_art1==============");

        //viral load chart
        //concept ids to use 163023 to get the date breed and 856 for the number of copies comapre the with obs_datetime
        //to obtain all those with 12months
        //select count(*) from (select distinct(person_id),max(value_datetime) from obs where concept_id=163023 and value_datetime >='%s'  group by person_id) bt
        q = new HqlQueryBuilder();
        q.select(String.format("count(*) from (select distinct(person_id),max(obs_datetime) as 'date_bread' from %s where concept_id=%s and obs_datetime >='%s'  group by person_id) bt", "obs", 163023, getVlReportingPeriod(administrationService)));

        log.info(q.toString() + " Clients with Vl");

        List<List<Object>> tt_clients_with_vl = administrationService.executeSQL(q.toString(), true);
        model.addAttribute("tt_clients_with_vl_in_period", tt_clients_with_vl.get(0).get(0));

        //suppressed and non suppressed
        //select distinct(person_id),value_numeric,max(obs_datetime) from obs where concept_id=856 and obs_datetime >='2021-10-01' group by person_id
        q = new HqlQueryBuilder();
        q.select(String.format("distinct(person_id),value_numeric,max(obs_datetime) from %s", "obs"));
        q.where(String.format("concept_id=%s and obs_datetime >='%s'", 856, getVlReportingPeriod(administrationService)));
        q.groupBy("person_id");
        log.info(q.toString() + " suppressed and non suppressed");

        List<List<Object>> tt_supplessed_and_non_supplessed = administrationService.executeSQL(q.toString(), true);

        int suppressed = 0, non_suppressed = 0;
        if (tt_supplessed_and_non_supplessed.size() != 0) {
            int numberOfCopies = Integer.parseInt(administrationService.getGlobalProperty("ugandaemr.dsdm.viralloadSuppressionCopies"));
            for (List<Object> item : tt_supplessed_and_non_supplessed) {
                int vl = (int) Double.parseDouble(item.get(1).toString());
                if (vl <= numberOfCopies)
                    suppressed += 1;
                else
                    non_suppressed += 1;
            }
        }
        model.addAttribute("tt_suppressed_in_period", suppressed);
        model.addAttribute("tt_non_suppressed_in_period", non_suppressed);

        //To be tried out in the next release
        /*
            CohortDefinition cohortDefinition = hivCohortDefinitionLibrary.getPatientsTxLostToFollowupByDays("28");

            try {
                Cohort cohort =Context.getService(CohortDefinitionService.class).evaluate(cohortDefinition, evaluationContext);
                log.error(cohort.size() + " Testing_Cohort definitions===========================");

            } catch (EvaluationException e) {
                throw new RuntimeException(e);
            }
        */
    }

    private String getReportingPeriod() {

        LocalDateTime now = LocalDateTime.now();

        Month m = now.getMonth();
        String quarter = "", startDate = "", endDate = "";

        if (m.getValue() == 1 || m.getValue() == 2 || m.getValue() == 3) {
            //Q1
            startDate = String.format("%s-%s-%s", now.getYear(), 01, 01);
            endDate = String.format("%s-%s-%s", now.getYear(), 3, 31);
            quarter = String.format("Q1 %s (Jan-Mar)", now.getYear());

        } else if (m.getValue() == 4 || m.getValue() == 5 || m.getValue() == 6) {
            //Q2
            startDate = String.format("%s-%s-%s", now.getYear(), 04, 01);
            endDate = String.format("%s-%s-%s", now.getYear(), 6, 30);
            quarter = String.format("Q2 %s (Apr-Jun)", now.getYear());

        } else if (m.getValue() == 7 || m.getValue() == 8 || m.getValue() == 9) {
            startDate = String.format("%s-%s-%s", now.getYear(), 07, 01);
            endDate = String.format("%s-%s-%s", now.getYear(), 9, 30);
            quarter = String.format("Q3 %s (Jul-Sept)", now.getYear());

        } else {
            startDate = String.format("%s-%s-%s", now.getYear(), 10, 01);
            endDate = String.format("%s-%s-%s", now.getYear(), 12, 31);
            quarter = String.format("Q4 %s (Oct-Dec)", now.getYear());
        }

        return String.format("%s" + dateSpliter + "%s" + dateSpliter + "%s", quarter, startDate, endDate);
    }

    private String getVlReportingPeriod(AdministrationService administrationService) {

        int monthsToSubtract = Integer.parseInt(administrationService.getGlobalProperty("ugandaemr.dsdm.validPeriodInMothsForViralLoad"));
        LocalDateTime updatedTime = LocalDateTime.now().minusMonths(monthsToSubtract);

        return String.format("%s-%s-%s", updatedTime.getYear(), updatedTime.getMonthValue(), updatedTime.getDayOfMonth());

    }
}
