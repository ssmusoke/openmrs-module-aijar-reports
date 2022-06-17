package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import org.openmrs.annotation.Handler;
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
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.CQIHIVAdultToolDataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Handler(supports = {CQIHIVAdultToolDataSetDefinition.class})
public class CQIHIVAdultToolDataSetEvaluator implements DataSetEvaluator {

    @Autowired
    EvaluationService evaluationService;


    Map<Integer,String> drugNames = new HashMap<>();
    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {

        CQIHIVAdultToolDataSetDefinition definition = (CQIHIVAdultToolDataSetDefinition) dataSetDefinition;

        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);

        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");

        context = ObjectUtil.nvl(context, new EvaluationContext());

        String dataQuery = "SELECT patient,gender,identifier,p.birthdate,TIMESTAMPDIFF(YEAR, p.birthdate, '%s') as age,\n" +
                "       Preg.name as pregnant_status,\n" +
                "       Wgt.value_numeric as Weight,\n" +
                "       DSD.name as DSDM,\n" +
                "       vst_type.name as Visit_Type,\n" +
                "       last_enc.visit_date as Last_visit_date,\n" +
                "       returndate AS Next_Appointment_Date,\n" +
                "       MMD.value_numeric as NO_of_Days,\n" +
                "       ARTStartDate,\n" +
                "       adherence.name as Adherence,\n" +
                "       current_regimen.name as Current_Regimen,\n" +
                "       REGIMEN_START_DATE.start_date as ARV_Start_Date,\n" +
                "       VL.value_numeric as VL_copies,\n" +
                "       VL_Q.name,\n" +
                "       TPT.name as TPT_Status,\n" +
                "       TB.name as TB_Status,\n" +
                "       HEPB.name as HEP_B_Status,\n" +
                "       SYPHILLIS.name as Sphillis_Status,\n" +
                "       family.name as Family_Planning,\n" +
                "       ADV_DZZ.name as Advanced_Disease\n" +
                "\n" +
                "FROM  (select e.patient_id as patient from encounter e INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id WHERE e.voided = 0 and et.uuid in('8d5b27bc-c2cc-11de-8d13-0010c6dffd0f','8d5b2be0-c2cc-11de-8d13-0010c6dffd0f') and encounter_datetime<= '%s' and encounter_datetime>= DATE_SUB('%s', INTERVAL 1 YEAR) group by patient)cohort join\n" +
                "    person p on p.person_id = cohort.patient LEFT JOIN\n" +
                "    (SELECT pi.patient_id as patientid,identifier FROM patient_identifier pi INNER JOIN patient_identifier_type pit ON pi.identifier_type = pit.patient_identifier_type_id and pit.uuid='e1731641-30ab-102d-86b0-7a5022ba4115'  WHERE  pi.voided=0 group by pi.patient_id)ids on patient=patientid\n" +
                "    LEFT JOIN(SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=90041 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=90041 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id)Preg on patient=Preg.person_id\n" +
                "   LEFT JOIN(SELECT o.person_id,value_numeric from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=5089 and voided=0 group by person_id)A on o.person_id = A.person_id\n" +
                "where o.concept_id=5089 and obs_datetime =A.latest_date and o.voided=0  and obs_datetime <='%s' group by o.person_id)Wgt on patient=Wgt.person_id\n" +
                "   LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165143 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='SHORT' and cn.locale='en'\n" +
                "where o.concept_id=165143 and obs_datetime =A.latest_date and o.voided=0  and obs_datetime <='%s' group by o.person_id)DSD on patient =DSD.person_id\n" +
                "   LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=160288 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=160288 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id) vst_type on patient= vst_type.person_id\n" +
                "   LEFT JOIN (select e.patient_id,max(DATE(encounter_datetime))visit_date from encounter e INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id WHERE e.voided = 0 and et.uuid ='8d5b2be0-c2cc-11de-8d13-0010c6dffd0f' and encounter_datetime<= '%s' group by patient_id)as last_enc on patient=last_enc.patient_id\n" +
                "   LEFT JOIN (SELECT person_id, max(DATE (value_datetime))as returndate FROM obs WHERE concept_id=5096 and voided=0 and  value_datetime<='%s' AND obs_datetime <='%s' group by person_id)return_date on patient=return_date.person_id\n" +
                "   LEFT JOIN (SELECT o.person_id,value_numeric from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99036 and voided=0 group by person_id)A on o.person_id = A.person_id\n" +
                "where o.concept_id=99036 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id)MMD on patient=DSD.person_id\n" +
                "   LEFT JOIN (SELECT person_id, max(DATE (value_datetime))as ARTStartDate FROM obs WHERE concept_id=99161 and voided=0 and  value_datetime<='%s' AND obs_datetime <='%s' group by person_id)ARTStart on patient=ARTStart.person_id\n" +
                "   LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=90315 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=90315 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id) current_regimen on patient= current_regimen.person_id\n" +
                "   LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=90221 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=90221 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id) adherence on patient= adherence.person_id\n" +
                "   LEFT JOIN (SELECT o.person_id,value_numeric from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=856 and voided=0 group by person_id)A on o.person_id = A.person_id\n" +
                "where o.concept_id=856 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id)VL on patient=VL.person_id\n" +
                "    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=1305 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=1305 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id)VL_Q on patient= VL_Q.person_id\n" +
                "    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165288 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=165288 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id)TPT on patient= TPT.person_id\n" +
                "    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=90216 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=90216 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id)TB on patient= TB.person_id\n" +
                "    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime )latest_date from obs where concept_id=1322 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=1322 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id)HEPB on patient= HEPB.person_id\n" +
                "    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99753 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=99753 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id)SYPHILLIS on patient= SYPHILLIS.person_id\n" +
                "    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=90238 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=90238 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id)family on patient= family.person_id\n" +
                "    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165272 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=165272 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id)ADV_DZZ on patient= ADV_DZZ.person_id\n" +
                "    LEFT JOIN (SELECT B.person_id, min(B.obs_datetime) as start_date from obs B inner join (SELECT o.person_id,o.value_coded from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=90315\n" +
                "   and voided=0 group by person_id)A on o.person_id = A.person_id where o.concept_id=90315 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s')C on B.person_id=C.person_id where B.value_coded=C.value_coded and obs_datetime<='%s' and voided=0 group by B.person_id)REGIMEN_START_DATE ON patient=p.person_id\n"+
                " limit 3000";

