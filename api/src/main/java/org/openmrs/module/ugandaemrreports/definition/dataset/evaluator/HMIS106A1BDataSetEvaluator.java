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
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.HMIS106A1BDataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by carapai on 17/10/2016.
 */
@Handler(supports = {HMIS106A1BDataSetDefinition.class})
public class HMIS106A1BDataSetEvaluator implements DataSetEvaluator {
    @Autowired
    private EvaluationService evaluationService;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evaluationContext) throws EvaluationException {

        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, evaluationContext);
        HMIS106A1BDataSetDefinition definition = (HMIS106A1BDataSetDefinition) dataSetDefinition;

        String startDate = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");

        evaluationContext = ObjectUtil.nvl(evaluationContext, new EvaluationContext());

        PatientDataHelper pdh = new PatientDataHelper();

        String sql = "SELECT\n" +
                "  A.person_id,\n" +
                "  A.dt,\n" +
                "  B.cd4,\n" +
                "  C.ti,\n" +
                "  D.tout,\n" +
                "  E.to_date,\n" +
                "  F.stopped,\n" +
                "  G.stopped_date,\n" +
                "  H.dead,\n" +
                "  I.death_date,\n" +
                "  J.missed,\n" +
                "  K.lost,\n" +
                "  L.lost_date,\n" +
                "  M.dead,\n" +
                "  M.death_date,\n" +
                "  M.gender,\n" +
                "  N.preg_or_lact,\n" +
                "  O.regimen\n" +
                "FROM\n" +
                "  (SELECT\n" +
                "     F.person_id,\n" +
                "     F.dt\n" +
                "   FROM (SELECT\n" +
                "           C.person_id,\n" +
                "           e.encounter_datetime AS dt\n" +
                "         FROM (SELECT\n" +
                "                 A.person_id,\n" +
                "                 B.encounter_id\n" +
                "               FROM (SELECT person_id\n" +
                "                     FROM obs\n" +
                "                     WHERE person_id NOT IN (SELECT person_id\n" +
                "                                             FROM obs\n" +
                "                                             WHERE concept_id = 99161)\n" +
                "                     GROUP BY person_id) A INNER JOIN (SELECT\n" +
                "                                                         person_id,\n" +
                "                                                         min(encounter_id) AS encounter_id\n" +
                "                                                       FROM obs\n" +
                "                                                       WHERE concept_id = 90315\n" +
                "                                                       GROUP BY person_id) B\n" +
                "                   ON (A.person_id = B.person_id)) C INNER JOIN encounter e ON (e.encounter_id = C.encounter_id)\n" +
                "         UNION ALL SELECT\n" +
                "                     person_id,\n" +
                "                     value_datetime AS dt\n" +
                "                   FROM obs\n" +
                "                   WHERE concept_id = 99161) F\n" +
                "   WHERE F.dt BETWEEN DATE_ADD('2016-01-01', INTERVAL -72 MONTH) AND DATE_ADD('2016-03-31', INTERVAL -6 MONTH)) A\n" +
                "  LEFT JOIN\n" +
                "  (SELECT\n" +
                "     person_id,\n" +
                "     group_concat(concat(encounter_id, '|', value_numeric, '|', DATE(obs_datetime))) AS cd4\n" +
                "   FROM obs\n" +
                "   WHERE concept_id = 99071\n" +
                "   GROUP BY person_id) B\n" +
                "    ON (A.person_id = B.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT\n" +
                "     person_id,\n" +
                "     group_concat(concat(encounter_id, '|', value_coded, '|', DATE(obs_datetime))) AS ti\n" +
                "   FROM obs\n" +
                "   WHERE concept_id = 99110\n" +
                "   GROUP BY person_id) C\n" +
                "    ON (A.person_id = C.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT\n" +
                "     person_id,\n" +
                "     group_concat(concat(encounter_id, '|', value_coded, '|', DATE(obs_datetime))) AS tout\n" +
                "   FROM obs\n" +
                "   WHERE concept_id = 90306\n" +
                "   GROUP BY person_id) D\n" +
                "    ON (A.person_id = D.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT\n" +
                "     person_id,\n" +
                "     group_concat(concat(encounter_id, '|', DATE(value_datetime), '|', DATE(obs_datetime))) AS to_date\n" +
                "   FROM obs\n" +
                "   WHERE concept_id = 99165\n" +
                "   GROUP BY person_id) E\n" +
                "    ON (A.person_id = E.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT\n" +
                "     person_id,\n" +
                "     group_concat(concat(encounter_id, '|', value_coded, '|', DATE(obs_datetime))) AS stopped\n" +
                "   FROM obs\n" +
                "   WHERE concept_id = 99132 AND value_coded = 1363\n" +
                "   GROUP BY person_id) F\n" +
                "    ON (A.person_id = F.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT\n" +
                "     person_id,\n" +
                "     group_concat(concat(encounter_id, '|', DATE(value_datetime), '|', DATE(obs_datetime))) AS stopped_date\n" +
                "   FROM obs\n" +
                "   WHERE concept_id = 99084\n" +
                "   GROUP BY person_id) G\n" +
                "    ON (A.person_id = G.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT\n" +
                "     person_id,\n" +
                "     group_concat(concat(encounter_id, '|', value_coded, '|', DATE(obs_datetime))) AS dead\n" +
                "   FROM obs\n" +
                "   WHERE concept_id = 99112\n" +
                "   GROUP BY person_id) H\n" +
                "    ON (A.person_id = H.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT\n" +
                "     person_id,\n" +
                "     group_concat(concat(encounter_id, '|', DATE(value_datetime), '|', DATE(obs_datetime))) AS death_date\n" +
                "   FROM obs\n" +
                "   WHERE concept_id = 90272\n" +
                "   GROUP BY person_id) I\n" +
                "    ON (A.person_id = I.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT\n" +
                "     person_id,\n" +
                "     group_concat(concat(encounter_id, '|', value_coded, '|', DATE(obs_datetime))) AS missed\n" +
                "   FROM obs\n" +
                "   WHERE concept_id = 99132 AND value_coded = 99133\n" +
                "   GROUP BY person_id) J\n" +
                "    ON (A.person_id = J.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT\n" +
                "     person_id,\n" +
                "     group_concat(concat(encounter_id, '|', value_coded, '|', DATE(obs_datetime))) AS lost\n" +
                "   FROM obs\n" +
                "   WHERE concept_id = 5240\n" +
                "   GROUP BY person_id) K\n" +
                "    ON (A.person_id = K.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT\n" +
                "     person_id,\n" +
                "     group_concat(concat(encounter_id, '|', DATE(value_datetime), '|', DATE(obs_datetime))) AS lost_date\n" +
                "   FROM obs\n" +
                "   WHERE concept_id = 90209\n" +
                "   GROUP BY person_id) L\n" +
                "    ON (A.person_id = L.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT\n" +
                "     person_id,\n" +
                "     gender,\n" +
                "     birthdate,\n" +
                "     dead,\n" +
                "     death_date\n" +
                "   FROM person\n" +
                "   GROUP BY person_id) M\n" +
                "    ON (A.person_id = M.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT\n" +
                "     person_id,\n" +
                "     group_concat(concat(encounter_id, '|', value_coded, '|', DATE(obs_datetime))) AS preg_or_lact\n" +
                "   FROM obs\n" +
                "   WHERE concept_id IN (99072, 99603)\n" +
                "   GROUP BY person_id) N\n" +
                "    ON (A.person_id = N.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT\n" +
                "     person_id,\n" +
                "     group_concat(concat(encounter_id, '|', value_coded, '|', DATE(obs_datetime))) AS regimen\n" +
                "   FROM obs\n" +
                "   WHERE concept_id = 90315\n" +
                "   GROUP BY person_id) O\n" +
                "    ON (A.person_id = O.person_id)";

        SqlQueryBuilder q = new SqlQueryBuilder(sql);

        List<Object[]> results = evaluationService.evaluateToList(q, evaluationContext);

        for (Object[] r : results) {
            DataSetRow row = new DataSetRow();

            pdh.addCol(row, "Name", r[1]);
        }

        return null;
    }
}
