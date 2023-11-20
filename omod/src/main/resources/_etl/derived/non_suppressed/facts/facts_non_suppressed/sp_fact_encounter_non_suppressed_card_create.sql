-- $BEGIN
CREATE TABLE mamba_fact_encounter_non_suppressed_card
(
    id                                     INT AUTO_INCREMENT,
    encounter_id                           INT NULL,
    client_id                              INT NULL,
    encounter_date                         DATE NULL,

    vl_qualitative                         VARCHAR(80) NULL,
    register_serial_number                 VARCHAR(80) NULL,
    cd4_count                              INT NULL,
    tuberculosis_status                    VARCHAR(80) NULL,
    current_arv_regimen                    VARCHAR(80) NULL,
    breast_feeding                         VARCHAR(80) NULL,
    eligible_for_art_pregnant              VARCHAR(80) NULL,
    clinical_impression_comment            VARCHAR(80) NULL,
    hiv_vl_date                            VARCHAR(80) NULL,
    date_vl_results_received_at_facility   DATE NULL,
    session_date                           DATE NULL,
    adherence_assessment_score             VARCHAR(80) NULL,
    date_vl_results_given_to_client        DATE NULL,
    serum_crag_screening_result            VARCHAR(80) NULL,
    serum_crag_screening                   VARCHAR(80) NULL,
    restarted_iac                          VARCHAR(80) NULL,
    hivdr_sample_collected                 VARCHAR(80) NULL,
    tb_lam_results                         VARCHAR(80) NULL,
    date_cd4_sample_collected              DATE NULL,
    date_of_vl_sample_collection           DATE NULL,
    on_fluconazole_treatment               VARCHAR(80) NULL,
    tb_lam_test_done                       VARCHAR(80) NULL,
    date_hivr_results_recieved_at_facility DATE NULL,
    hivdr_results                          VARCHAR(80) NULL,
        PRIMARY KEY (id)
) CHARSET = UTF8;

-- $END

