-- $BEGIN
CALL sp_fact_latest_hepatitis_b_test_patients_create();
CALL sp_fact_latest_hepatitis_b_test_patients_insert();
CALL sp_fact_latest_hepatitis_b_test_patients_update();
-- $END