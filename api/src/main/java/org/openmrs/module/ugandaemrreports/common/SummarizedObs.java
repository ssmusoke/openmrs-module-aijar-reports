package org.openmrs.module.ugandaemrreports.common;

import java.util.Date;

/**
 * Created by carapai on 16/07/2017.
 */
public class SummarizedObs {

    private String encounterType;
    private Integer y;
    private Integer q;
    private Integer m;
    private String concept;
    private String vals;
    private String patients;
    private String ageGender;
    private Integer total;

    public SummarizedObs() {
    }

    public SummarizedObs(String encounterType, Integer y, Integer q, Integer m, String concept, String vals, String patients, String ageGender, Integer total) {
        this.encounterType = encounterType;
        this.y = y;
        this.q = q;
        this.m = m;
        this.concept = concept;
        this.vals = vals;
        this.patients = patients;
        this.ageGender = ageGender;
        this.total = total;
    }

    public String getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(String encounterType) {
        this.encounterType = encounterType;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public Integer getQ() {
        return q;
    }

    public void setQ(Integer q) {
        this.q = q;
    }

    public Integer getM() {
        return m;
    }

    public void setM(Integer m) {
        this.m = m;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getVals() {
        return vals;
    }

    public void setVals(String vals) {
        this.vals = vals;
    }

    public String getPatients() {
        return patients;
    }

    public void setPatients(String patients) {
        this.patients = patients;
    }

    public String getAgeGender() {
        return ageGender;
    }

    public void setAgeGender(String ageGender) {
        this.ageGender = ageGender;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
