package org.openmrs.module.ugandaemrreports.definition.dataset.queries;

public class Queries {
    public static String ewiQuery(String startDate, String endDate) {
        return String.format("select person_id\n" +
                        "  from obs\n" +
                        "  where concept_id = 99161\n" +
                        "    and value_datetime between '%s' and '%s'\n" +
                        "  union\n" +
                        "  select patient_id\n" +
                        "  from encounter\n" +
                        "  where encounter_datetime between '%s' and '%s'\n" +
                        "    and encounter_type =\n" +
                        "        (select encounter_type_id from encounter_type et where et.uuid = '8d5b2be0-c2cc-11de-8d13-0010c6dffd0f');",
                startDate, endDate, startDate, endDate);
    }

    public static String ewiEncounterQuery(String startDate, String cohortString) {
        return String.format("select e.patient_id, e.encounter_id, DATE(e.encounter_datetime), DATE(o.value_datetime)\n" +
                "from encounter e\n" +
                "       left join obs o on (e.encounter_id = o.encounter_id)\n" +
                "where e.encounter_type = 9\n" +
                "  and e.encounter_datetime >= '%s'\n" +
                "  and o.concept_id = 5096\n" +
                "  and e.patient_id in (%s);", startDate, cohortString);
    }

    public static String ewiPatientDataQuery(String cohortString) {
        return String.format("select p.person_id,\n" +
                "       p.gender,\n" +
                "       p.birthdate,\n" +
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
                "        group by o.person_id) as transfer\n" +
                "from person p\n" +
                "       inner join patient_identifier pi on (p.person_id = pi.patient_id)\n" +
                "where pi.identifier_type = (select patient_identifier_type_id\n" +
                "                            from patient_identifier_type pit\n" +
                "                            where pit.uuid = 'e1731641-30ab-102d-86b0-7a5022ba4115')\n" +
                "  and p.person_id in (%s);", cohortString);
    }
}
