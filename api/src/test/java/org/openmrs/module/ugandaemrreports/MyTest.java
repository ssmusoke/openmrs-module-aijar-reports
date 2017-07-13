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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class MyTest {
    Sql2o sql2o = new Sql2o("jdbc:mysql://localhost:3306/openmrs", "openmrs", "openmrs");

    @Test
    public void dummyTest() throws IOException {

        String indexDirectory = System.getProperty("user.dir") + File.separator + "lucene";
        int total = UgandaEMRLucene.lucenizeNormalizedObs(indexDirectory, sql2o, "2017-07-31", false);
        assertEquals(total, 0);
    }

    @Test
    public void testSearch() throws IOException, ParseException {
        String indexDirectory = System.getProperty("user.dir") + File.separator + "lucene";
        String inQuery = UgandaEMRLucene.constructInQuery("personId", Arrays.asList("51", "54"));
        List<NormalizedObs> data = UgandaEMRLucene.getData(indexDirectory, "personId", inQuery);
        System.out.println(data.size());
        assertNotEquals(0, data.size());
    }

    @Test
    public void checkObsNormalTable() throws IOException, ParseException {
        UgandaEMRLucene.normalizeObs("2017-06-30", sql2o);
        assertNotEquals(0, 1);
    }
}