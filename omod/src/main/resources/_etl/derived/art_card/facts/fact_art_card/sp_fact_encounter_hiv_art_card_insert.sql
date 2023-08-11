-- $BEGIN
INSERT INTO mamba_fact_encounter_hiv_art_card (encounter_id,
                                               client_id,
                                               encounter_date,
                                               method_of_family_planning,
                                               cd4,
                                               hiv_viral_load,
                                               historical_drug_start_date,
                                               historical_drug_stop_date,
                                               medication_orders,
                                               viral_load_qualitative,
                                               hepatitis_b_test___qualitative,
                                               duration_units,
                                               return_visit_date,
                                               cd4_count,
                                               estimated_date_of_confinement,
                                               pmtct,
                                               pregnant,
                                               scheduled_patient_visist,
                                               who_hiv_clinical_stage,
                                               name_of_location_transferred_to,
                                               tuberculosis_status,
                                               tuberculosis_treatment_start_date,
                                               adherence_assessment_code,
                                               reason_for_missing_arv_administration,
                                               medication_or_other_side_effects,
                                               family_planning_status,
                                               symptom_diagnosis,
                                               transfered_out_to_another_facility,
                                               tuberculosis_treatment_stop_date,
                                               current_arv_regimen,
                                               art_duration,
                                               current_art_duration,
                                               mid_upper_arm_circumference_code,
                                               district_tuberculosis_number,
                                               other_medications_dispensed,
                                               arv_regimen_days_dispensed,
                                               ar_regimen_dose,
                                               nutrition_support_and_infant_feeding,
                                               other_side_effects,
                                               other_reason_for_missing_arv,
                                               current_regimen_other,
                                               transfer_out_date,
                                               cotrim_given,
                                               syphilis_test_result_for_partner,
                                               eid_visit_1_z_score,
                                               medication_duration,
                                               medication_prescribed_per_dose,
                                               tuberculosis_polymerase,
                                               specimen_sources,
                                               estimated_gestational_age,
                                               hiv_viral_load_date,
                                               other_reason_for_appointment,
                                               nutrition_assesment,
                                               differentiated_service_delivery,
                                               stable_in_dsdm,
                                               tpt_start_date,
                                               tpt_completion_date,
                                               advanced_disease_status,
                                               tpt_status,
                                               rpr_test_results,
                                               crag_test_results,
                                               tb_lam_results,
                                               cervical_cancer_screening,
                                               intention_to_conceive,
                                               tb_microscopy_results,
                                               quantity_unit,
                                               tpt_side_effects,
                                               lab_number,
                                               test,
                                               test_result,
                                               refill_point_code,
                                               next_return_date_at_facility,
                                               indication_for_viral_load_testing)
SELECT encounter_id,
       client_id,
       encounter_datetime,
       method_of_family_planning,
       cd4,
       hiv_viral_load,
       historical_drug_start_date,
       historical_drug_stop_date,
       medication_orders,
       viral_load_qualitative,
       hepatitis_b_test___qualitative,
       duration_units,
       return_visit_date,
       cd4_count,
       estimated_date_of_confinement,
       pmtct,
       pregnant,
       scheduled_patient_visist,
       who_hiv_clinical_stage,
       name_of_location_transferred_to,
       tuberculosis_status,
       tuberculosis_treatment_start_date,
       adherence_assessment_code,
       reason_for_missing_arv_administration,
       medication_or_other_side_effects,
       family_planning_status,
       symptom_diagnosis,
       transfered_out_to_another_facility,
       tuberculosis_treatment_stop_date,
       current_arv_regimen,
       art_duration,
       current_art_duration,
       mid_upper_arm_circumference_code,
       district_tuberculosis_number,
       other_medications_dispensed,
       arv_regimen_days_dispensed,
       ar_regimen_dose,
       nutrition_support_and_infant_feeding,
       other_side_effects,
       other_reason_for_missing_arv,
       current_regimen_other,
       transfer_out_date,
       cotrim_given,
       syphilis_test_result_for_partner,
       eid_visit_1_z_score,
       medication_duration,
       medication_prescribed_per_dose,
       tuberculosis_polymerase,
       specimen_sources,
       estimated_gestational_age,
       hiv_viral_load_date,
       other_reason_for_appointment,
       nutrition_assesment,
       differentiated_service_delivery,
       stable_in_dsdm,
       tpt_start_date,
       tpt_completion_date,
       advanced_disease_status,
       tpt_status,
       rpr_test_results,
       crag_test_results,
       tb_lam_results,
       cervical_cancer_screening,
       intention_to_conceive,
       tb_microscopy_results,
       quantity_unit,
       tpt_side_effects,
       lab_number,
       test,
       test_result,
       refill_point_code,
       next_return_date_at_facility,
       indication_for_viral_load_testing
FROM mamba_flat_encounter_art_card ;
-- $END