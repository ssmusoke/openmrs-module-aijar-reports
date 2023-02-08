package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.PatientDataHelper;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.ActivesInCareCohortDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.ReportingComprehensiveAuditToolDataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Handler(supports = {ReportingComprehensiveAuditToolDataSetDefinition.class})
public class ReportingComprehensiveAuditToolDataSetEvaluator implements DataSetEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {

        ReportingComprehensiveAuditToolDataSetDefinition definition = (ReportingComprehensiveAuditToolDataSetDefinition) dataSetDefinition;

        CohortDefinitionService cohortDefinitionService =Context.getService(CohortDefinitionService.class);
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);

        String endDate = DateUtil.formatDate(new Date(), "yyyy-MM-dd");

        context = ObjectUtil.nvl(context, new EvaluationContext());
        Date myendDate = new Date();
        Map<String, Object> parameterValues = new HashMap<String, Object>();
        Map<String, Object> parameterValuesForPreviousPeriod = new HashMap<String, Object>();
        Calendar newDate = toCalendar(myendDate);
        newDate.add(Calendar.MONTH, -3);
        newDate.add(Calendar.DATE, 1);

        Date startDate = newDate.getTime();
        String startDateString = DateUtil.formatDate(startDate, "yyyy-MM-dd");

        parameterValues.put("startDate",startDate);
        parameterValues.put("endDate", myendDate);
        EvaluationContext myContext = new EvaluationContext();
        myContext.setParameterValues(parameterValues);


        Calendar newDate1 = toCalendar(startDate);
        newDate1.add(Calendar.DATE, -1);
        Date endDateForPreviousPeriod =newDate1.getTime();

        newDate1.add(Calendar.MONTH, -3);
        newDate1.set(Calendar.DATE, 1);
        Date startDateForPreviousPeriod =newDate1.getTime();

        Calendar newDate2 = toCalendar(startDateForPreviousPeriod);
        newDate2.add(Calendar.MONTH, -3);
        Date otherPreviousStartDate =newDate2.getTime();

        newDate2 = toCalendar(endDateForPreviousPeriod);
        newDate2.add(Calendar.MONTH, -3);
        Date otherPreviousEndDate =newDate2.getTime();

        String lostInPreviousPeriodString = "select t.patient_id from (select patient_id, max(value_datetime) return_visit_date,datediff('"+DateUtil.formatDate(endDateForPreviousPeriod, "yyyy-MM-dd")+"',max(value_datetime)) ltfp_days from encounter e inner  join obs o on e.encounter_id = o.encounter_id inner join encounter_type t on  t.encounter_type_id =e.encounter_type where encounter_datetime <='" + DateUtil.formatDate(endDateForPreviousPeriod, "yyyy-MM-dd")+"'"+
                "and t.uuid = '8d5b2be0-c2cc-11de-8d13-0010c6dffd0f' and  o.concept_id=5096 and o.value_datetime >= '"+DateUtil.formatDate(startDateForPreviousPeriod, "yyyy-MM-dd")+"'  and e.voided=0 and o.voided=0 group by patient_id) as t  where ltfp_days >=28";

        String startARTInCurrentPeriodString = "SELECT person_id FROM obs  WHERE concept_id=99161 and voided=0 and value_datetime between '"+startDateString+"' AND '"+endDate+"' ";

        SqlQueryBuilder startART = new SqlQueryBuilder();
        startART.append(startARTInCurrentPeriodString);
        List<Integer> startARTInReportingPeriod =  evaluationService.evaluateToList(startART,Integer.class, context);

        SqlQueryBuilder lostinpreviousperiod = new SqlQueryBuilder();
        lostinpreviousperiod.append(lostInPreviousPeriodString);
        List<Integer> lostInPreviousPeriod =  evaluationService.evaluateToList(lostinpreviousperiod,Integer.class, context);


        ActivesInCareCohortDefinition activesInCareCohortDefinitionPreviousPeriod = new ActivesInCareCohortDefinition();
        activesInCareCohortDefinitionPreviousPeriod.setEndDate(endDateForPreviousPeriod);
        activesInCareCohortDefinitionPreviousPeriod.setStartDate(startDateForPreviousPeriod);
        activesInCareCohortDefinitionPreviousPeriod.setLostToFollowupDays("28");
        Cohort activeInPreviousPeriod = cohortDefinitionService.evaluate(activesInCareCohortDefinitionPreviousPeriod,context);

        ActivesInCareCohortDefinition activesInCareCohortDefinitionInOtherPreviousPeriod = new ActivesInCareCohortDefinition();
        activesInCareCohortDefinitionInOtherPreviousPeriod.setEndDate(otherPreviousEndDate);
        activesInCareCohortDefinitionInOtherPreviousPeriod.setStartDate(otherPreviousStartDate);
        activesInCareCohortDefinitionInOtherPreviousPeriod.setLostToFollowupDays("28");
        Cohort activeInOtherPreviousPeriod = cohortDefinitionService.evaluate(activesInCareCohortDefinitionInOtherPreviousPeriod,context);


        String dataQuery = "SELECT hiv.*,hiv1.*,ns.* from reporting_audit_tool_hiv hiv " +
                "left join reporting_audit_tool_hiv1 hiv1 on hiv.PATTIENT_NO = hiv1.PATTIENT_NO left join reporting_audit_tool_non_suppressed ns on hiv.PATTIENT_NO = ns.PATTIENT_NO";


        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(dataQuery);
        List<Object[]> results = evaluationService.evaluateToList(q, context);
        PatientDataHelper pdh = new PatientDataHelper();
        if(results.size()>0 && !results.isEmpty()) {
            for (Object[] o : results) {
                int patientno = (int)o[1];
                DataSetRow row = new DataSetRow();
                pdh.addCol(row, "ID", o[2]);
                pdh.addCol(row, "Gender", o[3]);
                pdh.addCol(row, "Date of Birth",  o[4]);
                pdh.addCol(row, "Age", o[5]);
                pdh.addCol(row, "Preg", o[6]);
                pdh.addCol(row, "Weight", o[7]);
                pdh.addCol(row, "DSDM", o[8]);
                pdh.addCol(row, "VisitType", o[9]);
                pdh.addCol(row, "Last Visit Date",  o[10]);
                pdh.addCol(row, "Next Appointment Date",  o[11]);
                pdh.addCol(row, "Prescription Duration", o[12]);
                pdh.addCol(row, "ART Start Date", o[13]);
                pdh.addCol(row, "Current Regimen", (String)o[15]);
                pdh.addCol(row, "Adherence", o[14]);
                pdh.addCol(row, "VL Quantitative", o[16]);


                pdh.addCol(row, "Last TPT Status", o[17]);
                pdh.addCol(row, "TB Status", o[18]);
                pdh.addCol(row, "HEPB", o[19]);
                pdh.addCol(row, "SYPHILLIS", o[20]);
                pdh.addCol(row, "FAMILY", o[21]);
                pdh.addCol(row, "ADV", o[22]);
                pdh.addCol(row, "VL Date", o[23]);
                pdh.addCol(row, "CHILD_AGE", o[24]);
                pdh.addCol(row, "CHILD_KNOWN", o[25]);
                pdh.addCol(row, "CHILD_HIV", o[26]);
                pdh.addCol(row, "CHILD_ART", o[27]);

                pdh.addCol(row, "PARTNER_AGE", o[28]);
                pdh.addCol(row, "PARTNER_KNOWN", o[29]);
                pdh.addCol(row, "PARTNER_HIV", o[30]);
                pdh.addCol(row, "PARTNER_ART", o[31]);
                pdh.addCol(row, "PSY_CODES", o[32]);
                pdh.addCol(row, "DEPRESSION", o[33]);
                pdh.addCol(row, "GBV", o[34]);
                pdh.addCol(row, "LINKAGE", o[35]);
                pdh.addCol(row, "OVC1", o[36]);
                pdh.addCol(row, "OVC2", o[37]);
                pdh.addCol(row, "NUTRITION_STATUS", o[38]);
                pdh.addCol(row, "NUTRITION_SUPPORT", o[39]);
                pdh.addCol(row, "CACX_STATUS", o[40]);

                String stable =(String)o[41];
                if(stable.equals("yes")){
                    pdh.addCol(row, "STABLE", "STABLE");
                }else if(stable.equals("no")){
                    pdh.addCol(row, "STABLE", "UNSTABLE");
                }else{
                    pdh.addCol(row, "STABLE", "");
                }

                //regimen lines
                pdh.addCol(row, "REGIMEN_LINE", o[42]);

                String ff = (String)o[43];
                if(!ff.equals("")){
                    pdh.addCol(row, "PP", "Priority population(PP)");
                }else{
                    pdh.addCol(row, "PP", "");
                }
                boolean dead =false;
                int dead_no = (int)o[44];
                if(dead_no==1)
                    dead= true;
                String TO = (String) o[45];
                String daysString = (String)o[46];
                String monthsString = (String)o[47];
                pdh.addCol(row, "DurationOnART", monthsString);
                int daysActive=-1;

                try {
                    if (daysString != "" || !daysString.equals("")) {
                        daysActive = Integer.parseInt(daysString);
                    }

                }catch (NumberFormatException e){
                    daysActive=999;
                    pdh.addCol(row, "FOLLOWUP", "");
                    e.printStackTrace();
                }
                if(daysActive <=0){
                    pdh.addCol(row, "CLIENT_STATUS", "Active(TX_CURR)");
                }else if(daysActive >=1 && daysActive<=7){
                    pdh.addCol(row, "CLIENT_STATUS", "Missed Appointment(TX_CURR)");
                }else if(daysActive >=8 && daysActive <= 28){
                    pdh.addCol(row, "CLIENT_STATUS", "Lost (Pre-IIT)");
                }else if(daysActive>28 && daysActive<=999) {
                    pdh.addCol(row, "CLIENT_STATUS", "LTFU(TX-ML)");
                }else{
                    pdh.addCol(row, "CLIENT_STATUS", "Lost to Followup");
                }

                pdh.addCol(row, "IAC", o[48]);
                pdh.addCol(row, "NEW_BLED_DATE", o[52]);
                pdh.addCol(row, "HEALTH_EDUC_DATE", o[53]);
                pdh.addCol(row, "ISSUES", o[54]);
                pdh.addCol(row, "APPROACHES", o[55]);
                pdh.addCol(row, "Current Regimen Date",  o[77]);

                pdh.addCol(row, "cd4", o[72]);
                pdh.addCol(row, "baseline_cd4", o[73]);
                pdh.addCol(row, "TB_LAM", o[74]);
                pdh.addCol(row, "TB_CRAG", o[75]);
                pdh.addCol(row, "WHO", o[76]);

                pdh.addCol(row, "VLREPEAT", o[80]);
                pdh.addCol(row, "HIVDR_SAMPLE_COLLECTED", o[81]);
                pdh.addCol(row, "VL_AFTER_IAC", o[82]);
                pdh.addCol(row, "VL_COPIES", o[83]);
                pdh.addCol(row, "RESULTS_RECEIVED", o[84]);
                pdh.addCol(row, "HIVDR_RESULTS", o[85]);
                pdh.addCol(row, "HIVDR_RESULTS_DATE", o[86]);
                pdh.addCol(row, "DECISION_DATE", o[87]);
                pdh.addCol(row, "DECISION_OUTCOME", o[88]);
                pdh.addCol(row, "NEW REGIMEN", o[89]);

                pdh.addCol(row, "ENROLLMENT_DATE", o[58]);
                pdh.addCol(row, "TEST_TYPE", o[59]);
                pdh.addCol(row, "CARE_ENTRY", o[60]);
                pdh.addCol(row, "TEMP", o[61]);
                pdh.addCol(row, "RR", o[62]);
                pdh.addCol(row, "HR", o[63]);
                pdh.addCol(row, "CLIENT_CATEGORY", o[64]);
                pdh.addCol(row, "MARITAL", o[65]);
                pdh.addCol(row, "REGISTRATION_DATE", o[66]);
                pdh.addCol(row, "SIGNS", o[67]);
                pdh.addCol(row, "SIDE_EFFECTS", o[68]);
                pdh.addCol(row, "PSS4", o[69]);
                pdh.addCol(row, "PSS7", o[70]);
                pdh.addCol(row, "PSS9", o[71]);


                if(activeInPreviousPeriod.contains(patientno)){
                    pdh.addCol(row, "previousStatus", "Active(TX_CURR)");
                }else if(lostInPreviousPeriod.contains(patientno)&& activeInOtherPreviousPeriod.contains(patientno)) {
                    pdh.addCol(row, "previousStatus", "Lost(TX-ML)");
                }else if ( startARTInReportingPeriod.contains(patientno)){
                    pdh.addCol(row, "previousStatus", " ");
                } else{
                    pdh.addCol(row, "previousStatus", "LTFU");
                }

                dataSet.addRow(row);
            }


        }
        return dataSet;
    }

    public static Calendar toCalendar(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }


}
