package org.openmrs.module.ugandaemrreports;

import com.google.common.base.Splitter;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.ugandaemrreports.common.*;
import org.openmrs.module.ugandaemrreports.library.UgandaEMRReporting;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.openmrs.module.ugandaemrreports.library.UgandaEMRReporting.*;
import static org.openmrs.module.ugandaemrreports.library.UgandaEMRReporting.artRegisterEncounterConcepts;
import static org.openmrs.module.ugandaemrreports.library.UgandaEMRReporting.artRegisterSummaryConcepts;

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

    @Test
    public void shouldReturnSqlConnection() throws SQLException, ClassNotFoundException {
        Connection connection = UgandaEMRReporting.testSqlConnection();
        Map<String, String> concepts = UgandaEMRReporting.getConceptsTypes(connection);
        assertNotEquals(concepts.size(), 0);
    }

    @Test
    public void shouldSummarizeObs() throws SQLException, ClassNotFoundException {
        Connection connection = UgandaEMRReporting.testSqlConnection();
        int response = UgandaEMRReporting.summarizeObs(UgandaEMRReporting.obsSummaryMonthQuery("1900-01-01"), connection);
        assertNotEquals(response, 0);
    }

    @Test
    public void shouldGenerateArtRegister() throws SQLException, ClassNotFoundException {
        Connection connection = UgandaEMRReporting.testSqlConnection();

        String month = UgandaEMRReporting.getObsPeriod(d, Enums.Period.MONTHLY);
        String currentMonth = UgandaEMRReporting.getObsPeriod(new Date(), Enums.Period.MONTHLY);
        LocalDate localDate = StubDate.dateOf(d);

        List<SummarizedObs> startedArtThisMonth = UgandaEMRReporting.getSummarizedObs(connection, month, "value_datetime", "ab505422-26d9-41f1-a079-c3d222000440");
        String allPatients = summarizedObsPatientsToString(startedArtThisMonth);

        List<String> patients = Splitter.on(",").splitToList(allPatients);

        Map<Integer, List<PersonDemographics>> demographics = getPatientDemographics(connection, allPatients);

        List<String> encounterSummaryConcepts = new ArrayList<>(artRegisterConcepts().values());
        List<String> summaryConcepts = new ArrayList<>(artRegisterSummaryConcepts().values());
        List<String> encounterConcepts = new ArrayList<>(artRegisterEncounterConcepts().values());

        Map<String, Map<String, List<SummarizedObs>>> summarizedObs = getSummarizedObs(connection, month, encounterSummaryConcepts, "8d5b2be0-c2cc-11de-8d13-0010c6dffd0f");
        Map<Integer, Map<String, List<NormalizedObs>>> artSummaryObs = getNormalizedObs(connection, summaryConcepts, allPatients, "8d5b27bc-c2cc-11de-8d13-0010c6dffd0f");
        Map<Integer, Map<String, List<NormalizedObs>>> artEncounterObs = getNormalizedObs(connection, encounterConcepts, allPatients, "8d5b2be0-c2cc-11de-8d13-0010c6dffd0f");

        PatientDataHelper pdh = new PatientDataHelper();

        for (String patient : patients) {
            DataSetRow row = new DataSetRow();
            String ti = "";
            List<PersonDemographics> personDemographics = demographics.get(Integer.valueOf(patient));

            PersonDemographics personDemos = personDemographics != null && personDemographics.size() > 0 ? personDemographics.get(0) : new PersonDemographics();

            Map<String, List<NormalizedObs>> patientSummaryPage = artSummaryObs.get(Integer.valueOf(patient));
            Map<String, List<NormalizedObs>> patientOtherEncounters = artEncounterObs.get(Integer.valueOf(patient));

            List<NormalizedObs> stop = patientSummaryPage.get("cd36c403-d88c-4496-96e2-09af6da090c1");
            List<NormalizedObs> restart = patientSummaryPage.get("406e1978-8c2e-40c5-b04e-ae214fdfed0e");
            List<NormalizedObs> to = patientSummaryPage.get("fc1b1e96-4afb-423b-87e5-bb80d451c967");
            List<NormalizedObs> artStartDate = patientSummaryPage.get("ab505422-26d9-41f1-a079-c3d222000440");
            List<NormalizedObs> artTI = patientSummaryPage.get("f363f153-f659-438b-802f-9cc1828b5fa9");
            List<NormalizedObs> entry = patientSummaryPage.get("dcdfe3ce-30ab-102d-86b0-7a5022ba4115");

            List<NormalizedObs> baselineWeight = patientSummaryPage.get("900b8fd9-2039-4efc-897b-9b8ce37396f5");
            List<NormalizedObs> baselineCS = patientSummaryPage.get("39243cef-b375-44b1-9e79-cbf21bd10878");
            List<NormalizedObs> baselineCD4 = patientSummaryPage.get("c17bd9df-23e6-4e65-ba42-eb6d9250ca3f");
            List<NormalizedObs> baselineRegimen = patientSummaryPage.get("c3332e8d-2548-4ad6-931d-6855692694a3");

            List<NormalizedObs> weights = patientOtherEncounters.get("dce09e2f-30ab-102d-86b0-7a5022ba4115");
            List<NormalizedObs> cd4s = patientOtherEncounters.get("dcbcba2c-30ab-102d-86b0-7a5022ba4115");
            List<NormalizedObs> vls = patientOtherEncounters.get("dc8d83e3-30ab-102d-86b0-7a5022ba4115");
            List<NormalizedObs> vlDates = patientOtherEncounters.get("0b434cfa-b11c-4d14-aaa2-9aed6ca2da88");
            List<NormalizedObs> arvDays = patientOtherEncounters.get("7593ede6-6574-4326-a8a6-3d742e843659");

            List<SummarizedObs> functionalStatus = summarizedObs.get(month).get("dce09a15-30ab-102d-86b0-7a5022ba4115");

            if ((artTI != null && artTI.size() > 0) || (entry != null && entry.size() > 0 && entry.get(0).getValueCoded().equals("dcd7e8e5-30ab-102d-86b0-7a5022ba4115"))) {
                ti = "✔";
            }

            pdh.addCol(row, "Date ART Started", artStartDate.get(0).getValueDatetime());
            pdh.addCol(row, "Unique ID no", patient);
            pdh.addCol(row, "TI", ti);
            pdh.addCol(row, "Patient Clinic ID", processString(personDemos.getIdentifiers()).get("e1731641-30ab-102d-86b0-7a5022ba4115"));
            pdh.addCol(row, "Name", personDemos.getNames());
            pdh.addCol(row, "Gender", personDemos.getGender());
            pdh.addCol(row, "Age", artStartDate.get(0).getAgeAtValueDatetime());
            // TODO display address correctly
            pdh.addCol(row, "Address", personDemos.getAddresses());

            SummarizedObs currentFunctionalStatus = patientInGroup(functionalStatus, patient, "obs_datetime", true);

            if (currentFunctionalStatus != null) {
                pdh.addCol(row, "FUS", currentFunctionalStatus.getValueCodedName());
            } else {
                pdh.addCol(row, "FUS", "");
            }

            if (baselineWeight != null && baselineWeight.size() > 0) {
                pdh.addCol(row, "Weight", baselineWeight.get(0).getValueNumeric());
            } else {
                pdh.addCol(row, "Weight", "");
            }

            if (baselineCS != null && baselineCS.size() > 0) {
                pdh.addCol(row, "CS", baselineCS.get(0).getReportName());
            } else {
                pdh.addCol(row, "CS", "");
            }

            if (baselineCD4 != null && baselineCD4.size() > 0) {
                pdh.addCol(row, "CD4VL", baselineCD4.get(0).getValueNumeric());
            } else {
                pdh.addCol(row, "CD4VL", "");
            }

            pdh.addCol(row, "CPT", "");
            pdh.addCol(row, "INH", "");
            pdh.addCol(row, "TB", "");
            pdh.addCol(row, "P1", "");
            pdh.addCol(row, "P2", "");
            pdh.addCol(row, "P3", "");
            pdh.addCol(row, "P4", "");

            if (baselineRegimen != null && baselineRegimen.size() > 0) {
                pdh.addCol(row, "BASE REGIMEN", baselineRegimen.get(0).getReportName());
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
                Map<String, List<SummarizedObs>> patientData = summarizedObs.get(workingMonth);

                if (period <= Integer.valueOf(currentMonth) && patientData != null) {

                    List<SummarizedObs> tb = patientData.get("dce02aa1-30ab-102d-86b0-7a5022ba4115");
                    List<SummarizedObs> adh = patientData.get("dce03b2f-30ab-102d-86b0-7a5022ba4115");
                    List<SummarizedObs> cpt = patientData.get("38801143-01ac-4328-b0e1-a7b23c84c8a3");
                    List<SummarizedObs> arvs = patientData.get("dd2b0b4d-30ab-102d-86b0-7a5022ba4115");
                    List<SummarizedObs> appointments = patientData.get("dcac04cf-30ab-102d-86b0-7a5022ba4115");
                    List<SummarizedObs> deaths = patientData.get("deaths");
                    List<SummarizedObs> clinicalStages = patientData.get("dcdff274-30ab-102d-86b0-7a5022ba4115");

                    String tbStatus = "";
                    String adherence = "";
                    String cotrim = "";
                    String regimen = "";

                    String status = "";
                    String adherenceAndCPT = "";

                    SummarizedObs currentTbStatus = patientInGroup(tb, patient, "obs_datetime", false);
                    SummarizedObs currentADH = patientInGroup(adh, patient, "obs_datetime", false);
                    SummarizedObs currentCPT = patientInGroup(cpt, patient, "obs_datetime", false);
                    SummarizedObs currentARV = patientInGroup(arvs, patient, "obs_datetime", false);
                    SummarizedObs currentAppointment = patientInGroup(appointments, patient, "value_datetime", false);
                    SummarizedObs currentlyDead = patientInGroup(deaths, patient, "deaths", false);


                    NormalizedObs currentStops = patientInGroup(patient, stop, "monthly", workingMonth);
                    NormalizedObs currentRestarts = patientInGroup(patient, restart, "monthly", workingMonth);
                    NormalizedObs currentTo = patientInGroup(patient, to, "monthly", workingMonth);

                    if (currentTbStatus != null) {
                        tbStatus = currentTbStatus.getReportName();
                    }

                    if (currentADH != null) {
                        adherence = currentADH.getReportName();
                    }
                    if (currentCPT != null) {
                        cotrim = "Y";
                    }

                    if (currentlyDead != null) {
                        status = "1";
                    }

                    if (currentStops != null) {
                        status = "2";
                    }

                    if (currentRestarts != null) {
                        status = "6";
                    }

                    if (currentTo != null) {
                        status = "5";
                    }

                    if (currentARV != null) {
                        regimen = currentARV.getReportName();
                    } else if (StringUtils.isNotBlank(status)) {
                        regimen = status;
                    } else if (currentAppointment != null) {
                        regimen = "3";
                    } else {
                        NormalizedObs currentDays1MonthsAgo = patientInGroup(patient, arvDays, "obs_datetime", String.valueOf(period - 1));
                        NormalizedObs currentDays2MonthsAgo = patientInGroup(patient, arvDays, "obs_datetime", String.valueOf(period - 2));

                        if (currentDays1MonthsAgo != null) {
                            if (currentDays1MonthsAgo.getValueNumeric() == 30.0) {
                                regimen = "3";
                            } else if (currentDays1MonthsAgo.getValueNumeric() == 60.0) {
                                regimen = "→";
                            }
                        } else {
                            if (currentDays2MonthsAgo != null) {
                                if (currentDays2MonthsAgo.getValueNumeric() >= 30.0 && currentDays2MonthsAgo.getValueNumeric() <= 60.0) {
                                    regimen = "3";
                                } else if (currentDays2MonthsAgo.getValueNumeric() == 90.0) {
                                    regimen = "→";
                                }
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
                    pdh.addCol(row, "FUS" + String.valueOf(i), regimen + "\n" + tbStatus + "\n" + adherenceAndCPT);

                    if (i == 6 || i == 12 || i == 24 || i == 36 || i == 48 || i == 60 || i == 72) {
                        NormalizedObs currentWeight = patientInGroup(patient, weights, "obs_datetime", workingMonth);
                        NormalizedObs currentCD4 = patientInGroup(patient, cd4s, "obs_datetime", workingMonth);
                        NormalizedObs currentViralLoadDate = patientInGroup(patient, vlDates, "value_datetime", workingMonth);

                        SummarizedObs currentClinicalStage = patientInGroup(patient, workingMonth, clinicalStages, "obs_datetime");

                        String cd4 = "";
                        String vl = viralLoad(vls, i);

                        if (currentClinicalStage != null) {
                            pdh.addCol(row, "CI" + String.valueOf(i), currentClinicalStage.getReportName());
                        } else {
                            pdh.addCol(row, "CI" + String.valueOf(i), "");
                        }

                        if (currentWeight != null) {
                            pdh.addCol(row, "W" + String.valueOf(i), currentWeight.getValueNumeric());
                        } else {
                            pdh.addCol(row, "W" + String.valueOf(i), "");
                        }

                        if (currentCD4 != null) {
                            cd4 = String.valueOf(currentCD4.getValueNumeric());
                        }

                        if (currentViralLoadDate != null) {
                            NormalizedObs vlValue = patientInGroup(patient, vls, currentViralLoadDate.getEncounter());
                            if (vlValue != null) {
                                vl = String.valueOf(vlValue.getValueNumeric());
                            }
                        }

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
        }
        assertNotNull(connection);
        assertEquals(month, "201507");
        assertEquals(currentMonth, "201708");
        assertEquals(patients.size(), 137);
        assertEquals(demographics.size(), 137);

    }

}