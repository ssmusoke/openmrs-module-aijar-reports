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
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.HTSDatasetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.openmrs.module.ugandaemrreports.reports.Helper.*;

@Handler(supports = {HTSDatasetDefinition.class})
public class HTSDatasetEvaluator implements DataSetEvaluator {

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
        HTSDatasetDefinition definition = (HTSDatasetDefinition) dataSetDefinition;

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
                Map<String, String> identifiers = processString(data.getIdentifiers());
                String telephone = attributes.get("14d4f066-15f5-102d-96e4-000c29c2a5d7");
                String maritalStatus = attributes.get("8d871f2a-c2cc-11de-8d13-0010c6dffd0f");
                String NIN = identifiers.get("f0c16a6d-dc5f-4118-a803-616d0075d282");

                Observation serialNo = searchObservations(observations, concept(1646));
                Observation model = searchObservations(observations, concept(165171));
                Observation approach = searchObservations(observations, concept(99462));
                Observation community_testing_point = searchObservations(observations, concept(165160));
                Observation testing_reason = searchObservations(observations, concept(165168));
                Observation special_category = searchObservations(observations, concept(165169));
                Observation testingForFirstTime = searchObservations(observations, concept(165180));
                Observation testLast12Months = searchObservations(observations, concept(162965));
                Observation counselled = searchObservations(observations, concept(162918));
                Observation receivedResultsAsCouple = searchObservations(observations, concept(99494));
                Observation receivedResultsAsIndividual = searchObservations(observations, concept(165183));
                Observation hivResults = searchObservations(observations, concept(99493));
                Observation recentInfection = searchObservations(observations, concept(141520));
                Observation coupleResults = searchObservations(observations, concept(99497));
                Observation hctEntry = searchObservations(observations, concept(162925));
                Observation presumptiveTB = searchObservations(observations, concept(99498));
                Observation presumptiveTBRefferred = searchObservations(observations, concept(165178));

                Observation linkedToCare = searchObservations(observations, concept(162982));
                Observation previousTestResults = searchObservations(observations, concept(165181));
                Observation counselledAsACouple = searchObservations(observations, concept(99368));

                List<String> names = Splitter.on(" ").splitToList(Splitter.on(",").splitToList(data.getNames()).get(0));

                pdh.addCol(row, "date", data.getEncounterDate());

                if (names.size() > 1) {
                    pdh.addCol(row, "first_name", names.get(0));
                    pdh.addCol(row, "last_name", names.get(1));
                } else if (names.size() == 1) {
                    pdh.addCol(row, "first_name", names.get(0));
                    pdh.addCol(row, "last_name", "");
                } else {
                    pdh.addCol(row, "first_name", "");
                    pdh.addCol(row, "last_name", "");
                }


                if (serialNo != null) {
                    pdh.addCol(row, "reg", serialNo.getValue());
                } else {
                    pdh.addCol(row, "reg", "");
                }
                if (model != null) {
                    pdh.addCol(row, "model", model.getValue());
                } else {
                    pdh.addCol(row, "model", "");
                }
                if (approach != null) {
                    pdh.addCol(row, "approach", approach.getValue());
                } else {
                    pdh.addCol(row, "approach", "");
                }
                if (community_testing_point != null) {
                    pdh.addCol(row, "community_test_point", community_testing_point.getValue());
                } else {
                    pdh.addCol(row, "community_test_point", "");
                }
                if (testing_reason != null) {
                    pdh.addCol(row, "testing_reason", testing_reason.getValue());
                } else {
                    pdh.addCol(row, "testing_reason", "");
                }
                if (special_category != null) {
                    pdh.addCol(row, "special_category", special_category.getValue());
                } else {
                    pdh.addCol(row, "special_category", "");
                }
                if (NIN != null) {
                    pdh.addCol(row, "NIN", NIN);
                } else {
                    pdh.addCol(row, "NIN", "");
                }

                Integer age = data.getAge();

                pdh.addCol(row, "age", age);

                pdh.addCol(row, "sex", data.getGender());

                if (maritalStatus1 != null) {
                    pdh.addCol(row, "marital", processString2(maritalStatus1).get(1));
                } else if (maritalStatus != null) {
                    pdh.addCol(row, "marital", convert(maritalStatus));
                } else {
                    pdh.addCol(row, "marital", "");
                }

                if (addresses.size() == 6) {
                    pdh.addCol(row, "district", addresses.get(1));
                    pdh.addCol(row, "sub-county", addresses.get(3));
                    pdh.addCol(row, "parish" , addresses.get(4));
                    pdh.addCol(row, "village", addresses.get(5));

                } else {
                    pdh.addCol(row, "district", "");
                    pdh.addCol(row, "sub-county", "");
                    pdh.addCol(row, "parish", "");
                    pdh.addCol(row, "village", "");
                }

