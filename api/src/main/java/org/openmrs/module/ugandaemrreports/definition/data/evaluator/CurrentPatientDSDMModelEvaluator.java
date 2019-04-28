package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.data.patient.evaluator.SqlPatientDataEvaluator;
import org.openmrs.module.reporting.data.patient.service.PatientDataService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.CurrentPatientDSDMModelCohortDefinition;
import org.openmrs.module.ugandaemrreports.definition.data.definition.FUStatusPatientDataDefinition;
import org.openmrs.module.ugandaemrreports.library.HIVPatientDataLibrary;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 */
@Handler(supports = CurrentPatientDSDMModelCohortDefinition.class, order = 50)
public class CurrentPatientDSDMModelEvaluator implements CohortDefinitionEvaluator {
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
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
        CurrentPatientDSDMModelCohortDefinition def = (CurrentPatientDSDMModelCohortDefinition) cohortDefinition;

        EvaluatedCohort c = new EvaluatedCohort(def, context);
        String startDate = DateUtil.formatDate(def.getStartDate(), "yyyy-MM-dd");
        String enddate = DateUtil.formatDate(def.getEndDate(), "yyyy-MM-dd");

        String query = String.format("Select pg.patient_id from patient_program pg\n" +
                "inner join program p on p.program_id = pg.program_id\n" +
                "inner join encounter e on e.patient_id = pg.patient_id\n" +
                "WHERE p.uuid NOT IN ('19cfd5dd-927f-44ad-8be2-80d8dc5c337d','37811e6c-819f-4e1c-b9cd-89fbd39f4bd4')\n" +
                "AND e.encounter_datetime between '%s'  and '%s'",startDate,enddate);
        SqlQueryBuilder q = new SqlQueryBuilder(query);


        List<Integer> patients = evaluationService.evaluateToList(q,Integer.class, context);
        for (Integer p:patients)
        {
            System.out.println(p);
            c.addMember(p);
        }

        return c;
    }

}
