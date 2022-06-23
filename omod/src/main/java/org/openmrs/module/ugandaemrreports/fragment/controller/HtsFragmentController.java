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
        String startDate = (getReportingPeriod().split(dateSpliter)[1]).trim();
        String endDate = (getReportingPeriod().split(dateSpliter)[2]).trim();

        AdministrationService administrationService = Context.getAdministrationService();

        //Total enrolled in Care in a specified period; New ON ART
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
        //NOT NEEDED ANY MORE DELETE AFTER TESTING
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


        //Reason for Testing =Pie= Compute percentages to plot to graph:
        q = new HqlQueryBuilder();
        q.select("distinct(person_id), value_coded, max(obs_datetime)");
        q.from(Obs.class);
        q.where(String.format("concept_id=%s and obs_datetime between '%s' and '%s'",165168, startDate, endDate));
        q.groupBy("person_id");

        log.error(q.toString() + " query reason for testing");

        List<List<Object>> hts_reason_4_testing = administrationService.executeSQL(q.toString(), true);
        int total_reason_4_testing= hts_reason_4_testing.size();

        log.info(total_reason_4_testing + ":Hts Records Returned");

        int hts_apn = 0, hts_iapn = 0,hts_prep = 0,hts_pep = 0,hts_hitp = 0,hts_ihr = 0,hts_si = 0
                ,hts_pmtct = 0,hts_o = 0,hts_sns = 0;

        if (hts_reason_4_testing.size() != 0) {
            for (List<Object> item : hts_reason_4_testing) {
                int drs = (int) Double.parseDouble(item.get(1).toString());
                switch(drs) {
                    case 165161:
                        hts_apn += 1;
                        break;
                    case 165162:
                        hts_iapn += 1;
                        break;
                    case 165163:
                        hts_prep += 1;
                        break;
                    case 99056:
                        hts_pep += 1;
                        break;
                    case 165164:
                        hts_hitp += 1;
                        break;
                    case 165165:
                        hts_ihr += 1;
                        break;
                    case 165166:
                        hts_si += 1;
                        break;
                    case 90012:
                        hts_pmtct += 1;
                        break;
                    case 90002:
                        hts_o += 1;
                        break;
                    case 166506:
                        hts_sns+=1;
                        break;
                }
            }
        }

        //percentage equivalent, compute and add to model attribute for rendering to UI
        if(total_reason_4_testing!=0) {
            model.addAttribute("hts_apn", (hts_apn / total_reason_4_testing) * 100);
            model.addAttribute("hts_iapn", (hts_iapn / total_reason_4_testing) * 100);
            model.addAttribute("hts_prep", (hts_prep / total_reason_4_testing) * 100);
            model.addAttribute("hts_pep", (hts_pep / total_reason_4_testing) * 100);
            model.addAttribute("hts_hitp", (hts_hitp / total_reason_4_testing) * 100);
            model.addAttribute("hts_ihr", (hts_ihr / total_reason_4_testing) * 100);
            model.addAttribute("hts_si", (hts_si / total_reason_4_testing) * 100);
            model.addAttribute("hts_pmtct", (hts_pmtct / total_reason_4_testing) * 100);
            model.addAttribute("hts_o", (hts_o / total_reason_4_testing) * 100);
            model.addAttribute("hts_sns", (hts_sns / total_reason_4_testing) * 100);
        }else{
            model.addAttribute("hts_apn", hts_apn);
            model.addAttribute("hts_iapn", hts_iapn );
            model.addAttribute("hts_prep", hts_prep );
            model.addAttribute("hts_pep", hts_pep );
            model.addAttribute("hts_hitp", hts_hitp );
            model.addAttribute("hts_ihr", hts_ihr );
            model.addAttribute("hts_si", hts_si );
            model.addAttribute("hts_pmtct", hts_pmtct );
            model.addAttribute("hts_o", hts_o );
            model.addAttribute("hts_sns", hts_sns );
        }


        //Entry Point For Health Facility Testing: Pie= Compute percentages to plot to graph:
        q = new HqlQueryBuilder();
        q.select("distinct(person_id), value_coded, max(obs_datetime)");
        q.from(Obs.class);
        q.where(String.format("concept_id=%s and obs_datetime between '%s' and '%s'",162925, startDate, endDate));
        q.groupBy("person_id");

        log.error(q.toString() + " query entry point");

        List<List<Object>> hts_entry_point = administrationService.executeSQL(q.toString(), true);
        int hts_entry_point_size= hts_entry_point.size();

        log.info(hts_entry_point_size + ":Hts Records Returned Entry point");

        int htc_e_opd = 0, htc_e_ward = 0,htc_e_artc = 0,htc_e_tbc = 0, htc_e_nu = 0,htc_e_ycc = 0,htc_e_anc = 0
                , htc_e_m = 0,htc_e_pnc = 0,htc_e_fp = 0, htc_e_stic = 0,htc_e_o = 0;

        if (hts_entry_point.size() != 0) {
            for (List<Object> item : hts_entry_point) {
                int drs = (int) Double.parseDouble(item.get(1).toString());
                switch(drs) {
                    case 160542:
                        htc_e_opd += 1;
                        break;
                    case 165179:
                        htc_e_ward += 1;
                        break;
                    case 165047:
                        htc_e_artc += 1;
                        break;
                    case 165048:
                        htc_e_tbc += 1;
                        break;
                    case 165156:
                        htc_e_nu += 1;
                        break;
                    case 99593:
                        htc_e_ycc += 1;
                        break;
                    case 164983:
                        htc_e_anc += 1;
                        break;
                    case 160456:
                        htc_e_m += 1;
                        break;
                    case 165046:
                        htc_e_pnc += 1;
                        break;
                    case 164984:
                        htc_e_fp+=1;
                        break;
                    case 90015:
                        htc_e_stic+=1;
                        break;
                    case 90002:
                        htc_e_o+=1;
                        break;


                }
            }
        }

        //percentage equivalent, compute and add to model attribute for rendering to UI
        if(hts_entry_point_size!=0) {
            model.addAttribute("htc_e_opd", (htc_e_opd / hts_entry_point_size) * 100);
            model.addAttribute("htc_e_ward", (htc_e_ward / hts_entry_point_size) * 100);
            model.addAttribute("htc_e_artc", (htc_e_artc / hts_entry_point_size) * 100);
            model.addAttribute("htc_e_tbc", (htc_e_tbc / hts_entry_point_size) * 100);
            model.addAttribute("htc_e_nu", (htc_e_nu / hts_entry_point_size) * 100);
            model.addAttribute("htc_e_ycc", (htc_e_ycc / hts_entry_point_size) * 100);
            model.addAttribute("htc_e_anc", (htc_e_anc / hts_entry_point_size) * 100);
            model.addAttribute("htc_e_m", (htc_e_m / hts_entry_point_size) * 100);
            model.addAttribute("htc_e_pnc", (htc_e_pnc / hts_entry_point_size) * 100);
            model.addAttribute("htc_e_fp", (htc_e_fp / hts_entry_point_size) * 100);
            model.addAttribute("htc_e_stic", (htc_e_stic / hts_entry_point_size) * 100);
            model.addAttribute("htc_e_o", (htc_e_o / hts_entry_point_size) * 100);
        }else{
            model.addAttribute("htc_e_opd", htc_e_opd);
            model.addAttribute("htc_e_ward", htc_e_ward);
            model.addAttribute("htc_e_artc", htc_e_artc);
            model.addAttribute("htc_e_tbc", htc_e_tbc);
            model.addAttribute("htc_e_nu", htc_e_nu);
            model.addAttribute("htc_e_ycc", htc_e_ycc);
            model.addAttribute("htc_e_anc", htc_e_anc);
            model.addAttribute("htc_e_m", htc_e_m);
            model.addAttribute("htc_e_pnc", htc_e_pnc);
            model.addAttribute("htc_e_fp", htc_e_fp );
            model.addAttribute("htc_e_stic", htc_e_stic);
            model.addAttribute("htc_e_o", htc_e_o );
        }


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
}
