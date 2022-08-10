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
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.CQIHIVPMTCTToolDataSetDefinition;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Handler(supports = {CQIHIVPMTCTToolDataSetDefinition.class})
public class CQIHIVPMTCTToolDataSetEvaluator implements DataSetEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    Map<Integer,String> drugNames = new HashMap<>();
    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {

        CQIHIVPMTCTToolDataSetDefinition definition = (CQIHIVPMTCTToolDataSetDefinition) dataSetDefinition;

        CohortDefinitionService cohortDefinitionService = Context.getService(CohortDefinitionService.class);
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);

        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");

        context = ObjectUtil.nvl(context, new EvaluationContext());
        Date myendDate = definition.getEndDate();
        Map<String, Object> parameterValues = new HashMap<String, Object>();
        Map<String, Object> parameterValuesForPreviousPeriod = new HashMap<String, Object>();
        Calendar newDate = toCalendar(myendDate);
        newDate.add(Calendar.MONTH, -3);
        newDate.add(Calendar.DATE, 1);

        Date startDate = newDate.getTime();

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

        EvaluationContext myContextPreviousPeriod = new EvaluationContext();
        parameterValuesForPreviousPeriod.put("startDate",startDateForPreviousPeriod);
        parameterValuesForPreviousPeriod.put("endDate",endDateForPreviousPeriod);
        myContextPreviousPeriod.setParameterValues(parameterValuesForPreviousPeriod);

        Calendar newDate2 = toCalendar(startDateForPreviousPeriod);
        newDate2.add(Calendar.MONTH, -3);
        Date otherPreviousStartDate =newDate2.getTime();

        newDate2 = toCalendar(endDateForPreviousPeriod);
        newDate2.add(Calendar.MONTH, -3);
        Date otherPreviousEndDate =newDate2.getTime();

        EvaluationContext myContextOtherPreviousPeriod = new EvaluationContext();
        parameterValuesForPreviousPeriod.put("startDate",otherPreviousStartDate);
        parameterValuesForPreviousPeriod.put("endDate",otherPreviousEndDate);
        myContextOtherPreviousPeriod.setParameterValues(parameterValuesForPreviousPeriod);

        String eidQuery ="SELECT patient,\n" +
                "                IFNULL(EDD.edd_date,''),\n" +
                "                EIDNO.id as EIDNO,\n" +
                "                IFNULL(EIDDOB.dob,'') as EID_DOB,\n" +
                "                TIMESTAMPDIFF(MONTH , EIDDOB.dob, '%s') as EID_age,\n" +
                "                EID_W.value_numeric as EID_Weight,\n" +
                "                IFNULL(EID_NEXT_APPT.value_datetime,'')AS NEXT_APPOINTMENT_DATE,\n" +
                "                EID_FEEDING.name as Feeding,\n" +
                "                IFNULL(CTX.mydate,'') as CTX_START,\n" +
                "                TIMESTAMPDIFF(MONTH, CTX.mydate, '%s') as agectx,\n" +
                "                IFNULL(1stPCR.mydate,'') as 1stPCRDATE,\n" +
                "                TIMESTAMPDIFF(MONTH, 1stPCR.mydate, '%s') as age1stPCR,\n" +
                "                1stPCRResult.name,\n" +
                "                IFNULL(1stPCRReceived.mydate,'') as 1stPCRRecieved,\n" +
                "                IFNULL(2ndPCR.mydate,'') as 2ndPCRDATE,\n" +
                "                TIMESTAMPDIFF(MONTH, 2ndPCR.mydate, '%s') as age2ndPCR,\n" +
                "                2ndPCRResult.name,\n" +
                "                IFNULL(2ndPCRReceived.mydate,'') as 2ndPCRRecieved,\n" +
                "                IFNULL(repeatPCR.mydate,'') as repeatPCRDATE,\n" +
                "                TIMESTAMPDIFF(MONTH, repeatPCR.mydate, '%s') as age3rdPCR,\n" +
                "                repeatPCRResult.name,\n" +
                "                IFNULL(repeatPCRReceived.mydate,'') as repeatPCRRecieved,\n" +
                "                IFNULL(rapidTest.mydate,'') as rapidTestDate,\n" +
                "                TIMESTAMPDIFF(MONTH, rapidTest.mydate, '%s') as ageatRapidTest,\n" +
                "                rapidTestResult.name,\n" +
                "                finalOutcome.name,\n" +
                "                linkageNo.value_text,\n" +
                "                IFNULL(NVP.mydate,'') AS NVP_START_DATE,\n" +
                "                IF(TIMESTAMPDIFF(DAY , NVP.mydate, '%s')<=2,'Y','N') as NVP\n" +
                "\n" +
                "                FROM  (select DISTINCT o.person_id as patient from obs o  WHERE o.voided = 0 and concept_id=90041 and obs_datetime<= '%s' and obs_datetime= DATE_SUB('%s', INTERVAL 1 YEAR))cohort join\n" +
                "                    person p on p.person_id = cohort.patient\n" +
                "                    LEFT JOIN (SELECT person_id, max(DATE (value_datetime))as edd_date FROM obs WHERE concept_id=5596 and voided=0 and  obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_id)EDD on patient=EDD.person_id\n" +
                "                    LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')and concept_id=99771 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "                    on o.person_id = A.person_b where o.concept_id=99771 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) NVP on patient = NVP.parent\n" +
                "                    LEFT JOIN (SELECT person_a as parent,pi.identifier as id  from relationship left join patient_identifier pi on person_b = patient_id where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f') and pi.uuid=6) EIDNO on patient = EIDNO.parent\n" +
                "                    LEFT JOIN (SELECT person_a as parent,p.birthdate as dob  from relationship left join person p on person_b = p.person_id where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f') ) EIDDOB on patient = EIDDOB.parent\n" +
                "                    LEFT JOIN (SELECT parent,value_numeric  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "                    and concept_id=5089 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "                    on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=5089 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) EID_W on patient = EID_W.parent\n" +
                "                    LEFT JOIN (SELECT parent,value_datetime from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "                    and concept_id=5096 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A       on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=5096 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) EID_NEXT_APPT on patient = EID_NEXT_APPT.parent\n" +
                "                    LEFT JOIN (SELECT parent,cn.name from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "                    and concept_id=99451 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A       on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99451 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) EID_FEEDING on patient = EID_FEEDING.parent\n" +
                "                    LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "                    and concept_id=99773 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A       on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99773 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) CTX on patient = CTX.parent\n" +
                "                    LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "                    and concept_id=99606 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A       on o.person_id = A.person_b where o.concept_id=99606 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) 1stPCR on patient = 1stPCR.parent\n" +
                "                    LEFT JOIN (SELECT parent,cn.name  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "                    and concept_id=99435 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A       on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99435 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) 1stPCRResult on patient = 1stPCRResult.parent\n" +
                "                    LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "                    and concept_id=99438 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "                        on o.person_id = A.person_b where o.concept_id=99438 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) 1stPCRReceived on patient = 1stPCRReceived.parent\n" +
                "                    LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "                    and concept_id=99436 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "                        on o.person_id = A.person_b where o.concept_id=99436 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) 2ndPCR on patient = 2ndPCR.parent\n" +
                "                    LEFT JOIN (SELECT parent,cn.name  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "                    and concept_id=99440 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "                        on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99440 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) 2ndPCRResult on patient = 2ndPCRResult.parent\n" +
                "                    LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "                    and concept_id=99442 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "                        on o.person_id = A.person_b where o.concept_id=99442 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) 2ndPCRReceived on patient = 2ndPCRReceived.parent\n" +
                "                    LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "                    and concept_id=165405 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "                        on o.person_id = A.person_b where o.concept_id=165405 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) repeatPCR on patient = repeatPCR.parent\n" +
                "                    LEFT JOIN (SELECT parent,cn.name  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "                    and concept_id=165406 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "                        on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=165406 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) repeatPCRResult on patient = repeatPCRResult.parent\n" +
                "                    LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "                    and concept_id=165408 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "                        on o.person_id = A.person_b where o.concept_id=165408 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) repeatPCRReceived on patient = repeatPCRReceived.parent\n" +
                "                    LEFT JOIN (SELECT parent,DATE(value_datetime) mydate  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "                    and concept_id=162879 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "                        on o.person_id = A.person_b where o.concept_id=162879 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) rapidTest on patient = rapidTest.parent\n" +
                "                    LEFT JOIN (SELECT parent,cn.name  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "                    and concept_id=162880 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "                        on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=162880 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) rapidTestResult on patient = rapidTestResult.parent\n" +
                "                    LEFT JOIN (SELECT parent,cn.name  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "                    and concept_id=99797 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "                        on o.person_id = A.person_b LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' where o.concept_id=99797 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) finalOutcome on patient = finalOutcome.parent\n" +
                "                    LEFT JOIN (SELECT parent,value_text  from obs o inner join (SELECT person_a as parent,person_b,max(obs_datetime)latest_date from relationship inner join obs   on person_id = relationship.person_b  where relationship =(select relationship_type_id from relationship_type where uuid= '8d91a210-c2cc-11de-8d13-0010c6dffd0f')\n" +
                "                       and concept_id=99751 and obs.voided=0 and obs_datetime<='%s' AND obs_datetime >=DATE_SUB('%s', INTERVAL 1 YEAR) group by person_b)A\n" +
                "                        on o.person_id = A.person_b where o.concept_id=99751 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by parent) linkageNo on patient = linkageNo.parent\n";
        eidQuery =eidQuery.replaceAll("%s",endDate);
        SqlQueryBuilder eid = new SqlQueryBuilder();
        eid.append(eidQuery);
        List<Object[]> eidResults = evaluationService.evaluateToList(eid, context);

        Cohort lostInPreviousPeriod = cohortDefinitionService.evaluate(hivCohortDefinitionLibrary.getPatientsTxLostToFollowupByDays("28"),myContextPreviousPeriod);
        Cohort activeInPreviousPeriod = cohortDefinitionService.evaluate(hivCohortDefinitionLibrary.getActivePatientsWithLostToFollowUpAsByDays("28"),myContextPreviousPeriod);
        Cohort activeInOtherPreviousPeriod = cohortDefinitionService.evaluate(hivCohortDefinitionLibrary.getActivePatientsWithLostToFollowUpAsByDays("28"),myContextOtherPreviousPeriod);

        String arvStartDateQuery = "SELECT B.person_id, min(B.obs_datetime) as start_date from obs B inner join (SELECT o.person_id,o.value_coded from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=90315 \n" +
                "           and voided=0 group by person_id)A on o.person_id = A.person_id where o.concept_id=90315 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s')C on B.person_id=C.person_id where B.value_coded=C.value_coded and obs_datetime<='%s' and voided=0 group by B.person_id";

        arvStartDateQuery =arvStartDateQuery.replaceAll("%s",endDate);
        SqlQueryBuilder q1 = new SqlQueryBuilder();
        q1.append(arvStartDateQuery);
        Map<Integer,Object> arvStartDateMap = evaluationService.evaluateToMap(q1,Integer.class,Object.class, context);

        String dataQuery = "SELECT patient,gender,identifier,p.birthdate,TIMESTAMPDIFF(YEAR, p.birthdate, '%s') as age,\n" +
                "       Preg.name as pregnant_status,\n" +
                "       Wgt.value_numeric as Weight,\n" +
                "       IF(DSD.name='FTR','FTDR',DSD.name) as DSDM,\n" +
                "       vst_type.name as Visit_Type,\n" +
                "       last_enc.visit_date as Last_visit_date,\n" +
                "       returndate AS Next_Appointment_Date,\n" +
                "       MMD.value_numeric as NO_of_Days,\n" +
                "       ARTStartDate,\n" +
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
                "       vl_bled.vl_date,\n" +
                "       INDEX_TESTING_CHILD_AGE.no,\n" +
                "       RELATIONSHIP_CHILD_STATUS.no,\n" +
                "       RELATIONSHIP_CHILD_POSITIVE.no,\n" +
                "       RELATIONSHIP_CHILD_ONART.no,\n" +
                "       INDEX_TESTING_PARTNER.no,\n" +
                "       RELATIONSHIP_PARTNER_STATUS.no,\n" +
                "       RELATIONSHIP_PARTNER_POSITIVE.no,\n" +
                "       RELATIONSHIP_PARTNER_ONART.no,\n" +
                "       psy_codes.name as CODES,\n" +
                "       DEPRESSION.name as depression,\n" +
                "       GBV.name,\n" +
                "       LINKAGE.name,\n" +
                "       OVC_SCREENING.name,\n" +
                "       OVC_ENROL.name,\n" +
                "       NUTRITION_STATUS.name,\n" +
                "       NUTRITION_SUPPORT.name,\n" +
                "       CACX_STATUS.name,\n" +
                "       IFNULL(STABLE.name,'') AS stable,\n" +
                "       REGIMEN_LINES.concept_id,\n" +
                "       IFNULL(PP.name,'') as PP," +
                "       p.dead," +
                "       IFNULL(TOD.TOdate,'')," +
                "       IFNULL(TIMESTAMPDIFF(DAY,DATE(returndate),DATE('%s')),''), " +
                "       IFNULL(TIMESTAMPDIFF(MONTH ,DATE(ARTStartDate),DATE(last_enc.visit_date)),''),  " +
                "       IF(VL.value_numeric>=1000,IFNULL(IAC.SESSIONS,0),NULL), " +
                "       IF(VL.value_numeric>=1000,IFNULL(HIVDRTEST.HIVDR_date,''),''), " +
                "       HIVDR_TEST_COLECTED.name as SAMPLE_COLLECTED, " +
                "       IF(VL.value_numeric>=1000,IF(SWITCHED.value_coded in (163162,163164),'Y','N'),'')  " +
                "" +
                "FROM  (select DISTINCT o.person_id as patient from obs o  WHERE o.voided = 0 and concept_id=90041 and obs_datetime<= '%s' and obs_datetime= DATE_SUB('%s', INTERVAL 1 YEAR))cohort join\n" +
                "      person p on p.person_id = cohort.patient LEFT JOIN\n" +
                "    (SELECT pi.patient_id as patientid,identifier FROM patient_identifier pi INNER JOIN patient_identifier_type pit ON pi.identifier_type = pit.patient_identifier_type_id and pit.uuid='e1731641-30ab-102d-86b0-7a5022ba4115'  WHERE  pi.voided=0 group by pi.patient_id)ids on patient=patientid\n" +
                "    LEFT JOIN(SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=90041 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=90041 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id)Preg on patient=Preg.person_id\n" +
                "   LEFT JOIN(SELECT o.person_id,value_numeric from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=5089 and voided=0 group by person_id)A on o.person_id = A.person_id\n" +
                "where o.concept_id=5089 and obs_datetime =A.latest_date and o.voided=0  and obs_datetime <='%s' group by o.person_id)Wgt on patient=Wgt.person_id\n" +
                "   LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165143 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='SHORT' and cn.locale='en'\n" +
                "where o.concept_id=165143 and obs_datetime =A.latest_date and o.voided=0  and obs_datetime <='%s' group by o.person_id)DSD on patient =DSD.person_id\n" +
                "   LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=160288 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=160288 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id) vst_type on patient= vst_type.person_id\n" +
                "   LEFT JOIN (select e.patient_id,max(DATE(encounter_datetime))visit_date from encounter e INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id WHERE e.voided = 0 and et.uuid in ('8d5b2be0-c2cc-11de-8d13-0010c6dffd0f','8d5b27bc-c2cc-11de-8d13-0010c6dffd0f') and encounter_datetime<= '%s' group by patient_id)as last_enc on patient=last_enc.patient_id\n" +
                "   LEFT JOIN (SELECT person_id, max(DATE (value_datetime))as returndate FROM obs WHERE concept_id=5096 and voided=0 AND obs_datetime <='%s' group by person_id)return_date on patient=return_date.person_id\n" +
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
                "    LEFT JOIN (SELECT AGE.person_id,count(*) as no from (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id\n" +
                "where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=164352 and value_coded=90280\n" +
                ")RELATIONSHIP INNER JOIN (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id\n" +
                "where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=99074 and value_numeric <=19\n" +
                ")AGE on RELATIONSHIP.obs_group_id = AGE.obs_group_id group by AGE.person_id)INDEX_TESTING_CHILD_AGE on INDEX_TESTING_CHILD_AGE.person_id = patient\n" +
                "    LEFT JOIN (Select AGE.person_id,count(*) as no from (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id\n" +
                "where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=164352 and value_coded=90280\n" +
                ")RELATIONSHIP INNER JOIN (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id\n" +
                "where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id where concept_id=99074 and value_numeric <=19\n" +
                ")AGE  on RELATIONSHIP.obs_group_id = AGE.obs_group_id INNER JOIN (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id\n" +
                "where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=165275)C on C.obs_group_id= AGE.obs_group_id group by AGE.person_id\n" +
                ")RELATIONSHIP_CHILD_STATUS on RELATIONSHIP_CHILD_STATUS.person_id = patient\n" +
                "    LEFT JOIN (Select AGE.person_id,count(*) as no from (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id\n" +
                "where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=164352 and value_coded=90280\n" +
                ")RELATIONSHIP INNER JOIN (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id\n" +
                "where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id where concept_id=99074 and value_numeric <=19\n" +
                ")AGE  on RELATIONSHIP.obs_group_id = AGE.obs_group_id INNER JOIN (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id\n" +
                "where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=165275 AND value_coded=90166)C on C.obs_group_id= AGE.obs_group_id group by AGE.person_id\n" +
                ")RELATIONSHIP_CHILD_POSITIVE on RELATIONSHIP_CHILD_POSITIVE.person_id = patient\n" +
                "    LEFT JOIN (Select AGE.person_id,count(*) as no from (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id\n" +
                "where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=164352 and value_coded=90280\n" +
                ")RELATIONSHIP INNER JOIN (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id\n" +
                "where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id where concept_id=99074 and value_numeric <=19\n" +
                ")AGE  on RELATIONSHIP.obs_group_id = AGE.obs_group_id INNER JOIN (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id\n" +
                "where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=90270 AND value_coded=90003)C on C.obs_group_id= AGE.obs_group_id group by AGE.person_id\n" +
                ")RELATIONSHIP_CHILD_ONART on RELATIONSHIP_CHILD_ONART.person_id = patient\n" +
                "LEFT JOIN (Select RELATIONSHIP.person_id,count(*) as no from (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id\n" +
                "where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=164352 and value_coded in (90288,165274)\n" +
                ")RELATIONSHIP  group by RELATIONSHIP.person_id)INDEX_TESTING_PARTNER on INDEX_TESTING_PARTNER.person_id = patient\n" +
                "    LEFT JOIN (Select RELATIONSHIP.person_id,count(*) as no from (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id\n" +
                "where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=164352 and value_coded in (90288,165274)\n" +
                ")RELATIONSHIP INNER JOIN(SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id\n" +
                "where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=165275)C on C.obs_group_id= RELATIONSHIP.obs_group_id group by RELATIONSHIP.person_id\n" +
                ")RELATIONSHIP_PARTNER_STATUS on RELATIONSHIP_PARTNER_STATUS.person_id = patient\n" +
                "    LEFT JOIN (Select RELATIONSHIP.person_id,count(*) as no from (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id\n" +
                "where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=164352 and value_coded in (90288,165274)\n" +
                ")RELATIONSHIP INNER JOIN  (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id\n" +
                "where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=165275 AND value_coded=90166)C on C.obs_group_id= RELATIONSHIP.obs_group_id group by RELATIONSHIP.person_id\n" +
                ")RELATIONSHIP_PARTNER_POSITIVE on RELATIONSHIP_PARTNER_POSITIVE.person_id = patient\n" +
                "    LEFT JOIN (Select RELATIONSHIP.person_id,count(*) as no from (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id\n" +
                "where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=164352 and value_coded=90280\n" +
                ")RELATIONSHIP INNER JOIN (SELECT family.person_id,obs_group_id from obs family inner join  (SELECT o.person_id,obs_id from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99075 and voided=0 group by person_id)A on o.person_id = A.person_id\n" +
                "where concept_id=99075 AND obs_datetime =A.latest_date and o.voided=0)B on family.obs_group_id = B.obs_id  where concept_id=90270 AND value_coded=90003)C on C.obs_group_id= RELATIONSHIP.obs_group_id group by RELATIONSHIP.person_id\n" +
                ")RELATIONSHIP_PARTNER_ONART on RELATIONSHIP_PARTNER_ONART.person_id = patient\n" +
                "    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165185 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=165185 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id)psy_codes on patient= psy_codes.person_id\n" +
                "    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165194 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=165194 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id)DEPRESSION on patient= DEPRESSION.person_id\n" +
                "    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165302 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=165302 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id)GBV on patient= GBV.person_id\n" +
                "    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165193 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=165193 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id)LINKAGE on patient= LINKAGE.person_id\n" +
                "    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165200 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=165200 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id)OVC_SCREENING on patient= OVC_SCREENING.person_id\n" +
                "    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165212 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=165212 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id)OVC_ENROL on patient= OVC_ENROL.person_id\n" +
                "    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165050 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=165050 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id)NUTRITION_STATUS on patient= NUTRITION_STATUS.person_id\n" +
                "    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=99054 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=99054 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id)NUTRITION_SUPPORT on patient= NUTRITION_SUPPORT.person_id\n" +
                "    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165315 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=165315 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id)CACX_STATUS on patient= CACX_STATUS.person_id\n" +
                "    LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=165144 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=165144 and obs_datetime =A.latest_date and o.voided=0  and obs_datetime <='%s' group by o.person_id)STABLE on patient =STABLE.person_id\n" +
                "    LEFT JOIN (SELECT pp.patient_id, program_workflow_state.concept_id from patient_state inner join program_workflow_state on patient_state.state = program_workflow_state.program_workflow_state_id\n" +
                "        inner join program_workflow on program_workflow_state.program_workflow_id = program_workflow.program_workflow_id inner join program on program_workflow.program_id = program.program_id inner join patient_program pp\n" +
                "            on patient_state.patient_program_id = pp.patient_program_id and program_workflow.concept_id=166214 and patient_state.end_date is null)REGIMEN_LINES ON patient = REGIMEN_LINES.patient_id " +
                " LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id= 165169 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n" +
                "where o.concept_id=165169 and obs_datetime =A.latest_date and o.voided=0  and obs_datetime <='%s' group by o.person_id)PP on patient =PP.person_id " +
                "    LEFT JOIN (SELECT person_id, max(DATE (value_datetime))as TOdate FROM obs WHERE concept_id=99165 and voided=0 and  value_datetime<='%s' AND obs_datetime <='%s' group by person_id)TOD on patient=TOD.person_id" +
                "    LEFT JOIN (select obs.person_id,count(value_datetime) SESSIONS from obs inner join (SELECT person_id, max(DATE (value_datetime))as vldate FROM obs WHERE concept_id=163023 and voided=0 and  value_datetime<='%s' AND obs_datetime <='%s' group by person_id\n" +
                ")vl_date on vl_date.person_id= obs.person_id where concept_id=163154 and value_datetime>=vldate and obs_datetime between DATE_SUB('%s', INTERVAL 1 YEAR) and '%s' GROUP BY obs.person_id)IAC on patient =IAC.person_id " +
                "    LEFT JOIN (SELECT person_id, max(DATE (value_datetime))as HIVDR_date FROM obs WHERE concept_id=164989 and voided=0 AND obs_datetime <='%s' group by person_id) HIVDRTEST on patient = HIVDRTEST.person_id" +
                "     LEFT JOIN (SELECT o.person_id,cn.name from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=164989 and voided=0 group by person_id)A on o.person_id = A.person_id LEFT JOIN concept_name cn ON value_coded = cn.concept_id and cn.concept_name_type='SHORT' and cn.locale='en'\n" +
                "where o.concept_id=164989 and obs_datetime =A.latest_date and o.voided=0  and obs_datetime <='%s' group by o.person_id)HIVDR_TEST_COLECTED ON patient=HIVDR_TEST_COLECTED.person_id " +
                "    LEFT JOIN (SELECT o.person_id, value_coded from obs o inner join (SELECT person_id,max(obs_datetime)latest_date from obs where concept_id=163166 and voided=0 group by person_id)A on o.person_id = A.person_id\n" +
                " where o.concept_id=163166 and obs_datetime =A.latest_date and o.voided=0 and obs_datetime <='%s' group by o.person_id)SWITCHED on patient = SWITCHED.person_id ";
        dataQuery =dataQuery.replaceAll("%s",endDate);

        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(dataQuery);
        List<Object[]> results = evaluationService.evaluateToList(q, context);
        PatientDataHelper pdh = new PatientDataHelper();
        if(results.size()>0 && !results.isEmpty()) {
            for (Object[] o : results) {
                int patientno = (int) o[0];
                DataSetRow row = new DataSetRow();
                pdh.addCol(row, "ID", (String) o[2]);
                pdh.addCol(row, "Gender", (String) o[1]);
                pdh.addCol(row, "Date of Birth", o[3]);
                pdh.addCol(row, "Age", o[4]);
                pdh.addCol(row, "Preg", o[5]);
                pdh.addCol(row, "Weight", o[6]);
                pdh.addCol(row, "DSDM", (String) o[7]);
                pdh.addCol(row, "VisitType", (String) o[8]);
                pdh.addCol(row, "Last Visit Date", o[9]);
                pdh.addCol(row, "Next Appointment Date", o[10]);
                pdh.addCol(row, "Prescription Duration", o[11]);
                pdh.addCol(row, "ART Start Date", o[12]);
//                pdh.addCol(row, "CurrentRegimenLine", (String)o[]);
                pdh.addCol(row, "Current Regimen", (String) o[14]);
//                pdh.addCol(row, "Current Regimen Date",  o[15]);
                pdh.addCol(row, "Adherence", o[13]);
                pdh.addCol(row, "VL Quantitative", o[15]);
                pdh.addCol(row, "VL QTY", o[16]);

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

                String stable = (String) o[41];
                if (stable.equals("yes")) {
                    pdh.addCol(row, "STABLE", "STABLE");
                } else if (stable.toLowerCase().equals("no")) {
                    pdh.addCol(row, "STABLE", "UNSTABLE");
                } else {
                    pdh.addCol(row, "STABLE", "");
                }

                //regimen lines
                int lines_concept = (int) o[42];
                if (lines_concept == 90271) {
                    pdh.addCol(row, "REGIMEN_LINE", 1);
                } else if (lines_concept == 90305) {
                    pdh.addCol(row, "REGIMEN_LINE", 2);
                } else if (lines_concept == 162987) {
                    pdh.addCol(row, "REGIMEN_LINE", 3);
                }
                String ff = (String) o[43];
                if (!ff.equals("")) {
                    pdh.addCol(row, "PP", "Priority population(PP)");
                } else {
                    pdh.addCol(row, "PP", "");
                }
                boolean dead = false;
                dead = (boolean) o[44];
                String TO = (String) o[45];
                String daysString = (String) o[46];
                String monthsString = (String) o[47];
                int daysActive = -1;
                int monthsonART = -1;

                try {
                    if (daysString != "" || !daysString.equals("")) {
                        daysActive = Integer.parseInt(daysString);
                    }
                    if (monthsString != "" || !monthsString.equals("")) {
                        monthsonART = Integer.parseInt(monthsString);
                    }else{
                        pdh.addCol(row, "FOLLOWUP", "");
                    }

                }catch (NumberFormatException e){
                    daysActive=999;
                    pdh.addCol(row, "FOLLOWUP", "");
                    e.printStackTrace();
                }
                if(daysActive <=0){
                    pdh.addCol(row, "CLIENT_STATUS", "TX_CURR(Active)");
                    pdh.addCol(row, "FOLLOWUP", "");
                }else if(daysActive >=1 && daysActive<=7){
                    pdh.addCol(row, "CLIENT_STATUS", "TX_CURR(Missed Appointment)");
                    pdh.addCol(row, "FOLLOWUP", "");
                }else if(daysActive >=8 && daysActive < 28){
                    pdh.addCol(row, "CLIENT_STATUS", "TX_CURR(Pre_IIT)");
                    pdh.addCol(row, "FOLLOWUP", "");
                }else if(daysActive>=28 && daysActive<=999) {
                    pdh.addCol(row, "CLIENT_STATUS", "TX_ML");
                    if (dead) {
                        pdh.addCol(row, "FOLLOWUP", "Dead");
                    } else if (!TO.equals("")) {
                        pdh.addCol(row, "FOLLOWUP", "Transferred Out");
                    } else if (monthsonART < 3 && monthsonART >= 0) {
                        pdh.addCol(row, "FOLLOWUP", "IIT (On RX < 3 months");
                    } else if (monthsonART >= 3 && monthsonART <= 5) {
                        pdh.addCol(row, "FOLLOWUP", "IIT (On RX 3-5 months)");

                    } else if (monthsonART >= 6) {
                        pdh.addCol(row, "FOLLOWUP", "IIT (On RX 6+months)");
                    }
                }else{
                    pdh.addCol(row, "CLIENT_STATUS", "Lost to Followup");
                    pdh.addCol(row, "FOLLOWUP", "");
                }

                pdh.addCol(row, "IAC", o[48]);
                if ((String) o[50] == "yes") {
                    pdh.addCol(row, "HIVDRT", o[49]);
                } else {
                    pdh.addCol(row, "HIVDRT", "");
                }
                pdh.addCol(row, "SWITCHED", o[51]);
                fillInCurrentARVStartDate(patientno, arvStartDateMap, pdh, row);


                if (activeInPreviousPeriod.contains(patientno)) {
                    pdh.addCol(row, "previousStatus", "TX_CURR(Active)");
                } else if (lostInPreviousPeriod.contains(patientno) && activeInOtherPreviousPeriod.contains(patientno)) {
                    pdh.addCol(row, "previousStatus", "TX_ML(Lost)");
                } else {
                    pdh.addCol(row, "previousStatus", "Lost to Followup");
                }

                if (eidResults.size() > 0 && !eidResults.isEmpty()) {
                    for (Object[] e : eidResults) {
                        if(o[0].equals(e[0])) {
                            pdh.addCol(row, "EDD", e[1]);
                            pdh.addCol(row, "EID_NO", e[2]);
                            pdh.addCol(row, "EID_DOB", e[3]);
                            pdh.addCol(row, "EID_AGE", e[4]);
                            pdh.addCol(row, "EID_WEIGHT", e[5]);
                            pdh.addCol(row, "EID_NAPPT", e[6]);
                            pdh.addCol(row, "feeding", e[7]);
                            pdh.addCol(row, "CTX_DATE", e[8]);
                            pdh.addCol(row, "CTX_AGE", e[9]);
                            pdh.addCol(row, "1PCR_DATE", e[10]);
                            pdh.addCol(row, "1PCR_AGE", e[11]);
                            pdh.addCol(row, "1PCR_RESULT", e[12]);
                            pdh.addCol(row, "1PCR_GIVEN", e[13]);
                            pdh.addCol(row, "2PCR_DATE", e[14]);
                            pdh.addCol(row, "2PCR_AGE", e[15]);
                            pdh.addCol(row, "2PCR_RESULT", e[16]);
                            pdh.addCol(row, "2PCR_GIVEN", e[17]);

                            pdh.addCol(row, "3PCR_DATE", e[18]);
                            pdh.addCol(row, "3PCR_AGE", e[19]);
                            pdh.addCol(row, "3PCR_RESULT", e[20]);
                            pdh.addCol(row, "3PCR_GIVEN", e[21]);

                            pdh.addCol(row, "RAPID_DATE", e[22]);
                            pdh.addCol(row, "RAPID_AGE", e[23]);
                            pdh.addCol(row, "RAPID_RESULT", e[24]);
                            pdh.addCol(row, "OUTCOME", e[25]);
                            pdh.addCol(row, "LINKAGE_NO", e[26]);
                        }

                    }
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

    private void fillInCurrentARVStartDate(int patientno,Map<Integer,Object>map,PatientDataHelper pdh,DataSetRow row){
        if(!map.isEmpty()){
            if(map.containsKey(patientno)){
                pdh.addCol(row, "Current Regimen Date",  map.get(patientno));
            }else{
                pdh.addCol(row, "Current Regimen Date",  "");
            }

        }else{
            pdh.addCol(row, "Current Regimen Date",  "");
        }
    }
}
