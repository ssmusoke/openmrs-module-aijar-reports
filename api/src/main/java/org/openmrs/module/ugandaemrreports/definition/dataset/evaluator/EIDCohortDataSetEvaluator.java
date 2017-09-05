package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.collections.CollectionUtils;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.DateUtil;
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
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.EIDCohortDataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
@Handler(supports = {EIDCohortDataSetDefinition.class})
public class EIDCohortDataSetEvaluator implements DataSetEvaluator {

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
        EIDCohortDataSetDefinition definition = (EIDCohortDataSetDefinition) dataSetDefinition;

        String date = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");

        String[] indicators = {
                "% mothers who received ARVs for EMTCT",
                "% Infants who received ARVs for EMTCT at 0-6 weeks",
                "% HEI received 1st DNA PCR at age 6-8 weeks",
                "% HEI received 1st DNA PCR at age 6-8 weeks and results  given to the care giver",
                "% HEI tested positive by 1st PCR at age 6-8 weeks and results given to the care giver",
                "% HEI who received cotrimoxazole at age 6-8 weeks",
                "% HEI who were Exclusively Breastfed at 6 months among HEI assessed",
                "% HEI active in care",
                "% HEI tested HIV positive between 0 and 12 months",
                "% HEI tested HIV positive and started on ART",
                "% HEI transferred out between 0 and 12 months",
                "% HEI lost between 0 and 12 months",
                "% HEI died between 0 and 12 months",
                "% HEI who received 2nd DBS test",
                "% HEI tested with AB test at ≥ 18 months and results are available",
                "% HEI with AB test negative at 18 months",
                "% HEI active at 18 months but no AB test done",
                "% HEI transferred out between 0 and 18 months",
                "% HEI lost to follow-up between 0 and 18 months",
                "% HEI died between 0 and 18 months",
                "% HEI tested HIV positive between 0 and 18 months",
                "% HEI tested HIV positive and started on ART"
        };

        String[] targets = {
                "100%",
                "90%",
                "80%",
                "80%",
                "100%",
                "80%",
                "100%",
                "80%",
                "<5%",
                "100%",
                "NA",
                "NA",
                "NA",
                "80%",
                "80%",
                "80%",
                "0%",
                "NA",
                "NA",
                "NA",
                "<5%",
                "100%"
        };

        String[] targetNumbers = {
                "100",
                "90",
                "80",
                "80",
                "100",
                "80",
                "100",
                "80",
                "5",
                "100",
                "NA",
                "NA",
                "NA",
                "80",
                "80",
                "80",
                "0",
                "NA",
                "NA",
                "NA",
                "5",
                "100"
        };


        String[] targetSigns = {
                ">=",
                ">=",
                ">=",
                ">=",
                ">=",
                ">=",
                ">=",
                ">=",
                "<",
                ">=",
                "-",
                "-",
                "-",
                ">=",
                ">=",
                ">=",
                "=",
                "-",
                "-",
                "-",
                "<",
                ">="
        };


        String[] numerators = {
                "Number of mothers who received ART (Col 13 code 1)",
                "Number of infants who received ARVs for EMTCT (Col 14 code 1 plus 2)",
                "Number of HEI received 1st DNA PCR at age 6-8 weeks (Col 16)",
                "Number of HEI who received 1st DNA PCR at age 6-8 weeks and results given to the care giver (Col 20)",
                "Number of HEI tested positive by 1st DNA PCR at age  6-8 weeks  and results given to the care giver (Col 20)",
                "Number of HEI who received cotrimoxazole at age 6-8 weeks (Col 8)",
                "Number of HEI who were Exclusively Breastfed (EBF) at 6 months (Look at longitudinal section and find visit infant was 6 month and look for code EBF)",
                "Number of HEI attended a follow-up visit in the last 3 months prior to the assessment (Col 28)",
                "Number of HEI who tested HIV positive by any test DNA/PCR or Rapid test between 0 and 12 months  (Col 29)",
                "Number of HEI who tested HIV positive within a cohort and started ART (Col 29)",
                "Number of HEI who transferred out between 0 and 12 months (Col 29)",
                "Number of HEI lost between 0 and 12 months (Col 29)",
                "Number of HEI who died between 0 and 12 months visit (Col 29)",
                "Number of HEI who received 2nd DBS test (Col 24)",
                "Number of HEI tested with AB at ≥ 18 months and results are available (Col 27)",
                "Number of HEI with AB test negative at 18 months (Col 28)",
                "Number of HEI attended 18 month visit and no AB test result documented (Col 28)",
                "Number of HEI transferred out (Col 29)",
                "Number of HEI lost to follow up (Col 29)",
                "Number of HEI reported dead (Col 29))",
                "Number of HEI identified as HIV positive within a cohort (Col 18, 24 and 27)",
                "Number of HEI who tested HIV positive within a cohort and started ART (Col 29)"
        };

