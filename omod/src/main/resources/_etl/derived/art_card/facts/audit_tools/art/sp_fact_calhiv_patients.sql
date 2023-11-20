-- $BEGIN
CALL sp_fact_calhiv_patients_create();
CALL sp_fact_calhiv_patients_insert();
CALL sp_fact_calhiv_patients_update();
-- $END