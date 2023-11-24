-- $BEGIN
CALL sp_fact_latest_nutrition_assesment_patients_create();
CALL sp_fact_latest_nutrition_assesment_patients_insert();
CALL sp_fact_latest_nutrition_assesment_patients_update();
-- $END