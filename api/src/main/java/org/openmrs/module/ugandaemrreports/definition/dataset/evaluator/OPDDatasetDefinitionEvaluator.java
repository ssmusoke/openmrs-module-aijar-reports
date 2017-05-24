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
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.OPDDatasetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by carapai on 11/05/2016.
 */
@Handler(supports = {OPDDatasetDefinition.class})
public class OPDDatasetDefinitionEvaluator implements DataSetEvaluator {

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
        OPDDatasetDefinition definition = (OPDDatasetDefinition) dataSetDefinition;

        String date = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");

        LocalDate workingDate = StubDate.dateOf(date);

        int beginningMonth = workingDate.getMonthOfYear();
        int beginningYear = workingDate.getYear();

        context = ObjectUtil.nvl(context, new EvaluationContext());

        String sql = "SELECT \r\n" +
        		"\tSERIAL_NO.value_text \tAS serial_no,\r\n" +
        		"\tCONCAT(PN.family_name, ' ', PN.given_name)\tAS patientName,\r\n" +
        		"\tCONCAT('Village: ', IFNULL(UCASE(PA.address5),''), '\\r\\n',\r\n" +
        		"\t'Parish: ', IFNULL(UCASE(PA.address4),''), '\\r\\n', \r\n" +
        		"\t'Sub-county: ', IFNULL(UCASE(PA.address3),''), '\\r\\n', \r\n" +
        		"\t'County: ',   IFNULL(UCASE(PA.state_province),''), '\\r\\n', \r\n" +
        		"\t'District: ', IFNULL(UCASE(PA.county_district),''))\tAS residence,\r\n" +
        		"\tDATEDIFF(A.encounter_datetime,P.birthdate) \tAS ageInDays,\r\n" +
        		"\tYEAR(A.encounter_datetime) - YEAR(P.birthdate) - (RIGHT(A.encounter_datetime, 5) < RIGHT(P.birthdate, 5)) \tAS ageInYears,\r\n" +
        		"\tP.gender\tAS sex,\t\r\n" +
        		"\tCONCAT(IFNULL(MUAC.value_numeric,''),'\\n', \r\n" +
        		"\tIFNULL(WEIGHT.value_numeric,''),'\\n', \r\n" +
        		"\tIFNULL(HEIGHT.value_numeric,'')) \tAS muacWeightHeight,\r\n" +
        		"\tCONCAT(IFNULL(BMI.value_numeric,''),'\\n', \r\n" +
        		"\tIFNULL(W4A_ZSCORE.name,''),'\\n', \r\n" +
        		"\tCASE H4A_ZSCORE.value_coded\r\n" +
        		"\tWHEN 1115 THEN 'N'\r\n" +
        		"\tWHEN 123814 THEN 'U'\r\n" +
        		"\tEND) \tAS bmiZScore,\r\n" +
        		"\tCONCAT(IFNULL(BLOOD_PRESSURE_DIA.value_numeric,''),'/',\r\n" +
        		"\tIFNULL(BLOOD_PRESSURE_SYS.value_numeric,''),'\\n', \r\n" +
        		"\tIFNULL(BLOOD_SUGAR.value_numeric,'')) \tAS bloodPressureBloodSugar,\r\n" +
        		"\tIFNULL(NEXT_OF_KIN.value_text, '')\tAS nextOfKin,\r\n" +
        		"\tCASE PALLIATIVE_CARE.value_coded \r\n" +
        		"\tWHEN 1065 THEN 'Y'\r\n" +
        		"\tWHEN 1066 THEN 'N'\r\n" +
        		"\tELSE ''\r\n" +
        		"\tEND\tAS needForPalliativeCare,\r\n" +
        		"\tCASE PATIENT_CLASS.value_coded \r\n" +
        		"\tWHEN 1597 THEN 'Y'\r\n" +
        		"\tELSE ''\r\n" +
        		"\tEND\tAS classificationNewAttendance,\t\r\n" +
        		"\tCASE PATIENT_CLASS.value_coded \r\n" +
        		"\tWHEN 164142 THEN 'Y'\r\n" +
        		"\tELSE ''\r\n" +
        		"\tEND\tAS classificationReAttendance,\r\n" +
        		"\tCONCAT(CASE TOBACCO_USE.value_coded \r\n" +
        		"\tWHEN 1065 THEN 'Y'\r\n" +
        		"\tWHEN 1066 THEN 'N'\r\n" +
        		"\tELSE ''\r\n" +
        		"\tEND,'\\n',\r\n" +
        		"\tCASE ALCOHOL_USE.value_coded \r\n" +
        		"\tWHEN 1065 THEN 'Y'\r\n" +
        		"\tWHEN 1066 THEN 'N'\r\n" +
        		"\tELSE ''\r\n" +
        		"\tEND)\tAS tobaccoUseAlcoholUse,\r\n" +
        		"\tCASE FEVER.value_coded \r\n" +
        		"\tWHEN 5945 THEN 'Yes'\r\n" +
        		"\tELSE 'No'\r\n" +
        		"\tEND\tAS fever,\r\n" +
        		"\tCASE MALARIA_TEST.value_coded \r\n" +
        		"\tWHEN 32 THEN 'B/S'\r\n" +
        		"\tWHEN 1643 THEN 'RDT'\r\n" +
        		"\tWHEN 1118 THEN 'ND'\r\n" +
        		"\tELSE ''\r\n" +
        		"\tEND\tAS malariaTestsDone,\r\n" +
        		"\tCASE MALARIA_TEST_RESULTS.value_coded \r\n" +
        		"\tWHEN 703 THEN 'POS'\r\n" +
        		"\tWHEN 664 THEN 'NEG'\r\n" +
        		"\tWHEN 1118 THEN 'ND'\r\n" +
        		"\tELSE ''\r\n" +
        		"\tEND\tAS malariaTestResults,\r\n" +
        		"\tCASE NEW_PRESUMED_TB_CASE.value_coded \r\n" +
        		"\tWHEN 1065 THEN 'Y'\r\n" +
        		"\tWHEN 1066 THEN 'N'\r\n" +
        		"\tELSE ''\r\n" +
        		"\tEND\tAS newPresumedTbCase,\r\n" +
        		"\tCASE PATIENT_SENT_TO_LAB.value_coded \r\n" +
        		"\tWHEN 90073 THEN 'Y'\r\n" +
        		"\tELSE ''\r\n" +
        		"\tEND\tAS patientSentToLab,\r\n" +
        		"\tCASE LAB_TB_RESULTS.value_coded \r\n" +
        		"\tWHEN 703 THEN 'POS'\r\n" +
        		"\tWHEN 664 THEN 'NEG'\r\n" +
        		"\tWHEN 1175 THEN 'N/A'\r\n" +
        		"\tELSE ''\r\n" +
        		"\tEND\tAS labTbResult,\r\n" +
        		"\tCASE LINKED_TO_TB_CLINIC.value_coded \r\n" +
        		"\tWHEN 1065 THEN 'Y'\r\n" +
        		"\tWHEN 1066 THEN 'N'\r\n" +
        		"\tELSE ''\r\n" +
        		"\tEND\tAS linkedToTbClinic,\r\n" +
        		"\tNEW_DIAGNOSIS.diagnoses\tAS newDiagnosis,\r\n" +
        		"\tDRUGS.drugs\tAS drugsTreatment,\r\n" +
        		"\tCASE DISABILITY.value_coded \r\n" +
        		"\tWHEN 1065 THEN 'Y'\r\n" +
        		"\tWHEN 1066 THEN 'N'\r\n" +
        		"\tELSE ''\r\n" +
        		"\tEND\tAS disability,\r\n" +
        		"\tIFNULL(REF_IN_NUM.value_text, '')\tAS refInNum,\r\n" +
        		"\tIFNULL(REF_OUT_NUM.value_text, '')\tAS refOutNum\t\r\n" +
        		"FROM\r\n" +
        		"(SELECT\r\n" +
        		"     e.encounter_id,\r\n" +
        		"     e.patient_id,\r\n" +
        		"     e.encounter_datetime\r\n" +
        		"   FROM encounter e\r\n" +
        		"   INNER JOIN encounter_type et ON et.encounter_type_id = e.encounter_type AND (et.uuid = 'ee4780f5-b5eb-423b-932f-00b5879df5ab')\r\n" +
        		String.format("   WHERE YEAR(e.encounter_datetime) = %d AND MONTH(e.encounter_datetime) = %d AND e.voided = 0\r\n", beginningYear, beginningMonth) +
        		"   ORDER BY e.encounter_datetime) A \r\n" +
        		"  INNER JOIN person P\r\n" +
        		"   ON (P.person_id = A.patient_id)\r\n" +
        		"  LEFT JOIN person_name PN ON (P.person_id = PN.person_id)\r\n" +
        		"  LEFT JOIN person_address PA ON (P.person_id = PA.person_id AND PA.preferred = 1 AND PA.voided = 0)\r\n" +
        		"  LEFT JOIN obs SERIAL_NO ON SERIAL_NO.encounter_id = A.encounter_id AND SERIAL_NO.concept_id = 1646 AND SERIAL_NO.voided = 0 \r\n" +
        		"  LEFT JOIN obs MUAC ON MUAC.encounter_id = A.encounter_id AND MUAC.concept_id = 1343 AND MUAC.voided = 0 \r\n" +
        		"  LEFT JOIN obs WEIGHT ON WEIGHT.encounter_id = A.encounter_id AND WEIGHT.concept_id = 5089 AND WEIGHT.voided = 0 \r\n" +
        		"  LEFT JOIN obs HEIGHT ON HEIGHT.encounter_id = A.encounter_id AND HEIGHT.concept_id = 5090 AND HEIGHT.voided = 0 \r\n" +
        		"  LEFT JOIN obs BMI ON BMI.encounter_id = A.encounter_id AND BMI.concept_id = 1342 AND BMI.voided = 0 \r\n" +
        		"  LEFT JOIN (SELECT \r\n" +
        		"\to.encounter_id, cn.name\r\n" +
        		"\t     FROM obs o \r\n" +
        		"\t     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 1854 AND o.voided = 0\r\n" +
        		"\t     GROUP BY o.encounter_id ) W4A_ZSCORE ON W4A_ZSCORE.encounter_id = A.encounter_id    \r\n" +
        		"  LEFT JOIN obs H4A_ZSCORE ON H4A_ZSCORE.encounter_id = A.encounter_id AND H4A_ZSCORE.concept_id = 164088 AND H4A_ZSCORE.voided = 0 \r\n" +
        		"  LEFT JOIN obs BLOOD_PRESSURE_DIA ON BLOOD_PRESSURE_DIA.encounter_id = A.encounter_id AND BLOOD_PRESSURE_DIA.concept_id = 5086 AND BLOOD_PRESSURE_DIA.voided = 0 \r\n" +
        		"  LEFT JOIN obs BLOOD_PRESSURE_SYS ON BLOOD_PRESSURE_SYS.encounter_id = A.encounter_id AND BLOOD_PRESSURE_SYS.concept_id = 5085 AND BLOOD_PRESSURE_SYS.voided = 0 \r\n" +
        		"  LEFT JOIN obs BLOOD_SUGAR ON BLOOD_SUGAR.encounter_id = A.encounter_id AND BLOOD_SUGAR.concept_id = 887 AND BLOOD_SUGAR.voided = 0 \r\n" +
        		"  LEFT JOIN obs NEXT_OF_KIN ON NEXT_OF_KIN.encounter_id = A.encounter_id AND NEXT_OF_KIN.concept_id = 162729 AND NEXT_OF_KIN.voided = 0 \r\n" +
        		"  LEFT JOIN obs PALLIATIVE_CARE ON PALLIATIVE_CARE.encounter_id = A.encounter_id AND PALLIATIVE_CARE.concept_id = 164924 AND PALLIATIVE_CARE.voided = 0 \r\n" +
        		"  LEFT JOIN obs PATIENT_CLASS ON PATIENT_CLASS.encounter_id = A.encounter_id AND PATIENT_CLASS.concept_id = 162728 AND PATIENT_CLASS.voided = 0 \r\n" +
        		"  LEFT JOIN obs TOBACCO_USE ON TOBACCO_USE.encounter_id = A.encounter_id AND TOBACCO_USE.concept_id = 163731 AND TOBACCO_USE.voided = 0 \r\n" +
        		"  LEFT JOIN obs ALCOHOL_USE ON ALCOHOL_USE.encounter_id = A.encounter_id AND ALCOHOL_USE.concept_id = 159449 AND ALCOHOL_USE.voided = 0 \r\n" +
        		"  LEFT JOIN obs FEVER ON FEVER.encounter_id = A.encounter_id AND FEVER.concept_id = 1069 AND FEVER.voided = 0 \r\n" +
        		"  LEFT JOIN obs MALARIA_TEST ON MALARIA_TEST.encounter_id = A.encounter_id AND MALARIA_TEST.concept_id = 164433 AND MALARIA_TEST.voided = 0 \r\n" +
        		"  LEFT JOIN obs MALARIA_TEST_RESULTS ON MALARIA_TEST_RESULTS.encounter_id = A.encounter_id AND MALARIA_TEST_RESULTS.concept_id = 164434 AND MALARIA_TEST_RESULTS.voided = 0 \r\n" +
        		"  LEFT JOIN obs NEW_PRESUMED_TB_CASE ON NEW_PRESUMED_TB_CASE.encounter_id = A.encounter_id AND NEW_PRESUMED_TB_CASE.concept_id = 99498 AND NEW_PRESUMED_TB_CASE.voided = 0 \r\n" +
        		"  LEFT JOIN obs PATIENT_SENT_TO_LAB ON PATIENT_SENT_TO_LAB.encounter_id = A.encounter_id AND PATIENT_SENT_TO_LAB.concept_id = 90216 AND PATIENT_SENT_TO_LAB.voided = 0 \r\n" +
        		"  LEFT JOIN obs LAB_TB_RESULTS ON LAB_TB_RESULTS.encounter_id = A.encounter_id AND LAB_TB_RESULTS.concept_id = 160108 AND LAB_TB_RESULTS.voided = 0 \r\n" +
        		"  LEFT JOIN obs LINKED_TO_TB_CLINIC ON LINKED_TO_TB_CLINIC.encounter_id = A.encounter_id AND LINKED_TO_TB_CLINIC.concept_id = 164435 AND LINKED_TO_TB_CLINIC.voided = 0 \r\n" +
        		"  LEFT JOIN (SELECT \r\n" +
        		"\to.encounter_id,REPLACE(GROUP_CONCAT(CONCAT('\"', UCASE(cn.name) , '\"\\n')),\",\",\"\") diagnoses\r\n" +
        		"\t     FROM obs o \r\n" +
        		"\t     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 163149 AND o.voided = 0\r\n" +
        		"\t     GROUP BY o.encounter_id ) NEW_DIAGNOSIS ON NEW_DIAGNOSIS.encounter_id = A.encounter_id\r\n" +
        		"  LEFT JOIN (SELECT \r\n" +
        		"\to.encounter_id,REPLACE(GROUP_CONCAT(CONCAT('\"', UCASE(cn.name) , '\"\\n')),\",\",\"\") drugs\r\n" +
        		"\t     FROM obs o \r\n" +
        		"\t     INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.locale = 'en' AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.voided = 0 WHERE o.concept_id = 1282 AND o.voided = 0\r\n" +
        		"\t     GROUP BY o.encounter_id) DRUGS ON DRUGS.encounter_id = A.encounter_id\r\n" +
        		"  LEFT JOIN obs DISABILITY ON DISABILITY.encounter_id = A.encounter_id AND DISABILITY.concept_id = 162558 AND DISABILITY.voided = 0   \r\n" +
        		"  LEFT JOIN obs REF_IN_NUM ON REF_IN_NUM.encounter_id = A.encounter_id AND REF_IN_NUM.concept_id = 164436 AND REF_IN_NUM.voided = 0 \r\n" +
        		"  LEFT JOIN obs REF_OUT_NUM ON REF_OUT_NUM.encounter_id = A.encounter_id AND REF_OUT_NUM.concept_id = 99767 AND REF_OUT_NUM.voided = 0 \r\n";        

