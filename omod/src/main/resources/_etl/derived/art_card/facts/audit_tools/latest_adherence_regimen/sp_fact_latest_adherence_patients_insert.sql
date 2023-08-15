-- $BEGIN
INSERT INTO mamba_fact_patients_latest_adherence (client_id,
                                                adherence)
SELECT a.client_id, adherence_assessment_code
FROM mamba_fact_encounter_hiv_art_card b
         JOIN
     (SELECT client_id, MAX(encounter_date) AS latest_encounter_date
      FROM mamba_fact_encounter_hiv_art_card
      WHERE adherence_assessment_code IS NOT NULL
      GROUP BY client_id) a ON encounter_date = latest_encounter_date AND
                               b.client_id = a.client_id;
-- $END