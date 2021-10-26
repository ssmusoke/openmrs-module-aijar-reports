package org.openmrs.module.ugandaemrreports.reports;

import com.google.common.base.Splitter;
import com.google.common.collect.*;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.ugandaemrreports.common.*;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.sort;
import static java.util.Comparator.comparing;

public class Helper {

    private static List<PersonDemographics> getPersonDemographics(Connection connection, String sql)
            throws SQLException {

        Statement stmt = connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                java.sql.ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(Integer.MIN_VALUE);

        ResultSet rs = stmt.executeQuery(sql);
        List<PersonDemographics> personDemographics = new ArrayList<>();
        while (rs.next()) {
            PersonDemographics demographic = new PersonDemographics();
            demographic.setAddresses(rs.getString("addresses"));
            demographic.setAttributes(rs.getString("attributes"));
            demographic.setBirthDate(rs.getString("birthdate"));
            demographic.setGender(rs.getString("gender"));
            demographic.setIdentifiers(rs.getString("identifiers"));
            demographic.setNames(rs.getString("names"));
            demographic.setPersonId(rs.getInt("person_id"));
            personDemographics.add(demographic);
        }
        rs.close();
        stmt.close();

        return personDemographics;
    }


    public static List<PatientEncounterObs> getEncounterObs(Connection connection, String encounterType, String obs, Date startDate, Date endDate) throws SQLException {

        String sql = "SELECT\n" +
                "  e.patient_id,\n" +
                "  DATE(encounter_datetime)                                 AS encounter_date,\n" +
                "  (SELECT GROUP_CONCAT(CONCAT_WS(' ', COALESCE(given_name, ''), COALESCE(family_name, '')))\n" +
                "   FROM person_name pn\n" +
                "   WHERE e.patient_id = pn.person_id)                      AS 'names',\n" +
                "  (SELECT p.gender\n" +
                "   FROM person p\n" +
                "   WHERE p.person_id = e.patient_id)                       AS gender,\n" +
                "  (SELECT birthdate\n" +
                "   FROM person p\n" +
                "   WHERE p.person_id = e.patient_id)                       AS dob,\n" +
                "  (SELECT YEAR(e.encounter_datetime) - YEAR(birthdate) - (RIGHT(e.encounter_datetime, 5) < RIGHT(birthdate, 5))\n" +
                "   FROM person p\n" +
                "   WHERE p.person_id = e.patient_id)                       AS age,\n" +
                "  (SELECT group_concat(concat_ws(':', o.concept_id, cn.name, DATE(o.obs_datetime)))\n" +
                "   FROM obs o LEFT JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en' \n" +
                "   WHERE o.concept_id = 90244 AND o.person_id = e.patient_id) AS marital,\n" +
                "  (SELECT GROUP_CONCAT(CONCAT_WS(':', COALESCE(pit.uuid, ''), COALESCE(identifier, '')))\n" +
                "   FROM patient_identifier pi INNER JOIN patient_identifier_type pit\n" +
                "       ON (pi.identifier_type = pit.patient_identifier_type_id)\n" +
                "   WHERE pi.patient_id = e.patient_id)                     AS 'identifiers',\n" +
                "  (SELECT GROUP_CONCAT(CONCAT_WS(':', COALESCE(pat.uuid, ''), COALESCE(value, '')))\n" +
                "   FROM person_attribute pa INNER JOIN person_attribute_type pat\n" +
                "       ON (pa.person_attribute_type_id = pat.person_attribute_type_id)\n" +
                "   WHERE e.patient_id = pa.person_id)                      AS 'attributes',\n" +
                "  (SELECT GROUP_CONCAT(\n" +
                "      CONCAT_WS(':', COALESCE(country, ''), COALESCE(county_district, ''), COALESCE(state_province, ''),\n" +
                "                COALESCE(address3, ''),\n" +
                "                COALESCE(address4, ''), COALESCE(address5, '')))\n" +
                "   FROM person_address pas\n" +
                "   WHERE e.patient_id = pas.person_id)                     AS 'addresses',\n" +
                "  (SELECT group_concat(\n" +
                "      concat_ws(':', o.concept_id, concat_ws('', DATE(o.value_datetime), o.value_text, cn.name, o.value_numeric),\n" +
                "                DATE(o.obs_datetime),COALESCE(o.obs_group_id, '')))\n" +
                "   FROM obs o LEFT JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.concept_name_type='FULLY_SPECIFIED' and cn.locale='en'\n";
        if (obs != null) {
            sql += String.format("   WHERE o.encounter_id = e.encounter_id AND o.voided = 0 AND concept_id IN(%s)) AS obs\n", obs);
        } else {
            sql += "   WHERE o.encounter_id = e.encounter_id AND o.voided = 0) AS obs\n";
        }

        sql += "FROM encounter e\n" +
                "WHERE e.encounter_type = (SELECT encounter_type_id\n" +
                "                          FROM encounter_type\n" +
                "                          WHERE uuid =\n" +
                String.format("                                '%s') AND voided = 0", encounterType);

        if (startDate != null) {
            sql += " AND e.encounter_datetime >= '" + DateUtil.formatDate(startDate, "yyyy-MM-dd") + "'";
        }

        if (endDate != null) {
            sql += " AND e.encounter_datetime <= '" + DateUtil.formatDate(endDate, "yyyy-MM-dd") + "'";
        }

        Statement stmt = connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                java.sql.ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(Integer.MIN_VALUE);

        ResultSet rs = stmt.executeQuery(sql);
        List<PatientEncounterObs> patientEncounterObs = new ArrayList<>();
        while (rs.next()) {
            PatientEncounterObs encounterObs = new PatientEncounterObs();
            encounterObs.setPatientId(rs.getInt(1));
            encounterObs.setEncounterDate(rs.getDate(2));
            encounterObs.setNames(rs.getString(3));
            encounterObs.setGender(rs.getString(4));
            encounterObs.setDob(rs.getDate(5));
            encounterObs.setAge(rs.getInt(6));
            encounterObs.setMaritalStatus(rs.getString(7));
            encounterObs.setIdentifiers(rs.getString(8));
            encounterObs.setAttributes(rs.getString(9));
            encounterObs.setAddresses(rs.getString(10));

            List<String> currentObs = Splitter.on(",").splitToList(rs.getString(11));

            List<Observation> foundObs = new ArrayList<>();
            if (currentObs.size() > 0) {
                for (String o : currentObs) {
                    List<String> data = Splitter.on(":").splitToList(o);
                    Integer concept = Integer.valueOf(data.get(0));
                    String value = data.get(1);
                    Date obsDatetime = DateUtil.parseYmd(data.get(2));
                    Integer obsGroup = null;

                    if (StringUtils.isNotBlank(data.get(3))) {
                        obsGroup = Integer.valueOf(data.get(3));
                    }
                    foundObs.add(new Observation(concept, obsDatetime, value, obsGroup));
                }
            }
            encounterObs.setObs(foundObs);
            patientEncounterObs.add(encounterObs);
        }
        rs.close();
        stmt.close();
        return patientEncounterObs;
    }

