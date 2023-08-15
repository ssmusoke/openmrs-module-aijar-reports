-- $BEGIN
CALL sp_fact_latest_adherence_patients_create();
CALL sp_fact_latest_adherence_patients_insert();
CALL sp_fact_latest_adherence_patients_update();
-- $END