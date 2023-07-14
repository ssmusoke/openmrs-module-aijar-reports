-- $BEGIN
INSERT INTO mamba_fact_active_in_care(client_id,
                                      latest_return_date,
                                      days_left_to_be_lost,
                                      last_encounter_date,
                                      dead)
SELECT client_id,
       return_visit_date,
       TIMESTAMPDIFF(DAY, DATE(return_visit_date), DATE(CURRENT_DATE())) AS days_lost,
       MAX(encounter_date)                                               AS last_encounter_date,
       dead
FROM mamba_fact_encounter_hiv_art_card leeft
         JOIN person p ON client_id = p.person_id
GROUP BY client_id;
-- $END