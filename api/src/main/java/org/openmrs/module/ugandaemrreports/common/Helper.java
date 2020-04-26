package org.openmrs.module.ugandaemrreports.common;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.definition.service.SerializedDefinitionService;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.CsvReportRenderer;
import org.openmrs.module.reporting.report.renderer.ExcelTemplateRenderer;
import org.openmrs.module.reporting.report.renderer.XlsReportRenderer;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.util.OpenmrsClassLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This class contains the logic necessary to set-up and tear down a report definitions
 */
public class Helper {

    public static void purgeReportDefinition(String name) {
        ReportDefinitionService rds = Context.getService(ReportDefinitionService.class);
        try {
            ReportDefinition findDefinition = findReportDefinition(name);
            if (findDefinition != null) {
                rds.purgeDefinition(findDefinition);
            }
        } catch (RuntimeException e) {
            // intentional empty as the author is too long out of business...
        }
    }

    public static ReportDefinition findReportDefinition(String name) {
        ReportDefinitionService s = (ReportDefinitionService) Context.getService(ReportDefinitionService.class);
        List<ReportDefinition> defs = s.getDefinitions(name, true);
        for (ReportDefinition def : defs) {
            return def;
        }
        throw new RuntimeException("Couldn't find Definition " + name);
    }

    public static void saveReportDefinition(ReportDefinition rd) {
        ReportDefinitionService rds = (ReportDefinitionService) Context.getService(ReportDefinitionService.class);

        //try to find existing report definitions to replace
        List<ReportDefinition> definitions = rds.getDefinitions(rd.getName(), true);
        if (definitions.size() > 0) {
            ReportDefinition existingDef = definitions.get(0);
            rd.setId(existingDef.getId());
            rd.setUuid(existingDef.getUuid());
        }
        try {
            rds.saveDefinition(rd);
        } catch (Exception e) {
            SerializedDefinitionService s = (SerializedDefinitionService) Context
                    .getService(SerializedDefinitionService.class);
            s.saveDefinition(rd);
        }
    }

    /**
     * @return a new ReportDesign for a standard Excel output
     */
    public static ReportDesign createExcelDesign(ReportDefinition reportDefinition, String reportDesignName,
                                                 boolean includeParameters) {
        ReportDesign design = new ReportDesign();
        design.setName(reportDesignName);
        design.setReportDefinition(reportDefinition);

        design.setRendererType(XlsReportRenderer.class);
        if (includeParameters) {
            design.addPropertyValue(XlsReportRenderer.INCLUDE_DATASET_NAME_AND_PARAMETERS_PROPERTY, "true");
        }
        return design;
    }

    /**
     * @return a new ReportDesign for a standard CSV output
     */
    public static ReportDesign createCsvReportDesign(ReportDefinition reportDefinition, String reportDesignName) {
        ReportDesign design = new ReportDesign();
        design.setName(reportDesignName);
        design.setReportDefinition(reportDefinition);
        design.setRendererType(CsvReportRenderer.class);
        return design;
    }

    public static void saveReportDesign(ReportDesign design) {
        ReportService rs = Context.getService(ReportService.class);
        rs.saveReportDesign(design);
    }

