-- $BEGIN
INSERT INTO mamba_fact_patients_latest_vl_after_iac(client_id,
                                                    encounter_date,
                                                    results)
SELECT o.person_id,obs_datetime, cn.name
FROM obs o
         INNER JOIN encounter e ON o.encounter_id = e.encounter_id
         INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id AND
                                         et.uuid = '38cb2232-30fc-4b1f-8df1-47c795771ee9'
         INNER JOIN (SELECT person_id, MAX(obs_datetime) latest_date
                     FROM obs
                     WHERE concept_id = 1305
                       AND obs_group_id in (SELECT obs_id from obs where concept_id=163157 and voided=0 GROUP BY person_id)
                       AND voided = 0
                     GROUP BY person_id) a ON o.person_id = a.person_id
         LEFT JOIN concept_name cn
                   ON value_coded = cn.concept_id AND cn.concept_name_type = 'FULLY_SPECIFIED' AND
                      cn.locale = 'en'
WHERE o.concept_id = 1305
  AND obs_datetime = a.latest_date
  AND o.voided = 0
  AND obs_datetime <= CURRENT_DATE()
GROUP BY o.person_id;
-- $END