package org.openmrs.module.ugandaemrreports;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.junit.Test;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.ugandaemrreports.common.*;
import org.openmrs.module.ugandaemrreports.definition.predicates.SummarizedObsPatientPredicate;
import org.openmrs.module.ugandaemrreports.definition.predicates.SummarizedObsPredicate;
import org.openmrs.module.ugandaemrreports.library.UgandaEMRReporting;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotEquals;
import static org.openmrs.module.ugandaemrreports.library.UgandaEMRReporting.*;

public class MyTest {
    Date d = DateUtil.parseDate("2015-07-01", "yyyy-MM-dd");


    @Test
    public void testMap() {
        Map<String, String> period = new HashMap<>();

        period.put("y", "2016");
        period.put("q", "1");
        String string = Joiner.on(" AND ").withKeyValueSeparator(" = ").join(period);
        System.out.printf(string);
    }

    @Test
    public void createSummaryTable() throws SQLException, ClassNotFoundException {
        Connection connection = testSqlConnection();
        createSummarizedObsTable(connection);

        summarizeObs("1900-01-01", connection);
    }

    @Test
    public void testDatabaseSearch() throws IOException, ParseException, SQLException, ClassNotFoundException {
        Connection connection = testSqlConnection();
        String where = joinQuery(constructSQLQuery("concept", "=", "90315"), constructSQLInQuery("y", Arrays.asList("2017", "2016")), Enums.UgandaEMRJoiner.AND);
        String query = "select * from obs_summary where " + where;
        List<SummarizedObs> data = getSummarizedObs(connection, query);
        System.out.println(data.size());
        assertNotEquals(0, data.size());
    }
}