package org.openmrs.module.ugandaemrreports.definition.dataset.definition;

import org.codehaus.jackson.JsonNode;
import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

import java.io.File;
import java.util.Date;

public class AggregateReportDataSetDefinition extends BaseDataSetDefinition {
    @ConfigurationProperty
    private Date startDate;

    @ConfigurationProperty
    private Date endDate;

    @ConfigurationProperty
    private File reportDesign;

    public AggregateReportDataSetDefinition() {
        super();
    }

    public AggregateReportDataSetDefinition(String name, String description) {
        super(name, description);
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public File getReportDesign() {
        return reportDesign;
    }

    public void setReportDesign(File reportDesign) {
        this.reportDesign = reportDesign;
    }
}
