package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.LocalDate;
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
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.EIDCohortDataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.openmrs.module.ugandaemrreports.common.Periods.monthStartFor;

/**
 * Created by carapai on 11/05/2016.
 */
@Handler(supports = {EIDCohortDataSetDefinition.class})
public class EIDCohortDataSetEvaluator implements DataSetEvaluator {

    @Autowired
    private EvaluationService evaluationService;

    private String[] targetNumbers = {
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


    private String[] targetSigns = {
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

    @Override
    public MapDataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        MapDataSet dataSet = new MapDataSet(dataSetDefinition, context);
        EIDCohortDataSetDefinition definition = (EIDCohortDataSetDefinition) dataSetDefinition;

        String date = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
        String year = DateUtil.formatDate(definition.getStartDate(), "yyyy");
        int totalMonths = 12;

        List<String> months;

        String baseSql = "SELECT\n" +
                "  p.person_id,\n" +
                "  p.birthdate,\n" +
                "  EXTRACT(YEAR_MONTH FROM p.birthdate)          AS birthmonth,\n" +
                "  e.encounter_datetime        AS date_chart_opened,\n" +
                "  MONTH(e.encounter_datetime) AS month_chart_opened,\n" +
                "  p.death_date\n" +
                "FROM person p INNER JOIN encounter e ON (e.patient_id = p.person_id AND e.encounter_type = (SELECT encounter_type_id\n" +
                "                                                                                            FROM encounter_type\n" +
                "                                                                                            WHERE uuid =\n" +
                "                                                                                                  '9fcfcc91-ad60-4d84-9710-11cc25258719')\n";
        if (definition.getYearly()) {
            baseSql += String.format("                                         AND YEAR('%s') = YEAR(p.birthdate))", date);
            months = Arrays.asList(year + "00", year + "01", year + "02", year + "03", year + "04", year + "05", year + "06", year + "07", year + "08", year + "09", year + "100", year + "11", year + "12");
            dataSet.addData(new DataSetColumn("Y", "Y", Integer.class), Integer.valueOf(year));
        } else {

            LocalDate localDate = StubDate.dateOf(definition.getStartDate());
            LocalDate workingDate = monthStartFor(localDate);

            LocalDate localDate1 = monthStartFor(workingDate.minusMonths(12));
            LocalDate localDate2 = monthStartFor(workingDate.minusMonths(24));

            String date1 = DateUtil.formatDate(localDate1.toDate(), "yyyyMM");
            String date2 = DateUtil.formatDate(localDate2.toDate(), "yyyyMM");

            String allDates = date1 + "," + date2;

            months = Arrays.asList("0", date1, date2);

            baseSql += String.format("                                         AND EXTRACT(YEAR_MONTH FROM p.birthdate) IN(%s))", allDates);
            totalMonths = 2;


            dataSet.addData(new DataSetColumn("M", "M", String.class), localDate1.toString("MMM"));
            dataSet.addData(new DataSetColumn("Y1", "Y1", Integer.class), localDate1.getYear());
            dataSet.addData(new DataSetColumn("Y2", "Y2", Integer.class), localDate2.getYear());


        }

        SqlQueryBuilder q = new SqlQueryBuilder(baseSql);
        List<Object[]> results = evaluationService.evaluateToList(q, context);

        Multimap<String, Integer> allClients = ArrayListMultimap.create();

        Map<String, Collection<Integer>> numeratorsAndDenominators = getPatients(context);


        for (Object[] o : results) {
            allClients.put(String.valueOf(o[2]), (Integer) o[0]);
        }


        Collection<Integer> allClientsCohort = allClients.values();

        for (int i = 0; i < 22; i++) {

            Collection<Integer> currentNumerator = numeratorsAndDenominators.get("N" + String.valueOf(i + 1));
            Collection<Integer> currentDenominator = numeratorsAndDenominators.get("D" + String.valueOf(i + 1));

            String numerator = "-";
            String denominator;

            for (int j = 1; j <= totalMonths; j++) {

                Collection<Integer> currentCohort = allClients.get(months.get(j));

                String uniqueNumeratorString = "-";
                String uniqueDenominatorString;

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

                dataSet.addData(new DataSetColumn("N" + String.valueOf(i) + String.valueOf(j), "N" + String.valueOf(i) + String.valueOf(j), String.class), uniqueNumeratorString);
                dataSet.addData(new DataSetColumn("D" + String.valueOf(i) + String.valueOf(j), "D" + String.valueOf(i) + String.valueOf(j), String.class), uniqueDenominatorString);

                List<String> targets = addTarget(uniqueDenominatorString, uniqueNumeratorString, i);
                dataSet.addData(new DataSetColumn("P" + String.valueOf(i) + String.valueOf(j), "P" + String.valueOf(i) + String.valueOf(j), String.class), targets.get(0));
                dataSet.addData(new DataSetColumn("T" + String.valueOf(i) + String.valueOf(j), "T" + String.valueOf(i) + String.valueOf(j), String.class), targets.get(1));
            }
            if (definition.getYearly()) {
                if (currentNumerator != null) {
                    Collection uniqueNumerator = CollectionUtils.intersection(allClientsCohort, currentNumerator);
                    numerator = String.valueOf(uniqueNumerator.size());
                }

                if (currentDenominator != null) {
                    Collection uniqueDenominator = CollectionUtils.intersection(allClientsCohort, currentDenominator);
                    denominator = String.valueOf(uniqueDenominator.size());
                } else {
                    denominator = String.valueOf(allClientsCohort.size());
                }

                dataSet.addData(new DataSetColumn("TN" + String.valueOf(i), "TN" + String.valueOf(i), String.class), numerator);
                dataSet.addData(new DataSetColumn("TD" + String.valueOf(i), "TD" + String.valueOf(i), String.class), denominator);

                List<String> targets = addTarget(denominator, numerator, i);
                dataSet.addData(new DataSetColumn("TP" + String.valueOf(i), "TP" + String.valueOf(i), String.class), targets.get(0));
                dataSet.addData(new DataSetColumn("TT" + String.valueOf(i), "TT" + String.valueOf(i), String.class), targets.get(1));
            }
        }
        return dataSet;
    }

    private List<String> addTarget(String uniqueDenominatorString, String uniqueNumeratorString, Integer i) {
        List<String> result = new ArrayList<String>();
        if (uniqueDenominatorString.equalsIgnoreCase("-") || uniqueNumeratorString.equalsIgnoreCase("-")) {
            result.add("-");
            result.add("-");

        } else {
            Integer numerator = Integer.valueOf(uniqueNumeratorString);
            Integer denominator = Integer.valueOf(uniqueDenominatorString);

            if (denominator != 0) {
                Integer realPercentage = (numerator * 100) / denominator;
                result.add(String.valueOf(realPercentage));

                if (targetNumbers[i].equalsIgnoreCase("NA")) {
                    result.add("-");
                } else {

                    Integer targetPercentage = Integer.valueOf(targetNumbers[i]);

                    if (targetSigns[i].equalsIgnoreCase("=") && realPercentage.intValue() == targetPercentage.intValue()) {
                        result.add("Y");
                    } else if (targetSigns[i].equalsIgnoreCase("<") && realPercentage < targetPercentage) {
                        result.add("Y");
                    } else if (targetSigns[i].equalsIgnoreCase("<=") && realPercentage <= targetPercentage) {
                        result.add("Y");
                    } else if (targetSigns[i].equalsIgnoreCase(">") && realPercentage > targetPercentage) {
                        result.add("Y");
                    } else if (targetSigns[i].equalsIgnoreCase(">=") && realPercentage >= targetPercentage) {
                        result.add("Y");
                    } else {
                        result.add("N");
                    }
                }
            } else {
                result.add("-");
                result.add("-");
            }

        }
        return result;
    }

    private Map<String, Collection<Integer>> getPatients(EvaluationContext context) {
        Map<String, Collection<Integer>> patients = new HashMap<String, Collection<Integer>>();

        String numerator1 = "SELECT person_id AS number FROM obs WHERE concept_id = 162872 AND value_coded = 90003";
        String numerator2 = "SELECT person_id AS number FROM obs WHERE concept_id = 99787 AND value_coded IN (162966,99788)";
        String numerator3 = "SELECT p.person_id AS number FROM obs o INNER JOIN person p on( o.person_id = p.person_id) WHERE o.concept_id = 99606 AND FLOOR(DATEDIFF(o.value_datetime, p.birthdate)/7) BETWEEN 6 AND 8";
        String numerator4 = "SELECT o.person_id FROM obs o INNER JOIN person p on(o.person_id = p.person_id) INNER JOIN obs oi on(oi.person_id = o.person_id) WHERE o.concept_id = 99606 AND oi.concept_id = 99438 AND FLOOR(DATEDIFF(o.value_datetime, p.birthdate)/7) BETWEEN 6 AND 8";
        String numerator5 = "SELECT o.person_id FROM obs o INNER JOIN person p on(o.person_id = p.person_id) INNER JOIN obs oi on( oi.person_id = o.person_id) INNER JOIN obs ou on(o.person_id = ou.person_id ) WHERE o.concept_id = 99606 AND oi.concept_id = 99438 AND ou.concept_id = 99435 AND FLOOR(DATEDIFF(o.value_datetime, p.birthdate)/7) BETWEEN 6 AND 8 AND ou.value_coded = 703 ";
        String numerator6 = "SELECT o.person_id FROM obs o INNER JOIN person p on(o.person_id = p.person_id) WHERE  o.concept_id = 99773 AND FLOOR(DATEDIFF(o.value_datetime, p.birthdate)/7) BETWEEN 6 AND 8";
        String numerator7 = "SELECT o.person_id FROM obs o INNER JOIN person p on(o.person_id = p.person_id) inner join obs oi on(o.encounter_id = oi.encounter_id ) WHERE o.concept_id = 99449 AND oi.concept_id = 99451 AND o.value_numeric = 6  AND oi.value_coded = 5526";
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