    /**
     * Given a location on the classpath, return the contents of this resource as a String
     */
    public static String getStringFromResource(String resourceName) {
        InputStream is = null;
        try {
            is = OpenmrsClassLoader.getInstance().getResourceAsStream(resourceName);
            return IOUtils.toString(is, "UTF-8");
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to load resource: " + resourceName, e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static ReportDesign createRowPerPatientXlsOverviewReportDesign(ReportDefinition rd, String resourceName,
                                                                          String name,
                                                                          Map<? extends Object, ? extends Object>
                                                                                  properties)
            throws IOException {

        ReportService rs = Context.getService(ReportService.class);
        for (ReportDesign rdd : rs.getAllReportDesigns(false)) {
            if (name.equals(rdd.getName())) {
                rs.purgeReportDesign(rdd);
            }
        }

        ReportDesignResource resource = new ReportDesignResource();
        resource.setName(resourceName);
        resource.setExtension("xls");
        InputStream is = OpenmrsClassLoader.getInstance().getResourceAsStream(resourceName);
        resource.setContents(org.apache.poi.util.IOUtils.toByteArray(is));
        final ReportDesign design = new ReportDesign();
        design.setName(name);
        design.setReportDefinition(rd);
        design.setRendererType(ExcelTemplateRenderer.class);
        design.addResource(resource);
        if (properties != null) {
            design.getProperties().putAll(properties);
        }
        resource.setReportDesign(design);

        return design;
    }

    public static <T> List<T> slice(List<T> list, int index, int count) {
        List<T> result = new ArrayList<T>();
        if (index >= 0 && index < list.size()) {
            int end = index + count < list.size() ? index + count : list.size();
            for (int i = index; i < end; i++) {
                result.add(list.get(i));
            }
        }
        return result;
    }

    public static List<String> splitQuery(String query, int keyIndex) {
        List<String> l = new ArrayList<String>();
        if (StringUtils.isNotBlank(query)) {
            String[] results = query.split(",");
            for (String r : results) {
                String[] rows = r.split(Pattern.quote("|"));
                if ((keyIndex - 1) < rows.length) {
                    l.add(rows[keyIndex - 1]);
                }
            }
        }
        return l;
    }

    public static TreeMap<String, String> splitQuery(String query, int keyIndex, int valueIndex) {
        TreeMap<String, String> map = new TreeMap<String, String>();
        if (StringUtils.isNotBlank(query)) {
            String[] results = query.split(",");
            for (String r : results) {
                String[] rows = r.split(Pattern.quote("|"));
                if ((keyIndex - 1) < rows.length && (valueIndex - 1) < rows.length) {
                    map.put(rows[keyIndex - 1], rows[valueIndex - 1]);
                }
            }
        }
        return map;
    }

    public static TreeMap<String, TreeMap<String, String>> splitQuery(String query, int keyIndex, int value1Index, int value2Index) {
        TreeMap<String, TreeMap<String, String>> map = new TreeMap<String, TreeMap<String, String>>();
        if (StringUtils.isNotBlank(query)) {
            String[] results = query.split(",");
            for (String r : results) {
                TreeMap<String, String> otherMap = new TreeMap<String, String>();
                String[] rows = r.split(Pattern.quote("|"));
                if ((keyIndex - 1) < rows.length && (value1Index - 1) < rows.length && (value2Index - 1) < rows.length) {
                    otherMap.put(rows[value1Index - 1], rows[value2Index - 1]);
                    map.put(rows[keyIndex - 1], otherMap);
                }
            }
        }
        return map;
    }

    public static String getString(String concept) {
        HashMap<String, String> map = new HashMap<String, String>();
        //Arvs
        map.put("99015", "1a");
        map.put("99016", "1b");
        map.put("99005", "1c");
        map.put("99006", "1d");
        map.put("99039", "1e");
        map.put("99040", "1f");
        map.put("99041", "1g");
        map.put("99042", "1h");
        map.put("99007", "2a2");
        map.put("99008", "2a4");
        map.put("99044", "2b");
        map.put("99043", "2c");
        map.put("99282", "2d2");
        map.put("99283", "2d4");
        map.put("99046", "2e");
        map.put("99017", "5a");
        map.put("99018", "5b");
        map.put("99045", "5f");
        map.put("99284", "5g");
        map.put("99285", "5h");
        map.put("99286", "5j");
        map.put("90002", "othr");

        // Nutritional Status
        map.put("99271", "MAM");
        map.put("99272", "SAM");
        map.put("99273", "SAMO");
        map.put("99473", "PWG/PA");

        //TB Status
        map.put("90079", "1");
        map.put("90073", "2");
        map.put("90078", "3");
        map.put("90071", "4");

        //Functional Status
        map.put("90037", "A");
        map.put("90038", "W");
        map.put("90039", "B");

        //Adherence
        map.put("90156", "G");
        map.put("90157", "F");
        map.put("90158", "P");

        if (StringUtils.isNotBlank(concept)) {
            return map.get(concept);
        }
        return "";
    }

    public static List<Date> getDates(LocalDate beginning, Enums.Period period, Enums.PeriodInterval periodInterval, Integer periodDifference) {
        List<LocalDate> localDates = new ArrayList<LocalDate>();
        List<Date> convertedDates = new ArrayList<Date>();
        if (period == Enums.Period.MONTHLY) {
            if (periodInterval == Enums.PeriodInterval.AFTER) {
                localDates = Periods.addMonths(beginning, periodDifference);
            } else if (periodInterval == Enums.PeriodInterval.BEFORE) {
                localDates = Periods.subtractMonths(beginning, periodDifference);
            } else {
                localDates.add(Periods.monthStartFor(beginning));
                localDates.add(Periods.monthEndFor(beginning));
            }
        } else if (period == Enums.Period.QUARTERLY) {
            if (periodInterval == Enums.PeriodInterval.AFTER) {
                localDates = Periods.addQuarters(beginning, periodDifference);
            } else if (periodInterval == Enums.PeriodInterval.BEFORE) {
                localDates = Periods.subtractQuarters(beginning, periodDifference);
            } else {
                localDates.add(Periods.quarterStartFor(beginning));
                localDates.add(Periods.quarterEndFor(beginning));
            }
        } else {
            localDates.add(beginning);
        }

        if (localDates.size() == 1) {
            convertedDates.add(localDates.get(0).toDate());
        } else if (localDates.size() > 1) {
            convertedDates.add(localDates.get(0).toDate());
            convertedDates.add(localDates.get(1).toDate());

        }
        return convertedDates;
    }

    public void addIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition, String dimensionOptions) {
        CohortIndicator ci = new CohortIndicator();
        ci.addParameter(ReportingConstants.START_DATE_PARAMETER);
        ci.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ci.setType(CohortIndicator.IndicatorType.COUNT);
        ci.setCohortDefinition(Mapped.mapStraightThrough(cohortDefinition));
        dsd.addColumn(key, label, Mapped.mapStraightThrough(ci), dimensionOptions);
    }

}
