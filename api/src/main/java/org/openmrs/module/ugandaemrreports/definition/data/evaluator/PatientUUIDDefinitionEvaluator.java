package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Person;
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
import org.openmrs.module.ugandaemrreports.definition.data.definition.PersonUUIDDataDefinition;
import org.openmrs.module.ugandaemrreports.library.HIVPatientDataLibrary;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 */
@Handler(supports = PersonUUIDDataDefinition.class, order = 50)
public class PatientUUIDDefinitionEvaluator implements PatientDataEvaluator {
    protected static final Log log = LogFactory.getLog(PersonUUIDDataDefinition.class);

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
        PersonUUIDDataDefinition def = (PersonUUIDDataDefinition) definition;

        EvaluatedPatientData c = new EvaluatedPatientData(def, context);
        if (context.getBaseCohort() != null && context.getBaseCohort().isEmpty()) {
            return c;
        }

                String query = "Select p.person_id,pa.patient_id,p.uuid from person  p, patient pa, encounter e where\n" +
                        "p.person_id=pa.patient_id and e.patient_id=p.person_id";


        SqlQueryBuilder q = new SqlQueryBuilder(query);

        List<Object[]> results = evaluationService.evaluateToList(q, context);


        for (Object[] row : results) {
            Integer personID = Integer.valueOf(String.valueOf(row[0]));
            String patientId = String.valueOf(row[1]);
            String personUUID = String.valueOf (row[2]) ;
            Person person =new Person();
            if(personID!=null)
            {
                person.setPersonId(personID);
                person.setUuid(personUUID);
                c.addData(personID,person);
            }
            else
            {
                c.addData(personID,null);
            }
        }

        return c;
    }

}
