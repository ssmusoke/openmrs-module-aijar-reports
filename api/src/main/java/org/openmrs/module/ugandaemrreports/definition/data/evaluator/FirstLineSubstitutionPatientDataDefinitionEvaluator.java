package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.openmrs.annotation.Handler;
import org.openmrs.api.ObsService;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.evaluator.PatientDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.SubstituteOrSwitch;
import org.openmrs.module.ugandaemrreports.definition.data.definition.FirstLineSubstitutionPatientDataDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carapai on 13/09/2016.
 */
@Handler(supports = FirstLineSubstitutionPatientDataDefinition.class, order = 50)

public class FirstLineSubstitutionPatientDataDefinitionEvaluator implements PatientDataEvaluator {

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private ObsService obsService;

    @Override
    public EvaluatedPatientData evaluate(PatientDataDefinition definition, EvaluationContext context) throws EvaluationException {
        FirstLineSubstitutionPatientDataDefinition def = (FirstLineSubstitutionPatientDataDefinition) definition;

        EvaluatedPatientData c = new EvaluatedPatientData(def, context);

        if (context.getBaseCohort() != null && context.getBaseCohort().isEmpty()) {
            return c;
        }

        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append("SELECT person_id,GROUP_CONCAT(CONCAT(obs_id,'-',concept_id,'-',obs_group_id)) as 'col' FROM obs WHERE obs_group_id in (select obs_id from obs where concept_id = 99062 and voided = 0)  and concept_id in(99163,90246) and voided = 0 group by person_id");


        List<Object[]> result = evaluationService.evaluateToList(q, new EvaluationContext());

        for (Object[] row : result) {
            Integer patientId = (Integer) row[0];
            String data = (String) row[1];
            List<SubstituteOrSwitch> substituteOrSwitches = new ArrayList<SubstituteOrSwitch>();

            String[] substitutions = data.split(",");

            for (String sub : substitutions) {
                String[] comp = sub.split("-");
                Integer obsId = Integer.valueOf(comp[0]);
                Integer conceptId = Integer.valueOf(comp[1]);
                final Integer obsGroupId = Integer.valueOf(comp[2]);

                SubstituteOrSwitch res = (SubstituteOrSwitch) CollectionUtils.find(substituteOrSwitches, new Predicate() {
                    public boolean evaluate(Object o) {
                        return ((SubstituteOrSwitch) o).getObsGroupId() == obsGroupId;
                    }
                });

                //System.out.println(comp);
                if (res == null) {
                    SubstituteOrSwitch substituteOrSwitch = new SubstituteOrSwitch(obsGroupId, obsId, conceptId);
                    substituteOrSwitches.add(substituteOrSwitch);
                } else {
                    int index = substituteOrSwitches.indexOf(res);
                    substituteOrSwitches.get(index).add(obsId, conceptId);
                }
            }

            if (substituteOrSwitches.size() > def.getSubstitutionOrSwitchNo() - 1) {
                SubstituteOrSwitch s1 = substituteOrSwitches.get(def.getSubstitutionOrSwitchNo() - 1);

                List<Integer> keys = new ArrayList<Integer>(s1.getObs().keySet());
                List<Integer> values = new ArrayList<Integer>(s1.getObs().values());

                Integer conceptId = def.getWhat().getConceptId();

                Integer index = values.indexOf(conceptId);

                if (index != -1) {
                    c.addData(patientId, obsService.getObs(keys.get(index)));
                }
            }
        }
        return c;
    }
}
