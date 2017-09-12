package org.openmrs.module.ugandaemrreports.common;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.openmrs.module.reporting.common.DateUtil;

import java.util.*;

/**
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
        LocalDate endDate = quarterEndFor(date);
        LocalDate startDate = quarterStartFor(date);

        LocalDate beginningDate = startDate.plusMonths(numberOfQuarters * 3);

        LocalDate endingDate = endDate.plusMonths(numberOfQuarters * 3);

        return Arrays.asList(beginningDate, endingDate);
    }

    public static List<LocalDate> subtractQuarters(LocalDate date, Integer numberOfQuarters) {
        LocalDate endDate = quarterEndFor(date);
        LocalDate startDate = quarterStartFor(date);

        LocalDate beginningDate = startDate.minusMonths(numberOfQuarters * 3);

        LocalDate endingDate = endDate.minusMonths(numberOfQuarters * 3);

        return Arrays.asList(beginningDate, endingDate);
    }

    public static List<LocalDate> addMonths(LocalDate date, Integer numberOfMonths) {
        LocalDate workingDate = monthStartFor(date);

        LocalDate addedMonths = workingDate.plusMonths(numberOfMonths);

        return Arrays.asList(monthStartFor(addedMonths), monthEndFor(addedMonths));
    }

    public static List<LocalDate> subtractMonths(LocalDate date, Integer numberOfMonths) {
        LocalDate workingDate = monthStartFor(date);

        LocalDate addedMonths = workingDate.minusMonths(numberOfMonths);

        return Arrays.asList(monthStartFor(addedMonths), monthEndFor(addedMonths));
    }

    public static List<LocalDate> getDatesDuringPeriods(LocalDate workingDate, Integer getPeriodToAdd, Enums.Period period) {
        List<LocalDate> dates;
        if (getPeriodToAdd > 0) {
            if (period == Enums.Period.QUARTERLY) {
                dates = Periods.addQuarters(workingDate, getPeriodToAdd);
            } else if (period == Enums.Period.MONTHLY) {
                dates = Periods.addMonths(workingDate, getPeriodToAdd);
            } else {
                dates = Arrays.asList(workingDate, StubDate.dateOf(DateUtil.formatDate(new Date(), "yyyy-MM-dd")));
            }
        } else {
            if (period == Enums.Period.MONTHLY) {
                dates = Arrays.asList(Periods.monthStartFor(workingDate), Periods.monthEndFor(workingDate));
            } else if (period == Enums.Period.QUARTERLY) {
                dates = Arrays.asList(Periods.quarterStartFor(workingDate), Periods.quarterEndFor(workingDate));
            } else {
                dates = Arrays.asList(workingDate, StubDate.dateOf(DateUtil.formatDate(new Date(), "yyyy-MM-dd")));
            }
        }
        return dates;
    }

    public static TreeMap<String, Interval> getQuarters(LocalDate workingDate, int numbers) {

        TreeMap<String, Interval> intervalTreeMap = new TreeMap<String, Interval>();

        for (int i = 0; i < numbers; i++) {
            List<LocalDate> localDates = getDatesDuringPeriods(workingDate, i, Enums.Period.QUARTERLY);
            Collections.sort(localDates);
            DateTime start = new DateTime(localDates.get(0).getYear(), localDates.get(0).getMonthOfYear(), localDates.get(0).getDayOfMonth(), 0, 0, 0, 0);
            DateTime end = new DateTime(localDates.get(1).getYear(), localDates.get(1).getMonthOfYear(), localDates.get(1).getDayOfMonth(), 0, 0, 0, 0);
            Interval interval = new Interval(start, end);

            intervalTreeMap.put(String.valueOf(i), interval);
        }

        return intervalTreeMap;
    }

    public static TreeMap<String, Interval> getMonths(LocalDate workingDate, int numbers) {

        TreeMap<String, Interval> intervalTreeMap = new TreeMap<String, Interval>();

        for (int i = 0; i < numbers; i++) {
            List<LocalDate> localDates = getDatesDuringPeriods(workingDate, i, Enums.Period.MONTHLY);
            Collections.sort(localDates);
            DateTime start = new DateTime(localDates.get(0).getYear(), localDates.get(0).getMonthOfYear(), localDates.get(0).getDayOfMonth(), 0, 0, 0, 0);
            DateTime end = new DateTime(localDates.get(1).getYear(), localDates.get(1).getMonthOfYear(), localDates.get(1).getDayOfMonth(), 0, 0, 0, 0);
            Interval interval = new Interval(start, end);

            intervalTreeMap.put(String.valueOf(i), interval);
        }

        return intervalTreeMap;
    }

    public static boolean isDateInTheInterval(String date, Interval interval) {
        DateTime d = DateTime.parse(date, DateTimeFormat.forPattern("yyyy-MM-dd"));
        return interval.contains(d);
    }

    public static Integer isDateInTheInterval(String date, TreeMap<String, Interval> intervals) {
        for (Map.Entry<String, Interval> entry : intervals.entrySet()) {
            String key = entry.getKey();
            Interval value = entry.getValue();
            if (isDateInTheInterval(date, value)) {
                return Integer.valueOf(key);
            }
        }
        return null;
    }

    public static List<String> listOfDatesInPeriods(TreeMap<String, Interval> periods, List<String> dates) {
        List<String> ps = new ArrayList<String>();
        if (CollectionUtils.isNotEmpty(dates)) {
            for (String date : dates) {
                for (Map.Entry<String, Interval> entry : periods.entrySet()) {
                    String key = entry.getKey();
                    Interval value = entry.getValue();
                    if (isDateInTheInterval(date, value)) {
                        ps.add(key);
                        break;
                    }
                }
            }
        }
        return ps;
    }


    public static TreeMap<String, TreeMap<String, String>> listOfDatesInPeriods(TreeMap<String, Interval> periods, TreeMap<String, String> dateValues) {
        TreeMap<String, TreeMap<String, String>> ps = new TreeMap<String, TreeMap<String, String>>();
        if (MapUtils.isNotEmpty(dateValues)) {
            for (Map.Entry<String, String> dateValue : dateValues.entrySet()) {
                for (Map.Entry<String, Interval> entry : periods.entrySet()) {
                    String key = entry.getKey();
                    Interval value = entry.getValue();
                    if (isDateInTheInterval(dateValue.getKey(), value)) {
                        if (ps.containsKey(key)) {
                            TreeMap<String, String> oldMap = ps.get(key);
                            oldMap.put(dateValue.getKey(), dateValue.getValue());
                            ps.put(key, oldMap);
                        } else {
                            TreeMap<String, String> newMap = new TreeMap<String, String>();
                            newMap.put(dateValue.getKey(), dateValue.getValue());
                            ps.put(key, newMap);
                        }
                        break;
                    }
                }
            }
        }
        return ps;
    }

    public static TreeMap<String, String> changeKeys(TreeMap<String, String> map1, TreeMap<String, String> map2) {
        TreeMap<String, String> emptyMap = new TreeMap<String, String>();
        for (Map.Entry<String, String> entry : map1.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (map2.containsKey(key)) {
                emptyMap.put(map2.get(key), value);
            }
        }
        return emptyMap;
    }
}
