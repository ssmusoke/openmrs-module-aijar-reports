-- $BEGIN
CALL sp_fact_nationality_patients_create();
CALL sp_fact_nationality_patients_insert();
CALL sp_fact_nationality_patients_update();
-- $END