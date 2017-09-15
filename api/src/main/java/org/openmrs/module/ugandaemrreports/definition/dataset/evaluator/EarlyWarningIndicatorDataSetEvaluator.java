package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
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
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.EarlyWarningIndicatorsDatasetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

import static org.openmrs.module.ugandaemrreports.common.Periods.monthStartFor;

/**
 * Created by carapai on 11/05/2016.
 */
@Handler(supports = {EarlyWarningIndicatorsDatasetDefinition.class})
public class EarlyWarningIndicatorDataSetEvaluator implements DataSetEvaluator {

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public MapDataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        MapDataSet dataSet = new MapDataSet(dataSetDefinition, context);
        EarlyWarningIndicatorsDatasetDefinition definition = (EarlyWarningIndicatorsDatasetDefinition) dataSetDefinition;

        String startDate = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");


        String startedArtDuringPeriodSql = String.format("SELECT\n" +
                "  person_id,\n" +
                "  DATE(value_datetime)\n" +
                "FROM obs\n" +
                "WHERE concept_id = 99161 AND value_datetime BETWEEN '%s' AND '%s';", startDate, endDate);

        SqlQueryBuilder q = new SqlQueryBuilder(startedArtDuringPeriodSql);
        Map<Integer, Date> results = evaluationService.evaluateToMap(q, Integer.class, Date.class, context);
        Integer startedThisPeriod = results.keySet().size();
        String ind1 = "0";
        String ind2 = "0";
        String p1 = "-";
        String p2 = "-";

        String patients = Joiner.on(",").join(results.keySet());

        if (results.size() > 0) {

            String encounterAppointmentSql = "SELECT\n" +
                    "  patient_id,\n" +
                    "  GROUP_CONCAT(DATEDIFF(encounter_datetime, (SELECT MAX(DATE(value_datetime))\n" +
                    "                                             FROM obs o\n" +
                    "                                             WHERE o.person_id = e.patient_id AND o.concept_id = 5096 AND\n" +
                    "                                                   e.encounter_id > o.encounter_id AND o.voided = 0)) ORDER BY\n" +
                    "               e.encounter_id) AS appointemnts\n" +
                    "FROM encounter e\n" +
                    String.format("WHERE e.encounter_type = 15 AND e.voided = 0 AND e.patient_id IN (%s) AND\n", patients) +
                    "      e.encounter_datetime >= (SELECT DATE(art.value_datetime)\n" +
                    "                               FROM obs art\n" +
                    "                               WHERE e.patient_id = art.person_id AND\n" +
                    "                                     art.concept_id = 99161 AND\n" +
                    "                                     art.voided = 0 LIMIT 1)\n" +
                    "      AND e.encounter_datetime <= (SELECT DATE_ADD(add_art.value_datetime, INTERVAL 1 YEAR)\n" +
                    "                                   FROM obs add_art\n" +
                    "                                   WHERE e.patient_id = add_art.person_id AND\n" +
                    "                                         add_art.concept_id = 99161 AND\n" +
                    "                                         add_art.voided = 0 LIMIT 1)\n" +
                    "GROUP BY e.patient_id;";

            SqlQueryBuilder encounterAppointmentQueryBuilder = new SqlQueryBuilder(encounterAppointmentSql);
            Map<Integer, String> encounterAppointments = evaluationService.evaluateToMap(encounterAppointmentQueryBuilder, Integer.class, String.class, context);

            List<Integer> didNotMissAnyAppointment = new ArrayList<>();
            List<Integer> lastBy12Months = new ArrayList<>();

            for (Map.Entry<Integer, String> records : encounterAppointments.entrySet()) {
                if (records.getValue() != null) {
                    List<Integer> days = Splitter.on(",").splitToList(records.getValue()).stream().map(Integer::parseInt).collect(Collectors.toList());

                    if (days.stream().allMatch(i -> i <= 7)) {
                        didNotMissAnyAppointment.add(records.getKey());
                    }

                    if (days.stream().anyMatch(i -> i > 89)) {
                        lastBy12Months.add(records.getKey());
                    }
                }
            }

            ind1 = String.valueOf(didNotMissAnyAppointment.size());
            ind2 = String.valueOf(lastBy12Months.size());
            p1 = String.valueOf(didNotMissAnyAppointment.size() * 1.0 / startedThisPeriod);
            p2 = String.valueOf(lastBy12Months.size() * 1.0 / startedThisPeriod);


        }
        dataSet.addData(new DataSetColumn("IND1", "IND1", String.class), startedThisPeriod);

        dataSet.addData(new DataSetColumn("IND2", "IND2", String.class), ind1);
        dataSet.addData(new DataSetColumn("P1", "P1", String.class), p1);

        dataSet.addData(new DataSetColumn("IND3", "IND3", String.class), ind2);
        dataSet.addData(new DataSetColumn("P2", "P2", String.class), p2);
        return dataSet;
    }

}
