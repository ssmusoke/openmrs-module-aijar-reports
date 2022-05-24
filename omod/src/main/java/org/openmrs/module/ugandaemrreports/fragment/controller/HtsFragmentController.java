package org.openmrs.module.ugandaemrreports.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
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

        //HTS
        q = new HqlQueryBuilder();
        q.select("");
        q.from(Encounter.class);
        q.where(String.format("", startDate, endDate));
        q.groupBy("");

        log.error(q.toString() + " Hts");

        List<List<Object>> hts = administrationService.executeSQL(q.toString(), true);
        log.info(hts.size() + ":Hts Records Returned");

        //add returned record to a model attribute for further processing on the UI
        model.addAttribute("hts", hts);

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

    private String getVlReportingPeriod(AdministrationService administrationService) {

        int monthsToSubtract = Integer.parseInt(administrationService.getGlobalProperty("ugandaemr.dsdm.validPeriodInMothsForViralLoad"));
        LocalDateTime updatedTime = LocalDateTime.now().minusMonths(monthsToSubtract);

        return String.format("%s-%s-%s", updatedTime.getYear(), updatedTime.getMonthValue(), updatedTime.getDayOfMonth());

    }
}
