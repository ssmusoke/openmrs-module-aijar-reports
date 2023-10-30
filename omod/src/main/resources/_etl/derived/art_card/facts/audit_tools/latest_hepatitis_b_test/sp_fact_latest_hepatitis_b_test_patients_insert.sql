-- $BEGIN
INSERT INTO mamba_fact_patients_latest_hepatitis_b_test(client_id,
                                                        encounter_date,
                                                        result)
SELECT b.client_id,encounter_date, hepatitis_b_test___qualitative
FROM mamba_fact_encounter_hiv_art_card b
         JOIN
     (SELECT encounter_id, MAX(encounter_date) AS latest_encounter_date
      FROM mamba_fact_encounter_hiv_art_card
      WHERE hepatitis_b_test___qualitative IS NOT NULL
      GROUP BY client_id) a
     ON a.encounter_id = b.encounter_id ;
-- $END