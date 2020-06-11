package org.openmrs.module.ugandaemrreports.definition.dataset.definition;

import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

import java.util.Date;

/**
 */
public class MedianBaselineCD4DatasetDefinition extends BaseDataSetDefinition {

    private static final long serialVersionUID = 6405583324151111487L;
    @ConfigurationProperty
    private Date startDate;

    @ConfigurationProperty
    private  Date endDate;

    public MedianBaselineCD4DatasetDefinition() {
        super();
    }

    public MedianBaselineCD4DatasetDefinition(String name, String description) {
        super(name, description);
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

}
