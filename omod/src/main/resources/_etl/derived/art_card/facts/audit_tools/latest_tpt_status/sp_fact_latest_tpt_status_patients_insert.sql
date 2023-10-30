-- $BEGIN
INSERT INTO mamba_fact_patients_latest_tpt_status(client_id,
                                                  encounter_date,
                                                  status)
SELECT b.client_id,encounter_date, tpt_status
FROM mamba_fact_encounter_hiv_art_card b
         JOIN
     (SELECT client_id, MAX(encounter_id) as encounter_id
      FROM mamba_fact_encounter_hiv_art_card
      WHERE tpt_status IS NOT NULL
      GROUP BY client_id) a
     ON a.encounter_id = b.encounter_id;
-- $END