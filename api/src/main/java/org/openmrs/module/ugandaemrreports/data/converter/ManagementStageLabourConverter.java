package org.openmrs.module.ugandaemrreports.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

public class ManagementStageLabourConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        Concept value = ((Obs) obj).getValueCoded();
        if (value != null && value.equals(Dictionary.getConcept("e123d685-812a-43c3-bc05-db4e14d8c05c"))) {
            return "Ergometrine";
        } else if (value != null && value.equals(Dictionary.getConcept("eca9da28-31d3-4e6f-828d-441e9237b7a5"))) {
            return "Oxtocin (Pitocin)";
        } else if (value != null && value.equals(Dictionary.getConcept("1c4323a3-6cc6-44d0-81ee-839014bca19c"))) {
            return "Misoprostol";
        }
        return null;
    }
    @Override
    public Class<?> getInputDataType() {
        return Obs.class;
    }

    @Override
    public Class<?> getDataType() {
        return String.class;
    }

}
