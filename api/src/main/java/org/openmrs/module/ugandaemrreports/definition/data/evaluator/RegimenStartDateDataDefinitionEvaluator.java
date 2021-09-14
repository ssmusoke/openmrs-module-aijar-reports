package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.evaluator.PatientDataEvaluator;
import org.openmrs.module.reporting.data.patient.evaluator.SqlPatientDataEvaluator;
import org.openmrs.module.reporting.data.patient.service.PatientDataService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.DSDMModel;
import org.openmrs.module.ugandaemrreports.data.converter.ObsDataConverter;
import org.openmrs.module.ugandaemrreports.definition.data.definition.RegimenStartDateDataDefinition;
import org.openmrs.module.ugandaemrreports.library.HIVPatientDataLibrary;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 */
@Handler(supports = RegimenStartDateDataDefinition.class, order = 50)
public class RegimenStartDateDataDefinitionEvaluator implements PatientDataEvaluator {
    protected static final Log log = LogFactory.getLog(RegimenStartDateDataDefinition.class);

    @Autowired
    private HIVPatientDataLibrary hivLibrary;

    @Autowired
    private PatientDataService patientDataService;

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private SqlPatientDataEvaluator sqlPatientDataEvaluator;

    @Autowired
    private HIVMetadata hivMetadata;

    @Override
    public EvaluatedPatientData evaluate(PatientDataDefinition definition, EvaluationContext context) throws EvaluationException {
        RegimenStartDateDataDefinition def = (RegimenStartDateDataDefinition) definition;

        EvaluatedPatientData c = new EvaluatedPatientData(def, context);
        if (context.getBaseCohort() != null && context.getBaseCohort().isEmpty()) {
            return c;
        }

        String startDate = DateUtil.formatDate(def.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(def.getEndDate(), "yyyy-MM-dd");

        String query = "SELECT o.person_id,MIN(o.obs_datetime) regimen_start_date from obs o inner join\n" +
                "(SELECT A.person_id, value_coded from obs inner join\n" +
                "(SELECT person_id,max(obs_datetime)latest_date from obs where\n" +
                "  concept_id=90315 and voided=0 and obs_datetime between '"+ startDate+"' and '"+ endDate +"' group by person_id)A\n" +
                "  on obs.person_id = A.person_id where obs.concept_id=90315 and obs_datetime =A.latest_date and obs.voided=0)B on o.person_id=B.person_id\n" +
                "where concept_id=90315 and o.value_coded=B.value_coded group by o.person_id";


        SqlQueryBuilder q = new SqlQueryBuilder(query);

        List<Object[]> results = evaluationService.evaluateToList(q, context);


        for (Object[] row : results) {
            Integer patientId = Integer.valueOf(String.valueOf(row[0]));
            Date obs_datetime = (Date) (row[1]) ;
            Obs o = new Obs();

            if(obs_datetime!=null)
            {
                o.setObsDatetime(obs_datetime);
                c.addData(patientId, o);
            }
            else
            {
                c.addData(patientId,null);
            }
        }

        return c;
    }

}
