-- $BEGIN
INSERT INTO mamba_fact_patients_latest_adherence (client_id,
                                                adherence)
SELECT b.client_id, adherence_assessment_code
FROM mamba_fact_encounter_hiv_art_card b
         JOIN
     (SELECT client_id, MAX(encounter_id) as encounter_id
      FROM mamba_fact_encounter_hiv_art_card
      WHERE adherence_assessment_code IS NOT NULL
      GROUP BY client_id) a ON b.encounter_id = a.encounter_id;
-- $END