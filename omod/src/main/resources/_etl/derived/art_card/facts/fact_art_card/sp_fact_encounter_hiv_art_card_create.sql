-- $BEGIN
CREATE TABLE mamba_fact_encounter_hiv_art_card
(
    id                                    INT AUTO_INCREMENT,
    encounter_id                          INT          NULL,
    client_id                             INT          NULL,
    encounter_date                        DATE         NULL,

    method_of_family_planning             VARCHAR(255) NULL,
    cd4                                   INT NULL,
    hiv_viral_load                        VARCHAR(255) NULL,
    historical_drug_start_date            DATE NULL,
    historical_drug_stop_date             DATE NULL,
    medication_orders                     VARCHAR(255) NULL,
    viral_load_qualitative                VARCHAR(255) NULL,
    hepatitis_b_test___qualitative        VARCHAR(255) NULL,
    duration_units                        VARCHAR(255) NULL,
    return_visit_date                     DATE NULL,
    cd4_count                             INT NULL,
    estimated_date_of_confinement         DATE NULL,
    pmtct                                 VARCHAR(255) NULL,
    pregnant                              VARCHAR(255) NULL,
    scheduled_patient_visist              VARCHAR(255) NULL,
    who_hiv_clinical_stage                VARCHAR(255) NULL,
    name_of_location_transferred_to       TEXT NULL,
    tuberculosis_status                   VARCHAR(255) NULL,
    tuberculosis_treatment_start_date     VARCHAR(255) NULL,
    adherence_assessment_code             VARCHAR(255) NULL,
    reason_for_missing_arv_administration VARCHAR(255) NULL,
    medication_or_other_side_effects      TEXT NULL,
    family_planning_status                VARCHAR(255) NULL,
    symptom_diagnosis                     VARCHAR(255) NULL,
    transfered_out_to_another_facility    VARCHAR(255) NULL,
    tuberculosis_treatment_stop_date      DATE NULL,
    current_arv_regimen                   VARCHAR(255) NULL,
    art_duration                          INT NULL,
    current_art_duration                  INT NULL,
    mid_upper_arm_circumference_code      VARCHAR(255) NULL,
    district_tuberculosis_number          VARCHAR(255) NULL,
    other_medications_dispensed           TEXT NULL,
    arv_regimen_days_dispensed            DOUBLE NULL,
    ar_regimen_dose                       DOUBLE NULL,
    nutrition_support_and_infant_feeding  VARCHAR(255) NULL,
    other_side_effects                    TEXT NULL,
    other_reason_for_missing_arv          TEXT NULL,
    current_regimen_other                 TEXT NULL,
    transfer_out_date                     DATE NULL,
    cotrim_given                          VARCHAR(80) NULL,
    syphilis_test_result_for_partner      VARCHAR(255) NULL,
    eid_visit_1_z_score                   VARCHAR(255) NULL,
    medication_duration                   VARCHAR(255) NULL,
    medication_prescribed_per_dose        VARCHAR(255) NULL,
    tuberculosis_polymerase               VARCHAR(255) NULL,
    specimen_sources                      VARCHAR(255) NULL,
    estimated_gestational_age             INT NULL,
    hiv_viral_load_date                   DATE NULL,
    other_reason_for_appointment          TEXT NULL,
    nutrition_assesment                   VARCHAR(255) NULL,
    differentiated_service_delivery       VARCHAR(255) NULL,
    stable_in_dsdm                        VARCHAR(255) NULL,
    tpt_start_date                        DATE NULL,
    tpt_completion_date                   DATE NULL,
    advanced_disease_status               VARCHAR(255) NULL,
    tpt_status                            VARCHAR(255) NULL,
    rpr_test_results                      VARCHAR(255) NULL,
    crag_test_results                     VARCHAR(255) NULL,
    tb_lam_results                        VARCHAR(255) NULL,
    cervical_cancer_screening             VARCHAR(255) NULL,
    intention_to_conceive                 VARCHAR(255) NULL,
    tb_microscopy_results                 VARCHAR(255) NULL,
    quantity_unit                         VARCHAR(255) NULL,
    tpt_side_effects                      VARCHAR(255) NULL,
    lab_number                            TEXT NULL,
    test                                  VARCHAR(255) NULL,
    test_result                           VARCHAR(255) NULL,
    refill_point_code                     VARCHAR(80) NULL,
    next_return_date_at_facility          DATE NULL,
    indication_for_viral_load_testing     VARCHAR(255) NULL,

    PRIMARY KEY (id)
)
    CHARSET = UTF8MB4;

CREATE INDEX
    mamba_fact_encounter_hiv_art_card_client_id_index ON mamba_fact_encounter_hiv_art_card (client_id);

CREATE INDEX
    mamba_fact_encounter_hiv_art_card_encounter_id_index ON mamba_fact_encounter_hiv_art_card (encounter_id);

CREATE INDEX
    mamba_fact_encounter_hiv_art_card_encounter_date_index ON mamba_fact_encounter_hiv_art_card (encounter_date);
-- $END

