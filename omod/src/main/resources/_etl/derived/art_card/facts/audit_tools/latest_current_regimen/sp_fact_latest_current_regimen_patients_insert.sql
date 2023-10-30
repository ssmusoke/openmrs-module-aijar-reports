-- $BEGIN
INSERT INTO mamba_fact_patients_latest_current_regimen (client_id,
                                                current_regimen)
SELECT b.client_id, current_arv_regimen
FROM mamba_fact_encounter_hiv_art_card b
         JOIN
     (SELECT client_id, MAX(encounter_id) as encounter_id
      FROM mamba_fact_encounter_hiv_art_card
      WHERE current_arv_regimen IS NOT NULL
      GROUP BY client_id) a ON a.encounter_id = b.encounter_id;
-- $END