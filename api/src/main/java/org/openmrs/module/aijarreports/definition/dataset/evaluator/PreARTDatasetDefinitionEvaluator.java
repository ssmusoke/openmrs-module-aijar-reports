package org.openmrs.module.aijarreports.definition.dataset.evaluator;

import org.openmrs.Cohort;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.aijarreports.common.PatientDataHelper;
import org.openmrs.module.aijarreports.definition.dataset.definition.PreARTDatasetDefinition;
import org.openmrs.module.aijarreports.metadata.CommonReportMetadata;
import org.openmrs.module.aijarreports.metadata.HIVMetadata;
import org.openmrs.module.reporting.cohort.CohortUtil;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;

import java.util.Date;
import java.util.List;

/**
 * Created by carapai on 11/05/2016.
 */
@Handler(supports = {PreARTDatasetDefinition.class})
public class PreARTDatasetDefinitionEvaluator implements DataSetEvaluator {
    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
        PreARTDatasetDefinition definition = (PreARTDatasetDefinition) dataSetDefinition;

        PatientIdentifierType patientIdentifierType = definition.getPatientIdentifierType();
        List<EncounterType> ets = definition.getEncounterTypes();

        context = ObjectUtil.nvl(context, new EvaluationContext());
        Cohort cohort = context.getBaseCohort();

        Date endDateParameter = (Date) context.getParameterValue("endDate");
        if (endDateParameter == null) {
            endDateParameter = new Date();
        }
        Date startDateParameter = (Date) context.getParameterValue("startDate");
        if (startDateParameter == null) {
            startDateParameter = new Date(0);
        }

        if (cohort == null) {
            cohort = Context.getPatientSetService().getAllPatients();
        }

        if (context.getLimit() != null) {
            CohortUtil.limitCohort(cohort, context.getLimit());
        }

        List<Patient> patients = Context.getPatientSetService().getPatients(cohort.getMemberIds());

        PatientDataHelper pdh = new PatientDataHelper();
        HIVMetadata hivMetadata = new HIVMetadata();
        CommonReportMetadata commonMetadata = new CommonReportMetadata();

        for (Patient p : patients) {
            DataSetRow row = new DataSetRow();

            pdh.addCol(row, "ID", p.getPatientId());
            pdh.addCol(row, "givenName", pdh.getGivenName(p));
            pdh.addCol(row, "familyName", pdh.getFamilyName(p));
            pdh.addCol(row, "birthDate", p.getBirthdate());
            pdh.addCol(row, "gender", pdh.getGender(p));

            dataSet.addRow(row);
        }

        return dataSet;
    }
}
