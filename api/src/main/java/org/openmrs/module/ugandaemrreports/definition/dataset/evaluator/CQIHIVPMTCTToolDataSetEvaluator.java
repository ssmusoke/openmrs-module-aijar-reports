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
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.CQIHIVPMTCTToolDataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Handler(supports = {CQIHIVPMTCTToolDataSetDefinition.class})
public class CQIHIVPMTCTToolDataSetEvaluator implements DataSetEvaluator {

    @Autowired
    EvaluationService evaluationService;


    Map<Integer,String> drugNames = new HashMap<>();
    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {

        CQIHIVPMTCTToolDataSetDefinition definition = (CQIHIVPMTCTToolDataSetDefinition) dataSetDefinition;

        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);

        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");

        context = ObjectUtil.nvl(context, new EvaluationContext());

        String dataQuery = "SELECT patient,gender,identifier,p.birthdate,TIMESTAMPDIFF(YEAR, p.birthdate, '%s') as age,\n" +
                "       Preg.name as pregnant_status,\n" +
                "       Wgt.value_numeric as Weight,\n" +
                "       DSD.name as DSDM,\n" +
                "       vst_type.name as Visit_Type,\n" +
                "       IFNULL(last_enc.visit_date,'') as Last_visit_date,\n" +
                "       IFNULL(returndate,'') AS Next_Appointment_Date,\n" +
                "       MMD.value_numeric as NO_of_Days,\n" +
                "       IFNULL(ARTStartDate,''),\n" +
                "       adherence.name as Adherence,\n" +
                "       current_regimen.name as Current_Regimen,\n" +
                "       VL.value_numeric as VL_copies,\n" +
                "       VL_Q.name,\n" +
                "       TPT.name as TPT_Status,\n" +
                "       TB.name as TB_Status,\n" +
                "       HEPB.name as HEP_B_Status,\n" +
                "       SYPHILLIS.name as Sphillis_Status,\n" +
                "       family.name as Family_Planning,\n" +
                "       ADV_DZZ.name as Advanced_Disease,\n" +
                "       IFNULL(vl_bled.vl_date,''),\n" +
                "       IFNULL(EDD.edd_date,''),\n" +
                "       EIDNO.id as EIDNO,\n" +
                "       IFNULL(EIDDOB.dob,'') as EID_DOB,\n" +
                "       TIMESTAMPDIFF(MONTH , EIDDOB.dob, '%s') as EID_age,\n" +
                "       EID_W.value_numeric as EID_Weight,\n" +
                "       IFNULL(EID_NEXT_APPT.value_datetime,'')AS NEXT_APPOINTMENT_DATE,\n" +
                "       EID_FEEDING.name as Feeding,\n" +
                "       IFNULL(CTX.mydate,'') as CTX_START,\n" +
                "       TIMESTAMPDIFF(MONTH, CTX.mydate, '%s') as agectx,\n" +
                "       IFNULL(1stPCR.mydate,'') as 1stPCRDATE,\n" +
                "       TIMESTAMPDIFF(MONTH, 1stPCR.mydate, '%s') as age1stPCR,\n" +
                "       1stPCRResult.name,\n" +
                "       IFNULL(1stPCRReceived.mydate,'') as 1stPCRRecieved,\n" +
                "       IFNULL(2ndPCR.mydate,'') as 2ndPCRDATE,\n" +
                "       TIMESTAMPDIFF(MONTH, 2ndPCR.mydate, '%s') as age2ndPCR,\n" +
                "       2ndPCRResult.name,\n" +
                "       IFNULL(2ndPCRReceived.mydate,'') as 2ndPCRRecieved,\n" +
                "       IFNULL(repeatPCR.mydate,'') as repeatPCRDATE,\n" +
                "       TIMESTAMPDIFF(MONTH, repeatPCR.mydate, '%s') as age3rdPCR,\n" +
                "       repeatPCRResult.name,\n" +
                "       IFNULL(repeatPCRReceived.mydate,'') as repeatPCRRecieved,\n" +
                "       IFNULL(rapidTest.mydate,'') as rapidTestDate,\n" +
                "       TIMESTAMPDIFF(MONTH, rapidTest.mydate, '%s') as ageatRapidTest,\n" +
                "       rapidTestResult.name,\n" +
                "       finalOutcome.name,\n" +
                "       linkageNo.value_text,\n" +
                "       IFNULL(NVP.mydate,'') AS NVP_START_DATE,\n" +
                "       IF(TIMESTAMPDIFF(DAY , NVP.mydate, '%s')<=2,'Y','N') as NVP\n" +
                "\n" +
                "FROM  (select DISTINCT o.person_id as patient from obs o  WHERE o.voided = 0 and concept_id=90041 and obs_datetime<= '%s' and obs_datetime= DATE_SUB('%s', INTERVAL 1 YEAR))cohort join\n" +
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
                "where o.concept_id=99036 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id)MMD on patient=MMD.person_id\n" +
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
                "    LEFT JOIN (SELECT person_id, max(DATE (value_datetime))as vl_date FROM obs WHERE concept_id=163023 and voided=0 and  value_datetime<='%s' AND obs_datetime <='%s' group by person_id)vl_bled on patient=vl_bled.person_id\n" +
                "    LEFT JOIN (SELECT person_id, max(DATE (value_datetime))as edd_date FROM obs WHERE concept_id=5596 and voided=0 and  obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_id)EDD on patient=EDD.person_id\n" +
                "    LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "       and concept_id=99771 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "    on o.person_id = A.person_b where o.concept_id=99771 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) NVP on patient = NVP.parent\n" +
                "    LEFT JOIN (SELECT person_a as parent,pi.identifier as id  from relationship left join patient_identifier pi on person_b = patient_id where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f') and pi.uuid=6) EIDNO on patient = EIDNO.parent\n" +
                "    LEFT JOIN (SELECT person_a as parent,p.birthdate as dob  from relationship left join person p on person_b = p.person_id where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f') ) EIDDOB on patient = EIDDOB.parent\n" +
                "    LEFT JOIN (SELECT parent,value_numeric  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "               and concept_id=5089 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "              on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=5089 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) EID_W on patient = EID_W.parent\n" +
                "    LEFT JOIN (SELECT parent,value_datetime from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "               and concept_id=5096 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "              on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=5096 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) EID_NEXT_APPT on patient = EID_NEXT_APPT.parent\n" +
                "    LEFT JOIN (SELECT parent,cn.name from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "               and concept_id=99451 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "              on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99451 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) EID_FEEDING on patient = EID_FEEDING.parent\n" +
                "    LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "               and concept_id=99773 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "              on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99773 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) CTX on patient = CTX.parent\n" +
                "    LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "               and concept_id=99606 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "              on o.person_id = A.person_b where o.concept_id=99606 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) 1stPCR on patient = 1stPCR.parent\n" +
                "    LEFT JOIN (SELECT parent,cn.name  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "               and concept_id=99435 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "              on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99435 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) 1stPCRResult on patient = 1stPCRResult.parent\n" +
                "    LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "               and concept_id=99438 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "              on o.person_id = A.person_b where o.concept_id=99438 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) 1stPCRReceived on patient = 1stPCRReceived.parent\n" +
                "    LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "               and concept_id=99436 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "              on o.person_id = A.person_b where o.concept_id=99436 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) 2ndPCR on patient = 2ndPCR.parent\n" +
                "    LEFT JOIN (SELECT parent,cn.name  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "               and concept_id=99440 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "              on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99440 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) 2ndPCRResult on patient = 2ndPCRResult.parent\n" +
                "    LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "               and concept_id=99442 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "              on o.person_id = A.person_b where o.concept_id=99442 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) 2ndPCRReceived on patient = 2ndPCRReceived.parent\n" +
                "    LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "               and concept_id=165405 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "              on o.person_id = A.person_b where o.concept_id=165405 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) repeatPCR on patient = repeatPCR.parent\n" +
                "    LEFT JOIN (SELECT parent,cn.name  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "               and concept_id=165406 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "              on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=165406 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) repeatPCRResult on patient = repeatPCRResult.parent\n" +
                "    LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "               and concept_id=165408 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "              on o.person_id = A.person_b where o.concept_id=165408 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) repeatPCRReceived on patient = repeatPCRReceived.parent\n" +
                "    LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "               and concept_id=162879 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "              on o.person_id = A.person_b where o.concept_id=162879 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) rapidTest on patient = rapidTest.parent\n" +
                "    LEFT JOIN (SELECT parent,cn.name  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "               and concept_id=162880 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "              on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=162880 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) rapidTestResult on patient = rapidTestResult.parent\n" +
                "    LEFT JOIN (SELECT parent,cn.name  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "               and concept_id=99797 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "              on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99797 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) finalOutcome on patient = finalOutcome.parent\n" +
                "    LEFT JOIN (SELECT parent,value_text  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "               and concept_id=99751 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "              on o.person_id = A.person_b where o.concept_id=99751 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) linkageNo on patient = linkageNo.parent";
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
                pdh.addCol(row, "Date of Birth",  formatDate(o[3]));
                pdh.addCol(row, "Age", o[4]);
                pdh.addCol(row, "Weight", o[6]);
                pdh.addCol(row, "DSDM", (String)o[7]);
                pdh.addCol(row, "VisitType", (String)o[8]);
                pdh.addCol(row, "Last Visit Date",  formatDate(o[9]));
                pdh.addCol(row, "Next Appointment Date",  formatDate(o[10]));
                pdh.addCol(row, "Prescription Duration", o[11]);
//                pdh.addCol(row, "Client Status", (String)o[]);
                pdh.addCol(row, "ART Start Date", String.valueOf(o[12]));
//                pdh.addCol(row, "CurrentRegimenLine", (String)o[]);
                pdh.addCol(row, "Current Regimen", (String)o[14]);
//                pdh.addCol(row, "Current Regimen Date",  o[15]);
                pdh.addCol(row, "Adherence", o[13]);
                pdh.addCol(row, "VL Quantitative", o[15]);
                pdh.addCol(row, "VL QTY", o[16]);
//                pdh.addCol(row, "VL Date", o[17]);
                pdh.addCol(row, "Last TPT Status", o[17]);
                pdh.addCol(row, "TB Status", o[18]);
                pdh.addCol(row, "HEPB", o[19]);
                pdh.addCol(row, "SYPHILLIS", o[20]);
                pdh.addCol(row, "FAMILY", o[21]);
                pdh.addCol(row, "ADV", o[22]);
                pdh.addCol(row, "VL Date", o[23]);

//                START OF EID DATA
                pdh.addCol(row, "EDD", o[24]);
                pdh.addCol(row, "EID_NO", o[25]);
                pdh.addCol(row, "EID_DOB", String.valueOf(o[26]));
                pdh.addCol(row, "EID_AGE", o[27]);
                pdh.addCol(row, "EID_WEIGHT", o[28]);
                pdh.addCol(row, "EID_NAPPT", o[29]);
                pdh.addCol(row, "feeding", o[30]);
                pdh.addCol(row, "CTX_DATE", String.valueOf(o[31]));
                pdh.addCol(row, "CTX_AGE", o[32]);
                pdh.addCol(row, "1PCR_DATE", String.valueOf(o[33]));
                pdh.addCol(row, "1PCR_AGE", o[34]);
                pdh.addCol(row, "1PCR_RESULT", o[35]);
                pdh.addCol(row, "1PCR_GIVEN", String.valueOf(o[36]));
                pdh.addCol(row, "2PCR_DATE", String.valueOf(o[37]));
                pdh.addCol(row, "2PCR_AGE", o[38]);
                pdh.addCol(row, "2PCR_RESULT", o[39]);
                pdh.addCol(row, "2PCR_GIVEN", String.valueOf(o[40]));

                pdh.addCol(row, "3PCR_DATE", String.valueOf(o[41]));
                pdh.addCol(row, "3PCR_AGE", o[42]);
                pdh.addCol(row, "3PCR_RESULT", o[43]);
                pdh.addCol(row, "3PCR_GIVEN", String.valueOf(o[44]));

                pdh.addCol(row, "RAPID_DATE", String.valueOf(o[45]));
                pdh.addCol(row, "RAPID_AGE", o[46]);
                pdh.addCol(row, "RAPID_RESULT", o[47]);
                pdh.addCol(row, "OUTCOME", o[48]);
                pdh.addCol(row, "LINKAGE_NO", o[49]);
//                pdh.addCol(row, "LINKAGE_NO", o[50]);
//                pdh.addCol(row, "", (String)o[]);
                dataSet.addRow(row);
            }


        }
        return dataSet;
    }

    private String formatDate(Object rowField){
        String date="";
        if(!String.valueOf(rowField).equals("")){
            date = DateUtil.formatDate(DateUtil.parseYmd(String.valueOf(rowField)), "yyyy-MM-dd");
        }
        return date;

    }
}