    private static String constructSQLInQuery(String column, String values) {
        return column + " IN(" + values + ")";
    }

    private static String joinQuery(String query1, String query2, Enums.UgandaEMRJoiner joiner) {
        return query1 + " " + joiner.toString() + " " + query2;
    }


    public static Multimap<Integer, Date> getData(Connection connection, String sql, String columnLabel1, String columnLabel2) throws SQLException {

        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        Multimap<Integer, Date> result = TreeMultimap.create();
        while (rs.next()) {
            result.put(rs.getInt(columnLabel1), rs.getDate(columnLabel2));
        }
        rs.close();
        stmt.close();

        return result;
    }

    public static Map<Integer, Date> convert(Multimap<Integer, Date> m) {
        Map<Integer, Date> map = new HashMap<>();
        if (m == null) {
            return map;
        }

        for (Map.Entry<Integer, Collection<Date>> entry : m.asMap().entrySet()) {
            map.put(entry.getKey(), new ArrayList<>(entry.getValue()).get(0));
        }
        return map;
    }

    public static String convert(String concept) {
        Map<String, String> result = new HashMap<>();
        // Entry point
        result.put("90012", "eMTCT");
        result.put("90016", "TB");
        result.put("99593", "YCC");
        result.put("90019", "Outreach");
        result.put("90013", "Out Patient");
        result.put("90015", "STI");
        result.put("90018", "Inpatient");
        result.put("90002", "Other");
        // TB status
        result.put("90079", "1");
        result.put("90073", "2");
        result.put("90078", "3");
        result.put("90071", "4");
        // Infant feeding status
        result.put("5526", "EBF");
        result.put("99089", "RF");
        result.put("6046", "MF");
        result.put("99791", "CF");
        result.put("99792", "W");
        result.put("99793", "NLB");
        // Infant ARVs for eMTCT @TODO Requires revision
        result.put("163013", "2");
        result.put("162966", "2");
        result.put("99789", "2");
        result.put("99790", "3");
        result.put("99788", "1");
        result.put("1067", "5");
        result.put("163009", "5");
        result.put("163010", "5");

        return result.get(concept);
    }


