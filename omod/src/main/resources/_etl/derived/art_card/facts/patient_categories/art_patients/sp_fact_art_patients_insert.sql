-- $BEGIN
INSERT INTO mamba_fact_art_patients(client_id,
                                      birthdate,
                                      age,
                                      gender,
                                      dead,
                                      age_group)
SELECT DISTINCT e.patient_id, birthdate, mdp.age, gender, dead, mda.datim_agegroup as age_group
FROM mamba_dim_encounter e
         INNER JOIN mamba_fact_patients_latest_patient_demographics mdp ON e.patient_id = mdp.patient_id
LEFT JOIN mamba_dim_agegroup mda on mda.age = mdp.age
WHERE e.voided = 0
  AND e.encounter_type_uuid IN
      ('8d5b27bc-c2cc-11de-8d13-0010c6dffd0f', '8d5b2be0-c2cc-11de-8d13-0010c6dffd0f');
-- $END