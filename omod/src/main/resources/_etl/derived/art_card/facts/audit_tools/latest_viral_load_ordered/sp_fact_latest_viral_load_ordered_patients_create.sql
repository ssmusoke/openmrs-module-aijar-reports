-- $BEGIN
CREATE TABLE mamba_fact_patients_latest_viral_load_ordered
(
    id                                      INT AUTO_INCREMENT,
    client_id                               INT NOT NULL,
    encounter_date                          DATE NULL,
    order_date                             DATE NULL,

    PRIMARY KEY (id)
) CHARSET = UTF8MB4;

-- $END

