package org.openmrs.module.ugandaemrreports;

import com.google.common.collect.ImmutableMap;
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
import java.util.Set;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class MyTest {

    @Test
    public void dummyTest() throws IOException {

        String indexDirectory = System.getProperty("user.dir") + File.separator + "lucene";

        IndexWriter writer = UgandaEMRLucene.createWriter(indexDirectory);
        writer.deleteAll();

        Sql2o sql2o = new Sql2o("jdbc:mysql://localhost:3306/openmrs", "openmrs", "openmrs");
        sql2o.setDefaultColumnMappings(UgandaEMRLucene.getColumnMappings());
        String sql = "SELECT * FROM obs_normal LIMIT 10000";

        try (Connection con = sql2o.open()) {
            try (ResultSetIterable<NormalizedObs> normalizedObservations = con.createQuery(sql).executeAndFetchLazy(NormalizedObs.class)) {
                for (NormalizedObs normalizedObs : normalizedObservations) {
                    Document document = UgandaEMRLucene.createDoc(normalizedObs);
                    writer.addDocument(document);
                }

                writer.commit();
                writer.close();
            }
        }

        assertNotNull(writer);
    }

    @Test
    public void testSearch() throws IOException, ParseException {
        String indexDirectory = System.getProperty("user.dir") + File.separator + "lucene";
        List<NormalizedObs> data = UgandaEMRLucene.getData(indexDirectory, "personId", "personId:\"51\" AND encounterType: \"8d5b27bc-c2cc-11de-8d13-0010c6dffd0f\"");
        System.out.println(data.size());
        assertNotEquals(0, data.size());
    }
}