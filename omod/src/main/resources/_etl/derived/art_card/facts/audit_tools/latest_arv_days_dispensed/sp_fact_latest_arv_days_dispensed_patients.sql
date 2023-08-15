-- $BEGIN
CALL sp_fact_latest_arv_days_dispensed_patients_create();
CALL sp_fact_latest_arv_days_dispensed_patients_insert();
CALL sp_fact_latest_arv_days_dispensed_patients_update();
-- $END