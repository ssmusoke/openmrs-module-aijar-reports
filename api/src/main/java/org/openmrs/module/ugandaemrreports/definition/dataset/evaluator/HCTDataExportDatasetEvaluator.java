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
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.HCTDataExportDatasetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.openmrs.module.ugandaemrreports.reports.Helper.*;

@Handler(supports = {HCTDataExportDatasetDefinition.class})
public class HCTDataExportDatasetEvaluator implements DataSetEvaluator {

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
        HCTDataExportDatasetDefinition definition = (HCTDataExportDatasetDefinition) dataSetDefinition;

        Date startDate = definition.getStartDate();
        Date endDate = definition.getEndDate();

        PatientDataHelper pdh = new PatientDataHelper();


        try {
            List<PatientEncounterObs> patientEncounterObs = getEncounterObs(sqlConnection(), "264daIZd-f80e-48fe-nba9-P37f2W1905Pv", null, startDate, endDate);

            for (PatientEncounterObs data : patientEncounterObs) {
                DataSetRow row = new DataSetRow();
                List<Observation> observations = data.getObs();
                String maritalStatus1 = data.getMaritalStatus();
                List<String> addresses = processString2(data.getAddresses());
                Map<String, String> attributes = processString(data.getAttributes());
                String telephone = attributes.get("14d4f066-15f5-102d-96e4-000c29c2a5d7");
                String maritalStatus = attributes.get("8d871f2a-c2cc-11de-8d13-0010c6dffd0f");

                Observation registrationNo = searchObservations(observations, concept(164985));
                Observation testBe4 = searchObservations(observations, conceptValue(99464, "1065"));
                Observation counselled = searchObservations(observations, conceptValue(162918, "90003"));
                Observation receivedResults = searchObservations(observations, conceptValue(99411, "1065"));
                Observation hivResults = searchObservations(observations, concept(99493));
                Observation counselledAsCouple = searchObservations(observations, conceptValue(99368, "99367"));
                Observation resultAsCouple = searchObservations(observations, conceptValue(99494, "1065"));
                Observation discordantResults = searchObservations(observations, concept(99497));
                Observation hctEntry = searchObservations(observations, concept(162925));
                Observation counselingapproach = searchObservations(observations, concept(99462));
                Observation presumptiveTB = searchObservations(observations, conceptValue(99498, "1065"));
                Observation cd4 = searchObservations(observations, concept(5497));
                Observation numberoftimestested = searchObservations(observations, concept(162965));
                Observation numberofsexualpartners = searchObservations(observations, concept(99463));
                Observation spousetested = searchObservations(observations, conceptValue(99472, "1065"));
                Observation spousehivtestresults = searchObservations(observations, concept(99477));
                Observation cotrimgiven = searchObservations(observations, conceptValue(99413, "1065"));



                Observation linkedToCare = searchObservations(observations, conceptValue(162982, "1065"));
                Observation entrypointintocare = searchObservations(observations, concept(90200));


                List<String> names = Splitter.on(" ").splitToList(Splitter.on(",").splitToList(data.getNames()).get(0));

                pdh.addCol(row, "Date",formatDate(data.getEncounterDate()));

                if (names.size() > 1) {
                    pdh.addCol(row, "First Name", names.get(0));
                    pdh.addCol(row, "Last Name", names.get(1));
                } else if (names.size() == 1) {
                    pdh.addCol(row, "First Name", names.get(0));
                    pdh.addCol(row, "Last Name", "");
                } else {
                    pdh.addCol(row, "First Name", "");
                    pdh.addCol(row, "Last Name", "");
                }


                if (registrationNo != null) {
                    pdh.addCol(row, "Registration Number", registrationNo.getValue());
                } else {
                    pdh.addCol(row, "Registration Number", "");
                }

                Integer age = data.getAge();

                if (age!= null) {
                    pdh.addCol(row, "Age", age);
                } else {
                    pdh.addCol(row, "Age", "");
                }


                pdh.addCol(row, "Sex", data.getGender());

                if (maritalStatus1 != null) {
                    pdh.addCol(row, "Marital Status", convert(processString2(maritalStatus1).get(1)));
                } else if (maritalStatus != null) {
                    pdh.addCol(row, "Marital Status", maritalStatus);
                } else {
                    pdh.addCol(row, "Marital Status", "");
                }

                if (addresses.size() == 6) {
                    pdh.addCol(row, "District", addresses.get(1));
                    pdh.addCol(row, "Sub-County", addresses.get(3) + " " + addresses.get(4));
                    pdh.addCol(row, "Village", addresses.get(5));

                } else {
                    pdh.addCol(row, "District", "");
                    pdh.addCol(row, "Sub-County", "");
                    pdh.addCol(row, "Village", "");
                }

                pdh.addCol(row, "Telephone", telephone);

                if (counselled != null) {
                    pdh.addCol(row, "Pretest Counseling", "Y");
                } else {
                    pdh.addCol(row, "Pretest Counseling", "N");
                }
                if (counselledAsCouple != null) {
                    pdh.addCol(row, "Counseling Session Type", convert(counselledAsCouple.getValue()));
                } else {
                    pdh.addCol(row, "Counseling Session Type", "");
                }

                if (counselingapproach != null) {
                    pdh.addCol(row, "Counseling Approach", convert(counselingapproach.getValue()));
                } else {
                    pdh.addCol(row, "Counseling Approach", "");
                }

                if (hctEntry != null) {
                    pdh.addCol(row, "Entry Point", convert(hctEntry.getValue()));
                } else {
                    pdh.addCol(row, "Entry Point", "");
                }

                if (testBe4 != null) {
                    pdh.addCol(row, "First Time Testing", "N");
                } else {
                    pdh.addCol(row, "First Time Testing", "Y");
                }

                if (numberoftimestested != null) {
                    pdh.addCol(row, "Number of Times Tested", numberoftimestested.getValue());
                } else {
                    pdh.addCol(row, "Number of Times Tested", "");
                }
                if (numberofsexualpartners != null) {
                    pdh.addCol(row, "Number of Sexual Partners", numberofsexualpartners.getValue());
                } else {
                    pdh.addCol(row, "Number of Sexual Partners", "");
                }
                if (spousetested != null) {
                    pdh.addCol(row, "Spouse Tested", "Y");
                } else {
                    pdh.addCol(row, "Spouse Tested", "N");
                }
                if (spousehivtestresults != null) {
                    pdh.addCol(row, "Spouse HIV Results", convert(spousehivtestresults.getValue()));
                } else {
                    pdh.addCol(row, "Spouse HIV Results", "");
                }
                if (hivResults != null) {
                    pdh.addCol(row, "HIV Final Results", convert(hivResults.getValue()));
                } else {
                    pdh.addCol(row, "HIV Final Results", "");
                }
                if (receivedResults != null) {
                    pdh.addCol(row, "Recieved Final Results", "Y");
                } else {
                    pdh.addCol(row, "Recieved Final Results", "N");
                }
                if (resultAsCouple != null) {
                    pdh.addCol(row, "Recieved Results As a Couple", "Y");
                } else {
                    pdh.addCol(row, "Recieved Results As a Couple", "N");
                }
                if (discordantResults != null) {
                    pdh.addCol(row, "Couple Results", discordantResults.getValue());
                } else {
                    pdh.addCol(row, "Couple Results ", "N");
                }

                if (presumptiveTB != null) {
                    pdh.addCol(row, "Presumptive TB", "Y");
                } else {
                    pdh.addCol(row, "Presumptive TB", "N");
                }

                if (cotrimgiven != null) {
                    pdh.addCol(row, "Cotrim Given", "Y");
                } else {
                    pdh.addCol(row, "Cotrim Given", "N");
                }
                if (linkedToCare != null) {
                    pdh.addCol(row, "Linked To Care", "Y");
                } else {
                    pdh.addCol(row, "Linked To Care", "N");
                }
                if(entrypointintocare!=null)
                {
                    pdh.addCol(row,"Entry Point To Care",convert(entrypointintocare.getValue()));
                }
                else
                {
                    pdh.addCol(row, "Entry Point To Care", "");

                }


//                pdh.addCol(row, "tested", "-");

                if (cd4 != null) {
                    pdh.addCol(row, "CD4", cd4.getValue());
                } else {
                    pdh.addCol(row, "CD4", "");
                }





                dataSet.addRow(row);

            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return dataSet;

    }

    private Predicate<Observation> conceptValue(Integer concept, String value) {
        return p -> p.getConcept().compareTo(concept) == 0 && p.getValue().compareTo(value) == 0;
    }

    private Predicate<Observation> concept(Integer concept) {
        return p -> p.getConcept().compareTo(concept) == 0;
    }

    private String convert(String value) {
        Map<String, String> conceptsNames = new HashMap<>();

        conceptsNames.put("99416", "Facility based");
        conceptsNames.put("162920", "Work place");
        conceptsNames.put("162921", "Comm/Outreach");
        conceptsNames.put("162922", "HBHCT");
        conceptsNames.put("162923", "Circumcision (SMC)");
        conceptsNames.put("99056", "PEP");
        conceptsNames.put("90012", "PMTCT");
        conceptsNames.put("162924", "MARPS");
        conceptsNames.put("164983", "ANC");
        conceptsNames.put("160456", "Maternity");
        conceptsNames.put("164984", "Family Planning");
        conceptsNames.put("160456", "Gynecological OPD");
        conceptsNames.put("160473", "Emergency Gynecological ward");
        conceptsNames.put("164982", "Men's Access Clinic");
        conceptsNames.put("164981", "Mother Baby Pair Clinic");

        conceptsNames.put("90167", "HIV-");
        conceptsNames.put("90166", "HIV+");
        conceptsNames.put("162926", "INC");
        conceptsNames.put("162927", "NT");

        conceptsNames.put("90005", "SINGLE");
        conceptsNames.put("90006", "MARRIED");
        conceptsNames.put("90007", "DIVORCED");
        conceptsNames.put("90008", "SEPARATED");
        conceptsNames.put("90009", "WIDOWED");
        conceptsNames.put("90280", "CHILD");
        conceptsNames.put("1057", "NEVER MARRIED");
        conceptsNames.put("1060", "LIVING WITH PARTNER");
        conceptsNames.put("90021","ADOLESCENT OUTREACH");
        conceptsNames.put("90012","PMTCT");
        conceptsNames.put("90014","PEDIATRIC OUTPATIENT");
        conceptsNames.put("90025","POLICE RECRUITEMENT");
        conceptsNames.put("90023","COMMUNITY BASED ORGANIZATION");
        conceptsNames.put("99087","EXPOSED INFANT ");
        conceptsNames.put("90019","IDU OUTREACH");
        conceptsNames.put("90018","MEDICAL INPATIENT");
        conceptsNames.put("90013","MEDICAL OUTPATIENT");
        conceptsNames.put("90024","MILITARY RECRUITEMENT ");
        conceptsNames.put("90014","PEDIATRIC OUTPATIENT");
        conceptsNames.put("90017","PRIVATE PROVIDER");
        conceptsNames.put("90020","SEX WORKER OUTREACH");
        conceptsNames.put("90015","STI OUTPATIENT");
        conceptsNames.put("90016","TB TREATMENT CENTER");
        conceptsNames.put("99593","YCC-Young child clinic");
        conceptsNames.put("99365","VCT");
        conceptsNames.put("90022","Indivual");
        conceptsNames.put("99367","Couple");
        conceptsNames.put("99366","Group");
        conceptsNames.put("162919","CICT");
        conceptsNames.put("99459","PITC");


        return conceptsNames.get(value);
    }
    private String formatDate(Date date) {
        DateFormat dateFormatter = new SimpleDateFormat("MMM dd,yyyy");
        return dateFormatter.format(date);
    }

}
