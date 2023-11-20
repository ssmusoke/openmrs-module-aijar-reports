-- $BEGIN
CALL sp_fact_latest_iac_sessions_patients_create();
CALL sp_fact_latest_iac_sessions_patients_insert();
CALL sp_fact_latest_iac_sessions_patients_update();
-- $END