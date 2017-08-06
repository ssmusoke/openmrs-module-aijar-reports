package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import java.util.List;

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
	
	@Autowired
	private EvaluationService evaluationService;
	
	@Override
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
		SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
		TBDatasetDefinition definition = (TBDatasetDefinition) dataSetDefinition;
				
		String date = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
		
		LocalDate workingDate = StubDate.dateOf(date);
		int beginningMonth = workingDate.getMonthOfYear();
		int beginningYear = workingDate.getYear();
		
		context = ObjectUtil.nvl(context, new EvaluationContext());	

		String summarySql = "SELECT  " +
		"	IFNULL(UNIT_TB_NO.value_text,'') 										AS unitTbNo, " +
		"	IFNULL(SUBDIST_TB_NO.value_text,'')										AS hsdNo, " +
		"	IFNULL(DIST_TB_NO.value_text,'')										AS distTbNo, " +
		"	CONCAT(PN.family_name, ' ', PN.given_name, '\\r\\n', IFNULL(CONTACT.value_text, ''), ' ', IFNULL(PAT.value,''))	AS patientAndContact, " +
		"	CONCAT(CASE HEALTH_WORKER.value_coded  " +
		"			WHEN 1065 THEN 'Y' " +
		"			WHEN 1066 THEN 'N' " +
		"			ELSE '' " +
		"		END, " +
		"		IFNULL(CADRE_HEALTH_WORKER.name,''))									AS healthWorker, " +
		"	P.gender													AS sex, " +
		"	YEAR(A.encounter_datetime) - YEAR(P.birthdate) - (RIGHT(A.encounter_datetime, 5) < RIGHT(P.birthdate, 5)) 	AS age, " +
		"	CONCAT('District: ', IFNULL(PA.county_district,''), '\\r\\n', " +
		"		'County: ', IFNULL(PA.state_province,''), '\\r\\n',  " +
		"		'Sub-county: ', IFNULL(PA.address3,''), '\\r\\n',  " +
		"		'Parish: ',   IFNULL(PA.address4,''), '\\r\\n',  " +
		"		'Village: ', IFNULL(PA.address5,''))									AS address, " +
		"	CONCAT(DATE_FORMAT(A.encounter_datetime,'%d/%m/%Y'),'\\n', IFNULL(REG.name,''))					AS dateTreatmentStartedAndRegimen, " +
		"	CASE DISEASE_CLASS.value_coded  " +
		"		WHEN 113489 THEN 'P-BC' " +
		"		WHEN 113491 THEN 'P-CD' " +
		"		WHEN 5042 THEN 'EP' " +
		"		ELSE '' " +
		"	END														AS diseaseClass, " +
		"	IFNULL(TYPE_OF_PATIENT.name,'')											AS typeOfPatient, " +
		"	CONCAT(IFNULL(TRANSFER_IN_FROM.value_text,''),'\\n', IFNULL(TRANSFER_IN_TB_NO.identifier,''))			AS transferIn, " +
		"	IFNULL(SPUTUM_SMEAR_RESULTS.ss, '')										AS sputumSmearResults, " +
		"	IFNULL(DST_RESULTS.dst, '')											AS dstResults, " +
		"	CASE COUNSELLING_TESTING.value_coded  " +
		"		WHEN 99294 THEN 'C' " +
		"		WHEN 99406 THEN 'CT' " +
		"		WHEN 99407 THEN 'CT1' " +
		"		WHEN 99408 THEN 'CT2' " +
		"		ELSE '' " +
		"	END														AS hivTest, " +
		"	CASE RECEIVED_RESULTS.value_coded  " +
		"		WHEN 1065 THEN 'Y' " +
		"		WHEN 1066 THEN 'N' " +
		"		ELSE '' " +
		"	END														AS patientReceivedHivResults, " +
		"	CONCAT(CASE ON_CPT.value_coded  " +
		"			WHEN 1065 THEN 'Y' " +
		"			WHEN 1066 THEN 'N' " +
		"			ELSE '' " +
		"		END, '\\n', " +
		"		IFNULL(DATE_FORMAT(CPT_DATE.value_datetime,'%d/%m/%Y'),''))						AS cpt, " +
		"	CONCAT(CASE ON_ART.value_coded  " +
		"			WHEN 1065 THEN 'Y' " +
		"			WHEN 1066 THEN 'N' " +
		"			ELSE '' " +
		"		END, '\\n', " +
		"		IFNULL(ART_NO.value_text, ''), '\\n', " +
		"		IFNULL(DATE_FORMAT(ART_DATE.value_datetime,'%d/%m/%Y'),''))						AS artAndArtNo, " +
		"	CONCAT(IFNULL(TOTAL_CONTACT_LT_5.value_numeric,''),'\\n', IFNULL(TOTAL_CONTACT_LT_5_IPT.value_numeric,''))	AS contact5Years, " +
		"	CONCAT(CASE TREATMENT_MODEL.value_coded  " +
		"			WHEN 99416 THEN 'F' " +
		"			WHEN 99417 THEN 'C' " +
		"			ELSE '' " +
		"		END, '\\n', " +
		"		IFNULL(DATE_FORMAT(DOTS_DATE.value_datetime,'%d/%m/%Y'),''), '\\n', " +
		"		IFNULL(TREATMENT_SUPPORTER.value_text, ''))								AS treatmentModelAndNameOfTreatmentSupporter, " +
		"	IFNULL(TREATMENT_OUTCOME.value_coded, -1)									AS treatmentOutcome, " +
		"	IFNULL(TRANSFER_OUT_UNIT.value_text, '')									AS transferOutUnit, " +
		"	IFNULL(DATE_FORMAT(TREATMENT_OUTCOME_DATE.value_datetime,'%d/%m/%Y'),'')					AS treatmentOutcomeDate, " +
		"	CASE DIAGNOSED_DR_TB.value_coded  " +
		"		WHEN 1065 THEN 'Y' " +
		"		WHEN 1066 THEN 'N' " +
		"		ELSE '' " +
		"	END														AS diagnosedWithDrTb, " +
		"	IFNULL(REMARKS.value_text, '')											AS remarks, " +
		"	IFNULL(GROUP_CONCAT(CASE TREATMENT_PHASE.value_coded  " +
		"		WHEN 159794 THEN 'Y' " +
		"		ELSE NULL " +
		"	END),'') 														AS intensive, " +
		"	IFNULL(GROUP_CONCAT(CASE TREATMENT_PHASE.value_coded  " +
		"		WHEN 159795 THEN 'Y' " +
		"		ELSE NULL " +
		"	END),'')														AS continous " +
		"	 " +
		"FROM " +
		"	(SELECT " +
		"	     e.encounter_id, " +
		"	     e.patient_id, " +
		"	     e.encounter_datetime " +
		"	   FROM encounter e " +
		"	   INNER JOIN encounter_type et ON et.encounter_type_id = e.encounter_type AND (et.uuid = '334bf97e-28e2-4a27-8727-a5ce31c7cd66') " +
		String.format(
            "   WHERE YEAR(e.encounter_datetime) = %s AND MONTH(e.encounter_datetime) = %s AND e.voided = 0 ",
            beginningYear, beginningMonth) +
		"	   ORDER BY e.encounter_datetime) A  " +
		"	  INNER JOIN person P " +
		"	   ON (P.person_id = A.patient_id) " +
		"	  LEFT JOIN person_name PN ON (P.person_id = PN.person_id) " +
		"	  LEFT JOIN obs UNIT_TB_NO ON UNIT_TB_NO.encounter_id = A.encounter_id AND UNIT_TB_NO.concept_id = 164955 AND UNIT_TB_NO.voided = 0  " +
		"	  LEFT JOIN obs SUBDIST_TB_NO ON SUBDIST_TB_NO.encounter_id = A.encounter_id AND SUBDIST_TB_NO.concept_id = 99370 AND SUBDIST_TB_NO.voided = 0  " +
		"	  LEFT JOIN obs DIST_TB_NO ON DIST_TB_NO.encounter_id = A.encounter_id AND DIST_TB_NO.concept_id = 99031 AND DIST_TB_NO.voided = 0 	   " +
		"	  LEFT JOIN person_attribute PAT ON (P.person_id = PAT.person_id AND PAT.person_attribute_type_id = 8 AND PAT.voided = 0) " +
		"	  LEFT JOIN obs CONTACT ON CONTACT.encounter_id = A.encounter_id AND CONTACT.concept_id = 163258 AND CONTACT.voided = 0  " +
		"	  LEFT JOIN obs HEALTH_WORKER ON HEALTH_WORKER.encounter_id = A.encounter_id AND HEALTH_WORKER.concept_id = 5619 AND HEALTH_WORKER.voided = 0  " +
		"	  LEFT JOIN (SELECT  " +
		"			o.encounter_id, cn.name " +
		"		     FROM obs o  " +
		"		     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 1783 AND o.voided = 0 " +
		"		     GROUP BY o.encounter_id ) CADRE_HEALTH_WORKER ON CADRE_HEALTH_WORKER.encounter_id = A.encounter_id   " +
		"	  LEFT JOIN person_address PA ON (P.person_id = PA.person_id AND PA.preferred = 1 AND PA.voided = 0) " +
		"	  LEFT JOIN (SELECT  " +
		"			o.encounter_id, cn.name " +
		"		     FROM obs o  " +
		"		     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 99374 AND o.voided = 0 " +
		"		     GROUP BY o.encounter_id) REG ON REG.encounter_id = A.encounter_id " +
		"	  LEFT JOIN obs DISEASE_CLASS ON DISEASE_CLASS.encounter_id = A.encounter_id AND DISEASE_CLASS.concept_id = 99379 AND DISEASE_CLASS.voided = 0  " +
		"	  LEFT JOIN (SELECT  " +
		"			o.encounter_id, cn.name " +
		"		     FROM obs o  " +
		"		     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 99386 AND o.voided = 0 " +
		"		     GROUP BY o.encounter_id ) TYPE_OF_PATIENT ON TYPE_OF_PATIENT.encounter_id = A.encounter_id  " +
		"	 LEFT JOIN obs COUNSELLING_TESTING ON COUNSELLING_TESTING.encounter_id = A.encounter_id AND COUNSELLING_TESTING.concept_id = 99409 AND COUNSELLING_TESTING.voided = 0  " +
		"	 LEFT JOIN obs RECEIVED_RESULTS ON RECEIVED_RESULTS.encounter_id = A.encounter_id AND RECEIVED_RESULTS.concept_id = 99411 AND RECEIVED_RESULTS.voided = 0  " +
		"	 LEFT JOIN obs ON_CPT ON ON_CPT.encounter_id = A.encounter_id AND ON_CPT.concept_id = 160434 AND ON_CPT.voided = 0  " +
		"	 LEFT JOIN obs CPT_DATE ON CPT_DATE.encounter_id = A.encounter_id AND CPT_DATE.concept_id = 164361 AND CPT_DATE.voided = 0  " +
		"	 LEFT JOIN obs ON_ART ON ON_ART.encounter_id = A.encounter_id AND ON_ART.concept_id = 159991 AND ON_ART.voided = 0  " +
		"	 LEFT JOIN obs ART_DATE ON ART_DATE.encounter_id = A.encounter_id AND ART_DATE.concept_id = 99161 AND ART_DATE.voided = 0  " +
		"	 LEFT JOIN obs ART_NO ON ART_NO.encounter_id = A.encounter_id AND ART_NO.concept_id = 99431 AND ART_NO.voided = 0  " +
		"	 LEFT JOIN obs TREATMENT_OUTCOME ON TREATMENT_OUTCOME.encounter_id = A.encounter_id AND TREATMENT_OUTCOME.concept_id = 99423 AND TREATMENT_OUTCOME.voided = 0  " +
		"	 LEFT JOIN obs REMARKS ON REMARKS.encounter_id = A.encounter_id AND REMARKS.concept_id = 159395 AND REMARKS.voided = 0  " +
		"	 LEFT JOIN obs TRANSFER_IN_FROM ON TRANSFER_IN_FROM.encounter_id = A.encounter_id AND TRANSFER_IN_FROM.concept_id = 99109 AND TRANSFER_IN_FROM.voided = 0  " +
		"	 LEFT JOIN ( " +
		"		SELECT  " +
		"			PI.patient_id,PI.identifier  " +
		"		FROM patient_identifier PI  " +
		"		INNER JOIN patient_identifier_type PIT ON PI.identifier_type = PIT.patient_identifier_type_id AND PI.voided = 0 AND PIT.uuid='1d2be2a3-7d90-42a6-aasa5-a04b684a365b') TRANSFER_IN_TB_NO ON P.person_id = TRANSFER_IN_TB_NO.patient_id " +
		"	 LEFT JOIN (SELECT  " +
		"			o.encounter_id, cn.name " +
		"		     FROM obs o  " +
		"		     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 99030 AND o.voided = 0 " +
		"		     GROUP BY o.encounter_id ) MUAC ON MUAC.encounter_id = A.encounter_id " +
		"	 LEFT JOIN obs TOTAL_CONTACT_LT_5 ON TOTAL_CONTACT_LT_5.encounter_id = A.encounter_id AND TOTAL_CONTACT_LT_5.concept_id = 164419 AND TOTAL_CONTACT_LT_5.voided = 0  " +
		"	 LEFT JOIN obs TOTAL_CONTACT_LT_5_IPT ON TOTAL_CONTACT_LT_5_IPT.encounter_id = A.encounter_id AND TOTAL_CONTACT_LT_5_IPT.concept_id = 164421 AND TOTAL_CONTACT_LT_5_IPT.voided = 0  " +
		"	 LEFT JOIN obs TREATMENT_MODEL ON TREATMENT_MODEL.encounter_id = A.encounter_id AND TREATMENT_MODEL.concept_id = 99418 AND TREATMENT_MODEL.voided = 0  " +
		"	 LEFT JOIN obs DOTS_DATE ON DOTS_DATE.encounter_id = A.encounter_id AND DOTS_DATE.concept_id = 90217 AND DOTS_DATE.voided = 0  " +
		"	 LEFT JOIN obs TREATMENT_SUPPORTER ON TREATMENT_SUPPORTER.encounter_id = A.encounter_id AND TREATMENT_SUPPORTER.concept_id = 99142 AND TREATMENT_SUPPORTER.voided = 0  " +
		"	 LEFT JOIN obs TREATMENT_PHASE ON TREATMENT_PHASE.encounter_id = A.encounter_id AND TREATMENT_PHASE.concept_id = 159792 AND TREATMENT_PHASE.voided = 0  " +
		"	  " +
		"	 LEFT JOIN obs DIAGNOSED_DR_TB ON DIAGNOSED_DR_TB.encounter_id = A.encounter_id AND DIAGNOSED_DR_TB.concept_id = 90211 AND DIAGNOSED_DR_TB.voided = 0 " +
		"	 LEFT JOIN (SELECT  " +
		"			o.encounter_id, GROUP_CONCAT(CONCAT(IFNULL(cn.name,''),'\\n', IFNULL(DATE_FORMAT(o.obs_datetime,'%d/%m/%Y'),''))) AS ss " +
		"		     FROM obs o  " +
		"		     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 307 AND o.voided = 0 " +
		"		     GROUP BY o.encounter_id ) SPUTUM_SMEAR_RESULTS ON SPUTUM_SMEAR_RESULTS.encounter_id = A.encounter_id  " +
		"	 LEFT JOIN (SELECT  " +
		"			o.encounter_id, GROUP_CONCAT(CONCAT(IFNULL(cn.name,''),'\\n', IFNULL(DATE_FORMAT(o.obs_datetime,'%d/%m/%Y'),'')) SEPARATOR '\\n') AS dst " +
		"		     FROM obs o  " +
		"		     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 159984 AND o.voided = 0 " +
		"		     GROUP BY o.encounter_id ) DST_RESULTS ON DST_RESULTS.encounter_id = A.encounter_id  " +
		"	 LEFT JOIN obs TREATMENT_OUTCOME_DATE ON TREATMENT_OUTCOME_DATE.encounter_id = A.encounter_id AND TREATMENT_OUTCOME_DATE.concept_id = 159787 AND TREATMENT_OUTCOME_DATE.voided = 0 " +
		"	 LEFT JOIN obs TRANSFER_OUT_UNIT ON TRANSFER_OUT_UNIT.encounter_id = A.encounter_id AND TRANSFER_OUT_UNIT.concept_id = 90211 AND TRANSFER_OUT_UNIT.voided = 0 " +
		"	 GROUP BY A.patient_id, A.encounter_id ";
		
		//System.out.println(summarySql);
		SqlQueryBuilder q = new SqlQueryBuilder(summarySql);
		
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

			pdh.addCol(row, "Sputum Smear Results", "");
			pdh.addCol(row, "Follow up 1 Results", "");						
			pdh.addCol(row, "Follow up 2 Results", "");						
			pdh.addCol(row, "Follow up 3 Results", "");						
			pdh.addCol(row, "Follow up 4 Results", "");						
			
			String sputumSmearResults = r[12].toString();
			String[] sputumSmearResultsSplit = sputumSmearResults.split(",");
			for (int i = 0; i < sputumSmearResultsSplit.length; i++) {
				switch (i) {
					case 0:
						pdh.addCol(row, "Sputum Smear Results", sputumSmearResultsSplit[i]);
						break;
					case 1:
						pdh.addCol(row, "Follow up 1 Results", sputumSmearResultsSplit[i]);						
						break;
					case 2:
						pdh.addCol(row, "Follow up 2 Results", sputumSmearResultsSplit[i]);						
						break;
					case 3:
						pdh.addCol(row, "Follow up 3 Results", sputumSmearResultsSplit[i]);						
						break;
					case 4:
						pdh.addCol(row, "Follow up 4 Results", sputumSmearResultsSplit[i]);						
						break;
					
					default:
						break;
				}				
			}
			
			pdh.addCol(row, "DST Results", r[13]);
			pdh.addCol(row, "HIV Test", r[14]);
			pdh.addCol(row, "Patient Received HIV Results", r[15]);
			pdh.addCol(row, "CPT", r[16]);
			pdh.addCol(row, "ART and ART No", r[17]);
			pdh.addCol(row, "Contact <5 Years", r[18]);
			pdh.addCol(row, "Treatment model and Name of treatment supporter", r[19]);
			
			pdh.addCol(row, "Cured", "");
			pdh.addCol(row, "Completed", "");
			pdh.addCol(row, "Failure", "");
			pdh.addCol(row, "Died", "");
			pdh.addCol(row, "Lost to Follow Up", "");
			pdh.addCol(row, "Transferred Out", "");
			
			Integer treatmentOutcome = Integer.valueOf(r[20].toString());
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
					String transferOutUnit = (String) r[21];
					String treatmentOutcomeDate = (String) r[22];
					String transferredOut = lostToFollowUp + "\r\n"  
											+ transferOutUnit + "\r\n" 
											+ treatmentOutcomeDate;
					pdh.addCol(row, "Transferred Out", transferredOut);					
					break;
					
				default:
					break;
			}
			
			pdh.addCol(row, "Diagnosed with DR TB", r[23]);
			pdh.addCol(row, "Remarks", r[24]);
			pdh.addCol(row, "Other Results", "");						
			
			pdh.addCol(row, "Z Score and INR No", "");

			pdh.addCol(row, "Intensive 1", "");						
			pdh.addCol(row, "Intensive 2", "");						
			pdh.addCol(row, "Intensive 3", "");						
			pdh.addCol(row, "Intensive 4", "");
						
			String intensivePhase = r[25] == null ? "" : r[25].toString();
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
			
			String continousPhase = r[26] == null ? "" : r[26].toString();
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
	
	/**
	 * The row sql
	 * ==================================================================
		SELECT 
			IFNULL(UNIT_TB_NO.value_text,'') 										AS unitTbNo,
			IFNULL(SUBDIST_TB_NO.value_text,'')										AS hsdNo,
			IFNULL(DIST_TB_NO.value_text,'')										AS distTbNo,
			CONCAT(PN.family_name, ' ', PN.given_name, '\r\n', IFNULL(CONTACT.value_text, ''), ' ', IFNULL(PAT.value,''))	AS patientAndContact,
			CONCAT(CASE HEALTH_WORKER.value_coded 
					WHEN 1065 THEN 'Y'
					WHEN 1066 THEN 'N'
					ELSE ''
				END,
				IFNULL(CADRE_HEALTH_WORKER.name,''))									AS healthWorker,
			P.gender													AS sex,
			YEAR(A.encounter_datetime) - YEAR(P.birthdate) - (RIGHT(A.encounter_datetime, 5) < RIGHT(P.birthdate, 5)) 	AS age,
			CONCAT('District: ', IFNULL(PA.county_district,''), '\r\n',
				'County: ', IFNULL(PA.state_province,''), '\r\n', 
				'Sub-county: ', IFNULL(PA.address3,''), '\r\n', 
				'Parish: ',   IFNULL(PA.address4,''), '\r\n', 
				'Village: ', IFNULL(PA.address5,''))									AS address,
			CONCAT(DATE_FORMAT(A.encounter_datetime,'%d/%m/%Y'),'\n', IFNULL(REG.name,''))					AS dateTreatmentStartedAndRegimen,
			CASE DISEASE_CLASS.value_coded 
				WHEN 113489 THEN 'P-BC'
				WHEN 113491 THEN 'P-CD'
				WHEN 5042 THEN 'EP'
				ELSE ''
			END														AS diseaseClass,
			IFNULL(TYPE_OF_PATIENT.name,'')											AS typeOfPatient,
			CONCAT(IFNULL(TRANSFER_IN_FROM.value_text,''),'\n', IFNULL(TRANSFER_IN_TB_NO.identifier,''))			AS transferIn,
			IFNULL(SPUTUM_SMEAR_RESULTS.ss, '')										AS sputumSmearResults,
			IFNULL(DST_RESULTS.dst, '')											AS dstResults,
			CASE COUNSELLING_TESTING.value_coded 
				WHEN 99294 THEN 'C'
				WHEN 99406 THEN 'CT'
				WHEN 99407 THEN 'CT1'
				WHEN 99408 THEN 'CT2'
				ELSE ''
			END														AS hivTest,
			CASE RECEIVED_RESULTS.value_coded 
				WHEN 1065 THEN 'Y'
				WHEN 1066 THEN 'N'
				ELSE ''
			END														AS patientReceivedHivResults,
			CONCAT(CASE ON_CPT.value_coded 
					WHEN 1065 THEN 'Y'
					WHEN 1066 THEN 'N'
					ELSE ''
				END, '\n',
				IFNULL(DATE_FORMAT(CPT_DATE.value_datetime,'%d/%m/%Y'),''))						AS cpt,
			CONCAT(CASE ON_ART.value_coded 
					WHEN 1065 THEN 'Y'
					WHEN 1066 THEN 'N'
					ELSE ''
				END, '\n',
				IFNULL(ART_NO.value_text, ''), '\n',
				IFNULL(DATE_FORMAT(ART_DATE.value_datetime,'%d/%m/%Y'),''))						AS artAndArtNo,
			CONCAT(IFNULL(TOTAL_CONTACT_LT_5.value_numeric,''),'\n', IFNULL(TOTAL_CONTACT_LT_5_IPT.value_numeric,''))	AS contact5Years,
			CONCAT(CASE TREATMENT_MODEL.value_coded 
					WHEN 99416 THEN 'F'
					WHEN 99417 THEN 'C'
					ELSE ''
				END, '\n',
				IFNULL(DATE_FORMAT(DOTS_DATE.value_datetime,'%d/%m/%Y'),''), '\n',
				IFNULL(TREATMENT_SUPPORTER.value_text, ''))								AS treatmentModelAndNameOfTreatmentSupporter,
			IFNULL(TREATMENT_OUTCOME.value_coded, -1)									AS treatmentOutcome,
			IFNULL(TRANSFER_OUT_UNIT.value_text, '')									AS transferOutUnit,
			IFNULL(DATE_FORMAT(TREATMENT_OUTCOME_DATE.value_datetime,'%d/%m/%Y'),'')					AS treatmentOutcomeDate,
			CASE DIAGNOSED_DR_TB.value_coded 
				WHEN 1065 THEN 'Y'
				WHEN 1066 THEN 'N'
				ELSE ''
			END														AS diagnosedWithDrTb,
			IFNULL(REMARKS.value_text, '')											AS remarks,
			IFNULL(GROUP_CONCAT(CASE TREATMENT_PHASE.value_coded 
				WHEN 159794 THEN 'Y'
				ELSE NULL
			END),'') 														AS intensive,
			IFNULL(GROUP_CONCAT(CASE TREATMENT_PHASE.value_coded 
				WHEN 159795 THEN 'Y'
				ELSE NULL
			END),'')														AS continous
			
		FROM
			(SELECT
			     e.encounter_id,
			     e.patient_id,
			     e.encounter_datetime
			   FROM encounter e
			   INNER JOIN encounter_type et ON et.encounter_type_id = e.encounter_type AND (et.uuid = '334bf97e-28e2-4a27-8727-a5ce31c7cd66')
			   WHERE YEAR(e.encounter_datetime) = 2017 AND MONTH(e.encounter_datetime) = 5 AND e.voided = 0
			   -- WHERE e.encounter_datetime <= '2017-05-31' AND e.encounter_datetime >= DATE_ADD('2017-05-31', INTERVAL -6 MONTH) AND e.voided = 0
			   ORDER BY e.encounter_datetime) A 
			  INNER JOIN person P
			   ON (P.person_id = A.patient_id)
			  LEFT JOIN person_name PN ON (P.person_id = PN.person_id)
			  LEFT JOIN obs UNIT_TB_NO ON UNIT_TB_NO.encounter_id = A.encounter_id AND UNIT_TB_NO.concept_id = 164955 AND UNIT_TB_NO.voided = 0 
			  LEFT JOIN obs SUBDIST_TB_NO ON SUBDIST_TB_NO.encounter_id = A.encounter_id AND SUBDIST_TB_NO.concept_id = 99370 AND SUBDIST_TB_NO.voided = 0 
			  LEFT JOIN obs DIST_TB_NO ON DIST_TB_NO.encounter_id = A.encounter_id AND DIST_TB_NO.concept_id = 99031 AND DIST_TB_NO.voided = 0 	  
			  LEFT JOIN person_attribute PAT ON (P.person_id = PAT.person_id AND PAT.person_attribute_type_id = 8 AND PAT.voided = 0)
			  LEFT JOIN obs CONTACT ON CONTACT.encounter_id = A.encounter_id AND CONTACT.concept_id = 163258 AND CONTACT.voided = 0 
			  LEFT JOIN obs HEALTH_WORKER ON HEALTH_WORKER.encounter_id = A.encounter_id AND HEALTH_WORKER.concept_id = 5619 AND HEALTH_WORKER.voided = 0 
			  LEFT JOIN (SELECT 
					o.encounter_id, cn.name
				     FROM obs o 
				     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 1783 AND o.voided = 0
				     GROUP BY o.encounter_id ) CADRE_HEALTH_WORKER ON CADRE_HEALTH_WORKER.encounter_id = A.encounter_id  
			  LEFT JOIN person_address PA ON (P.person_id = PA.person_id AND PA.preferred = 1 AND PA.voided = 0)
			  LEFT JOIN (SELECT 
					o.encounter_id, cn.name
				     FROM obs o 
				     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 99374 AND o.voided = 0
				     GROUP BY o.encounter_id) REG ON REG.encounter_id = A.encounter_id
			  LEFT JOIN obs DISEASE_CLASS ON DISEASE_CLASS.encounter_id = A.encounter_id AND DISEASE_CLASS.concept_id = 99379 AND DISEASE_CLASS.voided = 0 
			  LEFT JOIN (SELECT 
					o.encounter_id, cn.name
				     FROM obs o 
				     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 99386 AND o.voided = 0
				     GROUP BY o.encounter_id ) TYPE_OF_PATIENT ON TYPE_OF_PATIENT.encounter_id = A.encounter_id 
			 LEFT JOIN obs COUNSELLING_TESTING ON COUNSELLING_TESTING.encounter_id = A.encounter_id AND COUNSELLING_TESTING.concept_id = 99409 AND COUNSELLING_TESTING.voided = 0 
			 LEFT JOIN obs RECEIVED_RESULTS ON RECEIVED_RESULTS.encounter_id = A.encounter_id AND RECEIVED_RESULTS.concept_id = 99411 AND RECEIVED_RESULTS.voided = 0 
			 LEFT JOIN obs ON_CPT ON ON_CPT.encounter_id = A.encounter_id AND ON_CPT.concept_id = 160434 AND ON_CPT.voided = 0 
			 LEFT JOIN obs CPT_DATE ON CPT_DATE.encounter_id = A.encounter_id AND CPT_DATE.concept_id = 164361 AND CPT_DATE.voided = 0 
			 LEFT JOIN obs ON_ART ON ON_ART.encounter_id = A.encounter_id AND ON_ART.concept_id = 159991 AND ON_ART.voided = 0 
			 LEFT JOIN obs ART_DATE ON ART_DATE.encounter_id = A.encounter_id AND ART_DATE.concept_id = 99161 AND ART_DATE.voided = 0 
			 LEFT JOIN obs ART_NO ON ART_NO.encounter_id = A.encounter_id AND ART_NO.concept_id = 99431 AND ART_NO.voided = 0 
			 LEFT JOIN obs TREATMENT_OUTCOME ON TREATMENT_OUTCOME.encounter_id = A.encounter_id AND TREATMENT_OUTCOME.concept_id = 99423 AND TREATMENT_OUTCOME.voided = 0 
			 LEFT JOIN obs REMARKS ON REMARKS.encounter_id = A.encounter_id AND REMARKS.concept_id = 159395 AND REMARKS.voided = 0 
			 LEFT JOIN obs TRANSFER_IN_FROM ON TRANSFER_IN_FROM.encounter_id = A.encounter_id AND TRANSFER_IN_FROM.concept_id = 99109 AND TRANSFER_IN_FROM.voided = 0 
			 LEFT JOIN (
				SELECT 
					PI.patient_id,PI.identifier 
				FROM patient_identifier PI 
				INNER JOIN patient_identifier_type PIT ON PI.identifier_type = PIT.patient_identifier_type_id AND PI.voided = 0 AND PIT.uuid='1d2be2a3-7d90-42a6-aasa5-a04b684a365b') TRANSFER_IN_TB_NO ON P.person_id = TRANSFER_IN_TB_NO.patient_id
			 LEFT JOIN (SELECT 
					o.encounter_id, cn.name
				     FROM obs o 
				     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 99030 AND o.voided = 0
				     GROUP BY o.encounter_id ) MUAC ON MUAC.encounter_id = A.encounter_id
			 LEFT JOIN obs TOTAL_CONTACT_LT_5 ON TOTAL_CONTACT_LT_5.encounter_id = A.encounter_id AND TOTAL_CONTACT_LT_5.concept_id = 164419 AND TOTAL_CONTACT_LT_5.voided = 0 
			 LEFT JOIN obs TOTAL_CONTACT_LT_5_IPT ON TOTAL_CONTACT_LT_5_IPT.encounter_id = A.encounter_id AND TOTAL_CONTACT_LT_5_IPT.concept_id = 164421 AND TOTAL_CONTACT_LT_5_IPT.voided = 0 
			 LEFT JOIN obs TREATMENT_MODEL ON TREATMENT_MODEL.encounter_id = A.encounter_id AND TREATMENT_MODEL.concept_id = 99418 AND TREATMENT_MODEL.voided = 0 
			 LEFT JOIN obs DOTS_DATE ON DOTS_DATE.encounter_id = A.encounter_id AND DOTS_DATE.concept_id = 90217 AND DOTS_DATE.voided = 0 
			 LEFT JOIN obs TREATMENT_SUPPORTER ON TREATMENT_SUPPORTER.encounter_id = A.encounter_id AND TREATMENT_SUPPORTER.concept_id = 99142 AND TREATMENT_SUPPORTER.voided = 0 
			 LEFT JOIN obs TREATMENT_PHASE ON TREATMENT_PHASE.encounter_id = A.encounter_id AND TREATMENT_PHASE.concept_id = 159792 AND TREATMENT_PHASE.voided = 0 
			 
			 LEFT JOIN obs DIAGNOSED_DR_TB ON DIAGNOSED_DR_TB.encounter_id = A.encounter_id AND DIAGNOSED_DR_TB.concept_id = 90211 AND DIAGNOSED_DR_TB.voided = 0
			 LEFT JOIN (SELECT 
					o.encounter_id, GROUP_CONCAT(CONCAT(IFNULL(cn.name,''),'\n', IFNULL(DATE_FORMAT(o.obs_datetime,'%d/%m/%Y'),''))) AS ss
				     FROM obs o 
				     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 307 AND o.voided = 0
				     GROUP BY o.encounter_id ) SPUTUM_SMEAR_RESULTS ON SPUTUM_SMEAR_RESULTS.encounter_id = A.encounter_id 
			 LEFT JOIN (SELECT 
					o.encounter_id, GROUP_CONCAT(CONCAT(IFNULL(cn.name,''),'\n', IFNULL(DATE_FORMAT(o.obs_datetime,'%d/%m/%Y'),'')) SEPARATOR '\n') AS dst
				     FROM obs o 
				     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 159984 AND o.voided = 0
				     GROUP BY o.encounter_id ) DST_RESULTS ON DST_RESULTS.encounter_id = A.encounter_id 
			 LEFT JOIN obs TREATMENT_OUTCOME_DATE ON TREATMENT_OUTCOME_DATE.encounter_id = A.encounter_id AND TREATMENT_OUTCOME_DATE.concept_id = 159787 AND TREATMENT_OUTCOME_DATE.voided = 0
			 LEFT JOIN obs TRANSFER_OUT_UNIT ON TRANSFER_OUT_UNIT.encounter_id = A.encounter_id AND TRANSFER_OUT_UNIT.concept_id = 90211 AND TRANSFER_OUT_UNIT.voided = 0
			 GROUP BY A.patient_id, A.encounter_id	  
	 * ==================================================================
	 * */
}
