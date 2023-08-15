-- $BEGIN
CALL sp_fact_latest_family_planning_patients_create();
CALL sp_fact_latest_family_planning_patients_insert();
CALL sp_fact_latest_family_planning_patients_update();
-- $END