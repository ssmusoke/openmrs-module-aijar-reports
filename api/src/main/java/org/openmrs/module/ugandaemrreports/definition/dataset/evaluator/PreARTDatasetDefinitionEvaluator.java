package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import com.google.common.base.Joiner;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.ugandaemrreports.common.*;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.PreARTDatasetDefinition;
import org.openmrs.module.ugandaemrreports.library.UgandaEMRReporting;

import java.sql.SQLException;
import java.util.*;

import static org.openmrs.module.ugandaemrreports.library.UgandaEMRReporting.*;
import static org.openmrs.module.ugandaemrreports.library.UgandaEMRReporting.processString;

/**
 * Created by carapai on 11/05/2016.
 */
@Handler(supports = {PreARTDatasetDefinition.class})
public class PreARTDatasetDefinitionEvaluator implements DataSetEvaluator {

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
        PreARTDatasetDefinition definition = (PreARTDatasetDefinition) dataSetDefinition;

        LocalDate localDate = StubDate.dateOf(definition.getStartDate());
        Integer year = localDate.getYear();
        String enrolledQuery = String.format("SELECT patient_id, DATE(encounter_datetime) as enrollment FROM encounter WHERE voided = 0 AND YEAR(encounter_datetime) = %s AND encounter_type = (SELECT encounter_type_id FROM encounter_type WHERE uuid = '8d5b27bc-c2cc-11de-8d13-0010c6dffd0f');", year);

