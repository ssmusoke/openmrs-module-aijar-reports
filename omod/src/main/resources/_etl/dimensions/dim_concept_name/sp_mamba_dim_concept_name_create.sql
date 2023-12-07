-- $BEGIN

CREATE TABLE mamba_dim_concept_name
(
    id                INT          NOT NULL AUTO_INCREMENT,
    concept_name_id   INT          NOT NULL,
    concept_id        INT,
    name              VARCHAR(255) NOT NULL,
    locale            VARCHAR(50)  NOT NULL,
    locale_preferred  TINYINT,
    concept_name_type VARCHAR(255),

    PRIMARY KEY (id)
)
    CHARSET = UTF8;

CREATE INDEX mamba_dim_concept_name_concept_name_id_index
    ON mamba_dim_concept_name (concept_name_id);

CREATE INDEX mamba_dim_concept_name_concept_id_index
    ON mamba_dim_concept_name (concept_id);

CREATE INDEX mamba_dim_concept_name_concept_name_type_index
    ON mamba_dim_concept_name (concept_name_type);

CREATE INDEX mamba_dim_concept_name_locale_index
    ON mamba_dim_concept_name (locale);

CREATE INDEX mamba_dim_concept_name_locale_preferred_index
    ON mamba_dim_concept_name (locale_preferred);

-- $END
