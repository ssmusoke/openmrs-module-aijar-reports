-- $BEGIN
-- CALL sp_dim_client_hiv_hts;
CALL sp_fact_encounter_hiv_art_card;
CALL sp_fact_encounter_hiv_art_summary;
CALL sp_fact_encounter_hiv_art_health_education;
CALL sp_fact_current_arv_regimen_start_date;
CALL sp_fact_active_in_care;
-- CALL sp_fact_all_art_patients;

-- $END