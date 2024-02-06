-- $BEGIN
CREATE TABLE mamba_fact_patients_interruptions_details
(
    id                               INT AUTO_INCREMENT,
    client_id                        INT          NOT NULL,
    case_id                          VARCHAR(250) NOT NULL,
    art_enrollment_date              DATE NULL,
    days_since_initiation            INT NULL,
    last_dispense_date               DATE NULL,
    last_dispense_amount             INT NULL,
    current_regimen_start_date       DATE NULL,
    last_VL_result                   INT NULL,
    VL_last_date                     DATE NULL,
    last_dispense_description        VARCHAR(250) NULL,
    all_interruptions                INT NULL,
    iit_in_last_12Months             INT NULL,
    longest_IIT_ever                 INT NULL,
    last_IIT_duration                INT NULL,
    last_encounter_interruption_date DATE NULL,


    PRIMARY KEY (id)
) CHARSET = UTF8;

CREATE INDEX
    mamba_fact_patients_interruptions_details_client_id_index ON mamba_fact_patients_interruptions_details (client_id);
CREATE INDEX
    mamba_fact_patients_interruptions_details_case_id_index ON mamba_fact_patients_interruptions_details (case_id);

-- $END

