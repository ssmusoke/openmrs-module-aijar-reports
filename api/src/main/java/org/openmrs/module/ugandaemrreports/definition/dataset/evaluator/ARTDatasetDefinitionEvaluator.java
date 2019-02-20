package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Multimap;
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
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.openmrs.module.ugandaemrreports.reports.Helper.*;

/**
 */
@Handler(supports = {ARTDatasetDefinition.class})
public class ARTDatasetDefinitionEvaluator implements DataSetEvaluator {
    @Autowired
    private HIVMetadata hivMetadata;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
        ARTDatasetDefinition definition = (ARTDatasetDefinition) dataSetDefinition;

        String month = getObsPeriod(definition.getStartDate(), Enums.Period.MONTHLY);
        Integer currentMonth = Integer.valueOf(getObsPeriod(new Date(), Enums.Period.MONTHLY));
        LocalDate localDate = StubDate.dateOf(definition.getStartDate());
        String startDate = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");
//        LocalDate endDate = StubDate.dateOf(definition.getEndDate());



//        String startArtThisMonth = ("select person_id,DATE(value_datetime) as obs_date from obs where " +
//                "DATE_FORMAT(value_datetime, '%Y%m') = '%s' and concept_id = 99161 and voided = 0")
//                .replaceAll("%s", month);
        String startArtThisMonth =String.format("select person_id,DATE(value_datetime) as obs_date from obs where \n" +
                        "value_datetime between '%s' and '%s'  and concept_id = 99161 and voided = 0;",startDate,endDate);

