package org.openmrs.module.aijarreports.library;

import org.openmrs.module.aijarreports.metadata.HIVMetadata;
import org.openmrs.module.reporting.data.converter.ConcatenatedPropertyConverter;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredAddressDataDefinition;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BasePatientDataLibrary extends BaseDefinitionLibrary<PatientDataDefinition> {

    @Autowired
    private DataFactory df;

    @Autowired
    private HIVMetadata metadata;

    @Override
    public String getKeyPrefix() {
        return "aijar.patientdata.";
    }

    @Override
    public Class<? super PatientDataDefinition> getDefinitionType() {
        return PatientDataDefinition.class;
    }

    @DocumentedDefinition("village")
    public PatientDataDefinition getVillage() {
        return df.getPreferredAddress("cityVillage");
    }

    @DocumentedDefinition("traditionalAuthority")
    public PatientDataDefinition getTraditionalAuthority() {
        return df.getPreferredAddress("countyDistrict");
    }

    @DocumentedDefinition("district")
    public PatientDataDefinition getDistrict() {
        return df.getPreferredAddress("stateProvince");
    }

    @DocumentedDefinition("addressFull")
    public PatientDataDefinition getAddressFull() {
        PreferredAddressDataDefinition pdd = new PreferredAddressDataDefinition();
        return df.convert(pdd, new ConcatenatedPropertyConverter(", ", "district", "traditionalAuthority", "village"));
    }
}
