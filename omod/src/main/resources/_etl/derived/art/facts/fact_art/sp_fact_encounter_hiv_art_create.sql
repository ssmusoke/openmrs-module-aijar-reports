-- $BEGIN
CREATE TABLE mamba_fact_encounter_hiv_art_card
(
    id                                   INT AUTO_INCREMENT,
    encounter_id                         INT NULL,
    client_id                            INT NULL,
    encounter_date                       DATE NULL,

    hemoglobin                           CHAR(255) CHARACTER SET UTF8 NULL,
    malnutrition                         CHAR(255) CHARACTER SET UTF8 NULL,
    method_of_family_planning            CHAR(255) CHARACTER SET UTF8 NULL,
    oedema                               CHAR(255) CHARACTER SET UTF8 NULL,
    cd4_panel                            CHAR(255) CHARACTER SET UTF8 NULL,
    cd4_percent                          CHAR(255) CHARACTER SET UTF8 NULL,
    hiv_viral_load                       CHAR(255) CHARACTER SET UTF8 NULL,
    historical_drug_start_date           CHAR(255) CHARACTER SET UTF8 NULL,
    historical_drug_stop_date            CHAR(255) CHARACTER SET UTF8 NULL,
    current_drugs_used                   CHAR(255) CHARACTER SET UTF8 NULL,
    tests_ordered                        CHAR(255) CHARACTER SET UTF8 NULL,
    number_of_weeks_pregnant             CHAR(255) CHARACTER SET UTF8 NULL,
    medication_orders                    CHAR(255) CHARACTER SET UTF8 NULL,
    viral_load_qualitative               CHAR(255) CHARACTER SET UTF8 NULL,
    hepatitis_b_test_qualitative         CHAR(255) CHARACTER SET UTF8 NULL,
    mid_upper_arm_circumference          CHAR(255) CHARACTER SET UTF8 NULL,
    medication_strength                  CHAR(255) CHARACTER SET UTF8 NULL,
    register_serial_number               CHAR(255) CHARACTER SET UTF8 NULL,
    duration_units                       CHAR(255) CHARACTER SET UTF8 NULL,
    systolic_blood_pressure              CHAR(255) CHARACTER SET UTF8 NULL,
    diastolic_blood_pressure             CHAR(255) CHARACTER SET UTF8 NULL,
    pulse                                CHAR(255) CHARACTER SET UTF8 NULL,
    temperature                          CHAR(255) CHARACTER SET UTF8 NULL,
    weight                               CHAR(255) CHARACTER SET UTF8 NULL,
    height                               CHAR(255) CHARACTER SET UTF8 NULL,
    return_visit_date                    CHAR(255) CHARACTER SET UTF8 NULL,
    respiratory_rate                     CHAR(255) CHARACTER SET UTF8 NULL,
    head_circumference                   CHAR(255) CHARACTER SET UTF8 NULL,
    cd4_count                            CHAR(255) CHARACTER SET UTF8 NULL,
    estimated_date_of_confinement        CHAR(255) CHARACTER SET UTF8 NULL,
    pmtct                                CHAR(255) CHARACTER SET UTF8 NULL,
    pregnant                             CHAR(255) CHARACTER SET UTF8 NULL,
    scheduled_patient_visit              CHAR(255) CHARACTER SET UTF8 NULL,
    entry_point_into_hiv_care            CHAR(255) CHARACTER SET UTF8 NULL,
    who_hiv_clinical_stage               CHAR(255) CHARACTER SET UTF8 NULL,
    name_of_location_transferred_to      CHAR(255) CHARACTER SET UTF8 NULL,
    tuberculosis_status                  CHAR(255) CHARACTER SET UTF8 NULL,
    tuberculosis_treatment_start_date    CHAR(255) CHARACTER SET UTF8 NULL,
    adherence_to_cotrim                  CHAR(255) CHARACTER SET UTF8 NULL,
    arv_adherence_assessment_code        CHAR(255) CHARACTER SET UTF8 NULL,
    reason_for_missing_arv               CHAR(255) CHARACTER SET UTF8 NULL,
    medication_or_other_side_effects     CHAR(255) CHARACTER SET UTF8 NULL,
    history_of_functional_status         CHAR(255) CHARACTER SET UTF8 NULL,
    body_weight                          CHAR(255) CHARACTER SET UTF8 NULL,
    family_planning_status               CHAR(255) CHARACTER SET UTF8 NULL,
    symptom_diagnosis                    CHAR(255) CHARACTER SET UTF8 NULL,
    address                              CHAR(255) CHARACTER SET UTF8 NULL,
    date_positive_hiv_test_confirmed     CHAR(255) CHARACTER SET UTF8 NULL,
    treatment_supporter_telephone_number CHAR(255) CHARACTER SET UTF8 NULL,
    transferred_out                      CHAR(255) CHARACTER SET UTF8 NULL,
    tuberculosis_treatment_stop_date     CHAR(255) CHARACTER SET UTF8 NULL,
    current_arv_regimen                  CHAR(255) CHARACTER SET UTF8 NULL,
    art_duration                         CHAR(255) CHARACTER SET UTF8 NULL,
    current_art_duration                 CHAR(255) CHARACTER SET UTF8 NULL,
    antenatal_number                     CHAR(255) CHARACTER SET UTF8 NULL,
    mid_upper_arm_circumference_code     CHAR(255) CHARACTER SET UTF8 NULL,
    district_tuberculosis_number         CHAR(255) CHARACTER SET UTF8 NULL,
    opportunistic_infection              CHAR(255) CHARACTER SET UTF8 NULL,
    trimethoprim_days_dispensed          CHAR(255) CHARACTER SET UTF8 NULL,
    other_medications_dispensed          CHAR(255) CHARACTER SET UTF8 NULL,
    arv_regimen_days_dispensed           CHAR(255) CHARACTER SET UTF8 NULL,
    trimethoprim_dosage                  CHAR(255) CHARACTER SET UTF8 NULL,
    ar_regimen_dose                      CHAR(255) CHARACTER SET UTF8 NULL,
    nutrition_support_and_infant_feeding CHAR(255) CHARACTER SET UTF8 NULL,
    baseline_regimen                     CHAR(255) CHARACTER SET UTF8 NULL,
    baseline_weight                      CHAR(255) CHARACTER SET UTF8 NULL,
    baseline_stage                       CHAR(255) CHARACTER SET UTF8 NULL,
    baseline_cd4                         CHAR(255) CHARACTER SET UTF8 NULL,
    baseline_pregnancy                   CHAR(255) CHARACTER SET UTF8 NULL,
    name_of_family_member                CHAR(255) CHARACTER SET UTF8 NULL,
    age_of_family_member                 CHAR(255) CHARACTER SET UTF8 NULL,
    family_member_set                    CHAR(255) CHARACTER SET UTF8 NULL,
    hiv_test                             CHAR(255) CHARACTER SET UTF8 NULL,
    hiv_test_facility                    CHAR(255) CHARACTER SET UTF8 NULL,
    other_side_effects                   CHAR(255) CHARACTER SET UTF8 NULL,
    other_tests_ordered                  CHAR(255) CHARACTER SET UTF8 NULL,
    care_entry_point_set                 CHAR(255) CHARACTER SET UTF8 NULL,
    treatment_supporter_tel_no           CHAR(255) CHARACTER SET UTF8 NULL,
    other_reason_for_missing_arv         CHAR(255) CHARACTER SET UTF8 NULL,
    current_regimen_other                CHAR(255) CHARACTER SET UTF8 NULL,
    treatment_supporter_name             CHAR(255) CHARACTER SET UTF8 NULL,
    cd4_classification_for_infants       CHAR(255) CHARACTER SET UTF8 NULL,
    baseline_regimen_start_date          CHAR(255) CHARACTER SET UTF8 NULL,
    baseline_regimen_set                 CHAR(255) CHARACTER SET UTF8 NULL,
    transfer_out_date                    CHAR(255) CHARACTER SET UTF8 NULL,
    transfer_out_set                     CHAR(255) CHARACTER SET UTF8 NULL,
    health_education_disclosure          CHAR(255) CHARACTER SET UTF8 NULL,
    other_referral_ordered               CHAR(255) CHARACTER SET UTF8 NULL,
    age_in_months                        CHAR(255) CHARACTER SET UTF8 NULL,
    test_result_type                     CHAR(255) CHARACTER SET UTF8 NULL,
    lab_result_txt                       CHAR(255) CHARACTER SET UTF8 NULL,
    lab_result_set                       CHAR(255) CHARACTER SET UTF8 NULL,
    counselling_session_type             CHAR(255) CHARACTER SET UTF8 NULL,
    cotrim_given                         CHAR(255) CHARACTER SET UTF8 NULL,
    eid_visit_1_appointment_date         CHAR(255) CHARACTER SET UTF8 NULL,
    feeding_status_at_eid_visit_1        CHAR(255) CHARACTER SET UTF8 NULL,
    counselling_approach                 CHAR(255) CHARACTER SET UTF8 NULL,
    current_hiv_test_result              CHAR(255) CHARACTER SET UTF8 NULL,
    results_received_as_a_couple         CHAR(255) CHARACTER SET UTF8 NULL,
    tb_suspect                           CHAR(255) CHARACTER SET UTF8 NULL,
    baseline_lactating                   CHAR(255) CHARACTER SET UTF8 NULL,
    inh_dosage                           CHAR(255) CHARACTER SET UTF8 NULL,
    inh_days_dispensed                   CHAR(255) CHARACTER SET UTF8 NULL,
    age_unit                             CHAR(255) CHARACTER SET UTF8 NULL,
    syphilis_test_result                 CHAR(255) CHARACTER SET UTF8 NULL,
    syphilis_test_result_for_partner     CHAR(255) CHARACTER SET UTF8 NULL,
    ctx_given_at_eid_visit_1             CHAR(255) CHARACTER SET UTF8 NULL,
    nvp_given_at_eid_visit_1             CHAR(255) CHARACTER SET UTF8 NULL,
    eid_visit_1_muac                     CHAR(255) CHARACTER SET UTF8 NULL,
    medication_duration                  CHAR(255) CHARACTER SET UTF8 NULL,
    clinical_impression_comment          CHAR(255) CHARACTER SET UTF8 NULL,
    reason_for_appointment               CHAR(255) CHARACTER SET UTF8 NULL,
    medication_history                   CHAR(255) CHARACTER SET UTF8 NULL,
    quantity_of_medication               CHAR(255) CHARACTER SET UTF8 NULL,
    tb_with_rifampin_resistance_checking CHAR(255) CHARACTER SET UTF8 NULL,
    specimen_sources                     CHAR(255) CHARACTER SET UTF8 NULL,
    eid_immunisation_codes               CHAR(255) CHARACTER SET UTF8 NULL,
    clinical_assessment_codes            CHAR(255) CHARACTER SET UTF8 NULL,
    refiil_of_art_for_the_mother         CHAR(255) CHARACTER SET UTF8 NULL,
    development_milestone                CHAR(255) CHARACTER SET UTF8 NULL,
    pre_test_counseling_done             CHAR(255) CHARACTER SET UTF8 NULL,
    hct_entry_point                      CHAR(255) CHARACTER SET UTF8 NULL,
    linked_to_care                       CHAR(255) CHARACTER SET UTF8 NULL,
    estimated_gestational_age            CHAR(255) CHARACTER SET UTF8 NULL,
    eid_concept_type                     CHAR(255) CHARACTER SET UTF8 NULL,
    hiv_viral_load_date                  CHAR(255) CHARACTER SET UTF8 NULL,
    relationship_to_patient              CHAR(255) CHARACTER SET UTF8 NULL,
    other_reason_for_appointment         CHAR(255) CHARACTER SET UTF8 NULL,
    nutrition_assessment                 CHAR(255) CHARACTER SET UTF8 NULL,
    art_pill_balance                     CHAR(255) CHARACTER SET UTF8 NULL,
    differentiated_service_delivery      CHAR(255) CHARACTER SET UTF8 NULL,
    stable_in_dsdm                       CHAR(255) CHARACTER SET UTF8 NULL,
    reason_for_testing                   CHAR(255) CHARACTER SET UTF8 NULL,
    previous_hiv_tests_date              CHAR(255) CHARACTER SET UTF8 NULL,
    milligram_per_meter_squared          CHAR(255) CHARACTER SET UTF8 NULL,
    hiv_testing_service_delivery_model   CHAR(255) CHARACTER SET UTF8 NULL,
    hiv_syphillis_duo                    CHAR(255) CHARACTER SET UTF8 NULL,
    prevention_services_received         CHAR(255) CHARACTER SET UTF8 NULL,
    hiv_first_time_tester                CHAR(255) CHARACTER SET UTF8 NULL,
    previous_hiv_test_results            CHAR(255) CHARACTER SET UTF8 NULL,
    results_received_as_individual       CHAR(255) CHARACTER SET UTF8 NULL,
    health_education_setting             CHAR(255) CHARACTER SET UTF8 NULL,
    health_edu_intervation_approaches    CHAR(255) CHARACTER SET UTF8 NULL,
    health_education_depression_status   CHAR(255) CHARACTER SET UTF8 NULL,
    ovc_screening                        CHAR(255) CHARACTER SET UTF8 NULL,
    art_preparation_readiness            CHAR(255) CHARACTER SET UTF8 NULL,
    ovc_assessment                       CHAR(255) CHARACTER SET UTF8 NULL,
    phdp_components                      CHAR(255) CHARACTER SET UTF8 NULL,
    tpt_start_date                       CHAR(255) CHARACTER SET UTF8 NULL,
    tpt_completion_date                  CHAR(255) CHARACTER SET UTF8 NULL,
    advanced_disease_status              CHAR(255) CHARACTER SET UTF8 NULL,
    family_member_hiv_status             CHAR(255) CHARACTER SET UTF8 NULL,
    tpt_status                           CHAR(255) CHARACTER SET UTF8 NULL,
    rpr_test_results                     CHAR(255) CHARACTER SET UTF8 NULL,
    crag_test_results                    CHAR(255) CHARACTER SET UTF8 NULL,
    tb_lam_results                       CHAR(255) CHARACTER SET UTF8 NULL,
    gender_based_violance                CHAR(255) CHARACTER SET UTF8 NULL,
    dapsone_ctx_medset                   CHAR(255) CHARACTER SET UTF8 NULL,
    tuberculosis_medication_set          CHAR(255) CHARACTER SET UTF8 NULL,
    fluconazole_medication_set           CHAR(255) CHARACTER SET UTF8 NULL,
    cervical_cancer_screening            CHAR(255) CHARACTER SET UTF8 NULL,
    intention_to_conceive                CHAR(255) CHARACTER SET UTF8 NULL,
    viral_load_test                      CHAR(255) CHARACTER SET UTF8 NULL,
    genexpert_test                       CHAR(255) CHARACTER SET UTF8 NULL,
    tb_microscopy_results                CHAR(255) CHARACTER SET UTF8 NULL,
    tb_microscopy_test                   CHAR(255) CHARACTER SET UTF8 NULL,
    tb_lam                               CHAR(255) CHARACTER SET UTF8 NULL,
    rpr_test                             CHAR(255) CHARACTER SET UTF8 NULL,
    crag_test                            CHAR(255) CHARACTER SET UTF8 NULL,
    arv_med_set                          CHAR(255) CHARACTER SET UTF8 NULL,
    quantity_unit                        CHAR(255) CHARACTER SET UTF8 NULL,
    tpt_side_effects                     CHAR(255) CHARACTER SET UTF8 NULL,
    split_into_drugs                     CHAR(255) CHARACTER SET UTF8 NULL,
    lab_number                           CHAR(255) CHARACTER SET UTF8 NULL,
    other_drug_dispensed_set             CHAR(255) CHARACTER SET UTF8 NULL,
    test                                 CHAR(255) CHARACTER SET UTF8 NULL,
    test_result                          CHAR(255) CHARACTER SET UTF8 NULL,
    other_tests                          CHAR(255) CHARACTER SET UTF8 NULL,
    refill_point_code                    CHAR(255) CHARACTER SET UTF8 NULL,
    next_return_date_at_facility         CHAR(255) CHARACTER SET UTF8 NULL,
    PRIMARY KEY (id)
);

CREATE INDEX
    mamba_fact_encounter_hiv_art_encounter_id_index ON mamba_fact_encounter_hiv_art_card (encounter_id);
CREATE INDEX
    mamba_fact_encounter_hiv_art_client_id_index ON mamba_fact_encounter_hiv_art_card (client_id);
-- $END