        try {
            Connection connection = sqlConnection();

            Multimap<Integer, Date> dates = getData(connection, startArtThisMonth, "person_id", "obs_date");

            String allPatients = Joiner.on(",").join(dates.keySet());

            List<Map.Entry<Integer, Date>> entries = new ArrayList<>(convert(dates).entrySet());
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
                    "  COALESCE(value_coded, COALESCE(DATE(value_datetime), COALESCE(value_numeric, value_text))) AS val,\n" +
                    "  CASE\n" +
                    "  WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "        FROM person AS p\n" +
                    "        WHERE p.person_id = o.person_id) > 10 AND value_coded = 99015\n" +
                    "    THEN '1a'\n" +
                    "  WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "        FROM person AS p\n" +
                    "        WHERE p.person_id = o.person_id) <= 10 AND value_coded = 99015\n" +
                    "    THEN '4a'\n" +
                    "  WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "        FROM person AS p\n" +
                    "        WHERE p.person_id = o.person_id) > 10 AND value_coded = 99016\n" +
                    "    THEN '1b'\n" +
                    "  WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "        FROM person AS p\n" +
                    "        WHERE p.person_id = o.person_id) <= 10 AND value_coded = 99016\n" +
                    "    THEN '4b'\n" +
                    "  WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "        FROM person AS p\n" +
                    "        WHERE p.person_id = o.person_id) > 10 AND value_coded = 99005\n" +
                    "    THEN '1c'\n" +
                    "  WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "        FROM person AS p\n" +
                    "        WHERE p.person_id = o.person_id) <= 10 AND value_coded = 99005\n" +
                    "    THEN '4c'\n" +
                    "  WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "        FROM person AS p\n" +
                    "        WHERE p.person_id = o.person_id) > 10 AND value_coded = 99006\n" +
                    "    THEN '1d'\n" +
                    "  WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "        FROM person AS p\n" +
                    "        WHERE p.person_id = o.person_id) <= 10 AND value_coded = 99006\n" +
                    "    THEN '4d'\n" +
                    "  WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "        FROM person AS p\n" +
                    "        WHERE p.person_id = o.person_id) > 10 AND value_coded = 99039\n" +
                    "    THEN '1e'\n" +
                    "\n" +
                    "  WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "        FROM person AS p\n" +
                    "        WHERE p.person_id = o.person_id) <= 10 AND value_coded = 99039\n" +
                    "    THEN '4j'\n" +
                    "  WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "        FROM person AS p\n" +
                    "        WHERE p.person_id = o.person_id) > 10 AND value_coded = 99040\n" +
                    "    THEN '1f'\n" +
                    "\n" +
                    "  WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "        FROM person AS p\n" +
                    "        WHERE p.person_id = o.person_id) <= 10 AND value_coded = 99040\n" +
                    "    THEN '4i'\n" +
                    "  WHEN value_coded = 99041\n" +
                    "    THEN '1g'\n" +
                    "  WHEN value_coded = 99042\n" +
                    "    THEN '1h'\n" +
                    "  WHEN value_coded = 99007\n" +
                    "    THEN '2a2'\n" +
                    "  WHEN value_coded = 99008\n" +
                    "    THEN '2a4'\n" +
                    "  WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "        FROM person AS p\n" +
                    "        WHERE p.person_id = o.person_id) > 10 AND value_coded = 99044\n" +
                    "    THEN '2b'\n" +
                    "  WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "        FROM person AS p\n" +
                    "        WHERE p.person_id = o.person_id) <= 10 AND value_coded = 99044\n" +
                    "    THEN '5d'\n" +
                    "  WHEN value_coded = 99043\n" +
                    "    THEN '2c'\n" +
                    "  WHEN value_coded = 99282\n" +
                    "    THEN '2d2'\n" +
                    "  WHEN value_coded = 99283\n" +
                    "    THEN '2d4'\n" +
                    "  WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "        FROM person AS p\n" +
                    "        WHERE p.person_id = o.person_id) > 10 AND value_coded = 99046\n" +
                    "    THEN '2e'\n" +
                    "  WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "        FROM person AS p\n" +
                    "        WHERE p.person_id = o.person_id) <= 10 AND value_coded = 99046\n" +
                    "    THEN '5l'\n" +
                    "  WHEN value_coded = 99017\n" +
                    "    THEN '5a'\n" +
                    "  WHEN value_coded = 99018\n" +
                    "    THEN '5b'\n" +
                    "  WHEN value_coded = 99045\n" +
                    "    THEN '5f'\n" +
                    "  WHEN value_coded = 99284\n" +
                    "    THEN '5g'\n" +
                    "  WHEN value_coded = 99285\n" +
                    "    THEN '5h'\n" +
                    "  WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "        FROM person AS p\n" +
                    "        WHERE p.person_id = o.person_id) > 10 AND value_coded = 99286\n" +
                    "    THEN '2c'\n" +
                    "  WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "        FROM person AS p\n" +
                    "        WHERE p.person_id = o.person_id) <= 10 AND value_coded = 99286\n" +
                    "    THEN '5l'\n" +
                    "  WHEN value_coded = 99884\n" +
                    "    THEN '4e'\n" +
                    "  WHEN value_coded = 99885\n" +
                    "    THEN '4f'\n" +
                    "  WHEN value_coded = 99888\n" +
                    "    THEN '2h'\n" +
                    "  WHEN value_coded = 163017\n" +
                    "    THEN '2g'\n" +
                    "  WHEN value_coded = 90002\n" +
                    "    THEN 'othr'\n" +
                    "  WHEN value_coded IN (90033, 90079, 1204)\n" +
                    "    THEN '1'\n" +
                    "  WHEN value_coded IN (90034, 90073, 1205)\n" +
                    "    THEN '2'\n" +
                    "  WHEN value_coded IN (90035, 90078, 1206)\n" +
                    "    THEN '3'\n" +
                    "  WHEN value_coded IN (90036, 90071, 1207)\n" +
                    "    THEN '4'\n" +
                    "  WHEN value_coded = 90293\n" +
                    "    THEN 'T1'\n" +
                    "  WHEN value_coded = 90294\n" +
                    "    THEN 'T2'\n" +
                    "  WHEN value_coded = 90295\n" +
                    "    THEN 'T3'\n" +
                    "  WHEN value_coded = 90295\n" +
                    "    THEN 'T4'\n" +
                    "  WHEN value_coded = 90156\n" +
                    "    THEN 'G'\n" +
                    "  WHEN value_coded = 90157\n" +
                    "    THEN 'F'\n" +
                    "  WHEN value_coded = 90158\n" +
                    "    THEN 'P'\n" +
                    "  WHEN value_coded = 90003\n" +
                    "    THEN 'Y'\n" +
                    "  ELSE ''\n" +
                    "  END                                                                                        AS report_name\n" +
                    "FROM obs o\n" +
                    "WHERE o.voided = 0 AND o.encounter_id IN (%s) AND o.concept_id IN (%s)\n" +
                    "UNION ALL\n" +
                    "SELECT\n" +
                    "  p.person_id,\n" +
                    "  0,\n" +
                    "  0,\n" +
                    "  death_date,\n" +
                    "  DATE(death_date),\n" +
                    "  ''\n" +
                    "FROM person p INNER JOIN obs art ON (p.person_id = art.person_id)\n" +
                    "WHERE art.concept_id = 99161 AND p.person_id IN (%s) AND art.voided = 0 AND p.voided = 0 AND p.death_date >= art.value_datetime;", encounters, concepts, allPatients);

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
                ObsData baselineWeight = getData(patientData, "99069");
                ObsData baselineCs = getData(patientData, "99070");
                ObsData baselineCd4 = getData(patientData, "99071");
                ObsData baselineRegimen = getData(patientData, "99061");


