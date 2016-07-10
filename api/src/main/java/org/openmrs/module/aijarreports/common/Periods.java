package org.openmrs.module.aijarreports.common;

import org.joda.time.LocalDate;

import java.util.Arrays;
import java.util.List;

/**
 * Created by carapai on 04/07/2016.
 */
public class Periods {

    public static LocalDate quarterStartFor(LocalDate date) {
        return date.withDayOfMonth(1).withMonthOfYear((((date.getMonthOfYear() - 1) / 3) * 3) + 1);
    }

    public static LocalDate monthStartFor(LocalDate date) {
        return date.withDayOfMonth(1);
    }

    public static LocalDate monthEndFor(LocalDate date) {
        return date.plusMonths(1).withDayOfMonth(1).minusDays(1);
    }

    public static LocalDate quarterEndFor(LocalDate date) {
        return quarterStartFor(date).plusMonths(3).minusDays(1);
    }

    public static List<LocalDate> addQuarters(LocalDate date, Integer numberOfQuarters) {
        LocalDate startDate = quarterEndFor(date);

        LocalDate addedQuarters = startDate.plusMonths(numberOfQuarters * 3).minusDays(1);

        LocalDate beginningDate = quarterStartFor(addedQuarters);

        return Arrays.asList(beginningDate, addedQuarters);
    }

    public static List<LocalDate> addMonths(LocalDate date, Integer numberOfMonths) {
        LocalDate workingDate = monthStartFor(date);

        LocalDate addedMonths = workingDate.plusMonths(numberOfMonths);

        return Arrays.asList(monthStartFor(addedMonths), monthEndFor(addedMonths));
    }
}
