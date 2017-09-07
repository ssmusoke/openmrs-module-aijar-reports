package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
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
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.ARTDatasetDefinition;
import org.openmrs.module.ugandaemrreports.definition.predicates.SummarizedObsPatientPredicate;
import org.openmrs.module.ugandaemrreports.definition.predicates.SummarizedObsPredicate;
import org.openmrs.module.ugandaemrreports.library.UgandaEMRReporting;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.openmrs.module.ugandaemrreports.library.UgandaEMRReporting.*;

/**
 * Created by carapai on 11/05/2016.
 */
@Handler(supports = {ARTDatasetDefinition.class})
public class ARTDatasetDefinitionEvaluator implements DataSetEvaluator {

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
        ARTDatasetDefinition definition = (ARTDatasetDefinition) dataSetDefinition;

        String month = getObsPeriod(definition.getStartDate(), Enums.Period.MONTHLY);
        Integer currentMonth = Integer.valueOf(getObsPeriod(new Date(), Enums.Period.MONTHLY));
        LocalDate localDate = StubDate.dateOf(definition.getStartDate());

        Map<String, String> where = new HashMap<>();

        where.put("y", String.valueOf(localDate.getYear()));
        where.put("m", String.valueOf(localDate.getMonthOfYear()));
        where.put("concept", "99161");

