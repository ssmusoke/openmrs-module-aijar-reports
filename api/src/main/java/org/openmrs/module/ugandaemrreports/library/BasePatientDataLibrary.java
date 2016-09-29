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
        return df.getPreferredAddress("cityVillage");
    }

    public PatientDataDefinition getTraditionalAuthority() {
        return df.getPreferredAddress("countyDistrict");
    }

    public PatientDataDefinition getDistrict() {
        return df.getPreferredAddress("stateProvince");
    }

    public PatientDataDefinition getAddressFull() {
        PreferredAddressDataDefinition pdd = new PreferredAddressDataDefinition();
        return df.convert(pdd, new ConcatenatedPropertyConverter(", ", "district", "traditionalAuthority", "village"));
    }

    public PatientDataDefinition getTelephone() {
        PersonAttributeDataDefinition personAttributeDataDefinition = PatientColumns.createAttributeForPersonData("telephone", commonReportMetadata.getTelephone());
        return df.convert(personAttributeDataDefinition, new PropertyConverter(PersonAttribute.class, "value"));
    }
}
