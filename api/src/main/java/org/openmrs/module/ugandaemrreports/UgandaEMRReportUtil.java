package org.openmrs.module.ugandaemrreports;

import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.openmrs.module.reporting.report.renderer.CsvReportRenderer;
import org.openmrs.module.reporting.report.renderer.XmlReportRenderer;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility classes that can be used for convenience
 */
public class UgandaEMRReportUtil {

    /**
     * Maps a parameterizable item with no parameters
     *
     * @param parameterizable the parameterizable item
     * @param <T>
     * @return the mapped item
     */
    public static <T extends Parameterizable> Mapped<T> map(T parameterizable) {
        if (parameterizable == null) {
            throw new IllegalArgumentException("Parameterizable cannot be null");
        }
        return new Mapped<T>(parameterizable, null);
    }

    /**
     * Maps a parameterizable item using a string list of parameters and values
     *
     * @param parameterizable the parameterizable item
     * @param mappings        the string list of mappings
     * @param <T>
     * @return the mapped item
     */
    public static <T extends Parameterizable> Mapped<T> map(T parameterizable, String mappings) {
        if (parameterizable == null) {
            throw new IllegalArgumentException("Parameterizable cannot be null");
        }
        if (mappings == null) {
            mappings = ""; // probably not necessary, just to be safe
        }
        return new Mapped<T>(parameterizable, ParameterizableUtil.createParameterMappings(mappings));
    }

    /**
     * Maps a parameterizable item using a string list of parameters and values
     *
     * @param parameterizable the parameterizable item
     * @param mappings        the string list of mappings
     * @param <T>
     * @return the mapped item
     */
    public static <T extends Parameterizable> Mapped<T> map(T parameterizable, Object... mappings) {
        if (parameterizable == null) {
            throw new IllegalArgumentException("Parameterizable cannot be null");
        }

        Map<String, Object> paramMap = new HashMap<String, Object>();
        for (int m = 0; m < mappings.length; m += 2) {
            String param = (String) mappings[m];
            Object value = mappings[m + 1];
            paramMap.put(param, value);
        }
        return new Mapped<T>(parameterizable, paramMap);
    }

    /**
     * @return a new ReportDesign for a CSV output
     */
    public static ReportDesign createCSVDesign(String reportDesignUuid, ReportDefinition reportDefinition) {
        ReportDesign design = ReportManagerUtil.createCsvReportDesign(reportDesignUuid, reportDefinition);
        return design;
    }
    public static ReportDesign createXmlReportDesign( ReportDefinition reportDefinition) {
        ReportDesign design = new ReportDesign();
        design.setName("XML");
        design.setReportDefinition(reportDefinition);
        design.setRendererType(XmlReportRenderer.class);
        return design;
    }
    public static ReportDesign createJSONReportDesign(String reportDesignUuid, ReportDefinition reportDefinition) {
        ReportDesign design = ReportManagerUtil.createJSONReportDesign(reportDesignUuid, reportDefinition);
        return design;
    }


}