        SqlQueryBuilder q = new SqlQueryBuilder(sql);


        List<Object[]> results = evaluationService.evaluateToList(q, context);

        PatientDataHelper pdh = new PatientDataHelper();

        for (Object[] r : results) {
            DataSetRow row = new DataSetRow();

            String age028Days = "";
            String age29Days4Yrs = "";
            String age559Yrs = "";
            String age60AndAbove = "";
            
            Long AgeInDays = (Long) r[3];
            Long AgeInYears = (Long) r[4];
            
            if (AgeInDays < 29) {
            	age028Days = "Y";
            }

            if (AgeInDays >= 29 && AgeInYears < 5) {
            	age29Days4Yrs = "Y";
            }

            if (AgeInYears >= 5 && AgeInYears < 60) {
            	age559Yrs = "Y";
            }

            if (AgeInYears > 60) {
            	age60AndAbove = "Y";
            }

            pdh.addCol(row, "Serial No", r[0]);
            pdh.addCol(row, "Name of patient", r[1]);
            pdh.addCol(row, "Residence", r[2]);
            pdh.addCol(row, "Age 0-28Days", age028Days);
            pdh.addCol(row, "Age 29Days-4yrs", age29Days4Yrs);
            pdh.addCol(row, "Age 5-59yrs", age559Yrs);
            pdh.addCol(row, "Age 60 and above", age60AndAbove);
            pdh.addCol(row, "Sex", r[5]);
            pdh.addCol(row, "MUAC Weight Height", r[6]);
            pdh.addCol(row, "BMI Z Score", r[7]);
            pdh.addCol(row, "Blood Pressure Blood Sugar", r[8]);
            pdh.addCol(row, "Next of Kin", r[9]);
            pdh.addCol(row, "Need for palliative care", r[10]);
            pdh.addCol(row, "Classification New Attendance", r[11]);
            pdh.addCol(row, "Classification Re-attendance", r[12]);
            pdh.addCol(row, "Tobacco Use Alcohol Use", r[13]);
            pdh.addCol(row, "Fever", r[14]);
            pdh.addCol(row, "Malaria Tests Done", r[15]);
            pdh.addCol(row, "Malaria Test Results", r[16]);
            pdh.addCol(row, "New presumed TB case", r[17]);
            pdh.addCol(row, "Patient sent to lab", r[18]);
            pdh.addCol(row, "Lab TB result", r[19]);
            pdh.addCol(row, "Linked to TB clinic", r[20]);
            pdh.addCol(row, "New diagnosis", r[21]);
            pdh.addCol(row, "Drugs Treatment", r[22]);
            pdh.addCol(row, "Disability", r[23]);
            pdh.addCol(row, "Ref in Num", r[24]);
            pdh.addCol(row, "Ref out Num", r[25]);
            
            dataSet.addRow(row);
        }
        return dataSet;
    }

}