                pdh.addCol(row, "telephone", telephone);


                if (testingForFirstTime != null&& testingForFirstTime.getValue().equals("YES")) {
                    pdh.addCol(row, "first","Y" );
                } else {
                    pdh.addCol(row, "first", "N");
                }

                if (testLast12Months != null && Integer.valueOf(testLast12Months.getValue()) > 2) {
                    pdh.addCol(row, "tested > 2", "Y");
                } else {
                    pdh.addCol(row, "tested > 2", "N");
                }

                if (counselled != null && counselled.getValue().equals("YES")) {
                    pdh.addCol(row, "counselled", "Y");
                } else {
                    pdh.addCol(row, "counselled", "N");
                }



                if ((receivedResultsAsIndividual != null &&  receivedResultsAsIndividual.getValue().equals("YES")) || (receivedResultsAsCouple !=null&&receivedResultsAsCouple.getValue().equals("YES"))) {
                    pdh.addCol(row, "received", "Y");
                } else {
                    pdh.addCol(row, "received", "N");
                }

                if (hivResults != null) {
                    pdh.addCol(row, "tested", "Y");
                    pdh.addCol(row, "results", hivResults.getValue());
                } else {
                    pdh.addCol(row, "results", "");
                    pdh.addCol(row, "tested", "N");
                }

                if (recentInfection != null) {
                    pdh.addCol(row, "recent", recentInfection.getValue());
                } else {
                    pdh.addCol(row, "recent", "");
                }

                if(previousTestResults !=null){
                    if (previousTestResults.getValue().equals("HIV+")) {
                        pdh.addCol(row, "already_positive", "Y");
                    } else {
                        pdh.addCol(row, "already_positive", "N");
                    }
                }
                else{
                    pdh.addCol(row, "already_positive", "NA");
                }

                if(counselledAsACouple !=null){
                    if (counselledAsACouple.getValue().equals("Couple Counselling session")) {
                        pdh.addCol(row, "c-couple", "Y");
                    } else {
                        pdh.addCol(row, "c-couple", "N");
                    }
                }else{
                    pdh.addCol(row, "c-couple", "");
                }

                if (receivedResultsAsCouple != null) {
                    if (receivedResultsAsCouple.getValue().equals("YES")) {
                        pdh.addCol(row, "r-couple", "Y");
                    } else {
                        pdh.addCol(row, "r-couple", "N");
                    }
                } else {
                    pdh.addCol(row, "r-couple", "");
                }

                if (coupleResults != null) {
                    if(coupleResults.getValue().equals("DISCORDANT COUPLE"))
                    {
                     pdh.addCol(row, "discordant", "Y");
                    } else {
                    pdh.addCol(row, "discordant", "N");
                    }
                }else{
                    pdh.addCol(row, "discordant", "");
                }

                if (coupleResults != null) {
                    if(coupleResults.getValue().equals("Concordant Positive"))
                    {
                     pdh.addCol(row, "concordant", "Y");
                    } else {
                    pdh.addCol(row, "concordant", "N");
                    }
                }else{
                    pdh.addCol(row, "concordant", "");
                }

                if (hctEntry != null) {
                    pdh.addCol(row, "entry", hctEntry.getValue());
                } else {
                    pdh.addCol(row, "entry", "");
                }

                if (presumptiveTB != null && presumptiveTB.getValue().equals("YES")) {
                    pdh.addCol(row, "tb", "Y");
                } else {
                    pdh.addCol(row, "tb", "N");
                }

                if (presumptiveTBRefferred != null && presumptiveTBRefferred.getValue().equals("YES")) {
                    pdh.addCol(row, "tb_refferred", "Y");
                } else {
                    pdh.addCol(row, "tb_refferred", "N");
                }

                if (linkedToCare != null && linkedToCare.getValue().equals("YES")) {
                    pdh.addCol(row, "linked", "Y");
                } else {
                    pdh.addCol(row, "linked", "N");
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
        conceptsNames.put("5555", "MARRIED");
        conceptsNames.put("90007", "DIVORCED");
        conceptsNames.put("90008", "SEPARATED");
        conceptsNames.put("90009", "WIDOWED");
        conceptsNames.put("90280", "CHILD");
        conceptsNames.put("1057", "NEVER MARRIED");
        conceptsNames.put("1056", "SEPARATED");
        conceptsNames.put("1060", "LIVING WITH PARTNER");



        return conceptsNames.get(value);
    }

}
