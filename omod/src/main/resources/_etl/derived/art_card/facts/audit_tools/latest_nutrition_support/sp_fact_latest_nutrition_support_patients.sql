-- $BEGIN
CALL sp_fact_latest_nutrition_support_patients_create();
CALL sp_fact_latest_nutrition_support_patients_insert();
CALL sp_fact_latest_nutrition_support_patients_update();
-- $END