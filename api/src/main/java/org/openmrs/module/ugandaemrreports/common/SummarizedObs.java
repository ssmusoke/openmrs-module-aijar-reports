package org.openmrs.module.ugandaemrreports.common;

import java.util.Date;

/**
 * Created by carapai on 16/07/2017.
 */
public class SummarizedObs {

    private String encounterType;
    private String encounterTypeName;
    private String concept;
    private String conceptName;
    private String valueCoded;
    private String valueCodedName;
    private String reportName;
    private String period;
    private String patients;
    private Integer total;
    private String periodType;
    private Date dateGenerated;
    private String periodGroupedBy;

    public SummarizedObs() {
    }

    public SummarizedObs(String encounterType, String encounterTypeName, String concept, String conceptName, String valueCoded, String valueCodedName, String reportName, String period, String patients, Integer total, String periodType, Date dateGenerated, String periodGroupedBy) {
        this.encounterType = encounterType;
        this.encounterTypeName = encounterTypeName;
        this.concept = concept;
        this.conceptName = conceptName;
        this.valueCoded = valueCoded;
        this.valueCodedName = valueCodedName;
        this.reportName = reportName;
        this.period = period;
        this.patients = patients;
        this.total = total;
        this.periodType = periodType;
        this.dateGenerated = dateGenerated;
        this.periodGroupedBy = periodGroupedBy;
    }

    public String getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(String encounterType) {
        this.encounterType = encounterType;
    }

    public String getEncounterTypeName() {
        return encounterTypeName;
    }

    public void setEncounterTypeName(String encounterTypeName) {
        this.encounterTypeName = encounterTypeName;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    public String getValueCoded() {
        return valueCoded;
    }

    public void setValueCoded(String valueCoded) {
        this.valueCoded = valueCoded;
    }

    public String getValueCodedName() {
        return valueCodedName;
    }

    public void setValueCodedName(String valueCodedName) {
        this.valueCodedName = valueCodedName;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getPatients() {
        return patients;
    }

    public void setPatients(String patients) {
        this.patients = patients;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getPeriodType() {
        return periodType;
    }

    public void setPeriodType(String periodType) {
        this.periodType = periodType;
    }

    public Date getDateGenerated() {
        return dateGenerated;
    }

    public void setDateGenerated(Date dateGenerated) {
        this.dateGenerated = dateGenerated;
    }

    public String getPeriodGroupedBy() {
        return periodGroupedBy;
    }

    public void setPeriodGroupedBy(String periodGroupedBy) {
        this.periodGroupedBy = periodGroupedBy;
    }
}
