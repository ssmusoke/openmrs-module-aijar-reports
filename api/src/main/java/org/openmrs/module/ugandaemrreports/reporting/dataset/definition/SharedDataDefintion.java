/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.ugandaemrreports.reporting.dataset.definition;

import org.openmrs.Concept;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculation;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.ObsForPersonDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.ugandaemrreports.definition.data.definition.CalculationDataDefinition;
import org.openmrs.module.ugandaemrreports.reporting.calculation.ProviderNameCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.AgeLimitCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.PersonAddressCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.WhoCd4VLCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.*;
import org.openmrs.module.ugandaemrreports.reporting.calculation.pnc.RtwRfwCalculation;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.NameOfHealthUnitDatasetDefinition;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 */
@Component
public class SharedDataDefintion {

    DataConverter identifierFormatter = new ObjectFormatter("{identifier}");

    public DataDefinition definition(String name, Concept concept) {
        ObsForPersonDataDefinition obsForPersonDataDefinition = new ObsForPersonDataDefinition();
        obsForPersonDataDefinition.setName(name);
        obsForPersonDataDefinition.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        obsForPersonDataDefinition.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        obsForPersonDataDefinition.setQuestion(concept);
        obsForPersonDataDefinition.setWhich(TimeQualifier.LAST);
        return obsForPersonDataDefinition;
    }
    public DataDefinition getPreARTNumber()
    {
        PatientIdentifierType preARTNo = MetadataUtils.existing(PatientIdentifierType.class, "e1731641-30ab-102d-86b0-7a5022ba4115");
        DataDefinition identifierDefPre = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(preARTNo.getName(), preARTNo), identifierFormatter);
        return identifierDefPre;
    }
    public PersonAttributeType getPatientNationality()
    {
        PersonAttributeType nationality = Context.getPersonService().getPersonAttributeTypeByUuid("dec484be-1c43-416a-9ad0-18bd9ef28929");
         return nationality;
    }
    public PersonAttributeType getPhoneNumber()
    {
        PersonAttributeType phoneNumber = Context.getPersonService().getPersonAttributeTypeByUuid("14d4f066-15f5-102d-96e4-000c29c2a5d7");
         return phoneNumber;
    }
    public PersonAttributeType getAlternatePhoneNumber()
    {
        PersonAttributeType alternatePhonenumber = Context.getPersonService().getPersonAttributeTypeByUuid("553834ef-b3fe-4c79-826a-6d4b6978bcff");
        return alternatePhonenumber;
    }
    public  DataDefinition getNationalIDNumber()
    {
        PatientIdentifierType nationalIdentifiernumber = MetadataUtils.existing(PatientIdentifierType.class, "f0c16a6d-dc5f-4118-a803-616d0075d282");
        DataDefinition nationalID = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(nationalIdentifiernumber.getName(), nationalIdentifiernumber), identifierFormatter);
         return nationalID;
    }

    public Concept getConcept(String uuid) {
        return Dictionary.getConcept(uuid);
    }

    public DataDefinition getVillageAndParish(){
        CalculationDataDefinition cdf =new CalculationDataDefinition("village+parish", new PersonAddressCalculation());
        cdf.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cdf;
    }
    public DataDefinition getAgeDataDefinition(Integer lower, Integer upper) {
        CalculationDataDefinition cdf = new CalculationDataDefinition("Age-"+lower+"-"+upper+"yrs", new AgeLimitCalculation());
        cdf.addCalculationParameter("lowerLimit", lower);
        cdf.addCalculationParameter("upperLimit", upper);
        cdf.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cdf;
    }
    public DataDefinition getWHOCD4ViralLoadCalculation(String q, String a){
        CalculationDataDefinition cd = new CalculationDataDefinition("", new WhoCd4VLCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        cd.addCalculationParameter("question", q);
        cd.addCalculationParameter("answer", a);
        return cd;
    }

    public DataDefinition referredToOrFrom(){
        CalculationDataDefinition cd = new CalculationDataDefinition("RTW/RFW", new RtwRfwCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;
    }
    public DataDefinition getNameofProvideratDelivery() {
        CalculationDataDefinition cd = new CalculationDataDefinition("Delivered By", new ProviderNameCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));

        return cd;
    }

    public DataDefinition getBloodPressure(){
        CalculationDataDefinition cdf = new CalculationDataDefinition("bp", new BloodPressureCalculation());
        cdf.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cdf;
    }

    public DataDefinition getIronGiven() {
        CalculationDataDefinition cd = new CalculationDataDefinition("Iron given", new IronGivenCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;
    }

    public PersonName getPersonNamesByProviderUUID(String providerUUID) {
        return Context.getProviderService().getProviderByUuid(providerUUID).getPerson().getPersonName();
    }
    public DataDefinition getFolicAcidGiven() {
        CalculationDataDefinition cd = new CalculationDataDefinition("Folic acid given", new FolicAcidCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;
    }
    public DataDefinition getEncounterDate(String date, PatientCalculation encounterDateCalculation) {
        CalculationDataDefinition cd = new CalculationDataDefinition(date, encounterDateCalculation);
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;
    }

    public DataSetDefinition healthFacilityName() {
        NameOfHealthUnitDatasetDefinition dsd = new NameOfHealthUnitDatasetDefinition();
        dsd.setFacilityName("aijar.healthCenterName");
        return dsd;
    }
}
