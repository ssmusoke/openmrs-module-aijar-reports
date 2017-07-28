package org.openmrs.module.ugandaemrreports;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Test;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.ugandaemrreports.common.Enums;
import org.openmrs.module.ugandaemrreports.library.UgandaEMRReporting;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MyTest {
    Date d = DateUtil.parseDate("2015-07-01", "yyyy-MM-dd");

    @Test
    public void shouldJoinStringUsingOR() throws IOException, ParseException {
        assertEquals(UgandaEMRReporting.joinQuery("1", "2", Enums.UgandaEMRJoiner.OR), "1 OR 2");
    }

    @Test
    public void shouldJoinStringUsingAND() throws IOException, ParseException {
        assertEquals(UgandaEMRReporting.joinQuery("1", "2", Enums.UgandaEMRJoiner.AND), "1 AND 2");
    }

    @Test
    public void shouldComplexJoin() throws IOException, ParseException {
        assertEquals(UgandaEMRReporting.joinQuery(UgandaEMRReporting.constructSQLInQuery("name", Arrays.asList(1, 2, 4, 53, 3)), "2", Enums.UgandaEMRJoiner.AND), "name IN(1,2,4,53,3) AND 2");
    }

    @Test
    public void shouldConstructMonthlyPeriod() throws IOException, ParseException {

        assertEquals(UgandaEMRReporting.getObsPeriod(d, Enums.Period.MONTHLY), "201507");
    }

    @Test
    public void shouldConstructQuarterlyPeriod() throws IOException, ParseException {

        assertEquals(UgandaEMRReporting.getObsPeriod(d, Enums.Period.QUARTERLY), "2015Q3");
    }

    @Test
    public void shouldConstructYearlyPeriod() throws IOException, ParseException {

        assertEquals(UgandaEMRReporting.getObsPeriod(d, Enums.Period.YEARLY), "2015");
    }

    @Test
    public void shouldConstructWeeklyPeriod() throws IOException, ParseException {

        assertEquals(UgandaEMRReporting.getObsPeriod(d, Enums.Period.WEEKLY), "2015W27");
    }

    /*@Test
    public void shouldLoopArtYearsProperly() {
        LocalDate localDate = StubDate.dateOf(d);
        for (int i = 0; i <= 72; i++) {
            String period = UgandaEMRReporting.getObsPeriod(Periods.addMonths(localDate, i).get(0).toDate(), Enums.Period.MONTHLY);
            System.out.println(period);
        }
    }

    @Test
    public void shouldLoopPreArtQuartersProperly() {
        LocalDate localDate = StubDate.dateOf(d);
        for (int i = 0; i < 16; i++) {
            String period = UgandaEMRReporting.getObsPeriod(Periods.addQuarters(localDate, i).get(0).toDate(), Enums.Period.QUARTERLY);
            System.out.println(period);
        }
    }*/

    @Test
    public void shouldReturnSqlConnection() throws SQLException, ClassNotFoundException {
        assertNotNull(UgandaEMRReporting.testSqlConnection());
    }

}