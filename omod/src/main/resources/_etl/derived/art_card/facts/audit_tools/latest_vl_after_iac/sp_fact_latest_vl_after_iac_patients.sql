-- $BEGIN
CALL sp_fact_latest_vl_after_iac_patients_create();
CALL sp_fact_latest_vl_after_iac_patients_insert();
CALL sp_fact_latest_vl_after_iac_patients_update();
-- $END