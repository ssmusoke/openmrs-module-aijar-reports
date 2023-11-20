-- $BEGIN
CALL sp_fact_encounter_non_suppressed_card_create();
CALL sp_fact_encounter_non_suppressed_card_insert();
CALL sp_fact_encounter_non_suppressed_card_update();
-- $END