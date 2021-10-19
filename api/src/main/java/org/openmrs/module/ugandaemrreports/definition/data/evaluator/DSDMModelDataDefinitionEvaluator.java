package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.openmrs.module.ugandaemrreports.definition.data.definition.DSDMModelDataDefinition;
import org.openmrs.module.ugandaemrreports.definition.data.definition.FUStatusPatientDataDefinition;
import org.openmrs.module.ugandaemrreports.library.HIVPatientDataLibrary;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 */
@Handler(supports = DSDMModelDataDefinition.class, order = 50)
public class DSDMModelDataDefinitionEvaluator implements PatientDataEvaluator {
    protected static final Log log = LogFactory.getLog(FUStatusPatientDataDefinition.class);

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
        DSDMModelDataDefinition def = (DSDMModelDataDefinition) definition;

        EvaluatedPatientData c = new EvaluatedPatientData(def, context);
        if (context.getBaseCohort() != null && context.getBaseCohort().isEmpty()) {
            return c;
        }

        String endDate = DateUtil.formatDate(def.getEndDate(), "yyyy-MM-dd");

        String query = "Select pg.patient_id, p.name,pg.date_enrolled,pg.date_completed\n" +
                "             from patient_program as pg\n" +
                "             Inner join program p on p.program_id=pg.program_id\n" +
                "where ((pg.date_completed > '"+endDate+"') OR date_completed IS NULL) and p.uuid in ('de5d54ae-c304-11e8-9ad0-529269fb1459',\n" +
                "    'de5d5b34-c304-11e8-9ad0-529269fb1459',\n" +
                "    'de5d5896-c304-11e8-9ad0-529269fb1459',\n" +
                "    'de5d5da0-c304-11e8-9ad0-529269fb1459',\n" +
                "    'de5d6034-c304-11e8-9ad0-529269fb1459')  group by pg.patient_id";


        SqlQueryBuilder q = new SqlQueryBuilder(query);

        List<Object[]> results = evaluationService.evaluateToList(q, context);


        for (Object[] row : results) {
            Integer patientId = Integer.valueOf(String.valueOf(row[0]));
            String progId = String.valueOf(row[1]);
            Date obs_datetime = (Date) (row[2]) ;
            if(obs_datetime!=null)
            {
                c.addData(patientId, new DSDMModel(obs_datetime,progId));
            }
            else
            {
                c.addData(patientId,null);
            }
        }

        return c;
    }

}
