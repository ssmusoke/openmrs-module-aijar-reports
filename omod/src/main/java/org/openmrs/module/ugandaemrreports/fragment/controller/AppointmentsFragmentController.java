package org.openmrs.module.ugandaemrreports.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class AppointmentsFragmentController {

    protected final Log log = LogFactory.getLog(getClass());

    @Autowired
    EncounterService encounterService;

    HqlQueryBuilder q = null;
    String dateSpliter = "=:";

    public void controller(FragmentModel model) {

        //Computing Reporting period basing on the Current Date on the Server/Pc.
        model.addAttribute("quarter", getTodayReportingPeriod().split(dateSpliter)[0]);
        model.addAttribute("sdate", getTodayReportingPeriod().split(dateSpliter)[1]);


        //Automatically generated basing on the server/pc date.
        String startDate = (getTodayReportingPeriod().split(dateSpliter)[1]).trim();


        AdministrationService administrationService = Context.getAdministrationService();

        //scheduled appointments
        //select count(*) from obs where concept_id=5096 and date(value_datetime)='2022-06-06'
        q = new HqlQueryBuilder();
        q.select("count(*) as 'today_appointments'");
        q.from(Obs.class);
        q.where(String.format("concept_id=%s and date(value_datetime)='%s'", 5096, startDate));

        List<List<Object>> today_appointments = administrationService.executeSQL(q.toString(), true);
        model.addAttribute("today_appointments", today_appointments.get(0).get(0));


        //visit attended on scheduled
        //select value_numeric from obs where concept_id=90069 and date(obs_datetime)='2022-06-06'
        q = new HqlQueryBuilder();
        q.select("value_numeric");
        q.from(Obs.class);
        q.where(String.format("concept_id=%s and date(obs_datetime) ='%s'",90069, startDate));

        List<List<Object>> v_onattended = administrationService.executeSQL(q.toString(), true);

        log.info(v_onattended.size() + ":onattended");
        int attended_ontime = 0, non_attended_ontime = 0;
        if (v_onattended.size() != 0) {
            for (List<Object> item : v_onattended) {
                int v = (int) Double.parseDouble(item.get(1).toString());
                if (v == 1)
                    attended_ontime += 1;
                else
                    non_attended_ontime += 1;
            }
        }

        //add returned record to a model attribute for further processing on the UI
        model.addAttribute("attended_ontime", attended_ontime);
        model.addAttribute("non_attended_ontime", non_attended_ontime);

        //reasons for the next appointment
        //select value_coded,count(value_coded),max(obs_datetime) from obs where concept_id=160288 group by value_coded
        q = new HqlQueryBuilder();
        q.select("value_coded,count(value_coded) as 'reason_count',max(obs_datetime) as 'max_obs'");
        q.from(Obs.class);
        q.where(String.format("concept_id=%s",160288));
        q.groupBy("value_coded");

        log.error(q.toString() + " reasons for the next appointment");

        List<List<Object>> rn_appointments = administrationService.executeSQL(q.toString(), true);
        log.info(rn_appointments.size() + ":rn_appointments");
        int ra_ai=0,ra_ar=0,ra_ars=0,ra_vl=0,ra_cd4=0,ra_iac=0,ra_tbdr=0,ra_tbfu=0,ra_anc=0,ra_eid=0,ra_pmtct=0,
                ra_hord=0,ra_nc=0,ra_others=0;
        if (rn_appointments.size() != 0) {
            for (List<Object> item : rn_appointments) {
                int na_reason = (int) Double.parseDouble(item.get(1).toString());
                int v_coded = (int) Double.parseDouble(item.get(0).toString());
                switch(v_coded) {
                    case 164968:
                        ra_ai += na_reason;
                        break;
                    case 164972:
                        ra_ar += na_reason;
                        break;
                    case 164970:
                        ra_ars += na_reason;
                        break;
                    case 856:
                        ra_vl += na_reason;
                        break;
                    case 5497:
                        ra_cd4 += na_reason;
                        break;
                    case 163153:
                        ra_iac += na_reason;
                        break;
                    case 164971:
                        ra_tbdr += na_reason;
                        break;
                    case 164973:
                        ra_tbfu += na_reason;
                        break;
                    case 99324:
                        ra_anc += na_reason;
                        break;
                    case 164974:
                        ra_eid += na_reason;
                        break;
                    case 90012:
                        ra_pmtct += na_reason;
                        break;
                    case 903:
                        ra_hord += na_reason;
                        break;
                    case 5484:
                        ra_nc += na_reason;
                        break;
                    case 90002:
                        ra_others += na_reason;
                        break;
                }
            }
        }

        //add returned record to a model attribute for further processing on the UI
        model.addAttribute("ra_ai", ra_ai);
        model.addAttribute("ra_ar", ra_ar);
        model.addAttribute("ra_ars", ra_ars);
        model.addAttribute("ra_vl", ra_vl);
        model.addAttribute("ra_cd4", ra_cd4);
        model.addAttribute("ra_iac", ra_iac);
        model.addAttribute("ra_tbdr", ra_tbdr);
        model.addAttribute("ra_tbfu", ra_tbfu);
        model.addAttribute("ra_anc", ra_anc);
        model.addAttribute("ra_eid", ra_eid);
        model.addAttribute("ra_pmtct", ra_pmtct);
        model.addAttribute("ra_hord", ra_hord);
        model.addAttribute("ra_nc", ra_nc);
        model.addAttribute("ra_others", ra_others);


        //week appointment status
        //scheduled week appointments starting from current date excluding weekends
        //select count(*) from obs where concept_id=5096 and date(value_datetime)='2022-06-06'
        ArrayList<String> upfrontWeekDates= getUpfrontWeekDates();

        for (int i=0; i<upfrontWeekDates.size();i++) {

            q = new HqlQueryBuilder();
            q.select("count(*) as 'my_appointments'");
            q.from(Obs.class);
            q.where(String.format("concept_id=%s and date(value_datetime)='%s'", 5096, upfrontWeekDates.get(i)));

            List<List<Object>> my_appointments = administrationService.executeSQL(q.toString(), true);
            model.addAttribute(String.format("Day%s",i),String.format("%s%s%s",upfrontWeekDates.get(i),dateSpliter,
                    my_appointments.get(0).get(0)));
        }

    }

    private ArrayList<String> getUpfrontWeekDates(){

        String startDate="";
        ArrayList<String> weekDates=new ArrayList<>();
        LocalDateTime now;

        int loopTimes=7;
        for(int i=0;i<loopTimes;i++){

            now=LocalDateTime.now().plusDays(i);
            startDate = String.format("%s-%s-%s", now.getYear(), now.getMonthValue(), now.getDayOfMonth());

            if(now.getDayOfWeek()== DayOfWeek.SATURDAY || now.getDayOfWeek()== DayOfWeek.SUNDAY)
                loopTimes+=1;
            else
                weekDates.add(startDate);
        }

        return weekDates;
    }
    private String getTodayReportingPeriod() {

        LocalDateTime now = LocalDateTime.now();


        Month m = now.getMonth();
        String quarter = "",startDate="";
        startDate = String.format("%s-%s-%s", now.getYear(), now.getMonthValue(), now.getDayOfMonth());

        if (m.getValue() == 1 || m.getValue() == 2 || m.getValue() == 3) {
            quarter = String.format("Q1 %s (Jan-Mar)", now.getYear());
        } else if (m.getValue() == 4 || m.getValue() == 5 || m.getValue() == 6) {
            quarter = String.format("Q2 %s (Apr-Jun)", now.getYear());
        } else if (m.getValue() == 7 || m.getValue() == 8 || m.getValue() == 9) {
            quarter = String.format("Q3 %s (Jul-Sept)", now.getYear());
        } else {
            quarter = String.format("Q4 %s (Oct-Dec)", now.getYear());
        }

        return String.format("%s" + dateSpliter + "%s", quarter, startDate);
    }
}
