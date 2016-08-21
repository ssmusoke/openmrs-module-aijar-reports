package org.openmrs.module.ugandaemrreports;

import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Utility classes that can be used for convenience
 */
public class UgandaEMRReportUtil {

    /**
     * @return a new ReportDesign for a standard Excel output
     */
    public static ReportDesign createExcelDesign(String reportDesignUuid, ReportDefinition reportDefinition) {
        ReportDesign design = ReportManagerUtil.createExcelDesign(reportDesignUuid, reportDefinition);
        return design;
    }

    /**
     * @return a new ReportDesign for a standard Excel output
     */
    public static ReportDesign createExcelDesignWithProperties(String reportDesignUuid, ReportDefinition reportDefinition, Properties props) {
        ReportDesign design = ReportManagerUtil.createExcelDesign(reportDesignUuid, reportDefinition);
        design.setProperties(props);
        return design;
    }

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
}
