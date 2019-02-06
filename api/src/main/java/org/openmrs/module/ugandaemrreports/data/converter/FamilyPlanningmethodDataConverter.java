package org.openmrs.module.ugandaemrreports.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

public class FamilyPlanningmethodDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        Concept value = ((Obs)obj).getValueCoded();
        if(value != null && value.equals(Dictionary.getConcept("dcb2fba9-30ab-102d-86b0-7a5022ba4115"))) {
            return "PP - BTL";
        }
        else if(value != null && value.equals(Dictionary.getConcept("fed07c37-7bb6-4baa-adf9-596ce4c4e93c"))) {
            return "PP-IUD";
        }
        else if(value != null && value.equals(Dictionary.getConcept("dd4c3016-13cf-458a-8e93-fe54460be667"))) {
            return "PAC - IUD";
        }
        else if(value != null && value.equals(Dictionary.getConcept("bb83fd9d-24c5-4d49-89c0-97e13c792aaf"))) {
            return "IMPLANT";
        }
        else if(value != null && value.equals(Dictionary.getConcept("efbe5bf3-3411-4949-855b-636ada05f5e7"))) {
            return "COS";
        }
        else if(value != null && value.equals(Dictionary.getConcept("82624AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))) {
            return "POP";
        }
        else if(value != null && value.equals(Dictionary.getConcept("336650b2-65f7-4202-80eb-3c6437878262"))) {
            return "ORAL PILLS";
        }
        else if(value != null && value.equals(Dictionary.getConcept("3e18cafc-8edc-4648-94b3-835de371a2f2"))) {
            return "DEPO";
        }

        else if(value != null && value.equals(Dictionary.getConcept("aaf150a5-92d2-416f-8254-95d34ed9c4ab"))) {
            return "NO FAMILY PLANNING GIVEN";
        }

        else if(value != null && value.equals(Dictionary.getConcept("dc692ad3-30ab-102d-86b0-7a5022ba4115"))) {
            return "CONDOMS";
        }
        else if(value != null && value.equals(Dictionary.getConcept("aa14bbbb-cbbe-445d-8958-9f521220b0fd"))) {
            return "Moon beads";
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
