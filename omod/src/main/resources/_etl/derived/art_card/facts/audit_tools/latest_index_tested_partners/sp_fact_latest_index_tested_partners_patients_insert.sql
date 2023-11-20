-- $BEGIN
INSERT INTO mamba_fact_patients_latest_index_tested_partners(client_id,
                                                             no)
Select person_id, count(*) as no from obs  WHERE concept_id = 164352
                                             AND value_coded IN (90288, 165274) AND voided=0 GROUP BY person_id;
-- $END