        try {
            Connection connection = testSqlConnection();
            List<SummarizedObs> startedArtThisMonth = getSummarizedObs(connection, where);

            String allPatients = summarizedObsPatientsToString(startedArtThisMonth);

            Map<Integer, Date> dates = new HashMap<>();

            for (Map.Entry<Integer, Collection<Date>> d : getData(startedArtThisMonth.get(0).getVals()).asMap().entrySet()) {
                dates.put(d.getKey(), new ArrayList<>(d.getValue()).get(0));
            }

            List<Map.Entry<Integer, Date>> entries = new ArrayList<>(dates.entrySet());
            entries.sort(Comparator.comparing(Map.Entry::getValue));

            Map<Integer, List<PersonDemographics>> demographics = getPatientDemographics(connection, allPatients);

            String concepts = Joiner.on(",").join(artRegisterConcepts().values());


            String encountersBeforeArtQuery = "SELECT\n" +
                    "  e.encounter_id             AS e_id,\n" +
                    "  DATE(e.encounter_datetime) AS e_date\n" +
                    "FROM encounter e INNER JOIN obs art ON (e.patient_id = art.person_id)\n" +
                    "WHERE art.concept_id = 99161 AND art.voided = 0 AND e.voided = 0 AND e.encounter_datetime >= art.value_datetime AND\n" +
                    "      e.patient_id IN (%s)\n" +
                    "      AND encounter_type = (SELECT encounter_type_id\n" +
                    "                            FROM encounter_type\n" +
                    "                            WHERE uuid = '8d5b2be0-c2cc-11de-8d13-0010c6dffd0f')\n" +
                    "UNION ALL\n" +
                    "SELECT\n" +
                    "  e.encounter_id             AS e_id,\n" +
                    "  DATE(e.encounter_datetime) AS e_date\n" +
                    "FROM encounter e\n" +
                    "WHERE e.patient_id IN (%s) AND e.voided = 0 AND e.encounter_type = (SELECT encounter_type_id\n" +
                    "                                                 FROM encounter_type\n" +
                    "                                                 WHERE uuid = '8d5b27bc-c2cc-11de-8d13-0010c6dffd0f');";

            encountersBeforeArtQuery = encountersBeforeArtQuery.replaceAll("%s", allPatients);
            Multimap<Integer, Date> encounterData = getData(connection, encountersBeforeArtQuery, "e_id", "e_date");

            String encounters = Joiner.on(",").join(encounterData.asMap().keySet());

            String obsQuery = String.format("SELECT\n" +
                    "  person_id,\n" +
                    "  concept_id,\n" +
                    "  encounter_id,\n" +
                    "  (SELECT encounter_datetime\n" +
                    "   FROM encounter e\n" +
                    "   WHERE e.encounter_id = o.encounter_id)                                                    AS enc_date,\n" +
                    "  COALESCE(value_coded, COALESCE(DATE(value_datetime), COALESCE(value_numeric, value_text))) AS val\n" +
                    "FROM obs o\n" +
                    "WHERE o.voided = 0 AND o.encounter_id IN (%s) AND o.concept_id IN (%s)\n" +
                    "UNION ALL\n" +
                    "SELECT\n" +
                    "  p.person_id,\n" +
                    "  0,\n" +
                    "  0,\n" +
                    "  death_date,\n" +
                    "  DATE(death_date)\n" +
                    "FROM person p INNER JOIN obs art ON (p.person_id = art.person_id)\n" +
                    "WHERE art.concept_id = 99161 AND p.person_id IN (%s) AND art.voided = 0 AND p.voided = 0 AND\n" +
                    "      p.death_date >= art.value_datetime;", encounters, concepts, allPatients);

            List<ObsData> table = getData(connection, obsQuery);

            PatientDataHelper pdh = new PatientDataHelper();

            for (Map.Entry<Integer, Date> patient : entries) {
                DataSetRow row = new DataSetRow();

                Integer key = patient.getKey();

                List<PersonDemographics> personDemographics = demographics.get(key);

                List<ObsData> patientData = table.stream()
                        .filter(line -> line.getPatientId().compareTo(key) == 0)
                        .collect(Collectors.toList());

                ObsData artStartDate = getData(patientData, "99161");
                ObsData tbStartDate = getData(patientData, "90217");
                ObsData tbStopDate = getData(patientData, "90310");
                ObsData ti = getData(patientData, "99160");
                ObsData death = getData(patientData, "death");
                ObsData to = getData(patientData, "99165");
                ObsData baselineWeight = getData(patientData, "99069");
                ObsData baselineCs = getData(patientData, "99070");
                ObsData baselineCd4 = getData(patientData, "99071");
                ObsData baselineRegimen = getData(patientData, "99061");


                String died = death != null ? getObsPeriod(DateUtil.parseYmd(death.getVal()), Enums.Period.MONTHLY) : "";
                String transferred = to != null ? getObsPeriod(DateUtil.parseYmd(to.getVal()), Enums.Period.MONTHLY) : "";

                boolean hasDied = false;
                boolean hasTransferred = false;

                String startedTB = tbStartDate != null ? DateUtil.formatDate(DateUtil.parseYmd(tbStartDate.getVal()), "MM/yyyy") : "";
                String stoppedTB = tbStopDate != null ? DateUtil.formatDate(DateUtil.parseYmd(tbStopDate.getVal()), "MM/yyyy") : "";


                PersonDemographics personDemos = personDemographics != null && personDemographics.size() > 0 ? personDemographics.get(0) : new PersonDemographics();

                List<String> addresses = processString2(personDemos.getAddresses());
                String address = "";
                if (addresses.size() == 6) {
                    address = addresses.get(1) + "\n" + addresses.get(3) + "\n" + addresses.get(4) + "\n" + addresses.get(5);
                }

                pdh.addCol(row, "Date ART Started", artStartDate);
                pdh.addCol(row, "Unique ID no", patient);
                pdh.addCol(row, "TI", ti);
                pdh.addCol(row, "Patient Clinic ID", processString(personDemos.getIdentifiers()).get("e1731641-30ab-102d-86b0-7a5022ba4115"));
                pdh.addCol(row, "Name", personDemos.getNames());
                pdh.addCol(row, "Gender", personDemos.getGender());
                if (personDemos.getBirthDate() != null && artStartDate != null) {
                    Years age = Years.yearsBetween(StubDate.dateOf(personDemos.getBirthDate()), StubDate.dateOf(patient.getValue()));
                    pdh.addCol(row, "Age", age.getYears());
                } else {
                    pdh.addCol(row, "Age", "");
                }
                pdh.addCol(row, "Address", address);
                pdh.addCol(row, "Weight", baselineWeight);

                ObsData functionalStatusDuringArtStart = getFirstData(patientData, "90235");

                ObsData firstCPT = getFirstData(patientData, "99037");
                ObsData firstINH = getFirstData(patientData, "99604");
                List<ObsData> viralLoads = getDataAsList(patientData, "856");

                ObsData firstViralLoad = viralLoad(viralLoads, 6);

                String fvl = "";

                if (firstViralLoad != null) {
                    fvl = firstViralLoad.getVal();
                }

                if (functionalStatusDuringArtStart != null) {
                    pdh.addCol(row, "FUS", functionalStatusDuringArtStart.getVal());
                } else {
                    pdh.addCol(row, "FUS", "");
                }


                if (baselineCs != null) {
                    pdh.addCol(row, "CS", baselineCs.getVal());
                } else {
                    pdh.addCol(row, "CS", "");
                }

                pdh.addCol(row, "CDVL", baselineCd4 == null ? "" : baselineCd4.getVal() + "\n" + fvl);

                pdh.addCol(row, "CPT", firstCPT == null ? "" : DateUtil.formatDate(firstCPT.getEncounterDate(), "MM/yyyy"));
                pdh.addCol(row, "INH", firstINH == null ? "" : DateUtil.formatDate(firstINH.getEncounterDate(), "MM/yyyy"));
                pdh.addCol(row, "TB", startedTB + "\n" + stoppedTB);
                pdh.addCol(row, "P1", "");
                pdh.addCol(row, "P2", "");
                pdh.addCol(row, "P3", "");
                pdh.addCol(row, "P4", "");

                if (baselineRegimen != null) {
                    pdh.addCol(row, "BASE REGIMEN", baselineRegimen);
                } else {
                    pdh.addCol(row, "BASE REGIMEN", "");
                }

                pdh.addCol(row, "L1S", "");
                pdh.addCol(row, "L2S", "");
                pdh.addCol(row, "L3S", "");
                pdh.addCol(row, "Patient Clinic ID", processString(personDemos.getIdentifiers()).get("e1731641-30ab-102d-86b0-7a5022ba4115"));


                for (int i = 0; i <= 72; i++) {
                    String workingMonth = getObsPeriod(Periods.addMonths(localDate, i).get(0).toDate(), Enums.Period.MONTHLY);
                    Integer period = Integer.valueOf(workingMonth);

                    if (period <= currentMonth && (!hasDied || !hasTransferred)) {

                        ObsData tbStatus = getData(patientData, workingMonth, "90216");
                        ObsData arvAdh = getData(patientData, workingMonth, "90221");

                        ObsData inhDosage = getData(patientData, workingMonth, "99604");
                        ObsData cptDosage = getData(patientData, workingMonth, "99037");

                        ObsData currentRegimen = getData(patientData, workingMonth, "90315");
                        ObsData returnDate = getData(patientData, workingMonth, "5096");

                        ObsData arvStopDate = getData(patientData, workingMonth, "99084");
                        ObsData arvRestartDate = getData(patientData, workingMonth, "99085");

                        ObsData toDate = getData(patientData, workingMonth, "99165");
                        ObsData currentlyDead = getData(patientData, workingMonth, "deaths");

                        String cotrim = "";
                        String status = "";
                        String adherenceAndCPT = "";

                        if (inhDosage != null || cptDosage != null) {
                            cotrim = "Y";
                        }
                        if (currentRegimen != null) {
                            status = currentRegimen.getVal();
                        } else if (returnDate != null) {
                            status = "3";
                        } else {
                            if (arvStopDate != null) {
                                status = "2";
                            } else if (arvRestartDate != null) {
                                status = "6";
                            } else if (currentlyDead != null) {
                                status = "1";
                                hasDied = true;
                            } else if (toDate != null) {
                                status = "5";
                                hasTransferred = true;
                            } else {
                                ObsData lastAppointment = getLastAppointments(patientData, workingMonth);

                                if (lastAppointment != null) {
                                    Integer appointmentPeriod = Integer.parseInt(getObsPeriod(DateUtil.parseYmd(lastAppointment.getVal()), Enums.Period.MONTHLY));
                                    Integer diff = period - appointmentPeriod;
                                    if (diff < 0) {
                                        status = "→";
                                    } else if (diff < 3) {
                                        status = "3";
                                    } else {
                                        status = "4";
                                    }
                                }
                            }

                        }

                        if (arvAdh != null && cotrim != null) {
                            adherenceAndCPT = arvAdh.getVal() + "|" + cotrim;
                        } else if (arvAdh != null) {
                            adherenceAndCPT = arvAdh.getVal();
                        } else if (cotrim != null) {
                            adherenceAndCPT = cotrim;
                        }
                        pdh.addCol(row, "FUS" + String.valueOf(i), status + "\n" + tbStatus + "\n" + adherenceAndCPT);

                        if (i == 6 || i == 12 || i == 24 || i == 36 || i == 48 || i == 60 || i == 72) {
                            ObsData weight = getData(patientData, workingMonth, "90236");
                            ObsData cd4 = getData(patientData, workingMonth, "5497");
                            ObsData clinicalStage = getData(patientData, workingMonth, "90203");
                            ObsData viralLoad = viralLoad(viralLoads, i);

                            String vl = viralLoad == null ? "" : viralLoad.getVal();

                            if (clinicalStage != null) {
                                pdh.addCol(row, "CI" + String.valueOf(i), clinicalStage);
                            } else {
                                pdh.addCol(row, "CI" + String.valueOf(i), "");
                            }

                            pdh.addCol(row, "W" + String.valueOf(i), weight);

                            pdh.addCol(row, "CDVL" + String.valueOf(i), cd4 + "\n" + vl);
                        }
                    } else {
                        pdh.addCol(row, "FUS" + String.valueOf(i), "");
                        if (i == 6 || i == 12 || i == 24 || i == 36 || i == 48 || i == 60 || i == 72) {
                            pdh.addCol(row, "CI" + String.valueOf(i), "");
                            pdh.addCol(row, "W" + String.valueOf(i), "");
                            pdh.addCol(row, "CDVL" + String.valueOf(i), "");
                        }
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
