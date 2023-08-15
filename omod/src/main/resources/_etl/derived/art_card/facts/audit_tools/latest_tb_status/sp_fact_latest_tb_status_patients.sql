-- $BEGIN
CALL sp_fact_latest_tb_status_patients_create();
CALL sp_fact_latest_tb_status_patients_insert();
CALL sp_fact_latest_tb_status_patients_update();
-- $END