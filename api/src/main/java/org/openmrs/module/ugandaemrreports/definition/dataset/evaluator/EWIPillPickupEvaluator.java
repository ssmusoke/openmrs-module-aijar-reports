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
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.ARTDatasetDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.EWIPillPickupDataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.openmrs.module.ugandaemrreports.definition.dataset.queries.PillPickupQueries.*;
import static org.openmrs.module.ugandaemrreports.reports.Helper.*;

@Handler(supports = {EWIPillPickupDataSetDefinition.class})
public class EWIPillPickupEvaluator implements DataSetEvaluator {
    @Autowired
    private EvaluationService evaluationService;
    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
        EWIPillPickupDataSetDefinition definition = (EWIPillPickupDataSetDefinition) dataSetDefinition;
        String startDate = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");
        String cohortQueryString = ewiPillPickupQuery(startDate, endDate);
        try {
            List<Integer> patients = getEWICohort(sqlConnection(), cohortQueryString);
            String cohortString = Joiner.on(',').join(patients);
            String encounterQuery = ewiPillPickupEncounterQuery(startDate, cohortString);
            String ewiPillPickupBaselinePickupQuery = ewiPillPickupBaselinePickupQuery(startDate,endDate,cohortString);
//            List<EWIPatientEncounter> encounters = getEWIPatientEncounters(sqlConnection(), encounterQuery);
//            Map<Integer, List<EWIPatientEncounter>> groupedPatients = encounters.stream().collect(groupingBy(EWIPatientEncounter::getPersonId));

            List<EWIPatientEncounter> encounters =getBaselinePickup(sqlConnection(),ewiPillPickupBaselinePickupQuery);
            List<EWIPatientEncounter> noOfDaysPickedEncounters = getNumberOfDaysPickedAtBaseline(sqlConnection(), ewiNumberOfDaysPickedAtBaselinePickup(startDate,endDate,cohortString));
//
            Map<Integer, List<EWIPatientEncounter>> groupedPatients = encounters.stream() .collect(Collectors.groupingBy(EWIPatientEncounter::getPersonId));
            Map<Integer, List<EWIPatientEncounter>> groupedDaysOfPatients = noOfDaysPickedEncounters.stream() .collect(Collectors.groupingBy(EWIPatientEncounter::getPersonId));


            String ewiDataQuery = ewiPillPickupPatientDataQuery(startDate,endDate,cohortString);
            List<EWIPatientData> ewiPatientData = getEWIPillPickupPatients(sqlConnection(), ewiDataQuery);
            Map<Integer, List<EWIPatientData>> groupedPatientData = ewiPatientData.stream().collect(groupingBy(EWIPatientData::getPersonId));
            PatientDataHelper pdh = new PatientDataHelper();
            for (Integer patient : patients) {
                DataSetRow row = new DataSetRow();
                List<EWIPatientData> ewiPatientData1 = groupedPatientData.get(patient);
                List<EWIPatientEncounter> ewiPatientEncounters = groupedPatients.get(patient);
                List<EWIPatientEncounter> days = groupedDaysOfPatients.get(patient);

                    if (ewiPatientData1 != null && ewiPatientData1.size() > 0 && ewiPatientEncounters != null && ewiPatientEncounters.size() > 0) {
                        EWIPatientData patientData = ewiPatientData1.get(0);
                        pdh.addCol(row, "PatientID", patientData.getArtClinicNumber());
                        pdh.addCol(row, "Sex", patientData.getGender());
                        pdh.addCol(row, "DOB", new SimpleDateFormat("yyyy-MM-dd").parse(patientData.getDob()));

                        if(patientData.getTransferOutDate()==null &&patientData.getDeathDate()!=null)
                            pdh.addCol(row,"transferOrDeath",new SimpleDateFormat("yyyy-MM-dd").parse(patientData.getDeathDate()));

                        else if(patientData.getDeathDate()==null &&patientData.getTransferOutDate()!=null)
                            pdh.addCol(row,"transferOrDeath",new SimpleDateFormat("yyyy-MM-dd").parse(patientData.getTransferOutDate()));
                        else if(patientData.getDeathDate()==null &&patientData.getTransferOutDate()==null &&patientData.getArv_stop()!=null)
                            pdh.addCol(row,"transferOrDeath",new SimpleDateFormat("yyyy-MM-dd").parse(patientData.getArv_stop()));
                        else
                            pdh.addCol(row,"transferOrDeath","");

                        String baselinePickupDate = null;
                        String pickup1= null;
                        baselinePickupDate = ewiPatientEncounters.get(0).getBaselinePickupDate().split(",")[0];


                        pdh.addCol(row, "pickupDate", new SimpleDateFormat("yyyy-MM-dd").parse(baselinePickupDate));
                        try{
                        pickup1 = ewiPatientEncounters.get(0).getBaselinePickupDate().split(",")[1];
                        pdh.addCol(row,"pickup1",new SimpleDateFormat("yyyy-MM-dd").parse(pickup1));}
                        catch(ArrayIndexOutOfBoundsException exception) {
                            pdh.addCol(row,"pickup1","");
                        }
                        if(days== null || days.size()==0) {
                            pdh.addCol(row,"noOfDaysPicked","");
                        }
                        else
                        {
                            pdh.addCol(row,"noOfDaysPicked",days.get(0).getNumberOfDaysPickedUpAtBaseline());
                        }

                    } else {
                        pdh.addCol(row, "PatientID", "");
                        pdh.addCol(row, "Sex", "");
                        pdh.addCol(row, "DOB", "");
                        pdh.addCol(row, "pickupDate", "");
                        pdh.addCol(row,"pickup1","");
                        pdh.addCol(row,"noOfDaysPicked","");
                        pdh.addCol(row,"transferOrDeath","");
                    }
                    pdh.addCol(row, "Age", "");
                  dataSet.addRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataSet;
    }
}
