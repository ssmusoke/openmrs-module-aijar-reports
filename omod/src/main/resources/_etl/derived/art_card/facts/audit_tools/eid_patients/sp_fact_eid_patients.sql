-- $BEGIN
CALL sp_fact_eid_patients_create();
CALL sp_fact_eid_patients_insert();
CALL sp_fact_eid_patients_update();
-- $END