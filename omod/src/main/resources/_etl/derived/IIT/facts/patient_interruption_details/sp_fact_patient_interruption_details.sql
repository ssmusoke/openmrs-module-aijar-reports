-- $BEGIN
CALL sp_fact_patient_interruption_details_create();
CALL sp_fact_patient_interruption_details_insert();
CALL sp_fact_patient_interruption_details_update();
-- $END