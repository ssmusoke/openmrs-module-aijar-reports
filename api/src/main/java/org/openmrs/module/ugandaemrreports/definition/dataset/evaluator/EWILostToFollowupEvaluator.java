package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import com.google.common.base.Joiner;
import org.joda.time.LocalDate;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.EWIPatientData;
import org.openmrs.module.ugandaemrreports.common.EWIPatientEncounter;
import org.openmrs.module.ugandaemrreports.common.PatientDataHelper;
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.EWILostToFollowupDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.openmrs.module.ugandaemrreports.definition.dataset.queries.Queries.*;
import static org.openmrs.module.ugandaemrreports.reports.Helper.*;

/**
 */
@Handler(supports = {EWILostToFollowupDefinition.class})
public class EWILostToFollowupEvaluator implements DataSetEvaluator {
    @Autowired
    private EvaluationService evaluationService;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
        EWILostToFollowupDefinition definition = (EWILostToFollowupDefinition) dataSetDefinition;

        String startDate = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");


        String cohortQueryString = ewiQuery(startDate, endDate);


        try {
            List<Integer> patients = getEWICohort(sqlConnection(), cohortQueryString);

            String cohortString = Joiner.on(',').join(patients);

            String encounterQuery = ewiEncounterQuery(startDate, cohortString);

            List<EWIPatientEncounter> encounters = getEWIPatientEncounters(sqlConnection(), encounterQuery);

            Map<Integer, List<EWIPatientEncounter>> groupedPatients = encounters.stream().collect(groupingBy(EWIPatientEncounter::getPersonId));

            String ewiDataQuery = ewiPatientDataQuery(cohortString);

            List<EWIPatientData> ewiPatientData = getEWIPatients(sqlConnection(), ewiDataQuery);

            Map<Integer, List<EWIPatientData>> groupedPatientData = ewiPatientData.stream().collect(groupingBy(EWIPatientData::getPersonId));


            PatientDataHelper pdh = new PatientDataHelper();
            for (Integer patient : patients) {
                DataSetRow row = new DataSetRow();

                List<EWIPatientData> ewiPatientData1 = groupedPatientData.get(patient);
                List<EWIPatientEncounter> ewiPatientEncounters = groupedPatients.get(patient);
//                ewiPatientEncounters.sort(Comparator.comparing(EWIPatientEncounter::getEncounterDate));

                if (ewiPatientData1 != null && ewiPatientData1.size() > 0) {
                    EWIPatientData patientData = ewiPatientData1.get(0);
                    pdh.addCol(row, "Patient ID", patientData.getArtClinicNumber());
                    pdh.addCol(row, "Sex", patientData.getGender());
                    pdh.addCol(row, "Birth Date", patientData.getDob());
                    String patientArtStartDate = null;
                    if (patientData.getArtStartDate() != null && patientData.getArtStartDate().compareToIgnoreCase(startDate) >= 0) {
                        patientArtStartDate = patientData.getArtStartDate();
                    } else {
                        patientArtStartDate = ewiPatientEncounters.get(0).getEncounterDate();
                    }

                    pdh.addCol(row, "ART Start Date", patientArtStartDate);

                    LocalDate localDate = StubDate.dateOf(patientArtStartDate);

                    String artStartAfter12 = localDate.plusMonths(12).toString("yyyy-MM-dd");
                    String artStartAfter15 = localDate.plusMonths(15).toString("yyyy-MM-dd");
                    /*List<EWIPatientEncounter> filteredEncounters = ewiPatientEncounters.stream().filter(e -> e.getEncounterDate().compareTo(artStartAfter12) <= 0).collect(Collectors.toList());
                    EWIPatientEncounter lastContact = Collections.max(filteredEncounters, Comparator.comparing(EWIPatientEncounter::getEncounterDate));*/
                    pdh.addCol(row, "12 Months Date", artStartAfter12);
                    pdh.addCol(row, "15 Months Date", artStartAfter15);
                    /*if(lastContact != null){
                        pdh.addCol(row, "Last Clinical Consultation", lastContact.getEncounterDate());
                    }else{
                        pdh.addCol(row, "Last Clinical Consultation", "");
                    }*/

                } else {
                    pdh.addCol(row, "Patient ID", "");
                    pdh.addCol(row, "Sex", "");
                    pdh.addCol(row, "Birth Date", "");
                    pdh.addCol(row, "ART Start Date", "");
                    pdh.addCol(row, "12 Months Date", "");
                    pdh.addCol(row, "15 Months Date", "");
                    pdh.addCol(row, "Last Clinical Consultation", "");
                }

                pdh.addCol(row, "Age", "");

                dataSet.addRow(row);
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return dataSet;
    }
}
