-- $BEGIN
CREATE TABLE mamba_fact_encounter_hiv_art_health_education
(
    id                          INT AUTO_INCREMENT,
    encounter_id                INT NULL,
    client_id                   INT NULL,
    encounter_datetime          DATE NULL,
    ovc_screening               VARCHAR(255)  DEFAULT NULL,
    other_linkages              VARCHAR(255)  DEFAULT NULL,
    ovc_assessment              VARCHAR(255)  DEFAULT NULL,
    art_preparation             VARCHAR(255)  DEFAULT NULL,
    depression_status           VARCHAR(255)  DEFAULT NULL,
    gender_based_violance       VARCHAR(255)  DEFAULT NULL,
    other_phdp_components       VARCHAR(255)  DEFAULT NULL,
    prevention_components       VARCHAR(255)  DEFAULT NULL,
    pss_issues_identified       VARCHAR(255)  DEFAULT NULL,
    intervation_approaches      VARCHAR(255)  DEFAULT NULL,
    linkages_and_refferals      VARCHAR(255)  DEFAULT NULL,
    clinic_contact_comments     TEXT  DEFAULT NULL,
    scheduled_patient_visit     VARCHAR(255)  DEFAULT NULL,
    health_education_setting    VARCHAR(255)  DEFAULT NULL,
    clinical_impression_comment TEXT  DEFAULT NULL,
    health_education_disclosure VARCHAR(255)  DEFAULT NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8;


CREATE INDEX
    mamba_fact_encounter_hiv_art_health_education_client_id_index ON mamba_fact_encounter_hiv_art_health_education (client_id);

CREATE INDEX
    mamba_fact_health_education_encounter_id_index ON mamba_fact_encounter_hiv_art_health_education (encounter_id);


-- $END

