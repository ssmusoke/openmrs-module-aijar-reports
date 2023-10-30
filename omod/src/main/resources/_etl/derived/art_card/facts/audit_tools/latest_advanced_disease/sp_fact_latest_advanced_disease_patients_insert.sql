-- $BEGIN
INSERT INTO mamba_fact_patients_latest_advanced_disease(client_id,
                                                        encounter_date,
                                                        advanced_disease)
SELECT b.client_id,encounter_date, advanced_disease_status
FROM mamba_fact_encounter_hiv_art_card b
         JOIN
     (SELECT encounter_id, MAX(encounter_date) AS latest_encounter_date
      FROM mamba_fact_encounter_hiv_art_card
      WHERE advanced_disease_status IS NOT NULL
      GROUP BY client_id) a
     ON a.encounter_id = b.encounter_id;
-- $END