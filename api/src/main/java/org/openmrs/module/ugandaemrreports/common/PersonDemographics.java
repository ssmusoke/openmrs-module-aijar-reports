package org.openmrs.module.ugandaemrreports.common;

/**
 * Created by carapai on 26/07/2017.
 */
public class PersonDemographics {
    private Integer personId;
    private String gender;
    private String birthDate;
    private String identifiers;
    private String attributes;
    private String names;
    private String addresses;

    public PersonDemographics() {
    }

    public PersonDemographics(Integer personId, String gender, String birthDate, String identifiers, String attributes, String names, String addresses) {
        this.personId = personId;
        this.gender = gender;
        this.birthDate = birthDate;
        this.identifiers = identifiers;
        this.attributes = attributes;
        this.names = names;
        this.addresses = addresses;
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

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }



    public String getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(String identifiers) {
        this.identifiers = identifiers;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public String getAddresses() {
        return addresses;
    }

    public void setAddresses(String addresses) {
        this.addresses = addresses;
    }
}