        dataQuery =dataQuery.replaceAll("%s",endDate);

        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(dataQuery);
        List<Object[]> results = evaluationService.evaluateToList(q, context);

        PatientDataHelper pdh = new PatientDataHelper();
        if(results.size()>0 && !results.isEmpty()) {
            for (Object[] o : results) {
                DataSetRow row = new DataSetRow();
                pdh.addCol(row, "ID", (String)o[2]);
                pdh.addCol(row, "Gender", (String)o[1]);
                pdh.addCol(row, "Date of Birth",  DateUtil.formatDate(DateUtil.parseYmd(String.valueOf(o[3])), "yyyy-MM-dd"));
                pdh.addCol(row, "Age", o[4]);
                pdh.addCol(row, "Weight", o[6]);
                pdh.addCol(row, "DSDM", (String)o[7]);
                pdh.addCol(row, "VisitType", (String)o[8]);
                pdh.addCol(row, "Last Visit Date",  DateUtil.formatDate(DateUtil.parseYmd(String.valueOf(o[9])), "yyyy-MM-dd"));
                pdh.addCol(row, "Next Appointment Date",  DateUtil.formatDate(DateUtil.parseYmd(String.valueOf(o[10])), "yyyy-MM-dd"));
                pdh.addCol(row, "Prescription Duration", o[11]);
//                pdh.addCol(row, "Client Status", (String)o[]);
                pdh.addCol(row, "ART Start Date", String.valueOf(o[12]));
//                pdh.addCol(row, "CurrentRegimenLine", (String)o[]);
                pdh.addCol(row, "Current Regimen", (String)o[14]);
                pdh.addCol(row, "Current Regimen Date",  o[15]);
                pdh.addCol(row, "Adherence", o[13]);
                pdh.addCol(row, "VL Quantitative", o[16]);
                pdh.addCol(row, "VL QTY", o[17]);
//                pdh.addCol(row, "VL Date", o[17]);
                pdh.addCol(row, "Last TPT Status", o[18]);
                pdh.addCol(row, "TB Status", o[19]);
                pdh.addCol(row, "HEPB", o[20]);
                pdh.addCol(row, "SYPHILLIS", o[21]);
                pdh.addCol(row, "FAMILY", o[22]);
                pdh.addCol(row, "ADV", o[23]);
                pdh.addCol(row, "VL Date", o[24]);
//                pdh.addCol(row, "", (String)o[]);
                dataSet.addRow(row);
            }


        }
        return dataSet;
    }

}
