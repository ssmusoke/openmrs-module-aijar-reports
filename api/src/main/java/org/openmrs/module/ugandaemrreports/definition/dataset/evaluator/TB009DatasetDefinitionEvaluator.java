package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import org.joda.time.LocalDate;
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
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.TB009DatasetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Handler(supports = {TB009DatasetDefinition.class })
public class TB009DatasetDefinitionEvaluator implements DataSetEvaluator {

	@Autowired
	private EvaluationService evaluationService;
	
	@Override
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
		SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
		TB009DatasetDefinition definition = (TB009DatasetDefinition) dataSetDefinition;
				
		String startDate = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
		String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");


		
		context = ObjectUtil.nvl(context, new EvaluationContext());	

		String summarySql = "SELECT A.patient_id patient_id, A.encounter_datetime\tAS doa,\n" +
				"\t\t\t DISTRICT.value_text As District_No,\n" +
				"\t\t\t UNIT.value_text As Unit_No,\n" +
				"\t\t\t NIN.identifier\tAS NIN_no,\n" +
				"\t\t\t PN.family_name,\n" +
				"\t\t\t PN.given_name,\n" +
				"\t\t\t NEXT_OF_KIN.value_text AS NEXT_OF_KIN,\n" +
				"\t\t\t NEXT_OF_KIN_CONTACT.value_text AS NEXT_OF_KIN_CONTACT,\n" +
				"\t\t\t P.gender AS SEX,\n" +
				"\t\t\t YEAR(A.encounter_datetime) - YEAR(P.birthdate) - (RIGHT(A.encounter_datetime, 5) < RIGHT(P.birthdate, 5)) \tAS age,\n" +
				"             CATEGORY.value as Client_Category,\n" +
				"\t\t\t IFNULL(PA.address5,'')\tAS village,\n" +
				"\t\t\t IFNULL(PA.address4,'')\tAS Parish,\n" +
				"\t\t\t IFNULL(PA.address3,'')\tAS SubCounty,\n" +
				"\t\t\t IFNULL(PA.county_district,'')\tAS District,\n" +
				"             DISEASE_CLASS.name AS disease_classification,\n" +
				"             1stLineSatrtDate.value_datetime AS DATE_STARTED_1ST_LINE,\n" +
				"             1ST_REGIMEN.name AS DRUG_1ST_LINE,\n" +
				"             PATIENT_TYPE.name AS patient_type,\n" +
				"             RISK_GROUP.name AS Risk_Group,\n" +
				"             PRE_RX_SMEAR.value_datetime AS Smear_date_taken,\n" +
				"             PRE_RX_SMEAR.name AS Smear_Results,\n" +
				"             PRE_RX_GENE.value_datetime AS Genexpert_date_taken,\n" +
				"             PRE_RX_GENE.name AS Genexpert_Results,\n" +
				"             PRE_RX_OTHER.value_datetime AS Other_date_taken,\n" +
				"             PRE_RX_OTHER.value_text AS Other_Results,\n" +
				"             MONTH_2_TEST.value_coded AS Month_2_Results,\n" +
				"             MONTH_2_TEST.value_datetime AS Month_2_date,\n" +
				"             MONTH_5_TEST.value_coded AS Month_5_Results,\n" +
				"             MONTH_5_TEST.value_datetime AS Month_5_date,\n" +
				"             MONTH_6_TEST.value_coded AS Month_6_Results,\n" +
				"             MONTH_6_TEST.value_datetime AS Month_6_date,\n" +
				"             WEIGHT.weight as weight,\n" +
				"             HEIGHT.height as bmi,\n" +
				"             MUAC.muac ,\n" +
				"             MUACCODE.muac_code,\n" +
				"             ZSCORE.score,\n" +
				"             NUTRITION_STATUS.status,\n" +
				"             NUTRITION.support,\n" +
				"             INR.INRNo, \n" +
				"			  TRANSFER_IN.value_text AS transferred_in_from,\n" +
				"             TRANSFER_IN_TB_NO.value_text AS transfer_in_Unit_TB_No,\n" +
				"             REFFERAL_TYPE.name AS refferal,\n" +
				"             REFFERAL_DATE.value_datetime AS refferal_Date" +
				"             from (SELECT\t    e.encounter_id,\n" +
				"\t\t\t    e.patient_id,\n" +
				"\t\t\t    e.encounter_datetime\n" +
				"\t\t\t  FROM   encounter e\n" +
				"\t\t\t  INNER JOIN encounter_type et ON et.encounter_type_id = e.encounter_type AND et.uuid = '334bf97e-28e2-4a27-8727-a5ce31c7cd66'\n" +
	String.format(			"\t\t              WHERE e.encounter_datetime between '%s' and '%s') A\n",startDate,endDate) +
				"INNER JOIN person P\n" +
				"\t\t\t   ON (P.person_id = A.patient_id)\n" +
				"\t\t\t LEFT JOIN person_name PN ON (P.person_id = PN.person_id)\n" +
				"\t\t\t LEFT JOIN person_address PA ON (P.person_id = PA.person_id AND PA.preferred = 1 AND PA.voided = 0)\n" +
				"\t\t\t LEFT JOIN person_attribute PAT ON (P.person_id = PAT.person_id AND PAT.person_attribute_type_id = 8 AND PAT.voided = 0)\n" +
				"\t\t\t LEFT JOIN (select patient_id,identifier from patient_identifier PI inner join patient_identifier_type pit on\n" +
				"\t\t\t     PI.identifier_type = pit.patient_identifier_type_id where pit.uuid='f0c16a6d-dc5f-4118-a803-616d0075d282' and PI.voided=0)NIN  ON P.person_id = NIN.patient_id\n" +
				"             LEFT JOIN obs DISTRICT ON DISTRICT.encounter_id = A.encounter_id AND DISTRICT.concept_id = 99031 AND DISTRICT.voided = 0\n" +
				"             LEFT JOIN obs UNIT ON UNIT.encounter_id = A.encounter_id AND UNIT.concept_id = 165826 AND UNIT.voided = 0\n" +
				"             LEFT JOIN obs NEXT_OF_KIN ON NEXT_OF_KIN.encounter_id = A.encounter_id AND NEXT_OF_KIN.concept_id = 162729 AND NEXT_OF_KIN.voided = 0\n" +
				"             LEFT JOIN obs NEXT_OF_KIN_CONTACT ON NEXT_OF_KIN_CONTACT.encounter_id = A.encounter_id AND NEXT_OF_KIN_CONTACT.concept_id = 165052 AND NEXT_OF_KIN_CONTACT.voided = 0\n" +
				"             LEFT JOIN (select person_id,cn.name as value from  person_attribute PA inner join person_attribute_type PAT  on PA.person_attribute_type_id = PAT.person_attribute_type_id INNER JOIN\n" +
				"                 concept_name cn ON cn.concept_id=PA.value and PAT.uuid='dec484be-1c43-416a-9ad0-18bd9ef28929' and PA.voided=0) CATEGORY ON P.person_id = CATEGORY.person_id\n" +
				"             LEFT JOIN (SELECT\n" +
				"\t\t\t\to.encounter_id, cn.name\n" +
				"\t\t\t     FROM obs o\n" +
				"\t\t\t     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 99379 AND o.voided = 0\n" +
				"\t\t\t     GROUP BY o.encounter_id ) DISEASE_CLASS ON DISEASE_CLASS.encounter_id = A.encounter_id\n" +
				"             LEFT JOIN obs 1stLineSatrtDate ON 1stLineSatrtDate.encounter_id = A.encounter_id AND 1stLineSatrtDate.concept_id = 165838 AND 1stLineSatrtDate.voided = 0\n" +
				"             LEFT JOIN  (SELECT\n" +
				"\t\t\t\to.encounter_id, cn.name\n" +
				"\t\t\t     FROM obs o\n" +
				"\t\t\t     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 159958 AND o.voided = 0\n" +
				"\t\t\t     GROUP BY o.encounter_id ) 1ST_REGIMEN ON 1ST_REGIMEN.encounter_id = A.encounter_id\n" +
				"\t\t\t LEFT JOIN  (SELECT\n" +
				"\t\t\t\to.encounter_id, cn.name\n" +
				"\t\t\t     FROM obs o\n" +
				"\t\t\t     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 99386 AND o.voided = 0\n" +
				"\t\t\t     GROUP BY o.encounter_id ) PATIENT_TYPE ON PATIENT_TYPE.encounter_id = A.encounter_id\n" +
				"             LEFT JOIN  (SELECT\n" +
				"\t\t\t\to.encounter_id, cn.name\n" +
				"\t\t\t     FROM obs o\n" +
				"\t\t\t     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 165169 AND o.voided = 0\n" +
				"\t\t\t     GROUP BY o.encounter_id ) RISK_GROUP ON RISK_GROUP.encounter_id = A.encounter_id\n" +
				"             LEFT JOIN  (SELECT p.encounter_id, date_taken.value_datetime,cn.name from obs p inner join obs results on p.encounter_id=results.encounter_id\n" +
				"                    INNER JOIN obs date_taken on p.obs_group_id= date_taken.obs_group_id\n" +
				"                    INNER JOIN concept_name cn\n" +
				"                        ON results.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0\n" +
				"                        where p.obs_group_id in (SELECT obs_id from obs o  where o.concept_id= 165846  and o.voided=0)\n" +
				"                        and p.concept_id=165844 and p.value_coded=90225 and results.concept_id=90225\n" +
				"                          and date_taken.concept_id=164431 and date_taken.voided=0 and results.value_coded is not null group by p.encounter_id)PRE_RX_SMEAR on PRE_RX_SMEAR.encounter_id= A.encounter_id\n" +
				"             LEFT JOIN  (SELECT p.encounter_id, date_taken.value_datetime,cn.name from obs p inner join obs results on p.encounter_id=results.encounter_id\n" +
				"                    INNER JOIN obs date_taken on p.obs_group_id= date_taken.obs_group_id\n" +
				"                    INNER JOIN concept_name cn\n" +
				"                        ON results.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0\n" +
				"                        where p.obs_group_id in (SELECT obs_id from obs o  where o.concept_id= 165846  and o.voided=0)\n" +
				"                        and p.concept_id=165844 and p.value_coded=165413 and results.concept_id=162202\n" +
				"                          and date_taken.concept_id=164431 and date_taken.voided=0 and results.value_coded is not null group by p.encounter_id)PRE_RX_GENE on PRE_RX_GENE.encounter_id= A.encounter_id\n" +
				"             LEFT JOIN  (SELECT p.encounter_id, date_taken.value_datetime,results.value_text from obs p inner join obs results on p.encounter_id=results.encounter_id\n" +
				"                    INNER JOIN obs date_taken on p.obs_group_id= date_taken.obs_group_id\n" +
				"                        where p.obs_group_id in (SELECT obs_id from obs o  where o.concept_id= 165846  and o.voided=0)\n" +
				"                        and p.concept_id=165844 and p.value_coded=90002 and results.concept_id=99291\n" +
				"                          and date_taken.concept_id=164431 and date_taken.voided=0 and results.value_coded is not null group by p.encounter_id)PRE_RX_OTHER on PRE_RX_OTHER.encounter_id= A.encounter_id\n" +
				"             LEFT JOIN (SELECT o.person_id,date_taken.value_datetime,cn.name value_coded from obs o join obs test on o.encounter_id = test.encounter_id  INNER JOIN obs date_taken on o.obs_group_id= date_taken.obs_group_id\n" +
				"                 join encounter e2 on o.encounter_id = e2.encounter_id INNER JOIN concept_name cn ON test.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED'\n" +
				"                      AND cn.voided = 0 WHERE o.obs_group_id in (SELECT obs_id from obs o  where o.concept_id= 99292  and o.voided=0)\n" +
	String.format(			"                    and date_taken.concept_id= 164431 and date_taken.voided=0 and test.concept_id=90225 and test.voided=0 AND e2.encounter_datetime between DATE_ADD('%s',INTERVAL 2 MONTH)\n",startDate) +
	String.format(			"                    AND DATE_ADD('%s',INTERVAL 2 MONTH) group by o.encounter_id order by encounter_datetime DESC) MONTH_2_TEST ON MONTH_2_TEST.person_id = A.patient_id\n",endDate) +
				"             LEFT JOIN (SELECT o.person_id,date_taken.value_datetime,cn.name value_coded from obs o join obs test on o.encounter_id = test.encounter_id  INNER JOIN obs date_taken on o.obs_group_id= date_taken.obs_group_id\n" +
				"                 join encounter e2 on o.encounter_id = e2.encounter_id INNER JOIN concept_name cn ON test.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED'\n" +
				"                      AND cn.voided = 0 WHERE o.obs_group_id in (SELECT obs_id from obs o  where o.concept_id= 99292  and o.voided=0)\n" +
	String.format(			"                    and date_taken.concept_id= 164431 and date_taken.voided=0 and test.concept_id=90225 and test.voided=0 AND e2.encounter_datetime between DATE_ADD('%s',INTERVAL 5 MONTH)\n",startDate) +
	String.format(			"                    AND DATE_ADD('%s',INTERVAL 5 MONTH) group by o.encounter_id order by encounter_datetime DESC) MONTH_5_TEST ON MONTH_5_TEST.person_id = A.patient_id\n",endDate) +
				"\t\t\t LEFT JOIN (SELECT o.person_id,date_taken.value_datetime,cn.name value_coded from obs o join obs test on o.encounter_id = test.encounter_id  INNER JOIN obs date_taken on o.obs_group_id= date_taken.obs_group_id\n" +
				"                 join encounter e2 on o.encounter_id = e2.encounter_id INNER JOIN concept_name cn ON test.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED'\n" +
				"                      AND cn.voided = 0 WHERE o.obs_group_id in (SELECT obs_id from obs o  where o.concept_id= 99292  and o.voided=0)\n" +
	String.format(			"                    and date_taken.concept_id= 164431 and date_taken.voided=0 and test.concept_id=90225 and test.voided=0 AND e2.encounter_datetime between DATE_ADD('%s',INTERVAL 6 MONTH)\n",startDate) +
	String.format(			"                    AND DATE_ADD('%s',INTERVAL 6 MONTH) group by o.encounter_id order by encounter_datetime DESC) MONTH_6_TEST ON MONTH_6_TEST.person_id = A.patient_id\n",endDate) +
				"             LEFT JOIN (SELECT o.person_id,value_numeric weight, min(encounter_datetime) from obs o join encounter e2 on o.encounter_id = e2.encounter_id\n" +
				"                 inner  join encounter_type t on e2.encounter_type = t.encounter_type_id and t.uuid='455bad1f-5e97-4ee9-9558-ff1df8808732' AND o.voided = 0 WHERE o.concept_id=5089\n" +
				"                 group by o.person_id order by encounter_datetime ASC  ) WEIGHT ON WEIGHT.person_id = A.patient_id\n" +
				"             LEFT JOIN (SELECT o.person_id,value_numeric height, min(encounter_datetime) from obs o join encounter e2 on o.encounter_id = e2.encounter_id\n" +
				"                 inner  join encounter_type t on e2.encounter_type = t.encounter_type_id and t.uuid='455bad1f-5e97-4ee9-9558-ff1df8808732' AND o.voided = 0 WHERE o.concept_id=5090\n" +
				"                 group by o.person_id order by encounter_datetime ASC ) HEIGHT ON HEIGHT.person_id = A.patient_id\n" +
				"             LEFT JOIN (SELECT o.person_id,value_numeric muac, min(encounter_datetime) from obs o join encounter e2 on o.encounter_id = e2.encounter_id\n" +
				"                 inner  join encounter_type t on e2.encounter_type = t.encounter_type_id and t.uuid='455bad1f-5e97-4ee9-9558-ff1df8808732' AND o.voided = 0 WHERE o.concept_id=1343\n" +
				"                 group by o.person_id order by encounter_datetime ASC ) MUAC ON MUAC.person_id = A.patient_id\n" +
				"             LEFT JOIN (SELECT o.person_id,cn.name muac_code, min(encounter_datetime) from obs o join encounter e2 on o.encounter_id = e2.encounter_id INNER  JOIN concept_name cn\n" +
				"                        ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0\n" +
				"                 inner  join encounter_type t on e2.encounter_type = t.encounter_type_id and t.uuid='455bad1f-5e97-4ee9-9558-ff1df8808732' AND o.voided = 0 WHERE o.concept_id=99030\n" +
				"                 group by o.person_id order by encounter_datetime ASC ) MUACCODE ON MUACCODE.person_id = A.patient_id\n" +
				"             LEFT JOIN (SELECT o.person_id,cn.name score, min(encounter_datetime) from obs o join encounter e2 on o.encounter_id = e2.encounter_id INNER  JOIN concept_name cn\n" +
				"                        ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0\n" +
				"                 inner  join encounter_type t on e2.encounter_type = t.encounter_type_id and t.uuid='455bad1f-5e97-4ee9-9558-ff1df8808732' AND o.voided = 0 WHERE o.concept_id=99800\n" +
				"                 group by o.person_id order by encounter_datetime ASC ) ZSCORE ON ZSCORE.person_id = A.patient_id\n" +
				"             LEFT JOIN (SELECT o.person_id,cn.name status, min(encounter_datetime) from obs o join encounter e2 on o.encounter_id = e2.encounter_id INNER  JOIN concept_name cn\n" +
				"                        ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0\n" +
				"                 inner  join encounter_type t on e2.encounter_type = t.encounter_type_id and t.uuid='455bad1f-5e97-4ee9-9558-ff1df8808732' AND o.voided = 0 WHERE o.concept_id=165050\n" +
				"                 group by o.person_id order by encounter_datetime ASC ) NUTRITION_STATUS ON NUTRITION_STATUS.person_id = A.patient_id\n" +
				"\t\t\t LEFT JOIN (SELECT o.person_id,cn.name support, min(encounter_datetime) from obs o join encounter e2 on o.encounter_id = e2.encounter_id INNER  JOIN concept_name cn\n" +
				"                        ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0\n" +
				"                 inner  join encounter_type t on e2.encounter_type = t.encounter_type_id and t.uuid='455bad1f-5e97-4ee9-9558-ff1df8808732' AND o.voided = 0 WHERE o.concept_id=99054\n" +
				"                 group by o.person_id order by encounter_datetime ASC ) NUTRITION ON NUTRITION.person_id = A.patient_id\n" +
				"             LEFT JOIN (SELECT o.person_id,value_numeric INRNo, min(encounter_datetime) from obs o join encounter e2 on o.encounter_id = e2.encounter_id\n" +
				"                 inner  join encounter_type t on e2.encounter_type = t.encounter_type_id and t.uuid='455bad1f-5e97-4ee9-9558-ff1df8808732' AND o.voided = 0 WHERE o.concept_id=99733\n" +
				"                 group by o.person_id order by encounter_datetime ASC ) INR ON INR.person_id = A.patient_id " +
                " LEFT JOIN (SELECT o.encounter_id,value_text FROM obs o  WHERE o.concept_id = 90211 AND o.voided = 0 GROUP BY o.encounter_id )TRANSFER_IN ON TRANSFER_IN.encounter_id = A.encounter_id\n" +
                "                            LEFT JOIN (SELECT o.encounter_id,value_text FROM obs o  WHERE o.concept_id = 165826 AND o.voided = 0 GROUP BY o.encounter_id )TRANSFER_IN_TB_NO ON TRANSFER_IN_TB_NO.encounter_id = A.encounter_id\n" +
                "                            LEFT JOIN (SELECT o.encounter_id, cn.name FROM obs o INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 165855 AND o.voided = 0\n" +
                "\t\t\t\t                 GROUP BY o.encounter_id ) REFFERAL_TYPE ON REFFERAL_TYPE.encounter_id = A.encounter_id\n" +
                "                            LEFT JOIN (SELECT o.encounter_id,value_datetime FROM obs o  WHERE o.concept_id = 166160 AND o.voided = 0 GROUP BY o.encounter_id ) REFFERAL_DATE ON REFFERAL_DATE.encounter_id = A.encounter_id\n"+
				"             INNER JOIN (SELECT patient_id,cn.name outcome, date_completed outcome_date from patient_program pp inner join program p on pp.program_id = p.program_id and p.uuid='9dc21a72-0971-11e7-8037-507b9dc4c741'\n" +
				"               LEFT JOIN concept_name cn ON pp.outcome_concept_id = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 where pp.date_enrolled between\n" +
				String.format(		"\t\t\t    '%s' and '%s' group by patient_id ) PROGRAM on PROGRAM.patient_id = A.patient_id\n",startDate,endDate);

