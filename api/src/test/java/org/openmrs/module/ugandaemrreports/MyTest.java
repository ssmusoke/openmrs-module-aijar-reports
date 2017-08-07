package org.openmrs.module.ugandaemrreports;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.junit.Test;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.ugandaemrreports.common.*;
import org.openmrs.module.ugandaemrreports.definition.predicates.SummarizedObsPatientPredicate;
import org.openmrs.module.ugandaemrreports.definition.predicates.SummarizedObsPredicate;
import org.openmrs.module.ugandaemrreports.library.UgandaEMRReporting;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.openmrs.module.ugandaemrreports.library.UgandaEMRReporting.*;

public class MyTest {
    Date d = DateUtil.parseDate("2015-07-01", "yyyy-MM-dd");

    @Test
    public void shouldJoinStringUsingOR() throws IOException, ParseException {
        assertEquals(UgandaEMRReporting.joinQuery("1", "2", Enums.UgandaEMRJoiner.OR), "1 OR 2");
    }

    @Test
    public void shouldJoinStringUsingAND() throws IOException, ParseException {
        assertEquals(UgandaEMRReporting.joinQuery("1", "2", Enums.UgandaEMRJoiner.AND), "1 AND 2");
    }

    @Test
    public void shouldComplexJoin() throws IOException, ParseException {
        assertEquals(UgandaEMRReporting.joinQuery(UgandaEMRReporting.constructSQLInQuery("name", Arrays.asList(1, 2, 4, 53, 3)), "2", Enums.UgandaEMRJoiner.AND), "name IN(1,2,4,53,3) AND 2");
    }

    @Test
    public void shouldConstructMonthlyPeriod() throws IOException, ParseException {

        assertEquals(UgandaEMRReporting.getObsPeriod(d, Enums.Period.MONTHLY), "201507");
    }

    @Test
    public void shouldConstructQuarterlyPeriod() throws IOException, ParseException {

        assertEquals(UgandaEMRReporting.getObsPeriod(d, Enums.Period.QUARTERLY), "2015Q3");
    }

    @Test
    public void shouldConstructYearlyPeriod() throws IOException, ParseException {

        assertEquals(UgandaEMRReporting.getObsPeriod(d, Enums.Period.YEARLY), "2015");
    }

    @Test
    public void shouldConstructWeeklyPeriod() throws IOException, ParseException {

        assertEquals(UgandaEMRReporting.getObsPeriod(d, Enums.Period.WEEKLY), "2015W27");
    }

    /*@Test
    public void shouldLoopArtYearsProperly() {
        LocalDate localDate = StubDate.dateOf(d);
        for (int i = 0; i <= 72; i++) {
            String period = UgandaEMRReporting.getObsPeriod(Periods.addMonths(localDate, i).get(0).toDate(), Enums.Period.MONTHLY);
            System.out.println(period);
        }
    }

    @Test
    public void shouldLoopPreArtQuartersProperly() {
        LocalDate localDate = StubDate.dateOf(d);
        for (int i = 0; i < 16; i++) {
            String period = UgandaEMRReporting.getObsPeriod(Periods.addQuarters(localDate, i).get(0).toDate(), Enums.Period.QUARTERLY);
            System.out.println(period);
        }
    }*/

    /*@Test
    public void shouldReturnSqlConnection() throws SQLException, ClassNotFoundException {
        assertNotNull(UgandaEMRReporting.testSqlConnection());
    }*/

    /*@Test
    public void shouldDenormalizeObs() throws SQLException, ClassNotFoundException {
        Connection connection = UgandaEMRReporting.testSqlConnection();
        int response = UgandaEMRReporting.normalizeObs("1900-01-01", connection, 100000);
        assertNotEquals(response, 0);
    }

    @Test
    public void shouldSummarizeObs() throws SQLException, ClassNotFoundException {
        Connection connection = UgandaEMRReporting.testSqlConnection();
        int response = UgandaEMRReporting.summarizeObs(UgandaEMRReporting.obsSummaryMonthQuery("1900-01-01"), connection);
        assertNotEquals(response, 0);
    }*/

