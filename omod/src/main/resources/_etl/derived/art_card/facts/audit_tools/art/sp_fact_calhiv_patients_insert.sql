-- $BEGIN
INSERT INTO mamba_fact_audit_tool_art_patients (client_id,
                                                identifier,
                                                cd4,
                                                nationality,
                                                marital_status,
                                                birthdate,
                                                age,
                                                dead,
                                                gender,
                                                last_visit_date,
                                                return_visit_date,
                                                client_status,
                                                transfer_out_date,
                                                current_arv_regimen,
                                                arv_regimen_start_date,
                                                adherence_assessment_code,
                                                arv_regimen_days_dispensed,
                                                hiv_viral_load,
                                                hiv_viral_load_date,
                                                new_sample_collection_date,
                                                advanced_disease_status,
                                                family_planning_status,
                                                nutrition_assessment,
                                                nutrition_support_and_infant_feeding,
                                                hepatitis_b_test_qualitative,
                                                syphilis_test_result_for_partner,
                                                cervical_cancer_screening,
                                                tuberculosis_status,
                                                tpt_status,
                                                cd4_count,
                                                crag_test_results,
                                                who_hiv_clinical_stage,
                                                differentiated_service_delivery,
                                                baseline_cd4,
                                                baseline_regimen_start_date,
                                                special_category,
                                                regimen_line,
                                                health_education_setting,
                                                pss_issues_identified,
                                                art_preparation,
                                                depression_status,
                                                gender_based_violence,
                                                health_education_disclosure,
                                                ovc_screening,
                                                ovc_assessment,
                                                prevention_components,
                                                linkages_and_refferals,
                                                iac_sessions,
                                                hivdr_results,
                                                date_hivdr_results_received_at_facility,
                                                vl_after_iac,
                                                decision_outcome,
                                                duration_on_art,
                                                medication_or_other_side_effects,
                                                sample_type,
                                                hiv_vl_date,
                                                known_status_children,
                                                pos_status_children,
                                                known_status_spouse,
                                                po_status_spouse)
SELECT cohort.patient_id,
       identifiers.identifier                                               AS identifier,
       cd4,
       nationality.name                                                     AS nationality,
       marital_status.name                                                  AS marital_status,
       birthdate,
       TIMESTAMPDIFF(YEAR ,birthdate, now())                                AS age,
       dead,
       gender,
       last_vist_date,
       return_visit_date,
       IF(dead = 0 and (transfer_out_date is NULL or last_vist_date > transfer_out_date),
          IF(days_left_to_be_lost <= 0, 'Active(TX_CURR)', IF(
                          days_left_to_be_lost >= 1 AND days_left_to_be_lost <= 7, 'Missed Appointment(TX_CURR)',
                          IF(days_left_to_be_lost >= 8 AND days_left_to_be_lost <= 28, 'Lost (Pre-IIT)',
                             IF(days_left_to_be_lost > 28 AND days_left_to_be_lost <= 999, 'LTFU(TX-ML)',
                                'Lost to Followup')))), '')                 AS client_status,
       transfer_out_date,
       sub_current_arv_regimen.current_arv_regimen,
       arv_regimen_start_date,
       adherence_assessment_code,
       arv_regimen_days_dispensed,
       sub_hiv_viral_load.hiv_viral_load,
       sub_hiv_viral_load.hiv_viral_load_date,
       IF(sub_hiv_sample_collection.hiv_viral_load_date > sub_hiv_viral_load.hiv_viral_load_date,
          sub_hiv_sample_collection.hiv_viral_load_date, NULL)              AS new_sample_collection_date,
       advanced_disease_status,
       family_planning_status,
       nutrition_assesment,
       nutrition_support_and_infant_feeding,
       hepatitis_b_test___qualitative,
       syphilis_test_result_for_partner,
       cervical_cancer_screening,
       tuberculosis_status,
       tpt_status,
       cd4_count,
       crag_test_results,
       who_hiv_clinical_stage,
       differentiated_service_delivery,
       baseline_cd4,
       baseline_regimen_start_date,
       IF(IFNULL(special_category, '') = '', '', 'Priority population(PP)') AS special_category,
       IF(line = 90271, 1,
          IF(line = 90305, 2,
             IF(line = 162987, 3, 1)))                                      AS regimen_line,
       health_education_setting,
       pss_issues_identified,
       art_preparation,
       depression_status,
       gender_based_violance,
       health_education_disclosure,
       ovc_screening,
       ovc_assessment,
       prevention_components,
       linkages_and_refferals,
       sessions                                                             AS iac_sessions,
       hivdr_results,
       date_hivr_results_recieved_at_facility,
       vl_after_iac.name,
       decision_outcome.name,
       TIMESTAMPDIFF(MONTH ,baseline_regimen_start_date, last_vist_date)    AS duration_on_art,
       sub_side_effects.medication_or_other_side_effects                    AS side_effects,
       specimen_sources,
       hiv_vl_date,
       relationship_child_status.no                                         AS known_status_children,
       relationship_child_pos.no                                            AS pos_status_children,
       relationship_spouse_status.no                                        AS known_status_spouse,
       relationship_spouse_pos.no                                           AS po_status_spouse

