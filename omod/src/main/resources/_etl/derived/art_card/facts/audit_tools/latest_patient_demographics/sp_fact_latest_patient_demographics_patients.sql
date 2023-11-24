-- $BEGIN
CALL sp_fact_latest_patient_demographics_patients_create();
CALL sp_fact_latest_patient_demographics_patients_insert();
CALL sp_fact_latest_patient_demographics_patients_update();
-- $END