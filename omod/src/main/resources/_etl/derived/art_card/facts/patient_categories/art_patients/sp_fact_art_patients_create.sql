-- $BEGIN
CREATE TABLE mamba_fact_art_patients
(
    id        INT AUTO_INCREMENT,
    client_id INT NULL,
    birthdate DATE NULL,
    age       INT NULL,
    gender    VARCHAR(10) NULL,
    dead      BIT NULL,
    age_group VARCHAR(20) NULL,


    PRIMARY KEY (id)
) CHARSET = UTF8MB4;

-- $END

