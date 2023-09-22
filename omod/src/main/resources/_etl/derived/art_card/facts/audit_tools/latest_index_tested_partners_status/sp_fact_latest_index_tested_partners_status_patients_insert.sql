-- $BEGIN
INSERT INTO mamba_fact_patients_latest_index_tested_partners_status(client_id,
                                                                    no)
SELECT status.person_id, COUNT(*) AS no
FROM (SELECT family.person_id, obs_group_id
    FROM obs family
    INNER JOIN (SELECT o.person_id, obs_id
    FROM obs o
    WHERE concept_id = 99075
    AND o.voided = 0) b
    ON family.obs_group_id = b.obs_id
    WHERE concept_id = 164352
    AND value_coded IN (90288, 165274)) relationship_spouse
    INNER JOIN (SELECT family.person_id, obs_group_id
    FROM obs family
    INNER JOIN (SELECT o.person_id, obs_id
    FROM obs o
    WHERE concept_id = 99075
    AND o.voided = 0) b
    ON family.obs_group_id = b.obs_id
    WHERE concept_id = 165275 and voided =0) status
ON status.obs_group_id = relationship_spouse.obs_group_id
GROUP BY status.person_id;
-- $END