		String ecounterAndProgranQuery = "SELECT   A.patient_id patient_id,\n" +
				"             HIVSTATUS.status as  HIVSTATUS,\n" +
				"             HIVSTATUSDATE.value_datetime as HIVSTATUSDATE,\n" +
				"             CPTDATE.value_datetime as CPT_DATE,\n" +
				"             CPT.status as CPPT_STATUS,\n" +
				"             ART_STATUS.status as ART_STATUS,\n" +
				"             ARTSTARTDATE.value_datetime as ARTSTART_DATE,\n" +
				"             ARTNO.value_text as ART_NO,\n" +
				"             TREATMENTMODEL.status as TREATMENT_MODEL,\n" +
				"             WEEK1_2.encounter_datetime AS  WEEK1_2DATE,\n" +
				"             WEEK1_2.value_numeric AS  WEEK1_2DAYS,\n" +
				"             WEEK3_4.encounter_datetime AS  WEEK3_4DATE,\n" +
				"             WEEK3_4.value_numeric AS  WEEK3_4DAYS,\n" +
				"             WEEK5_6.encounter_datetime AS  WEEK5_6DATE,\n" +
				"             WEEK5_6.value_numeric AS  WEEK5_6DAYS,\n" +
				"             WEEK7_8.encounter_datetime AS  WEEK7_8DATE,\n" +
				"             WEEK7_8.value_numeric AS  WEEK7_8DAYS,\n" +
				"             MONTH_3.encounter_datetime AS  MONTH_3DATE,\n" +
				"             MONTH_3.value_numeric AS  MONTH_3DAYS,\n" +
				"             MONTH_4.encounter_datetime AS  MONTH_4DATE,\n" +
				"             MONTH_4.value_numeric AS  MONTH_4DAYS,\n" +
				"             MONTH_5.encounter_datetime AS  MONTH_5DATE,\n" +
				"             MONTH_5.value_numeric AS  MONTH_5DAYS,\n" +
				"             MONTH_6.encounter_datetime AS  MONTH_6DATE,\n" +
				"             MONTH_6.value_numeric AS  MONTH_6DAYS,\n" +
				"             MONTH_7.encounter_datetime AS  MONTH_7DATE,\n" +
				"             MONTH_7.value_numeric AS  MONTH_7DAYS,\n" +
				"             MONTH_8.encounter_datetime AS  MONTH_8DATE,\n" +
				"             MONTH_8.value_numeric AS  MONTH_8DAYS,\n" +
				"             MONTH_9.encounter_datetime AS  MONTH_9DATE,\n" +
				"             MONTH_9.value_numeric AS  MONTH_9DAYS,\n" +
				"             MONTH_10.encounter_datetime AS  MONTH_10DATE,\n" +
				"             MONTH_10.value_numeric AS  MONTH_10DAYS,\n" +
				"             MONTH_11.encounter_datetime AS  MONTH_11DATE,\n" +
				"             MONTH_11.value_numeric AS  MONTH_11DAYS,\n" +
				"             MONTH_12.encounter_datetime AS  MONTH_12DATE,\n" +
				"             MONTH_12.value_numeric AS  MONTH_12DAYS,\n" +
				"             T_OUT_DATE.value_datetime as Transfer_out_Date,\n" +
				"             T_OUT_HF.value_text as Transfer_out_Facility,\n" +
				"             T_OUT_DISTRICT.value_text as District,\n" +
				"             T_OUT_TEL.value_text as TO_TEL,\n" +
				"             PROGRAM.outcome as Outcome,\n" +
				"             PROGRAM.outcome_date as Outcome_Date,\n" +
				"\t\t\t DRTB.name AS diagnosed_with_DRTB,\n" +
				"             DRTB_DATE.value_datetime AS date_diagnosed_with_DRTB,\n" +
				"             STARTED_DRTB.value_coded as STARTED_ON_DR_TREATMENT,\n" +
				"             STARTED_DRTB_DATE.value_datetime as DATE_STARTED_ON_DR_TREATMENT,\n" +
				"             DRTB_no.value_text as DRTBNO\n" +
				"             from (SELECT\t    e.encounter_id,\n" +
				"\t\t\t    e.patient_id,\n" +
				"\t\t\t    e.encounter_datetime\n" +
				"\t\t\t  FROM   encounter e\n" +
				"\t\t\t  INNER JOIN encounter_type et ON et.encounter_type_id = e.encounter_type AND et.uuid = '334bf97e-28e2-4a27-8727-a5ce31c7cd66'\n" +
		String.format(			"\t\t              WHERE e.encounter_datetime between '%s' and '%s') A\n",startDate,endDate)+
				"             LEFT JOIN (SELECT o.person_id,o.encounter_id,cn.name status from obs o  INNER  JOIN concept_name cn\n" +
				"                        ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id=165396\n" +
				"                 and o.voided=0 group by encounter_id ) HIVSTATUS ON HIVSTATUS.encounter_id= A.encounter_id\n" +
				"             LEFT JOIN (SELECT o.person_id,o.encounter_id,value_datetime from obs o WHERE o.concept_id=164431 and o.voided=0 group by encounter_id) HIVSTATUSDATE ON HIVSTATUSDATE.encounter_id= A.encounter_id\n" +
				"\t\t     LEFT JOIN (SELECT o.person_id,o.encounter_id,value_datetime from obs o WHERE o.concept_id=165881 and o.voided=0 group by encounter_id) CPTDATE ON CPTDATE.encounter_id= A.encounter_id\n" +
				"             LEFT JOIN (SELECT o.person_id,o.encounter_id,cn.name status from obs o INNER  JOIN concept_name cn\n" +
				"                        ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0\n" +
				"                  WHERE o.concept_id=166070 AND o.voided = 0 group by encounter_id) CPT ON CPT.encounter_id= A.encounter_id\n" +
				"             LEFT JOIN (SELECT o.person_id,o.encounter_id,cn.name status from obs o INNER  JOIN concept_name cn\n" +
				"                        ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0\n" +
				"                  WHERE o.concept_id=1358 AND o.voided = 0 group by encounter_id) ART_STATUS ON ART_STATUS.encounter_id= A.encounter_id\n" +
				"             LEFT JOIN (SELECT o.person_id,o.encounter_id,value_datetime from obs o WHERE o.concept_id=99161 and o.voided=0 group by encounter_id) ARTSTARTDATE ON ARTSTARTDATE.encounter_id= A.encounter_id\n" +
				"             LEFT JOIN (SELECT o.person_id,o.encounter_id,value_text from obs o WHERE o.concept_id=99431 and o.voided=0 group by encounter_id) ARTNO ON ARTNO.encounter_id= A.encounter_id\n" +
				"             LEFT JOIN (SELECT o.person_id,o.encounter_id,cn.name status from obs o  INNER  JOIN concept_name cn\n" +
				"                        ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id=165869\n" +
				"                 and o.voided=0 group by encounter_id ) TREATMENTMODEL ON TREATMENTMODEL.encounter_id= A.encounter_id\n" +
				"             LEFT JOIN (SELECT o.person_id,encounter_datetime, days.value_numeric from encounter e2 INNER JOIN obs days on e2.encounter_id = days.encounter_id join obs o on o.encounter_id=e2.encounter_id\n" +
				"                 inner  join encounter_type t on  e2.encounter_type = t.encounter_type_id and t.uuid='455bad1f-5e97-4ee9-9558-ff1df8808732' inner join patient_program pp on e2.patient_id = pp.patient_id " +
				" 					 inner join program p on pp.program_id = p.program_id and p.uuid='9dc21a72-0971-11e7-8037-507b9dc4c741' \n" +
				String.format("                    WHERE o.obs_group_id in (SELECT obs_id from obs o  where o.concept_id= 165305  and o.voided=0) and days.obs_group_id=o.obs_group_id and pp.date_enrolled between '%s' and '%s'\n",startDate,endDate) +
				"                    and days.concept_id= 159368 and days.voided=0 AND e2.encounter_datetime between pp.date_enrolled \n" +
				"                    AND DATE_ADD(pp.date_enrolled,INTERVAL 2 WEEK) group by o.obs_group_id order by encounter_datetime DESC ) WEEK1_2 ON WEEK1_2.person_id = A.patient_id\n" +
				"             LEFT JOIN (SELECT o.person_id,encounter_datetime, days.value_numeric from encounter e2 INNER JOIN obs days on e2.encounter_id = days.encounter_id join obs o on o.encounter_id=e2.encounter_id\n" +
				"                 inner  join encounter_type t on  e2.encounter_type = t.encounter_type_id and t.uuid='455bad1f-5e97-4ee9-9558-ff1df8808732' inner join patient_program pp on e2.patient_id = pp.patient_id " +
				" 					 inner join program p on pp.program_id = p.program_id and p.uuid='9dc21a72-0971-11e7-8037-507b9dc4c741' \n" +
				String.format("                    WHERE o.obs_group_id in (SELECT obs_id from obs o  where o.concept_id= 165305  and o.voided=0) and days.obs_group_id=o.obs_group_id and pp.date_enrolled between '%s' and '%s'\n",startDate,endDate) +
				"                    and days.concept_id= 159368 and days.voided=0 AND e2.encounter_datetime between DATE_ADD(pp.date_enrolled,INTERVAL 15 DAY)\n" +
				"                    AND DATE_ADD(pp.date_enrolled,INTERVAL 4 WEEK) group by o.obs_group_id order by encounter_datetime DESC ) WEEK3_4 ON WEEK3_4.person_id = A.patient_id\n" +
				"             LEFT JOIN (SELECT o.person_id,encounter_datetime, days.value_numeric from encounter e2 INNER JOIN obs days on e2.encounter_id = days.encounter_id join obs o on o.encounter_id=e2.encounter_id\n" +
				"                 inner  join encounter_type t on  e2.encounter_type = t.encounter_type_id and t.uuid='455bad1f-5e97-4ee9-9558-ff1df8808732' inner join patient_program pp on e2.patient_id = pp.patient_id " +
				" 					 inner join program p on pp.program_id = p.program_id and p.uuid='9dc21a72-0971-11e7-8037-507b9dc4c741' \n" +
				String.format("                    WHERE o.obs_group_id in (SELECT obs_id from obs o  where o.concept_id= 165305  and o.voided=0) and days.obs_group_id=o.obs_group_id and pp.date_enrolled between '%s' and '%s'\n",startDate,endDate) +
				"                    and days.concept_id= 159368 and days.voided=0 AND e2.encounter_datetime between DATE_ADD(pp.date_enrolled,INTERVAL 29 DAY)\n" +
				"                    AND DATE_ADD(pp.date_enrolled,INTERVAL 6 WEEK) group by o.obs_group_id order by encounter_datetime DESC ) WEEK5_6 ON WEEK5_6.person_id = A.patient_id\n" +
				"             LEFT JOIN (SELECT o.person_id,encounter_datetime, days.value_numeric from encounter e2 INNER JOIN obs days on e2.encounter_id = days.encounter_id join obs o on o.encounter_id=e2.encounter_id\n" +
				"                 inner  join encounter_type t on  e2.encounter_type = t.encounter_type_id and t.uuid='455bad1f-5e97-4ee9-9558-ff1df8808732' inner join patient_program pp on e2.patient_id = pp.patient_id " +
				" 					 inner join program p on pp.program_id = p.program_id and p.uuid='9dc21a72-0971-11e7-8037-507b9dc4c741' \n" +
				String.format("                    WHERE o.obs_group_id in (SELECT obs_id from obs o  where o.concept_id= 165305  and o.voided=0) and days.obs_group_id=o.obs_group_id and pp.date_enrolled between '%s' and '%s'\n",startDate,endDate) +
				"                    and days.concept_id= 159368 and days.voided=0 AND e2.encounter_datetime between DATE_ADD(pp.date_enrolled,INTERVAL 43 DAY)\n" +
				"                    AND DATE_ADD(pp.date_enrolled,INTERVAL 8 WEEK) group by o.obs_group_id order by encounter_datetime DESC ) WEEK7_8 ON WEEK7_8.person_id = A.patient_id\n" +
				"             LEFT JOIN (SELECT o.person_id,encounter_datetime, days.value_numeric from encounter e2 INNER JOIN obs days on e2.encounter_id = days.encounter_id join obs o on o.encounter_id=e2.encounter_id\n" +
				"                    inner  join encounter_type t on  e2.encounter_type = t.encounter_type_id and t.uuid='455bad1f-5e97-4ee9-9558-ff1df8808732' inner join patient_program pp on e2.patient_id = pp.patient_id " +
				" 					 inner join program p on pp.program_id = p.program_id and p.uuid='9dc21a72-0971-11e7-8037-507b9dc4c741' \n" +
				String.format("                    WHERE o.obs_group_id in (SELECT obs_id from obs o  where o.concept_id= 165305  and o.voided=0) and days.obs_group_id=o.obs_group_id and pp.date_enrolled between '%s' and '%s'\n",startDate,endDate) +
				"                    and days.concept_id= 159368 and days.voided=0 AND e2.encounter_datetime between DATE_ADD(pp.date_enrolled,INTERVAL 3 MONTH )\n" +
				"                    AND DATE_SUB(DATE_ADD(pp.date_enrolled,INTERVAL 4 MONTH),INTERVAL 1 DAY) group by o.obs_group_id order by encounter_datetime DESC ) MONTH_3 ON MONTH_3.person_id = A.patient_id\n" +
				"             LEFT JOIN (SELECT o.person_id,encounter_datetime, days.value_numeric from encounter e2 INNER JOIN obs days on e2.encounter_id = days.encounter_id join obs o on o.encounter_id=e2.encounter_id\n" +
				"                    inner  join encounter_type t on  e2.encounter_type = t.encounter_type_id and t.uuid='455bad1f-5e97-4ee9-9558-ff1df8808732' inner join patient_program pp on e2.patient_id = pp.patient_id " +
				" 					 inner join program p on pp.program_id = p.program_id and p.uuid='9dc21a72-0971-11e7-8037-507b9dc4c741' \n" +
				String.format("                    WHERE o.obs_group_id in (SELECT obs_id from obs o  where o.concept_id= 165305  and o.voided=0) and days.obs_group_id=o.obs_group_id and pp.date_enrolled between '%s' and '%s'\n",startDate,endDate) +
				"                    and days.concept_id= 159368 and days.voided=0 AND e2.encounter_datetime between DATE_ADD(pp.date_enrolled,INTERVAL 4 MONTH )\n" +
				"                    AND DATE_SUB(DATE_ADD(pp.date_enrolled,INTERVAL 5 MONTH),INTERVAL 1 DAY) group by o.obs_group_id order by encounter_datetime DESC ) MONTH_4 ON MONTH_4.person_id = A.patient_id\n" +
				"             LEFT JOIN (SELECT o.person_id,encounter_datetime, days.value_numeric from encounter e2 INNER JOIN obs days on e2.encounter_id = days.encounter_id join obs o on o.encounter_id=e2.encounter_id\n" +
				"                    inner  join encounter_type t on  e2.encounter_type = t.encounter_type_id and t.uuid='455bad1f-5e97-4ee9-9558-ff1df8808732' inner join patient_program pp on e2.patient_id = pp.patient_id " +
				" 					 inner join program p on pp.program_id = p.program_id and p.uuid='9dc21a72-0971-11e7-8037-507b9dc4c741' \n" +
				String.format("                    WHERE o.obs_group_id in (SELECT obs_id from obs o  where o.concept_id= 165305  and o.voided=0) and days.obs_group_id=o.obs_group_id and pp.date_enrolled between '%s' and '%s'\n",startDate,endDate) +
				"                    and days.concept_id= 159368 and days.voided=0 AND e2.encounter_datetime between DATE_ADD(pp.date_enrolled,INTERVAL 5 MONTH )\n" +
				"                    AND DATE_SUB(DATE_ADD(pp.date_enrolled,INTERVAL 6 MONTH),INTERVAL 1 DAY) group by o.obs_group_id order by encounter_datetime DESC ) MONTH_5 ON MONTH_5.person_id = A.patient_id\n" +
				"             LEFT JOIN (SELECT o.person_id,encounter_datetime, days.value_numeric from encounter e2 INNER JOIN obs days on e2.encounter_id = days.encounter_id join obs o on o.encounter_id=e2.encounter_id\n" +
				"                    inner  join encounter_type t on  e2.encounter_type = t.encounter_type_id and t.uuid='455bad1f-5e97-4ee9-9558-ff1df8808732' inner join patient_program pp on e2.patient_id = pp.patient_id " +
				" 					 inner join program p on pp.program_id = p.program_id and p.uuid='9dc21a72-0971-11e7-8037-507b9dc4c741' \n" +
				String.format("                    WHERE o.obs_group_id in (SELECT obs_id from obs o  where o.concept_id= 165305  and o.voided=0) and days.obs_group_id=o.obs_group_id and pp.date_enrolled between '%s' and '%s'\n",startDate,endDate) +
				"                    and days.concept_id= 159368 and days.voided=0 AND e2.encounter_datetime between DATE_ADD(pp.date_enrolled,INTERVAL 6 MONTH )\n" +
				"                    AND DATE_SUB(DATE_ADD(pp.date_enrolled,INTERVAL 7 MONTH),INTERVAL 1 DAY) group by o.obs_group_id order by encounter_datetime DESC ) MONTH_6 ON MONTH_6.person_id = A.patient_id\n" +
				"             LEFT JOIN (SELECT o.person_id,encounter_datetime, days.value_numeric from encounter e2 INNER JOIN obs days on e2.encounter_id = days.encounter_id join obs o on o.encounter_id=e2.encounter_id\n" +
				"                    inner  join encounter_type t on  e2.encounter_type = t.encounter_type_id and t.uuid='455bad1f-5e97-4ee9-9558-ff1df8808732' inner join patient_program pp on e2.patient_id = pp.patient_id " +
				" 					 inner join program p on pp.program_id = p.program_id and p.uuid='9dc21a72-0971-11e7-8037-507b9dc4c741' \n" +
				String.format("                    WHERE o.obs_group_id in (SELECT obs_id from obs o  where o.concept_id= 165305  and o.voided=0) and days.obs_group_id=o.obs_group_id and pp.date_enrolled between '%s' and '%s'\n",startDate,endDate) +
				"                    and days.concept_id= 159368 and days.voided=0 AND e2.encounter_datetime between DATE_ADD(pp.date_enrolled,INTERVAL 7 MONTH )\n" +
				"                    AND DATE_SUB(DATE_ADD(pp.date_enrolled,INTERVAL 8 MONTH),INTERVAL 1 DAY) group by o.obs_group_id order by encounter_datetime DESC ) MONTH_7 ON MONTH_7.person_id = A.patient_id\n" +
				"             LEFT JOIN (SELECT o.person_id,encounter_datetime, days.value_numeric from encounter e2 INNER JOIN obs days on e2.encounter_id = days.encounter_id join obs o on o.encounter_id=e2.encounter_id\n" +
				"                    inner  join encounter_type t on  e2.encounter_type = t.encounter_type_id and t.uuid='455bad1f-5e97-4ee9-9558-ff1df8808732' inner join patient_program pp on e2.patient_id = pp.patient_id " +
				" 					 inner join program p on pp.program_id = p.program_id and p.uuid='9dc21a72-0971-11e7-8037-507b9dc4c741' \n" +
				String.format("                    WHERE o.obs_group_id in (SELECT obs_id from obs o  where o.concept_id= 165305  and o.voided=0) and days.obs_group_id=o.obs_group_id and pp.date_enrolled between '%s' and '%s'\n",startDate,endDate) +
				"                    and days.concept_id= 159368 and days.voided=0 AND e2.encounter_datetime between DATE_ADD(pp.date_enrolled,INTERVAL 8 MONTH )\n" +
				"                    AND DATE_SUB(DATE_ADD(pp.date_enrolled,INTERVAL 9 MONTH),INTERVAL 1 DAY) group by o.obs_group_id order by encounter_datetime DESC ) MONTH_8 ON MONTH_8.person_id = A.patient_id\n" +
				"             LEFT JOIN (SELECT o.person_id,encounter_datetime, days.value_numeric from encounter e2 INNER JOIN obs days on e2.encounter_id = days.encounter_id join obs o on o.encounter_id=e2.encounter_id\n" +
				"                    inner  join encounter_type t on  e2.encounter_type = t.encounter_type_id and t.uuid='455bad1f-5e97-4ee9-9558-ff1df8808732' inner join patient_program pp on e2.patient_id = pp.patient_id " +
				" 					 inner join program p on pp.program_id = p.program_id and p.uuid='9dc21a72-0971-11e7-8037-507b9dc4c741' \n" +
				String.format("                    WHERE o.obs_group_id in (SELECT obs_id from obs o  where o.concept_id= 165305  and o.voided=0) and days.obs_group_id=o.obs_group_id and pp.date_enrolled between '%s' and '%s'\n",startDate,endDate) +
				"                    and days.concept_id= 159368 and days.voided=0 AND e2.encounter_datetime between DATE_ADD(pp.date_enrolled,INTERVAL 9 MONTH )\n" +
				"                    AND DATE_SUB(DATE_ADD(pp.date_enrolled,INTERVAL 10 MONTH),INTERVAL 1 DAY) group by o.obs_group_id order by encounter_datetime DESC ) MONTH_9 ON MONTH_9.person_id = A.patient_id\n" +
				"             LEFT JOIN (SELECT o.person_id,encounter_datetime, days.value_numeric from encounter e2 INNER JOIN obs days on e2.encounter_id = days.encounter_id join obs o on o.encounter_id=e2.encounter_id\n" +
				"                    inner  join encounter_type t on  e2.encounter_type = t.encounter_type_id and t.uuid='455bad1f-5e97-4ee9-9558-ff1df8808732' inner join patient_program pp on e2.patient_id = pp.patient_id " +
				" 					 inner join program p on pp.program_id = p.program_id and p.uuid='9dc21a72-0971-11e7-8037-507b9dc4c741' \n" +
				String.format("                    WHERE o.obs_group_id in (SELECT obs_id from obs o  where o.concept_id= 165305  and o.voided=0) and days.obs_group_id=o.obs_group_id and pp.date_enrolled between '%s' and '%s'\n",startDate,endDate) +
				"                    and days.concept_id= 159368 and days.voided=0 AND e2.encounter_datetime between DATE_ADD(pp.date_enrolled,INTERVAL 10 MONTH )\n" +
				"                    AND DATE_SUB(DATE_ADD(pp.date_enrolled,INTERVAL 11 MONTH),INTERVAL 1 DAY) group by o.obs_group_id order by encounter_datetime DESC ) MONTH_10 ON MONTH_10.person_id = A.patient_id\n" +
				"             LEFT JOIN (SELECT o.person_id,encounter_datetime, days.value_numeric from encounter e2 INNER JOIN obs days on e2.encounter_id = days.encounter_id join obs o on o.encounter_id=e2.encounter_id\n" +
				"                    inner  join encounter_type t on  e2.encounter_type = t.encounter_type_id and t.uuid='455bad1f-5e97-4ee9-9558-ff1df8808732' inner join patient_program pp on e2.patient_id = pp.patient_id " +
				" 					 inner join program p on pp.program_id = p.program_id and p.uuid='9dc21a72-0971-11e7-8037-507b9dc4c741' \n" +
				String.format("                    WHERE o.obs_group_id in (SELECT obs_id from obs o  where o.concept_id= 165305  and o.voided=0) and days.obs_group_id=o.obs_group_id and pp.date_enrolled between '%s' and '%s'\n",startDate,endDate) +
				"                    and days.concept_id= 159368 and days.voided=0 AND e2.encounter_datetime between DATE_ADD(pp.date_enrolled,INTERVAL 11 MONTH )\n" +
				"                    AND DATE_SUB(DATE_ADD(pp.date_enrolled,INTERVAL 12 MONTH),INTERVAL 1 DAY) group by o.obs_group_id order by encounter_datetime DESC ) MONTH_11 ON MONTH_11.person_id = A.patient_id\n" +
				"             LEFT JOIN (SELECT o.person_id,encounter_datetime, days.value_numeric from encounter e2 INNER JOIN obs days on e2.encounter_id = days.encounter_id join obs o on o.encounter_id=e2.encounter_id\n" +
				"                    inner  join encounter_type t on  e2.encounter_type = t.encounter_type_id and t.uuid='455bad1f-5e97-4ee9-9558-ff1df8808732' inner join patient_program pp on e2.patient_id = pp.patient_id " +
				" 					 inner join program p on pp.program_id = p.program_id and p.uuid='9dc21a72-0971-11e7-8037-507b9dc4c741' \n" +
				String.format("                    WHERE o.obs_group_id in (SELECT obs_id from obs o  where o.concept_id= 165305  and o.voided=0) and days.obs_group_id=o.obs_group_id and pp.date_enrolled between '%s' and '%s'\n",startDate,endDate) +
				"                    and days.concept_id= 159368 and days.voided=0 AND e2.encounter_datetime between DATE_ADD(pp.date_enrolled,INTERVAL 12 MONTH )\n" +
				"                    AND DATE_SUB(DATE_ADD(pp.date_enrolled,INTERVAL 13 MONTH),INTERVAL 1 DAY) group by o.obs_group_id order by encounter_datetime DESC ) MONTH_12 ON MONTH_12.person_id = A.patient_id\n" +
				"             LEFT JOIN (SELECT person_id, value_datetime,encounter_id from obs where concept_id=165854 and obs_group_id in (SELECT obs_group_id from obs groupid where concept_id=165855 and value_coded=160036 and voided=0) and voided=0  order by obs_group_id )T_OUT_DATE on T_OUT_DATE.encounter_id= A.encounter_id\n" +
				"             LEFT JOIN (SELECT person_id, value_text,encounter_id from obs where concept_id=90211 and obs_group_id in (SELECT obs_group_id from obs groupid where concept_id=165855 and value_coded=160036 and voided=0) and voided=0  order by obs_group_id )T_OUT_HF on T_OUT_HF.encounter_id= A.encounter_id\n" +
				"             LEFT JOIN (SELECT person_id, value_text,encounter_id from obs where concept_id=165853 and obs_group_id in (SELECT obs_group_id from obs groupid where concept_id=165855 and value_coded=160036 and voided=0) and voided=0  order by obs_group_id )T_OUT_DISTRICT on T_OUT_DISTRICT.encounter_id= A.encounter_id\n" +
				"             LEFT JOIN (SELECT person_id, value_text,encounter_id from obs where concept_id=159635 and obs_group_id in (SELECT obs_group_id from obs groupid where concept_id=165855 and value_coded=160036 and voided=0) and voided=0  order by obs_group_id )T_OUT_TEL on T_OUT_TEL.encounter_id= A.encounter_id\n" +
				"             INNER JOIN (SELECT patient_id,cn.name outcome, date_completed outcome_date from patient_program pp inner join program p on pp.program_id = p.program_id and p.uuid='9dc21a72-0971-11e7-8037-507b9dc4c741'\n" +
				"               LEFT JOIN concept_name cn ON pp.outcome_concept_id = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 where pp.date_enrolled between\n" +
		String.format(		"\t\t\t    '%s' and '%s' group by patient_id ) PROGRAM on PROGRAM.patient_id = A.patient_id\n",startDate,endDate) +
				"             LEFT JOIN (SELECT o.encounter_id, cn.name FROM obs o INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 90216 AND o.voided = 0\n" +
				"\t\t\t     GROUP BY o.encounter_id ) DRTB ON DRTB.encounter_id = A.encounter_id\n" +
				"             LEFT JOIN (SELECT o.encounter_id,value_datetime FROM obs o  WHERE o.concept_id = 165840 AND o.voided = 0 GROUP BY o.encounter_id ) DRTB_DATE ON DRTB_DATE.encounter_id = A.encounter_id\n" +
				"             LEFT JOIN (SELECT o.encounter_id, o.value_coded FROM obs o  WHERE o.concept_id = 165842 AND o.voided = 0\n" +
				"\t\t\t     GROUP BY o.encounter_id ) STARTED_DRTB ON STARTED_DRTB.encounter_id = A.encounter_id\n" +
				"             LEFT JOIN (SELECT o.encounter_id,value_datetime FROM obs o  WHERE o.concept_id = 165841 AND o.voided = 0 GROUP BY o.encounter_id ) STARTED_DRTB_DATE ON STARTED_DRTB_DATE.encounter_id = A.encounter_id\n" +
				"             LEFT JOIN (SELECT o.encounter_id,value_text FROM obs o  WHERE o.concept_id = 165843 AND o.voided = 0 GROUP BY o.encounter_id ) DRTB_no ON DRTB_no.encounter_id = A.encounter_id\n";



