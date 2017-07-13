package org.openmrs.module.ugandaemrreports.lucene;

import com.google.common.base.Joiner;
import com.google.common.collect.Table;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.module.ugandaemrreports.common.NormalizedObs;
import org.sql2o.Connection;
import org.sql2o.ResultSetIterable;
import org.sql2o.Sql2o;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by carapai on 10/07/2017.
 */
public class UgandaEMRLucene {
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

    public static Document createDoc(NormalizedObs doc) {
        FieldType titleType = new FieldType();
        titleType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        titleType.setStored(true);
        titleType.setTokenized(true);

        Document doc1 = new Document();

        ObjectMapper oMapper = new ObjectMapper();

        Map<String, Object> map = oMapper.convertValue(doc, Map.class);

        for (Map.Entry<String, Object> column : map.entrySet()) {
            Field titleField1 = new Field(column.getKey(), String.valueOf(column.getValue()), titleType);
            doc1.add(titleField1);
        }
        return doc1;
    }

    public static List<Map<String, String>> getData(String indexDirectory, String column, String search, Collection<String> columns) throws IOException, ParseException {
        List<Map<String, String>> result = new ArrayList<>();
        try {
            IndexSearcher searcher = createSearcher(indexDirectory);
            QueryParser qp = new QueryParser(column, new StandardAnalyzer());
            int total = searcher.count(qp.parse(search));

            if (total > 0) {
                ScoreDoc[] sDocs = searcher.search(qp.parse(search), total).scoreDocs;
                for (ScoreDoc sd : sDocs) {
                    result.add(convert(searcher.doc(sd.doc), columns));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static List<NormalizedObs> getData(String indexDirectory, String column, String search) throws IOException, ParseException {
        List<NormalizedObs> result = new ArrayList<>();

        try {
            IndexSearcher searcher = createSearcher(indexDirectory);
            QueryParser qp = new QueryParser(column, new StandardAnalyzer());
            int total = searcher.count(qp.parse(search));
            if (total > 0) {
                ScoreDoc[] sDocs = searcher.search(qp.parse(search), total).scoreDocs;
                for (ScoreDoc sd : sDocs) {
                    result.add(convert(searcher.doc(sd.doc)));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static NormalizedObs convert(Map<String, String> object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(object, NormalizedObs.class);
    }

    private static NormalizedObs convert(Document document) throws IOException {
        Map<String, String> data = new HashMap<>();
        for (String c : getColumnMappings().values()) {
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

    public static Map<String, String> getColumnMappings() {
        Map<String, String> colMaps = new HashMap<String, String>();

        colMaps.put("person_id", "personId");
        colMaps.put("person", "person");
        colMaps.put("birth_date", "birthDate");
        colMaps.put("birth_date_estimated", "birthDateEstimated");
        colMaps.put("identifiers", "identifiers");
        colMaps.put("attributes", "attributes");
        colMaps.put("names", "names");
        colMaps.put("addresses", "addresses");
        colMaps.put("encounter_type", "encounterType");
        colMaps.put("encounter_type_name", "encounterTypeName");
        colMaps.put("form", "form");
        colMaps.put("form_name", "formName");
        colMaps.put("encounter", "encounter");
        colMaps.put("encounter_datetime", "encounterDatetime");
        colMaps.put("age_at_encounter", "ageAtEncounter");
        colMaps.put("encounter_year", "encounterYear");
        colMaps.put("encounter_month", "encounterMonth");
        colMaps.put("encounter_quarter", "encounterQuarter");
        colMaps.put("concept", "concept");
        colMaps.put("concept_name", "conceptName");
        colMaps.put("`order`", "order");
        colMaps.put("obs_datetime", "obsDatetime");
        colMaps.put("obs_datetime_year", "obsDatetimeYear");
        colMaps.put("obs_datetime_month", "obsDatetimeMonth");
        colMaps.put("obs_datetime_quarter", "obsDatetimeQuarter");
        colMaps.put("age_at_observation", "ageAtObservation");
        colMaps.put("location", "location");
        colMaps.put("location_name", "locationName");
        colMaps.put("obs_group", "obsGroup");
        colMaps.put("accession_number", "accessionNumber");
        colMaps.put("value_group", "valueGroup");
        colMaps.put("value_group", "valueGroup");
        colMaps.put("value_boolean", "valueBoolean");
        colMaps.put("value_coded", "valueCoded");
        colMaps.put("value_coded_name1", "valueCodedName1");
        colMaps.put("report_name", "reportName");
        colMaps.put("value_coded_name", "valueCodedName");
        colMaps.put("value_drug", "valueDrug");
        colMaps.put("value_datetime", "valueDatetime");
        colMaps.put("age_at_value_datetime", "ageAtValueDatetime");
        colMaps.put("value_datetime_year", "valueDatetimeYear");
        colMaps.put("value_datetime_month", "valueDatetimeMonth");
        colMaps.put("value_datetime_quarter", "valueDatetimeQuarter");
        colMaps.put("value_numeric", "valueNumeric");
        colMaps.put("value_modifier", "valueModifier");
        colMaps.put("value_text", "valueText");
        colMaps.put("value_complex", "valueComplex");
        colMaps.put("comments", "comments");
        colMaps.put("creator", "creator");
        colMaps.put("date_created", "dateCreated");
        colMaps.put("voided", "voided");
        colMaps.put("voided_by", "voidedBy");
        colMaps.put("date_voided", "dateVoided");
        colMaps.put("void_reason", "voidReason");
        colMaps.put("uuid", "uuid");
        colMaps.put("previous_version", "previousVersion");
        colMaps.put("form_namespace_and_path", "formNamespaceAndPath");
        colMaps.put("status", "status");
        return colMaps;

    }

    public static String constructInQuery(String column, List<String> values) {
        column += ":(\"" + values.get(0) + "\" ";
        StringBuilder columnBuilder = new StringBuilder(column);
        for (String value : values.subList(1, values.size())) {
            columnBuilder.append("\"").append(value).append("\"");
        }
        column = columnBuilder.toString();
        return column + ")";
    }

    public static void createNormalizedObsTable(Sql2o sql2o) {
        String sql = "CREATE TABLE IF NOT EXISTS obs_normal\n" +
                "(\n" +
                "  person_id               INT          NOT NULL,\n" +
                "  person                  CHAR(38)     NOT NULL,\n" +
                "  birth_date              DATE         NULL,\n" +
                "  birth_date_estimated    TINYINT      NULL,\n" +
                "  identifiers             TEXT         NULL,\n" +
                "  attributes              TEXT         NULL,\n" +
                "  names                   TEXT         NULL,\n" +
                "  addresses               TEXT         NULL,\n" +
                "  encounter_type          CHAR(38)     NULL,\n" +
                "  encounter_type_name     VARCHAR(255) NULL,\n" +
                "  form                    CHAR(38)     NULL,\n" +
                "  form_name               VARCHAR(255) NULL,\n" +
                "  encounter               CHAR(38)     NULL,\n" +
                "  encounter_datetime      DATETIME     NULL,\n" +
                "  age_at_encounter        INT(3)       NULL,\n" +
                "  encounter_year          INT(4),\n" +
                "  encounter_month         INT(6),\n" +
                "  encounter_quarter       CHAR(6),\n" +
                "  concept                 CHAR(38)     NOT NULL,\n" +
                "  concept_name            VARCHAR(255) NULL,\n" +
                "  `order`                 CHAR(38)     NULL,\n" +
                "  obs_datetime            DATETIME     NOT NULL,\n" +
                "  obs_datetime_year       INT(4),\n" +
                "  obs_datetime_month      INT(6),\n" +
                "  obs_datetime_quarter    CHAR(6),\n" +
                "  age_at_observation      INT(3)       NULL,\n" +
                "  location                CHAR(38)     NULL,\n" +
                "  location_name           VARCHAR(255) NULL,\n" +
                "  obs_group               CHAR(38)     NULL,\n" +
                "  accession_number        VARCHAR(255) NULL,\n" +
                "  value_group             CHAR(38)     NULL,\n" +
                "  value_boolean           TINYINT(1)   NULL,\n" +
                "  value_coded             CHAR(38)     NULL,\n" +
                "  value_coded_name1       VARCHAR(255) NULL,\n" +
                "  report_name             VARCHAR(255) NULL,\n" +
                "  value_coded_name        CHAR(38)     NULL,\n" +
                "  value_drug              CHAR(38)     NULL,\n" +
                "  value_datetime          DATETIME     NULL,\n" +
                "  age_at_value_datetime   INT(3)       NULL,\n" +
                "  value_datetime_year     INT(4),\n" +
                "  value_datetime_month    INT(6),\n" +
                "  value_datetime_quarter  CHAR(6),\n" +
                "  value_numeric           DOUBLE       NULL,\n" +
                "  value_modifier          VARCHAR(2)   NULL,\n" +
                "  value_text              TEXT         NULL,\n" +
                "  value_complex           VARCHAR(255) NULL,\n" +
                "  comments                VARCHAR(255) NULL,\n" +
                "  creator                 CHAR(38)     NOT NULL,\n" +
                "  date_created            DATETIME     NOT NULL,\n" +
                "  voided                  TINYINT(1)   NOT NULL,\n" +
                "  voided_by               CHAR(38)     NULL,\n" +
                "  date_voided             DATETIME     NULL,\n" +
                "  void_reason             VARCHAR(255) NULL,\n" +
                "  uuid                    CHAR(38)     NOT NULL,\n" +
                "  previous_version        CHAR(38)     NULL,\n" +
                "  form_namespace_and_path VARCHAR(255) NULL,\n" +
                "  status                  VARCHAR(10)  NOT NULL\n" +
                ")  ENGINE = MYISAM;";

        try (Connection con = sql2o.open()) {
            con.createQuery(sql).executeUpdate();
        }
    }

    public static void normalizeObs(String startDate, Sql2o sql2o) {

        String sql = "INSERT INTO obs_normal (\n" +
                "  person_id\n" +
                "  , person\n" +
                "  , birth_date\n" +
                "  , birth_date_estimated\n" +
                "  , identifiers\n" +
                "  , attributes\n" +
                "  , names\n" +
                "  , addresses\n" +
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
                "  , `order`\n" +
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
                "  , value_coded\n" +
                "  , value_coded_name1\n" +
                "  , report_name\n" +
                "  , value_coded_name\n" +
                "  , value_drug\n" +
                "  , value_datetime\n" +
                "  , age_at_value_datetime\n" +
                "  , value_datetime_year\n" +
                "  , value_datetime_month\n" +
                "  , value_datetime_quarter\n" +
                "  , value_numeric\n" +
                "  , value_modifier\n" +
                "  , value_text\n" +
                "  , value_complex\n" +
                "  , comments\n" +
                "  , creator\n" +
                "  , date_created\n" +
                "  , voided\n" +
                "  , voided_by\n" +
                "  , date_voided\n" +
                "  , void_reason\n" +
                "  , uuid\n" +
                "  , previous_version\n" +
                "  , form_namespace_and_path\n" +
                "  , status)\n" +
                "\n" +
                "  SELECT\n" +
                "    o.person_id,\n" +
                "    (SELECT p.uuid\n" +
                "     FROM person AS p\n" +
                "     WHERE p.person_id = o.person_id)                                      AS person,\n" +
                "    (SELECT p.birthdate\n" +
                "     FROM person AS p\n" +
                "     WHERE p.person_id = o.person_id)                                      AS birth_date,\n" +
                "    (SELECT p.birthdate_estimated\n" +
                "     FROM person AS p\n" +
                "     WHERE p.person_id = o.person_id)                                      AS birth_date_estimated,\n" +
                "\n" +
                "    (SELECT GROUP_CONCAT(CONCAT_WS(':', COALESCE(pit.uuid, ''), COALESCE(identifier, '')))\n" +
                "     FROM patient_identifier pi INNER JOIN patient_identifier_type pit\n" +
                "         ON (pi.identifier_type = pit.patient_identifier_type_id)\n" +
                "     WHERE pi.patient_id = o.person_id)                                    AS 'identifiers',\n" +
                "    (SELECT GROUP_CONCAT(CONCAT_WS(':', COALESCE(pat.uuid, ''), COALESCE(value, '')))\n" +
                "     FROM person_attribute pa INNER JOIN person_attribute_type pat\n" +
                "         ON (pa.person_attribute_type_id = pat.person_attribute_type_id)\n" +
                "     WHERE o.person_id = pa.person_id)                                     AS 'attributes',\n" +
                "    (SELECT GROUP_CONCAT(CONCAT_WS(' ', COALESCE(given_name, ''), COALESCE(family_name, '')))\n" +
                "     FROM person_name pn\n" +
                "     WHERE o.person_id = pn.person_id)                                     AS 'names',\n" +
                "    (SELECT GROUP_CONCAT(CONCAT_WS(':', COALESCE(country, ''), COALESCE(state_province, ''), COALESCE(address1, ''),\n" +
                "                                   COALESCE(address2, '')))\n" +
                "     FROM person_address pas\n" +
                "     WHERE o.person_id = pas.person_id)                                    AS 'addresses',\n" +
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
                "    (SELECT oo.uuid\n" +
                "     FROM `orders` AS oo\n" +
                "     WHERE oo.order_id = o.order_id)                                       AS `order`,\n" +
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
                "    (SELECT uuid\n" +
                "     FROM concept AS c\n" +
                "     WHERE c.concept_id = o.value_coded)                                   AS value_coded,\n" +
                "    (SELECT cn.name\n" +
                "     FROM concept_name AS cn\n" +
                "     WHERE cn.concept_id = o.value_coded AND cn.concept_name_type = 'FULLY_SPECIFIED' AND cn.locale = 'en'\n" +
                "     LIMIT 1)                                                              AS value_coded_name1,\n" +
                "    CASE\n" +
                "    WHEN value_coded = 90033\n" +
                "      THEN '1'\n" +
                "    WHEN value_coded = 90034\n" +
                "      THEN '2'\n" +
                "    WHEN value_coded = 90035\n" +
                "      THEN '3'\n" +
                "    WHEN value_coded = 90036\n" +
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
                "    END                                                                    AS 'report_name',\n" +
                "    (SELECT cn.uuid\n" +
                "     FROM concept_name AS cn\n" +
                "     WHERE cn.concept_name_id = o.value_coded_name_id)                     AS value_coded_name,\n" +
                "    (SELECT d.uuid\n" +
                "     FROM drug AS d\n" +
                "     WHERE d.drug_id = o.value_drug)                                       AS value_drug,\n" +
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
                "    value_modifier,\n" +
                "    value_text,\n" +
                "    value_complex,\n" +
                "    comments,\n" +
                "    (SELECT u.uuid\n" +
                "     FROM users AS u\n" +
                "     WHERE u.user_id = o.creator)                                          AS creator,\n" +
                "    date_created,\n" +
                "    voided,\n" +
                "    (SELECT u.uuid\n" +
                "     FROM users AS u\n" +
                "     WHERE u.user_id = o.voided_by)                                        AS voided_by,\n" +
                "    date_voided,\n" +
                "    void_reason,\n" +
                "    uuid,\n" +
                "    (SELECT oi.uuid\n" +
                "     FROM obs AS oi\n" +
                "     WHERE oi.obs_id = o.previous_version)                                 AS previous_version,\n" +
                "    form_namespace_and_path,\n" +
                "    (CASE WHEN o.date_voided IS NOT NULL\n" +
                "      THEN 'VOIDED'\n" +
                "     ELSE 'NEW'\n" +
                "     END)                                                                  AS state\n" +
                "  FROM obs o\n" +
                String.format("  WHERE o.date_created > '%s' OR o.date_voided > '%s';", startDate, startDate);
        try (Connection con = sql2o.open()) {
            String table = con.createQuery("SHOW TABLES LIKE 'obs_normal';").executeScalar(String.class);

            if (table.equals("null")) {
                createNormalizedObsTable(sql2o);
            }

            con.createQuery(sql).executeUpdate();
        }
    }

    public static int lucenizeNormalizedObs(String indexDirectory, Sql2o sql2o, String startDate, boolean deleteIndexes) throws IOException {
        int total;
        IndexWriter writer = createWriter(indexDirectory);
        if (deleteIndexes) {
            writer.deleteAll();
        }

        String all = String.format("SELECT count(*) FROM obs_normal WHERE date_created > '%s' OR date_voided > '%s'", startDate, startDate);

        try (Connection con = sql2o.open()) {
            total = con.createQuery(all).executeScalar(Integer.class);

            for (int i = 0; i < total / 10000 + 1; i++) {
                String offset = String.valueOf(i * 10000);
                String sql = String.format("SELECT * FROM obs_normal WHERE date_created > '%s' OR date_voided > '%s' LIMIT %s,%s", startDate, startDate, offset, 10000);
                sql2o.setDefaultColumnMappings(UgandaEMRLucene.getColumnMappings());
                try (ResultSetIterable<NormalizedObs> normalizedObservations = con.createQuery(sql).executeAndFetchLazy(NormalizedObs.class)) {
                    for (NormalizedObs normalizedObs : normalizedObservations) {
                        Document document = UgandaEMRLucene.createDoc(normalizedObs);
                        writer.addDocument(document);
                    }
                }
            }

            writer.commit();
            writer.close();
        }

        return total;

    }
}
