-- $BEGIN
INSERT INTO mamba_fact_patients_latest_who_stage(client_id,
                                                 encounter_date,
                                                 stage)
SELECT b.client_id,encounter_date, who_hiv_clinical_stage
FROM mamba_fact_encounter_hiv_art_card b
         JOIN
     (SELECT encounter_id, MAX(encounter_date) AS latest_encounter_date
      FROM mamba_fact_encounter_hiv_art_card
      WHERE who_hiv_clinical_stage IS NOT NULL
      GROUP BY client_id) a
     ON a.encounter_id = b.encounter_id ;
-- $END