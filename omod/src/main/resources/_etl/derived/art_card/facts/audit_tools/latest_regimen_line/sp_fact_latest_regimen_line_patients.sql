-- $BEGIN
CALL sp_fact_latest_regimen_line_patients_create();
CALL sp_fact_latest_regimen_line_patients_insert();
CALL sp_fact_latest_regimen_line_patients_update();
-- $END