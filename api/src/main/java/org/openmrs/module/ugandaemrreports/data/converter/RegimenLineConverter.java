package org.openmrs.module.ugandaemrreports.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;

public class RegimenLineConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        Concept value = ((Obs) obj).getValueCoded();
        String string = (value.getUuid());
        Integer patientAge = ((Obs) obj).getPerson().getAge();

        if (patientAge < 14) {
            String RegimenLine = "";
            switch (string) {
                case "dd2b361c-30ab-102d-86b0-7a5022ba4115":
                case "dd2b3eee-30ab-102d-86b0-7a5022ba4115":
                case "14c56659-3d4e-4b88-b3ff-e2d43dbfb865":
                case "583a954b-0cd5-4b69-aef6-87c281e03a55":
                case "6cdbfee8-87bf-406c-8dc3-3a22d95e952c":
                case "f30e9dae-cc6a-4669-98d5-ad25b8a3ce9c":
                case "f99ef0fa-b299-4573-ae31-f4d09b1b69d5":
                    RegimenLine = "FirstLine";
                    break;
                case "dd2b9181-30ab-102d-86b0-7a5022ba4115":
                case "fe78521e-eb7a-440f-912d-0eb9bf2d4b2c":
                case "d4393bd0-3a9e-4716-8968-1057c58c32bc":
                case "dd2b9e11-30ab-102d-86b0-7a5022ba4115":
                case "b06bdb63-dd08-4b80-af5a-d17f6b3062a5":
                case "4b9c639e-3d06-4f2a-9c34-dd07e44f4fa6":
                case "4a608d68-516f-44d2-9e0b-1783dc0d870e":
                case "f00e5ff7-73bb-4385-8ee1-ea7aa772ec3e":
                case "faf13d3c-7ca8-4995-ab29-749f3960b83d":
                    RegimenLine = "Second Line";
                    break;


            }
            return RegimenLine;

        } else {
            String RegimenLine = "";
            switch (string) {
                case "dd2b361c-30ab-102d-86b0-7a5022ba4115":
                case "012a1378-b005-4793-8ea0-d01fceea769d":
                case "dd2b8b27-30ab-102d-86b0-7a5022ba4115":
                case "dd2b84c5-30ab-102d-86b0-7a5022ba4115":
                case "25b0b83c-a7b8-4663-b727-0c03c982bab2":
                case "dd2b3eee-30ab-102d-86b0-7a5022ba4115":
                case "20bcbf56-2784-4bf0-a6b4-23ba43764163":
                    RegimenLine = "First Line";
                    break;
                case "d4393bd0-3a9e-4716-8968-1057c58c32bc":
                case "4b9c639e-3d06-4f2a-9c34-dd07e44f4fa6":
                case "29439504-5f5d-49ac-b8e4-258adc08c67a":
                case "942e427c-7a3b-49b6-97f3-5cdbfeb8d0e3":
                case "f30e9dae-cc6a-4669-98d5-ad25b8a3ce9c":
                    RegimenLine = "Second Line";
                    break;
                case "607ffca4-6f15-4e85-b0a5-8226d4f25592":
                case "4c27fe52-98fd-4068-9e81-ea9caba4b583":
                case "583a954b-0cd5-4b69-aef6-87c281e03a55":
                case "6cdbfee8-87bf-406c-8dc3-3a22d95e952c":
                    RegimenLine = "Third Line";
                    break;
            }
            return RegimenLine;

        }
    }




    @Override
    public Class<?> getInputDataType () {
        return Obs.class;
    }
    @Override
    public Class<?> getDataType() {
        return String.class;
    }
}
