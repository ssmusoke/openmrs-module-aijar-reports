package org.openmrs.module.aijarreports.common;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.openmrs.module.reporting.common.DateUtil;

import java.util.Date;

/**
 * Created by carapai on 04/07/2016.
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
