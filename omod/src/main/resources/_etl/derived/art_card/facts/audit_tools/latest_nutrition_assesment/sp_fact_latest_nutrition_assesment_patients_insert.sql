-- $BEGIN
INSERT INTO mamba_fact_patients_latest_nutrition_assesment(client_id,
                                                           encounter_date,
                                                           status)
SELECT b.client_id,encounter_date, nutrition_assesment
FROM mamba_fact_encounter_hiv_art_card b
         JOIN
     (SELECT client_id, MAX(encounter_id) as encounter_id
      FROM mamba_fact_encounter_hiv_art_card
      WHERE nutrition_assesment IS NOT NULL
      GROUP BY client_id) a
     ON a.encounter_id = b.encounter_id;
-- $END