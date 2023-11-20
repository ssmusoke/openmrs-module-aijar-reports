-- $BEGIN
CALL sp_fact_latest_who_stage_patients_create();
CALL sp_fact_latest_who_stage_patients_insert();
CALL sp_fact_latest_who_stage_patients_update();
-- $END