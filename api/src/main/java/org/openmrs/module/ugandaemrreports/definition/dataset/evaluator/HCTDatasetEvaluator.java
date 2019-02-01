package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import com.google.common.base.Splitter;
import org.joda.time.LocalDate;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.Observation;
import org.openmrs.module.ugandaemrreports.common.PatientDataHelper;
import org.openmrs.module.ugandaemrreports.common.PatientEncounterObs;
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.HCTDatasetDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.TBDatasetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.openmrs.module.ugandaemrreports.reports.Helper.*;
import static org.openmrs.module.ugandaemrreports.reports.Helper.processString2;

@Handler(supports = {HCTDatasetDefinition.class})
public class HCTDatasetEvaluator implements DataSetEvaluator {

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
        HCTDatasetDefinition definition = (HCTDatasetDefinition) dataSetDefinition;

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

                Observation registrationNo = searchObservations(observations, concept(164984));
                Observation testBe4 = searchObservations(observations, conceptValue(99464, "1065"));
                Observation testLast12Months = searchObservations(observations, concept(162965));
                Observation counselled = searchObservations(observations, conceptValue(162918, "90003"));
                Observation receivedResults = searchObservations(observations, conceptValue(99411, "1065"));
                Observation hivResults = searchObservations(observations, concept(99493));
                Observation counselledAsCouple = searchObservations(observations, conceptValue(99368, "99367"));
                Observation resultAsCouple = searchObservations(observations, conceptValue(99494, "1065"));
                Observation discordantResults = searchObservations(observations, conceptValue(99497, "6096"));
                Observation hctEntry = searchObservations(observations, concept(162925));
                Observation presumptiveTB = searchObservations(observations, conceptValue(99498, "1065"));
                Observation cd4 = searchObservations(observations, concept(5497));
                Observation linkedToCare = searchObservations(observations, conceptValue(162982, "1065"));

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


                if (registrationNo != null) {
                    pdh.addCol(row, "reg", registrationNo.getValue());
                } else {
                    pdh.addCol(row, "reg", "");
                }

                Integer age = data.getAge();

                if (age < 5) {
                    pdh.addCol(row, "<5", age);
                } else {
                    pdh.addCol(row, "<5", "");
                }

                if (age < 10 && age >= 5) {
                    pdh.addCol(row, "<10", age);
                } else {
                    pdh.addCol(row, "<10", "");
                }

                if (age < 15 && age >= 10) {
                    pdh.addCol(row, "<15", age);
                } else {
                    pdh.addCol(row, "<15", "");
                }

                if (age < 19 && age >= 15) {
                    pdh.addCol(row, "<19", age);
                } else {
                    pdh.addCol(row, "<19", "");
                }

                if (age < 49 && age >= 19) {
                    pdh.addCol(row, "<49", age);
                } else {
                    pdh.addCol(row, "<49", "");
                }

                if (age >= 49) {
                    pdh.addCol(row, ">49", age);
                } else {
                    pdh.addCol(row, ">49", "");
                }

                pdh.addCol(row, "sex", data.getGender());

                if (maritalStatus1 != null) {
                    pdh.addCol(row, "marital", convert(processString2(maritalStatus1).get(1)));
                } else if (maritalStatus != null) {
                    pdh.addCol(row, "marital", maritalStatus);
                } else {
                    pdh.addCol(row, "marital", "");
                }

                if (addresses.size() == 6) {
                    pdh.addCol(row, "district", addresses.get(1));
                    pdh.addCol(row, "sub-county", addresses.get(3) + " " + addresses.get(4));
                    pdh.addCol(row, "village", addresses.get(5));

                } else {
                    pdh.addCol(row, "district", "");
                    pdh.addCol(row, "sub-county", "");
                    pdh.addCol(row, "village", "");
                }

                pdh.addCol(row, "telephone", telephone);


                if (testBe4 != null) {
                    pdh.addCol(row, "first", "N");
                } else {
                    pdh.addCol(row, "first", "Y");
                }

                if (testLast12Months != null && Integer.valueOf(testLast12Months.getValue()) > 2) {
                    pdh.addCol(row, "tested > 2", "Y");
                } else {
                    pdh.addCol(row, "tested > 2", "N");
                }

                if (counselled != null) {
                    pdh.addCol(row, "counselled", "Y");
                } else {
                    pdh.addCol(row, "counselled", "N");
                }

                pdh.addCol(row, "tested", "-");


                if (receivedResults != null) {
                    pdh.addCol(row, "received", "Y");
                } else {
                    pdh.addCol(row, "received", "N");
                }

                if (hivResults != null) {
                    pdh.addCol(row, "results", convert(hivResults.getValue()));
                } else {
                    pdh.addCol(row, "results", "");
                }

                if (counselledAsCouple != null) {
                    pdh.addCol(row, "c-couple", "Y");
                } else {
                    pdh.addCol(row, "c-couple", "N");
                }

                if (resultAsCouple != null) {
                    pdh.addCol(row, "r-couple", "Y");
                } else {
                    pdh.addCol(row, "r-couple", "N");
                }

                if (discordantResults != null) {
                    pdh.addCol(row, "discordant", "Y");
                } else {
                    pdh.addCol(row, "discordant", "N");
                }

                if (hctEntry != null) {
                    pdh.addCol(row, "entry", convert(hctEntry.getValue()));
                } else {
                    pdh.addCol(row, "entry", "");
                }

                if (presumptiveTB != null) {
                    pdh.addCol(row, "tb", "Y");
                } else {
                    pdh.addCol(row, "tb", "N");
                }

                if (cd4 != null) {
                    pdh.addCol(row, "cd4", cd4.getValue());
                } else {
                    pdh.addCol(row, "cd4", "");
                }

                if (linkedToCare != null) {
                    pdh.addCol(row, "linked", "Y");
                    pdh.addCol(row, "where", "-");
                } else {
                    pdh.addCol(row, "linked", "N");
                    pdh.addCol(row, "where", "-");
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


        return conceptsNames.get(value);
    }

}
