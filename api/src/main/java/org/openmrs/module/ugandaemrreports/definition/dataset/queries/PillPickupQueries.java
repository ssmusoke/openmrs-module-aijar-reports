package org.openmrs.module.ugandaemrreports.definition.dataset.queries;

public class PillPickupQueries {

    public static String ewiPillPickupQuery(String startDate, String endDate) {
        return String.format("SELECT t.person_id FROM obs t inner join encounter e on t.encounter_id = e.encounter_id\n" +
                        "where t.obs_datetime between '%s' and '%s'\n" +
                        "  and e.encounter_type =(select encounter_type_id from encounter_type et where et.uuid = '8d5b2be0-c2cc-11de-8d13-0010c6dffd0f') and\n" +
                        "t.concept_id = (select c.concept_id from concept c where c.uuid='dd2b0b4d-30ab-102d-86b0-7a5022ba4115') and t.voided = 0 group by t.person_id union\n" +
                        "select o.person_id  from obs o where o.value_datetime between '%s' and '%s' and o.concept_id = 99161 and o.voided = 0 union\n" +
                        "select obs.person_id from obs obs where  obs.concept_id = 99160 and obs.value_datetime between '%s' and '%s' and obs.voided =0;",
                startDate, endDate,startDate, endDate,startDate, endDate);
    }
    public static String ewiPillPickupEncounterQuery(String startDate, String cohortString) {
        return String.format("select e.patient_id, e.encounter_id, DATE(e.encounter_datetime), DATE(o.value_datetime)\n" +
                "from encounter e\n" +
                "       left join obs o on (e.encounter_id = o.encounter_id)\n" +
                "where e.encounter_type = 9\n" +
                "  and e.encounter_datetime >= '%s'\n" +
                "  and o.concept_id = 5096\n" +
                "  and e.patient_id in (%s);", startDate, cohortString);
    }

    public static String ewiPillPickupBaselinePickupQuery(String startDate,String endDate,String cohortString){
        return String.format("select e.patient_id,GROUP_CONCAT(DATE (e.encounter_datetime)order  by e.encounter_datetime asc separator ',')as baselinepickup from encounter e left\n" +
                        "join obs o on (e.encounter_id = o.encounter_id) where e.encounter_datetime between '%s' and '%s' and\n" +
                        " e.encounter_type =(select encounter_type_id from encounter_type et where et.uuid = '8d5b2be0-c2cc-11de-8d13-0010c6dffd0f') and\n" +
                        " o.concept_id = (select c.concept_id from concept c where c.uuid='dd2b0b4d-30ab-102d-86b0-7a5022ba4115') and o.voided = 0  and e.patient_id in (%s) group by patient_id;"
                ,startDate,endDate,cohortString);
    }

    public static  String ewiNumberOfDaysPickedAtBaselinePickup(String startDate, String endDate,String cohortString){
        return String.format("select e.patient_id,obs.value_numeric  from obs obs inner join  " +
                "encounter e on obs.encounter_id = e.encounter_id inner join\n" +
                "(select e.patient_id, GROUP_CONCAT(DATE (e.encounter_datetime)order  by e.encounter_datetime asc separator ',')as baselinepickup" +
                " from encounter e left\n" +
                "join obs o on (e.encounter_id = o.encounter_id) where e.encounter_datetime between '%s' and '%s' and\n" +
                " e.encounter_type =(select encounter_type_id from encounter_type et where et.uuid = '8d5b2be0-c2cc-11de-8d13-0010c6dffd0f') " +
                "and\n" +
                " o.concept_id = (select c.concept_id from concept c where c.uuid='dd2b0b4d-30ab-102d-86b0-7a5022ba4115') " +
                "and o.voided = 0 and e.patient_id in (%s) group by e.patient_id\n" +
                ") temp on e.patient_id=temp.patient_id and e.encounter_datetime=substring_index(temp.baselinepickup,',',1)" +
                " where obs.concept_id=99036 and obs.voided=0\n" +
                "group by e.patient_id;",startDate,endDate,cohortString);
    }
    public static String ewiPillPickupPatientDataQuery(String startDate, String endDate, String cohortString) {
        return String.format("select p.person_id,\n" +
                "       p.gender,\n" +
                "      DATE(p.birthdate) ,\n" +
                "       p.death_date,\n" +
                "       pi.identifier,\n" +
                "       (select DATE(o.value_datetime)\n" +
                "        from obs o\n" +
                "        where o.person_id = p.person_id\n" +
                "          and o.concept_id = 99161\n" +
                "          and o.voided = 0\n" +
                "        group by o.person_id) as ART,\n" +
                "       (select DATE(o.value_datetime)\n" +
                "        from obs o\n" +
                "        where o.person_id = p.person_id\n" +
                "          and o.concept_id = 99165\n" +
                "          and o.voided = 0\n" +
                "        and o.value_datetime between '%s' and '%s' group by o.person_id) as transfer\n" +
                "from person p\n" +
                "       inner join patient_identifier pi on (p.person_id = pi.patient_id)\n" +
                "where pi.identifier_type = (select patient_identifier_type_id\n" +
                "                            from patient_identifier_type pit\n" +
                "                            where pit.uuid = 'e1731641-30ab-102d-86b0-7a5022ba4115')\n" +
                "  and p.person_id in (%s);",startDate,endDate, cohortString);
    }
}
