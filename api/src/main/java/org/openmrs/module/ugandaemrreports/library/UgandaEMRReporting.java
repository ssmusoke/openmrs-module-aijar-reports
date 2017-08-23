package org.openmrs.module.ugandaemrreports.library;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.*;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reportingcompatibility.service.ReportService;
import org.openmrs.module.ugandaemrreports.common.*;
import org.openmrs.module.ugandaemrreports.definition.predicates.SummarizedObsPeriodPredicate;
import org.openmrs.module.ugandaemrreports.definition.predicates.SummarizedObsPredicate;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

import static java.util.Collections.sort;

/**
 * Created by carapai on 10/07/2017.
 */
public class UgandaEMRReporting {

    public static DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");


    public static List<NormalizedObs> getNormalizedObs(Connection connection, String sql) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        List<NormalizedObs> normalizedObs = new ArrayList<>();
        while (rs.next()) {
            NormalizedObs obs = new NormalizedObs();
            obs.setAgeAtEncounter(rs.getInt("age_at_encounter"));
            obs.setAgeAtObservation(rs.getInt("age_at_observation"));
            obs.setAgeAtValueDatetime(rs.getInt("age_at_value_datetime"));
            obs.setConcept(rs.getString("concept"));
            obs.setConceptName(rs.getString("concept_name"));
            obs.setDateCreated(rs.getDate("date_created"));
            obs.setEncounter(rs.getString("encounter"));
            obs.setEncounterDatetime(rs.getString("encounter_datetime"));
            obs.setEncounterMonth(rs.getInt("encounter_month"));
            obs.setEncounterQuarter(rs.getString("encounter_quarter"));
            obs.setEncounterType(rs.getString("encounter_type"));
            obs.setEncounterTypeName(rs.getString("encounter_type_name"));
            obs.setEncounterYear(rs.getInt("encounter_year"));
            obs.setForm(rs.getString("form"));
            obs.setFormName(rs.getString("form_name"));
            obs.setLocation(rs.getString("location"));
            obs.setLocationName(rs.getString("location_name"));
            obs.setObsDatetime(rs.getDate("obs_datetime"));
            obs.setObsDatetimeMonth(rs.getInt("obs_datetime_month"));
            obs.setObsDatetimeQuarter(rs.getString("obs_datetime_quarter"));
            obs.setObsDatetimeYear(rs.getInt("obs_datetime_year"));
            obs.setObsGroup(rs.getString("obs_group"));
            obs.setObsId(rs.getInt("obs_id"));
            obs.setPerson(rs.getString("person"));
            obs.setPersonId(rs.getInt("person_id"));
            obs.setReportName(rs.getString("report_name"));
            obs.setValueCoded(rs.getString("value_coded"));
            obs.setValueCodedId(rs.getInt("value_coded_id"));
            obs.setValueCodedName(rs.getString("value_coded_name"));
            obs.setValueCodedName1(rs.getString("value_coded_name1"));
            obs.setValueDatetime(rs.getDate("value_datetime"));
            obs.setValueDatetimeMonth(rs.getInt("value_datetime_month"));
            obs.setValueDatetimeQuarter(rs.getString("value_datetime_quarter"));
            obs.setValueDatetimeYear(rs.getInt("value_datetime_year"));
            obs.setValueGroup(rs.getString("value_group"));
            obs.setValueNumeric(rs.getDouble("value_numeric"));
            obs.setValueText(rs.getString("value_text"));
            normalizedObs.add(obs);
        }
        rs.close();
        stmt.close();

