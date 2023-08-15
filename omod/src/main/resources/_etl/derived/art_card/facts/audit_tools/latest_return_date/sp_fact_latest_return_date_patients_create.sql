-- $BEGIN
CREATE TABLE mamba_fact_patients_latest_return_date
(
    id                                      INT AUTO_INCREMENT,
    client_id                               INT NOT NULL,
    return_date                             DATE NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8MB4;

-- $END

