package org.openmrs.module.ugandaemrreports.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

public class ConditionOfMotherAtDischargeConverter  implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        Concept value = ((Obs)obj).getValueCoded();
        if(value != null && value.equals(Dictionary.getConcept("3f02ee62-613d-4eba-88bc-d0cd66b863c5"))) {
            return "D";
        }
        else if(value != null && value.equals(Dictionary.getConcept("17fcfd67-a1a2-4361-9915-ad4e81a7a61d"))) {
            return "DD";
        }
        else if(value != null && value.equals(Dictionary.getConcept("6e4f1db1-1534-43ca-b2a8-5c01bc62e7ef"))) {
            return "R ";
        }
        else if(value != null && value.equals(Dictionary.getConcept("dd27a783-30ab-102d-86b0-7a5022ba4115"))) {
            return "T";
        }
        else if(value != null && value.equals(Dictionary.getConcept("6d15f334-2130-47a8-b5a2-3b26b6a65c69"))) {
            return "DF";
        }
        else if(value != null && value.equals(Dictionary.getConcept("792fe1f6-262e-4266-9226-63fe74268279"))) {
            return "DDF";
        }
        else if(value != null && value.equals(Dictionary.getConcept("4095ac37-4955-4718-8e3e-b6f6fb55cf6d"))) {
            return "RF";
        }
        else if(value != null && value.equals(Dictionary.getConcept("862b583e-97ba-4bef-8997-5a460449a87a"))) {
            return "TF";
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