FROM (SELECT DISTINCT e.patient_id, birthdate, dead, gender
      FROM mamba_dim_encounter e
               INNER JOIN mamba_dim_person mdp ON e.patient_id = mdp.person_id
      WHERE mdp.voided = 0
        AND e.voided = 0
        AND e.encounter_type_uuid IN ('8d5b27bc-c2cc-11de-8d13-0010c6dffd0f', '8d5b2be0-c2cc-11de-8d13-0010c6dffd0f')
        AND e.encounter_datetime <= CURRENT_DATE()
        AND e.encounter_datetime >= DATE_SUB(CURRENT_DATE(), INTERVAL 12 MONTH)) cohort
         LEFT JOIN(SELECT client_id,
                          MAX(encounter_date) AS latest_encounter_date,
                          cd4
                   FROM mamba_fact_encounter_hiv_art_card
                   WHERE cd4 IS NOT NULL
                   GROUP BY client_id) sub_cd4 ON sub_cd4.client_id = patient_id
         LEFT JOIN (SELECT person_id, mdcn.name
                    FROM person_attribute pa
                             INNER JOIN person_attribute_type pat
                                        ON pa.person_attribute_type_id = pat.person_attribute_type_id
                             INNER JOIN mamba_dim_concept_name mdcn ON pa.value = mdcn.concept_id
                    WHERE pat.uuid = 'dec484be-1c43-416a-9ad0-18bd9ef28929'
                      AND pa.voided = 0
                      AND mdcn.locale = 'en'
                      AND mdcn.concept_name_type = 'FULLY_SPECIFIED') nationality ON nationality.person_id = patient_id
         LEFT JOIN (SELECT person_id, mdcn.name
                    FROM person_attribute pa
                             INNER JOIN person_attribute_type pat
                                        ON pa.person_attribute_type_id = pat.person_attribute_type_id
                             INNER JOIN mamba_dim_concept_name mdcn ON pa.value = mdcn.concept_id
                    WHERE pat.uuid = '8d871f2a-c2cc-11de-8d13-0010c6dffd0f'
                      AND pa.voided = 0
                      AND mdcn.locale = 'en'
                      AND mdcn.concept_name_type = 'FULLY_SPECIFIED') marital_status
                   ON marital_status.person_id = patient_id
         LEFT JOIN (SELECT client_id, MAX(encounter_datetime) AS last_vist_date
                    FROM mamba_flat_encounter_art_card
                    GROUP BY client_id) last_encounter ON last_encounter.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, return_visit_date
                    FROM mamba_fact_encounter_hiv_art_card b
                             JOIN (SELECT client_id,
                                          MAX(encounter_date) AS latest_encounter_date
                                   FROM mamba_fact_encounter_hiv_art_card
                                   WHERE return_visit_date IS NOT NULL
                                   GROUP BY client_id) a
                                  ON a.client_id = b.client_id AND
                                     encounter_date = latest_encounter_date) return_date
                   ON return_date.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, current_arv_regimen
                    FROM mamba_fact_encounter_hiv_art_card b
                             JOIN
                         (SELECT client_id, MAX(encounter_date) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_card
                          WHERE current_arv_regimen IS NOT NULL
                          GROUP BY client_id) a ON a.client_id = b.client_id AND
                                                   latest_encounter_date = encounter_date) sub_current_arv_regimen
                   ON sub_current_arv_regimen.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, adherence_assessment_code
                    FROM mamba_fact_encounter_hiv_art_card b
                             JOIN
                         (SELECT client_id, MAX(encounter_date) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_card
                          WHERE adherence_assessment_code IS NOT NULL
                          GROUP BY client_id) a ON encounter_date = latest_encounter_date AND
                                                   b.client_id = a.client_id) sub_adherence_assessment_code
                   ON sub_adherence_assessment_code.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, arv_regimen_days_dispensed
                    FROM mamba_fact_encounter_hiv_art_card b
                             JOIN
                         (SELECT client_id, MAX(encounter_date) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_card
                          WHERE arv_regimen_days_dispensed IS NOT NULL
                          GROUP BY client_id) a ON a.client_id = b.client_id AND
                                                   encounter_date =
                                                   latest_encounter_date) sub_arv_regimen_days_dispensed
                   ON sub_arv_regimen_days_dispensed.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, hiv_viral_load, hiv_viral_load_date, specimen_sources
                    FROM mamba_fact_encounter_hiv_art_card b
                             JOIN
                         (SELECT client_id,
                                 MAX(encounter_date) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_card
                          WHERE hiv_viral_load IS NOT NULL
                          GROUP BY client_id) a ON a.client_id = b.client_id AND
                                                   encounter_date = latest_encounter_date) sub_hiv_viral_load
                   ON sub_hiv_viral_load.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, hiv_viral_load_date
                    FROM mamba_fact_encounter_hiv_art_card b
                             JOIN
                         (SELECT client_id, MAX(encounter_date) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_card
                          WHERE hiv_viral_load IS NULL
                            AND hiv_viral_load_date IS NOT NULL
                          GROUP BY client_id) a
                         ON encounter_date = latest_encounter_date AND a.client_id = b.client_id) sub_hiv_sample_collection
                   ON sub_hiv_sample_collection.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, advanced_disease_status
                    FROM mamba_fact_encounter_hiv_art_card b
                             JOIN
                         (SELECT client_id, MAX(encounter_date) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_card
                          WHERE advanced_disease_status IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND latest_encounter_date = encounter_date) sub_advanced_disease_status
                   ON sub_advanced_disease_status.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, family_planning_status
                    FROM mamba_fact_encounter_hiv_art_card b
                             JOIN
                         (SELECT client_id, MAX(encounter_date) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_card
                          WHERE family_planning_status IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_date = latest_encounter_date) sub_family_planning_status
                   ON sub_family_planning_status.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, nutrition_assesment
                    FROM mamba_fact_encounter_hiv_art_card b
                             JOIN
                         (SELECT client_id, MAX(encounter_date) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_card
                          WHERE nutrition_assesment IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_date = latest_encounter_date) sub_nutrition_assesment
                   ON sub_nutrition_assesment.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, nutrition_support_and_infant_feeding
                    FROM mamba_fact_encounter_hiv_art_card b
                             JOIN
                         (SELECT client_id, MAX(encounter_date) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_card
                          WHERE nutrition_support_and_infant_feeding IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_date = latest_encounter_date) sub_nutrition_support_and_infant_feeding
                   ON sub_nutrition_support_and_infant_feeding.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, hepatitis_b_test___qualitative
                    FROM mamba_fact_encounter_hiv_art_card b
                             JOIN
                         (SELECT client_id, MAX(encounter_date) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_card
                          WHERE hepatitis_b_test___qualitative IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_date = latest_encounter_date) sub_hepatitis_b_test___qualitative
                   ON sub_hepatitis_b_test___qualitative.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, syphilis_test_result_for_partner
                    FROM mamba_fact_encounter_hiv_art_card b
                             JOIN
                         (SELECT client_id, MAX(encounter_date) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_card
                          WHERE syphilis_test_result_for_partner IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_date = latest_encounter_date) sub_syphilis_test_result_for_partner
                   ON sub_syphilis_test_result_for_partner.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, cervical_cancer_screening
                    FROM mamba_fact_encounter_hiv_art_card b
                             JOIN
                         (SELECT client_id, MAX(encounter_date) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_card
                          WHERE cervical_cancer_screening IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_date = latest_encounter_date) sub_cervical_cancer_screening
                   ON sub_cervical_cancer_screening.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, tuberculosis_status
                    FROM mamba_fact_encounter_hiv_art_card b
                             JOIN
                         (SELECT client_id, MAX(encounter_date) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_card
                          WHERE tuberculosis_status IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_date = latest_encounter_date) sub_tuberculosis_status
                   ON sub_tuberculosis_status.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, tpt_status
                    FROM mamba_fact_encounter_hiv_art_card b
                             JOIN
                         (SELECT client_id, MAX(encounter_date) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_card
                          WHERE tpt_status IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_date = latest_encounter_date) sub_tpt_status
                   ON sub_tpt_status.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, cd4_count
                    FROM mamba_fact_encounter_hiv_art_card b
                             JOIN
                         (SELECT client_id, MAX(encounter_date) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_card
                          WHERE cd4_count IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_date = latest_encounter_date) sub_cd4_count
                   ON sub_cd4_count.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, crag_test_results
                    FROM mamba_fact_encounter_hiv_art_card b
                             JOIN
                         (SELECT client_id, MAX(encounter_date) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_card
                          WHERE crag_test_results IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_date = latest_encounter_date) sub_crag_test_results
                   ON sub_crag_test_results.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, who_hiv_clinical_stage
                    FROM mamba_fact_encounter_hiv_art_card b
                             JOIN
                         (SELECT client_id, MAX(encounter_date) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_card
                          WHERE who_hiv_clinical_stage IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_date = latest_encounter_date) sub_who_hiv_clinical_stage
                   ON sub_who_hiv_clinical_stage.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, differentiated_service_delivery
                    FROM mamba_fact_encounter_hiv_art_card b
                             JOIN
                         (SELECT client_id, MAX(encounter_date) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_card
                          WHERE differentiated_service_delivery IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_date = latest_encounter_date) sub_differentiated_service_delivery
                   ON sub_differentiated_service_delivery.client_id = patient_id
         LEFT JOIN (SELECT client_id,
                           baseline_cd4,
                           baseline_regimen_start_date,
                           special_category
                    FROM mamba_fact_encounter_hiv_art_summary
                    GROUP BY client_id) sub_art_summary ON sub_art_summary.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, health_education_setting
                    FROM mamba_fact_encounter_hiv_art_health_education b
                             JOIN
                         (SELECT client_id, MAX(encounter_datetime) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_health_education
                          WHERE health_education_setting IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_datetime = latest_encounter_date) sub_health_education_setting
                   ON sub_health_education_setting.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, pss_issues_identified
                    FROM mamba_fact_encounter_hiv_art_health_education b
                             JOIN
                         (SELECT client_id, MAX(encounter_datetime) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_health_education
                          WHERE pss_issues_identified IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_datetime = latest_encounter_date) sub_pss_issues_identified
                   ON sub_pss_issues_identified.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, art_preparation
                    FROM mamba_fact_encounter_hiv_art_health_education b
                             JOIN
                         (SELECT client_id, MAX(encounter_datetime) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_health_education
                          WHERE art_preparation IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_datetime = latest_encounter_date) sub_art_preparation
                   ON sub_art_preparation.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, depression_status
                    FROM mamba_fact_encounter_hiv_art_health_education b
                             JOIN
                         (SELECT client_id, MAX(encounter_datetime) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_health_education
                          WHERE depression_status IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_datetime = latest_encounter_date) sub_depression_status
                   ON sub_depression_status.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, gender_based_violance
                    FROM mamba_fact_encounter_hiv_art_health_education b
                             JOIN
                         (SELECT client_id, MAX(encounter_datetime) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_health_education
                          WHERE gender_based_violance IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_datetime = latest_encounter_date) sub_gender_based_violance
                   ON sub_gender_based_violance.client_id = patient_id
         LEFT JOIN (SELECT client_id, MAX(encounter_datetime) AS latest_encounter_date, health_education_disclosure
                    FROM mamba_fact_encounter_hiv_art_health_education
                    WHERE health_education_disclosure IS NOT NULL
                    GROUP BY client_id) sub_health_education_disclosure
                   ON sub_health_education_disclosure.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, ovc_screening
                    FROM mamba_fact_encounter_hiv_art_health_education b
                             JOIN
                         (SELECT client_id, MAX(encounter_datetime) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_health_education
                          WHERE ovc_screening IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_datetime = latest_encounter_date) sub_ovc_screening
                   ON sub_ovc_screening.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, ovc_assessment
                    FROM mamba_fact_encounter_hiv_art_health_education b
                             JOIN
                         (SELECT client_id, MAX(encounter_datetime) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_health_education
                          WHERE ovc_assessment IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_datetime = latest_encounter_date) sub_ovc_assessment
                   ON sub_ovc_assessment.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, prevention_components
                    FROM mamba_fact_encounter_hiv_art_health_education b
                             JOIN
                         (SELECT client_id, MAX(encounter_datetime) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_health_education
                          WHERE prevention_components IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_datetime = latest_encounter_date) sub_prevention_components
                   ON sub_prevention_components.client_id = patient_id
         LEFT JOIN (SELECT a.client_id, linkages_and_refferals
                    FROM mamba_fact_encounter_hiv_art_health_education b
                             JOIN
                         (SELECT client_id, MAX(encounter_datetime) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_health_education
                          WHERE linkages_and_refferals IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_datetime = latest_encounter_date) sub_linkages_and_refferals
                   ON sub_linkages_and_refferals.client_id = patient_id
         LEFT JOIN (SELECT client_id, days_left_to_be_lost, transfer_out_date FROM mamba_fact_active_in_care) actives
                   ON actives.client_id = patient_id
         LEFT JOIN mamba_fact_current_arv_regimen_start_date mfcarsd ON mfcarsd.client_id = patient_id
         LEFT JOIN (SELECT pp.patient_id, program_workflow_state.concept_id AS line
                    FROM patient_state
                             INNER JOIN program_workflow_state
                                        ON patient_state.state = program_workflow_state.program_workflow_state_id
                             INNER JOIN program_workflow ON program_workflow_state.program_workflow_id =
                                                            program_workflow.program_workflow_id
                             INNER JOIN program ON program_workflow.program_id = program.program_id
                             INNER JOIN patient_program pp
                                        ON patient_state.patient_program_id = pp.patient_program_id AND
                                           program_workflow.concept_id = 166214 AND
                                           patient_state.end_date IS NULL) regimen_lines
                   ON cohort.patient_id = regimen_lines.patient_id
         LEFT JOIN (SELECT obs.person_id, COUNT(value_datetime) sessions
                    FROM obs
                             INNER JOIN (SELECT person_id, MAX(DATE (value_datetime)) AS vldate
                                         FROM obs
                                         WHERE concept_id = 163023
                                           AND voided = 0
                                           AND value_datetime <= CURRENT_DATE()
                                           AND obs_datetime <= CURRENT_DATE()
                                         GROUP BY person_id) vl_date ON vl_date.person_id = obs.person_id
                    WHERE concept_id = 163154
                      AND value_datetime >= vldate
                      AND obs_datetime BETWEEN DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) AND CURRENT_DATE()
                    GROUP BY obs.person_id) iac ON cohort.patient_id = iac.person_id
         LEFT JOIN (SELECT a.client_id, hivdr_sample_collected
                    FROM mamba_fact_encounter_non_suppressed_card b
                             JOIN
                         (SELECT client_id, MAX(encounter_date) AS hivdr_sample_collected_date
                          FROM mamba_fact_encounter_non_suppressed_card
                          WHERE hivdr_sample_collected IS NOT NULL
                          GROUP BY client_id) a ON a.client_id = b.client_id AND encounter_date =
                                                                                 hivdr_sample_collected_date) sub_hivdr_sample_collected
                   ON sub_hiv_sample_collection.client_id = cohort.patient_id
         LEFT JOIN (SELECT a.client_id, hivdr_results
                    FROM mamba_fact_encounter_non_suppressed_card b
                             JOIN
                         (SELECT client_id, MAX(encounter_date) AS latest_encounter_date
                          FROM mamba_fact_encounter_non_suppressed_card
                          WHERE hivdr_results IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_date = latest_encounter_date) sub_hivdr_results
                   ON sub_hivdr_results.client_id = cohort.patient_id
         LEFT JOIN (SELECT pi.patient_id AS patientid, identifier
                    FROM patient_identifier pi
                             INNER JOIN patient_identifier_type pit
                                        ON pi.identifier_type = pit.patient_identifier_type_id AND
                                           pit.uuid = 'e1731641-30ab-102d-86b0-7a5022ba4115'
                    WHERE pi.voided = 0
                    GROUP BY pi.patient_id) identifiers ON cohort.patient_id = identifiers.patientid
         LEFT JOIN (SELECT a.client_id, date_hivr_results_recieved_at_facility
                    FROM mamba_fact_encounter_non_suppressed_card b
                             JOIN
                         (SELECT client_id,
                                 MAX(encounter_date) AS latest_encounter_date
                          FROM mamba_fact_encounter_non_suppressed_card
                          WHERE date_hivr_results_recieved_at_facility IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_date = latest_encounter_date) sub_date_hivr_results_recieved_at_facility
                   ON sub_date_hivr_results_recieved_at_facility.client_id = cohort.patient_id
         LEFT JOIN (SELECT o.person_id, cn.name
                    FROM obs o
                             INNER JOIN encounter e ON o.encounter_id = e.encounter_id
                             INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id AND
                                                             et.uuid = '38cb2232-30fc-4b1f-8df1-47c795771ee9'
                             INNER JOIN (SELECT person_id, MAX(obs_datetime) latest_date
                                         FROM obs
                                         WHERE concept_id = 163166
                                           AND voided = 0
                                         GROUP BY person_id) a ON o.person_id = a.person_id
                             LEFT JOIN concept_name cn
                                       ON value_coded = cn.concept_id AND cn.concept_name_type = 'FULLY_SPECIFIED' AND
                                          cn.locale = 'en'
                    WHERE o.concept_id = 163166
                      AND obs_datetime = a.latest_date
                      AND o.voided = 0
                      AND obs_datetime <= CURRENT_DATE()
                    GROUP BY o.person_id) decision_outcome ON cohort.patient_id = decision_outcome.person_id
         LEFT JOIN (SELECT o.person_id, cn.name
                    FROM obs o
                             INNER JOIN encounter e ON o.encounter_id = e.encounter_id
                             INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id AND
                                                             et.uuid = '38cb2232-30fc-4b1f-8df1-47c795771ee9'
                             INNER JOIN (SELECT person_id, MAX(obs_datetime) latest_date
                                         FROM obs
                                         WHERE concept_id = 1305
                                           AND voided = 0
                                         GROUP BY person_id) a ON o.person_id = a.person_id
                             LEFT JOIN concept_name cn
                                       ON value_coded = cn.concept_id AND cn.concept_name_type = 'FULLY_SPECIFIED' AND
                                          cn.locale = 'en'
                    WHERE o.concept_id = 1305
                      AND obs_datetime = a.latest_date
                      AND o.voided = 0
                      AND obs_datetime <= CURRENT_DATE()
                    GROUP BY o.person_id) vl_after_iac ON cohort.patient_id = vl_after_iac.person_id
         LEFT JOIN (SELECT a.client_id, medication_or_other_side_effects
                    FROM mamba_fact_encounter_hiv_art_card b
                             JOIN
                         (SELECT client_id, MAX(encounter_date) AS latest_encounter_date
                          FROM mamba_fact_encounter_hiv_art_card
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_date = latest_encounter_date
                    WHERE medication_or_other_side_effects is not NULL) sub_side_effects
                   ON sub_side_effects.client_id = cohort.patient_id
         LEFT JOIN (SELECT a.client_id, hiv_vl_date
                    FROM mamba_fact_encounter_non_suppressed_card b
                             JOIN
                         (SELECT client_id, MAX(encounter_date) AS latest_encounter_date
                          FROM mamba_fact_encounter_non_suppressed_card
                          WHERE hiv_vl_date IS NOT NULL
                          GROUP BY client_id) a
                         ON a.client_id = b.client_id AND encounter_date = latest_encounter_date) sub_hiv_vl_date
                   ON sub_hiv_vl_date.client_id = cohort.patient_id
         LEFT JOIN (SELECT age.person_id, COUNT(*) AS no
                    FROM (SELECT family.person_id, obs_group_id
                        FROM obs family
                        INNER JOIN (SELECT o.person_id, obs_id
                        FROM obs o
                        WHERE concept_id = 99075
                        AND o.voided = 0) b
                        ON family.obs_group_id = b.obs_id
                        WHERE concept_id = 164352
                        AND value_coded = 90280) relationship_child
                        JOIN (SELECT family.person_id, obs_group_id
                        FROM obs family
                        INNER JOIN (SELECT o.person_id, obs_id
                        FROM obs o
                        WHERE concept_id = 99075
                        AND o.voided = 0) b
                        ON family.obs_group_id = b.obs_id
                        WHERE concept_id = 99074
                        AND (TIMESTAMPDIFF(YEAR, obs_datetime, CURRENT_DATE ()) + value_numeric) <= 19) age
                    ON relationship_child.obs_group_id = age.obs_group_id
                        INNER JOIN (SELECT family.person_id, obs_group_id
                        FROM obs family
                        INNER JOIN (SELECT o.person_id, obs_id
                        FROM obs o
                        WHERE concept_id = 99075
                        AND o.voided = 0) b
                        ON family.obs_group_id = b.obs_id
                        WHERE concept_id = 165275) status ON status.obs_group_id = age.obs_group_id
                    GROUP BY age.person_id) relationship_child_status
                   ON relationship_child_status.person_id = cohort.patient_id
         LEFT JOIN (SELECT age.person_id, COUNT(*) AS no
                    FROM (SELECT family.person_id, obs_group_id
                        FROM obs family
                        INNER JOIN (SELECT o.person_id, obs_id
                        FROM obs o
                        WHERE concept_id = 99075
                        AND o.voided = 0) b
                        ON family.obs_group_id = b.obs_id
                        WHERE concept_id = 164352
                        AND value_coded = 90280) relationship_child
                        JOIN (SELECT family.person_id, obs_group_id
                        FROM obs family
                        INNER JOIN (SELECT o.person_id, obs_id
                        FROM obs o
                        WHERE concept_id = 99075
                        AND o.voided = 0) b
                        ON family.obs_group_id = b.obs_id
                        WHERE concept_id = 99074
                        AND (TIMESTAMPDIFF(YEAR, obs_datetime, CURRENT_DATE ()) + value_numeric) <= 19) age
                    ON relationship_child.obs_group_id = age.obs_group_id
                        INNER JOIN (SELECT family.person_id, obs_group_id
                        FROM obs family
                        INNER JOIN (SELECT o.person_id, obs_id
                        FROM obs o
                        WHERE concept_id = 99075
                        AND o.voided = 0) b
                        ON family.obs_group_id = b.obs_id
                        WHERE concept_id = 165275
                        AND value_coded = 90166) status ON status.obs_group_id = age.obs_group_id
                    GROUP BY age.person_id) relationship_child_pos
                   ON relationship_child_pos.person_id = cohort.patient_id
         LEFT JOIN (SELECT status.person_id, COUNT(*) AS no
                    FROM (SELECT family.person_id, obs_group_id
                        FROM obs family
                        INNER JOIN (SELECT o.person_id, obs_id
                        FROM obs o
                        WHERE concept_id = 99075 AND o.voided = 0) b
                        ON family.obs_group_id = b.obs_id
                        WHERE concept_id = 164352
                        AND value_coded IN (90288, 165274)) relationship_spouse
                        INNER JOIN (SELECT family.person_id, obs_group_id
                        FROM obs family
                        INNER JOIN (SELECT o.person_id, obs_id
                        FROM obs o
                        WHERE concept_id = 99075 AND o.voided = 0) b
                        ON family.obs_group_id = b.obs_id
                        WHERE concept_id = 165275) status
                    ON status.obs_group_id = relationship_spouse.obs_group_id
                    GROUP BY status.person_id) relationship_spouse_status
                   ON relationship_spouse_status.person_id = cohort.patient_id
         LEFT JOIN (SELECT status.person_id, COUNT(*) AS no
                    FROM (SELECT family.person_id, obs_group_id
                        FROM obs family
                        INNER JOIN (SELECT o.person_id, obs_id
                        FROM obs o
                        WHERE concept_id = 99075 AND o.voided = 0) b
                        ON family.obs_group_id = b.obs_id
                        WHERE concept_id = 164352
                        AND value_coded IN (90288, 165274)) relationship_spouse
                        INNER JOIN (SELECT family.person_id, obs_group_id
                        FROM obs family
                        INNER JOIN (SELECT o.person_id, obs_id
                        FROM obs o
                        WHERE concept_id = 99075 AND o.voided = 0) b
                        ON family.obs_group_id = b.obs_id
                        WHERE concept_id = 165275
                        AND value_coded = 90166) status
                    ON status.obs_group_id = relationship_spouse.obs_group_id
                    GROUP BY status.person_id) relationship_spouse_pos
                   ON relationship_spouse_pos.person_id = cohort.patient_id;
-- $END