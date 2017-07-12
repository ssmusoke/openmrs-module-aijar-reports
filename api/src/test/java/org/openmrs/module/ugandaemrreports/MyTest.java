package org.openmrs.module.ugandaemrreports;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Test;
import org.openmrs.module.ugandaemrreports.common.NormalizedObs;
import org.openmrs.module.ugandaemrreports.lucene.UgandaEMRLucene;
import org.sql2o.Connection;
import org.sql2o.ResultSetIterable;
import org.sql2o.Sql2o;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class MyTest {

    @Test
    public void dummyTest() throws IOException {

        String indexDirectory = System.getProperty("user.dir") + File.separator + "lucene";

        IndexWriter writer = UgandaEMRLucene.createWriter(indexDirectory);
        writer.deleteAll();

        Sql2o sql2o = new Sql2o("jdbc:mysql://localhost:3306/openmrs", "openmrs", "openmrs");
        sql2o.setDefaultColumnMappings(UgandaEMRLucene.getColumnMappings());
        String sql = "SELECT * FROM obs_normal LIMIT 100";

        try (Connection con = sql2o.open()) {
            try (ResultSetIterable<NormalizedObs> normalizedObservations = con.createQuery(sql).executeAndFetchLazy(NormalizedObs.class)) {
                for (NormalizedObs normalizedObs : normalizedObservations) {
                    System.out.println(normalizedObs);
                }
            }
        }

        assertNotNull(writer);

        /*try (Connection con = sql2o.open()) {
            List<Map<String, Object>> data = con.createQuery(sql).executeAndFetchTable().asList();
            for (Map<String, Object> document : data) {
                Document d = UgandaEMRLucene.createDoc(document);
                writer.addDocument(d);
            }
            writer.commit();
            writer.close();
            assertNotNull(writer);

        }*/
    }

    @Test
    public void testSearch() throws IOException, ParseException {
        String indexDirectory = System.getProperty("user.dir") + File.separator + "lucene";
        List<String> columns = Arrays.asList(
                "person_id"
                , "person"
                , "birth_date"
                , "birth_date_estimated"
                , "identifiers"
                , "attributes"
                , "names"
                , "addresses"
                , "encounter_type"
                , "encounter_type_name"
                , "form"
                , "form_name"
                , "encounter"
                , "encounter_datetime"
                , "age_at_encounter"
                , "encounter_year"
                , "encounter_month"
                , "encounter_quarter"
                , "concept"
                , "concept_name"
                , "`order`"
                , "obs_datetime"
                , "obs_datetime_year"
                , "obs_datetime_month"
                , "obs_datetime_quarter"
                , "age_at_observation"
                , "location"
                , "location_name"
                , "obs_group"
                , "accession_number"
                , "value_group"
                , "value_coded"
                , "value_coded_name1"
                , "report_name"
                , "value_coded_name"
                , "value_drug"
                , "value_datetime"
                , "age_at_value_datetime"
                , "value_datetime_year"
                , "value_datetime_month"
                , "value_datetime_quarter"
                , "value_numeric"
                , "value_modifier"
                , "value_text"
                , "value_complex"
                , "comments"
                , "creator"
                , "date_created"
                , "voided"
                , "voided_by"
                , "date_voided"
                , "void_reason"
                , "uuid"
                , "previous_version"
                , "form_namespace_and_path"
                , "status"
        );

        List<Map<String, String>> data = UgandaEMRLucene.getData(indexDirectory, "person_id", "51", columns);
        assertNotNull(data);
    }
}