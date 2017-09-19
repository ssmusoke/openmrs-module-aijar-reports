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
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.CBSAdultDatasetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 */
@Handler(supports = {CBSAdultDatasetDefinition.class})
public class CBSAdultDataSetEvaluator implements DataSetEvaluator {
    @Autowired
    private EvaluationService evaluationService;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evaluationContext) throws EvaluationException {

        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, evaluationContext);
        CBSAdultDatasetDefinition definition = (CBSAdultDatasetDefinition) dataSetDefinition;

        String startDate = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");

        evaluationContext = ObjectUtil.nvl(evaluationContext, new EvaluationContext());

        PatientDataHelper pdh = new PatientDataHelper();

        String sql = "select A.patient_id,A.identifiers,B.hiv_status,C.baseline_cd4,D.date_eligible,E.art_start_date,F.current_regimen,G.ti,H.ti_location,\n" +
                "  I.ti_regimen_date,J.ti_regimen_location,K.ti_regimen,L.tout,M.tout_date,N.tout_location,O.vl_date,P.vl_qualitative,\n" +
                "  Q.vl,R.dead,S.death_date,T.missed,U.lost,V.lost_date,W.death_date,W.dead,W.birthdate,W.gender,X.preg_or_lact,\n" +
                "  Y.encounters\n" +
                "from\n" +
                "  (select patient_id,group_concat(concat(identifier,'|',identifier_type)) as identifiers from patient_identifier where identifier_type in(4,9) GROUP BY  patient_id) A\n" +
                "LEFT JOIN\n" +
                "  (select person_id,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS hiv_status from obs WHERE concept_id = 99493 group by person_id) B\n" +
                "  on(A.patient_id = B.person_id)\n" +
                "LEFT JOIN\n" +
                "(select person_id,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS baseline_cd4 from obs WHERE concept_id = 99071 group by person_id) C\n" +
                "  on(A.patient_id = C.person_id)\n" +
                "LEFT JOIN\n" +
                "(select person_id,group_concat(concat(encounter_id,'|',DATE(value_datetime))) AS date_eligible from obs WHERE concept_id = 90297 and voided = 0 group by person_id ) D\n" +
                "  on(A.patient_id = D.person_id)\n" +
                "LEFT JOIN\n" +
                "(select person_id,group_concat(concat(encounter_id,'|',DATE(value_datetime))) AS art_start_date from obs WHERE concept_id = 99161 and voided = 0 group by person_id ) E\n" +
                "  on(A.patient_id = E.person_id)\n" +
                "LEFT JOIN\n" +
                "(select person_id,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS current_regimen from obs WHERE concept_id = 90315 group by person_id) F\n" +
                "  on(A.patient_id = F.person_id)\n" +
                "LEFT JOIN\n" +
                "(select person_id,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS ti from obs WHERE concept_id = 99110 group by person_id) G\n" +
                "  on(A.patient_id = G.person_id)\n" +
                "LEFT JOIN\n" +
                "(select person_id,group_concat(concat(encounter_id,'|',value_text,'|',DATE(obs_datetime))) AS ti_location from obs WHERE concept_id = 99109 group by person_id) H\n" +
                "  on(A.patient_id = H.person_id)\n" +
                "LEFT JOIN\n" +
                "(select person_id,group_concat(concat(encounter_id,'|',DATE(value_datetime))) AS ti_regimen_date from obs WHERE concept_id = 99160 group by person_id) I\n" +
                "  on(A.patient_id = I.person_id)\n" +
                "LEFT JOIN\n" +
                "(select person_id,group_concat(concat(encounter_id,'|',value_text,'|',DATE(obs_datetime))) AS ti_regimen_location from obs WHERE concept_id = 90206 group by person_id) J\n" +
                "  on(A.patient_id = J.person_id)\n" +
                "LEFT JOIN\n" +
                "(select person_id,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS ti_regimen from obs WHERE concept_id = 99064 group by person_id) K\n" +
                "  on(A.patient_id = K.person_id)\n" +
                "LEFT JOIN\n" +
                "(select person_id,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS tout from obs WHERE concept_id = 90306 group by person_id) L\n" +
                "  on(A.patient_id = L.person_id)\n" +
                "LEFT JOIN\n" +
                "(select person_id,group_concat(concat(encounter_id,'|',DATE(value_datetime))) AS tout_date from obs WHERE concept_id = 99165 group by person_id) M\n" +
                "  on(A.patient_id = M.person_id)\n" +
                "LEFT JOIN\n" +
                "(select person_id,group_concat(concat(encounter_id,'|',value_text,'|',DATE(obs_datetime))) AS tout_location from obs WHERE concept_id = 90211 group by person_id) N\n" +
                "  on(A.patient_id = N.person_id)\n" +
                "LEFT JOIN\n" +
                "(select person_id,group_concat(concat(encounter_id,'|',DATE(value_datetime))) AS vl_date from obs WHERE concept_id = 163023 group by person_id) O\n" +
                "  on(A.patient_id = O.person_id)\n" +
                "LEFT JOIN\n" +
                "(select person_id,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS vl_qualitative from obs WHERE concept_id = 1305 group by person_id) P\n" +
                "  on(A.patient_id = P.person_id)\n" +
                "LEFT JOIN\n" +
                "(select person_id,group_concat(concat(encounter_id,'|',value_numeric,'|',DATE(obs_datetime))) AS vl from obs WHERE concept_id = 856 group by person_id) Q\n" +
                "  on(A.patient_id = Q.person_id)\n" +
                "LEFT JOIN\n" +
                "(SELECT person_id,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS dead FROM obs WHERE concept_id = 99112 group by person_id) R\n" +
                "  ON(A.patient_id = R.person_id)\n" +
                "LEFT JOIN\n" +
                "(SELECT person_id,group_concat(concat(encounter_id,'|',DATE(value_datetime),'|',DATE(obs_datetime))) AS death_date FROM obs WHERE concept_id = 90272 group by person_id) S\n" +
                "  ON(A.patient_id = S.person_id)\n" +
                "LEFT JOIN\n" +
                "(SELECT person_id,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS missed FROM obs WHERE concept_id = 99132 and value_coded = 99133  group by person_id) T\n" +
                "  ON(A.patient_id = T.person_id)\n" +
                "LEFT JOIN\n" +
                "(SELECT person_id,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS lost FROM obs WHERE concept_id = 5240 group by person_id) U\n" +
                "  ON(A.patient_id = U.person_id)\n" +
                "LEFT JOIN\n" +
                "(SELECT person_id,group_concat(concat(encounter_id,'|',DATE(value_datetime),'|',DATE(obs_datetime))) AS lost_date FROM obs WHERE concept_id = 90209 group by person_id) V\n" +
                "  ON(A.patient_id = V.person_id)\n" +
                "LEFT JOIN\n" +
                "(SELECT person_id,gender,birthdate,dead,death_date FROM person group by person_id) W\n" +
                "  ON (A.patient_id = W.person_id)\n" +
                "LEFT JOIN\n" +
                "(SELECT person_id,group_concat(concat(encounter_id,'|',value_coded,'|',DATE(obs_datetime))) AS preg_or_lact FROM obs WHERE concept_id in(99072,99603) group by person_id) X\n" +
                "  ON(A.patient_id = X.person_id)\n" +
                "LEFT JOIN\n" +
                "(SELECT patient_id,group_concat(concat(encounter_id,'|',encounter_type,'|',DATE(encounter_datetime))) as encounters FROM encounter group by patient_id) Y\n" +
                "  ON (A.patient_id = Y.patient_id)";

        SqlQueryBuilder q = new SqlQueryBuilder(sql);

        List<Object[]> results = evaluationService.evaluateToList(q, evaluationContext);

        for (Object[] r : results) {
            DataSetRow row = new DataSetRow();

            pdh.addCol(row, "Name", r[1]);
        }

        return null;
    }
}
