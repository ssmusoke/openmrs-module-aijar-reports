-- $BEGIN
CALL sp_fact_latest_return_date_patients_create();
CALL sp_fact_latest_return_date_patients_insert();
CALL sp_fact_latest_return_date_patients_update();
-- $END