package org.openmrs.module.ugandaemrreports.metadata;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Metadata definitions for the HIV related reporting
 */
@Component("tbMetadata")
public class TBMetadata extends Metadata {


    public Concept getUnitTBNo() {
        return getConcept("304df0d0-afe4-4a61-a917-d684b100a65a");
    }

    public Concept getHSDTBNo() {
        return getConcept("d1cda288-4853-4450-afbc-76bd4e65ea70");
    }

    public Concept getDistrictTBNo() {
        return getConcept("67e9ec2f-4c72-408b-8122-3706909d77ec");
    }

    public Concept getContactPerson() {
        return getConcept("163258AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    }

    public Concept getContactPhoneNumber() {
        return getConcept("159635AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    }

    public Concept getRelationshipOfContactPerson() {
        return getConcept("dce138f8-30ab-102d-86b0-7a5022ba4115");
    }

    public Concept getIsPatientAHealthWorker() {
        return getConcept("5619AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    }

    public Concept getCadreOfHealthWorker() {
        return getConcept("1783AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    }

    public Concept getCurrentTBRegimen() {
        return getConcept("16fd7307-0b26-4c8b-afa3-8362baff4042");
    }

    public Concept getDiseaseClassification() {
        return getConcept("d45871ee-62d6-4d4d-b905-f7b75a3fd3bb");
    }
    
    public Concept getTransferInLocation() {
        return getConcept("88e07e7c-f7e4-4ccf-8068-b770a3e3957b");
    }

    public Concept getPatientType() {
        return getConcept("d45871ee-62d6-4d4d-b905-f7b75a3fd3bb");
    }

    public Concept getBacteriologicallyConfirmed() {
        return getConcept("d7134dc4-ca6f-4cf4-b085-3f13ede07a54");
    }

    public Concept getClinicallyDiagnosed() {
        return getConcept("b997423b-eb7b-4a79-bfd9-2b06afc8377c");
    }

    public Concept getEPTB() {
        return getConcept("fa8e6d5a-759f-4e94-b558-a81a6b3af4dc");
    }

    public Concept getDateStartedTBTreatment() {
        return getConcept("7326297e-0ccd-4355-9b86-dde1c056e2c2");
    }



    public PatientIdentifierType getTBIdentifier() {
        return getPatientIdentifierType("8110f2d2-1f98-4c38-aef3-11b19bb0a589");
    }

    public List<EncounterType> getTBEnrollmentEncounterType() {
        List<EncounterType> l = new ArrayList<EncounterType>();
        l.add(MetadataUtils.existing(EncounterType.class, "334bf97e-28e2-4a27-8727-a5ce31c7cd66"));
        return l;
    }

    public Concept getTBStartDate() {
        return getConcept("dce02eca-30ab-102d-86b0-7a5022ba4115");
    }

    public Concept getTBStopDate() {
        return getConcept("dd2adde2-30ab-102d-86b0-7a5022ba4115");
    }


    public Concept getTBStatus() {
        return getConcept("dce02aa1-30ab-102d-86b0-7a5022ba4115");
    }

    public Concept getTBStatusDiagnosed() {
        return getConcept("dcdac38b-30ab-102d-86b0-7a5022ba4115");
    }

    public Concept getTBStatusRx() {
        return getConcept("dcdaa6b4-30ab-102d-86b0-7a5022ba4115");
    }

    public Concept getNewTBPatient() {
        return getConcept("b3c43c5e-1987-42c1-a7b3-2c71dc58c126");
    }

    public Concept getRelapsedTBPatient() {
        return getConcept("8ad53c8c-e136-41e3-aab8-eace935a3bbe");
    }

    public Concept getFailedTBPatient() {
        return getConcept("8ad53c8c-e136-41e3-aab8-eace935a3bbe");
    }

    public Concept getLostToFollowupTBPatient() {
        return getConcept("a37462b6-1f47-4efb-8df5-2bdc742efc17");
    }

    public Concept getTBPatientWithUnknownTreatmentHistory() {
        return getConcept("1067AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    }

    public Concept getHIVStatusCategory() {
        return getConcept("5737ab4e-53f9-418e-94f4-35da78ab884f");
    }

    public Concept getNegativeHIVStatus() { return getConcept("dcdf4653-30ab-102d-86b0-7a5022ba4115"); }
    public Concept getNewlyPositiveHIVStatus() { return getConcept("f72917e2-0bfb-4d73-b87b-643d7180f338"); }
    public Concept getKnownPositiveHIVStatus() { return getConcept("6e46ac6b-944d-4a66-a994-b99ae90d4fed"); }
    public Concept getUnknownHIVStatus() { return getConcept("dcd6865a-30ab-102d-86b0-7a5022ba4115"); }

    public Concept getTypeOfPatient(){
        return getConcept("e077f196-c19a-417f-adc6-b175a3343bfd");
    }
    public Concept getNewPatientType(){
        return getConcept("b3c43c5e-1987-42c1-a7b3-2c71dc58c126");
    }
    public Concept getRelapsedPatientType(){
        return getConcept("8ad53c8c-e136-41e3-aab8-eace935a3bbe");
    }
    public Concept getTreatmentAfterLTFPPatientType(){
        return getConcept("13678f4d-69d4-4a93-a2f2-a7d21aadd1f9");
    }
    public Concept getTreatmentAfterFailurePatientType(){
        return getConcept("fa87a1c0-0315-4e95-a868-7394e2429d6d");
    }
    public Concept getTreatmentHistoryUnknownPatientType(){ return getConcept("d493a2d8-f519-4a57-9458-7962e61f4398"); }

    public Concept getHIVCounsellingAndTesting(){
        return getConcept("4395c8b9-2aaa-46b2-a228-5968e42bf085");
    }


    public List<Concept> getNewAndRelapsedTBPatients() {
        return getConceptList("b3c43c5e-1987-42c1-a7b3-2c71dc58c126,8ad53c8c-e136-41e3-aab8-eace935a3bbe");
    }

    public List<Concept> getTBPatientTypes() {
        return getConceptList("b3c43c5e-1987-42c1-a7b3-2c71dc58c126, 8ad53c8c-e136-41e3-aab8-eace935a3bbe, 7f5420a6-2c1a-4bb9-b6cf-8ed565933669,a37462b6-1f47-4efb-8df5-2bdc742efc17, 1067AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    }

    public Concept getTreatmentModel(){
        return getConcept("9e4e93fc-dcc0-4d36-9738-c0a5a489baa1");
    }

    public Concept getFacilityDOTsTreatmentModel(){
        return getConcept("f9cdc841-1901-407e-b3ca-65dd3762051d");
    }

    public Concept getDigitalCommunityDOTsTreatmentModel(){ return getConcept("ad6416c2-ca66-45e7-bfa0-9c03df0ab761"); }
    public Concept getNonDigitalCommunityDOTsTreatmentModel(){ return getConcept("89753861-f946-4c2a-9aeb-e6cabbb2dc25"); }
    public Concept getRiskGroup(){ return getConcept("927563c5-cb91-4536-b23c-563a72d3f829"); }

}
