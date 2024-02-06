-- $BEGIN

INSERT INTO mamba_z_encounter_obs
(
    encounter_id,
    person_id,
    obs_datetime,
    encounter_datetime,
    encounter_type_uuid,
    obs_question_concept_id,
    obs_value_text,
    obs_value_numeric,
    obs_value_coded,
    obs_value_datetime,
    obs_value_complex,
    obs_value_drug,
    obs_question_uuid,
    obs_answer_uuid,
    obs_value_coded_uuid,
    status,
    voided
)
SELECT o.encounter_id,
       o.person_id,
       o.obs_datetime,
       e.encounter_datetime,
       e.encounter_type_uuid,
       o.concept_id     AS obs_question_concept_id,
       o.value_text     AS obs_value_text,
       o.value_numeric  AS obs_value_numeric,
       o.value_coded    AS obs_value_coded,
       o.value_datetime AS obs_value_datetime,
       o.value_complex  AS obs_value_complex,
       o.value_drug     AS obs_value_drug,
       NULL             AS obs_question_uuid,
       NULL             AS obs_answer_uuid,
       NULL             AS obs_value_coded_uuid,
       o.status,
       o.voided
FROM obs o
         INNER JOIN mamba_dim_encounter e
                    ON o.encounter_id = e.encounter_id
WHERE o.encounter_id IS NOT NULL and o.voided =0;

-- $END