        return normalizedObs;
    }

    public static List<SummarizedObs> getSummarizedObs(Connection connection, String sql) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        List<SummarizedObs> summarizedObs = new ArrayList<>();
        while (rs.next()) {
            SummarizedObs obs = new SummarizedObs();
            obs.setConcept(rs.getString("concept"));
            obs.setConceptName(rs.getString("concept_name"));
            obs.setDateGenerated(rs.getDate("date_generated"));
            obs.setEncounterType(rs.getString("encounter_type"));
            obs.setEncounterTypeName(rs.getString("encounter_type_name"));
            obs.setPatients(rs.getString("patients"));
            obs.setPeriod(rs.getString("period"));
            obs.setPeriodGroupedBy(rs.getString("period_grouped_by"));
            obs.setPeriodType(rs.getString("period_type"));
            obs.setTotal(rs.getInt("total"));
            obs.setReportName(rs.getString("report_name"));
            obs.setValueCoded(rs.getString("value_coded"));
            obs.setValueCodedName(rs.getString("value_coded_name"));
            summarizedObs.add(obs);
        }
        rs.close();
        stmt.close();

        return summarizedObs;

    }


    public static List<PersonDemographics> getPersonDemographics(Connection connection, String sql) throws SQLException {

        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
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

    public static void createNormalizedObsTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS obs_normal\n" +
                "(\n" +
                "  obs_id                 INT          NOT NULL,\n" +
                "  person_id              INT          NOT NULL,\n" +
                "  person                 CHAR(38)     NOT NULL,\n" +
                "  encounter_type         CHAR(38)     NULL,\n" +
                "  encounter_type_name    VARCHAR(255) NULL,\n" +
                "  form                   CHAR(38)     NULL,\n" +
                "  form_name              VARCHAR(255) NULL,\n" +
                "  encounter              CHAR(38)     NULL,\n" +
                "  encounter_datetime     DATETIME     NULL,\n" +
                "  age_at_encounter       INT(3)       NULL,\n" +
                "  encounter_year         INT(4),\n" +
                "  encounter_month        INT(6),\n" +
                "  encounter_quarter      CHAR(6),\n" +
                "  concept                CHAR(38)     NOT NULL,\n" +
                "  concept_name           VARCHAR(255) NULL,\n" +
                "  obs_datetime           DATETIME     NOT NULL,\n" +
                "  obs_datetime_year      INT(4),\n" +
                "  obs_datetime_month     INT(6),\n" +
                "  obs_datetime_quarter   CHAR(6),\n" +
                "  age_at_observation     INT(3)       NULL,\n" +
                "  location               CHAR(38)     NULL,\n" +
                "  location_name          VARCHAR(255) NULL,\n" +
                "  obs_group              CHAR(38)     NULL,\n" +
                "  accession_number       VARCHAR(255) NULL,\n" +
                "  value_group            CHAR(38)     NULL,\n" +
                "  value_coded_id          INT(11),\n" +
                "  value_coded            CHAR(38)     NULL,\n" +
                "  value_coded_name1      VARCHAR(255) NULL,\n" +
                "  report_name            VARCHAR(255) NULL,\n" +
                "  value_coded_name       CHAR(38)     NULL,\n" +
                "  value_datetime         DATETIME     NULL,\n" +
                "  value_datetime_year    INT(4),\n" +
                "  value_datetime_month   INT(6),\n" +
                "  value_datetime_quarter CHAR(6),\n" +
                "  age_at_value_datetime  INT(3)       NULL,\n" +
                "  value_numeric          DOUBLE       NULL,\n" +
                "  value_text             TEXT         NULL,\n" +
                "  date_created           DATETIME     NOT NULL\n" +
                "\n" +
                ")\n" +
                "  ENGINE = MYISAM;";

        executeQuery(sql, connection);
    }

    public static void createSummarizedObsTable(Connection connection, String table) throws SQLException {
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s\n", table) +
                "(\n" +
                "  encounter_type      CHAR(38)     NULL,\n" +
                "  encounter_type_name VARCHAR(255) NULL,\n" +
                "  concept             CHAR(38)     NULL,\n" +
                "  concept_name        VARCHAR(255) NULL,\n" +
                "  value_coded         LONGTEXT     NULL,\n" +
                "  value_coded_name    VARCHAR(255) NULL,\n" +
                "  report_name         VARCHAR(255) NULL,\n" +
                "  period              CHAR(6)      NULL,\n" +
                "  patients            LONGTEXT     NULL,\n" +
                "  total               INT(32)      NULL,\n" +
                "  period_type         CHAR(10)     NULL,\n" +
                "  date_generated      DATETIME     NULL,\n" +
                "  period_grouped_by   CHAR(20)     NULL\n" +
                ")\n" +
                "  ENGINE = MYISAM;";

        executeQuery(sql, connection);
    }

    public static int normalizeObs(String startDate, Connection connection, int number) throws SQLException {
        String all = String.format("SELECT count(*) AS rowcount FROM obs WHERE date_created > '%s' AND voided = 0", startDate);

        Statement s = connection.createStatement();
        ResultSet r = s.executeQuery(all);
        r.next();
        int total = r.getInt("rowcount");
        r.close();

        int d = 0;

        createNormalizedObsTable(connection);
        for (int i = 0; i < total / number + 1; i++) {
            int offset = i * number;
            String sql = "INSERT INTO obs_normal (obs_id\n" +
                    "  , person_id\n" +
                    "  , person\n" +
                    "  , encounter_type\n" +
                    "  , encounter_type_name\n" +
                    "  , form\n" +
                    "  , form_name\n" +
                    "  , encounter\n" +
                    "  , encounter_datetime\n" +
                    "  , age_at_encounter\n" +
                    "  , encounter_year\n" +
                    "  , encounter_month\n" +
                    "  , encounter_quarter\n" +
                    "  , concept\n" +
                    "  , concept_name\n" +
                    "  , obs_datetime\n" +
                    "  , obs_datetime_year\n" +
                    "  , obs_datetime_month\n" +
                    "  , obs_datetime_quarter\n" +
                    "  , age_at_observation\n" +
                    "  , location\n" +
                    "  , location_name\n" +
                    "  , obs_group\n" +
                    "  , accession_number\n" +
                    "  , value_group\n" +
                    "  , value_coded_id\n" +
                    "  , value_coded\n" +
                    "  , value_coded_name1\n" +
                    "  , report_name\n" +
                    "  , value_coded_name\n" +
                    "  , value_datetime\n" +
                    "  , value_datetime_year\n" +
                    "  , value_datetime_month\n" +
                    "  , value_datetime_quarter\n" +
                    "  , age_at_value_datetime\n" +
                    "  , value_numeric\n" +
                    "  , value_text\n" +
                    "  , date_created\n" +
                    ")\n" +
                    "\n" +
                    "  SELECT\n" +
                    "    o.obs_id,\n" +
                    "    o.person_id,\n" +
                    "    (SELECT p.uuid\n" +
                    "     FROM person AS p\n" +
                    "     WHERE p.person_id = o.person_id)                                      AS person,\n" +
                    "    (SELECT et.uuid\n" +
                    "     FROM encounter_type AS et\n" +
                    "     WHERE et.encounter_type_id = (SELECT e.encounter_type\n" +
                    "                                   FROM encounter AS e\n" +
                    "                                   WHERE e.encounter_id = o.encounter_id)) AS encounter_type,\n" +
                    "    (SELECT et.name\n" +
                    "     FROM encounter_type AS et\n" +
                    "     WHERE et.encounter_type_id = (SELECT e.encounter_type\n" +
                    "                                   FROM encounter AS e\n" +
                    "                                   WHERE e.encounter_id =\n" +
                    "                                         o.encounter_id))                  AS encounter_type_name,\n" +
                    "    (SELECT f.uuid\n" +
                    "     FROM form AS f\n" +
                    "     WHERE f.form_id = (SELECT e.form_id\n" +
                    "                        FROM encounter AS e\n" +
                    "                        WHERE e.encounter_id = o.encounter_id))            AS form,\n" +
                    "    (SELECT f.name\n" +
                    "     FROM form AS f\n" +
                    "     WHERE f.form_id = (SELECT e.form_id\n" +
                    "                        FROM encounter AS e\n" +
                    "                        WHERE e.encounter_id = o.encounter_id))            AS form_name,\n" +
                    "    (SELECT e.uuid\n" +
                    "     FROM encounter AS e\n" +
                    "     WHERE e.encounter_id = o.encounter_id)                                AS encounter,\n" +
                    "    (SELECT e.encounter_datetime\n" +
                    "     FROM encounter AS e\n" +
                    "     WHERE e.encounter_id =\n" +
                    "           o.encounter_id)                                                 AS encounter_datetime,\n" +
                    "\n" +
                    "    (SELECT YEAR(e.encounter_datetime) - YEAR(p.birthdate) - (RIGHT(e.encounter_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "     FROM encounter AS e INNER JOIN person p ON (p.person_id = e.patient_id)\n" +
                    "     WHERE e.encounter_id =\n" +
                    "           o.encounter_id)                                                 AS age_at_encounter,\n" +
                    "    (SELECT YEAR(e.encounter_datetime)\n" +
                    "     FROM encounter AS e\n" +
                    "     WHERE e.encounter_id = o.encounter_id)                                AS encounter_year,\n" +
                    "    (SELECT CONCAT(YEAR(e.encounter_datetime),\n" +
                    "                   if(MONTH(e.encounter_datetime) < 10, CONCAT('0', MONTH(e.encounter_datetime)),\n" +
                    "                      MONTH(e.encounter_datetime)))\n" +
                    "     FROM encounter AS e\n" +
                    "     WHERE e.encounter_id =\n" +
                    "           o.encounter_id)                                                 AS encounter_month,\n" +
                    "\n" +
                    "    (SELECT CONCAT(YEAR(e.encounter_datetime), 'Q', QUARTER(e.encounter_datetime))\n" +
                    "     FROM encounter AS e\n" +
                    "     WHERE e.encounter_id =\n" +
                    "           o.encounter_id)                                                 AS encounter_quarter,\n" +
                    "    (SELECT uuid\n" +
                    "     FROM concept AS c\n" +
                    "     WHERE c.concept_id = o.concept_id)                                    AS concept,\n" +
                    "    (SELECT c.name\n" +
                    "     FROM concept_name AS c\n" +
                    "     WHERE c.concept_id = o.concept_id AND c.concept_name_type = 'FULLY_SPECIFIED' AND c.locale = 'en'\n" +
                    "     LIMIT 1)                                                              AS concept_name,\n" +
                    "    obs_datetime,\n" +
                    "    YEAR(\n" +
                    "        obs_datetime)                                                      AS obs_datetime_year,\n" +
                    "    CONCAT(YEAR(obs_datetime),\n" +
                    "           if(MONTH(obs_datetime) < 10, CONCAT('0', MONTH(obs_datetime)), MONTH(\n" +
                    "               obs_datetime)))                                             AS obs_datetime_month,\n" +
                    "    CONCAT(YEAR(obs_datetime), 'Q', QUARTER(\n" +
                    "        obs_datetime))                                                     AS obs_datetime_quarter,\n" +
                    "    (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "     FROM person AS p\n" +
                    "     WHERE p.person_id = o.person_id)                                      AS age_at_observation,\n" +
                    "    (SELECT l.uuid\n" +
                    "     FROM location AS l\n" +
                    "     WHERE o.location_id = l.location_id)                                  AS location,\n" +
                    "    (SELECT l.name\n" +
                    "     FROM location AS l\n" +
                    "     WHERE o.location_id = l.location_id)                                  AS location_name,\n" +
                    "    (SELECT uuid\n" +
                    "     FROM obs AS oi\n" +
                    "     WHERE oi.obs_id = o.obs_group_id)                                     AS obs_group,\n" +
                    "    accession_number,\n" +
                    "    value_group_id                                                         AS value_group,\n" +
                    "    value_coded                                                            AS value_code_id,\n" +
                    "    (SELECT uuid\n" +
                    "     FROM concept AS c\n" +
                    "     WHERE c.concept_id = o.value_coded)                                   AS value_coded,\n" +
                    "    (SELECT cn.name\n" +
                    "     FROM concept_name AS cn\n" +
                    "     WHERE cn.concept_id = o.value_coded AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.locale = 'en'\n" +
                    "     LIMIT 1)                                                              AS value_coded_name1,\n" +
                    "    CASE\n" +
                    "    WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "          FROM person AS p\n" +
                    "          WHERE p.person_id = o.person_id) > 10 AND value_coded = 99015\n" +
                    "      THEN '1a'\n" +
                    "    WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "          FROM person AS p\n" +
                    "          WHERE p.person_id = o.person_id) <= 10 AND value_coded = 99015\n" +
                    "      THEN '4a'\n" +
                    "    WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "          FROM person AS p\n" +
                    "          WHERE p.person_id = o.person_id) > 10 AND value_coded = 99016\n" +
                    "      THEN '1b'\n" +
                    "    WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "          FROM person AS p\n" +
                    "          WHERE p.person_id = o.person_id) <= 10 AND value_coded = 99016\n" +
                    "      THEN '4b'\n" +
                    "    WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "          FROM person AS p\n" +
                    "          WHERE p.person_id = o.person_id) > 10 AND value_coded = 99005\n" +
                    "      THEN '1c'\n" +
                    "    WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "          FROM person AS p\n" +
                    "          WHERE p.person_id = o.person_id) <= 10 AND value_coded = 99005\n" +
                    "      THEN '4c'\n" +
                    "    WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "          FROM person AS p\n" +
                    "          WHERE p.person_id = o.person_id) > 10 AND value_coded = 99006\n" +
                    "      THEN '1d'\n" +
                    "    WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "          FROM person AS p\n" +
                    "          WHERE p.person_id = o.person_id) <= 10 AND value_coded = 99006\n" +
                    "      THEN '4d'\n" +
                    "    WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "          FROM person AS p\n" +
                    "          WHERE p.person_id = o.person_id) > 10 AND value_coded = 99039\n" +
                    "      THEN '1e'\n" +
                    "\n" +
                    "    WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "          FROM person AS p\n" +
                    "          WHERE p.person_id = o.person_id) <= 10 AND value_coded = 99039\n" +
                    "      THEN '4j'\n" +
                    "    WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "          FROM person AS p\n" +
                    "          WHERE p.person_id = o.person_id) > 10 AND value_coded = 99040\n" +
                    "      THEN '1f'\n" +
                    "\n" +
                    "    WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "          FROM person AS p\n" +
                    "          WHERE p.person_id = o.person_id) <= 10 AND value_coded = 99040\n" +
                    "      THEN '4i'\n" +
                    "    WHEN value_coded = 99041\n" +
                    "      THEN '1g'\n" +
                    "    WHEN value_coded = 99042\n" +
                    "      THEN '1h'\n" +
                    "    WHEN value_coded = 99007\n" +
                    "      THEN '2a2'\n" +
                    "    WHEN value_coded = 99008\n" +
                    "      THEN '2a4'\n" +
                    "    WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "          FROM person AS p\n" +
                    "          WHERE p.person_id = o.person_id) > 10 AND value_coded = 99044\n" +
                    "      THEN '2b'\n" +
                    "    WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "          FROM person AS p\n" +
                    "          WHERE p.person_id = o.person_id) <= 10 AND value_coded = 99044\n" +
                    "      THEN '5d'\n" +
                    "    WHEN value_coded = 99043\n" +
                    "      THEN '2c'\n" +
                    "    WHEN value_coded = 99282\n" +
                    "      THEN '2d2'\n" +
                    "    WHEN value_coded = 99283\n" +
                    "      THEN '2d4'\n" +
                    "    WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "          FROM person AS p\n" +
                    "          WHERE p.person_id = o.person_id) > 10 AND value_coded = 99046\n" +
                    "      THEN '2e'\n" +
                    "    WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "          FROM person AS p\n" +
                    "          WHERE p.person_id = o.person_id) <= 10 AND value_coded = 99046\n" +
                    "      THEN '5l'\n" +
                    "    WHEN value_coded = 99017\n" +
                    "      THEN '5a'\n" +
                    "    WHEN value_coded = 99018\n" +
                    "      THEN '5b'\n" +
                    "    WHEN value_coded = 99045\n" +
                    "      THEN '5f'\n" +
                    "    WHEN value_coded = 99284\n" +
                    "      THEN '5g'\n" +
                    "    WHEN value_coded = 99285\n" +
                    "      THEN '5h'\n" +
                    "    WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "          FROM person AS p\n" +
                    "          WHERE p.person_id = o.person_id) > 10 AND value_coded = 99286\n" +
                    "      THEN '2c'\n" +
                    "    WHEN (SELECT YEAR(obs_datetime) - YEAR(p.birthdate) - (RIGHT(obs_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "          FROM person AS p\n" +
                    "          WHERE p.person_id = o.person_id) <= 10 AND value_coded = 99286\n" +
                    "      THEN '5l'\n" +
                    "    WHEN value_coded = 99884\n" +
                    "      THEN '4e'\n" +
                    "    WHEN value_coded = 99885\n" +
                    "      THEN '4f'\n" +
                    "    WHEN value_coded = 99888\n" +
                    "      THEN '2h'\n" +
                    "    WHEN value_coded = 163017\n" +
                    "      THEN '2g'\n" +
                    "    WHEN value_coded = 90002\n" +
                    "      THEN 'othr'\n" +
                    "    WHEN value_coded IN (90033, 90079,1204)\n" +
                    "      THEN '1'\n" +
                    "    WHEN value_coded IN (90034, 90073,1205)\n" +
                    "      THEN '2'\n" +
                    "    WHEN value_coded IN (90035, 90078,1206)\n" +
                    "      THEN '3'\n" +
                    "    WHEN value_coded IN (90036, 90071,1207)\n" +
                    "      THEN '4'\n" +
                    "    WHEN value_coded = 90293\n" +
                    "      THEN 'T1'\n" +
                    "    WHEN value_coded = 90294\n" +
                    "      THEN 'T2'\n" +
                    "    WHEN value_coded = 90295\n" +
                    "      THEN 'T3'\n" +
                    "    WHEN value_coded = 90295\n" +
                    "      THEN 'T4'\n" +
                    "    WHEN value_coded = 90156\n" +
                    "      THEN 'G'\n" +
                    "    WHEN value_coded = 90157\n" +
                    "      THEN 'F'\n" +
                    "    WHEN value_coded = 90158\n" +
                    "      THEN 'P'\n" +
                    "    WHEN value_coded = 90003\n" +
                    "      THEN 'Y'\n" +
                    "    ELSE ''\n" +
                    "    END                                                                    AS report_name,\n" +
                    "    (SELECT cn.uuid\n" +
                    "     FROM concept_name AS cn\n" +
                    "     WHERE cn.concept_name_id = o.value_coded_name_id)                     AS value_coded_name,\n" +
                    "    value_datetime,\n" +
                    "    YEAR(\n" +
                    "        value_datetime)                                                    AS value_datetime_year,\n" +
                    "    CONCAT(YEAR(value_datetime),\n" +
                    "           if(MONTH(value_datetime) < 10, CONCAT('0', MONTH(value_datetime)), MONTH(\n" +
                    "               value_datetime)))                                           AS value_datetime_month,\n" +
                    "    CONCAT(YEAR(value_datetime), 'Q', QUARTER(value_datetime))             AS value_datetime_quarter,\n" +
                    "    (SELECT YEAR(value_datetime) - YEAR(p.birthdate) - (RIGHT(value_datetime, 5) < RIGHT(p.birthdate, 5))\n" +
                    "     FROM person AS p\n" +
                    "     WHERE p.person_id = o.person_id)                                      AS age_at_value_datetime,\n" +
                    "    value_numeric,\n" +
                    "    value_text,\n" +
                    "    date_created\n" +
                    "  FROM obs o\n" +
                    String.format("  WHERE o.date_created > '%s' AND o.voided = 0 LIMIT %s,%s;", startDate, offset, number);
            d += d + executeQuery(sql, connection);
        }
        return d;
    }

    public static String obsSummaryMonthQuery(String startDate) {
        String sql = "INSERT INTO obs_summary (\n" +
                "  encounter_type,\n" +
                "  encounter_type_name,\n" +
                "  concept,\n" +
                "  concept_name,\n" +
                "  value_coded,\n" +
                "  value_coded_name,\n" +
                "  report_name,\n" +
                "  period,\n" +
                "  patients,\n" +
                "  total,\n" +
                "  period_type,\n" +
                "  date_generated,\n" +
                "  period_grouped_by)\n" +
                "\n" +
                "-- value_coded grouped by months\n" +
                "  SELECT\n" +
                "    encounter_type,\n" +
                "    encounter_type_name,\n" +
                "    concept,\n" +
                "    concept_name,\n" +
                "    value_coded,\n" +
                "    value_coded_name1,\n" +
                "    report_name,\n" +
                "    obs_datetime_month,\n" +
                "    GROUP_CONCAT(DISTINCT person_id ORDER BY person_id ASC),\n" +
                "    COUNT(DISTINCT person_id),\n" +
                "    'monthly',\n" +
                "    NOW(),\n" +
                "    'obs_datetime'\n" +
                "  FROM obs_normal\n" +
                "  WHERE date_created > '1900-01-01' AND value_coded IS NOT NULL\n" +
                "  GROUP BY encounter_type, concept, value_coded, obs_datetime_month\n" +
                "  UNION ALL\n" +
                "  -- value_datetime grouped by months\n" +
                "  SELECT\n" +
                "    encounter_type,\n" +
                "    encounter_type_name,\n" +
                "    concept,\n" +
                "    concept_name,\n" +
                "    GROUP_CONCAT(DISTINCT CONCAT_WS(':', person_id, DATE(value_datetime),obs_datetime_month) ORDER BY person_id),\n" +
                "    NULL,\n" +
                "    NULL,\n" +
                "    value_datetime_month,\n" +
                "    GROUP_CONCAT(DISTINCT person_id ORDER BY person_id ASC),\n" +
                "    COUNT(DISTINCT person_id),\n" +
                "    'monthly',\n" +
                "    NOW(),\n" +
                "    'value_datetime'\n" +
                "  FROM obs_normal\n" +
                "  WHERE date_created > '1900-01-01' AND value_datetime IS NOT NULL\n" +
                "  GROUP BY encounter_type, concept, value_datetime_month\n" +
                "  UNION ALL\n" +
                "  -- values_numeric\n" +
                "  SELECT\n" +
                "    encounter_type,\n" +
                "    encounter_type_name,\n" +
                "    concept,\n" +
                "    concept_name,\n" +
                "    GROUP_CONCAT(DISTINCT CONCAT_WS(':', person_id, value_numeric,obs_datetime_month) ORDER BY person_id),\n" +
                "    NULL,\n" +
                "    NULL,\n" +
                "    obs_datetime_month,\n" +
                "    GROUP_CONCAT(DISTINCT person_id ORDER BY person_id ASC),\n" +
                "    COUNT(DISTINCT person_id),\n" +
                "    'monthly',\n" +
                "    NOW(),\n" +
                "    'encounter_datetime'\n" +
                "  FROM obs_normal\n" +
                "  WHERE date_created > '1900-01-01' AND value_numeric IS NOT NULL\n" +
                "  GROUP BY encounter_type, concept, obs_datetime_month\n" +
                "  UNION ALL\n" +
                "\n" +
                "  -- values_text\n" +
                "  SELECT\n" +
                "    encounter_type,\n" +
                "    encounter_type_name,\n" +
                "    concept,\n" +
                "    concept_name,\n" +
                "    GROUP_CONCAT(DISTINCT CONCAT_WS(':', person_id, value_text,obs_datetime_month) ORDER BY person_id),\n" +
                "    NULL,\n" +
                "    NULL,\n" +
                "    obs_datetime_month,\n" +
                "    GROUP_CONCAT(DISTINCT person_id ORDER BY person_id ASC),\n" +
                "    COUNT(DISTINCT person_id),\n" +
                "    'monthly',\n" +
                "    NOW(),\n" +
                "    'encounter_datetime'\n" +
                "  FROM obs_normal\n" +
                "  WHERE date_created > '1900-01-01' AND value_text IS NOT NULL\n" +
                "  GROUP BY encounter_type, concept, obs_datetime_month\n" +
                "  UNION ALL\n" +
                "  SELECT\n" +
                "    encounter_type,\n" +
                "    encounter_type_name,\n" +
                "    'encounters',\n" +
                "    'encounters',\n" +
                "    'encounters',\n" +
                "    'encounters',\n" +
                "    NULL,\n" +
                "    encounter_month,\n" +
                "  GROUP_CONCAT(DISTINCT CONCAT_WS(':', person_id, DATE(encounter_datetime)) ORDER BY person_id ASC),\n" +
                "    COUNT(DISTINCT person_id),\n" +
                "    'monthly',\n" +
                "    NOW(),\n" +
                "    'encounter_datetime'\n" +
                "  FROM obs_normal\n" +
                "  WHERE date_created > '1900-01-01'\n" +
                "  GROUP BY encounter_type, encounter_month\n" +
                "  UNION ALL\n" +
                "  -- birth dates grouped by months\n" +
                "  SELECT\n" +
                "    'births',\n" +
                "    'births',\n" +
                "    'births',\n" +
                "    'births',\n" +
                "    'births',\n" +
                "    'births',\n" +
                "    NULL,\n" +
                "    CONCAT(YEAR(birthdate),\n" +
                "           if(MONTH(birthdate) < 10, CONCAT('0', MONTH(birthdate)),\n" +
                "              MONTH(birthdate))),\n" +
                "    GROUP_CONCAT(DISTINCT person_id ORDER BY person_id ASC),\n" +
                "    COUNT(DISTINCT person_id),\n" +
                "    'monthly',\n" +
                "    NOW(),\n" +
                "    'birth_date'\n" +
                "  FROM person\n" +
                "  WHERE date_created > '1900-01-01' AND birthdate IS NOT NULL\n" +
                "  GROUP BY CONCAT(YEAR(birthdate),\n" +
                "                  if(MONTH(birthdate) < 10, CONCAT('0', MONTH(birthdate)),\n" +
                "                     MONTH(birthdate)))\n" +
                "  UNION ALL\n" +
                "  -- death dates grouped by months\n" +
                "  SELECT\n" +
                "    'deaths',\n" +
                "    'deaths',\n" +
                "    'deaths',\n" +
                "    'deaths',\n" +
                "    'deaths',\n" +
                "    'deaths',\n" +
                "    NULL,\n" +
                "    CONCAT(YEAR(death_date),\n" +
                "           if(MONTH(death_date) < 10, CONCAT('0', MONTH(death_date)),\n" +
                "              MONTH(death_date))),\n" +
                "    GROUP_CONCAT(DISTINCT person_id ORDER BY person_id ASC),\n" +
                "    COUNT(DISTINCT person_id),\n" +
                "    'monthly',\n" +
                "    NOW(),\n" +
                "    'death_date'\n" +
                "  FROM person\n" +
                "  WHERE date_created > '1900-01-01' AND death_date IS NOT NULL\n" +
                "  GROUP BY CONCAT(YEAR(death_date),\n" +
                "                  if(MONTH(death_date) < 10, CONCAT('0', MONTH(death_date)),\n" +
                "                     MONTH(death_date)));";

        return sql.replace("1900-01-01", startDate);

    }

    public static String obsSummaryYearQuery(String startDate) {
        String sql = "INSERT INTO obs_summary_year (\n" +
                "  encounter_type,\n" +
                "  encounter_type_name,\n" +
                "  concept,\n" +
                "  concept_name,\n" +
                "  value_coded,\n" +
                "  value_coded_name,\n" +
                "  report_name,\n" +
                "  period,\n" +
                "  patients,\n" +
                "  total,\n" +
                "  period_type,\n" +
                "  date_generated,\n" +
                "  period_grouped_by)\n" +
                "\n" +
                "-- value_coded grouped by months\n" +
                "  SELECT\n" +
                "    encounter_type,\n" +
                "    encounter_type_name,\n" +
                "    concept,\n" +
                "    concept_name,\n" +
                "    value_coded,\n" +
                "    value_coded_name1,\n" +
                "    report_name,\n" +
                "    obs_datetime_year,\n" +
                "    GROUP_CONCAT(DISTINCT person_id ORDER BY person_id ASC),\n" +
                "    COUNT(DISTINCT person_id),\n" +
                "    'monthly',\n" +
                "    NOW(),\n" +
                "    'obs_datetime'\n" +
                "  FROM obs_normal\n" +
                "  WHERE date_created > '1900-01-01' AND value_coded IS NOT NULL\n" +
                "  GROUP BY encounter_type, concept, value_coded, obs_datetime_year\n" +
                "  UNION ALL\n" +
                "  -- value_datetime grouped by months\n" +
                "  SELECT\n" +
                "    encounter_type,\n" +
                "    encounter_type_name,\n" +
                "    concept,\n" +
                "    concept_name,\n" +
                "    GROUP_CONCAT(DISTINCT CONCAT_WS(':', person_id, DATE(value_datetime), obs_datetime_month) ORDER BY person_id),\n" +
                "    NULL,\n" +
                "    NULL,\n" +
                "    value_datetime_year,\n" +
                "    GROUP_CONCAT(DISTINCT person_id ORDER BY person_id ASC),\n" +
                "    COUNT(DISTINCT person_id),\n" +
                "    'monthly',\n" +
                "    NOW(),\n" +
                "    'value_datetime'\n" +
                "  FROM obs_normal\n" +
                "  WHERE date_created > '1900-01-01' AND value_datetime IS NOT NULL\n" +
                "  GROUP BY encounter_type, concept, value_datetime_year\n" +
                "  UNION ALL\n" +
                "  -- values_numeric\n" +
                "  SELECT\n" +
                "    encounter_type,\n" +
                "    encounter_type_name,\n" +
                "    concept,\n" +
                "    concept_name,\n" +
                "    GROUP_CONCAT(DISTINCT CONCAT_WS(':', person_id, value_numeric, obs_datetime_month) ORDER BY person_id),\n" +
                "    NULL,\n" +
                "    NULL,\n" +
                "    obs_datetime_year,\n" +
                "    GROUP_CONCAT(DISTINCT person_id ORDER BY person_id ASC),\n" +
                "    COUNT(DISTINCT person_id),\n" +
                "    'monthly',\n" +
                "    NOW(),\n" +
                "    'encounter_datetime'\n" +
                "  FROM obs_normal\n" +
                "  WHERE date_created > '1900-01-01' AND value_numeric IS NOT NULL\n" +
                "  GROUP BY encounter_type, concept, obs_datetime_year\n" +
                "  UNION ALL\n" +
                "\n" +
                "  -- values_text\n" +
                "  SELECT\n" +
                "    encounter_type,\n" +
                "    encounter_type_name,\n" +
                "    concept,\n" +
                "    concept_name,\n" +
                "    GROUP_CONCAT(DISTINCT CONCAT_WS(':', person_id, value_text, obs_datetime_month) ORDER BY person_id),\n" +
                "    NULL,\n" +
                "    NULL,\n" +
                "    obs_datetime_year,\n" +
                "    GROUP_CONCAT(DISTINCT person_id ORDER BY person_id ASC),\n" +
                "    COUNT(DISTINCT person_id),\n" +
                "    'monthly',\n" +
                "    NOW(),\n" +
                "    'encounter_datetime'\n" +
                "  FROM obs_normal\n" +
                "  WHERE date_created > '1900-01-01' AND value_text IS NOT NULL\n" +
                "  GROUP BY encounter_type, concept, obs_datetime_year\n" +
                "  UNION ALL\n" +
                "  SELECT\n" +
                "    encounter_type,\n" +
                "    encounter_type_name,\n" +
                "    'encounters',\n" +
                "    'encounters',\n" +
                "    'encounters',\n" +
                "    'encounters',\n" +
                "    NULL,\n" +
                "    encounter_year,\n" +
                "    GROUP_CONCAT(DISTINCT CONCAT_WS(':', person_id, DATE(encounter_datetime)) ORDER BY person_id ASC),\n" +
                "    COUNT(DISTINCT person_id),\n" +
                "    'monthly',\n" +
                "    NOW(),\n" +
                "    'encounter_datetime'\n" +
                "  FROM obs_normal\n" +
                "  WHERE date_created > '1900-01-01'\n" +
                "  GROUP BY encounter_type, encounter_year\n" +
                "  UNION ALL\n" +
                "  -- birth dates grouped by months\n" +
                "  SELECT\n" +
                "    'births',\n" +
                "    'births',\n" +
                "    'births',\n" +
                "    'births',\n" +
                "    'births',\n" +
                "    'births',\n" +
                "    NULL,\n" +
                "    YEAR(birthdate),\n" +
                "    GROUP_CONCAT(DISTINCT person_id ORDER BY person_id ASC),\n" +
                "    COUNT(DISTINCT person_id),\n" +
                "    'monthly',\n" +
                "    NOW(),\n" +
                "    'birth_date'\n" +
                "  FROM person\n" +
                "  WHERE date_created > '1900-01-01' AND birthdate IS NOT NULL\n" +
                "  GROUP BY YEAR(birthdate)\n" +
                "  UNION ALL\n" +
                "  -- death dates grouped by months\n" +
                "  SELECT\n" +
                "    'deaths',\n" +
                "    'deaths',\n" +
                "    'deaths',\n" +
                "    'deaths',\n" +
                "    'deaths',\n" +
                "    'deaths',\n" +
                "    NULL,\n" +
                "    YEAR(death_date),\n" +
                "    GROUP_CONCAT(DISTINCT person_id ORDER BY person_id ASC),\n" +
                "    COUNT(DISTINCT person_id),\n" +
                "    'monthly',\n" +
                "    NOW(),\n" +
                "    'death_date'\n" +
                "  FROM person\n" +
                "  WHERE date_created > '1900-01-01' AND death_date IS NOT NULL\n" +
                "  GROUP BY YEAR(death_date);";

        return sql.replace("1900-01-01", startDate);
    }

    public static String obsSummaryQuarterQuery(String startDate) {
        String sql = "INSERT INTO obs_summary_quarter (\n" +
                "  encounter_type,\n" +
                "  encounter_type_name,\n" +
                "  concept,\n" +
                "  concept_name,\n" +
                "  value_coded,\n" +
                "  value_coded_name,\n" +
                "  report_name,\n" +
                "  period,\n" +
                "  patients,\n" +
                "  total,\n" +
                "  period_type,\n" +
                "  date_generated,\n" +
                "  period_grouped_by)\n" +
                "\n" +
                "-- value_coded grouped by months\n" +
                "  SELECT\n" +
                "    encounter_type,\n" +
                "    encounter_type_name,\n" +
                "    concept,\n" +
                "    concept_name,\n" +
                "    value_coded,\n" +
                "    value_coded_name1,\n" +
                "    report_name,\n" +
                "    obs_datetime_quarter,\n" +
                "    GROUP_CONCAT(DISTINCT person_id ORDER BY person_id ASC),\n" +
                "    COUNT(DISTINCT person_id),\n" +
                "    'monthly',\n" +
                "    NOW(),\n" +
                "    'obs_datetime'\n" +
                "  FROM obs_normal\n" +
                "  WHERE date_created > '1900-01-01' AND value_coded IS NOT NULL\n" +
                "  GROUP BY encounter_type, concept, value_coded, obs_datetime_quarter\n" +
                "  UNION ALL\n" +
                "  -- value_datetime grouped by months\n" +
                "  SELECT\n" +
                "    encounter_type,\n" +
                "    encounter_type_name,\n" +
                "    concept,\n" +
                "    concept_name,\n" +
                "    GROUP_CONCAT(DISTINCT CONCAT_WS(':', person_id, DATE(value_datetime), obs_datetime_month) ORDER BY person_id),\n" +
                "    NULL,\n" +
                "    NULL,\n" +
                "    value_datetime_quarter,\n" +
                "    GROUP_CONCAT(DISTINCT person_id ORDER BY person_id ASC),\n" +
                "    COUNT(DISTINCT person_id),\n" +
                "    'monthly',\n" +
                "    NOW(),\n" +
                "    'value_datetime'\n" +
                "  FROM obs_normal\n" +
                "  WHERE date_created > '1900-01-01' AND value_datetime IS NOT NULL\n" +
                "  GROUP BY encounter_type, concept, value_datetime_quarter\n" +
                "  UNION ALL\n" +
                "  -- values_numeric\n" +
                "  SELECT\n" +
                "    encounter_type,\n" +
                "    encounter_type_name,\n" +
                "    concept,\n" +
                "    concept_name,\n" +
                "    GROUP_CONCAT(DISTINCT CONCAT_WS(':', person_id, value_numeric, obs_datetime_month) ORDER BY person_id),\n" +
                "    NULL,\n" +
                "    NULL,\n" +
                "    obs_datetime_quarter,\n" +
                "    GROUP_CONCAT(DISTINCT person_id ORDER BY person_id ASC),\n" +
                "    COUNT(DISTINCT person_id),\n" +
                "    'monthly',\n" +
                "    NOW(),\n" +
                "    'encounter_datetime'\n" +
                "  FROM obs_normal\n" +
                "  WHERE date_created > '1900-01-01' AND value_numeric IS NOT NULL\n" +
                "  GROUP BY encounter_type, concept, obs_datetime_quarter\n" +
                "  UNION ALL\n" +
                "\n" +
                "  -- values_text\n" +
                "  SELECT\n" +
                "    encounter_type,\n" +
                "    encounter_type_name,\n" +
                "    concept,\n" +
                "    concept_name,\n" +
                "    GROUP_CONCAT(DISTINCT CONCAT_WS(':', person_id, value_text, obs_datetime_month) ORDER BY person_id),\n" +
                "    NULL,\n" +
                "    NULL,\n" +
                "    obs_datetime_quarter,\n" +
                "    GROUP_CONCAT(DISTINCT person_id ORDER BY person_id ASC),\n" +
                "    COUNT(DISTINCT person_id),\n" +
                "    'monthly',\n" +
                "    NOW(),\n" +
                "    'encounter_datetime'\n" +
                "  FROM obs_normal\n" +
                "  WHERE date_created > '1900-01-01' AND value_text IS NOT NULL\n" +
                "  GROUP BY encounter_type, concept, obs_datetime_quarter\n" +
                "  UNION ALL\n" +
                "  SELECT\n" +
                "    encounter_type,\n" +
                "    encounter_type_name,\n" +
                "    'encounters',\n" +
                "    'encounters',\n" +
                "    'encounters',\n" +
                "    'encounters',\n" +
                "    NULL,\n" +
                "    encounter_quarter,\n" +
                "    GROUP_CONCAT(DISTINCT CONCAT_WS(':', person_id, DATE(encounter_datetime)) ORDER BY person_id ASC),\n" +
                "    COUNT(DISTINCT person_id),\n" +
                "    'monthly',\n" +
                "    NOW(),\n" +
                "    'encounter_datetime'\n" +
                "  FROM obs_normal\n" +
                "  WHERE date_created > '1900-01-01'\n" +
                "  GROUP BY encounter_type, encounter_quarter\n" +
                "  UNION ALL\n" +
                "  -- birth dates grouped by months\n" +
                "  SELECT\n" +
                "    'births',\n" +
                "    'births',\n" +
                "    'births',\n" +
                "    'births',\n" +
                "    'births',\n" +
                "    'births',\n" +
                "    NULL,\n" +
                "    CONCAT(YEAR(birthdate), 'Q', QUARTER(birthdate)),\n" +
                "    GROUP_CONCAT(DISTINCT person_id ORDER BY person_id ASC),\n" +
                "    COUNT(DISTINCT person_id),\n" +
                "    'monthly',\n" +
                "    NOW(),\n" +
                "    'birth_date'\n" +
                "  FROM person\n" +
                "  WHERE date_created > '1900-01-01' AND birthdate IS NOT NULL\n" +
                "  GROUP BY CONCAT(YEAR(birthdate), 'Q', QUARTER(birthdate))\n" +
                "  UNION ALL\n" +
                "  -- death dates grouped by months\n" +
                "  SELECT\n" +
                "    'deaths',\n" +
                "    'deaths',\n" +
                "    'deaths',\n" +
                "    'deaths',\n" +
                "    'deaths',\n" +
                "    'deaths',\n" +
                "    NULL,\n" +
                "    CONCAT(YEAR(death_date), 'Q', QUARTER(death_date)),\n" +
                "    GROUP_CONCAT(DISTINCT person_id ORDER BY person_id ASC),\n" +
                "    COUNT(DISTINCT person_id),\n" +
                "    'monthly',\n" +
                "    NOW(),\n" +
                "    'death_date'\n" +
                "  FROM person\n" +
                "  WHERE date_created > '1900-01-01' AND death_date IS NOT NULL\n" +
                "  GROUP BY CONCAT(YEAR(death_date), 'Q', QUARTER(death_date));";

        return sql.replace("1900-01-01", startDate);
    }

    public static int summarizeObs(String sql, Connection connection, String table) throws SQLException {
        createSummarizedObsTable(connection, table);
        executeQuery("SET SESSION group_concat_max_len = 10000000;", connection);
        return executeQuery(sql, connection);
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
        concepts.put("functional status", "dce09a15-30ab-102d-86b0-7a5022ba4115");
        concepts.put("inh dosage", "be211d29-1507-4e2e-9906-4bfeae4ddc1f");
        concepts.put("tb start date", "dce02eca-30ab-102d-86b0-7a5022ba4115");
        concepts.put("tb stop date", "dd2adde2-30ab-102d-86b0-7a5022ba4115");
        concepts.put("art start date", "ab505422-26d9-41f1-a079-c3d222000440");
        concepts.put("tb status", "dce02aa1-30ab-102d-86b0-7a5022ba4115");
        concepts.put("arv adh", "dce03b2f-30ab-102d-86b0-7a5022ba4115");
        concepts.put("cpt dosage", "38801143-01ac-4328-b0e1-a7b23c84c8a3");
        concepts.put("current regimen", "dd2b0b4d-30ab-102d-86b0-7a5022ba4115");
        concepts.put("return date", "dcac04cf-30ab-102d-86b0-7a5022ba4115");
        concepts.put("clinical stage", "dcdff274-30ab-102d-86b0-7a5022ba4115");
        concepts.put("baseline weight", "900b8fd9-2039-4efc-897b-9b8ce37396f5");
        concepts.put("baseline cs", "39243cef-b375-44b1-9e79-cbf21bd10878");
        concepts.put("baseline cd4", "c17bd9df-23e6-4e65-ba42-eb6d9250ca3f");
        concepts.put("baseline regimen", "c3332e8d-2548-4ad6-931d-6855692694a3");
        concepts.put("arv stop date", "cd36c403-d88c-4496-96e2-09af6da090c1");
        concepts.put("arv restart date", "406e1978-8c2e-40c5-b04e-ae214fdfed0e");
        concepts.put("to date", "fc1b1e96-4afb-423b-87e5-bb80d451c967");
        concepts.put("ti date", "f363f153-f659-438b-802f-9cc1828b5fa9");
        concepts.put("entry", "dcdfe3ce-30ab-102d-86b0-7a5022ba4115");
        concepts.put("deaths", "deaths");
        concepts.put("weight", "dce09e2f-30ab-102d-86b0-7a5022ba4115");
        concepts.put("cd4", "dcbcba2c-30ab-102d-86b0-7a5022ba4115");
        concepts.put("vl", "dc8d83e3-30ab-102d-86b0-7a5022ba4115");
        concepts.put("vl date", "0b434cfa-b11c-4d14-aaa2-9aed6ca2da88");
        concepts.put("vl qualitative", "dca12261-30ab-102d-86b0-7a5022ba4115");
        concepts.put("arvdays", "7593ede6-6574-4326-a8a6-3d742e843659");

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

    public static Map<String, String> preArtRegisterConcepts() {
        Map<String, String> concepts = new HashMap<>();
        concepts.put("tb start date", "dce02eca-30ab-102d-86b0-7a5022ba4115");
        concepts.put("tb stop date", "dd2adde2-30ab-102d-86b0-7a5022ba4115");
        concepts.put("art start date", "ab505422-26d9-41f1-a079-c3d222000440");
        concepts.put("tb status", "dce02aa1-30ab-102d-86b0-7a5022ba4115");
        concepts.put("cpt dosage", "38801143-01ac-4328-b0e1-a7b23c84c8a3");
        concepts.put("clinical stage", "dcdff274-30ab-102d-86b0-7a5022ba4115");
        concepts.put("baseline weight", "900b8fd9-2039-4efc-897b-9b8ce37396f5");
        concepts.put("baseline cs", "39243cef-b375-44b1-9e79-cbf21bd10878");
        concepts.put("baseline cd4", "c17bd9df-23e6-4e65-ba42-eb6d9250ca3f");
        concepts.put("baseline regimen", "c3332e8d-2548-4ad6-931d-6855692694a3");
        concepts.put("to date", "fc1b1e96-4afb-423b-87e5-bb80d451c967");
        concepts.put("ti date", "f363f153-f659-438b-802f-9cc1828b5fa9");
        concepts.put("entry", "dcdfe3ce-30ab-102d-86b0-7a5022ba4115");
        concepts.put("deaths", "deaths");
        concepts.put("weight", "dce09e2f-30ab-102d-86b0-7a5022ba4115");
        concepts.put("cd4", "dcbcba2c-30ab-102d-86b0-7a5022ba4115");
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

    public static Integer executeQuery(String query, Connection dbConn) throws SQLException {
        PreparedStatement stmt = dbConn.prepareStatement(query);
        return stmt.executeUpdate();
    }

    public static SummarizedObs viralLoad(List<SummarizedObs> vls, Integer no) {

        Map<String, List<SummarizedObs>> vlsGroupedByEncounterDate;
        if (vls != null && vls.size() > 0) {
            vlsGroupedByEncounterDate = vls.stream().collect(Collectors.groupingBy(SummarizedObs::getPeriod));
            List<String> keys = new ArrayList<>(vlsGroupedByEncounterDate.keySet());

            sort(keys);

            String k = "";

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
            if (StringUtils.isNotBlank(k)) {
                if (vlsGroupedByEncounterDate.get(k) != null && vlsGroupedByEncounterDate.get(k).size() > 0) {
                    return vlsGroupedByEncounterDate.get(k).get(0);
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


    public static Map<Integer, Map<String, List<NormalizedObs>>> getNormalizedObs(Connection connection, List<String> concepts, String patients, String encounterType) throws SQLException {
        String where = UgandaEMRReporting.joinQuery(UgandaEMRReporting.constructSQLInQuery("concept", concepts), "encounter_type = '" + encounterType + "'", Enums.UgandaEMRJoiner.AND);
        where = UgandaEMRReporting.joinQuery(where, UgandaEMRReporting.constructSQLInQuery("person_id", patients), Enums.UgandaEMRJoiner.AND);

        String normalizedSql = "select * from obs_normal where " + where;

        List<NormalizedObs> normalizedObs = getNormalizedObs(connection, normalizedSql);

        return normalizedObs.stream().collect(Collectors.groupingBy(NormalizedObs::getPersonId, Collectors.groupingBy(NormalizedObs::getConcept)));
    }

    public static Map<String, String> processString(String value) {
        Map<String, String> result = new HashMap<>();
        if (StringUtils.isNotBlank(value)) {
            List<String> splitData = Splitter.on(",").splitToList(value);

            for (String split : splitData) {
                List<String> keyValue = Splitter.on(":").splitToList(split);

                if (keyValue.size() == 2) {
                    result.put(keyValue.get(0), keyValue.get(1));
                }
            }
        }
        return result;
    }

    public static List<String> processString2(String value) {
        if (StringUtils.isNotBlank(value)) {
            List<String> splitData = Splitter.on(",").splitToList(value);
            return Splitter.on(":").splitToList(splitData.get(0));
        }
        return Arrays.asList("", "", "", "", "", "");
    }

    public static Connection testSqlConnection() throws SQLException, ClassNotFoundException {
        Properties props = new Properties();
        props.setProperty("driver.class", "com.mysql.jdbc.Driver");
        props.setProperty("driver.url", "jdbc:mysql://localhost:3306/ams");
        props.setProperty("user", "openmrs");
        props.setProperty("password", "openmrs");
        return getDatabaseConnection(props);
    }

    public static Connection sqlConnection() throws SQLException, ClassNotFoundException {

        Properties props = new Properties();
        props.setProperty("driver.class", "com.mysql.jdbc.Driver");
        props.setProperty("driver.url", Context.getRuntimeProperties().getProperty("connection.url"));
        props.setProperty("user", Context.getRuntimeProperties().getProperty("connection.username"));
        props.setProperty("password", Context.getRuntimeProperties().getProperty("connection.password"));
        return getDatabaseConnection(props);
    }

    public static void setGlobalProperty(String property, String propertyValue) {
        GlobalProperty globalProperty = new GlobalProperty();

        globalProperty.setProperty(property);
        globalProperty.setPropertyValue(propertyValue);

        Context.getAdministrationService().saveGlobalProperty(globalProperty);
    }

    public static String getGlobalProperty(String property) {
        return Context.getAdministrationService().getGlobalProperty(property);
    }

    public static List<SummarizedObs> getSummarizedObs(Connection connection, String period, String groupedBy, String concept, String table) throws SQLException {
        String sql = String.format("select * from %s where period = '%s' and period_grouped_by = '%s' and concept = '%s'", table, period, groupedBy, concept);
        return getSummarizedObs(connection, sql);

    }

    public static List<SummarizedObs> getSummarizedObs(Connection connection, String period, String groupedBy, String concept, String encounterType, String table) throws SQLException {
        String sql = String.format("select * from %s where period = '%s' and period_grouped_by = '%s' and concept = '%s' and encounter_type = '%s'", table, period, groupedBy, concept, encounterType);
        return getSummarizedObs(connection, sql);

    }

    public static List<SummarizedObs> getSummarizedObs(Connection connection, String table, List<String> concepts, List<String> patients) throws SQLException {
        String patientsToFind = Joiner.on("|").join(patients);
        // String where = joinQuery(String.format("CONCAT(',', patients, '','') REGEXP ',(%s),'", patientsToFind), constructSQLInQuery("concept", concepts), Enums.UgandaEMRJoiner.AND);
        String where = constructSQLInQuery("concept", concepts);
        String sql = String.format("select * from %s where " + where, table);

        return getSummarizedObs(connection, sql);
    }

    public static String summarizedObsPatientsToString(List<SummarizedObs> summarizedObs) {
        if (summarizedObs != null && summarizedObs.size() > 0) {
            StringBuilder patientString = new StringBuilder(summarizedObs.get(0).getPatients());

            for (SummarizedObs smo : summarizedObs.subList(1, summarizedObs.size())) {
                patientString.append(",").append(smo.getPatients());
            }
            return patientString.toString();
        }
        return "";
    }

    public static Map<String, String> summarizedObsPatientsToString2(List<SummarizedObs> summarizedObs) {
        Map<String, String> result = new HashMap<>();
        for (SummarizedObs smo : summarizedObs) {
            List<String> data = Splitter.on(",").splitToList(smo.getPatients());
            for (String d : data) {
                List<String> patientValues = Splitter.on(":").splitToList(d);
                result.put(patientValues.get(0), patientValues.get(1));
            }
        }
        return result;
    }

    public static List<SummarizedObs> getSummarizedObsList(List<SummarizedObs> obs, String concept, String period) {
        return new ArrayList<>(Collections2.filter(obs, new SummarizedObsPredicate(concept, period)));
    }

    public static SummarizedObs getSummarizedObs(List<SummarizedObs> obs, String period, String concept) {

        List<SummarizedObs> filtered = new ArrayList<>(Collections2.filter(obs, new SummarizedObsPredicate(concept, period)));

        if (filtered.size() > 0) {
            return filtered.get(0);
        }
        return null;
    }

    public static String getSummarizedObsValue(SummarizedObs summarizedObs, String patient) {
        if (summarizedObs != null) {
            String values = summarizedObs.getValueCoded();
            List<String> data = Splitter.on(",").splitToList(values);
            for (String d : data) {
                List<String> patientValues = Splitter.on(":").splitToList(d);
                if (patientValues.get(0).equals(patient)) {
                    return patientValues.get(1);
                }
            }
        }
        return "";
    }

    public static List<String> getSummarizedObsValues(SummarizedObs summarizedObs, String patient) {
        List<String> result = new ArrayList<>();
        if (summarizedObs != null) {
            String values = summarizedObs.getValueCoded();
            List<String> data = Splitter.on(",").splitToList(values);
            for (String d : data) {
                List<String> patientValues = Splitter.on(":").splitToList(d);
                if (patientValues.get(0).equals(patient)) {
                    result.add(patientValues.get(1));
                }
            }
        }
        sort(result);
        return result;
    }

    public static List<String> getSummarizedObsValues(List<SummarizedObs> summarizedObs, String patient) {
        List<String> result = new ArrayList<>();
        if (summarizedObs != null && summarizedObs.size() > 0) {
            for (SummarizedObs summarizedObs1 : summarizedObs) {
                String values = summarizedObs1.getValueCoded();
                List<String> data = Splitter.on(",").splitToList(values);
                for (String d : data) {
                    List<String> patientValues = Splitter.on(":").splitToList(d);
                    if (patientValues.get(0).equals(patient)) {
                        result.add(patientValues.get(1));
                    }
                }
            }
        }
        sort(result);
        return result;
    }

    public static Map<String, String> getSummarizedObsValuesAsMap(List<SummarizedObs> summarizedObs, String patient) {
        Map<String, String> result = new TreeMap<>();
        if (summarizedObs != null && summarizedObs.size() > 0) {
            for (SummarizedObs summarizedObs1 : summarizedObs) {
                String values = summarizedObs1.getValueCoded();
                List<String> data = Splitter.on(",").splitToList(values);
                for (String d : data) {
                    List<String> patientValues = Splitter.on(":").splitToList(d);
                    if (patientValues.get(0).equals(patient)) {
                        result.put(summarizedObs1.getPeriod(), patientValues.get(1));
                        break;
                    }
                }
            }
        }
        return result;
    }

    public static String getMostRecentValue(Map<String, String> dates, String period) {
        List<String> keys = new ArrayList<>(Collections2.filter(dates.keySet(), new SummarizedObsPeriodPredicate(period, ReportService.Modifier.LESS_EQUAL)));
        sort(keys);
        if (keys.size() > 0) {
            return dates.get(keys.get(keys.size() - 1));
        }
        return null;
    }

    public static Table<String, Integer, String> getDataTable(Connection connection, String sql) throws SQLException {

        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        Table<String, Integer, String> table = TreeBasedTable.create();
        while (rs.next()) {
            Integer patientId = rs.getInt(1);
            Integer conceptId = rs.getInt(2);
            Date encounterDate = rs.getDate(3);
            String val = rs.getString(4);
            String encounterMonth = DateUtil.formatDate(encounterDate, "yyyyMM") + String.valueOf(conceptId);
            table.put(encounterMonth, patientId, val);
        }
        rs.close();
        stmt.close();

        return table;
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

    public static String convert(String concept) {
        Map<String, String> result = new HashMap<>();

        result.put("90012", "eMTCT");
        result.put("90016", "TB");
        result.put("99593", "YCC");
        result.put("90019", "Outreach");
        result.put("90013", "Out Patient");
        result.put("90015", "STI");
        result.put("90018", "Inpatient");
        result.put("90002", "Other");
        result.put("90079", "1");
        result.put("90073", "2");
        result.put("90078", "3");
        result.put("90071", "4");

        return result.get(concept);
    }
}
