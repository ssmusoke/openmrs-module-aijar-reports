-- $BEGIN
-- CALL sp_dim_client_hiv_hts;

CALL sp_fact_no_of_interruptions_in_treatment;
CALL sp_fact_patient_interruption_details;


-- $END