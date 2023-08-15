-- $BEGIN
CALL sp_fact_latest_current_regimen_patients_create();
CALL sp_fact_latest_current_regimen_patients_insert();
CALL sp_fact_latest_current_regimen_patients_update();
-- $END