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

        String sql = "SELECT A.person_id,A.value_datetime,B.cd4,C.ti,D.tout,E.to_date,F.stopped,G.stopped_date,H.dead,I.death_date,J.missed,K.lost,L.lost_date,M.dead,M.death_date,M.gender,N.preg_or_lact,O.regimen\n" +
                "FROM\n" +
                "  (SELECT person_id,value_datetime FROM obs WHERE concept_id = 99161 AND value_datetime BETWEEN DATE_ADD('2016-01-01', INTERVAL - 72 MONTH) AND DATE_ADD('2016-03-31', INTERVAL - 6 MONTH)) A\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,group_concat(concat(encounter_id,'|',value_numeric,'|',DATE(obs_datetime))) AS cd4 FROM obs WHERE concept_id = 99071 group by person_id) B\n" +
                "    ON(A.person_id = B.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS ti FROM obs WHERE concept_id = 99110 group by person_id) C\n" +
                "    ON(A.person_id = C.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS tout FROM obs WHERE concept_id = 90306 group by person_id) D\n" +
                "    ON(A.person_id = D.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,group_concat(concat(encounter_id,'|',DATE(value_datetime),'|',DATE(obs_datetime))) AS to_date FROM obs WHERE concept_id = 99165 group by person_id) E\n" +
                "    ON(A.person_id = E.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS stopped FROM obs WHERE concept_id = 99132 and value_coded = 1363 group by person_id) F\n" +
                "    ON(A.person_id = F.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,group_concat(concat(encounter_id,'|',DATE(value_datetime),'|',DATE(obs_datetime))) AS stopped_date FROM obs WHERE concept_id = 99084 group by person_id) G\n" +
                "    ON(A.person_id = G.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS dead FROM obs WHERE concept_id = 99112 group by person_id) H\n" +
                "    ON(A.person_id = H.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,group_concat(concat(encounter_id,'|',DATE(value_datetime),'|',DATE(obs_datetime))) AS death_date FROM obs WHERE concept_id = 90272 group by person_id) I\n" +
                "    ON(A.person_id = I.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS missed FROM obs WHERE concept_id = 99132 and value_coded = 99133  group by person_id) J\n" +
                "    ON(A.person_id = J.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS lost FROM obs WHERE concept_id = 5240 group by person_id) K\n" +
                "    ON(A.person_id = K.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,group_concat(concat(encounter_id,'|',DATE(value_datetime),'|',DATE(obs_datetime))) AS lost_date FROM obs WHERE concept_id = 90209 group by person_id) L\n" +
                "    ON(A.person_id = L.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,gender,birthdate,dead,death_date FROM person group by person_id) M\n" +
                "    ON (A.person_id = M.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS preg_or_lact FROM obs WHERE concept_id in(99072,99603) group by person_id) N\n" +
                "    ON(A.person_id = N.person_id)\n" +
                "  LEFT JOIN\n" +
                "  (SELECT person_id,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS regimen FROM obs WHERE concept_id = 90315 group by person_id) O\n" +
                "    ON(A.person_id = O.person_id)";

        SqlQueryBuilder q = new SqlQueryBuilder(sql);

        List<Object[]> results = evaluationService.evaluateToList(q, evaluationContext);

        for (Object[] r : results) {
            DataSetRow row = new DataSetRow();

            pdh.addCol(row, "Name", r[1]);
        }

        return null;
    }
}
