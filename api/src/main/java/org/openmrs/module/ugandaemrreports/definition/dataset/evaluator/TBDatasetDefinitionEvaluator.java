package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import java.util.List;

import org.apache.log4j.Logger;
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
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.TBDatasetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

@Handler(supports = { TBDatasetDefinition.class })
public class TBDatasetDefinitionEvaluator implements DataSetEvaluator {
	
	private static final Integer TB_TREATMENT_PERIOD = -6;

	@Autowired
	private EvaluationService evaluationService;
	
	@Override
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
		SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
		TBDatasetDefinition definition = (TBDatasetDefinition) dataSetDefinition;
				
		String date = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
		
		LocalDate workingDate = StubDate.dateOf(date);
		LocalDate lastDateOfWorkingMonth = workingDate.dayOfMonth().withMaximumValue();		
		
		context = ObjectUtil.nvl(context, new EvaluationContext());	

		String summarySql = "SELECT \r\n" +
				"\tUNIQUE_TB_NO.identifier \tAS unitTbNo,\r\n" +
				"\tSUBDIST_TB_NO.identifier\tAS hsdNo,\r\n" +
				"\tDIST_TB_NO.identifier\tAS distTbNo,\r\n" +
				"\tCONCAT(PN.family_name, ' ', PN.given_name, '\\r\\n', IFNULL(CONTACT.value_text, ''), ' ', IFNULL(PAT.value,''))\tAS patientAndContact,\r\n" +
				"\tCONCAT(CASE HEALTH_WORKER.value_coded \r\n" +
				"\tWHEN 1065 THEN 'Y'\r\n" +
				"\tWHEN 1066 THEN 'N'\r\n" +
				"\tELSE ''\r\n" +
				"\tEND,\r\n" +
				"\tIFNULL(CADRE_HEALTH_WORKER.name,''))\tAS healthWorker,\r\n" +
				"\tP.gender\tAS sex,\r\n" +
				"\tYEAR(A.encounter_datetime) - YEAR(P.birthdate) - (RIGHT(A.encounter_datetime, 5) < RIGHT(P.birthdate, 5)) \tAS age,\r\n" +
				"\tCONCAT('District: ', IFNULL(PA.county_district,''), '\\r\\n',\r\n" +
				"\t'County: ', IFNULL(PA.state_province,''), '\\r\\n', \r\n" +
				"\t'Sub-county: ', IFNULL(PA.address3,''), '\\r\\n', \r\n" +
				"\t'Parish: ',   IFNULL(PA.address4,''), '\\r\\n', \r\n" +
				"\t'Village: ', IFNULL(PA.address5,''))\tAS address,\r\n" +
				"\tCONCAT(DATE_FORMAT(A.encounter_datetime,'%d/%m/%Y'),'\\n', IFNULL(REG.name,''))\tAS dateTreatmentStartedAndRegimen,\r\n" +
				"\tCASE DISEASE_CLASS.value_coded \r\n" +
				"\tWHEN 113489 THEN 'P-BC'\r\n" +
				"\tWHEN 113491 THEN 'P-CD'\r\n" +
				"\tWHEN 5042 THEN 'EP'\r\n" +
				"\tELSE ''\r\n" +
				"\tEND\tAS diseaseClass,\r\n" +
				"\tIFNULL(TYPE_OF_PATIENT.name,'')\tAS typeOfPatient,\r\n" +
				"\tCONCAT(IFNULL(TRANSFER_IN_FROM.value_text,''),'\\n', IFNULL(TRANSFER_IN_TB_NO.identifier,''))\tAS transferIn,\r\n" +
				"\tCONCAT(IFNULL(SPUTUM_SMEAR_RESULTS.name,''),'\\n', IFNULL(DATE_FORMAT(SPUTUM_SMEAR_DATE.value_datetime,'%d/%m/%Y'),\"\"))\tAS sputumSmearResults,\r\n" +
				"\tCASE COUNSELLING_TESTING.value_coded \r\n" +
				"\tWHEN 99294 THEN 'C'\r\n" +
				"\tWHEN 99406 THEN 'CT'\r\n" +
				"\tWHEN 99407 THEN 'CT1'\r\n" +
				"\tWHEN 99408 THEN 'CT2'\r\n" +
				"\tELSE ''\r\n" +
				"\tEND\tAS hivTest,\r\n" +
				"\tCASE RECEIVED_RESULTS.value_coded \r\n" +
				"\tWHEN 1065 THEN 'Y'\r\n" +
				"\tWHEN 1066 THEN 'N'\r\n" +
				"\tELSE ''\r\n" +
				"\tEND\tAS patientReceivedHivResults,\r\n" +
				"\tCONCAT(CASE ON_CPT.value_coded \r\n" +
				"\tWHEN 1065 THEN 'Y'\r\n" +
				"\tWHEN 1066 THEN 'N'\r\n" +
				"\tELSE ''\r\n" +
				"\tEND, '\\n',\r\n" +
				"\tIFNULL(DATE_FORMAT(CPT_DATE.value_datetime,'%d/%m/%Y'),''))\tAS cpt,\r\n" +
				"\tCONCAT(CASE ON_ART.value_coded \r\n" +
				"\tWHEN 1065 THEN 'Y'\r\n" +
				"\tWHEN 1066 THEN 'N'\r\n" +
				"\tELSE ''\r\n" +
				"\tEND, '\\n',\r\n" +
				"\tIFNULL(ART_NO.value_text, ''), '\\n',\r\n" +
				"\tIFNULL(DATE_FORMAT(ART_DATE.value_datetime,'%d/%m/%Y'),''))\tAS artAndArtNo,\r\n" +
				"\tCONCAT(IFNULL(TOTAL_CONTACT_LT_5.value_numeric,''),'\\n', IFNULL(TOTAL_CONTACT_LT_5_IPT.value_numeric,''))\tAS contact5Years,\r\n" +
				"\tCONCAT(CASE TREATMENT_MODEL.value_coded \r\n" +
				"\tWHEN 99416 THEN 'F'\r\n" +
				"\tWHEN 99417 THEN 'C'\r\n" +
				"\tELSE ''\r\n" +
				"\tEND, '\\n',\r\n" +
				"\tIFNULL(DATE_FORMAT(DOTS_DATE.value_datetime,'%d/%m/%Y'),''), '\\n',\r\n" +
				"\tIFNULL(TREATMENT_SUPPORTER.value_text, ''))\tAS treatmentModelAndNameOfTreatmentSupporter,\r\n" +
				"\tIFNULL(TREATMENT_OUTCOME.value_coded, -1)\tAS treatmentOutcome,\r\n" +
				"\tIFNULL(TRANSFER_OUT_UNIT.value_text, '')\tAS transferOutUnit,\r\n" +
				"\tIFNULL(DATE_FORMAT(TREATMENT_OUTCOME_DATE.value_datetime,'%d/%m/%Y'),'')\tAS treatmentOutcomeDate,\r\n" +
				"\tCASE DIAGNOSED_DR_TB.value_coded \r\n" +
				"\tWHEN 1065 THEN 'Y'\r\n" +
				"\tWHEN 1066 THEN 'N'\r\n" +
				"\tELSE ''\r\n" +
				"\tEND\tAS diagnosedWithDrTb,\r\n" +
				"\tIFNULL(REMARKS.value_text, '')\tAS remarks\t\r\n" +
				"FROM\r\n" +
				"(SELECT\r\n" +
				"     e.encounter_id,\r\n" +
				"     e.patient_id,\r\n" +
				"     e.encounter_datetime\r\n" +
				"   FROM encounter e\r\n" +
				"   INNER JOIN encounter_type et ON et.encounter_type_id = e.encounter_type AND (et.uuid = '334bf97e-28e2-4a27-8727-a5ce31c7cd66')\r\n" +
				String.format("	WHERE e.encounter_datetime <= '%s' AND e.encounter_datetime >= DATE_ADD('%s', INTERVAL %d MONTH) AND e.voided = 0\r\n", lastDateOfWorkingMonth.toString("yyyy-MM-dd"),lastDateOfWorkingMonth.toString("yyyy-MM-dd"),TB_TREATMENT_PERIOD) +					
				"   ORDER BY e.encounter_datetime) A \r\n" +
				"  INNER JOIN person P\r\n" +
				"   ON (P.person_id = A.patient_id)\r\n" +
				"  LEFT JOIN person_name PN ON (P.person_id = PN.person_id)\r\n" +
				"  LEFT JOIN (\r\n" +
				"\tSELECT \r\n" +
				"\tPI.patient_id,PI.identifier \r\n" +
				"\tFROM patient_identifier PI \r\n" +
				"\tINNER JOIN patient_identifier_type PIT ON PI.identifier_type = PIT.patient_identifier_type_id AND PI.voided = 0 AND PIT.uuid='8fd5e225-f91a-44af-ba04-3b41428d2164') UNIQUE_TB_NO ON P.person_id = UNIQUE_TB_NO.patient_id\r\n" +
				"  LEFT JOIN (\r\n" +
				"\tSELECT \r\n" +
				"\tPI.patient_id,PI.identifier \r\n" +
				"\tFROM patient_identifier PI \r\n" +
				"\tINNER JOIN patient_identifier_type PIT ON PI.identifier_type = PIT.patient_identifier_type_id AND PI.voided = 0 AND PIT.uuid='2a6f1f82-2b70-4a51-8507-3a849bc637c3') SUBDIST_TB_NO ON P.person_id = SUBDIST_TB_NO.patient_id\r\n" +
				"  LEFT JOIN (\r\n" +
				"\tSELECT \r\n" +
				"\tPI.patient_id,PI.identifier \r\n" +
				"\tFROM patient_identifier PI \r\n" +
				"\tINNER JOIN patient_identifier_type PIT ON PI.identifier_type = PIT.patient_identifier_type_id AND PI.voided = 0 AND PIT.uuid='8110f2d2-1f98-4c38-aef3-11b19bb0a589') DIST_TB_NO ON P.person_id = DIST_TB_NO.patient_id\r\n" +
				"  LEFT JOIN person_attribute PAT ON (P.person_id = PAT.person_id AND PAT.person_attribute_type_id = 8 AND PAT.voided = 0)\r\n" +
				"  LEFT JOIN obs CONTACT ON CONTACT.encounter_id = A.encounter_id AND CONTACT.concept_id = 163258 AND CONTACT.voided = 0 \r\n" +
				"  LEFT JOIN obs HEALTH_WORKER ON HEALTH_WORKER.encounter_id = A.encounter_id AND HEALTH_WORKER.concept_id = 5619 AND HEALTH_WORKER.voided = 0 \r\n" +
				"  LEFT JOIN (SELECT \r\n" +
				"\to.encounter_id, cn.name\r\n" +
				"\t     FROM obs o \r\n" +
				"\t     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 1783 AND o.voided = 0\r\n" +
				"\t     GROUP BY o.encounter_id ) CADRE_HEALTH_WORKER ON CADRE_HEALTH_WORKER.encounter_id = A.encounter_id  \r\n" +
				"  LEFT JOIN person_address PA ON (P.person_id = PA.person_id AND PA.preferred = 1 AND PA.voided = 0)\r\n" +
				"  LEFT JOIN (SELECT \r\n" +
				"\to.encounter_id, cn.name\r\n" +
				"\t     FROM obs o \r\n" +
				"\t     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 99374 AND o.voided = 0\r\n" +
				"\t     GROUP BY o.encounter_id) REG ON REG.encounter_id = A.encounter_id\r\n" +
				"  LEFT JOIN obs DISEASE_CLASS ON DISEASE_CLASS.encounter_id = A.encounter_id AND DISEASE_CLASS.concept_id = 99379 AND DISEASE_CLASS.voided = 0 \r\n" +
				"  LEFT JOIN (SELECT \r\n" +
				"\to.encounter_id, cn.name\r\n" +
				"\t     FROM obs o \r\n" +
				"\t     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 99386 AND o.voided = 0\r\n" +
				"\t     GROUP BY o.encounter_id ) TYPE_OF_PATIENT ON TYPE_OF_PATIENT.encounter_id = A.encounter_id \r\n" +
				" LEFT JOIN obs COUNSELLING_TESTING ON COUNSELLING_TESTING.encounter_id = A.encounter_id AND COUNSELLING_TESTING.concept_id = 99409 AND COUNSELLING_TESTING.voided = 0 \r\n" +
				" LEFT JOIN obs RECEIVED_RESULTS ON RECEIVED_RESULTS.encounter_id = A.encounter_id AND RECEIVED_RESULTS.concept_id = 99411 AND RECEIVED_RESULTS.voided = 0 \r\n" +
				" LEFT JOIN obs ON_CPT ON ON_CPT.encounter_id = A.encounter_id AND ON_CPT.concept_id = 160434 AND ON_CPT.voided = 0 \r\n" +
				" LEFT JOIN obs CPT_DATE ON CPT_DATE.encounter_id = A.encounter_id AND CPT_DATE.concept_id = 164361 AND CPT_DATE.voided = 0 \r\n" +
				" LEFT JOIN obs ON_ART ON ON_ART.encounter_id = A.encounter_id AND ON_ART.concept_id = 159991 AND ON_ART.voided = 0 \r\n" +
				" LEFT JOIN obs ART_DATE ON ART_DATE.encounter_id = A.encounter_id AND ART_DATE.concept_id = 99161 AND ART_DATE.voided = 0 \r\n" +
				" LEFT JOIN obs ART_NO ON ART_NO.encounter_id = A.encounter_id AND ART_NO.concept_id = 99431 AND ART_NO.voided = 0 \r\n" +
				" LEFT JOIN obs TREATMENT_OUTCOME ON TREATMENT_OUTCOME.encounter_id = A.encounter_id AND TREATMENT_OUTCOME.concept_id = 99423 AND TREATMENT_OUTCOME.voided = 0 \r\n" +
				" LEFT JOIN obs REMARKS ON REMARKS.encounter_id = A.encounter_id AND REMARKS.concept_id = 159395 AND REMARKS.voided = 0 \r\n" +
				" LEFT JOIN obs TRANSFER_IN_FROM ON TRANSFER_IN_FROM.encounter_id = A.encounter_id AND TRANSFER_IN_FROM.concept_id = 99109 AND TRANSFER_IN_FROM.voided = 0 \r\n" +
				" LEFT JOIN (\r\n" +
				"\tSELECT \r\n" +
				"\tPI.patient_id,PI.identifier \r\n" +
				"\tFROM patient_identifier PI \r\n" +
				"\tINNER JOIN patient_identifier_type PIT ON PI.identifier_type = PIT.patient_identifier_type_id AND PI.voided = 0 AND PIT.uuid='1d2be2a3-7d90-42a6-aasa5-a04b684a365b') TRANSFER_IN_TB_NO ON P.person_id = TRANSFER_IN_TB_NO.patient_id\r\n" +
				" LEFT JOIN (SELECT \r\n" +
				"\to.encounter_id, cn.name\r\n" +
				"\t     FROM obs o \r\n" +
				"\t     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 99030 AND o.voided = 0\r\n" +
				"\t     GROUP BY o.encounter_id ) MUAC ON MUAC.encounter_id = A.encounter_id\r\n" +
				" LEFT JOIN obs W4A_Z_SCORE ON W4A_Z_SCORE.encounter_id = A.encounter_id AND W4A_Z_SCORE.concept_id = 1854 AND W4A_Z_SCORE.voided = 0 \r\n" +
				" LEFT JOIN obs H4A_Z_SCORE ON H4A_Z_SCORE.encounter_id = A.encounter_id AND H4A_Z_SCORE.concept_id = 164088 AND H4A_Z_SCORE.voided = 0 \r\n" +
				" LEFT JOIN (\r\n" +
				"\tSELECT \r\n" +
				"\tPI.patient_id,PI.identifier \r\n" +
				"\tFROM patient_identifier PI \r\n" +
				"\tINNER JOIN patient_identifier_type PIT ON PI.identifier_type = PIT.patient_identifier_type_id AND PI.voided = 0 AND PIT.uuid='d4b21726-e908-4b1a-abab-b5f87cd01c18') INR_NO ON P.person_id = INR_NO.patient_id \r\n" +
				" LEFT JOIN obs TOTAL_CONTACT_LT_5 ON TOTAL_CONTACT_LT_5.encounter_id = A.encounter_id AND TOTAL_CONTACT_LT_5.concept_id = 164419 AND TOTAL_CONTACT_LT_5.voided = 0 \r\n" +
				" LEFT JOIN obs TOTAL_CONTACT_LT_5_IPT ON TOTAL_CONTACT_LT_5_IPT.encounter_id = A.encounter_id AND TOTAL_CONTACT_LT_5_IPT.concept_id = 164421 AND TOTAL_CONTACT_LT_5_IPT.voided = 0 \r\n" +
				" LEFT JOIN obs TREATMENT_MODEL ON TREATMENT_MODEL.encounter_id = A.encounter_id AND TREATMENT_MODEL.concept_id = 99418 AND TREATMENT_MODEL.voided = 0 \r\n" +
				" LEFT JOIN obs DOTS_DATE ON DOTS_DATE.encounter_id = A.encounter_id AND DOTS_DATE.concept_id = 90217 AND DOTS_DATE.voided = 0 \r\n" +
				" LEFT JOIN obs TREATMENT_SUPPORTER ON TREATMENT_SUPPORTER.encounter_id = A.encounter_id AND TREATMENT_SUPPORTER.concept_id = 99142 AND TREATMENT_SUPPORTER.voided = 0 \r\n" +
				" LEFT JOIN obs TREATMENT_PHASE ON TREATMENT_PHASE.encounter_id = A.encounter_id AND TREATMENT_PHASE.concept_id = 159792 AND TREATMENT_PHASE.voided = 0 \r\n" +
				" LEFT JOIN obs DIAGNOSED_DR_TB ON DIAGNOSED_DR_TB.encounter_id = A.encounter_id AND DIAGNOSED_DR_TB.concept_id = 90211 AND DIAGNOSED_DR_TB.voided = 0\r\n" +
				" LEFT JOIN (SELECT \r\n" +
				"\to.encounter_id, cn.name\r\n" +
				"\t     FROM obs o \r\n" +
				"\t     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 307 AND o.voided = 0\r\n" +
				"\t     GROUP BY o.encounter_id ) SPUTUM_SMEAR_RESULTS ON SPUTUM_SMEAR_RESULTS.encounter_id = A.encounter_id \r\n" +
				" LEFT JOIN obs SPUTUM_SMEAR_DATE ON SPUTUM_SMEAR_DATE.encounter_id = A.encounter_id AND SPUTUM_SMEAR_DATE.concept_id = 99392 AND SPUTUM_SMEAR_DATE.voided = 0\r\n" +
				" LEFT JOIN obs TREATMENT_OUTCOME_DATE ON TREATMENT_OUTCOME_DATE.encounter_id = A.encounter_id AND TREATMENT_OUTCOME_DATE.concept_id = 159787 AND TREATMENT_OUTCOME_DATE.voided = 0\r\n" +
				" LEFT JOIN obs TRANSFER_OUT_UNIT ON TRANSFER_OUT_UNIT.encounter_id = A.encounter_id AND TRANSFER_OUT_UNIT.concept_id = 90211 AND TRANSFER_OUT_UNIT.voided = 0\r\n" +
				" GROUP BY A.patient_id\r\n";
		