                boolean hasDied = false;
                boolean hasTransferred = false;

                String startedTB = tbStartDate != null ? DateUtil.formatDate(DateUtil.parseYmd(tbStartDate.getVal()), "MM/yyyy") : "";
                String stoppedTB = tbStopDate != null ? DateUtil.formatDate(DateUtil.parseYmd(tbStopDate.getVal()), "MM/yyyy") : "";


                PersonDemographics personDemos = personDemographics != null && personDemographics.size() > 0 ? personDemographics.get(0) : new PersonDemographics();

                List<String> addresses = processString2(personDemos.getAddresses());

                pdh.addCol(row, "Date ART Started", patient.getValue());
                pdh.addCol(row, "Unique ID no", "");
                pdh.addCol(row, "TI", ti == null ? "" : "TI");
                pdh.addCol(row, "Patient Clinic ID", processString(personDemos.getIdentifiers()).get("e1731641-30ab-102d-86b0-7a5022ba4115"));

                List<String> names = Splitter.on(" ").splitToList(personDemos.getNames());

                pdh.addCol(row, "Surname", names.size() > 0 ? names.get(0) : "");
                pdh.addCol(row, "GivenName", names.size() > 1 ? names.get(1) : "");
                pdh.addCol(row, "Gender", personDemos.getGender());
                if (personDemos.getBirthDate() != null && artStartDate != null) {
                    Years age = Years.yearsBetween(StubDate.dateOf(personDemos.getBirthDate()), StubDate.dateOf(patient.getValue()));
                    pdh.addCol(row, "Age", age.getYears());
                } else {
                    pdh.addCol(row, "Age", "");
                }

