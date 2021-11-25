package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import com.google.common.base.Joiner;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.MapDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.HIEOutcomesCohortDataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;


/**
 */
@Handler(supports = {HIEOutcomesCohortDataSetDefinition.class})
public class HIEOutcomesDatasetEvaluator implements DataSetEvaluator {

    @Autowired
    private EvaluationService evaluationService;


    String baseSql;
    @Override
    public MapDataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        MapDataSet dataSet = new MapDataSet(dataSetDefinition, context);
        HIEOutcomesCohortDataSetDefinition definition = (HIEOutcomesCohortDataSetDefinition) dataSetDefinition;

        String startdate = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
        String enddate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");


         baseSql = String.format("SELECT p.person_id,p.birthdate FROM person p INNER JOIN encounter e ON (e.patient_id = p.person_id AND e.encounter_type = (SELECT encounter_type_id FROM encounter_type\n" +
                 "WHERE uuid ='9fcfcc91-ad60-4d84-9710-11cc25258719')) WHERE  TIMESTAMPDIFF(MONTH,p.birthdate,'%s')=24",enddate);

        SqlQueryBuilder q = new SqlQueryBuilder(baseSql);
        Map<Integer, Date> results = evaluationService.evaluateToMap(q, Integer.class, Date.class, context);

        Integer enrolledIntoCare24MonthsAgo = results.size();
        String patientIDs = Joiner.on(",").join(results.keySet());

        Integer clientsEnrolledAt6Weeks=0;
        Integer cleintsEnrolledAt12Weeks=0;
        Integer ctxProphylaxisat2Months=0;
        Integer firstDNAPCR2Months=0;
        Integer secondDNAPCR9Months=0;
        Integer sixMonthsAfterBreastfeeding=0;
        Integer exclusiveBreasfeeding=0;
        Integer breastfeedingfor1Year=0;
        Integer numberTestedPositiveonPCR=0;
        Integer enrolledIntoCare=0;
        Integer diedBefore18Months=0;
        Integer transferedBefore18Months=0;
        Integer LosttoFollowUp=0;
        Integer clientsDischarged=0;
        Integer unknownFinalOutCome=0;