    /*@Test
    public void shouldGenerateArtRegister() throws SQLException, ClassNotFoundException {

        String month = UgandaEMRReporting.getObsPeriod(d, Enums.Period.MONTHLY);
        String currentMonth = UgandaEMRReporting.getObsPeriod(new Date(), Enums.Period.MONTHLY);
        LocalDate localDate = StubDate.dateOf(d);

        try {
            Connection connection = UgandaEMRReporting.testSqlConnection();
            List<SummarizedObs> startedArtThisMonth = UgandaEMRReporting.getSummarizedObs(connection, month, "value_datetime", "ab505422-26d9-41f1-a079-c3d222000440");
            String allPatients = summarizedObsPatientsToString(startedArtThisMonth);

            List<String> patients = Splitter.on(",").splitToList(allPatients);

            Map<Integer, List<PersonDemographics>> demographics = getPatientDemographics(connection, allPatients);

            List<String> encounterSummaryConcepts = new ArrayList<>(artRegisterConcepts().values());

            List<SummarizedObs> summarizedObs = getSummarizedObs(connection, encounterSummaryConcepts, patients);


            PatientDataHelper pdh = new PatientDataHelper();

            for (String patient : patients) {
                DataSetRow row = new DataSetRow();
                List<PersonDemographics> personDemographics = demographics.get(Integer.valueOf(patient));
                List<SummarizedObs> personObs = new ArrayList<>(Collections2.filter(summarizedObs, new SummarizedObsPatientPredicate(patient)));

                PersonDemographics personDemos = personDemographics != null && personDemographics.size() > 0 ? personDemographics.get(0) : new PersonDemographics();

                List<SummarizedObs> viralLoadResults = new ArrayList<>(Collections2.filter(personObs, new SummarizedObsPredicate("dc8d83e3-30ab-102d-86b0-7a5022ba4115", null)));
                List<SummarizedObs> appointments = new ArrayList<>(Collections2.filter(personObs, new SummarizedObsPredicate("dcac04cf-30ab-102d-86b0-7a5022ba4115", null)));
                Map<String, String> convertedAppointments = getSummarizedObsValuesAsMap(appointments, patient);

                String firstViralLoad = getSummarizedObsValue(viralLoad(viralLoadResults, 0), patient);

                String baselineWeight = getSummarizedObsValue(getSummarizedObs(personObs, null, "900b8fd9-2039-4efc-897b-9b8ce37396f5"), patient);
                SummarizedObs baselineCs = getSummarizedObs(personObs, null, "39243cef-b375-44b1-9e79-cbf21bd10878");
                String baselineCd4 = getSummarizedObsValue(getSummarizedObs(personObs, null, "c17bd9df-23e6-4e65-ba42-eb6d9250ca3f"), patient);
                SummarizedObs baselineRegimen = getSummarizedObs(personObs, null, "c3332e8d-2548-4ad6-931d-6855692694a3");
                String artStartDate = getSummarizedObsValue(getSummarizedObs(personObs, month, "ab505422-26d9-41f1-a079-c3d222000440"), patient);

                String tbStartDate = getSummarizedObsValue(getSummarizedObs(personObs, null, "dce02eca-30ab-102d-86b0-7a5022ba4115"), patient);
                String tbStopDate = getSummarizedObsValue(getSummarizedObs(personObs, null, "dd2adde2-30ab-102d-86b0-7a5022ba4115"), patient);

                String tiDate = getSummarizedObsValue(getSummarizedObs(personObs, null, "f363f153-f659-438b-802f-9cc1828b5fa9"), patient);

                SummarizedObs entry = getSummarizedObs(personObs, null, "dcdfe3ce-30ab-102d-86b0-7a5022ba4115");

                SummarizedObs functionalStatusDuringArtStart = getSummarizedObs(personObs, month, "dce09a15-30ab-102d-86b0-7a5022ba4115");

                String ti = "";

                Years age = Years.yearsBetween(StubDate.dateOf(personDemos.getBirthDate()), StubDate.dateOf(artStartDate));

                if (!Strings.isNullOrEmpty(tiDate) || (entry != null && entry.getValueCoded().equals("dcd7e8e5-30ab-102d-86b0-7a5022ba4115"))) {
                    ti = "✔";
                }
                List<String> addresses = processString2(personDemos.getAddresses());
                String address = addresses.get(1) + "\n" + addresses.get(3) + "\n" + addresses.get(4) + "\n" + addresses.get(5);

                pdh.addCol(row, "Date ART Started", artStartDate);
                pdh.addCol(row, "Unique ID no", patient);
                pdh.addCol(row, "TI", ti);
                pdh.addCol(row, "Patient Clinic ID", processString(personDemos.getIdentifiers()).get("e1731641-30ab-102d-86b0-7a5022ba4115"));
                pdh.addCol(row, "Name", personDemos.getNames());
                pdh.addCol(row, "Gender", personDemos.getGender());
                pdh.addCol(row, "Age", age.getYears());
                pdh.addCol(row, "Address", address);
                pdh.addCol(row, "Weight", baselineWeight);


                if (functionalStatusDuringArtStart != null) {
                    pdh.addCol(row, "FUS", functionalStatusDuringArtStart.getValueCodedName());
                } else {
                    pdh.addCol(row, "FUS", "");
                }


                if (baselineCs != null) {
                    pdh.addCol(row, "CS", baselineCs.getReportName());
                } else {
                    pdh.addCol(row, "CS", "");
                }

                pdh.addCol(row, "CDVL", baselineCd4 + "\n" + firstViralLoad);

                pdh.addCol(row, "CPT", "");
                pdh.addCol(row, "INH", "");
                pdh.addCol(row, "TB", tbStartDate + "\n" + tbStopDate);
                pdh.addCol(row, "P1", "");
                pdh.addCol(row, "P2", "");
                pdh.addCol(row, "P3", "");
                pdh.addCol(row, "P4", "");

                if (baselineRegimen != null) {
                    pdh.addCol(row, "BASE REGIMEN", baselineRegimen.getReportName());
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

                    if (period <= Integer.valueOf(currentMonth)) {

                        SummarizedObs tbStatus = getSummarizedObs(personObs, workingMonth, "dce02aa1-30ab-102d-86b0-7a5022ba4115");
                        SummarizedObs arvAdh = getSummarizedObs(personObs, workingMonth, "dce03b2f-30ab-102d-86b0-7a5022ba4115");

                        String inhDosage = getSummarizedObsValue(getSummarizedObs(personObs, workingMonth, "be211d29-1507-4e2e-9906-4bfeae4ddc1f"), patient);
                        String cptDosage = getSummarizedObsValue(getSummarizedObs(personObs, workingMonth, "38801143-01ac-4328-b0e1-a7b23c84c8a3"), patient);

                        SummarizedObs currentRegimen = getSummarizedObs(personObs, workingMonth, "dd2b0b4d-30ab-102d-86b0-7a5022ba4115");
                        String returnDate = getSummarizedObsValue(getSummarizedObs(personObs, workingMonth, "dcac04cf-30ab-102d-86b0-7a5022ba4115"), patient);

                        String latestAppointment = getMostRecentValue(convertedAppointments, workingMonth);

                        String arvStopDate = getSummarizedObsValue(getSummarizedObs(personObs, workingMonth, "cd36c403-d88c-4496-96e2-09af6da090c1"), patient);
                        String arvRestartDate = getSummarizedObsValue(getSummarizedObs(personObs, workingMonth, "406e1978-8c2e-40c5-b04e-ae214fdfed0e"), patient);
                        String toDate = getSummarizedObsValue(getSummarizedObs(personObs, workingMonth, "fc1b1e96-4afb-423b-87e5-bb80d451c967"), patient);

                        SummarizedObs currentlyDead = getSummarizedObs(personObs, workingMonth, "deaths");

                        String arvDays = getSummarizedObsValue(getSummarizedObs(personObs, workingMonth, "7593ede6-6574-4326-a8a6-3d742e843659"), patient);

                        String tb = "";
                        String adherence = "";
                        String cotrim = "";
                        String regimen = "";
                        String status = "";
                        String adherenceAndCPT = "";

                        if (tbStatus != null) {
                            tb = tbStatus.getReportName();
                        }

                        if (arvAdh != null) {
                            adherence = arvAdh.getReportName();
                        }
                        if (!Strings.isNullOrEmpty(inhDosage) || !Strings.isNullOrEmpty(cptDosage)) {
                            cotrim = "Y";
                        }

                        if (currentlyDead != null) {
                            status = "1";
                        }

                        if (StringUtils.isNotBlank(arvStopDate)) {
                            status = "2";
                        }

                        if (StringUtils.isNotBlank(arvRestartDate)) {
                            status = "6";
                        }

                        if (StringUtils.isNotBlank(toDate)) {
                            status = "5";
                        }

                        if (currentRegimen != null) {
                            regimen = currentRegimen.getReportName();
                        } else if (StringUtils.isNotBlank(status)) {
                            regimen = status;
                        } else if (StringUtils.isNotBlank(returnDate)) {
                            regimen = "3";
                        } else if (latestAppointment != null) {
                            List<String> data = Splitter.on("-").splitToList(latestAppointment);
                            Integer appointment = Integer.valueOf(data.get(0) + data.get(1));

                            if (appointment >= period) {
                                regimen = "→";
                            } else {
                                Integer diff = period - appointment;

                                if (diff < 3) {
                                    regimen = "3";
                                } else {
                                    regimen = "4";
                                }
                            }


                        }

                        if (StringUtils.isNotBlank(adherence) && StringUtils.isNotBlank(cotrim)) {
                            adherenceAndCPT = adherence + "|" + cotrim;
                        } else if (StringUtils.isNotBlank(adherence)) {
                            adherenceAndCPT = adherence;
                        } else if (StringUtils.isNotBlank(cotrim)) {
                            adherenceAndCPT = cotrim;
                        }
                        pdh.addCol(row, "FUS" + String.valueOf(i), regimen + "\n" + tb + "\n" + adherenceAndCPT);

                        if (i == 6 || i == 12 || i == 24 || i == 36 || i == 48 || i == 60 || i == 72) {
                            String weight = getSummarizedObsValue(getSummarizedObs(personObs, workingMonth, "dce09e2f-30ab-102d-86b0-7a5022ba4115"), patient);
                            String cd4 = getSummarizedObsValue(getSummarizedObs(personObs, workingMonth, "dcbcba2c-30ab-102d-86b0-7a5022ba4115"), patient);
                            SummarizedObs clinicalStage = getSummarizedObs(personObs, workingMonth, "dcdff274-30ab-102d-86b0-7a5022ba4115");

                            String vl = getSummarizedObsValue(viralLoad(viralLoadResults, i), patient);


                            if (clinicalStage != null) {
                                pdh.addCol(row, "CI" + String.valueOf(i), clinicalStage.getReportName());
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
                // dataSet.addRow(row);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        // return dataSet;
    }*/
}