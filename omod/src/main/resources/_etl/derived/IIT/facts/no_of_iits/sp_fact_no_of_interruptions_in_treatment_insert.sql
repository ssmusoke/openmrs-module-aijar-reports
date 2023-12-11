-- $BEGIN
INSERT INTO mamba_fact_patients_no_of_interruptions(client_id,
                                                        encounter_date,
                                                        return_date,
                                                    days_interrupted)
SELECT
    client_id,
    encounter_date,
    return_visit_date,
    DATEDIFF(
            encounter_date,
            (SELECT return_visit_date
             FROM mamba_fact_encounter_hiv_art_card AS sub
             WHERE sub.client_id = main.client_id
               AND sub.encounter_date < main.encounter_date
             ORDER BY sub.encounter_date DESC
                LIMIT 1)
    ) AS Days
FROM
    mamba_fact_encounter_hiv_art_card AS main
ORDER BY
    client_id, encounter_date;
-- $END