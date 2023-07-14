-- $BEGIN
CREATE TABLE mamba_fact_encounter_hiv_art_health_education
(
    id                          INT AUTO_INCREMENT,
    encounter_id                INT NULL,
    client_id                   INT NULL,
    encounter_datetime          DATE NULL,
    ovc_screening               VARCHAR(50)  DEFAULT NULL,
    other_linkages              VARCHAR(50)  DEFAULT NULL,
    ovc_assessment              VARCHAR(50)  DEFAULT NULL,
    art_preparation             VARCHAR(50)  DEFAULT NULL,
    depression_status           VARCHAR(50)  DEFAULT NULL,
    gender_based_violance       VARCHAR(50)  DEFAULT NULL,
    other_phdp_components       VARCHAR(50)  DEFAULT NULL,
    prevention_components       VARCHAR(50)  DEFAULT NULL,
    pss_issues_identified       VARCHAR(50)  DEFAULT NULL,
    intervation_approaches      VARCHAR(50)  DEFAULT NULL,
    linkages_and_refferals      VARCHAR(50)  DEFAULT NULL,
    clinic_contact_comments     VARCHAR(250)  DEFAULT NULL,
    scheduled_patient_visit     VARCHAR(50)  DEFAULT NULL,
    health_education_setting    VARCHAR(50)  DEFAULT NULL,
    clinical_impression_comment VARCHAR(250)  DEFAULT NULL,
    health_education_disclosure VARCHAR(50)  DEFAULT NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8MB4;

-- $END

