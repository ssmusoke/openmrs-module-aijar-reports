package org.openmrs.module.ugandaemrreports.common;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;

/**
 * Created by carapai on 11/07/2017.
 */
public class NormalizedObs {
    private Integer obsId;
    private Integer personId;
    private String person;
    private String encounterType;
    private String encounterTypeName;
    private String form;
    private String formName;
    private String encounter;
    private String encounterDatetime;
    private Integer ageAtEncounter;
    private Integer encounterYear;
    private Integer encounterMonth;
    private String encounterQuarter;
    private String concept;
    private String conceptName;
    private Date obsDatetime;
    private Integer obsDatetimeYear;
    private Integer obsDatetimeMonth;
    private String obsDatetimeQuarter;
    private Integer ageAtObservation;
    private String location;
    private String locationName;
    private String obsGroup;
    private String valueGroup;
    private Integer valueCodedId;
    private Integer valueBoolean;
    private String valueCoded;
    private String valueCodedName1;
    private String reportName;
    private String valueCodedName;
    private String valueDrug;
    private Date valueDatetime;
    private Integer ageAtValueDatetime;
    private Integer valueDatetimeYear;
    private Integer valueDatetimeMonth;
    private String valueDatetimeQuarter;
    private Double valueNumeric;
    private String valueText;
    private Date dateCreated;

    public Integer getObsId() {
        return obsId;
    }

    public void setObsId(Integer obsId) {
        this.obsId = obsId;
    }

    public Integer getPersonId() {
        return personId;
    }

    public void setPersonId(Integer personId) {
        this.personId = personId;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
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

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getEncounter() {
        return encounter;
    }

    public void setEncounter(String encounter) {
        this.encounter = encounter;
    }

    public String getEncounterDatetime() {
        return encounterDatetime;
    }

    public void setEncounterDatetime(String encounterDatetime) {
        this.encounterDatetime = encounterDatetime;
    }

    public Integer getAgeAtEncounter() {
        return ageAtEncounter;
    }

    public void setAgeAtEncounter(Integer ageAtEncounter) {
        this.ageAtEncounter = ageAtEncounter;
    }

    public Integer getEncounterYear() {
        return encounterYear;
    }

    public void setEncounterYear(Integer encounterYear) {
        this.encounterYear = encounterYear;
    }

    public Integer getEncounterMonth() {
        return encounterMonth;
    }

    public void setEncounterMonth(Integer encounterMonth) {
        this.encounterMonth = encounterMonth;
    }

    public String getEncounterQuarter() {
        return encounterQuarter;
    }

    public void setEncounterQuarter(String encounterQuarter) {
        this.encounterQuarter = encounterQuarter;
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

    public Date getObsDatetime() {
        return obsDatetime;
    }

    public void setObsDatetime(Date obsDatetime) {
        this.obsDatetime = obsDatetime;
    }

    public Integer getObsDatetimeYear() {
        return obsDatetimeYear;
    }

    public void setObsDatetimeYear(Integer obsDatetimeYear) {
        this.obsDatetimeYear = obsDatetimeYear;
    }

    public Integer getObsDatetimeMonth() {
        return obsDatetimeMonth;
    }

    public void setObsDatetimeMonth(Integer obsDatetimeMonth) {
        this.obsDatetimeMonth = obsDatetimeMonth;
    }

    public String getObsDatetimeQuarter() {
        return obsDatetimeQuarter;
    }

    public void setObsDatetimeQuarter(String obsDatetimeQuarter) {
        this.obsDatetimeQuarter = obsDatetimeQuarter;
    }

    public Integer getAgeAtObservation() {
        return ageAtObservation;
    }

    public void setAgeAtObservation(Integer ageAtObservation) {
        this.ageAtObservation = ageAtObservation;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getObsGroup() {
        return obsGroup;
    }

    public void setObsGroup(String obsGroup) {
        this.obsGroup = obsGroup;
    }

    public String getValueGroup() {
        return valueGroup;
    }

    public void setValueGroup(String valueGroup) {
        this.valueGroup = valueGroup;
    }

    public Integer getValueBoolean() {
        return valueBoolean;
    }

    public void setValueBoolean(Integer valueBoolean) {
        this.valueBoolean = valueBoolean;
    }

    public String getValueCoded() {
        return valueCoded;
    }

    public void setValueCoded(String valueCoded) {
        this.valueCoded = valueCoded;
    }

    public String getValueCodedName1() {
        return valueCodedName1;
    }

    public void setValueCodedName1(String valueCodedName1) {
        this.valueCodedName1 = valueCodedName1;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getValueCodedName() {
        return valueCodedName;
    }

    public void setValueCodedName(String valueCodedName) {
        this.valueCodedName = valueCodedName;
    }

    public String getValueDrug() {
        return valueDrug;
    }

    public void setValueDrug(String valueDrug) {
        this.valueDrug = valueDrug;
    }

    public Integer getAgeAtValueDatetime() {
        return ageAtValueDatetime;
    }

    public void setAgeAtValueDatetime(Integer ageAtValueDatetime) {
        this.ageAtValueDatetime = ageAtValueDatetime;
    }

    public Integer getValueDatetimeYear() {
        return valueDatetimeYear;
    }

    public void setValueDatetimeYear(Integer valueDatetimeYear) {
        this.valueDatetimeYear = valueDatetimeYear;
    }

    public Integer getValueDatetimeMonth() {
        return valueDatetimeMonth;
    }

    public void setValueDatetimeMonth(Integer valueDatetimeMonth) {
        this.valueDatetimeMonth = valueDatetimeMonth;
    }

    public String getValueDatetimeQuarter() {
        return valueDatetimeQuarter;
    }

    public void setValueDatetimeQuarter(String valueDatetimeQuarter) {
        this.valueDatetimeQuarter = valueDatetimeQuarter;
    }

    public Double getValueNumeric() {
        return valueNumeric;
    }

    public void setValueNumeric(Double valueNumeric) {
        this.valueNumeric = valueNumeric;
    }

    public String getValueText() {
        return valueText;
    }

    public void setValueText(String valueText) {
        this.valueText = valueText;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Integer getValueCodedId() {
        return valueCodedId;
    }

    public void setValueCodedId(Integer valueCodedId) {
        this.valueCodedId = valueCodedId;
    }

    public Date getValueDatetime() {
        return valueDatetime;
    }

    public void setValueDatetime(Date valueDatetime) {
        this.valueDatetime = valueDatetime;
    }
}
