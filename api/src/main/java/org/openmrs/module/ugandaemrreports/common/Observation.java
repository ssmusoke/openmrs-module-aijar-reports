package org.openmrs.module.ugandaemrreports.common;

import java.util.Date;

/**
 * Created by carapai on 26/11/2017.
 */
public class Observation {
    private Integer concept;
    private Date obsDatetime;
    private String value;
    private Integer obsGroup;

    public Observation(Integer concept, Date obsDatetime, String value, Integer obsGroup) {
        this.concept = concept;
        this.obsDatetime = obsDatetime;
        this.value = value;
        this.obsGroup = obsGroup;
    }

    public Integer getConcept() {
        return concept;
    }

    public void setConcept(Integer concept) {
        this.concept = concept;
    }

    public Date getObsDatetime() {
        return obsDatetime;
    }

    public void setObsDatetime(Date obsDatetime) {
        this.obsDatetime = obsDatetime;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getObsGroup() {
        return obsGroup;
    }

    public void setObsGroup(Integer obsGroup) {
        this.obsGroup = obsGroup;
    }
}
