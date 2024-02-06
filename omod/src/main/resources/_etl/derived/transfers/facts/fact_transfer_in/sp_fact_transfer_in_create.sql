-- $BEGIN
CREATE TABLE IF NOT EXISTS mamba_fact_transfer_in
(
    id                       INT AUTO_INCREMENT,
    client_id                         INT           NULL,
    encounter_date                    DATE          NOT NULL,
    transfer_in_date                  DATE    NOT NULL,

    PRIMARY KEY (id)

);

CREATE INDEX
    mamba_fact_transfer_in_client_id_index ON mamba_fact_transfer_in (client_id);
-- $END