        String[] denominators = {
                "Number of HEI registered in cohort (EI Register Col 1)",
                "Number of HEI registered in cohort  (Col 1)",
                "Number of HEI registered in cohort  (Col 1)",
                "Number of HEI registered in cohort  (Col 1)",
                "Number of HEI who received 1st DNA PCR at age 6-8 weeks and tested positive 9 (column 18)",
                "Number of HEI registered in cohort  (Col 1)",
                "Number of HEI who had feeding status assessed at 6 months (Longitudinal section, 6 month visit with EBF, RF, CF, W and NLB)",
                "Number of HEI registered in cohort (Col 1)",
                "Number of HEI registered in cohort (Col 1)",
                "Number of HEI identified as HIV positive within a cohort (Col 18 PLUS col 24)",
                "Number of HEI registered in cohort (Col 1)",
                "Number of HEI registered in cohort (Col 1)",
                "Number of HEI registered in cohort (Col 1)",
                "Number of HEI registered in cohort who had a negative 1st DNA PCR test result (Col 18, negative test result)",
                "Number of HEI eligible for test(All tested negative at 1 and 2 nd PCR in the cohort)",
                "Number of HEI registered in birth cohort  (Col 1)",
                "Number of HEI registered in birth cohort  (Col 1)",
                "Number of HEI registered in birth cohort  (Col 1)",
                "Number of HEI registered in birth cohort  (Col 1)",
                "Number of HEI registered in birth cohort  (Col 1)",
                "Number of HEI registered in birth cohort  (Col 1)",
                "Number of HEI identified as HIV positive within a cohort (Col 18 PLUS col 24)"
        };

        String[] sns = {
                "1.0",
                "2.0",
                "3.0",
                "4.0",
                "5.0",
                "6.0",
                "7.0",
                "8.0",
                "9.0",
                "10.0",
                "11.0",
                "12.0",
                "13.0",
                "14.0",
                "15.0",
                "16.0",
                "17.0",
                "18.0",
                "19.0",
                "20.0",
                "21.0",
                "22.0"
        };

        String baseSql = "SELECT\n" +
                "  p.person_id,\n" +
                "  p.birthdate,\n" +
                "  MONTH(p.birthdate)          AS birthmonth,\n" +
                "  e.encounter_datetime        AS date_chart_opened,\n" +
                "  MONTH(e.encounter_datetime) AS month_chart_opened,\n" +
                "  p.death_date\n" +
                "FROM person p INNER JOIN encounter e ON (e.patient_id = p.person_id AND e.encounter_type = (SELECT encounter_type_id\n" +
                "                                                                                            FROM encounter_type\n" +
                "                                                                                            WHERE uuid =\n" +
                "                                                                                                  '9fcfcc91-ad60-4d84-9710-11cc25258719')\n" +
                String.format("                                         AND YEAR('%s') = YEAR(p.birthdate))", date);

        SqlQueryBuilder q = new SqlQueryBuilder(baseSql);
        List<Object[]> results = evaluationService.evaluateToList(q, context);

        Multimap<String, Integer> allClients = ArrayListMultimap.create();

        Map<String, Collection<Integer>> numeratorsAndDenominators = getPatients(context);


        for (Object[] o : results) {
            allClients.put(String.valueOf(o[2]), (Integer) o[0]);
        }


        PatientDataHelper pdh = new PatientDataHelper();


