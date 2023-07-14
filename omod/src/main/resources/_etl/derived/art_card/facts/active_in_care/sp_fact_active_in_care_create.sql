-- $BEGIN
CREATE TABLE mamba_fact_active_in_care
(
    id                   INT AUTO_INCREMENT,
    client_id            INT  NULL,
    latest_return_date   DATE NULL,

    days_left_to_be_lost INT  NULL,
    last_encounter_date  DATE NULL,
    dead                 INT NULL,
    transfer_out_date    DATE NULL,


    PRIMARY KEY (id)
) CHARSET = UTF8MB4;

-- $END

