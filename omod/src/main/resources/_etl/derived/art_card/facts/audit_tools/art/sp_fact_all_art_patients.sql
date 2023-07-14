-- $BEGIN
CALL sp_fact_all_art_patients_create();
CALL sp_fact_all_art_patients_insert();
CALL sp_fact_all_art_patients_update();
-- $END