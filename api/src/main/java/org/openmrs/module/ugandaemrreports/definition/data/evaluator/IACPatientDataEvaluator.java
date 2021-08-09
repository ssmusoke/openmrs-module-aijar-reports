package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
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
import org.openmrs.module.ugandaemrreports.common.Adherence;
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.definition.data.definition.AdherencePatientDataDefinition;
import org.openmrs.module.ugandaemrreports.definition.data.definition.FUStatusPatientDataDefinition;
import org.openmrs.module.ugandaemrreports.definition.data.definition.IACPatientDataDefinition;
import org.openmrs.module.ugandaemrreports.library.HIVPatientDataLibrary;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;

/**
 */
@Handler(supports = IACPatientDataDefinition.class, order = 50)
public class IACPatientDataEvaluator implements PatientDataEvaluator {
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
        IACPatientDataDefinition def = (IACPatientDataDefinition) definition;

        EvaluatedPatientData c = new EvaluatedPatientData(def, context);

        if (context.getBaseCohort() != null && context.getBaseCohort().isEmpty()) {
            return c;
        }


        Map<Integer, Date> m = new HashMap<Integer, Date>();

        String startDate = DateUtil.formatDate(def.getStartDate(), "yyyy-MM-dd");

        String query = "select A.person_id, A.value_datetime, B.value_coded,A.obs_datetime from obs A left join obs B on A.encounter_id =B.encounter_id where A.concept_id = 163167  and A.voided=0 and B.concept_id=163170 and B.voided=0 and A.obs_datetime >='"+startDate+"'";

        SqlQueryBuilder q = new SqlQueryBuilder(query);

        List<Object[]> results = evaluationService.evaluateToList(q, context);


        List<Obs> obs = new ArrayList<>();

        for (Object[] row : results) {
            Obs o = new Obs();
            String patientId = String.valueOf(row[0]);

            o.setValueCoded(Context.getConceptService().getConcept(Integer.parseInt(String.valueOf(row[2]))));
            o.setValueDate(DateUtil.parseDate(String.valueOf(row[1]),"yyyy-MM-dd"));
            o.setPerson(Context.getPersonService().getPerson(Integer.parseInt(patientId)));

            o.setObsDatetime(DateUtil.parseDate(String.valueOf(row[3]),"yyyy-MM-dd"));
            obs.add(o);
        }

        Map<Integer, List<Obs>> grouped = obs.stream().collect(groupingBy(Obs::getPersonId));

        for (Map.Entry<Integer, List<Obs>> d : grouped.entrySet()) {
            c.addData(Integer.valueOf(d.getKey()), d.getValue());
        }

        return c;
    }

}
