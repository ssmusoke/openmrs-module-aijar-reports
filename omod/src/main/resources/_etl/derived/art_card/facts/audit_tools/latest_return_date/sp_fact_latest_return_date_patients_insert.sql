-- $BEGIN
INSERT INTO mamba_fact_patients_latest_return_date (client_id,
                                                return_date)
SELECT a.client_id, return_visit_date
FROM mamba_fact_encounter_hiv_art_card b
         JOIN (SELECT client_id,
                      MAX(encounter_date) AS latest_encounter_date
               FROM mamba_fact_encounter_hiv_art_card
               WHERE return_visit_date IS NOT NULL
               GROUP BY client_id) a
              ON a.client_id = b.client_id AND
                 encounter_date = latest_encounter_date;
-- $END