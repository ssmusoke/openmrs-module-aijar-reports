package org.openmrs.module.ugandaemrreports.library;

import org.openmrs.PersonAttribute;
import org.openmrs.module.reporting.data.converter.ConcatenatedPropertyConverter;
import org.openmrs.module.reporting.data.converter.PropertyConverter;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredAddressDataDefinition;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.metadata.CommonReportMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BasePatientDataLibrary extends BaseDefinitionLibrary<PatientDataDefinition> {

    @Autowired
    private DataFactory df;

    @Autowired
    private CommonReportMetadata commonReportMetadata;

    @Override
    public String getKeyPrefix() {
        return "ugemr.patientdata.";
    }

    @Override
    public Class<? super PatientDataDefinition> getDefinitionType() {
        return PatientDataDefinition.class;
    }

    public PatientDataDefinition getVillage() {
        return df.getPreferredAddress("address5");
    }

    public PatientDataDefinition getCounty() {
        return df.getPreferredAddress("stateProvince");
    }
    
    public PatientDataDefinition getSubcounty() {
        return df.getPreferredAddress("address3");
    }
    
    public PatientDataDefinition getParish() {
        return df.getPreferredAddress("adddress4");
    }

    public PatientDataDefinition getDistrict() {
        return df.getPreferredAddress("countyDistrict");
    }

    public PatientDataDefinition getAddressFull() {
        PreferredAddressDataDefinition pdd = new PreferredAddressDataDefinition();
        return df.convert(pdd, new ConcatenatedPropertyConverter(", ", "district", "county", "subcounty", "parish", "village"));
    }

    public PatientDataDefinition getTelephone() {
        PersonAttributeDataDefinition personAttributeDataDefinition = PatientColumns.createAttributeForPersonData("telephone", commonReportMetadata.getTelephone());
        return df.convert(personAttributeDataDefinition, new PropertyConverter(PersonAttribute.class, "value"));
    }
}
