-- $BEGIN
CALL sp_fact_latest_viral_load_patients_create();
CALL sp_fact_latest_viral_load_patients_insert();
CALL sp_fact_latest_viral_load_patients_update();
-- $END