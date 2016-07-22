package org.openmrs.module.aijarreports.definition.data.definition;

import org.openmrs.Obs;
import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;

import java.util.Date;

/**
 * Created by carapai on 15/07/2016.
 */
@Caching(
        strategy = ConfigurationPropertyCachingStrategy.class
)

public class EMTCTPatientDataDefinition extends BaseDataDefinition implements PatientDataDefinition {

    @ConfigurationProperty
    private Date onDate;

    @ConfigurationProperty
    private int pregnancyNo = 1;

    @Override
    public Class<?> getDataType() {
        return Obs.class;
    }

    public Date getOnDate() {
        return onDate;
    }

    public void setOnDate(Date onDate) {
        this.onDate = onDate;
    }

    public int getPregnancyNo() {
        return pregnancyNo;
    }

    public void setPregnancyNo(int pregnancyNo) {
        this.pregnancyNo = pregnancyNo;
    }
}
