package org.openmrs.module.ugandaemrreports.lucene;

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
import org.openmrs.module.ugandaemrreports.common.NormalizedObs;

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
        for (Map.Entry<String, String> column : getColumnMappings().entrySet()) {
            Field titleField1 = new Field(column.getKey(), String.valueOf(""), titleType);
            doc1.add(titleField1);
        }
        return doc1;
    }

    public static List<Map<String, String>> getData(String indexDirectory, String column, String value, List<String> columns) throws IOException, ParseException {
        List<Map<String, String>> result = new ArrayList<>();
        try {
            IndexSearcher searcher = createSearcher(indexDirectory);
            QueryParser qp = new QueryParser(column, new StandardAnalyzer());
            Query query = qp.parse(value);
            TopDocs hits = searcher.search(query, 10000);
            for (ScoreDoc sd : hits.scoreDocs) {
                Document d = searcher.doc(sd.doc);
                Map<String, String> data = new HashMap<>();
                for (String c : columns) {
                    data.put(c, d.get(c));
                }
                result.add(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
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
}
