package org.openmrs.module.ugandaemrreports.library;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrreports.common.Enums;
import org.openmrs.module.ugandaemrreports.common.ObsData;
import org.openmrs.module.ugandaemrreports.common.PersonDemographics;
import org.openmrs.module.ugandaemrreports.common.StubDate;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

import static java.util.Collections.sort;
import static java.util.Comparator.comparing;


/**
 * Created by carapai on 10/07/2017.
 */
public class UgandaEMRReporting {

    public static List<PersonDemographics> getPersonDemographics(Connection connection, String sql)
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

    public static String constructSQLQuery(String column, String rangeComparator, String value) {
        return column + " " + rangeComparator + " '" + value + "'";
    }

    public static String constructSQLInQuery(String column, Collection<?> values) {
        return column + " IN(" + Joiner.on(",").join(values) + ")";
    }

    public static String constructSQLInQuery(String column, String values) {
        return column + " IN(" + values + ")";
    }

    public static String constructSQLInQuery(String column, List<String> values) {
        String artRegisterObs = "'" + values.get(0) + "'";
        StringBuilder columnBuilder = new StringBuilder(artRegisterObs);

        for (String concept : values.subList(1, values.size())) {
            columnBuilder.append(",'").append(concept).append("'");
        }

        return constructSQLInQuery(column, columnBuilder.toString());
    }


    public static String joinQuery(String query1, String query2, Enums.UgandaEMRJoiner joiner) {
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
        Map<Integer, Date> map = new HashMap<Integer, Date>();
        if (m == null) {
            return map;
        }

        for (Map.Entry<Integer, Collection<Date>> entry : m.asMap().entrySet()) {
            map.put(entry.getKey(), new ArrayList<>(entry.getValue()).get(0));
        }
        return map;
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
            where = UgandaEMRReporting.joinQuery(where, UgandaEMRReporting.constructSQLInQuery("p.person_id", patients), Enums.UgandaEMRJoiner.AND);
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

        List<String> splitData = Splitter.on(",").splitToList(value);

        for (String split : splitData) {
            List<String> keyValue = Splitter.on(":").splitToList(split);

            if (keyValue.size() == 2) {
                result.put(keyValue.get(0), keyValue.get(1));
            }
        }
        return result;
    }

    public static List<String> processString2(String value) {
        List<String> splitData = Splitter.on(",").splitToList(value);

        return Splitter.on(":").splitToList(splitData.get(0));
    }

    public static Connection sqlConnection() throws SQLException, ClassNotFoundException {

        Properties props = new Properties();
        props.setProperty("driver.class", "com.mysql.jdbc.Driver");
        props.setProperty("driver.url", Context.getRuntimeProperties().getProperty("connection.url"));
        props.setProperty("user", Context.getRuntimeProperties().getProperty("connection.username"));
        props.setProperty("password", Context.getRuntimeProperties().getProperty("connection.password"));
        return getDatabaseConnection(props);
    }
}