		String followupSql = "SELECT \r\n" +
				"\tGROUP_CONCAT(CONCAT(IFNULL(SPUTUM_SMEAR_RESULTS.name,''),'\\n', IFNULL(DATE_FORMAT(SPUTUM_SMEAR_DATE.value_datetime,'%d/%m/%Y'),\"\")))\tAS followUpSputumSmearResults,\r\n" +
				"\tCONCAT(IFNULL(DST_RESULTS.name,''),'\\n', IFNULL(DATE_FORMAT(DST_DATE.value_datetime,'%d/%m/%Y'),\"\"))\tAS dstResults,\r\n" +
				"\tCONCAT(IFNULL(MUAC.name,''),'\\n',\r\n" +
				"\tCASE W4A_Z_SCORE.value_coded\r\n" +
				"\tWHEN 115 THEN 'N'\r\n" +
				"\tWHEN 99271 THEN 'MAM' \r\n" +
				"\tWHEN 99272 THEN  'SAM'\r\n" +
				"\tELSE ''\r\n" +
				"\tEND, '\\n',\r\n" +
				"\tCASE H4A_Z_SCORE.value_coded\r\n" +
				"\tWHEN 115 THEN 'N'\r\n" +
				"\tWHEN 164085 THEN 'S' \r\n" +
				"\tELSE ''\r\n" +
				"\tEND, '\\n',\r\n" +
				"\tIFNULL(INR_NO.identifier,\"\"))\tAS zScoreAndInrNo,\r\n" +
				"\tGROUP_CONCAT(CASE TREATMENT_PHASE.value_coded \r\n" +
				"\tWHEN 159794 THEN 'Y'\r\n" +
				"\tELSE ''\r\n" +
				"\tEND) \tAS intensive,\r\n" +
				"\tGROUP_CONCAT(CASE TREATMENT_PHASE.value_coded \r\n" +
				"\tWHEN 159795 THEN 'Y'\r\n" +
				"\tELSE ''\r\n" +
				"\tEND)\tAS continous,\r\n" +
				"\tUNIQUE_TB_NO.identifier \tAS unitTbNo,\r\n" +
				"\tA.encounter_id\tAS encounterId\r\n" +
				"FROM\r\n" +
				"(SELECT\r\n" +
				"     e.encounter_id,\r\n" +
				"     e.patient_id,\r\n" +
				"     e.encounter_datetime\r\n" +
				"   FROM encounter e\r\n" +
				"   INNER JOIN encounter_type et ON et.encounter_type_id = e.encounter_type AND et.uuid = '455bad1f-5e97-4ee9-9558-ff1df8808732'\r\n" +
				String.format("	WHERE e.encounter_datetime <= '%s' AND e.encounter_datetime >= DATE_ADD('%s', INTERVAL %d MONTH) AND e.voided = 0) A \r\n", lastDateOfWorkingMonth.toString("yyyy-MM-dd"), lastDateOfWorkingMonth.toString("yyyy-MM-dd"), TB_TREATMENT_PERIOD) +				
				"  INNER JOIN person P\r\n" +
				"   ON (P.person_id = A.patient_id)\r\n" +
				"  LEFT JOIN person_name PN ON (P.person_id = PN.person_id)\r\n" +
				"  LEFT JOIN (\r\n" +
				"\tSELECT \r\n" +
				"\tPI.patient_id,PI.identifier \r\n" +
				"\tFROM patient_identifier PI \r\n" +
				"\tINNER JOIN patient_identifier_type PIT ON PI.identifier_type = PIT.patient_identifier_type_id AND PI.voided = 0 AND PIT.uuid='8fd5e225-f91a-44af-ba04-3b41428d2164') UNIQUE_TB_NO ON P.person_id = UNIQUE_TB_NO.patient_id\r\n" +
				" LEFT JOIN (SELECT \r\n" +
				"\to.encounter_id, cn.name\r\n" +
				"\t     FROM obs o \r\n" +
				"\t     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 99030 AND o.voided = 0\r\n" +
				"\t     GROUP BY o.encounter_id ) MUAC ON MUAC.encounter_id = A.encounter_id\r\n" +
				" LEFT JOIN obs W4A_Z_SCORE ON W4A_Z_SCORE.encounter_id = A.encounter_id AND W4A_Z_SCORE.concept_id = 1854 AND W4A_Z_SCORE.voided = 0 \r\n" +
				" LEFT JOIN obs H4A_Z_SCORE ON H4A_Z_SCORE.encounter_id = A.encounter_id AND H4A_Z_SCORE.concept_id = 164088 AND H4A_Z_SCORE.voided = 0 \r\n" +
				" LEFT JOIN (\r\n" +
				"\tSELECT \r\n" +
				"\tPI.patient_id,PI.identifier \r\n" +
				"\tFROM patient_identifier PI \r\n" +
				"\tINNER JOIN patient_identifier_type PIT ON PI.identifier_type = PIT.patient_identifier_type_id AND PI.voided = 0 AND PIT.uuid='d4b21726-e908-4b1a-abab-b5f87cd01c18') INR_NO ON P.person_id = INR_NO.patient_id \r\n" +
				" LEFT JOIN obs TREATMENT_PHASE ON TREATMENT_PHASE.encounter_id = A.encounter_id AND TREATMENT_PHASE.concept_id = 159792 AND TREATMENT_PHASE.voided = 0 \r\n" +
				" LEFT JOIN (SELECT \r\n" +
				"\to.encounter_id, cn.name\r\n" +
				"\t     FROM obs o \r\n" +
				"\t     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 159984 AND o.voided = 0\r\n" +
				"\t     GROUP BY o.encounter_id ) DST_RESULTS ON DST_RESULTS.encounter_id = A.encounter_id \r\n" +
				" LEFT JOIN obs DST_DATE ON DST_DATE.encounter_id = A.encounter_id AND DST_DATE.concept_id = 164396 AND DST_DATE.voided = 0 \r\n" +
				" LEFT JOIN (SELECT \r\n" +
				"\to.encounter_id, cn.name\r\n" +
				"\t     FROM obs o \r\n" +
				"\t     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 307 AND o.voided = 0\r\n" +
				"\t     GROUP BY o.encounter_id ) SPUTUM_SMEAR_RESULTS ON SPUTUM_SMEAR_RESULTS.encounter_id = A.encounter_id \r\n" +
				" LEFT JOIN obs SPUTUM_SMEAR_DATE ON SPUTUM_SMEAR_DATE.encounter_id = A.encounter_id AND SPUTUM_SMEAR_DATE.concept_id = 99392 AND SPUTUM_SMEAR_DATE.voided = 0\r\n" +
				" GROUP BY UNIQUE_TB_NO.identifier\r\n";
		
