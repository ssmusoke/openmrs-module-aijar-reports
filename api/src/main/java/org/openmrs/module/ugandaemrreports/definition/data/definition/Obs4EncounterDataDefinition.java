package org.openmrs.module.ugandaemrreports.definition.data.definition;

import org.openmrs.EncounterType;
import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

import java.util.Date;
import java.util.List;

/**
 * Created by carapai on 27/09/2017.
 */
public class Obs4EncounterDataDefinition extends BaseDataDefinition implements PatientDataDefinition {

    //****** CONSTRUCTORS ******

    /**
     * Default Constructor
     */
    public Obs4EncounterDataDefinition() {
        super();
    }

    @ConfigurationProperty
    private Date startDate;

    @ConfigurationProperty
    private Date endDate;

    @ConfigurationProperty
    private EncounterType encounterType;

    //***** INSTANCE METHODS *****

    /**
     * @see org.openmrs.module.reporting.data.DataDefinition#getDataType()
     */
    public Class<?> getDataType() {
        return List.class;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public EncounterType getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(EncounterType encounterType) {
        this.encounterType = encounterType;
    }
}
