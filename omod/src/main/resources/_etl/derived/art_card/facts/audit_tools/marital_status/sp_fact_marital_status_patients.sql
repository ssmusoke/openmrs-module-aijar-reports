-- $BEGIN
CALL sp_fact_marital_status_patients_create();
CALL sp_fact_marital_status_patients_insert();
CALL sp_fact_marital_status_patients_update();
-- $END