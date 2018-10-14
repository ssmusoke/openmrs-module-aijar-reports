package org.openmrs.module.ugandaemrreports.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

public class FinalDiagnosisDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        Concept value = ((Obs) obj).getValueCoded();
        if (value != null && value.equals(Dictionary.getConcept("164917"))) {
            return "Abortion due to other causes";
        } else if (value != null && value.equals(Dictionary.getConcept("120295"))) {
            return "Complete";
        }
        else if (value != null && value.equals(Dictionary.getConcept("112416"))) {
            return "Threatening";
        }
        else if (value != null && value.equals(Dictionary.getConcept("164918"))) {
            return "Uknown cause of abortion";
        }
        else if (value != null && value.equals(Dictionary.getConcept("dc69f6f5-30ab-102d-86b0-7a5022ba4115"))) {
            return "APH";
        }

        else if (value != null && value.equals(Dictionary.getConcept("130108"))) {
            return "Placental abruption";
        }
        else if (value != null && value.equals(Dictionary.getConcept("114127"))) {
            return "Placenta praevia";
        }
        else if (value != null && value.equals(Dictionary.getConcept("130123"))) {
            return "Placenta  percreta";
        }

        else if (value != null && value.equals(Dictionary.getConcept("dc650021-30ab-102d-86b0-7a5022ba4115"))) {
            return "PPH";
        }
        else if (value != null && value.equals(Dictionary.getConcept("126877"))) {
            return "Secondary";
        }
        else if (value != null && value.equals(Dictionary.getConcept("dc55064a-30ab-102d-86b0-7a5022ba4115"))) {
            return "High blood pressure in pregnancy";
        }
        else if (value != null && value.equals(Dictionary.getConcept("118744"))) {
            return "Eclampsia";
        }
        else if (value != null && value.equals(Dictionary.getConcept("129251"))) {
            return "Pre-eclampsia";
        }
        else if (value != null && value.equals(Dictionary.getConcept("113006"))) {
            return "Severe pre-eclampsia";
        }
        else if (value != null && value.equals(Dictionary.getConcept("dc6504d3-30ab-102d-86b0-7a5022ba4115"))) {
            return "Abortions";
        }
        else if (value != null && value.equals(Dictionary.getConcept("113006"))) {
            return "Severe pre-eclampsia";
        }
        else if (value != null && value.equals(Dictionary.getConcept("135361AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))) {
            return "Malaria in pregnancy";
        }
        else if (value != null && value.equals(Dictionary.getConcept("115036AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))) {
            return "Obstructed Labour";
        }
        else if (value != null && value.equals(Dictionary.getConcept("130AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))) {
            return "Puerperal sepsis";
        }
        else if (value != null && value.equals(Dictionary.getConcept("dc69ec17-30ab-102d-86b0-7a5022ba4115"))) {
            return "Sepsis related to pregnancy";
        }

        else if (value != null && value.equals(Dictionary.getConcept("148834AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))) {
            return "Anaemia in pregnancy";
        }
        else if (value != null && value.equals(Dictionary.getConcept("127259AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))) {
            return "Ruptured uterus";
        }
        else if (value != null && value.equals(Dictionary.getConcept("dc550257-30ab-102d-86b0-7a5022ba4115"))) {
            return "Ectopic pregnancy";
        }
        else if (value != null && value.equals(Dictionary.getConcept("164916"))) {
            return "Abortion due to Gender based Violence";
        }
        else if (value != null && value.equals(Dictionary.getConcept("129211"))) {
            return "Premature rapture of Membranes";
        }
        else if (value != null && value.equals(Dictionary.getConcept("142478AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))) {
            return "Pregnancy induced diabetes melitus";
        }
        else if (value != null && value.equals(Dictionary.getConcept("5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))) {
            return "Other";
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
