-- $BEGIN
INSERT INTO mamba_fact_patients_latest_arv_days_dispensed(client_id,
                                                          encounter_date,
                                                          days)
SELECT a.client_id,encounter_date, arv_regimen_days_dispensed
FROM mamba_fact_encounter_hiv_art_card b
         JOIN
     (SELECT client_id, MAX(encounter_date) AS latest_encounter_date
      FROM mamba_fact_encounter_hiv_art_card
      WHERE arv_regimen_days_dispensed IS NOT NULL
      GROUP BY client_id) a ON a.client_id = b.client_id AND
                               encounter_date =
                               latest_encounter_date;
-- $END