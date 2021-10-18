package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import com.google.common.base.Splitter;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.Observation;
import org.openmrs.module.ugandaemrreports.common.PatientDataHelper;
import org.openmrs.module.ugandaemrreports.common.PatientEncounterObs;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.EmergencyARTRefillDatasetDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.HCTDatasetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.openmrs.module.ugandaemrreports.reports.Helper.*;

@Handler(supports = {EmergencyARTRefillDatasetDefinition.class})
public class EmergencyARTRefillDatasetEvaluator implements DataSetEvaluator {

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
        EmergencyARTRefillDatasetDefinition definition = (EmergencyARTRefillDatasetDefinition) dataSetDefinition;

        Date startDate = definition.getStartDate();
        Date endDate = definition.getEndDate();

        PatientDataHelper pdh = new PatientDataHelper();


        try {
            List<PatientEncounterObs> patientEncounterObs = getEncounterObs(sqlConnection(), "d18bd8f2-dfcd-11eb-ba80-0242ac130004", null, startDate, endDate);

            for (PatientEncounterObs data : patientEncounterObs) {
                DataSetRow row = new DataSetRow();
                List<Observation> observations = data.getObs();
                List<String> addresses = processString2(data.getAddresses());
                Map<String, String> attributes = processString(data.getAttributes());
                Map<String, String> identifiers = processString(data.getIdentifiers());
                String telephone = attributes.get("14d4f066-15f5-102d-96e4-000c29c2a5d7");
                String NIN = identifiers.get("f0c16a6d-dc5f-4118-a803-616d0075d282");

                Observation serialNo = searchObservations(observations, concept(1646));
                Observation clientCategory = searchObservations(observations, concept(165169));
                Observation ARTNo = searchObservations(observations, concept(90264));
                Observation facilityName = searchObservations(observations, concept(166453));
                Observation parentDistrict = searchObservations(observations, concept(166454));
                Observation reasonForVisit = searchObservations(observations, concept(160288));
                Observation current_regimen = searchObservations(observations, concept(90315));
                Observation weight = searchObservations(observations, concept(5089));
                Observation temperature = searchObservations(observations, concept(5088));
                Observation TBStatus = searchObservations(observations, concept(90216));
                Observation TBPills = searchObservations(observations, concept(166464));
                Observation INHStatus = searchObservations(observations, concept(165288));
                Observation INHPills = searchObservations(observations, concept(160856));
                Observation Nstatus = searchObservations(observations, concept(165050));
                Observation CTX = searchObservations(observations, concept(160856));
                Observation pills = searchObservations(observations, concept(99038));
                Observation followStatus = searchObservations(observations, concept(166460));

                List<String> names = Splitter.on(" ").splitToList(Splitter.on(",").splitToList(data.getNames()).get(0));

                pdh.addCol(row, "date", data.getEncounterDate());

                if (names.size() > 1) {
                    pdh.addCol(row, "familyName", names.get(0));
                    pdh.addCol(row, "givenName", names.get(1));
                } else if (names.size() == 1) {
                    pdh.addCol(row, "familyName", names.get(0));
                    pdh.addCol(row, "givenName", "");
                } else {
                    pdh.addCol(row, "familyName", "");
                    pdh.addCol(row, "givenName", "");
                }


                if (serialNo != null) {
                    pdh.addCol(row, "serialNo", serialNo.getValue());
                } else {
                    pdh.addCol(row, "serialNo", "");
                }

                Integer age = data.getAge();

                pdh.addCol(row, "Age", age);

                pdh.addCol(row, "Sex", data.getGender());



                if (addresses.size() == 6) {
                    pdh.addCol(row, "Address", addresses.get(1)+", "+addresses.get(3)+", "+ addresses.get(4)+", "+ addresses.get(5) );
                } else {
                    pdh.addCol(row, "Address", "" );
                }

                pdh.addCol(row, "telephone", telephone);

                if (NIN != null) {
                    pdh.addCol(row, "NIN", NIN);
                } else {
                    pdh.addCol(row, "NIN", "");
                }

                if (clientCategory != null) {
                    pdh.addCol(row, "Client_Category", clientCategory.getValue());
                } else {
                    pdh.addCol(row, "Client_Category", "");
                }

                if (ARTNo != null) {
                    pdh.addCol(row, "ARTNo", ARTNo.getValue());
                } else {
                    pdh.addCol(row, "ARTNo", "");
                }

                if (facilityName != null) {
                    pdh.addCol(row, "facilityName", facilityName.getValue());
                } else {
                    pdh.addCol(row, "facilityName", "");
                }

                if (parentDistrict != null) {
                    pdh.addCol(row, "parentDistrict", parentDistrict.getValue());
                } else {
                    pdh.addCol(row, "parentDistrict", "");
                }
                if (reasonForVisit != null) {
                    pdh.addCol(row, "reasonForVisit", reasonForVisit.getValue());
                } else {
                    pdh.addCol(row, "reasonForVisit", "");
                }
                if (current_regimen != null) {
                    pdh.addCol(row, "current_regimen", current_regimen.getValue());
                } else {
                    pdh.addCol(row, "current_regimen", "");
                }
                if (weight != null) {
                    pdh.addCol(row, "weight", weight.getValue());
                } else {
                    pdh.addCol(row, "weight", "");
                }
                if (temperature != null) {
                    pdh.addCol(row, "temperature", temperature.getValue());
                } else {
                    pdh.addCol(row, "temperature", "");
                }
                if (TBStatus != null) {
                    pdh.addCol(row, "TBStatus", TBStatus.getValue());
                } else {
                    pdh.addCol(row, "TBStatus", "");
                }
                if (TBPills != null) {
                    pdh.addCol(row, "TBPills", TBPills.getValue());
                } else {
                    pdh.addCol(row, "TBPills", "");
                }
                if (INHStatus != null) {
                    pdh.addCol(row, "INHStatus", INHStatus.getValue());
                } else {
                    pdh.addCol(row, "INHStatus", "");
                }
                if (INHPills != null) {
                    pdh.addCol(row, "INHPills", INHPills.getValue());
                } else {
                    pdh.addCol(row, "INHPills", "");
                }
                if (Nstatus != null) {
                    pdh.addCol(row, "Nstatus", Nstatus.getValue());
                } else {
                    pdh.addCol(row, "Nstatus", "");
                }
                if (CTX != null) {
                    pdh.addCol(row, "CTX", CTX.getValue());
                } else {
                    pdh.addCol(row, "CTX", "");
                }
                if (pills != null) {
                    pdh.addCol(row, "pills", pills.getValue());
                } else {
                    pdh.addCol(row, "pills", "");
                }
                if (followStatus != null) {
                    pdh.addCol(row, "followStatus", followStatus.getValue());
                } else {
                    pdh.addCol(row, "followStatus", "");
                }


                dataSet.addRow(row);

            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return dataSet;

    }

    private Predicate<Observation> concept(Integer concept) {
        return p -> p.getConcept().compareTo(concept) == 0;
    }


}
