package org.openmrs.module.ugandaemrreports.common;

/**
 * Created by carapai on 19/01/2017.
 */
public class ArtPatientData {
    private String encounterId;
    private String patientId;
    private String conceptId;
    private String valueCoded;
    private String encounterDate;
    private String valueDatetime;
    private String valueNumeric;
    private String valueText;
    private String encounterYear;
    private String valueDatetimeYear;
    private String encounterMonth;
    private String valueDatetimeMonth;

    public ArtPatientData() {
    }

    public ArtPatientData(String encounterId, String patientId, String conceptId, String valueCoded, String encounterDate, String valueDatetime, String valueNumeric, String valueText, String encounterYear, String valueDatetimeYear, String encounterMonth, String valueDatetimeMonth) {
        this.encounterId = encounterId;
        this.patientId = patientId;
        this.conceptId = conceptId;
        this.valueCoded = valueCoded;
        this.encounterDate = encounterDate;
        this.valueDatetime = valueDatetime;
        this.valueNumeric = valueNumeric;
        this.valueText = valueText;
        this.encounterYear = encounterYear;
        this.valueDatetimeYear = valueDatetimeYear;
        this.encounterMonth = encounterMonth;
        this.valueDatetimeMonth = valueDatetimeMonth;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }

    public String getConceptId() {
        return conceptId;
    }

    public void setConceptId(String conceptId) {
        this.conceptId = conceptId;
    }

    public String getValueCoded() {
        return valueCoded;
    }

    public void setValueCoded(String valueCoded) {
        this.valueCoded = valueCoded;
    }

    public String getEncounterDate() {
        return encounterDate;
    }

    public void setEncounterDate(String encounterDate) {
        this.encounterDate = encounterDate;
    }

    public String getValueDatetime() {
        return valueDatetime;
    }

    public void setValueDatetime(String valueDatetime) {
        this.valueDatetime = valueDatetime;
    }

    public String getValueNumeric() {
        return valueNumeric;
    }

    public void setValueNumeric(String valueNumeric) {
        this.valueNumeric = valueNumeric;
    }

    public String getValueText() {
        return valueText;
    }

    public void setValueText(String valueText) {
        this.valueText = valueText;
    }

    public String getEncounterYear() {
        return encounterYear;
    }

    public void setEncounterYear(String encounterYear) {
        this.encounterYear = encounterYear;
    }

    public String getEncounterMonth() {
        return encounterMonth;
    }

    public void setEncounterMonth(String encounterMonth) {
        this.encounterMonth = encounterMonth;
    }

    public String getValueDatetimeYear() {
        return valueDatetimeYear;
    }

    public void setValueDatetimeYear(String valueDatetimeYear) {
        this.valueDatetimeYear = valueDatetimeYear;
    }

    public String getValueDatetimeMonth() {
        return valueDatetimeMonth;
    }

    public void setValueDatetimeMonth(String valueDatetimeMonth) {
        this.valueDatetimeMonth = valueDatetimeMonth;
    }
}
