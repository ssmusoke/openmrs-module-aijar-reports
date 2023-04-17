package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

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
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.ReportingPMTCTDataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Handler(supports = {ReportingPMTCTDataSetDefinition.class})
public class ReportingPMTCTDataSetEvaluator implements DataSetEvaluator {


    @Autowired
    EvaluationService evaluationService;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {

        ReportingPMTCTDataSetDefinition definition = (ReportingPMTCTDataSetDefinition) dataSetDefinition;

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


        String dataQuery = "SELECT hiv.*,hiv1.*,ns.*,eid.* from reporting_audit_tool_eid eid inner join reporting_audit_tool_hiv hiv on eid.PATTIENT_NO = hiv.PATTIENT_NO and hiv.Pregnant_Status is not null left join reporting_audit_tool_hiv1 hiv1 on eid.PATTIENT_NO = hiv1.PATTIENT_NO\n" +
                "    left join reporting_audit_tool_non_suppressed ns on eid.PATTIENT_NO = ns.PATTIENT_NO";

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
                pdh.addCol(row, "DSDM", o[8]);
                pdh.addCol(row, "Last Visit Date",  o[10]);
                pdh.addCol(row, "Next Appointment Date",  o[11]);
                pdh.addCol(row, "Prescription Duration", o[12]);
                pdh.addCol(row, "ART Start Date", o[13]);
                pdh.addCol(row, "Current Regimen", (String)o[15]);
                pdh.addCol(row, "Adherence", o[14]);
                pdh.addCol(row, "VL Quantitative", o[16]);

                pdh.addCol(row, "Last TPT Status", o[17]);
                pdh.addCol(row, "TB Status", o[18]);
                pdh.addCol(row, "VL Date", o[23]);
                pdh.addCol(row, "CHILD_AGE", o[24]);
                pdh.addCol(row, "CHILD_KNOWN", o[25]);

                pdh.addCol(row, "PARTNER_AGE", o[28]);
                pdh.addCol(row, "PARTNER_KNOWN", o[29]);
                pdh.addCol(row, "PSY_CODES", o[32]);

                pdh.addCol(row, "LINKAGE", o[35]);
                pdh.addCol(row, "OVC1", o[36]);
                pdh.addCol(row, "OVC2", o[37]);
                pdh.addCol(row, "CACX_STATUS", o[40]);

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
                pdh.addCol(row, "Current Regimen Date",  o[77]);
                pdh.addCol(row, "HEALTH_EDUC_DATE", o[53]);
                pdh.addCol(row, "NEW_BLED_DATE", o[52]);
                pdh.addCol(row, "ISSUES", o[54]);
                pdh.addCol(row, "APPROACHES", o[55]);
                pdh.addCol(row, "PSS4", o[69]);
                pdh.addCol(row, "VL_AFTER_IAC", o[82]);
                pdh.addCol(row, "VL_COPIES", o[83]);

                pdh.addCol(row, "HIVDR_SAMPLE_COLLECTED", o[81]);
                pdh.addCol(row, "RESULTS_RECEIVED", o[84]);
                pdh.addCol(row, "HIVDR_RESULTS", o[85]);
                pdh.addCol(row, "HIVDR_RESULTS_DATE", o[86]);
                pdh.addCol(row, "DECISION_OUTCOME", o[88]);
                pdh.addCol(row, "NEW REGIMEN", o[89]);
                pdh.addCol(row, "HEPB", o[19]);
                pdh.addCol(row, "SYPHILLIS", o[20]);

                pdh.addCol(row, "EDD", o[92]);
                pdh.addCol(row, "EID_NO", o[93]);
                pdh.addCol(row, "EID_DOB", o[94]);
                pdh.addCol(row, "EID_AGE", o[95]);
                pdh.addCol(row, "EID_WEIGHT", o[96]);
                pdh.addCol(row, "EID_NAPPT", o[97]);
                pdh.addCol(row, "feeding", o[98]);
                pdh.addCol(row, "CTX_DATE", o[99]);
                pdh.addCol(row, "CTX_AGE", o[100]);
                pdh.addCol(row, "1PCR_DATE", o[101]);
                pdh.addCol(row, "1PCR_AGE", o[102]);
                pdh.addCol(row, "1PCR_RESULT", o[103]);
                pdh.addCol(row, "1PCR_GIVEN", o[104]);
                pdh.addCol(row, "2PCR_DATE", o[105]);
                pdh.addCol(row, "2PCR_AGE", o[106]);
                pdh.addCol(row, "2PCR_RESULT", o[107]);
                pdh.addCol(row, "2PCR_GIVEN", o[108]);

                pdh.addCol(row, "3PCR_DATE", o[109]);
                pdh.addCol(row, "3PCR_AGE", o[110]);
                pdh.addCol(row, "3PCR_RESULT", o[111]);
                pdh.addCol(row, "3PCR_GIVEN", o[112]);

                pdh.addCol(row, "RAPID_DATE", o[113]);
                pdh.addCol(row, "RAPID_AGE", o[114]);
                pdh.addCol(row, "RAPID_RESULT", o[115]);
                pdh.addCol(row, "OUTCOME", o[116]);
                pdh.addCol(row, "LINKAGE_NO", o[117]);
                pdh.addCol(row, "NVP", o[118]);
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

