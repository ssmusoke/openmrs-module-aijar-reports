-- $BEGIN
CALL sp_fact_latest_advanced_disease_patients_create();
CALL sp_fact_latest_advanced_disease_patients_insert();
CALL sp_fact_latest_advanced_disease_patients_update();
-- $END