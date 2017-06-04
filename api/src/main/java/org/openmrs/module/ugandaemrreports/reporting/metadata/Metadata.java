package org.openmrs.module.ugandaemrreports.reporting.metadata;

import java.util.List;

import org.openmrs.Concept;

/**
 * Metadata for reporting functionality
 */
public class Metadata {

    public static class Concept{
        public final static String PREGNANT = "dcd695dc-30ab-102d-86b0-7a5022ba4115";
        public final static String YES_CIEL = "1065AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        public final static String YES_WHO = "dcd695dc-30ab-102d-86b0-7a5022ba4115";
        public final static String TRANSFER_IN = "ea730d69-7eec-486a-aaf2-54f8bab5a44c";
        public final static String CARE_ENTRY_POINT = "dcdfe3ce-30ab-102d-86b0-7a5022ba4115";
        public final static String CARE_ENTRY_POINT_EMTCT = "dcd7e8e5-30ab-102d-86b0-7a5022ba4115";
        
        
        public final static String ART_START_DATE = "ab505422-26d9-41f1-a079-c3d222000440";
        public final static String PREGNANT_AT_ART_START = "b253be65-0155-4b43-ad15-88bc797322c9";
        public final static String LACTATING_AT_ART_START = "ab7bb4db-1a54-4225-b71c-d8e138b471e9";
        
        public final static String CURRENT_REGIMEN = "dd2b0b4d-30ab-102d-86b0-7a5022ba4115";
    
        public final static String CHILDREN_FIRST_LINE_REGIMEN = "dd2b361c-30ab-102d-86b0-7a5022ba4115,dd2b3eee-30ab-102d-86b0-7a5022ba4115,dd2b84c5-30ab-102d-86b0-7a5022ba4115,dd2b8b27-30ab-102d-86b0-7a5022ba4115,25b0b83c-a7b8-4663-b727-0c03c982bab2,f99ef0fa-b299-4573-ae31-f4d09b1b69d5,f30e9dae-cc6a-4669-98d5-ad25b8a3ce9c,6cdbfee8-87bf-406c-8dc3-3a22d95e952c,583a954b-0cd5-4b69-aef6-87c281e03a55,14c56659-3d4e-4b88-b3ff-e2d43dbfb865";
    
        public final static String ADULT_FIRST_LINE_REGIMEN = "dd2b361c-30ab-102d-86b0-7a5022ba4115,dd2b3eee-30ab-102d-86b0-7a5022ba4115,dd2b84c5-30ab-102d-86b0-7a5022ba4115,dd2b8b27-30ab-102d-86b0-7a5022ba4115,012a1378-b005-4793-8ea0-d01fceea769d,25b0b83c-a7b8-4663-b727-0c03c982bab2,f99ef0fa-b299-4573-ae31-f4d09b1b69d5,20bcbf56-2784-4bf0-a6b4-23ba43764163,b3bd1d21-aa40-4e8a-959f-2903b358069c,6cdbfee8-87bf-406c-8dc3-3a22d95e952c,583a954b-0cd5-4b69-aef6-87c281e03a55,dcd68a88-30ab-102d-86b0-7a5022ba4115"; // Last Concept is Other Specify which includes any unknown drugs
        
        public final static String CHILDREN_SECOND_LINE_REGIMEN = "dd2b3eee-30ab-102d-86b0-7a5022ba4115,dd2b9181-30ab-102d-86b0-7a5022ba4115,dd2b97d3-30ab-102d-86b0-7a5022ba4115,dd2b9e11-30ab-102d-86b0-7a5022ba4115,b06bdb63-dd08-4b80-af5a-d17f6b3062a5,4b9c639e-3d06-4f2a-9c34-dd07e44f4fa6,4a608d68-516f-44d2-9e0b-1783dc0d870e,f30e9dae-cc6a-4669-98d5-ad25b8a3ce9c,f00e5ff7-73bb-4385-8ee1-ea7aa772ec3e,faf13d3c-7ca8-4995-ab29-749f3960b83d,d4393bd0-3a9e-4716-8968-1057c58c32bc,6cdbfee8-87bf-406c-8dc3-3a22d95e952c,583a954b-0cd5-4b69-aef6-87c281e03a55,fe78521e-eb7a-440f-912d-0eb9bf2d4b2c,14c56659-3d4e-4b88-b3ff-e2d43dbfb865";
        
        public final static String ADULT_SECOND_LINE_REGIMEN = "dd2b452c-30ab-102d-86b0-7a5022ba4115,dd2b4d82-30ab-102d-86b0-7a5022ba4115,dd2b9181-30ab-102d-86b0-7a5022ba4115,dd2b97d3-30ab-102d-86b0-7a5022ba4115,dd2b9e11-30ab-102d-86b0-7a5022ba4115,b06bdb63-dd08-4b80-af5a-d17f6b3062a5,4b9c639e-3d06-4f2a-9c34-dd07e44f4fa6,4a608d68-516f-44d2-9e0b-1783dc0d870e,f30e9dae-cc6a-4669-98d5-ad25b8a3ce9c,834625e9-3273-445e-be99-2beca081702c,942e427c-7a3b-49b6-97f3-5cdbfeb8d0e3,29439504-5f5d-49ac-b8e4-258adc08c67a,f00e5ff7-73bb-4385-8ee1-ea7aa772ec3e,faf13d3c-7ca8-4995-ab29-749f3960b83d,d4393bd0-3a9e-4716-8968-1057c58c32bc,fe78521e-eb7a-440f-912d-0eb9bf2d4b2c,25186d70-ed8f-486c-83e5-fc31cbe95630";
        
        public final static String THIRD_LINE_REGIMEN  = "607ffca4-6f15-4e85-b0a5-8226d4f25592,4c27fe52-98fd-4068-9e81-ea9caba4b583";
        
        public final static String INH_DOSAGE = "be211d29-1507-4e2e-9906-4bfeae4ddc1f";
        public final static String CPT_DAPSONE_PILLS_DISPENSED = "38801143-01ac-4328-b0e1-a7b23c84c8a3";
        public static final String ASSESSED_FOR_TB = "dce02aa1-30ab-102d-86b0-7a5022ba4115";
    
        public static final String DIAGNOSED_WITH_TB = "dcdac38b-30ab-102d-86b0-7a5022ba4115";
        public static final String TB_TREATMENT_START_DATE = "dce02eca-30ab-102d-86b0-7a5022ba4115";
        public static final String ON_TB_TREATMENT = "dcdaa6b4-30ab-102d-86b0-7a5022ba4115";
        public static final String ASSESSED_FOR_MALNUTRITION = "dc655734-30ab-102d-86b0-7a5022ba4115";

        public static final String FAMILY_PLANNING_METHOD = "dc7620b3-30ab-102d-86b0-7a5022ba4115";

    }
    public static class Identifier{

    }
    public static class EncounterType{
        public final static String ART_SUMMARY_PAGE = "8d5b27bc-c2cc-11de-8d13-0010c6dffd0f";
        public final static String ART_ENCOUNTER_PAGE = "8d5b2be0-c2cc-11de-8d13-0010c6dffd0f";
        public final static String ART_HEALTH_EDUCATION_PAGE = "6d88e370-f2ba-476b-bf1b-d8eaf3b1b67e";
        public final static String EID_SUMMARY_PAGE = "9fcfcc91-ad60-4d84-9710-11cc25258719";
        public final static String EID_ENCOUNTER_PAGE = "4345dacb-909d-429c-99aa-045f2db77e2b";


    }
    public static class Program{

    }
}
