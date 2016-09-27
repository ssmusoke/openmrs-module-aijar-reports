package org.openmrs.module.ugandaemrreports.library;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by carapai on 12/09/2016.
 */
public class Parameters {

    public static final String START_DATE = "startDate=startDate";
    public static final String END_DATE = "endDate=endDate";

    public static final String ON_OR_AFTER_START_DATE = "onOrAfter=startDate";
    public static final String ON_OR_BEFORE_END_DATE = "onOrBefore=endDate";

    public static final String ON_OR_AFTER_END_DATE = "onOrAfter=endDate";
    public static final String ON_OR_BEFORE_START_DATE = "onOrBefore=startDate";

    public static final String VALUE1_START_DATE = "value1=startDate";
    public static final String VALUE2_END_DATE = "value2=endDate";

    public static final String VALUE1_END_DATE = "value1=endDate";
    public static final String VALUE2_START_DATE = "value2=startDate";

    public static final String VALUE_DATETIME_OR_AFTER_START_DATE = "valueDatetimeOrAfter=startDate";
    public static final String VALUE_DATETIME_ON_OR_BEFORE_END_DATE = "valueDatetimeOnOrBefore=endDate";

    public static final String VALUE_DATETIME_OR_AFTER_END_DATE = "valueDatetimeOrAfter=endDate";
    public static final String VALUE_DATETIME_ON_OR_BEFORE_START_DATE = "valueDatetimeOnOrBefore=startDate";


    public static String createParameterAfterDuration(String parameterName, String parameterValue, String duration) {
        return parameterName + "=" + parameterValue + "+" + duration;
    }

    public static String createParameterBeforeDuration(String parameterName, String parameterValue, String duration) {
        return parameterName + "=" + parameterValue + "-" + duration;
    }

    public static String combineParameters(String... parameters) {
        return StringUtils.join(parameters, ",");
    }
}