    public static String getOneData(Connection connection, String sql) throws SQLException {

        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        String result = rs.getString(1);
        rs.close();
        stmt.close();
        return result;
    }

    public static ObsData getData(List<ObsData> data, String concept) {

        return data.stream()
                .filter(line -> line.getConceptId().compareTo(concept) == 0).findAny().orElse(null);
    }

    public static ObsData getData(List<ObsData> data, String yearMonth, String concept) {

        return data.stream()
                .filter(line -> line.getConceptId().compareTo(concept) == 0 && getObsPeriod(line.getEncounterDate(),
                        Enums.Period.MONTHLY).compareTo(yearMonth) == 0).findAny().orElse(null);
    }

    public static ObsData getData(List<ObsData> data, Integer yearMonth) {

        return data.stream()
                .filter(line -> getObsPeriod(line.getEncounterDate(),
                        Enums.Period.MONTHLY).compareTo(String.valueOf(yearMonth)) == 0).findAny().orElse(null);
    }

    public static List<ObsData> getData(Connection connection, String sql) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        List<ObsData> result = new ArrayList<>();
        while (rs.next()) {
            Integer patientId = rs.getInt(1);
            String conceptId = rs.getString(2);
            Integer encounterId = rs.getInt(3);
            Date encounterDate = rs.getDate(4);
            String val = rs.getString(5);
            String reportName = rs.getString(6);
            result.add(new ObsData(patientId, conceptId, encounterId, encounterDate, val, reportName));
        }
        rs.close();
        stmt.close();

