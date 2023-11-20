-- $BEGIN
INSERT INTO mamba_fact_patients_latest_index_tested_children(client_id,
                                                             no)
SELECT age.person_id, COUNT(*) AS no
FROM (SELECT family.person_id, obs_group_id
    FROM obs family
    INNER JOIN (SELECT o.person_id, obs_id
    FROM obs o
    WHERE concept_id = 99075
    AND o.voided = 0) b
    ON family.obs_group_id = b.obs_id
    WHERE concept_id = 164352
    AND value_coded = 90280) relationship_child
    JOIN (SELECT family.person_id, obs_group_id
    FROM obs family
    INNER JOIN (SELECT o.person_id, obs_id
    FROM obs o
    WHERE concept_id = 99075
    AND o.voided = 0) b
    ON family.obs_group_id = b.obs_id
    WHERE concept_id = 99074
    AND (TIMESTAMPDIFF(YEAR, obs_datetime, CURRENT_DATE ()) + value_numeric) <= 19) age
ON relationship_child.obs_group_id = age.obs_group_id
GROUP BY age.person_id;
-- $END