        if (results.size()>0)
        {
            String initiatedonARTSixweeks = String.format("SELECT o.person_id,p.birthdate AS number FROM obs o INNER  JOIN person p on o.person_id = p.person_id WHERE concept_id = 99787 AND value_coded =162966 AND o.person_id IN (%s) GROUP BY person_id",patientIDs);
            clientsEnrolledAt6Weeks =(evaluationService.evaluateToList(new SqlQueryBuilder(initiatedonARTSixweeks),context)).size();
            String initiatedAt12Weeks=String.format("SELECT person_id AS number FROM obs WHERE concept_id = 99787 AND value_coded =165426 AND person_id in (%s)",patientIDs);
            cleintsEnrolledAt12Weeks=(evaluationService.evaluateToList(new SqlQueryBuilder(initiatedAt12Weeks), context)).size();
            String ctxprophylaxis = String.format("SELECT o.person_id FROM obs o INNER JOIN person p on(o.person_id = p.person_id) WHERE  o.concept_id = 99773 AND FLOOR(DATEDIFF(o.value_datetime, p.birthdate)/7) BETWEEN 6 AND 8 AND p.person_id IN (%s) ",patientIDs);
            ctxProphylaxisat2Months=(evaluationService.evaluateToList(new SqlQueryBuilder(ctxprophylaxis), context)).size();
            String firstDNAPCR=String.format("SELECT p.person_id AS number FROM obs o INNER JOIN person p on( o.person_id = p.person_id) WHERE o.concept_id = 99606 AND FLOOR(DATEDIFF(o.value_datetime, p.birthdate)/7) BETWEEN 6 AND 8 AND p.person_id IN (%s)",patientIDs);
            firstDNAPCR2Months=(evaluationService.evaluateToList(new SqlQueryBuilder(firstDNAPCR), context)).size();
            String secondDNAPCR=String.format("SELECT p.person_id FROM person p INNER JOIN obs o ON (o.person_id = p.person_id AND o.concept_id IN (99436, 162876) AND TIMESTAMPDIFF(MONTH, p.birthdate, o.value_datetime) BETWEEN 9 AND 12) INNER JOIN obs r ON (o.encounter_id = r.encounter_id AND r.concept_id IN (99435, 99440, 162881) AND r.value_coded = 703) AND p.person_id IN ('%s') GROUP BY o.person_id",patientIDs);
            secondDNAPCR9Months=(evaluationService.evaluateToList(new SqlQueryBuilder(secondDNAPCR), context)).size();
            String sixMonthsAfterBF= String.format("SELECT p.person_id AS number FROM obs o INNER JOIN person p on( o.person_id = p.person_id) WHERE o.concept_id = 165405 AND FLOOR(DATEDIFF(o.value_datetime, p.birthdate)/7) BETWEEN 6 AND 8 AND p.person_id IN (%s)",patientIDs);
            sixMonthsAfterBreastfeeding=(evaluationService.evaluateToList(new SqlQueryBuilder(sixMonthsAfterBF), context)).size();
            String clientsExclusivelyBreastfed=String.format("SELECT o.person_id FROM obs o INNER JOIN person p on(o.person_id = p.person_id) inner join obs oi on(o.encounter_id = oi.encounter_id ) WHERE o.concept_id = 99449 AND oi.concept_id = 99451 AND o.value_numeric = 6  AND oi.value_coded = 5526  AND p.person_id IN (%s)",patientIDs);
            exclusiveBreasfeeding=(evaluationService.evaluateToList(new SqlQueryBuilder(clientsExclusivelyBreastfed), context)).size();
            String breastfeedingfor1year= String.format("SELECT o.person_id FROM obs o INNER JOIN person p on(o.person_id = p.person_id) inner join obs oi on(o.encounter_id = oi.encounter_id ) WHERE o.concept_id = 99449 AND oi.concept_id = 99451 AND o.value_numeric = 1  AND oi.value_coded = 5526 AND p.person_id IN (%s)",patientIDs);
            breastfeedingfor1Year=(evaluationService.evaluateToList(new SqlQueryBuilder(breastfeedingfor1year), context)).size();
            String testedPositiveFromPCRTests=String.format("SELECT o.person_id FROM obs o INNER JOIN person p on(o.person_id = p.person_id)  WHERE o.concept_id IN(162880,164980,165406,99440,99435)  and o.value_coded=703 AND p.person_id IN (%s) group by p.person_id",patientIDs);
            numberTestedPositiveonPCR=(evaluationService.evaluateToList(new SqlQueryBuilder(testedPositiveFromPCRTests), context)).size();
            String enrolledIntoHIVCare= String.format("SELECT o.person_id FROM obs o INNER JOIN person p on(o.person_id = p.person_id)  WHERE o.concept_id=163004  and o.value_coded=1065 AND p.person_id IN (%s) group by p.person_id",patientIDs);
            enrolledIntoCare=(evaluationService.evaluateToList(new SqlQueryBuilder(enrolledIntoHIVCare), context)).size();
            String deathFinalOutCome=String.format("SELECT o.person_id FROM person p INNER JOIN obs o ON (o.concept_id = 99428 AND o.value_coded IN (165420,165421) AND p.person_id = o.person_id) INNER JOIN obs r ON (r.person_id = o.person_id AND TIMESTAMPDIFF(MONTH, p.birthdate, r.value_datetime) <= 18 AND r.concept_id = 162979) AND o.person_id IN (%s) GROUP BY o.person_id",patientIDs);
            diedBefore18Months=(evaluationService.evaluateToList(new SqlQueryBuilder(deathFinalOutCome), context)).size();
            String childrenTransfered=String.format("SELECT o.person_id FROM person p INNER JOIN obs o ON (o.concept_id = 99428 AND o.value_coded = 90306 AND p.person_id = o.person_id) INNER JOIN obs r ON (r.person_id = o.person_id AND TIMESTAMPDIFF(MONTH, p.birthdate, r.value_datetime) <= 18 AND r.concept_id = 162979) AND o.person_id IN (%s) GROUP BY o.person_id",patientIDs);
            transferedBefore18Months=(evaluationService.evaluateToList(new SqlQueryBuilder(childrenTransfered), context)).size();
            String childredLTF=String.format("SELECT o.person_id FROM person p INNER JOIN obs o ON (o.concept_id = 99428 AND o.value_coded = 5240 AND p.person_id = o.person_id) INNER JOIN obs r ON (r.person_id = o.person_id AND TIMESTAMPDIFF(MONTH, p.birthdate, r.value_datetime) <= 18 AND r.concept_id = 162979) AND o.person_id IN (%s) GROUP BY o.person_id",patientIDs);
            LosttoFollowUp=(evaluationService.evaluateToList(new SqlQueryBuilder(childredLTF), context)).size();
            String discharged= String.format("SELECT o.person_id FROM person p INNER JOIN obs o ON (o.concept_id = 99428 AND o.value_coded = 99427 AND p.person_id = o.person_id) INNER JOIN obs r ON (r.person_id = o.person_id AND TIMESTAMPDIFF(MONTH, p.birthdate, r.value_datetime) <= 18 AND r.concept_id = 162979) AND o.person_id IN (%s) GROUP BY o.person_id",patientIDs);
            clientsDischarged=(evaluationService.evaluateToList(new SqlQueryBuilder(discharged), context)).size();
            String unknownOutcome=String.format("SELECT o.person_id FROM person p INNER JOIN obs o ON (o.concept_id = 99428 AND o.value_coded = 1067 AND p.person_id = o.person_id) INNER JOIN obs r ON (r.person_id = o.person_id AND TIMESTAMPDIFF(MONTH, p.birthdate, r.value_datetime) <= 18 AND r.concept_id = 162979) AND o.person_id IN (%s) GROUP BY o.person_id",patientIDs);
            unknownFinalOutCome=(evaluationService.evaluateToList(new SqlQueryBuilder(unknownOutcome), context)).size();


        }