		//Logger.getLogger(TBDatasetDefinition.class).info(summarySql);
		SqlQueryBuilder q = new SqlQueryBuilder(summarySql);
		SqlQueryBuilder encounterQuery = new SqlQueryBuilder(ecounterAndProgranQuery);

		List<Object[]> results = evaluationService.evaluateToList(q, context);
		List<Object[]> results1 = evaluationService.evaluateToList(encounterQuery, context);

		PatientDataHelper pdh = new PatientDataHelper();

		for (Object[] r : results) {
			DataSetRow row = new DataSetRow();
			int patientId = Integer.valueOf(String.valueOf(r[0]));

			pdh.addCol(row, "patientid",patientId);
			pdh.addCol(row, "DOA", r[1]);
			pdh.addCol(row, "District No", r[2]);
			pdh.addCol(row, "Unit_No", r[3]);
			pdh.addCol(row, "NIN_no", r[4]);
			pdh.addCol(row, "Family_name", r[5]);
			pdh.addCol(row, "Given_name", r[6]);
			pdh.addCol(row, "Next_of_Kin", r[7]);
			pdh.addCol(row, "Next_of_kin_Contacts", r[8]);
			pdh.addCol(row, "Sex", r[9]);
			pdh.addCol(row, "Age", r[10]);
			pdh.addCol(row, "Client_category", r[11]);
			pdh.addCol(row, "Village", r[12]);
			pdh.addCol(row, "Parish", r[13]);
			pdh.addCol(row, "Sub county", r[14]);
			pdh.addCol(row, "District", r[15]);
			pdh.addCol(row, "Disease Classification", r[16]);
			pdh.addCol(row, "1st line start date", r[17]);
			pdh.addCol(row, "1st line drug", r[18]);
			pdh.addCol(row, "patient type", r[19]);
			pdh.addCol(row, "risk group", r[20]);
			pdh.addCol(row, "smear date taken", r[21]);
			pdh.addCol(row, "smear results", r[22]);
			pdh.addCol(row, "GeneXpert date taken", r[23]);
			pdh.addCol(row, "GeneXpert results", r[24]);
			pdh.addCol(row, "Other test date taken", r[25]);
			pdh.addCol(row, "Other test Results", r[26]);
			pdh.addCol(row, "Month 2 results", r[27]);
			pdh.addCol(row, "Month 2 date", r[28]);
			pdh.addCol(row, "Month 5 results", r[29]);
			pdh.addCol(row, "Month5date", r[30]);
			pdh.addCol(row, "Month 6 results", r[31]);
			pdh.addCol(row, "Month 6 date", r[32]);
			pdh.addCol(row, "weight", r[33]);
			pdh.addCol(row, "bmi", r[34]);
			pdh.addCol(row, "muac", r[35]);
			pdh.addCol(row, "muac code", r[36]);
			pdh.addCol(row, "z score", r[37]);
			pdh.addCol(row, "nutrition status", r[38]);
			pdh.addCol(row, "nutrition support", r[39]);
			pdh.addCol(row, "INR No", r[40]);
			pdh.addCol(row, "Transfer_From", r[41]);
			pdh.addCol(row, "Transfer_Tb_No", null);
			pdh.addCol(row, "Referral_From", r[43]);
			pdh.addCol(row, "Referral_Date", r[44]);

			if(results1 !=null){
				List<Object[]> objectList= results1.stream().filter(Object->Object[0].equals(r[0])).collect(Collectors.toList());
				Object[] enc = objectList.get(0);
			if(enc!=null) {
				pdh.addCol(row, "hiv status", enc[1]);
				pdh.addCol(row, "hiv status date",null);
				pdh.addCol(row, "cpt date", enc[3]);
				pdh.addCol(row, "cpt status", enc[4]);
				pdh.addCol(row, "ART status", enc[5]);
				pdh.addCol(row, "ART start Date", enc[6]);
				pdh.addCol(row, "ART No", enc[7]);
				pdh.addCol(row, "Treatment Model", enc[8]);
				pdh.addCol(row, "Week 1 date",enc[9]);
				pdh.addCol(row, "Week 1 days", enc[10]);
				pdh.addCol(row, "Week 3 date", enc[11]);
				pdh.addCol(row, "Week 3 days", enc[12]);
				pdh.addCol(row, "Week 5 date", enc[13]);
				pdh.addCol(row, "Week 5 days", enc[14]);
				pdh.addCol(row, "Week 7 date", enc[15]);
				pdh.addCol(row, "Week 7 days", enc[16]);
				pdh.addCol(row, "Month 3 date", enc[17]);
				pdh.addCol(row, "Month 3 days", enc[18]);
				pdh.addCol(row, "Month 4 date", enc[19]);
				pdh.addCol(row, "Month 4 days", enc[20]);
				pdh.addCol(row, "Month 5 date", enc[21]);
				pdh.addCol(row, "Month 5 days", enc[22]);
				pdh.addCol(row, "Month 6 date", enc[23]);
				pdh.addCol(row, "Month 6 days", enc[24]);
				pdh.addCol(row, "Month 7 date", enc[25]);
				pdh.addCol(row, "Month 7 days", enc[26]);
				pdh.addCol(row, "Month 8 date", enc[27]);
				pdh.addCol(row, "Month 8 days", enc[28]);
				pdh.addCol(row, "Month 9 date", enc[29]);
				pdh.addCol(row, "Month 9 days", enc[30]);
				pdh.addCol(row, "Month 10 date", enc[31]);
				pdh.addCol(row, "Month 10 days", enc[32]);
				pdh.addCol(row, "Month 11 date", enc[33]);
				pdh.addCol(row, "Month 11 days", enc[34]);
				pdh.addCol(row, "Month 12 date", enc[35]);
				pdh.addCol(row, "Month 12 days", enc[36]);
				pdh.addCol(row, "Transfer out date", enc[37]);
				pdh.addCol(row, "Transfer out Facility", enc[38]);
				pdh.addCol(row, "Transfer out District", enc[39]);
				pdh.addCol(row, "Transfer out Tel", enc[40]);
				pdh.addCol(row, "outcome results", enc[41]);
				pdh.addCol(row, "outcome date", enc[42]);
				pdh.addCol(row, "DR diagnosed", enc[43]);
				pdh.addCol(row, "DR diagnosed date", enc[44]);
			if(enc[45] !=null &&Integer.valueOf(String.valueOf(enc[45]))==1){
				pdh.addCol(row, "startedOn2ndLine", 'Y');
			}else{
				pdh.addCol(row, "startedOn2ndLine", "");
			}
				pdh.addCol(row, "startedOn2ndLine date", enc[46]);
				pdh.addCol(row, "DRTBNO", enc[47]);


			}
			}
			dataSet.addRow(row);
		}


		return dataSet;
		
	}
	
}
