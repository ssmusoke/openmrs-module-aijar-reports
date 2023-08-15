-- $BEGIN
CALL sp_fact_latest_index_tested_children_patients_create();
CALL sp_fact_latest_index_tested_children_patients_insert();
CALL sp_fact_latest_index_tested_children_patients_update();
-- $END