        dataSet.addData(new DataSetColumn("HEI1", "Total Number of HEI in the cohort", String.class), enrolledIntoCare24MonthsAgo);
        dataSet.addData(new DataSetColumn("HEI2", "Clients Enrolled in Care At 6 weeks", String.class), clientsEnrolledAt6Weeks);
        dataSet.addData(new DataSetColumn("HEI3", "Clients Enrolled in Care at 12 Weeks", String.class), cleintsEnrolledAt12Weeks);
        dataSet.addData(new DataSetColumn("HEI4", "CTX Prophylaxis given at 2 Months", String.class), ctxProphylaxisat2Months);
        dataSet.addData(new DataSetColumn("HEI5", "First DNA PCR Done at 2 Months", String.class), firstDNAPCR2Months);
        dataSet.addData(new DataSetColumn("HEI6", "Second DNA PCR Done at 9 Months", String.class), secondDNAPCR9Months);
        dataSet.addData(new DataSetColumn("HEI7", "Six  months after breastfeeding ", String.class), sixMonthsAfterBreastfeeding);
        dataSet.addData(new DataSetColumn("HEI8", "Children exclusively breastfed", String.class), exclusiveBreasfeeding);
        dataSet.addData(new DataSetColumn("HEI9", "Children exclusively breastfed for a year", String.class), breastfeedingfor1Year);
        dataSet.addData(new DataSetColumn("HEI10", "Children tested Positive on any PCR test 24 Months ago", String.class), numberTestedPositiveonPCR);
        dataSet.addData(new DataSetColumn("HEI11", "Children enrolled Into care after testing Positive", String.class), enrolledIntoCare);
        dataSet.addData(new DataSetColumn("HEI12", "Children that died before 18 months of age", String.class), diedBefore18Months);
        dataSet.addData(new DataSetColumn("HEI13", "Children that transfered Out before 18 months of age", String.class), transferedBefore18Months);
        dataSet.addData(new DataSetColumn("HEI14", "Children that  Lost to follow up before 18 months of age", String.class), LosttoFollowUp);
        dataSet.addData(new DataSetColumn("HEI15", "Children that  discharged before 18 months of age", String.class), clientsDischarged);
        dataSet.addData(new DataSetColumn("HEI16", "Children whose final out come  18 months of age", String.class), unknownFinalOutCome);







        return dataSet;


    }
}
