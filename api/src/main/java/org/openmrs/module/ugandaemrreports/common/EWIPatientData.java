package org.openmrs.module.ugandaemrreports.common;

public class EWIPatientData {
    private Integer personId;
    private String gender;
    private String dob;
    private String deathDate;
    private String artClinicNumber;
    private String artStartDate;
    private String transferOutDate;
    public EWIPatientData() {
    }
    public EWIPatientData(Integer personId, String gender, String dob, String deathDate, String artClinicNumber, String artStartDate, String transferOutDate) {
        this.personId = personId;
        this.gender = gender;
        this.dob = dob;
        this.deathDate = deathDate;
        this.artClinicNumber = artClinicNumber;
        this.artStartDate = artStartDate;
        this.transferOutDate = transferOutDate;
    }
    public Integer getPersonId() {
        return personId;
    }
    public void setPersonId(Integer personId) {
        this.personId = personId;
    }
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    public String getDob() {
        return dob;
    }
    public void setDob(String dob) {
        this.dob = dob;
    }
    public String getDeathDate() {
        return deathDate;
    }
    public void setDeathDate(String deathDate) {
        this.deathDate = deathDate;
    }
    public String getArtClinicNumber() {
        return artClinicNumber;
    }
    public void setArtClinicNumber(String artClinicNumber) {
        this.artClinicNumber = artClinicNumber;
    }
    public String getArtStartDate() {
        return artStartDate;
    }
    public void setArtStartDate(String artStartDate) {
        this.artStartDate = artStartDate;
    }
    public String getTransferOutDate() {
        return transferOutDate;
    }
    public void setTransferOutDate(String transferOutDate) {
        this.transferOutDate = transferOutDate;
    }
}