                if (addresses.size() == 6) {
                    pdh.addCol(row, "District", addresses.get(1));
                    pdh.addCol(row, "Subcounty/Parish", addresses.get(3) + " " + addresses.get(4));
                    pdh.addCol(row, "Village/Cell", addresses.get(5));

                } else {
                    pdh.addCol(row, "District", "");
                    pdh.addCol(row, "Subcounty/Parish", "");
                    pdh.addCol(row, "Village/Cell", "");
                }
                pdh.addCol(row, "Weight", baselineWeight == null ? "" : baselineWeight.getVal());

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
                    pdh.addCol(row, "FUS", functionalStatusDuringArtStart.getReportName());
                } else {
                    pdh.addCol(row, "FUS", "");
                }


                if (baselineCs != null) {
                    pdh.addCol(row, "CS", baselineCs.getReportName());
                } else {
                    pdh.addCol(row, "CS", "");
                }

                pdh.addCol(row, "CD4", baselineCd4 == null ? "" : baselineCd4.getVal());
                pdh.addCol(row, "VL", fvl);

                pdh.addCol(row, "CPT Start Date", firstCPT == null ? "" : DateUtil.formatDate(firstCPT.getEncounterDate(), "MM/yyyy"));
                pdh.addCol(row, "CPT Stop Date", "");
                pdh.addCol(row, "INH Start Date", firstINH == null ? "" : DateUtil.formatDate(firstINH.getEncounterDate(), "MM/yyyy"));
                pdh.addCol(row, "INH Stop Date", "");
                pdh.addCol(row, "TB Reg No", "");
                pdh.addCol(row, "TB Start Date", startedTB);
                pdh.addCol(row, "TB Stop Date", stoppedTB);

                pdh.addCol(row, "EDD1", "");
                pdh.addCol(row, "ANC1", "");
                pdh.addCol(row, "INFANT1", "");

                pdh.addCol(row, "EDD2", "");
                pdh.addCol(row, "ANC2", "");
                pdh.addCol(row, "INFANT2", "");

                pdh.addCol(row, "EDD3", "");
                pdh.addCol(row, "ANC3", "");
                pdh.addCol(row, "INFANT3", "");

                if (baselineRegimen != null) {
                    pdh.addCol(row, "BASE REGIMEN", baselineRegimen.getReportName());
                } else {
                    pdh.addCol(row, "BASE REGIMEN", "");
                }

                pdh.addCol(row, "L1S1", "");
                pdh.addCol(row, "L1S2", "");
                pdh.addCol(row, "L2S1", "");
                pdh.addCol(row, "L2S2", "");
                pdh.addCol(row, "L3S1", "");
                pdh.addCol(row, "L3S2", "");
                pdh.addCol(row, "Patient Clinic ID", processString(personDemos.getIdentifiers()).get("e1731641-30ab-102d-86b0-7a5022ba4115"));

                ObsData visit = null;

                for (int i = 0; i <= 72; i++) {
                    String workingMonth = getObsPeriod(Periods.addMonths(localDate, i).get(0).toDate(), Enums.Period.MONTHLY);
                    Integer period = Integer.valueOf(workingMonth);

                    ObsData currentEncounter = getData(patientData, period);

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

                        if (returnDate != null) {
                            visit = returnDate;
                        }

                        String cotrim = "";
                        String status = "";
                        String adherence = "";
                        String tb = "";

                        if (inhDosage != null || cptDosage != null) {
                            cotrim = "Y";
                        }
                        if (currentRegimen != null) {
                            status = currentRegimen.getReportName();
                        } else if (returnDate != null) {
                            status = "3";
                        } else if (currentEncounter != null) {
                            status = "=UNICHAR(8730)";
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
                                if (visit != null) {
                                    Integer appointmentPeriod = Integer.parseInt(getObsPeriod(DateUtil.parseYmd(visit.getVal()), Enums.Period.MONTHLY));
                                    Integer diff = period - appointmentPeriod;
                                    if (diff <= 0) {
                                        status = "=UNICHAR(8594)";
                                    } else if (diff < 3) {
                                        status = "3";
                                    } else {
                                        status = "4";
                                    }
                                }
                            }
                        }

                        if (tbStatus != null) {
                            tb = tbStatus.getReportName();
                        }
                        if (arvAdh != null) {
                            adherence = arvAdh.getReportName();
                        }
                        pdh.addCol(row, "FUS" + String.valueOf(i), status);
                        pdh.addCol(row, "TB" + String.valueOf(i), tb);
                        pdh.addCol(row, "A" + String.valueOf(i), adherence);
                        pdh.addCol(row, "C" + String.valueOf(i), cotrim);

                        if (i == 6 || i == 12 || i == 24 || i == 36 || i == 48 || i == 60 || i == 72) {
                            ObsData weight = getData(patientData, workingMonth, "90236");
                            ObsData cd4 = getData(patientData, workingMonth, "5497");
                            ObsData clinicalStage = getData(patientData, workingMonth, "90203");
                            ObsData viralLoad = viralLoad(viralLoads, i);

                            pdh.addCol(row, "CI" + String.valueOf(i), clinicalStage == null ? "" : clinicalStage.getReportName());

                            pdh.addCol(row, "W" + String.valueOf(i), weight == null ? "" : weight.getVal());

                            pdh.addCol(row, "CD4" + String.valueOf(i), (cd4 == null ? "" : cd4.getVal()));
                            pdh.addCol(row, "VL" + String.valueOf(i), (viralLoad == null ? "" : viralLoad.getVal()));
                        }
                    } else {
                        pdh.addCol(row, "FUS" + String.valueOf(i), "");
                        pdh.addCol(row, "TB" + String.valueOf(i), "");
                        pdh.addCol(row, "A" + String.valueOf(i), "");
                        pdh.addCol(row, "C" + String.valueOf(i), "");
                        if (i == 6 || i == 12 || i == 24 || i == 36 || i == 48 || i == 60 || i == 72) {
                            pdh.addCol(row, "CI" + String.valueOf(i), "");
                            pdh.addCol(row, "W" + String.valueOf(i), "");
                            pdh.addCol(row, "CD4" + String.valueOf(i), "");
                            pdh.addCol(row, "VL" + String.valueOf(i), "");
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
