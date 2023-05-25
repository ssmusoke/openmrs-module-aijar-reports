-- $BEGIN
CALL sp_fact_encounter_hiv_art_summary_create();
CALL sp_fact_encounter_hiv_art_summary_insert();
CALL sp_fact_encounter_hiv_art_summary_update();
-- $END