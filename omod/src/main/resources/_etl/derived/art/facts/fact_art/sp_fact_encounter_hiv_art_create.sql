-- $BEGIN
CREATE TABLE mamba_fact_encounter_hiv_art
(
    id              INT AUTO_INCREMENT,
    encounter_id    INT NULL,
    client_id       INT NULL,
    return_date     DATE NULL,
    current_regimen CHAR(255) CHARACTER SET UTF8MB4 NULL,
    who_stage       CHAR(255) CHARACTER SET UTF8MB4 NULL,
    no_of_days      CHAR(255) CHARACTER SET UTF8MB4 NULL,
    no_of_pills     INT NULL,
    tb_status       CHAR(255) CHARACTER SET UTF8MB4 NULL,
    dsdm            CHAR(255) CHARACTER SET UTF8MB4 NULL,
    pregnant        CHAR(255) CHARACTER SET UTF8MB4 NULL,
    emtct           CHAR(255) CHARACTER SET UTF8MB4 NULL,
    PRIMARY KEY (id)
);

CREATE INDEX
    mamba_fact_encounter_hiv_art_return_date_index ON mamba_fact_encounter_hiv_art (return_date);
-- $END

