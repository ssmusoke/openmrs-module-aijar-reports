-- $BEGIN
INSERT INTO mamba_fact_patients_interruptions_details(client_id, case_id, art_enrollment_date, days_since_initiation,
                                                      last_dispense_date, last_dispense_amount,
                                                      current_regimen_start_date, last_VL_result, VL_last_date,
                                                      last_dispense_description, all_interruptions,
                                                      iit_in_last_12Months, longest_IIT_ever, last_IIT_duration,
                                                      last_encounter_interruption_date)

SELECT person_id,
       uuid                                                            AS case_id,
       baseline_regimen_start_date                                     as art_enrollment_date,
       TIMESTAMPDIFF(DAY,baseline_regimen_start_date, last_visit_date) as days_since_initiation,
       last_visit_date                                                 as last_dispense_date,
       arv_days_dispensed                                              as last_dispense_amount,
       arv_regimen_start_date                                          as current_regimen_start_date,
       hiv_viral_load_copies                                           as last_VL_result,
       hiv_viral_collection_date                                       as VL_last_date,
       current_regimen                                                 as last_dispense_description,
       all_interruptions,
       iit_in_last_12Months,
       longest_IIT_ever,
       max_encounter_days_interrupted                                  AS last_IIT_duration,
       max_encounter_date                                              AS last_IIT_return_date

FROM mamba_dim_person
         INNER JOIN mamba_fact_audit_tool_art_patients a ON a.client_id = person_id
         LEFT JOIN (SELECT client_id, COUNT(days_interrupted) all_interruptions
                    FROM mamba_fact_patients_no_of_interruptions
                    WHERE days_interrupted >= 28
                    GROUP BY client_id) all_iits ON a.client_id = all_iits.client_id
         LEFT JOIN (SELECT client_id, COUNT(days_interrupted) iit_in_last_12months
                    FROM mamba_fact_patients_no_of_interruptions
                    WHERE days_interrupted >= 28
                      AND encounter_date BETWEEN DATE_SUB(CURRENT_DATE(), INTERVAL 12 MONTH) AND CURRENT_DATE()
                    GROUP BY client_id) mfpnoi1 ON a.client_id = mfpnoi1.client_id
         LEFT JOIN (SELECT client_id, MAX(days_interrupted) longest_IIT_ever
                    FROM mamba_fact_patients_no_of_interruptions
                    WHERE days_interrupted >= 28
                    GROUP BY client_id) mfpnoi ON a.client_id = mfpnoi.client_id
         LEFT JOIN (SELECT m.client_id,
                           max_encounter_date,
                           m.days_interrupted AS max_encounter_days_interrupted
                    FROM mamba_fact_patients_no_of_interruptions m
                             JOIN (SELECT client_id,
                                          MAX(encounter_date) AS max_encounter_date
                                   FROM mamba_fact_patients_no_of_interruptions
                                   WHERE days_interrupted >= 28
                                   GROUP BY client_id) subquery
                                  ON m.client_id = subquery.client_id AND
                                     m.encounter_date = subquery.max_encounter_date) long_interruptions
                   ON a.client_id = long_interruptions.client_id;
-- $END