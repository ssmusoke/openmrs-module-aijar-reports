package org.openmrs.module.ugandaemrreports.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

public class HtsFragmentController {


    protected final Log log = LogFactory.getLog(getClass());

    @Autowired
    EncounterService encounterService;

    HqlQueryBuilder q = null;
    String dateSpliter = "=:";

    public void controller(FragmentModel model) {

        //Computing Reporting period basing on the Current Date on the Server/Pc.
        model.addAttribute("quarter", getReportingPeriod().split(dateSpliter)[0]);
        model.addAttribute("sdate", getReportingPeriod().split(dateSpliter)[1]);
        model.addAttribute("edate", getReportingPeriod().split(dateSpliter)[2]);

        //Automatically generated basing on the server/pc date.
        String startDate = getReportingPeriod().split(dateSpliter)[1];
        String endDate = getReportingPeriod().split(dateSpliter)[2];

        AdministrationService administrationService = Context.getAdministrationService();

        //Total enrolled in Care in a specified period
        //select count(*) from encounter e where e.encounter_type =(select et.encounter_type_id from encounter_type et where et.uuid='8d5b27bc-c2cc-11de-8d13-0010c6dffd0f') and e.encounter_datetime between '2022-04-01' and '2022-07-31';
        q = new HqlQueryBuilder();
        q.select("count(*) as 'Total_Enrolled_In_A_Period'");
        q.from(Encounter.class);
        q.where(String.format("encounter_type =(select et.encounter_type_id from encounter_type et where et.uuid='8d5b27bc-c2cc-11de-8d13-0010c6dffd0f') and encounter_datetime between '%s' and '%s'", startDate, endDate));

        List<List<Object>> t_enrolled = administrationService.executeSQL(q.toString(), true);
        model.addAttribute("hts_total_enrolled_in_a_period", t_enrolled.get(0).get(0));

        //HTS - Delivery model
        //select distinct(person_id), value_coded, max(obs_datetime) from obs where concept_id=165171 group by person_id and obs_datetime between '%s' and '%s'
        q = new HqlQueryBuilder();
        q.select("distinct(person_id), value_coded, max(obs_datetime)");
        q.from(Obs.class);
        q.where(String.format("concept_id=%s and obs_datetime between '%s' and '%s'",165171, startDate, endDate));
        q.groupBy("person_id");

        log.error(q.toString() + " query delivery model");

        List<List<Object>> hts_dm = administrationService.executeSQL(q.toString(), true);
        log.info(hts_dm.size() + ":Hts Records Returned");
        int ctp = 0, hct = 0,others=0;
        if (hts_dm.size() != 0) {
            for (List<Object> item : hts_dm) {
                int dm = (int) Double.parseDouble(item.get(1).toString());
                if (dm == 165171)
                    ctp += 1;
                else if(dm == 99416)
                    hct += 1;
                else
                    others += 1;
            }
        }

        //add returned record to a model attribute for further processing on the UI
        model.addAttribute("hts_ctp", ctp);
        model.addAttribute("hts_hct", hct);
        model.addAttribute("hts_others", others);


        //HTS - Delivery model
        //select distinct(person_id), value_coded, max(obs_datetime) from obs where concept_id=99493 group by person_id and obs_datetime between '%s' and '%s'
        q = new HqlQueryBuilder();
        q.select("distinct(person_id), value_coded, max(obs_datetime)");
        q.from(Obs.class);
        q.where(String.format("concept_id=%s and obs_datetime between '%s' and '%s'",99493, startDate, endDate));
        q.groupBy("person_id");

        log.error(q.toString() + " query hts-test results");

        List<List<Object>> hts_ts = administrationService.executeSQL(q.toString(), true);
        log.info(hts_ts.size() + ":Hts test results Returned");
        int hts_p = 0, hts_n = 0,hts_inc=0,hts_nt=0;
        if (hts_ts.size() != 0) {
            for (List<Object> item : hts_ts) {
                int hts_result = (int) Double.parseDouble(item.get(1).toString());
                switch(hts_result) {
                    case 162927:
                        hts_nt += 1;
                        break;
                    case 90166:
                        hts_p += 1;
                        break;
                    case 90167:
                        hts_n += 1;
                        break;
                    case 162926:
                        hts_inc += 1;
                        break;
                }
            }
        }

        //add returned record to a model attribute for further processing on the UI
        model.addAttribute("hts_p", hts_p);
        model.addAttribute("hts_n", hts_n);
        model.addAttribute("hts_inc", hts_inc);
        model.addAttribute("hts_nt", hts_nt);

        //HTS - Counselling or not Counselling
        //select distinct(person_id), value_coded, max(obs_datetime) from obs where concept_id=162918 group by person_id and obs_datetime between '%s' and '%s'
        q = new HqlQueryBuilder();
        q.select("distinct(person_id), value_coded, max(obs_datetime)");
        q.from(Obs.class);
        q.where(String.format("concept_id=%s and obs_datetime between '%s' and '%s'",162918, startDate, endDate));
        q.groupBy("person_id");

        log.error(q.toString() + " query Counselling");

        List<List<Object>> hts_counselling = administrationService.executeSQL(q.toString(), true);
        log.info(hts_counselling.size() + ":Hts Records Returned");
        int htc_co = 0, hts_not_co = 0;
        if (hts_counselling.size() != 0) {
            for (List<Object> item : hts_counselling) {
                int dm = (int) Double.parseDouble(item.get(1).toString());
                if (dm == 90004)
                    htc_co += 1;
                else if(dm == 90003)
                    hts_not_co += 1;
            }
        }

        //add returned record to a model attribute for further processing on the UI
        model.addAttribute("htc_co", htc_co);
        model.addAttribute("hts_not_co", hts_not_co);

    }

    private String getReportingPeriod() {

        LocalDateTime now = LocalDateTime.now();

        Month m = now.getMonth();
        String quarter = "", startDate = "", endDate = "";

        if (m.getValue() == 1 || m.getValue() == 2 || m.getValue() == 3) {
            //Q1
            startDate = String.format("%s-%s-%s", now.getYear(), 01, 01);
            endDate = String.format("%s-%s-%s", now.getYear(), 3, 31);
            quarter = String.format("Q1 %s", now.getYear());

        } else if (m.getValue() == 4 || m.getValue() == 5 || m.getValue() == 6) {
            //Q2
            startDate = String.format("%s-%s-%s", now.getYear(), 04, 01);
            endDate = String.format("%s-%s-%s", now.getYear(), 6, 30);
            quarter = String.format("Q2 %s", now.getYear());

        } else if (m.getValue() == 7 || m.getValue() == 8 || m.getValue() == 9) {
            startDate = String.format("%s-%s-%s", now.getYear(), 07, 01);
            endDate = String.format("%s-%s-%s", now.getYear(), 9, 30);
            quarter = String.format("Q3 %s", now.getYear());

        } else {
            startDate = String.format("%s-%s-%s", now.getYear(), 10, 01);
            endDate = String.format("%s-%s-%s", now.getYear(), 12, 31);
            quarter = String.format("Q4 %s", now.getYear());
        }

        return String.format("%s" + dateSpliter + "%s" + dateSpliter + "%s", quarter, startDate, endDate);
    }
}
