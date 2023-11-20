-- $BEGIN
CALL sp_fact_latest_iac_decision_outcome_patients_create();
CALL sp_fact_latest_iac_decision_outcome_patients_insert();
CALL sp_fact_latest_iac_decision_outcome_patients_update();
-- $END