-- $BEGIN
CREATE TABLE mamba_fact_patients_latest_patient_demographics
(
    id         INT AUTO_INCREMENT,
    patient_id INT NOT NULL,
    birthdate  DATE NULL,
    age        INT NULL,
    gender     VARCHAR(10) NULL,
    dead       BIT NOT NULL DEFAULT 0,

    PRIMARY KEY (id)
) CHARSET = UTF8MB4;

-- $END

