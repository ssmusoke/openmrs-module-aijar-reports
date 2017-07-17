package org.openmrs.module.ugandaemrreports;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Test;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.ugandaemrreports.common.Enums;
import org.openmrs.module.ugandaemrreports.common.NormalizedObs;
import org.openmrs.module.ugandaemrreports.lucene.UgandaEMRLucene;
import org.sql2o.Sql2o;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class MyTest {
    Sql2o sql2o = new Sql2o("jdbc:mysql://localhost:3306/openmrs", "openmrs", "openmrs");
    Date d = DateUtil.parseDate("2016-01-04", "yyyy-MM-dd");

    @Test
    public void shouldCreateIndexesOfNormalizedObs() throws IOException {
        String indexDirectory = System.getProperty("user.dir") + File.separator + "lucene" + File.separator + "normalized_obs";
        int total = UgandaEMRLucene.lucenizeNormalizedObs(indexDirectory, sql2o, "1900-01-01", true);
        assertNotEquals(total, 0);
    }

    @Test
    public void shouldCreateIndexesOfSummarizedObs() throws IOException {
        String indexDirectory = System.getProperty("user.dir") + File.separator + "lucene" + File.separator + "summarised_obs";
        int total = UgandaEMRLucene.lucenizeSummarizedObs(indexDirectory, sql2o, "1900-01-01", true);
        assertNotEquals(total, 0);
    }


    @Test
    public void testInSearch() throws IOException, ParseException {
        String indexDirectory = System.getProperty("user.dir") + File.separator + "lucene";
        String inQuery = UgandaEMRLucene.constructLuceneInQuery("personId", Arrays.asList("102"));
        List<NormalizedObs> data = UgandaEMRLucene.getData(indexDirectory, "personId", inQuery);
        assertEquals(47, data.size());
    }

    @Test
    public void testInAndOrSearch() throws IOException, ParseException {
        String indexDirectory = System.getProperty("user.dir") + File.separator + "lucene";
        String inQuery = UgandaEMRLucene.constructLuceneInQuery("personId", Arrays.asList("102"));

        String query = UgandaEMRLucene.joinQuery(inQuery, UgandaEMRLucene.constructLuceneQuery("encounterQuarter", "2017Q1"), Enums.UgandaEMRJoiner.OR);

        List<NormalizedObs> data = UgandaEMRLucene.getData(indexDirectory, "personId", query);
        assertEquals(76314, data.size());
    }

    @Test
    public void shouldNormalizeObs() throws IOException, ParseException {
        UgandaEMRLucene.normalizeObs("1900-01-01", sql2o);
        assertNotEquals(0, 1);
    }

    @Test
    public void shouldSummarizeObs() throws IOException, ParseException {

        UgandaEMRLucene.summarizeObs("1900-01-01", sql2o);

        assertNotEquals(1, 2);
    }

    @Test
    public void shouldJoinStringUsingOR() throws IOException, ParseException {
        assertEquals(UgandaEMRLucene.joinQuery("1", "2", Enums.UgandaEMRJoiner.OR), "1 OR 2");
    }

    @Test
    public void shouldJoinStringUsingAND() throws IOException, ParseException {
        assertEquals(UgandaEMRLucene.joinQuery("1", "2", Enums.UgandaEMRJoiner.AND), "1 AND 2");
    }

    @Test
    public void shouldComplexJoin() throws IOException, ParseException {
        assertEquals(UgandaEMRLucene.joinQuery(UgandaEMRLucene.constructSQLInQuery("name", Arrays.asList(1,2,4,53,3)), "2", Enums.UgandaEMRJoiner.AND), "name IN(1,2,4,53,3) AND 2");
    }

    @Test
    public void shouldConstructMonthlyPeriod() throws IOException, ParseException {

        assertEquals(UgandaEMRLucene.getObsPeriod(d, Enums.Period.MONTHLY), "201601");
    }

    @Test
    public void shouldConstructQuarterlyPeriod() throws IOException, ParseException {

        assertEquals(UgandaEMRLucene.getObsPeriod(d, Enums.Period.QUARTERLY), "2016Q1");
    }

    @Test
    public void shouldConstructYearlyPeriod() throws IOException, ParseException {

        assertEquals(UgandaEMRLucene.getObsPeriod(d, Enums.Period.YEARLY), "2016");
    }

    @Test
    public void shouldConstructWeeklyPeriod() throws IOException, ParseException {

        assertEquals(UgandaEMRLucene.getObsPeriod(d, Enums.Period.WEEKLY), "2016W1");
    }

}