        try {
            Multimap<Integer, Date> summaryData = getData(sqlConnection(), enrolledQuery, "patient_id", "enrollment");

            Map<Integer, Date> dates = new HashMap<>();

            for (Map.Entry<Integer, Collection<Date>> d : summaryData.asMap().entrySet()) {
                dates.put(d.getKey(), new ArrayList<>(d.getValue()).get(0));
            }

            List<Map.Entry<Integer, Date>> entries = new ArrayList<>(dates.entrySet());
            entries.sort(Comparator.comparing(Map.Entry::getValue));

            String currentQuarter = UgandaEMRReporting.getObsPeriod(new Date(), Enums.Period.QUARTERLY);

            String patients = Joiner.on(",").join(summaryData.keySet());
            String concepts = Joiner.on(",").join(preArtConcepts().keySet());

            String encountersBeforeArtQuery = "SELECT e.encounter_id as e_id,DATE(e.encounter_datetime) as e_date\n" +
                    "FROM encounter e INNER JOIN obs art ON (e.patient_id = art.person_id)\n" +
                    "WHERE art.concept_id = 99161 AND art.voided = 0 AND e.voided = 0 AND e.encounter_datetime <= art.value_datetime AND\n" +
                    String.format("      e.patient_id IN (%s)\n", patients) +
                    "      AND encounter_type IN (SELECT encounter_type_id\n" +
                    "                             FROM encounter_type\n" +
                    "                             WHERE uuid IN\n" +
                    "                                   ('8d5b27bc-c2cc-11de-8d13-0010c6dffd0f', '8d5b2be0-c2cc-11de-8d13-0010c6dffd0f'));";
            Multimap<Integer, Date> encounterData = getData(sqlConnection(), encountersBeforeArtQuery, "e_id", "e_date");

            String encounters = Joiner.on(",").join(encounterData.asMap().keySet());

            String obsQuery = "SELECT\n" +
                    "  person_id,\n" +
                    "  concept_id,\n" +
                    "  (SELECT encounter_datetime\n" +
                    "   FROM encounter e\n" +
                    "   WHERE e.encounter_id = o.encounter_id) as enc_date,\n" +
                    "  COALESCE(value_coded, COALESCE(DATE(value_datetime), COALESCE(value_numeric, value_text))) AS val\n" +
                    "FROM obs o\n" +
                    "WHERE o.voided = 0\n" +
                    String.format("      AND o.encounter_id IN (%s)\n", encounters) +
                    "      AND o.concept_id IN\n" +
                    String.format("          (%s)", concepts);


            Table<String, Integer, String> table = getDataTable(sqlConnection(), obsQuery);
            Map<Integer, List<PersonDemographics>> demographics = getPatientDemographics(sqlConnection(), patients);

            PatientDataHelper pdh = new PatientDataHelper();
            for (Map.Entry<Integer, Date> patient : entries) {
                List<PersonDemographics> personDemographics = demographics.get(patient.getKey());
                Map<String, String> patientData = table.column(patient.getKey());

                String artStartDate = getData(patientData, "99161");
                String entryPoint = getData(patientData, "90200");
                String tbStartDate = getData(patientData, "90217");
                String tbStopDate = getData(patientData, "90310");
                String ti = getData(patientData, "99110") == null ? "" : "5";
                String eligible = getData(patientData, "90297");
                String eligibleAndReady = getData(patientData, "90299");

                String eligibleCS = getData(patientData, "99083") == null ? "" : "1";
                String eligiblePregnant = getData(patientData, "99602") == null ? "" : "3";
                String eligibleCD4 = getData(patientData, "99082") == null ? "" : "2";
                String eligibleLactating = getData(patientData, "99601") == null ? "" : "4";
                String eligibleTb = getData(patientData, "99600") == null ? "" : "5";

                String startedTB = tbStartDate != null ? DateUtil.formatDate(DateUtil.parseYmd(tbStartDate), "MM/yyyy") : "";
                String stoppedTB = tbStopDate != null ? DateUtil.formatDate(DateUtil.parseYmd(tbStopDate), "MM/yyyy") : "";

                Map<String, Date> clinicalStages = getClinicalStages(patientData, "90203");

                PersonDemographics personDemos = personDemographics != null && personDemographics.size() > 0 ? personDemographics.get(0) : new PersonDemographics();

                List<String> addresses = processString2(personDemos.getAddresses());
                String address = addresses.get(1) + "\n" + addresses.get(3) + "\n" + addresses.get(4) + "\n" + addresses.get(5);

                Date firstSummaryDate = patient.getValue();

                String enrollmentQuarter = UgandaEMRReporting.getObsPeriod(firstSummaryDate, Enums.Period.QUARTERLY);
                DataSetRow row = new DataSetRow();
                pdh.addCol(row, "Date Enrolled", patient.getValue());
                pdh.addCol(row, "Unique ID no", patient.getKey());
                pdh.addCol(row, "Patient Clinic ID", processString(personDemos.getIdentifiers()).get("e1731641-30ab-102d-86b0-7a5022ba4115"));
                pdh.addCol(row, "Name", personDemos.getNames());
                pdh.addCol(row, "Gender", personDemos.getGender());
                if (personDemos.getBirthDate() != null && firstSummaryDate != null) {
                    Years age = Years.yearsBetween(StubDate.dateOf(personDemos.getBirthDate()), StubDate.dateOf(firstSummaryDate));
                    pdh.addCol(row, "Age", age.getYears());
                } else {
                    pdh.addCol(row, "Age", "");
                }
                pdh.addCol(row, "Address", address);
                pdh.addCol(row, "Entry Point", convert(entryPoint));
                pdh.addCol(row, "Enrollment", ti);
                pdh.addCol(row, "CPT", getMinimum(patientData, "99037"));
                pdh.addCol(row, "INH", getMinimum(patientData, "99604"));

                pdh.addCol(row, "TB", startedTB + "\n" + stoppedTB);

                pdh.addCol(row, "CS1", clinicalStages.get("1"));
                pdh.addCol(row, "CS2", clinicalStages.get("2"));
                pdh.addCol(row, "CS3", clinicalStages.get("3"));
                pdh.addCol(row, "CS4", clinicalStages.get("4"));

                pdh.addCol(row, "Date Eligible", eligible != null ? DateUtil.parseYmd(eligible) : "");
                pdh.addCol(row, "Why Eligible", eligibleCS + "\n" + eligibleCD4 + "\n" + eligiblePregnant + "\n" + eligibleLactating + "\n" + eligibleTb);
                pdh.addCol(row, "Date Eligible and Ready", eligibleAndReady != null ? DateUtil.parseYmd(eligibleAndReady) : "");
                pdh.addCol(row, "ART Start Date", artStartDate != null ? DateUtil.parseYmd(artStartDate) : "");

                for (int i = 0; i < 16; i++) {
                    String period = UgandaEMRReporting.getObsPeriod(Periods.addQuarters(localDate, i).get(0).toDate(), Enums.Period.QUARTERLY);
                    if (period.compareTo(currentQuarter) < 0) {
                        if (artStartDate != null) {
                            String periodStartedArt = UgandaEMRReporting.getObsPeriod(DateUtil.parseYmd(artStartDate), Enums.Period.QUARTERLY);
                            if (period.compareTo(periodStartedArt) == 0) {
                                pdh.addCol(row, "FUS" + String.valueOf(i), "ART");
                            } else if (period.compareTo(periodStartedArt) > 0) {
                                pdh.addCol(row, "FUS" + String.valueOf(i), "");
                            } else {
                                List<String> tbStatus = getData(patientData, period, "90216");
                                List<String> cpt = getData(patientData, period, "99037");
                                List<String> inh = getData(patientData, period, "99604");
                                List<String> nutrition = getData(patientData, period, "68");
                                List<String> appointments = getData(patientData, period, "5096");

                                String cptInh = "";
                                String mul = "";
                                String tb = "";

                                if (cpt.size() > 0 || inh.size() > 0) {
                                    cptInh = "Y";
                                }
                                if (nutrition.size() > 0) {
                                    mul = nutrition.get(nutrition.size() - 1);
                                }
                                if (tbStatus.size() > 0) {
                                    tb = convert(tbStatus.get(tbStatus.size() - 1));
                                }

                                if (StringUtils.isNotBlank(tb) || StringUtils.isNotBlank(cptInh) || StringUtils.isNotBlank(mul)) {
                                    pdh.addCol(row, "FUS" + String.valueOf(i), "✓" + "\n" + tb + "\n" + cptInh + "|" + mul);
                                } else if (appointments.size() == 0 && period.compareTo(enrollmentQuarter) >= 0) {
                                    String lastPeriod = UgandaEMRReporting.getObsPeriod(Periods.addQuarters(localDate, i - 1).get(0).toDate(), Enums.Period.QUARTERLY);
                                    List<String> lastAppointments = getData(patientData, lastPeriod, "5096");
                                    if (lastAppointments.size() != 0) {
                                        String maxAppointment = lastAppointments.get(lastAppointments.size() - 1);
                                        String maxQuarter = UgandaEMRReporting.getObsPeriod(DateUtil.parseYmd(maxAppointment), Enums.Period.QUARTERLY);
                                        if (maxQuarter.compareTo(period) > 0) {
                                            pdh.addCol(row, "FUS" + String.valueOf(i), "→");
                                        } else {
                                            pdh.addCol(row, "FUS" + String.valueOf(i), "LOST");
                                        }
                                    } else {
                                        pdh.addCol(row, "FUS" + String.valueOf(i), "LOST");
                                    }

                                } else if (period.compareTo(enrollmentQuarter) < 0) {
                                    pdh.addCol(row, "FUS" + String.valueOf(i), "");
                                } else {
                                    pdh.addCol(row, "FUS" + String.valueOf(i), "Processing problem");
                                }
                            }
                        }
                    } else {
                        pdh.addCol(row, "FUS" + String.valueOf(i), "");
                    }
                }
                dataSet.addRow(row);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return dataSet;
    }
}
