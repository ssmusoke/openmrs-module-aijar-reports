package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import com.google.common.base.Splitter;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.ugandaemrreports.common.*;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.ARTDatasetDefinition;
import org.openmrs.module.ugandaemrreports.library.UgandaEMRReporting;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.openmrs.module.ugandaemrreports.library.UgandaEMRReporting.patientInGroup;

/**
 * Created by carapai on 11/05/2016.
 */
@Handler(supports = {ARTDatasetDefinition.class})
public class ARTDatasetDefinitionEvaluator implements DataSetEvaluator {

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
        ARTDatasetDefinition definition = (ARTDatasetDefinition) dataSetDefinition;

        String month = UgandaEMRReporting.getObsPeriod(definition.getStartDate(), Enums.Period.MONTHLY);
        Integer currentMonth = Integer.valueOf(UgandaEMRReporting.getObsPeriod(new Date(), Enums.Period.MONTHLY));
        LocalDate localDate = StubDate.dateOf(definition.getStartDate());

        String sql = String.format("select * from obs_summary where period = '%s' and period_grouped_by = 'value_datetime' and concept = 'ab505422-26d9-41f1-a079-c3d222000440'", month);

        List<SummarizedObs> summarizedObs;
        try {
            summarizedObs = UgandaEMRReporting.getSummarizedObs(UgandaEMRReporting.sqlConnection(), sql);


            String patients = summarizedObs.get(0).getPatients();

            Map<Integer, List<PersonDemographics>> demographics = UgandaEMRReporting.getPatientDemographics(UgandaEMRReporting.sqlConnection(), patients);
            List<String> encounterSummaryConcepts = new ArrayList<>(UgandaEMRReporting.artRegisterConcepts().values());
            List<String> summaryConcepts = new ArrayList<>(UgandaEMRReporting.artRegisterSummaryConcepts().values());
            List<String> encounterConcepts = new ArrayList<>(UgandaEMRReporting.artRegisterEncounterConcepts().values());

            String where = UgandaEMRReporting.joinQuery("period >= " + month, UgandaEMRReporting.constructSQLInQuery("concept", encounterSummaryConcepts), Enums.UgandaEMRJoiner.AND);
            where = UgandaEMRReporting.joinQuery(where, "encounter_type = '8d5b2be0-c2cc-11de-8d13-0010c6dffd0f'", Enums.UgandaEMRJoiner.AND);
            String normalizedSql = "select * from obs_summary where " + where;

            List<SummarizedObs> encounterSummarizedObs = UgandaEMRReporting.getSummarizedObs(UgandaEMRReporting.sqlConnection(), normalizedSql);
            Map<Integer, Map<String, List<NormalizedObs>>> summaryObs = UgandaEMRReporting.getNormalizedObs(UgandaEMRReporting.sqlConnection(), summaryConcepts, patients, "8d5b27bc-c2cc-11de-8d13-0010c6dffd0f");
            Map<Integer, Map<String, List<NormalizedObs>>> encounterObs = UgandaEMRReporting.getNormalizedObs(UgandaEMRReporting.sqlConnection(), encounterConcepts, patients, "8d5b2be0-c2cc-11de-8d13-0010c6dffd0f");
            Map<String, Map<String, List<SummarizedObs>>> multimap = encounterSummarizedObs.stream().collect(Collectors.groupingBy(SummarizedObs::getPeriod, Collectors.groupingBy(SummarizedObs::getConcept)));

            PatientDataHelper pdh = new PatientDataHelper();

            for (String patient : Splitter.on(",").splitToList(patients)) {
                DataSetRow row = new DataSetRow();
                String ti = "";
                List<PersonDemographics> personDemographics = demographics.get(Integer.valueOf(patient));

                PersonDemographics personDemos = personDemographics != null && personDemographics.size() > 0 ? personDemographics.get(0) : new PersonDemographics();

                Map<String, List<NormalizedObs>> patientSummaryPage = summaryObs.get(Integer.valueOf(patient));
                Map<String, List<NormalizedObs>> patientOtherEncounters = encounterObs.get(Integer.valueOf(patient));

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

                List<NormalizedObs> weights = patientOtherEncounters != null ? patientOtherEncounters.get("dce09e2f-30ab-102d-86b0-7a5022ba4115") : null;
                List<NormalizedObs> cd4s = patientOtherEncounters != null ? patientOtherEncounters.get("dcbcba2c-30ab-102d-86b0-7a5022ba4115") : null;
                List<NormalizedObs> vls = patientOtherEncounters != null ? patientOtherEncounters.get("dc8d83e3-30ab-102d-86b0-7a5022ba4115") : null;
                List<NormalizedObs> vlDates = patientOtherEncounters != null ? patientOtherEncounters.get("0b434cfa-b11c-4d14-aaa2-9aed6ca2da88") : null;
                List<SummarizedObs> functionalStatus = multimap.get(month).get("dce09a15-30ab-102d-86b0-7a5022ba4115");
                // List<NormalizedObs> vlQualitative = patientOtherEncounters != null ? patientOtherEncounters.get("dca12261-30ab-102d-86b0-7a5022ba4115") : null;
                List<NormalizedObs> arvDays = patientOtherEncounters != null ? patientOtherEncounters.get("7593ede6-6574-4326-a8a6-3d742e843659") : null;

                if ((artTI != null && artTI.size() > 0) || (entry != null && entry.size() > 0 && entry.get(0).getValueCoded().equals("dcd7e8e5-30ab-102d-86b0-7a5022ba4115"))) {
                    ti = "✔";
                }

                pdh.addCol(row, "Date ART Started", artStartDate.get(0).getValueDatetime());
                pdh.addCol(row, "Unique ID no", patient);
                pdh.addCol(row, "TI", ti);
                pdh.addCol(row, "Patient Clinic ID", UgandaEMRReporting.processString(personDemos.getIdentifiers()).get("e1731641-30ab-102d-86b0-7a5022ba4115"));
                pdh.addCol(row, "Name", personDemos.getNames());
                pdh.addCol(row, "Gender", personDemos.getGender());
                pdh.addCol(row, "Age", artStartDate.get(0).getAgeAtValueDatetime());
                // TODO display address correctly
                pdh.addCol(row, "Address", personDemos.getAddresses());

                SummarizedObs currentFunctionalStatus = patientInGroup(functionalStatus, patient, "obs_datetime");

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
                pdh.addCol(row, "Patient Clinic ID", UgandaEMRReporting.processString(personDemos.getIdentifiers()).get("e1731641-30ab-102d-86b0-7a5022ba4115"));

                for (int i = 0; i <= 72; i++) {
                    String workingMonth = UgandaEMRReporting.getObsPeriod(Periods.addMonths(localDate, i).get(0).toDate(), Enums.Period.MONTHLY);
                    Integer period = Integer.valueOf(workingMonth);
                    Map<String, List<SummarizedObs>> patientData = multimap.get(workingMonth);

                    if (period <= currentMonth && patientData != null) {

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

                        SummarizedObs currentTbStatus = patientInGroup(tb, patient, "obs_datetime");
                        SummarizedObs currentADH = patientInGroup(adh, patient, "obs_datetime");
                        SummarizedObs currentCPT = patientInGroup(cpt, patient, "obs_datetime");
                        SummarizedObs currentARV = patientInGroup(arvs, patient, "obs_datetime");
                        SummarizedObs currentAppointment = patientInGroup(appointments, patient, "value_datetime");
                        SummarizedObs currentlyDead = patientInGroup(deaths, patient, "deaths");


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
                            NormalizedObs currentViralLoadDate = patientInGroup(patient, vlDates, "monthly", workingMonth);

                            SummarizedObs currentClinicalStage = patientInGroup(patient, workingMonth, clinicalStages, "obs_datetime");

                            String cd4 = "";
                            String vl = "";

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
                dataSet.addRow(row);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return dataSet;
    }
}
