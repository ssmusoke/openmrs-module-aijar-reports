package org.openmrs.module.ugandaemrreports.common;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.openmrs.module.reporting.common.DateUtil;

import java.util.Date;

/**
 */
public class StubDate {

    public static LocalDate dateOf(String date) {
        return DateTimeFormat.forPattern("yyyy-MM-dd").withZone(DateTimeZone.UTC).parseDateTime(date).toLocalDate();
    }

    public static LocalDate dateOf(Date date) {
        String d = DateUtil.formatDate(date, "yyyy-MM-dd");
        return dateOf(d);
    }

}
