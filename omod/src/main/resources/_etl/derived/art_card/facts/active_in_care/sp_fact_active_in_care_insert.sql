-- $BEGIN
INSERT INTO mamba_fact_active_in_care(client_id,
                                      latest_return_date,
                                      days_left_to_be_lost,
                                      last_encounter_date,
                                      dead)
SELECT b.client_id,
       return_visit_date,
       TIMESTAMPDIFF(DAY, DATE(return_visit_date), DATE(CURRENT_DATE())) AS days_lost,
       encounter_date                                                    AS last_encounter_date,
       dead
FROM mamba_fact_encounter_hiv_art_card b
         JOIN
     (SELECT client_id, MAX(encounter_id) as encounter_id
      FROM mamba_fact_encounter_hiv_art_card
      WHERE return_visit_date IS NOT NULL
      GROUP BY client_id) a
     ON a.encounter_id = b.encounter_id
         JOIN person p ON b.client_id = p.person_id;
-- $END