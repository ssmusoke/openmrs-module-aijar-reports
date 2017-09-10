package org.openmrs.module.ugandaemrreports.library;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.LocalDate;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reportingcompatibility.service.ReportService;
import org.openmrs.module.ugandaemrreports.common.*;
import org.openmrs.module.ugandaemrreports.definition.predicates.SummarizedObsPeriodPredicate;
import org.openmrs.module.ugandaemrreports.definition.predicates.SummarizedObsPredicate;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_FORWARD_ONLY;
import static java.util.Collections.sort;
import static java.util.Comparator.comparing;


/**
 * Created by carapai on 10/07/2017.
 */
public class UgandaEMRReporting {

    public static DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");

    public static IndexWriter createWriter(String indexDirectory) throws IOException {
        FSDirectory dir = FSDirectory.open(Paths.get(indexDirectory));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        return new IndexWriter(dir, config);
    }

    private static IndexSearcher createSearcher(String indexDirectory) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(indexDirectory));
        IndexReader reader = DirectoryReader.open(dir);
        return new IndexSearcher(reader);
    }

    public static List<SummarizedObs> getSummarizedObsData(String indexDirectory, String column, String search)
            throws IOException, ParseException {
        List<SummarizedObs> result = new ArrayList<>();

        try {
            IndexSearcher searcher = createSearcher(indexDirectory);
            QueryParser qp = new QueryParser(column, new StandardAnalyzer());
            int total = searcher.count(qp.parse(search));
            if (total > 0) {
                for (ScoreDoc sDoc : searcher.search(qp.parse(search), total).scoreDocs) {
                    result.add(convert(searcher.doc(sDoc.doc)));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static List<SummarizedObs> getSummarizedObs(Connection connection, String sql) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        List<SummarizedObs> summarizedObs = new ArrayList<>();
        while (rs.next()) {
            SummarizedObs obs = new SummarizedObs();
            obs.setConcept(rs.getString("concept"));
            obs.setEncounterType(rs.getString("encounter_type"));
            obs.setPatients(rs.getString("patients"));
            obs.setAgeGender(rs.getString("age_gender"));
            obs.setY(rs.getInt("y"));
            obs.setQ(rs.getInt("q"));
            obs.setM(rs.getInt("m"));
            obs.setVals(rs.getString("vals"));
            obs.setTotal(rs.getInt("total"));
            summarizedObs.add(obs);
        }
        rs.close();
        stmt.close();
        return summarizedObs;
    }


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

    public static SummarizedObs convert(Map<String, String> object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(object, SummarizedObs.class);
    }

    private static SummarizedObs convert(Document document) throws IOException {
        Map<String, String> data = new HashMap<>();
        for (String c : getSummarizedObsColumnMappings().values()) {
            data.put(c, document.get(c).equals("null") ? null : document.get(c));
        }
        return convert(data);
    }

    public static Map<String, String> convert(Document document, Collection<String> columns) {
        Map<String, String> data = new HashMap<>();
        for (String c : columns) {
            data.put(c, document.get(c).equals("null") ? null : document.get(c));
        }

        return data;
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

    public static void createSummarizedObsTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS obs_summary (\n" +
                "  encounter_type CHAR(38) NULL,\n" +
                "  y              INT(4)   NULL,\n" +
                "  q              INT(1)   NULL,\n" +
                "  m              INT(2)   NULL,\n" +
                "  concept        CHAR(38) NULL,\n" +
                "  vals           LONGTEXT NULL,\n" +
                "  patients       LONGTEXT NULL,\n" +
                "  age_gender     LONGTEXT NULL,\n" +
                "  total          INT(7)   NULL\n" +
                ");";

        executeQuery(sql, connection);
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

    public static Multimap<Integer, Date> getData(String data) throws SQLException {


        Multimap<Integer, Date> result = TreeMultimap.create();

        List<String> patientData = Splitter.on(",").splitToList(data);
        for (String d : patientData) {
            List<String> gotData = Splitter.on(":").splitToList(d);

            result.put(Integer.valueOf(gotData.get(0)), DateUtil.parseYmd(gotData.get(1)));
        }
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

    public static List<ObsData> getDataAsList(List<ObsData> data, List<String> concept) {
        return data.stream()
                .filter(line -> concept.contains(line.getConceptId()))
                .collect(Collectors.toList());
    }

    public static ObsData getLastAppointments(List<ObsData> data, String yearMonth) {

        List<ObsData> filteredData = getDataAsList(data, "5096").stream()
                .filter(line -> getObsPeriod(DateUtil.parseYmd(line.getVal()),
                        Enums.Period.MONTHLY).compareTo(yearMonth) < 0).collect(Collectors.toList());

        return filteredData.stream().max(comparing(ObsData::getVal)).get();

    }

    public static void lucenizeSummarizedObs(IndexWriter writer, String startDate, Connection connection) throws SQLException, IOException {

        String query = "SELECT\n" +
                "  IFNULL((SELECT et.uuid\n" +
                "          FROM encounter_type AS et\n" +
                "          WHERE et.encounter_type_id =\n" +
                "                (SELECT e.encounter_type\n" +
                "                 FROM encounter AS e\n" +
                "                 WHERE e.encounter_id = o.encounter_id)), 'ANY') AS encounter_type,\n" +
                "  YEAR(obs_datetime)                                             AS y,\n" +
                "  QUARTER(obs_datetime)                                          AS q,\n" +
                "  MONTH(obs_datetime)                                            AS m,\n" +
                "  concept_id                                                     AS concept,\n" +
                "  value_coded                                                    AS vals,\n" +
                "  group_concat(DISTINCT person_id ORDER BY person_id\n" +
                "               ASC)                                              AS patients,\n" +
                "  group_concat(concat_ws(':', person_id, (SELECT p.gender\n" +
                "                                          FROM person p\n" +
                "                                          WHERE p.person_id = o.person_id),\n" +
                "                         (SELECT YEAR(o.obs_datetime) - YEAR(birthdate)\n" +
                "                                 - (RIGHT(o.obs_datetime, 5) <\n" +
                "                                    RIGHT(birthdate, 5))\n" +
                "                          FROM person p\n" +
                "                          WHERE p.person_id = o.person_id)) ORDER\n" +
                "               BY person_id SEPARATOR\n" +
                "               ',')                                              AS age_gender,\n" +
                "  COUNT(DISTINCT\n" +
                "        person_id)                                               AS total\n" +
                "FROM obs o\n" +
                "WHERE value_coded IS NOT NULL AND voided = 0 AND date_created > '1900-01-01'\n" +
                "GROUP BY encounter_type, concept, value_coded, y, q, m\n" +
                "UNION ALL\n" +
                "SELECT\n" +
                "  IFNULL((SELECT et.uuid\n" +
                "          FROM encounter_type AS et\n" +
                "          WHERE et.encounter_type_id =\n" +
                "                (SELECT e.encounter_type\n" +
                "                 FROM encounter AS e\n" +
                "                 WHERE e.encounter_id = o.encounter_id)), 'ANY')                          AS encounter_type,\n" +
                "  YEAR(obs_datetime)                                                                      AS y,\n" +
                "  QUARTER(obs_datetime)                                                                   AS q,\n" +
                "  MONTH(obs_datetime)                                                                     AS m,\n" +
                "  concept_id                                                                              AS concept,\n" +
                "  group_concat(concat_ws(':', person_id, value_numeric) ORDER BY person_id SEPARATOR ',') AS vals,\n" +
                "  group_concat(DISTINCT person_id ORDER BY person_id ASC)                                 AS patients,\n" +
                "  group_concat(concat_ws(':', person_id, (SELECT p.gender\n" +
                "                                          FROM person p\n" +
                "                                          WHERE p.person_id = o.person_id),\n" +
                "                         (SELECT YEAR(o.obs_datetime) - YEAR(birthdate)\n" +
                "                                 - (RIGHT(o.obs_datetime, 5) <\n" +
                "                                    RIGHT(birthdate, 5))\n" +
                "                          FROM person p\n" +
                "                          WHERE p.person_id = o.person_id)) ORDER\n" +
                "               BY person_id SEPARATOR\n" +
                "               ',')                                                                       AS age_gender,\n" +
                "  COUNT(DISTINCT\n" +
                "        person_id)                                                                        AS total\n" +
                "FROM obs o\n" +
                "WHERE value_numeric IS NOT NULL AND voided = 0 AND date_created > '1900-01-01'\n" +
                "GROUP BY encounter_type, concept, y, q, m\n" +
                "UNION ALL\n" +
                "SELECT\n" +
                "  IFNULL((SELECT et.uuid\n" +
                "          FROM encounter_type AS et\n" +
                "          WHERE et.encounter_type_id =\n" +
                "                (SELECT e.encounter_type\n" +
                "                 FROM encounter AS e\n" +
                "                 WHERE e.encounter_id = o.encounter_id)), 'ANY')                         AS encounter_type,\n" +
                "  YEAR(obs_datetime)                                                                     AS y,\n" +
                "  QUARTER(obs_datetime)                                                                  AS q,\n" +
                "  MONTH(obs_datetime)                                                                    AS m,\n" +
                "  concept_id                                                                             AS concept,\n" +
                "  group_concat(concat_ws('::', person_id, value_text) ORDER BY person_id SEPARATOR ',,') AS vals,\n" +
                "  group_concat(DISTINCT person_id ORDER BY person_id ASC)                                AS patients,\n" +
                "  group_concat(concat_ws(':', person_id, (SELECT p.gender\n" +
                "                                          FROM person p\n" +
                "                                          WHERE p.person_id = o.person_id),\n" +
                "                         (SELECT YEAR(o.obs_datetime) - YEAR(birthdate)\n" +
                "                                 - (RIGHT(o.obs_datetime, 5) <\n" +
                "                                    RIGHT(birthdate, 5))\n" +
                "                          FROM person p\n" +
                "                          WHERE p.person_id = o.person_id)) ORDER\n" +
                "               BY person_id SEPARATOR\n" +
                "               ',')                                                                      AS age_gender,\n" +
                "  COUNT(DISTINCT person_id)                                                              AS total\n" +
                "FROM obs o\n" +
                "WHERE value_text IS NOT NULL AND voided = 0 AND date_created > '1900-01-01'\n" +
                "GROUP BY encounter_type, concept, y, q, m\n" +
                "UNION ALL\n" +
                "SELECT\n" +
                "  IFNULL((SELECT et.uuid\n" +
                "          FROM encounter_type AS et\n" +
                "          WHERE et.encounter_type_id =\n" +
                "                (SELECT e.encounter_type\n" +
                "                 FROM encounter AS e\n" +
                "                 WHERE e.encounter_id = o.encounter_id)), 'ANY')                                 AS encounter_type,\n" +
                "  YEAR(value_datetime)                                                                           AS y,\n" +
                "  QUARTER(value_datetime)                                                                        AS q,\n" +
                "  MONTH(value_datetime)                                                                          AS m,\n" +
                "  concept_id                                                                                     AS concept,\n" +
                "  group_concat(concat_ws(':', person_id, DATE(value_datetime)) ORDER BY person_id SEPARATOR ',') AS vals,\n" +
                "  group_concat(DISTINCT person_id ORDER BY person_id ASC)                                        AS patients,\n" +
                "  group_concat(concat_ws(':', person_id, (SELECT p.gender\n" +
                "                                          FROM person p\n" +
                "                                          WHERE p.person_id = o.person_id),\n" +
                "                         (SELECT YEAR(o.value_datetime) - YEAR(birthdate)\n" +
                "                                 - (RIGHT(o.value_datetime, 5) <\n" +
                "                                    RIGHT(birthdate, 5))\n" +
                "                          FROM person p\n" +
                "                          WHERE p.person_id = o.person_id)) ORDER\n" +
                "               BY person_id SEPARATOR\n" +
                "               ',')                                                                              AS age_gender,\n" +
                "  COUNT(DISTINCT person_id)                                                                      AS total\n" +
                "FROM obs o\n" +
                "WHERE value_datetime IS NOT NULL AND voided = 0 AND date_created > '1900-01-01'\n" +
                "GROUP BY encounter_type, concept, y, q, m\n" +
                "UNION ALL\n" +
                "SELECT\n" +
                "  IFNULL((SELECT et.uuid\n" +
                "          FROM encounter_type AS et\n" +
                "          WHERE et.encounter_type_id = e.encounter_type), 'ANY') AS encounter_type,\n" +
                "  YEAR(e.encounter_datetime)                                     AS y,\n" +
                "  QUARTER(e.encounter_datetime)                                  AS q,\n" +
                "  MONTH(e.encounter_datetime)                                    AS m,\n" +
                "  'encounter'                                                    AS concept,\n" +
                "  'encounter'                                                    AS vals,\n" +
                "  group_concat(DISTINCT patient_id ORDER BY patient_id ASC)      AS patients,\n" +
                "  group_concat(concat_ws(':', patient_id, (SELECT p.gender\n" +
                "                                           FROM person p\n" +
                "                                           WHERE p.person_id = e.patient_id),\n" +
                "                         (SELECT YEAR(e.encounter_datetime) - YEAR(birthdate)\n" +
                "                                 - (RIGHT(e.encounter_datetime, 5) <\n" +
                "                                    RIGHT(birthdate, 5))\n" +
                "                          FROM person p\n" +
                "                          WHERE p.person_id = e.patient_id)) ORDER\n" +
                "               BY patient_id SEPARATOR\n" +
                "               ',')                                              AS age_gender,\n" +
                "  COUNT(DISTINCT patient_id)                                     AS total\n" +
                "FROM encounter e\n" +
                "WHERE voided = 0 AND date_created > '1900-01-01'\n" +
                "GROUP BY encounter_type, y, q, m\n" +
                "UNION ALL\n" +
                "SELECT\n" +
                "  'births'                                                                                  AS encounter_type,\n" +
                "  YEAR(birthdate)                                                                           AS y,\n" +
                "  QUARTER(birthdate)                                                                        AS q,\n" +
                "  MONTH(birthdate)                                                                          AS m,\n" +
                "  'births'                                                                                  AS concept,\n" +
                "  group_concat(concat_ws(':', person_id, DATE(birthdate)) ORDER BY person_id SEPARATOR ',') AS vals,\n" +
                "  GROUP_CONCAT(DISTINCT person_id ORDER BY person_id ASC)                                   AS patients,\n" +
                "  group_concat(concat_ws(':', person_id, gender) ORDER BY person_id SEPARATOR ',')          AS age_gender,\n" +
                "  COUNT(DISTINCT person_id)                                                                 AS total\n" +
                "FROM person\n" +
                "WHERE date_created > '1900-01-01' AND birthdate IS NOT NULL\n" +
                "GROUP BY y, q, m\n" +
                "UNION ALL\n" +
                "SELECT\n" +
                "  'deaths'                                                                                   AS encounter_type,\n" +
                "  YEAR(death_date)                                                                           AS y,\n" +
                "  QUARTER(death_date)                                                                        AS q,\n" +
                "  MONTH(death_date)                                                                          AS m,\n" +
                "  'deaths'                                                                                   AS concept,\n" +
                "  group_concat(concat_ws(':', person_id, DATE(death_date)) ORDER BY person_id SEPARATOR ',') AS vals,\n" +
                "  GROUP_CONCAT(DISTINCT person_id ORDER BY person_id ASC)                                    AS patients,\n" +
                "  group_concat(concat_ws(':', person_id, gender,\n" +
                "                         YEAR(death_date) - YEAR(birthdate) - (RIGHT(death_date, 5) < RIGHT(birthdate, 5))) ORDER BY\n" +
                "               person_id SEPARATOR ',')                                                      AS age_gender,\n" +
                "  COUNT(DISTINCT person_id)                                                                  AS total\n" +
                "FROM person\n" +
                "WHERE date_created > '1900-01-01' AND death_date IS NOT NULL\n" +
                "GROUP BY y, q, m;";

        query = query.replaceAll("1900-01-01", startDate);

        executeQuery("SET @@group_concat_max_len = 1000000;", connection);


        List<String> columns = Arrays.asList("encounterType",
                "period",
                "concept",
                "vals",
                "patients",
                "total");
        Statement stmt = connection.createStatement(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY);
        stmt.setFetchSize(Integer.MIN_VALUE);

        ResultSet rs = stmt.executeQuery(query);

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        FieldType titleType = new FieldType();
        titleType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        titleType.setStored(true);
        titleType.setTokenized(true);

        while (rs.next()) {
            Document document = new Document();
            for (int i = 1; i <= columnCount; i++) {
                String column = columns.get(i - 1);
                String value = rs.getString(i);
                Field titleField1 = new Field(column, value, titleType);
                document.add(titleField1);
            }
            writer.addDocument(document);
        }
        rs.close();
        stmt.close();
    }

    public static void summarizeObs(String startDate, Connection connection) throws SQLException {
        createSummarizedObsTable(connection);
        String query = "INSERT INTO obs_summary (encounter_type, y, q, m, concept, vals, patients, age_gender, total)\n" +
                "  SELECT\n" +
                "    IFNULL((SELECT et.uuid\n" +
                "            FROM encounter_type AS et\n" +
                "            WHERE et.encounter_type_id =\n" +
                "                  (SELECT e.encounter_type\n" +
                "                   FROM encounter AS e\n" +
                "                   WHERE e.encounter_id = o.encounter_id)), 'ANY') AS encounter_type,\n" +
                "    YEAR(obs_datetime)                                             AS y,\n" +
                "    QUARTER(obs_datetime)                                          AS q,\n" +
                "    MONTH(obs_datetime)                                            AS m,\n" +
                "    concept_id                                                     AS concept,\n" +
                "    value_coded                                                    AS vals,\n" +
                "    group_concat(DISTINCT person_id ORDER BY person_id\n" +
                "                 ASC)                                              AS patients,\n" +
                "    group_concat(concat_ws(':', person_id, (SELECT p.gender\n" +
                "                                            FROM person p\n" +
                "                                            WHERE p.person_id = o.person_id),\n" +
                "                           (SELECT YEAR(o.obs_datetime) - YEAR(birthdate)\n" +
                "                                   - (RIGHT(o.obs_datetime, 5) <\n" +
                "                                      RIGHT(birthdate, 5))\n" +
                "                            FROM person p\n" +
                "                            WHERE p.person_id = o.person_id)) ORDER\n" +
                "                 BY person_id SEPARATOR\n" +
                "                 ',')                                              AS age_gender,\n" +
                "    COUNT(DISTINCT\n" +
                "          person_id)                                               AS total\n" +
                "  FROM obs o\n" +
                "  WHERE value_coded IS NOT NULL AND voided = 0 AND date_created > '1900-01-01'\n" +
                "  GROUP BY encounter_type, concept, value_coded, y, q, m\n" +
                "  UNION ALL\n" +
                "  SELECT\n" +
                "    IFNULL((SELECT et.uuid\n" +
                "            FROM encounter_type AS et\n" +
                "            WHERE et.encounter_type_id =\n" +
                "                  (SELECT e.encounter_type\n" +
                "                   FROM encounter AS e\n" +
                "                   WHERE e.encounter_id = o.encounter_id)), 'ANY')                          AS encounter_type,\n" +
                "    YEAR(obs_datetime)                                                                      AS y,\n" +
                "    QUARTER(obs_datetime)                                                                   AS q,\n" +
                "    MONTH(obs_datetime)                                                                     AS m,\n" +
                "    concept_id                                                                              AS concept,\n" +
                "    group_concat(concat_ws(':', person_id, value_numeric) ORDER BY person_id SEPARATOR ',') AS vals,\n" +
                "    group_concat(DISTINCT person_id ORDER BY person_id ASC)                                 AS patients,\n" +
                "    group_concat(concat_ws(':', person_id, (SELECT p.gender\n" +
                "                                            FROM person p\n" +
                "                                            WHERE p.person_id = o.person_id),\n" +
                "                           (SELECT YEAR(o.obs_datetime) - YEAR(birthdate)\n" +
                "                                   - (RIGHT(o.obs_datetime, 5) <\n" +
                "                                      RIGHT(birthdate, 5))\n" +
                "                            FROM person p\n" +
                "                            WHERE p.person_id = o.person_id)) ORDER\n" +
                "                 BY person_id SEPARATOR\n" +
                "                 ',')                                                                       AS age_gender,\n" +
                "    COUNT(DISTINCT\n" +
                "          person_id)                                                                        AS total\n" +
                "  FROM obs o\n" +
                "  WHERE value_numeric IS NOT NULL AND voided = 0 AND date_created > '1900-01-01'\n" +
                "  GROUP BY encounter_type, concept, y, q, m\n" +
                "  UNION ALL\n" +
                "  SELECT\n" +
                "    IFNULL((SELECT et.uuid\n" +
                "            FROM encounter_type AS et\n" +
                "            WHERE et.encounter_type_id =\n" +
                "                  (SELECT e.encounter_type\n" +
                "                   FROM encounter AS e\n" +
                "                   WHERE e.encounter_id = o.encounter_id)), 'ANY')                         AS encounter_type,\n" +
                "    YEAR(obs_datetime)                                                                     AS y,\n" +
                "    QUARTER(obs_datetime)                                                                  AS q,\n" +
                "    MONTH(obs_datetime)                                                                    AS m,\n" +
                "    concept_id                                                                             AS concept,\n" +
                "    group_concat(concat_ws('::', person_id, value_text) ORDER BY person_id SEPARATOR ',,') AS vals,\n" +
                "    group_concat(DISTINCT person_id ORDER BY person_id ASC)                                AS patients,\n" +
                "    group_concat(concat_ws(':', person_id, (SELECT p.gender\n" +
                "                                            FROM person p\n" +
                "                                            WHERE p.person_id = o.person_id),\n" +
                "                           (SELECT YEAR(o.obs_datetime) - YEAR(birthdate)\n" +
                "                                   - (RIGHT(o.obs_datetime, 5) <\n" +
                "                                      RIGHT(birthdate, 5))\n" +
                "                            FROM person p\n" +
                "                            WHERE p.person_id = o.person_id)) ORDER\n" +
                "                 BY person_id SEPARATOR\n" +
                "                 ',')                                                                      AS age_gender,\n" +
                "    COUNT(DISTINCT person_id)                                                              AS total\n" +
                "  FROM obs o\n" +
                "  WHERE value_text IS NOT NULL AND voided = 0 AND date_created > '1900-01-01'\n" +
                "  GROUP BY encounter_type, concept, y, q, m\n" +
                "  UNION ALL\n" +
                "  SELECT\n" +
                "    IFNULL((SELECT et.uuid\n" +
                "            FROM encounter_type AS et\n" +
                "            WHERE et.encounter_type_id =\n" +
                "                  (SELECT e.encounter_type\n" +
                "                   FROM encounter AS e\n" +
                "                   WHERE e.encounter_id = o.encounter_id)), 'ANY')                                 AS encounter_type,\n" +
                "    YEAR(value_datetime)                                                                           AS y,\n" +
                "    QUARTER(value_datetime)                                                                        AS q,\n" +
                "    MONTH(value_datetime)                                                                          AS m,\n" +
                "    concept_id                                                                                     AS concept,\n" +
                "    group_concat(concat_ws(':', person_id, DATE(value_datetime)) ORDER BY person_id SEPARATOR ',') AS vals,\n" +
                "    group_concat(DISTINCT person_id ORDER BY person_id ASC)                                        AS patients,\n" +
                "    group_concat(concat_ws(':', person_id, (SELECT p.gender\n" +
                "                                            FROM person p\n" +
                "                                            WHERE p.person_id = o.person_id),\n" +
                "                           (SELECT YEAR(o.value_datetime) - YEAR(birthdate)\n" +
                "                                   - (RIGHT(o.value_datetime, 5) <\n" +
                "                                      RIGHT(birthdate, 5))\n" +
                "                            FROM person p\n" +
                "                            WHERE p.person_id = o.person_id)) ORDER\n" +
                "                 BY person_id SEPARATOR\n" +
                "                 ',')                                                                              AS age_gender,\n" +
                "    COUNT(DISTINCT person_id)                                                                      AS total\n" +
                "  FROM obs o\n" +
                "  WHERE value_datetime IS NOT NULL AND voided = 0 AND date_created > '1900-01-01'\n" +
                "  GROUP BY encounter_type, concept, y, q, m\n" +
                "  UNION ALL\n" +
                "  SELECT\n" +
                "    IFNULL((SELECT et.uuid\n" +
                "            FROM encounter_type AS et\n" +
                "            WHERE et.encounter_type_id = e.encounter_type), 'ANY') AS encounter_type,\n" +
                "    YEAR(e.encounter_datetime)                                     AS y,\n" +
                "    QUARTER(e.encounter_datetime)                                  AS q,\n" +
                "    MONTH(e.encounter_datetime)                                    AS m,\n" +
                "    'encounter'                                                    AS concept,\n" +
                "    'encounter'                                                    AS vals,\n" +
                "    group_concat(DISTINCT patient_id ORDER BY patient_id ASC)      AS patients,\n" +
                "    group_concat(concat_ws(':', patient_id, (SELECT p.gender\n" +
                "                                             FROM person p\n" +
                "                                             WHERE p.person_id = e.patient_id),\n" +
                "                           (SELECT YEAR(e.encounter_datetime) - YEAR(birthdate)\n" +
                "                                   - (RIGHT(e.encounter_datetime, 5) <\n" +
                "                                      RIGHT(birthdate, 5))\n" +
                "                            FROM person p\n" +
                "                            WHERE p.person_id = e.patient_id)) ORDER\n" +
                "                 BY patient_id SEPARATOR\n" +
                "                 ',')                                              AS age_gender,\n" +
                "    COUNT(DISTINCT patient_id)                                     AS total\n" +
                "  FROM encounter e\n" +
                "  WHERE voided = 0 AND date_created > '1900-01-01'\n" +
                "  GROUP BY encounter_type, y, q, m\n" +
                "  UNION ALL\n" +
                "  SELECT\n" +
                "    'births'                                                                                  AS encounter_type,\n" +
                "    YEAR(birthdate)                                                                           AS y,\n" +
                "    QUARTER(birthdate)                                                                        AS q,\n" +
                "    MONTH(birthdate)                                                                          AS m,\n" +
                "    'births'                                                                                  AS concept,\n" +
                "    group_concat(concat_ws(':', person_id, DATE(birthdate)) ORDER BY person_id SEPARATOR ',') AS vals,\n" +
                "    GROUP_CONCAT(DISTINCT person_id ORDER BY person_id ASC)                                   AS patients,\n" +
                "    group_concat(concat_ws(':', person_id, gender) ORDER BY person_id SEPARATOR ',')          AS age_gender,\n" +
                "    COUNT(DISTINCT person_id)                                                                 AS total\n" +
                "  FROM person\n" +
                "  WHERE date_created > '1900-01-01' AND birthdate IS NOT NULL\n" +
                "  GROUP BY y, q, m\n" +
                "  UNION ALL\n" +
                "  SELECT\n" +
                "    'deaths'                                                                                   AS encounter_type,\n" +
                "    YEAR(death_date)                                                                           AS y,\n" +
                "    QUARTER(death_date)                                                                        AS q,\n" +
                "    MONTH(death_date)                                                                          AS m,\n" +
                "    'deaths'                                                                                   AS concept,\n" +
                "    group_concat(concat_ws(':', person_id, DATE(death_date)) ORDER BY person_id SEPARATOR ',') AS vals,\n" +
                "    GROUP_CONCAT(DISTINCT person_id ORDER BY person_id ASC)                                    AS patients,\n" +
                "    group_concat(concat_ws(':', person_id, gender,\n" +
                "                           YEAR(death_date) - YEAR(birthdate) - (RIGHT(death_date, 5) < RIGHT(birthdate, 5))) ORDER BY\n" +
                "                 person_id SEPARATOR ',')                                                      AS age_gender,\n" +
                "    COUNT(DISTINCT person_id)                                                                  AS total\n" +
                "  FROM person\n" +
                "  WHERE date_created > '1900-01-01' AND death_date IS NOT NULL\n" +
                "  GROUP BY y, q, m;";
        query = query.replaceAll("1900-01-01", startDate);
        executeQuery("SET @@group_concat_max_len = 1000000;", connection);
        executeQuery(query, connection);
    }


    public static String summarizeObs() {
        try {
            Connection connection = UgandaEMRReporting.sqlConnection();
            String lastSummarizationDate = UgandaEMRReporting.getGlobalProperty("ugandaemrreports.lastSummarizationDate");

            if (org.apache.commons.lang.StringUtils.isBlank(lastSummarizationDate)) {
                lastSummarizationDate = "1900-01-01 00:00:00";
            }
            summarizeObs(lastSummarizationDate, connection);

            Date now = new Date();
            String newDate = UgandaEMRReporting.DEFAULT_DATE_FORMAT.format(now);

            UgandaEMRReporting.setGlobalProperty("ugandaemrreports.lastSummarizationDate", newDate);
            return "Obs have been successfully summarized";
        } catch (Exception e) {
            return "Error occurred";
        }

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

    public static Map<String, String> getSummarizedObsColumnMappings() {
        Map<String, String> colMaps = new HashMap<String, String>();
        colMaps.put("encounter_type", "encounterType");
        colMaps.put("period", "period");
        colMaps.put("concept", "concept");
        colMaps.put("vals", "vals");
        colMaps.put("patients", "patients");
        colMaps.put("total", "total");
        return colMaps;

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
        Map<String, String> result = new HashMap<>();

        List<String> splitData = Splitter.on(",").splitToList(value);

        return Splitter.on(":").splitToList(splitData.get(0));
    }

    public static Connection testSqlConnection() throws SQLException, ClassNotFoundException {
        Properties props = new Properties();
        props.setProperty("driver.class", "com.mysql.jdbc.Driver");
        props.setProperty("driver.url", "jdbc:mysql://localhost:3306/openmrs");
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

    public static List<SummarizedObs> getSummarizedObs(Connection connection, Map<String, String> where) throws SQLException {
        String whereString = Joiner.on(" AND ").withKeyValueSeparator(" = ").join(where);

        String sql = "select * from obs_summary where " + whereString;
        return getSummarizedObs(connection, sql);

    }

    public static List<SummarizedObs> getSummarizedObs(Connection connection, List<String> concepts, List<String> patients) throws SQLException {
        String patientsToFind = Joiner.on("|").join(patients);
        // String where = joinQuery(String.format("CONCAT(',', patients, '','') REGEXP ',(%s),'", patientsToFind), constructSQLInQuery("concept", concepts), Enums.UgandaEMRJoiner.AND);
        String where = constructSQLInQuery("concept", concepts);
        String sql = "select * from obs_summary where " + where;

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
            String values = summarizedObs.getVals();
            List<String> data = Splitter.on(",,").splitToList(values);
            for (String d : data) {
                List<String> patientValues = Splitter.on("::").splitToList(d);
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
            String values = summarizedObs.getVals();
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
                String values = summarizedObs1.getVals();
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
                String values = summarizedObs1.getVals();
                List<String> data = Splitter.on(",,").splitToList(values);
                for (String d : data) {
                    List<String> patientValues = Splitter.on("::").splitToList(d);
                    if (patientValues.get(0).equals(patient)) {
                        result.put(summarizedObs1.getPatients(), patientValues.get(1));
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

    public static String constructLuceneBetweenQuery(String column, String value1, String value2) {
        return column + ":[\"" + value1 + "\" TO \"" + value2 + "\"]";
    }

    public static String constructLuceneGreaterThanQuery(String column, String value) {
        return column + ":[*" + " TO \"" + value + "\"]";
    }

    public static String constructLuceneLessThanQuery(String column, String value) {
        return column + ":[\"" + value + "\" TO *]";
    }

    public static String constructLuceneQuery(String column, String value) {
        return column + ":\"" + value + "\"";
    }

    public static String constructLuceneInQuery(String column, List<String> values) {
        column += ":(\"" + values.get(0) + "\" ";
        StringBuilder columnBuilder = new StringBuilder(column);
        for (String value : values.subList(1, values.size())) {
            columnBuilder.append("\"").append(value).append("\"");
        }
        column = columnBuilder.toString();
        return column + ")";
    }
}