		//Logger.getLogger(TBDatasetDefinition.class).info(summarySql);
		SqlQueryBuilder q = new SqlQueryBuilder("SELECT TB_SUMMARY.*,TB_FOLLOWUP.* FROM (");
		q.append(summarySql);
		q.append(") AS TB_SUMMARY  LEFT JOIN (");
		q.append(followupSql);
		q.append(" ) AS TB_FOLLOWUP ON TB_SUMMARY.unitTbNo = TB_FOLLOWUP.unitTbNo");
		
		List<Object[]> results = evaluationService.evaluateToList(q, context);
		
		PatientDataHelper pdh = new PatientDataHelper();
				
		for (Object[] r : results) {
			DataSetRow row = new DataSetRow();

			pdh.addCol(row, "Unit TB No", r[0]);
			pdh.addCol(row, "HSD No", r[1]);
			pdh.addCol(row, "Dist TB No", r[2]);
			pdh.addCol(row, "Contact, Patient, Telephone", r[3]);
			pdh.addCol(row, "Health Worker", r[4]);
			pdh.addCol(row, "Sex", r[5]);
			pdh.addCol(row, "Age", r[6]);
			pdh.addCol(row, "Address", r[7]);
			pdh.addCol(row, "Date Treatment Started And Regimen", r[8]);
			pdh.addCol(row, "Disease Class", r[9]);
			pdh.addCol(row, "Type of Patient", r[10]);
			pdh.addCol(row, "Transfer In", r[11]);
			pdh.addCol(row, "Sputum Smear Results", r[12]);
			pdh.addCol(row, "HIV Test", r[13]);
			pdh.addCol(row, "Patient Received HIV Results", r[14]);
			pdh.addCol(row, "CPT", r[15]);
			pdh.addCol(row, "ART and ART No", r[16]);
			pdh.addCol(row, "Contact <5 Years", r[17]);
			pdh.addCol(row, "Treatment model and Name of treatment supporter", r[18]);
			
			pdh.addCol(row, "Cured", "");
			pdh.addCol(row, "Completed", "");
			pdh.addCol(row, "Failure", "");
			pdh.addCol(row, "Died", "");
			pdh.addCol(row, "Lost to Follow Up", "");
			pdh.addCol(row, "Transferred Out", "");
			
			Integer treatmentOutcome = Integer.valueOf(r[19].toString());
			switch (treatmentOutcome) {
				case 159791:
					pdh.addCol(row, "Cured", "Y");
					break;

				case 99421:
					pdh.addCol(row, "Completed", "Y");
					break;

				case 159874:
					pdh.addCol(row, "Failure", "Y");				
					break;

				case 1366:
					pdh.addCol(row, "Died", "Y");				
					break;

				case 5240:
					pdh.addCol(row, "Lost to Follow Up", "Y");				
					break;
					
				case 90306:
					String lostToFollowUp = "Y";
					String transferOutUnit = (String) r[20];
					String treatmentOutcomeDate = (String) r[21];
					String transferredOut = lostToFollowUp + "\r\n"  
											+ transferOutUnit + "\r\n" 
											+ treatmentOutcomeDate;
					pdh.addCol(row, "Transferred Out", transferredOut);					
					break;
					
				default:
					break;
			}
			
			pdh.addCol(row, "Diagnosed with DR TB", r[22]);
			pdh.addCol(row, "Remarks", r[23]);
			pdh.addCol(row, "Other Results", "");						

			pdh.addCol(row, "Follow up 2 Results", "");						
			pdh.addCol(row, "Follow up 3 Results", "");						
			pdh.addCol(row, "Follow up 5 Results", "");						
			pdh.addCol(row, "Follow up 8 Results", "");						
			
			String followUpSputumSmearResults = r[24].toString();
			String[] followUpSputumSmearResultsSplit = followUpSputumSmearResults.split(",");
			for (int i = 0; i < followUpSputumSmearResultsSplit.length; i++) {
				switch (i) {
					case 0:
						pdh.addCol(row, "Follow up 2 Results", followUpSputumSmearResultsSplit[i]);						
						break;
					case 1:
						pdh.addCol(row, "Follow up 3 Results", followUpSputumSmearResultsSplit[i]);						
						break;
					case 2:
						pdh.addCol(row, "Follow up 5 Results", followUpSputumSmearResultsSplit[i]);						
						break;
					case 3:
						pdh.addCol(row, "Follow up 8 Results", followUpSputumSmearResultsSplit[i]);						
						break;
					
					default:
						break;
				}				
			}
			
			pdh.addCol(row, "DST Results", r[25]);
			pdh.addCol(row, "Z Score and INR No", r[26]);

			pdh.addCol(row, "Intensive 1", "");						
			pdh.addCol(row, "Intensive 2", "");						
			pdh.addCol(row, "Intensive 3", "");						
			pdh.addCol(row, "Intensive 4", "");
						
			String intensivePhase = r[27].toString();
			String[] intensivePhaseSplit = intensivePhase.split(",");
			for (int i = 0; i < intensivePhaseSplit.length; i++) {
				switch (i) {
					case 0:
						pdh.addCol(row, "Intensive 1", intensivePhaseSplit[i]);						
						break;
					case 1:
						pdh.addCol(row, "Intensive 2", intensivePhaseSplit[i]);						
						break;
					case 2:
						pdh.addCol(row, "Intensive 3", intensivePhaseSplit[i]);						
						break;
					case 3:
						pdh.addCol(row, "Intensive 4", intensivePhaseSplit[i]);						
						break;
					
					default:
						break;
				}
			}

			pdh.addCol(row, "Continous 3", "");
			pdh.addCol(row, "Continous 4", "");
			pdh.addCol(row, "Continous 5", "");
			pdh.addCol(row, "Continous 6", "");
			pdh.addCol(row, "Continous 7", "");
			pdh.addCol(row, "Continous 8", "");
			
			String continousPhase = r[28].toString();
			String[] continousPhaseSplit = continousPhase.split(",");
			for (int i = 0; i < continousPhaseSplit.length; i++) {
				switch (i) {
					case 0:
						pdh.addCol(row, "Continous 3", continousPhaseSplit[i]);						
						break;
					case 1:
						pdh.addCol(row, "Continous 4", continousPhaseSplit[i]);						
						break;
					case 2:
						pdh.addCol(row, "Continous 5", continousPhaseSplit[i]);						
						break;
					case 3:
						pdh.addCol(row, "Continous 6", continousPhaseSplit[i]);						
						break;
					case 4:
						pdh.addCol(row, "Continous 7", continousPhaseSplit[i]);						
						break;
					case 5:
						pdh.addCol(row, "Continous 8", continousPhaseSplit[i]);						
						break;
					
					default:
						break;
				}
			}
			
			dataSet.addRow(row);
		}
		return dataSet;
		
	}
	
}