        return result;
    }

    public static List<String> getData(Map<String, String> data, String yearQuarter, String concept) {
        String quarter = yearQuarter.substring(Math.max(yearQuarter.length() - 2, 0));
        String year = yearQuarter.substring(0, Math.min(yearQuarter.length(), 4));

        List<String> result = new ArrayList<>();

        ImmutableMap<String, List<String>> quarters =
                new ImmutableMap.Builder<String, List<String>>()
                        .put("Q1", Arrays.asList("01", "02", "03"))
                        .put("Q2", Arrays.asList("04", "05", "06"))
                        .put("Q3", Arrays.asList("07", "08", "09"))
                        .put("Q4", Arrays.asList("10", "11", "12"))
                        .build();

        List<String> periods = quarters.get(quarter);
        String r1 = data.get(year + periods.get(0) + concept);
        String r2 = data.get(year + periods.get(1) + concept);
        String r3 = data.get(year + periods.get(2) + concept);

        if (r1 != null) {
            result.add(r1);
        }
        if (r2 != null) {
            result.add(r2);
        }
        if (r3 != null) {
            result.add(r3);
        }
        return result;
    }

    public static String getData(Map<String, String> data, String concept) {

        Set<String> keys = data.keySet();

        String criteria = keys.stream()
                .filter(e -> e.endsWith(concept))
                .findAny().orElse(null);
        if (criteria != null) {
            return data.get(criteria);
        }

        return null;
    }


    public static Table<String, Integer, String> getDataTable(Connection connection, String sql) throws SQLException {

        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        Table<String, Integer, String> table = TreeBasedTable.create();
        while (rs.next()) {
            Integer patientId = rs.getInt(1);
            String conceptId = rs.getString(2);
            Date encounterDate = rs.getDate(3);
            String val = rs.getString(4);
            String encounterMonth = DateUtil.formatDate(encounterDate, "yyyyMM") + conceptId;
            table.put(encounterMonth, patientId, val);
        }
        rs.close();
        stmt.close();

        return table;
    }

    public static Map<String, Date> getClinicalStages(Map<String, String> data, String concept) {
        Map<String, Date> result = new HashMap<>();

        ImmutableMap<String, String> quarters =
                new ImmutableMap.Builder<String, String>()
                        .put("90033", "1")
                        .put("90034", "2")
                        .put("90035", "3")
                        .put("90036", "4")
                        .put("90293", "T1")
                        .put("90294", "T2")
                        .put("90295", "T3")
                        .put("90296", "T4")
                        .build();

        for (Map.Entry<String, String> dic : data.entrySet()) {
            String value = quarters.get(dic.getValue());
            String key = dic.getKey();
            if (key.endsWith(concept) && !result.containsValue(value)) {
                result.put(value, DateUtil.parseDate(key.substring(0, Math.min(key.length(), 6)), "yyyyMM"));
            }
        }
        return result;
    }

    public static String getMinimum(Map<String, String> data, String concept) {
        List<String> result = new ArrayList<>();

        for (Map.Entry<String, String> dic : data.entrySet()) {
            String key = dic.getKey();
            if (key.endsWith(concept)) {
                result.add(key.substring(0, Math.min(key.length(), 6)));
            }
        }
        sort(result);

        if (result.size() > 0) {

            String month = result.get(0).substring(Math.max(result.get(0).length() - 2, 0));
            String year = result.get(0).substring(0, Math.min(result.get(0).length(), 4));
            return month + "/" + year;
        }
        return "";
    }

    public static ObsData getFirstData(List<ObsData> data, String concept) {
        List<ObsData> filteredData = getDataAsList(data, concept);
        filteredData.sort(comparing(ObsData::getEncounterId));
        if (filteredData.size() > 0) {
            return filteredData.get(0);
        }
        return null;
    }

    public static List<ObsData> getDataAsList(List<ObsData> data, String concept) {
        return data.stream()
                .filter(line -> line.getConceptId().compareTo(concept) == 0)
                .collect(Collectors.toList());
    }

    public static String getObsPeriod(Date period, Enums.Period periodType) {
        LocalDate localDate = StubDate.dateOf(period);

        if (periodType == Enums.Period.YEARLY) {
            return localDate.toString("yyyy");
        } else if (periodType == Enums.Period.MONTHLY) {
            return localDate.toString("yyyyMM");
        } else if (periodType == Enums.Period.QUARTERLY) {
            return String.valueOf(localDate.getYear()) + "Q" + String.valueOf(((localDate.getMonthOfYear() - 1) / 3) + 1);
        } else if (periodType == Enums.Period.WEEKLY) {
            return localDate.getWeekyear() + "W" + localDate.weekOfWeekyear().get();
        }

        return null;
    }

    public static Map<String, String> artRegisterConcepts() {
        Map<String, String> concepts = new HashMap<>();
        concepts.put("functional status", "90235");
        concepts.put("inh dosage", "99604");
        concepts.put("tb start date", "90217");
        concepts.put("tb stop date", "90310");
        concepts.put("art start date", "99161");
        concepts.put("tb status", "90216");
        concepts.put("arv adh", "90221");
        concepts.put("cpt dosage", "99037");
        concepts.put("current regimen", "90315");
        concepts.put("return date", "5096");
        concepts.put("clinical stage", "90203");
        concepts.put("baseline weight", "99069");
        concepts.put("baseline cs", "99070");
        concepts.put("baseline cd4", "99071");
        concepts.put("baseline regimen", "99061");
        concepts.put("arv stop date", "99084");
        concepts.put("arv restart date", "99085");
        concepts.put("to date", "99165");
        concepts.put("ti date", "99160");
        concepts.put("entry", "90200");
        concepts.put("deaths", "0");
        concepts.put("weight", "90236");
        concepts.put("cd4", "5497");
        concepts.put("vl", "856");
        concepts.put("vl date", "163023");
        concepts.put("vl qualitative", "1305");
        concepts.put("arvdays", "99036");

        return concepts;
    }

    public static Map<String, String> preArtConcepts() {
        return new ImmutableMap.Builder<String, String>()
                .put("99161", "Art Start Date")
                .put("99037", "CPT Dosage")
                .put("99604", "INH Dosage")
                .put("99083", "Eligible for Art Clinical Stage")
                .put("99602", "Eligible for Art Pregnant")
                .put("99082", "Eligible for Art CD4")
                .put("99601", "Breast Feeding")
                .put("99600", "Tb for ART")
                .put("68", "Malnutrition")
                .put("5096", "Return visit date")
                .put("90200", "Entry point")
                .put("99115", "Other Entry point")
                .put("90203", "Who clinical stage")
                .put("90216", "TB Status")
                .put("90217", "TB Start Date")
                .put("90310", "TB stop date")
                .put("90297", "eligible date to start art")
                .put("90299", "eligible and ready date to start art")
                .put("99110", "TI")
                .put("99165", "To Date")
                .build();
    }

    public static Connection getDatabaseConnection(Properties props) throws ClassNotFoundException, SQLException {

        String driverClassName = props.getProperty("driver.class");
        String driverURL = props.getProperty("driver.url");
        String username = props.getProperty("user");
        String password = props.getProperty("password");
        Class.forName(driverClassName);
        return DriverManager.getConnection(driverURL, username, password);
    }

    public static ObsData viralLoad(List<ObsData> vls, Integer no) {

        if (vls != null && vls.size() > 0) {
            Map<Integer, List<ObsData>> vlsGroupedByEncounterId = vls.stream().collect(Collectors.groupingBy(ObsData::getEncounterId));

            List<Integer> keys = new ArrayList<>(vlsGroupedByEncounterId.keySet());

            sort(keys);

            Integer k = null;

            if (no == 6 && keys.size() > 0) {
                k = keys.get(0);
            } else if (no == 12 && keys.size() > 1) {
                k = keys.get(1);
            } else if (no == 24 && keys.size() > 2) {
                k = keys.get(2);
            } else if (no == 36 && keys.size() > 3) {
                k = keys.get(3);
            } else if (no == 48 && keys.size() > 4) {
                k = keys.get(4);
            } else if (no == 60 && keys.size() > 5) {
                k = keys.get(5);
            } else if (no == 72 && keys.size() > 6) {
                k = keys.get(6);
            }
            if (k != null) {
                if (vlsGroupedByEncounterId.get(k) != null && vlsGroupedByEncounterId.get(k).size() > 0) {
                    return vlsGroupedByEncounterId.get(k).get(0);
                }
            } else {
                return null;
            }
        }
        return null;
    }


    public static Map<Integer, List<PersonDemographics>> getPatientDemographics(Connection connection, String patients) throws SQLException {
        String where = "p.voided = 0";

        if (patients != null && StringUtils.isNotBlank(patients)) {
            where = joinQuery(where, constructSQLInQuery("p.person_id", patients), Enums.UgandaEMRJoiner.AND);
        }

        String q = "SELECT\n" +
                "\n" +
                "  person_id,\n" +
                "  gender,\n" +
                "  birthdate,\n" +
                "  (SELECT GROUP_CONCAT(CONCAT_WS(':', COALESCE(pit.uuid, ''), COALESCE(identifier, '')))\n" +
                "   FROM patient_identifier pi INNER JOIN patient_identifier_type pit\n" +
                "       ON (pi.identifier_type = pit.patient_identifier_type_id)\n" +
                "   WHERE pi.patient_id = p.person_id) AS 'identifiers',\n" +
                "  (SELECT GROUP_CONCAT(CONCAT_WS(':', COALESCE(pat.uuid, ''), COALESCE(value, '')))\n" +
                "   FROM person_attribute pa INNER JOIN person_attribute_type pat\n" +
                "       ON (pa.person_attribute_type_id = pat.person_attribute_type_id)\n" +
                "   WHERE p.person_id = pa.person_id)  AS 'attributes',\n" +
                "  (SELECT GROUP_CONCAT(CONCAT_WS(' ', COALESCE(given_name, ''), COALESCE(family_name, '')))\n" +
                "   FROM person_name pn\n" +
                "   WHERE p.person_id = pn.person_id)  AS 'names',\n" +
                "  (SELECT GROUP_CONCAT(CONCAT_WS(':', COALESCE(country, ''),COALESCE(county_district, ''), COALESCE(state_province, ''), COALESCE(address3, ''),\n" +
                "                                 COALESCE(address4, ''), COALESCE(address5, '')))\n" +
                "   FROM person_address pas\n" +
                "   WHERE p.person_id = pas.person_id) AS 'addresses'\n" +
                "FROM person p where " + where;

        List<PersonDemographics> personDemographics = getPersonDemographics(connection, q);
        return personDemographics.stream().collect(Collectors.groupingBy(PersonDemographics::getPersonId));

    }

    public static Map<String, String> processString(String value) {
        Map<String, String> result = new HashMap<>();
        if (StringUtils.isNotBlank(value)) {
            List<String> splitData = Splitter.on(",").splitToList(value);
            for (String split : splitData) {
                if (StringUtils.isNotBlank(split)) {
                    List<String> keyValue = Splitter.on(":").splitToList(split);
                    if (keyValue.size() == 2) {
                        result.put(keyValue.get(0), keyValue.get(1));
                    }
                }
            }
        }
        return result;
    }

    public static List<String> processString2(String value) {
        if (value != null) {
            List<String> splitData = Splitter.on(",").splitToList(value);
            if (splitData.size() > 0) {
                return Splitter.on(":").splitToList(splitData.get(0));
            }
        }
        return new ArrayList<>();
    }

    public static Map<String, String> processString3(String value) {
        Map<String, String> result = new HashMap<>();

        List<String> splitData = Splitter.on(",").splitToList(value);

        for (String split : splitData) {
            List<String> keyValue = Splitter.on(":").splitToList(split);

            if (keyValue.size() >= 2) {
                String k = keyValue.get(0);
                String v = keyValue.get(1);
                if (result.containsKey(k)) {
                    result.put(k, result.get(k) + "," + v);
                } else {
                    result.put(k, v);
                }
            }
        }
        return result;
    }

    public static Map<String, String> processString3(String value, Map<String, String> answers) {
        Map<String, String> result = new HashMap<>();

        List<String> splitData = Splitter.on(",").splitToList(value);

        for (String split : splitData) {
            List<String> keyValue = Splitter.on(":").splitToList(split);

            if (keyValue.size() >= 2) {
                String k = keyValue.get(0);
                String v = keyValue.get(1);
                if (result.containsKey(k)) {
                    result.put(k, result.get(k) + "," + answers.get(v));
                } else {
                    result.put(k, answers.get(v));
                }
            }
        }
        return result;
    }

    public static List<EWIPatientData> getEWIPillPickupPatients(Connection connection, String sql)
            throws SQLException {
        Statement stmt = connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                java.sql.ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(Integer.MIN_VALUE);
        ResultSet rs = stmt.executeQuery(sql);
        List<EWIPatientData> ewiPatientData = new ArrayList<>();
        while (rs.next()) {
            Integer personId = rs.getInt(1);
            String gender = rs.getString(2);
            String dob = rs.getString(3);
            Integer age = rs.getInt(4);
            String deathDate = rs.getString(5);
            String clinicNo = rs.getString(6);
            String artStartDate = rs.getString(7);
            String transferOutDate = rs.getString(8);
            String arv_stop = rs.getString(9);
            EWIPatientData p = new EWIPatientData(personId, gender, dob, deathDate, clinicNo, artStartDate, transferOutDate,arv_stop,age);
            ewiPatientData.add(p);
        }
        rs.close();
        stmt.close();
        return ewiPatientData;
    }
    public static List<Integer> getEWICohort(Connection connection, String sql)
            throws SQLException {
        Statement stmt = connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                java.sql.ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(Integer.MIN_VALUE);
        ResultSet rs = stmt.executeQuery(sql);
        List<Integer> ewiPatients = new ArrayList<>();
        while (rs.next()) {
            Integer patient = rs.getInt(1);
            ewiPatients.add(patient);
        }
        rs.close();
        stmt.close();
        return ewiPatients;
    }
    public static List<EWIPatientEncounter> getEWIPatientEncounters(Connection connection, String sql)
            throws SQLException {
        Statement stmt = connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                java.sql.ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(Integer.MIN_VALUE);
        ResultSet rs = stmt.executeQuery(sql);
        List<EWIPatientEncounter> ewiPatients = new ArrayList<>();
        while (rs.next()) {
            Integer patient = rs.getInt(1);
            Integer encounterId = rs.getInt(2);
            String encounterDate = rs.getString(3);
            String nextVisitDate = rs.getString(4);
            EWIPatientEncounter ewiPatient = new EWIPatientEncounter(patient, encounterId, encounterDate, nextVisitDate);
            ewiPatients.add(ewiPatient);
        }
        rs.close();
        stmt.close();
        return ewiPatients;
    }

    public static List<EWIPatientEncounter> getBaselinePickup(Connection connection, String sql)
            throws SQLException {
        Statement stmt = connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                java.sql.ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(Integer.MIN_VALUE);
        ResultSet rs = stmt.executeQuery(sql);
        List<EWIPatientEncounter> ewiPatients = new ArrayList<>();
        while (rs.next()) {
            Integer patient = rs.getInt(1);
            String baselinepickup = rs.getString(2);

            EWIPatientEncounter ewiPatient = new EWIPatientEncounter(patient, baselinepickup);
            ewiPatients.add(ewiPatient);
        }
        rs.close();
        stmt.close();
        return ewiPatients;
    }

    public static List<EWIPatientEncounter> getNumberOfDaysPickedAtBaseline(Connection connection,String sql) throws SQLException{
        Statement stmt = connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                java.sql.ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(Integer.MIN_VALUE);
        ResultSet rs = stmt.executeQuery(sql);
        List<EWIPatientEncounter> ewiPatients = new ArrayList<>();
        while (rs.next()) {
            Integer patient = rs.getInt(1);
            Integer noOfDays = rs.getInt(2);

            EWIPatientEncounter ewiPatient = new EWIPatientEncounter(patient, noOfDays);
            ewiPatients.add(ewiPatient);
        }
        rs.close();
        stmt.close();
        return ewiPatients;

    }

    public static Connection sqlConnection() throws SQLException, ClassNotFoundException {

        Properties props = new Properties();
        props.setProperty("driver.class", "com.mysql.jdbc.Driver");
        props.setProperty("driver.url", Context.getRuntimeProperties().getProperty("connection.url"));
        props.setProperty("user", Context.getRuntimeProperties().getProperty("connection.username"));
        props.setProperty("password", Context.getRuntimeProperties().getProperty("connection.password"));
        return getDatabaseConnection(props);
    }

    public static Observation searchObservations(List<Observation> observations, Predicate<Observation> predicate) {
        return observations.stream()
                .filter(predicate)
                .findAny()
                .orElse(null);
    }

    public static Connection testSqlConnection() throws SQLException, ClassNotFoundException {
        Properties props = new Properties();
        props.setProperty("driver.class", "com.mysql.jdbc.Driver");
        props.setProperty("driver.url", "jdbc:mysql://localhost:3306/openmrs");
        props.setProperty("user", "openmrs");
        props.setProperty("password", "openmrs");
        return getDatabaseConnection(props);
    }

    public static void addIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition, String dimensionOptions) {
        CohortIndicator ci = new CohortIndicator();
        ci.addParameter(ReportingConstants.START_DATE_PARAMETER);
        ci.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ci.setType(CohortIndicator.IndicatorType.COUNT);
        ci.setCohortDefinition(Mapped.mapStraightThrough(cohortDefinition));
        dsd.addColumn(key, label, Mapped.mapStraightThrough(ci), dimensionOptions);


    }

    public static void addGender(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        if (key == "a") {
            addIndicator(dsd, "2" + key, label, cohortDefinition, "age=below1female");
            addIndicator(dsd, "3" + key, label, cohortDefinition, "age=between1and4female");
            addIndicator(dsd, "4" + key, label, cohortDefinition, "age=between5and9female");
            addIndicator(dsd, "5" + key, label, cohortDefinition, "age=between10and14female");
            addIndicator(dsd, "6" + key, label, cohortDefinition, "age=between15and19female");
            addIndicator(dsd, "7" + key, label, cohortDefinition, "age=between20and24female");
            addIndicator(dsd, "8" + key, label, cohortDefinition, "age=between25and29female");
            addIndicator(dsd, "9" + key, label, cohortDefinition, "age=between30and34female");
            addIndicator(dsd, "10" + key, label, cohortDefinition, "age=between35and39female");
            addIndicator(dsd, "11" + key, label, cohortDefinition, "age=between40and44female");
            addIndicator(dsd, "12" + key, label, cohortDefinition, "age=between45and49female");
            addIndicator(dsd, "13" + key, label, cohortDefinition, "age=above50female");
        } else if (key == "b") {
            addIndicator(dsd, "2" + key, label, cohortDefinition, "age=below1male");
            addIndicator(dsd, "3" + key, label, cohortDefinition, "age=between1and4male");
            addIndicator(dsd, "4" + key, label, cohortDefinition, "age=between5and9male");
            addIndicator(dsd, "5" + key, label, cohortDefinition, "age=between10and14male");
            addIndicator(dsd, "6" + key, label, cohortDefinition, "age=between15and19male");
            addIndicator(dsd, "7" + key, label, cohortDefinition, "age=between20and24male");
            addIndicator(dsd, "8" + key, label, cohortDefinition, "age=between25and29male");
            addIndicator(dsd, "9" + key, label, cohortDefinition, "age=between30and34male");
            addIndicator(dsd, "10" + key, label, cohortDefinition, "age=between35and39male");
            addIndicator(dsd, "11" + key, label, cohortDefinition, "age=between40and44male");
            addIndicator(dsd, "12" + key, label, cohortDefinition, "age=between45and49male");
            addIndicator(dsd, "13" + key, label, cohortDefinition, "age=above50male");
        }
    }

}