package org.openmrs.module.aijarreports;

import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;

/**
 * Utility classes that can be used for convenience
 */
public class AijarReportUtil {

    /**
     * @return a new ReportDesign for a standard Excel output
     */
    public static ReportDesign createExcelDesign(String reportDesignUuid, ReportDefinition reportDefinition) {
        ReportDesign design = ReportManagerUtil.createExcelDesign(reportDesignUuid, reportDefinition);
        return design;
    }
}
