-- $BEGIN
INSERT INTO mamba_fact_patients_latest_viral_load_ordered (client_id,
                                                encounter_date, order_date)
SELECT b.client_id,encounter_date, hiv_viral_load_date
FROM mamba_fact_encounter_hiv_art_card b
         JOIN
     (SELECT client_id, MAX(encounter_id) as encounter_id
      FROM mamba_fact_encounter_hiv_art_card
      WHERE hiv_viral_load IS NULL
        AND hiv_viral_load_date IS NOT NULL
      GROUP BY client_id) a
     ON  a.encounter_id = b.encounter_id;
-- $END