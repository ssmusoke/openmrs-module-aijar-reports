-- $BEGIN
INSERT INTO mamba_fact_patients_latest_current_regimen (client_id,
                                                current_regimen)
SELECT a.client_id, current_arv_regimen
FROM mamba_fact_encounter_hiv_art_card b
         JOIN
     (SELECT client_id, MAX(encounter_date) AS latest_encounter_date
      FROM mamba_fact_encounter_hiv_art_card
      WHERE current_arv_regimen IS NOT NULL
      GROUP BY client_id) a ON a.client_id = b.client_id AND
                               latest_encounter_date = encounter_date;
-- $END