package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.evaluator.PatientDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.Adherence;
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.definition.data.definition.AdherencePatientDataDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import static java.util.stream.Collectors.groupingBy;

/**
 */
@Handler(supports = AdherencePatientDataDefinition.class, order = 50)
public class AdherencePatientDataEvaluator implements PatientDataEvaluator {
    protected static final Log log = LogFactory.getLog(AdherencePatientDataDefinition.class);

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public EvaluatedPatientData evaluate(PatientDataDefinition definition, EvaluationContext context) throws EvaluationException {
        AdherencePatientDataDefinition def = (AdherencePatientDataDefinition) definition;

        EvaluatedPatientData c = new EvaluatedPatientData(def, context);

        if (context.getBaseCohort() != null && context.getBaseCohort().isEmpty()) {
            return c;
        }


        Map<Integer, Date> m = new HashMap<Integer, Date>();

        String startDate = DateUtil.formatDate(def.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(def.getEndDate(), "yyyy-MM-dd");

        String query = String.format("select o.person_id,cn.name,o.obs_datetime  from obs o\n" +
                "inner join  (concept_name cn) on o.value_coded = cn.concept_id \n" +
                "where o.concept_id = 90221 and obs_datetime >= date_sub('%s', interval 6 month) and cn.concept_name_type='FULLY_SPECIFIED' and obs_datetime < '%s';", startDate, startDate);


        SqlQueryBuilder q = new SqlQueryBuilder(query);

        List<Object[]> results = evaluationService.evaluateToList(q, context);


        List<Adherence> adherences = new ArrayList<>();

        for (Object[] row : results) {
            String patientId = String.valueOf(row[0]);
            String replaced_code = null;
            String value_coded = String.valueOf(row[1]);
            if(value_coded.equals("GOOD ADHERENCE"))
            {
              replaced_code = value_coded.replace("ADHERENCE","");
            }
            else if (value_coded.equals("FAIR ADHERENCE"))
            {
                replaced_code = value_coded.replace("ADHERENCE","");

            }
            else if (value_coded.equals("POOR ADHERENCE"))
            {
                replaced_code = value_coded.replace("ADHERENCE","");

            }

            String obs_datetime = String.valueOf(row[2]);
            adherences.add(new Adherence(patientId, replaced_code, obs_datetime));
        }

        Map<String, List<Adherence>> grouped = adherences.stream().collect(groupingBy(Adherence::getPatientId));

        for (Map.Entry<String, List<Adherence>> d : grouped.entrySet()) {
            c.addData(Integer.valueOf(d.getKey()), d.getValue());
        }
        return c;
    }

}