        for (int i = 0; i < 22; i++) {
            DataSetRow row = new DataSetRow();


            pdh.addCol(row, "S/N", sns[i]);
            pdh.addCol(row, "Indicator", indicators[i]);
            pdh.addCol(row, "Target", targets[i]);
            pdh.addCol(row, "Numerator", numerators[i]);
            pdh.addCol(row, "Denominators", denominators[i]);

            for (int j = 1; j <= 12; j++) {

                Collection<Integer> currentCohort = allClients.get(String.valueOf(j));
                Collection<Integer> currentNumerator = numeratorsAndDenominators.get("N" + String.valueOf(i + 1));
                Collection<Integer> currentDenominator = numeratorsAndDenominators.get("D" + String.valueOf(i + 1));

                String uniqueNumeratorString = "-";
                String uniqueDenominatorString = "-";

                if (currentNumerator != null) {
                    Collection uniqueNumerator = CollectionUtils.intersection(currentCohort, currentNumerator);
                    uniqueNumeratorString = String.valueOf(uniqueNumerator.size());
                }

                if (currentDenominator != null) {
                    Collection uniqueDenominator = CollectionUtils.intersection(currentCohort, currentDenominator);
                    uniqueDenominatorString = String.valueOf(uniqueDenominator.size());
                } else {
                    uniqueDenominatorString = String.valueOf(currentCohort.size());
                }

                pdh.addCol(row, "N" + String.valueOf(j), uniqueNumeratorString);
                pdh.addCol(row, "D" + String.valueOf(j), uniqueDenominatorString);

                if (uniqueDenominatorString.equalsIgnoreCase("-") || uniqueNumeratorString.equalsIgnoreCase("-")) {
                    pdh.addCol(row, "P" + String.valueOf(j), "-");
                    pdh.addCol(row, "T" + String.valueOf(j), "-");
                } else {
                    Integer numerator = Integer.valueOf(uniqueNumeratorString);
                    Integer denominator = Integer.valueOf(uniqueDenominatorString);

                    if (denominator != 0) {
                        Integer realPercentage = (numerator * 100) / denominator;

                        pdh.addCol(row, "P" + String.valueOf(j), (realPercentage));

                        if (targetNumbers[i].equalsIgnoreCase("NA")) {
                            pdh.addCol(row, "T" + String.valueOf(j), "-");
                        } else {

                            Integer targetPercentage = Integer.valueOf(targetNumbers[i]);

                            if (targetSigns[i].equalsIgnoreCase("=") && realPercentage.intValue() == targetPercentage.intValue()) {
                                pdh.addCol(row, "T" + String.valueOf(j), "Y");
                            } else if (targetSigns[i].equalsIgnoreCase("<") && realPercentage < targetPercentage) {
                                pdh.addCol(row, "T" + String.valueOf(j), "Y");
                            } else if (targetSigns[i].equalsIgnoreCase("<=") && realPercentage <= targetPercentage) {
                                pdh.addCol(row, "T" + String.valueOf(j), "Y");
                            } else if (targetSigns[i].equalsIgnoreCase(">") && realPercentage > targetPercentage) {
                                pdh.addCol(row, "T" + String.valueOf(j), "Y");
                            } else if (targetSigns[i].equalsIgnoreCase(">=") && realPercentage >= targetPercentage) {
                                pdh.addCol(row, "T" + String.valueOf(j), "Y");
                            } else {
                                pdh.addCol(row, "T" + String.valueOf(j), "N");
                            }
                        }
                    } else {
                        pdh.addCol(row, "P" + String.valueOf(j), "-");
                        pdh.addCol(row, "T" + String.valueOf(j), "-");
                    }

                }

            }
            dataSet.addRow(row);
        }
        return dataSet;
    }

    private Map<String, Collection<Integer>> getPatients(EvaluationContext context) {
        Map<String, Collection<Integer>> patients = new HashMap<String, Collection<Integer>>();

        String numerator1 = "SELECT person_id AS number FROM obs WHERE concept_id = 99783 AND value_coded = 163012";
        String numerator2 = "SELECT person_id AS number FROM obs WHERE concept_id = 99787 AND value_coded = 162966";
        String numerator3 = "SELECT p.person_id AS number FROM obs o INNER JOIN person p on(o.concept_id = 99606 AND o.person_id = p.person_id AND FLOOR(DATEDIFF(o.value_datetime, p.birthdate)/7) BETWEEN 6 AND 8)";
        String numerator4 = "SELECT o.person_id FROM obs o INNER JOIN person p on(o.concept_id = 99606 AND o.person_id = p.person_id AND FLOOR(DATEDIFF(o.value_datetime, p.birthdate)/7) BETWEEN 6 AND 8) inner join obs oi on(oi.concept_id = 99438 and oi.person_id = o.person_id)";
        String numerator5 = "SELECT o.person_id FROM obs o INNER JOIN person p on(o.concept_id = 99606 AND o.person_id = p.person_id AND FLOOR(DATEDIFF(o.value_datetime, p.birthdate)/7) BETWEEN 6 AND 8) inner join obs oi on(oi.concept_id = 99438 and oi.person_id = o.person_id) inner join obs ou on(ou.concept_id = 99435 and o.person_id = ou.person_id and ou.value_coded = 703)";
        String numerator6 = "SELECT o.person_id FROM obs o INNER JOIN person p on(o.concept_id = 99773 AND o.person_id = p.person_id AND FLOOR(DATEDIFF(o.value_datetime, p.birthdate)/7) BETWEEN 6 AND 8)";
        String numerator7 = "select o.person_id FROM obs o INNER JOIN person p on(o.concept_id = 99449 AND o.person_id = p.person_id and o.value_numeric = 6) inner join obs oi on(o.encounter_id = oi.encounter_id and oi.concept_id = 99451 and oi.value_coded = 5526)";
        String numerator8 = "SELECT p.person_id FROM person p INNER JOIN encounter e ON (e.patient_id = p.person_id AND e.encounter_type = (SELECT encounter_type_id FROM encounter_type WHERE uuid = '4345dacb-909d-429c-99aa-045f2db77e2b') AND e.encounter_datetime BETWEEN DATE_ADD(DATE_FORMAT(p.birthdate, '%Y-%m-01'), INTERVAL 9 MONTH) AND DATE_ADD(DATE_FORMAT(p.birthdate, '%Y-%m-01'), INTERVAL 12 MONTH)) GROUP BY p.person_id";
        String numerator9 = "SELECT p.person_id FROM person p INNER JOIN obs o ON (o.person_id = p.person_id AND o.concept_id IN (99606, 99436, 162876) AND TIMESTAMPDIFF(MONTH, p.birthdate, o.value_datetime) BETWEEN 0 AND 12) INNER JOIN obs r ON (o.encounter_id = r.encounter_id AND r.concept_id IN (99435, 99440, 162881) AND r.value_coded = 703) GROUP BY o.person_id";
        String numerator10 = "SELECT o.person_id FROM person p INNER JOIN obs o ON (o.concept_id = 99428 AND o.value_coded = 99430 AND p.person_id = o.person_id) INNER JOIN obs r ON (r.person_id = o.person_id AND TIMESTAMPDIFF(MONTH, p.birthdate, r.value_datetime) <= 12 AND r.concept_id = 162979) GROUP BY o.person_id";
        String numerator11 = "SELECT o.person_id FROM person p INNER JOIN obs o ON (o.concept_id = 99428 AND o.value_coded = 90306 AND p.person_id = o.person_id) INNER JOIN obs r ON (r.person_id = o.person_id AND TIMESTAMPDIFF(MONTH, p.birthdate, r.value_datetime) <= 12 AND r.concept_id = 162979) GROUP BY o.person_id";
        String numerator12 = "SELECT o.person_id FROM person p INNER JOIN obs o ON (o.concept_id = 99428 AND o.value_coded = 5240 AND p.person_id = o.person_id) INNER JOIN obs r ON (r.person_id = o.person_id AND TIMESTAMPDIFF(MONTH, p.birthdate, r.value_datetime) <= 12 AND r.concept_id = 162979) GROUP BY o.person_id";
        String numerator13 = "SELECT o.person_id FROM person p INNER JOIN obs o ON (o.concept_id = 99428 AND o.value_coded = 99112 AND p.person_id = o.person_id) INNER JOIN obs r ON (r.person_id = o.person_id AND TIMESTAMPDIFF(MONTH, p.birthdate, r.value_datetime) <= 12 AND r.concept_id = 162979) GROUP BY o.person_id";
        String numerator14 = "SELECT person_id FROM obs WHERE concept_id = 99440";
        String numerator15 = "SELECT o.person_id FROM obs o INNER JOIN obs r ON (o.concept_id = 162879 AND r.encounter_id = o.encounter_id AND r.concept_id = 162880)";
        String numerator16 = "SELECT o.person_id FROM obs o INNER JOIN obs r ON (r.encounter_id = o.encounter_id AND o.concept_id = 162879 AND r.concept_id = 162880 AND r.value_coded = 664)";
        String numerator17 = "SELECT p.person_id FROM person p INNER JOIN encounter e ON (e.patient_id = p.person_id AND e.encounter_type = (SELECT encounter_type_id FROM encounter_type WHERE uuid = '4345dacb-909d-429c-99aa-045f2db77e2b') AND e.encounter_datetime >= DATE_ADD(DATE_FORMAT(p.birthdate, '%Y-%m-01'), INTERVAL 18 MONTH) AND e.patient_id NOT IN (SELECT o.person_id FROM obs o INNER JOIN obs r ON (o.concept_id = 162879 AND r.encounter_id = o.encounter_id AND r.concept_id = 162880))) GROUP BY p.person_id";
        String numerator18 = "SELECT person_id FROM obs WHERE concept_id = 99428 AND value_coded = 90306";
        String numerator19 = "SELECT person_id FROM obs WHERE concept_id = 99428 AND value_coded = 5240";
        String numerator20 = "SELECT person_id FROM obs WHERE concept_id = 99428 AND value_coded = 99112";
        String numerator21 = "SELECT person_id FROM obs WHERE concept_id IN (99435, 99440, 162880) AND value_coded = 703 GROUP BY person_id";
        String numerator22 = "SELECT person_id FROM obs WHERE concept_id = 163004 AND value_coded = 1065";

        String denominator5 = "SELECT o.person_id FROM obs o INNER JOIN person p on(o.concept_id = 99606 AND o.person_id = p.person_id AND FLOOR(DATEDIFF(o.value_datetime, p.birthdate)/7) BETWEEN 6 AND 8) inner join obs ou on(ou.concept_id = 99435 and o.person_id = ou.person_id and ou.value_coded = 703)";
        String denominator7 = "SELECT o.person_id FROM obs o INNER JOIN person p on(o.concept_id = 99449 AND o.person_id = p.person_id and o.value_numeric = 6) inner join obs oi on(o.encounter_id = oi.encounter_id and oi.concept_id = 99451)";
        String denominator10 = "SELECT o.person_id FROM obs o WHERE o.concept_id in(99440,99435) AND o.value_coded = 703 GROUP BY o.person_id";
        String denominator14 = "SELECT person_id FROM obs WHERE concept_id = 99435 AND value_coded = 664";
        String denominator22 = "SELECT person_id FROM obs WHERE concept_id = 99797 AND value_coded = 703";

        SqlQueryBuilder numerator1Query = new SqlQueryBuilder(numerator1);
        SqlQueryBuilder numerator2Query = new SqlQueryBuilder(numerator2);
        SqlQueryBuilder numerator3Query = new SqlQueryBuilder(numerator3);
        SqlQueryBuilder numerator4Query = new SqlQueryBuilder(numerator4);
        SqlQueryBuilder numerator5Query = new SqlQueryBuilder(numerator5);
        SqlQueryBuilder numerator6Query = new SqlQueryBuilder(numerator6);
        SqlQueryBuilder numerator7Query = new SqlQueryBuilder(numerator7);
        SqlQueryBuilder numerator8Query = new SqlQueryBuilder(numerator8);
        SqlQueryBuilder numerator9Query = new SqlQueryBuilder(numerator9);
        SqlQueryBuilder numerator10Query = new SqlQueryBuilder(numerator10);
        SqlQueryBuilder numerator11Query = new SqlQueryBuilder(numerator11);
        SqlQueryBuilder numerator12Query = new SqlQueryBuilder(numerator12);
        SqlQueryBuilder numerator13Query = new SqlQueryBuilder(numerator13);
        SqlQueryBuilder numerator14Query = new SqlQueryBuilder(numerator14);
        SqlQueryBuilder numerator15Query = new SqlQueryBuilder(numerator15);
        SqlQueryBuilder numerator16Query = new SqlQueryBuilder(numerator16);
        SqlQueryBuilder numerator17Query = new SqlQueryBuilder(numerator17);
        SqlQueryBuilder numerator18Query = new SqlQueryBuilder(numerator18);
        SqlQueryBuilder numerator19Query = new SqlQueryBuilder(numerator19);
        SqlQueryBuilder numerator20Query = new SqlQueryBuilder(numerator20);
        SqlQueryBuilder numerator21Query = new SqlQueryBuilder(numerator21);
        SqlQueryBuilder numerator22Query = new SqlQueryBuilder(numerator22);

        SqlQueryBuilder denominator5Query = new SqlQueryBuilder(denominator5);
        SqlQueryBuilder denominator7Query = new SqlQueryBuilder(denominator7);
        SqlQueryBuilder denominator10Query = new SqlQueryBuilder(denominator10);
        SqlQueryBuilder denominator14Query = new SqlQueryBuilder(denominator14);
        SqlQueryBuilder denominator22Query = new SqlQueryBuilder(denominator22);

        patients.put("N1", evaluationService.evaluateToList(numerator1Query, Integer.class, context));
        patients.put("N2", evaluationService.evaluateToList(numerator2Query, Integer.class, context));
        patients.put("N3", evaluationService.evaluateToList(numerator3Query, Integer.class, context));
        patients.put("N4", evaluationService.evaluateToList(numerator4Query, Integer.class, context));
        patients.put("N5", evaluationService.evaluateToList(numerator5Query, Integer.class, context));
        patients.put("N6", evaluationService.evaluateToList(numerator6Query, Integer.class, context));
        patients.put("N7", evaluationService.evaluateToList(numerator7Query, Integer.class, context));
        patients.put("N8", evaluationService.evaluateToList(numerator8Query, Integer.class, context));
        patients.put("N9", evaluationService.evaluateToList(numerator9Query, Integer.class, context));
        patients.put("N10", evaluationService.evaluateToList(numerator10Query, Integer.class, context));
        patients.put("N11", evaluationService.evaluateToList(numerator11Query, Integer.class, context));
        patients.put("N12", evaluationService.evaluateToList(numerator12Query, Integer.class, context));
        patients.put("N13", evaluationService.evaluateToList(numerator13Query, Integer.class, context));
        patients.put("N14", evaluationService.evaluateToList(numerator14Query, Integer.class, context));
        patients.put("N15", evaluationService.evaluateToList(numerator15Query, Integer.class, context));
        patients.put("N16", evaluationService.evaluateToList(numerator16Query, Integer.class, context));
        patients.put("N17", evaluationService.evaluateToList(numerator17Query, Integer.class, context));
        patients.put("N18", evaluationService.evaluateToList(numerator18Query, Integer.class, context));
        patients.put("N19", evaluationService.evaluateToList(numerator19Query, Integer.class, context));
        patients.put("N20", evaluationService.evaluateToList(numerator20Query, Integer.class, context));
        patients.put("N21", evaluationService.evaluateToList(numerator21Query, Integer.class, context));
        patients.put("N22", evaluationService.evaluateToList(numerator22Query, Integer.class, context));

        patients.put("D5", evaluationService.evaluateToList(denominator5Query, Integer.class, context));
        patients.put("D7", evaluationService.evaluateToList(denominator7Query, Integer.class, context));
        patients.put("D10", evaluationService.evaluateToList(denominator10Query, Integer.class, context));
        patients.put("D14", evaluationService.evaluateToList(denominator14Query, Integer.class, context));
        patients.put("D22", evaluationService.evaluateToList(denominator22Query, Integer.class, context));


        return patients;

    }
}
