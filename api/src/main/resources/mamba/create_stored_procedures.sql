
        
    
        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  fn_calculate_agegroup  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP FUNCTION IF EXISTS fn_calculate_agegroup;

CREATE FUNCTION fn_calculate_agegroup(age INT) RETURNS VARCHAR(15)
    DETERMINISTIC
BEGIN
    DECLARE agegroup VARCHAR(15);
    IF (age < 1) THEN
        SET agegroup = '<1';
    ELSEIF age between 1 and 4 THEN
        SET agegroup = '1-4';
    ELSEIF age between 5 and 9 THEN
        SET agegroup = '5-9';
    ELSEIF age between 10 and 14 THEN
        SET agegroup = '10-14';
    ELSEIF age between 15 and 19 THEN
        SET agegroup = '15-19';
    ELSEIF age between 20 and 24 THEN
        SET agegroup = '20-24';
    ELSEIF age between 25 and 29 THEN
        SET agegroup = '25-29';
    ELSEIF age between 30 and 34 THEN
        SET agegroup = '30-34';
    ELSEIF age between 35 and 39 THEN
        SET agegroup = '35-39';
    ELSEIF age between 40 and 44 THEN
        SET agegroup = '40-44';
    ELSEIF age between 45 and 49 THEN
        SET agegroup = '45-49';
    ELSEIF age between 50 and 54 THEN
        SET agegroup = '50-54';
    ELSEIF age between 55 and 59 THEN
        SET agegroup = '55-59';
    ELSEIF age between 60 and 64 THEN
        SET agegroup = '60-64';
    ELSE
        SET agegroup = '65+';
    END IF;

    RETURN (agegroup);
END //

DELIMITER ;


        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  fn_get_obs_value_column  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP FUNCTION IF EXISTS fn_get_obs_value_column;

CREATE FUNCTION fn_get_obs_value_column(conceptDatatype VARCHAR(20)) RETURNS VARCHAR(20)
    DETERMINISTIC
BEGIN
    DECLARE obsValueColumn VARCHAR(20);
    IF (conceptDatatype = 'Text' OR conceptDatatype = 'Coded' OR conceptDatatype = 'N/A' OR
        conceptDatatype = 'Boolean') THEN
        SET obsValueColumn = 'obs_value_text';
    ELSEIF conceptDatatype = 'Date' OR conceptDatatype = 'Datetime' THEN
        SET obsValueColumn = 'obs_value_datetime';
    ELSEIF conceptDatatype = 'Numeric' THEN
        SET obsValueColumn = 'obs_value_numeric';
    END IF;

    RETURN (obsValueColumn);
END//

DELIMITER ;


        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_xf_system_drop_all_functions_in_schema  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_xf_system_drop_all_stored_functions_in_schema;

CREATE PROCEDURE sp_xf_system_drop_all_stored_functions_in_schema(
    IN database_name CHAR(255) CHARACTER SET UTF8MB4
)
BEGIN
    DELETE FROM `mysql`.`proc` WHERE `type` = 'FUNCTION' AND `db` = database_name; -- works in mysql before v.8

END //

DELIMITER ;


        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_xf_system_drop_all_stored_procedures_in_schema  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_xf_system_drop_all_stored_procedures_in_schema;

CREATE PROCEDURE sp_xf_system_drop_all_stored_procedures_in_schema(
    IN database_name CHAR(255) CHARACTER SET UTF8MB4
)
BEGIN

    DELETE FROM `mysql`.`proc` WHERE `type` = 'PROCEDURE' AND `db` = database_name; -- works in mysql before v.8

END //

DELIMITER ;


        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_xf_system_drop_all_objects_in_schema  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_xf_system_drop_all_objects_in_schema;

CREATE PROCEDURE sp_xf_system_drop_all_objects_in_schema(
    IN database_name CHAR(255) CHARACTER SET UTF8MB4
)
BEGIN

    CALL sp_xf_system_drop_all_stored_functions_in_schema(database_name);
    CALL sp_xf_system_drop_all_stored_procedures_in_schema(database_name);
    CALL sp_xf_system_drop_all_tables_in_schema(database_name);
    # CALL sp_xf_system_drop_all_views_in_schema (database_name);

END //

DELIMITER ;


        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_xf_system_drop_all_tables_in_schema  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_xf_system_drop_all_tables_in_schema;

-- CREATE PROCEDURE sp_xf_system_drop_all_tables_in_schema(IN database_name CHAR(255) CHARACTER SET UTF8MB4)
CREATE PROCEDURE sp_xf_system_drop_all_tables_in_schema()
BEGIN

    DECLARE tables_count INT;

    SET @database_name = (SELECT DATABASE());

    SELECT COUNT(1)
    INTO tables_count
    FROM information_schema.tables
    WHERE TABLE_TYPE = 'BASE TABLE'
      AND TABLE_SCHEMA = @database_name;

    IF tables_count > 0 THEN

        SET session group_concat_max_len = 20000;

        SET @tbls = (SELECT GROUP_CONCAT(@database_name, '.', TABLE_NAME SEPARATOR ', ')
                     FROM information_schema.tables
                     WHERE TABLE_TYPE = 'BASE TABLE'
                       AND TABLE_SCHEMA = @database_name
                       AND TABLE_NAME REGEXP '^(mamba_|dim_|fact_|flat_)');

        IF (@tbls IS NOT NULL) THEN

            SET @drop_tables = CONCAT('DROP TABLE IF EXISTS ', @tbls);

            SET foreign_key_checks = 0; -- Remove check, so we don't have to drop tables in the correct order, or care if they exist or not.
            PREPARE drop_tbls FROM @drop_tables;
            EXECUTE drop_tbls;
            DEALLOCATE PREPARE drop_tbls;
            SET foreign_key_checks = 1;

        END IF;

    END IF;

END //

DELIMITER ;


        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_xf_system_execute_etl  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_xf_system_execute_etl;

CREATE PROCEDURE sp_xf_system_execute_etl()
BEGIN
    DECLARE error_message VARCHAR(255) DEFAULT 'OK';
    DECLARE error_code CHAR(5) DEFAULT '00000';

    DECLARE start_time bigint;
    DECLARE end_time bigint;
    DECLARE start_date_time DATETIME;
    DECLARE end_date_time DATETIME;

    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
        BEGIN
            GET DIAGNOSTICS CONDITION 1
                error_code = RETURNED_SQLSTATE,
                error_message = MESSAGE_TEXT;

            -- SET @sql = CONCAT('SIGNAL SQLSTATE ''', error_code, ''' SET MESSAGE_TEXT = ''', error_message, '''');
            -- SET @sql = CONCAT('SET @signal = ''', @sql, '''');

            -- SET @sql = CONCAT('SIGNAL SQLSTATE ''', error_code, ''' SET MESSAGE_TEXT = ''', error_message, '''');
            -- PREPARE stmt FROM @sql;
            -- EXECUTE stmt;
            -- DEALLOCATE PREPARE stmt;

            INSERT INTO zzmamba_etl_tracker (initial_run_date,
                                             start_date,
                                             end_date,
                                             time_taken_microsec,
                                             completion_status,
                                             success_or_error_message,
                                             next_run_date)
            SELECT NOW(),
                   start_date_time,
                   NOW(),
                   (((UNIX_TIMESTAMP(NOW()) * 1000000 + MICROSECOND(NOW(6))) - @start_time) / 1000),
                   'ERROR',
                   (CONCAT(error_code, ' : ', error_message)),
                   NOW() + 5;
        END;

    -- Fix start time in microseconds
    SET start_date_time = NOW();
    SET @start_time = (UNIX_TIMESTAMP(NOW()) * 1000000 + MICROSECOND(NOW(6)));

    CALL sp_data_processing_etl();

    -- Fix end time in microseconds
    SET end_date_time = NOW();
    SET @end_time = (UNIX_TIMESTAMP(NOW()) * 1000000 + MICROSECOND(NOW(6)));

    -- Result
    SET @time_taken = (@end_time - @start_time) / 1000;
    SELECT @time_taken;


    INSERT INTO zzmamba_etl_tracker (initial_run_date,
                                     start_date,
                                     end_date,
                                     time_taken_microsec,
                                     completion_status,
                                     success_or_error_message,
                                     next_run_date)
    SELECT NOW(), start_date_time, end_date_time, @time_taken, 'SUCCESS', 'OK', NOW() + 5;

END //

DELIMITER ;


        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_flat_encounter_table_create  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_flat_encounter_table_create;

CREATE PROCEDURE sp_flat_encounter_table_create(
    IN flat_encounter_table_name CHAR(255) CHARACTER SET UTF8MB4
)
BEGIN

    SET session group_concat_max_len = 20000;
    SET @column_labels := NULL;

    SET @drop_table = CONCAT('DROP TABLE IF EXISTS `', flat_encounter_table_name, '`');

    SELECT GROUP_CONCAT(column_label SEPARATOR ' TEXT, ')
    INTO @column_labels
    FROM mamba_dim_concept_metadata
    WHERE flat_table_name = flat_encounter_table_name
      AND concept_datatype IS NOT NULL;

    IF @column_labels IS NULL THEN
        SET @create_table = CONCAT(
                'CREATE TABLE `', flat_encounter_table_name, '` (encounter_id INT, client_id INT);');
    ELSE
        SET @create_table = CONCAT(
                'CREATE TABLE `', flat_encounter_table_name, '` (encounter_id INT, client_id INT, ', @column_labels,
                ' TEXT);');
    END IF;


    PREPARE deletetb FROM @drop_table;
    PREPARE createtb FROM @create_table;

    EXECUTE deletetb;
    EXECUTE createtb;

    DEALLOCATE PREPARE deletetb;
    DEALLOCATE PREPARE createtb;

END //

DELIMITER ;


        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_flat_encounter_table_create_all  ----------------------------
-- ---------------------------------------------------------------------------------------------

-- Flatten all Encounters given in Config folder
DELIMITER //

DROP PROCEDURE IF EXISTS sp_flat_encounter_table_create_all;

CREATE PROCEDURE sp_flat_encounter_table_create_all()
BEGIN

    DECLARE tbl_name CHAR(50) CHARACTER SET UTF8MB4;

    DECLARE done INT DEFAULT FALSE;

    DECLARE cursor_flat_tables CURSOR FOR
        SELECT DISTINCT(flat_table_name) FROM mamba_dim_concept_metadata;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cursor_flat_tables;
    computations_loop:
    LOOP
        FETCH cursor_flat_tables INTO tbl_name;

        IF done THEN
            LEAVE computations_loop;
        END IF;

        CALL sp_flat_encounter_table_create(tbl_name);

    END LOOP computations_loop;
    CLOSE cursor_flat_tables;

END //

DELIMITER ;


        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_flat_encounter_table_insert  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_flat_encounter_table_insert;

CREATE PROCEDURE sp_flat_encounter_table_insert(
    IN flat_encounter_table_name CHAR(255) CHARACTER SET UTF8MB4
)
BEGIN

    SET session group_concat_max_len = 20000;
    SET @tbl_name = flat_encounter_table_name;

    SET @old_sql = (SELECT GROUP_CONCAT(COLUMN_NAME SEPARATOR ', ')
                    FROM INFORMATION_SCHEMA.COLUMNS
                    WHERE TABLE_NAME = @tbl_name
                      AND TABLE_SCHEMA = Database());

    SELECT GROUP_CONCAT(DISTINCT
                        CONCAT(' MAX(CASE WHEN column_label = ''', column_label, ''' THEN ',
                               fn_get_obs_value_column(concept_datatype), ' END) ', column_label)
                        ORDER BY concept_metadata_id ASC)
    INTO @column_labels
    FROM mamba_dim_concept_metadata
    WHERE flat_table_name = @tbl_name;

    SET @insert_stmt = CONCAT(
            'INSERT INTO `', @tbl_name, '` SELECT eo.encounter_id, eo.person_id, ', @column_labels, '
            FROM mamba_z_encounter_obs eo
                INNER JOIN mamba_dim_concept_metadata cm
                ON IF(cm.concept_answer_obs=1, cm.concept_uuid=eo.obs_value_coded_uuid, cm.concept_uuid=eo.obs_question_uuid)
            WHERE cm.flat_table_name = ''', @tbl_name, '''
            AND eo.encounter_type_uuid = cm.encounter_type_uuid
            GROUP BY eo.encounter_id, eo.person_id;');

    PREPARE inserttbl FROM @insert_stmt;
    EXECUTE inserttbl;
    DEALLOCATE PREPARE inserttbl;

END //

DELIMITER ;


        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_flat_encounter_table_insert_all  ----------------------------
-- ---------------------------------------------------------------------------------------------

-- Flatten all Encounters given in Config folder
DELIMITER //

DROP PROCEDURE IF EXISTS sp_flat_encounter_table_insert_all;

CREATE PROCEDURE sp_flat_encounter_table_insert_all()
BEGIN

    DECLARE tbl_name CHAR(50) CHARACTER SET UTF8MB4;

    DECLARE done INT DEFAULT FALSE;

    DECLARE cursor_flat_tables CURSOR FOR
        SELECT DISTINCT(flat_table_name) FROM mamba_dim_concept_metadata;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cursor_flat_tables;
    computations_loop:
    LOOP
        FETCH cursor_flat_tables INTO tbl_name;

        IF done THEN
            LEAVE computations_loop;
        END IF;

        CALL sp_flat_encounter_table_insert(tbl_name);

    END LOOP computations_loop;
    CLOSE cursor_flat_tables;

END //

DELIMITER ;


        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_multiselect_values_update  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS `sp_multiselect_values_update`;

CREATE PROCEDURE `sp_multiselect_values_update`(
    IN table_to_update CHAR(100) CHARACTER SET UTF8MB4,
    IN column_names TEXT CHARACTER SET UTF8MB4,
    IN value_yes CHAR(100) CHARACTER SET UTF8MB4,
    IN value_no CHAR(100) CHARACTER SET UTF8MB4
)
BEGIN

    SET @table_columns = column_names;
    SET @start_pos = 1;
    SET @comma_pos = locate(',', @table_columns);
    SET @end_loop = 0;

    SET @column_label = '';

    REPEAT
        IF @comma_pos > 0 THEN
            SET @column_label = substring(@table_columns, @start_pos, @comma_pos - @start_pos);
            SET @end_loop = 0;
        ELSE
            SET @column_label = substring(@table_columns, @start_pos);
            SET @end_loop = 1;
        END IF;

        -- UPDATE fact_hts SET @column_label=IF(@column_label IS NULL OR '', new_value_if_false, new_value_if_true);

        SET @update_sql = CONCAT(
                'UPDATE ', table_to_update, ' SET ', @column_label, '= IF(', @column_label, ' IS NOT NULL, ''',
                value_yes, ''', ''', value_no, ''');');
        PREPARE stmt FROM @update_sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;

        IF @end_loop = 0 THEN
            SET @table_columns = substring(@table_columns, @comma_pos + 1);
            SET @comma_pos = locate(',', @table_columns);
        END IF;
    UNTIL @end_loop = 1
        END REPEAT;

END //

DELIMITER ;


        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_extract_report_metadata  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_extract_report_metadata;

CREATE PROCEDURE sp_extract_report_metadata(
    IN report_data MEDIUMTEXT CHARACTER SET UTF8MB4,
    IN metadata_table CHAR(255) CHARACTER SET UTF8MB4
)
BEGIN

    SET session group_concat_max_len = 20000;

    SELECT JSON_EXTRACT(report_data, '$.flat_report_metadata') INTO @report_array;
    SELECT JSON_LENGTH(@report_array) INTO @report_array_len;

    SET @report_count = 0;
    WHILE @report_count < @report_array_len
        DO

            SELECT JSON_EXTRACT(@report_array, CONCAT('$[', @report_count, ']')) INTO @report;
            SELECT JSON_EXTRACT(@report, '$.report_name') INTO @report_name;
            SELECT JSON_EXTRACT(@report, '$.flat_table_name') INTO @flat_table_name;
            SELECT JSON_EXTRACT(@report, '$.encounter_type_uuid') INTO @encounter_type;
            SELECT JSON_EXTRACT(@report, '$.table_columns') INTO @column_array;

            SELECT JSON_KEYS(@column_array) INTO @column_keys_array;
            SELECT JSON_LENGTH(@column_keys_array) INTO @column_keys_array_len;
            SET @col_count = 0;
            WHILE @col_count < @column_keys_array_len
                DO
                    SELECT JSON_EXTRACT(@column_keys_array, CONCAT('$[', @col_count, ']')) INTO @field_name;
                    SELECT JSON_EXTRACT(@column_array, CONCAT('$.', @field_name)) INTO @concept_uuid;

                    SET @tbl_name = '';
                    INSERT INTO mamba_dim_concept_metadata(report_name,
                                                           flat_table_name,
                                                           encounter_type_uuid,
                                                           column_label,
                                                           concept_uuid)
                    VALUES (JSON_UNQUOTE(@report_name),
                            JSON_UNQUOTE(@flat_table_name),
                            JSON_UNQUOTE(@encounter_type),
                            JSON_UNQUOTE(@field_name),
                            JSON_UNQUOTE(@concept_uuid));

                    SET @col_count = @col_count + 1;
                END WHILE;

            SET @report_count = @report_count + 1;
        END WHILE;

END //

DELIMITER ;


        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_load_agegroup  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_load_agegroup;

CREATE PROCEDURE sp_load_agegroup()
BEGIN
    DECLARE age INT DEFAULT 0;
    WHILE age <= 120
        DO
            INSERT INTO dim_agegroup(age, datim_agegroup, normal_agegroup)
            VALUES (age, fn_calculate_agegroup(age), IF(age < 15, '<15', '15+'));
            SET age = age + 1;
        END WHILE;
END //

DELIMITER ;


        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_concept_datatype_create  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_concept_datatype_create;

CREATE PROCEDURE sp_mamba_dim_concept_datatype_create()
BEGIN
-- $BEGIN

CREATE TABLE mamba_dim_concept_datatype
(
    concept_datatype_id  int                             NOT NULL AUTO_INCREMENT,
    external_datatype_id int,
    datatype_name        CHAR(255) CHARACTER SET UTF8MB4 NULL,
    PRIMARY KEY (concept_datatype_id)
);

create index mamba_dim_concept_datatype_external_datatype_id_index
    on mamba_dim_concept_datatype (external_datatype_id);


-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_concept_datatype_insert  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_concept_datatype_insert;

CREATE PROCEDURE sp_mamba_dim_concept_datatype_insert()
BEGIN
-- $BEGIN

INSERT INTO mamba_dim_concept_datatype (external_datatype_id,
                                        datatype_name)
SELECT dt.concept_datatype_id AS external_datatype_id,
       dt.name                AS datatype_name
FROM concept_datatype dt
WHERE dt.retired = 0;

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_concept_datatype  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_concept_datatype;

CREATE PROCEDURE sp_mamba_dim_concept_datatype()
BEGIN
-- $BEGIN

CALL sp_mamba_dim_concept_datatype_create();
CALL sp_mamba_dim_concept_datatype_insert();

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_concept_create  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_concept_create;

CREATE PROCEDURE sp_mamba_dim_concept_create()
BEGIN
-- $BEGIN

CREATE TABLE mamba_dim_concept
(
    concept_id           INT                             NOT NULL AUTO_INCREMENT,
    uuid                 CHAR(38) CHARACTER SET UTF8MB4  NOT NULL,
    external_concept_id  INT,
    external_datatype_id INT, -- make it a FK
    datatype             CHAR(255) CHARACTER SET UTF8MB4 NULL,
    PRIMARY KEY (concept_id)
);

CREATE INDEX mamba_dim_concept_external_concept_id_index
    ON mamba_dim_concept (external_concept_id);

CREATE INDEX mamba_dim_concept_external_datatype_id_index
    ON mamba_dim_concept (external_datatype_id);

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_concept_insert  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_concept_insert;

CREATE PROCEDURE sp_mamba_dim_concept_insert()
BEGIN
-- $BEGIN

INSERT INTO mamba_dim_concept (uuid,
                               external_concept_id,
                               external_datatype_id)
SELECT c.uuid        AS uuid,
       c.concept_id  AS external_concept_id,
       c.datatype_id AS external_datatype_id
FROM concept c
WHERE c.retired = 0;

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_concept_update  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_concept_update;

CREATE PROCEDURE sp_mamba_dim_concept_update()
BEGIN
-- $BEGIN

UPDATE mamba_dim_concept c
    INNER JOIN mamba_dim_concept_datatype dt
    ON c.external_datatype_id = dt.external_datatype_id
SET c.datatype = dt.datatype_name
WHERE c.concept_id > 0;

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_concept  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_concept;

CREATE PROCEDURE sp_mamba_dim_concept()
BEGIN
-- $BEGIN

CALL sp_mamba_dim_concept_create();
CALL sp_mamba_dim_concept_insert();
CALL sp_mamba_dim_concept_update();

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_concept_answer_create  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_concept_answer_create;

CREATE PROCEDURE sp_mamba_dim_concept_answer_create()
BEGIN
-- $BEGIN

CREATE TABLE mamba_dim_concept_answer
(
    concept_answer_id INT NOT NULL AUTO_INCREMENT,
    concept_id        INT,
    answer_concept    INT,
    answer_drug       INT,
    PRIMARY KEY (concept_answer_id)
);

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_concept_answer_insert  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_concept_answer_insert;

CREATE PROCEDURE sp_mamba_dim_concept_answer_insert()
BEGIN
-- $BEGIN

INSERT INTO mamba_dim_concept_answer (concept_id,
                                      answer_concept,
                                      answer_drug)
SELECT ca.concept_id     AS concept_id,
       ca.answer_concept AS answer_concept,
       ca.answer_drug    AS answer_drug
FROM concept_answer ca;

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_concept_answer  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_concept_answer;

CREATE PROCEDURE sp_mamba_dim_concept_answer()
BEGIN
-- $BEGIN

CALL sp_mamba_dim_concept_answer_create();
CALL sp_mamba_dim_concept_answer_insert();

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_concept_name_create  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_concept_name_create;

CREATE PROCEDURE sp_mamba_dim_concept_name_create()
BEGIN
-- $BEGIN

CREATE TABLE mamba_dim_concept_name
(
    concept_name_id     INT                             NOT NULL AUTO_INCREMENT,
    external_concept_id INT,
    concept_name        CHAR(255) CHARACTER SET UTF8MB4 NULL,
    PRIMARY KEY (concept_name_id)
);

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_concept_name_insert  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_concept_name_insert;

CREATE PROCEDURE sp_mamba_dim_concept_name_insert()
BEGIN
-- $BEGIN

INSERT INTO mamba_dim_concept_name (external_concept_id,
                                    concept_name)
SELECT cn.concept_id AS external_concept_id,
       cn.name       AS concept_name
FROM concept_name cn
WHERE cn.locale = 'en'
  AND cn.locale_preferred = 1;

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_concept_name  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_concept_name;

CREATE PROCEDURE sp_mamba_dim_concept_name()
BEGIN
-- $BEGIN

CALL sp_mamba_dim_concept_name_create();
CALL sp_mamba_dim_concept_name_insert();

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_encounter_type_create  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_encounter_type_create;

CREATE PROCEDURE sp_mamba_dim_encounter_type_create()
BEGIN
-- $BEGIN

CREATE TABLE mamba_dim_encounter_type
(
    encounter_type_id          INT                            NOT NULL AUTO_INCREMENT,
    external_encounter_type_id INT,
    encounter_type_uuid        CHAR(38) CHARACTER SET UTF8MB4 NOT NULL,
    PRIMARY KEY (encounter_type_id)
);

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_encounter_type_insert  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_encounter_type_insert;

CREATE PROCEDURE sp_mamba_dim_encounter_type_insert()
BEGIN
-- $BEGIN

INSERT INTO mamba_dim_encounter_type (external_encounter_type_id,
                                      encounter_type_uuid)
SELECT et.encounter_type_id AS external_encounter_type_id,
       et.uuid              AS encounter_type_uuid
FROM encounter_type et
WHERE et.retired = 0;

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_encounter_type  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_encounter_type;

CREATE PROCEDURE sp_mamba_dim_encounter_type()
BEGIN
-- $BEGIN

CALL sp_mamba_dim_encounter_type_create();
CALL sp_mamba_dim_encounter_type_insert();

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_encounter_create  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_encounter_create;

CREATE PROCEDURE sp_mamba_dim_encounter_create()
BEGIN
-- $BEGIN

CREATE TABLE mamba_dim_encounter
(
    encounter_id               INT                            NOT NULL AUTO_INCREMENT,
    external_encounter_id      INT,
    external_encounter_type_id INT,
    encounter_type_uuid        CHAR(38) CHARACTER SET UTF8MB4 NULL,
    PRIMARY KEY (encounter_id)
);

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_encounter_insert  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_encounter_insert;

CREATE PROCEDURE sp_mamba_dim_encounter_insert()
BEGIN
-- $BEGIN

INSERT INTO mamba_dim_encounter (external_encounter_id,
                                 external_encounter_type_id)
SELECT e.encounter_id   AS external_encounter_id,
       e.encounter_type AS external_encounter_type_id
FROM encounter e;

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_encounter_update  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_encounter_update;

CREATE PROCEDURE sp_mamba_dim_encounter_update()
BEGIN
-- $BEGIN

UPDATE mamba_dim_encounter e
    INNER JOIN mamba_dim_encounter_type et
    ON e.external_encounter_type_id = et.external_encounter_type_id
SET e.encounter_type_uuid = et.encounter_type_uuid
WHERE e.encounter_id > 0;

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_encounter  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_encounter;

CREATE PROCEDURE sp_mamba_dim_encounter()
BEGIN
-- $BEGIN

CALL sp_mamba_dim_encounter_create();
CALL sp_mamba_dim_encounter_insert();
CALL sp_mamba_dim_encounter_update();

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_concept_metadata_create  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_concept_metadata_create;

CREATE PROCEDURE sp_mamba_dim_concept_metadata_create()
BEGIN
-- $BEGIN

CREATE TABLE mamba_dim_concept_metadata
(
    concept_metadata_id INT                             NOT NULL AUTO_INCREMENT,
    column_number       INT,
    column_label        CHAR(50) CHARACTER SET UTF8MB4  NOT NULL,
    concept_uuid        CHAR(38) CHARACTER SET UTF8MB4  NOT NULL,
    concept_datatype    CHAR(255) CHARACTER SET UTF8MB4 NULL,
    concept_answer_obs  TINYINT(1)                      NOT NULL DEFAULT 0,
    report_name         CHAR(255) CHARACTER SET UTF8MB4 NOT NULL,
    flat_table_name     CHAR(255) CHARACTER SET UTF8MB4 NULL,
    encounter_type_uuid CHAR(38) CHARACTER SET UTF8MB4  NOT NULL,

    PRIMARY KEY (concept_metadata_id)
);

create index mamba_dim_concept_metadata_concept_uuid_index
    on mamba_dim_concept_metadata (concept_uuid);

-- ALTER TABLE `mamba_dim_concept_metadata`
--     ADD COLUMN `encounter_type_id` INT NULL AFTER `output_table_name`,
--     ADD CONSTRAINT `fk_encounter_type_id`
--         FOREIGN KEY (`encounter_type_id`) REFERENCES `mamba_dim_encounter_type` (`encounter_type_id`);

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_concept_metadata_insert  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_concept_metadata_insert;

CREATE PROCEDURE sp_mamba_dim_concept_metadata_insert()
BEGIN
  -- $BEGIN

  SET @report_data = '{"flat_report_metadata":[
  {
  "report_name": "Clinical_Assessment_Card_Report",
  "flat_table_name": "mamba_flat_encounter_art_card",
  "encounter_type_uuid": "8d5b2be0-c2cc-11de-8d13-0010c6dffd0f",
  "table_columns": {
    "hemoglobin": "dc548e89-30ab-102d-86b0-7a5022ba4115",
    "malnutrition": "dc655734-30ab-102d-86b0-7a5022ba4115",
    "method_of_family_planning": "dc7620b3-30ab-102d-86b0-7a5022ba4115",
    "oedema": "460AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "cd4_panel": "dc824b7b-30ab-102d-86b0-7a5022ba4115",
    "cd4_percent": "dc86e9fb-30ab-102d-86b0-7a5022ba4115",
    "hiv_viral_load": "dc8d83e3-30ab-102d-86b0-7a5022ba4115",
    "historical_drug_start_date": "1190AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "historical_drug_stop_date": "1191AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "current_drugs_used": "dc9b5b8f-30ab-102d-86b0-7a5022ba4115",
    "tests_ordered": "dca07f4a-30ab-102d-86b0-7a5022ba4115",
    "number_of_weeks_pregnant": "dca0a383-30ab-102d-86b0-7a5022ba4115",
    "medication_orders": "1282AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "viral_load_qualitative": "dca12261-30ab-102d-86b0-7a5022ba4115",
    "hepatitis_b_test_qualitative": "dca16e53-30ab-102d-86b0-7a5022ba4115",
    "mid_upper_arm_circumference": "1343AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "medication_strength": "1444AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "register_serial_number": "1646AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "duration_units": "1732AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "systolic_blood_pressure": "5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "diastolic_blood_pressure": "5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "pulse": "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "temperature": "5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "weight": "5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "height": "5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "return_visit_date": "dcac04cf-30ab-102d-86b0-7a5022ba4115",
    "respiratory_rate": "5242AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "head_circumference": "5314AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "cd4_count": "dcbcba2c-30ab-102d-86b0-7a5022ba4115",
    "estimated_date_of_confinement": "dcc033e5-30ab-102d-86b0-7a5022ba4115",
    "pmtct": "dcd7e8e5-30ab-102d-86b0-7a5022ba4115",
    "pregnant": "dcda5179-30ab-102d-86b0-7a5022ba4115",
    "scheduled_patient_visit": "dcda9857-30ab-102d-86b0-7a5022ba4115",
    "entry_point_into_hiv_care": "dcdfe3ce-30ab-102d-86b0-7a5022ba4115",
    "who_hiv_clinical_stage": "dcdff274-30ab-102d-86b0-7a5022ba4115",
    "name_of_location_transferred_to": "dce015bb-30ab-102d-86b0-7a5022ba4115",
    "tuberculosis_status": "dce02aa1-30ab-102d-86b0-7a5022ba4115",
    "tuberculosis_treatment_start_date": "dce02eca-30ab-102d-86b0-7a5022ba4115",
    "adherence_to_cotrim": "dce0370c-30ab-102d-86b0-7a5022ba4115",
    "arv_adherence_assessment_code": "dce03b2f-30ab-102d-86b0-7a5022ba4115",
    "reason_for_missing_arv": "dce045a4-30ab-102d-86b0-7a5022ba4115",
    "medication_or_other_side_effects": "dce05b7f-30ab-102d-86b0-7a5022ba4115",
    "history_of_functional_status": "dce09a15-30ab-102d-86b0-7a5022ba4115",
    "body_weight": "dce09e2f-30ab-102d-86b0-7a5022ba4115",
    "family_planning_status": "dce0a659-30ab-102d-86b0-7a5022ba4115",
    "symptom_diagnosis": "dce0e02a-30ab-102d-86b0-7a5022ba4115",
    "address": "dce122f3-30ab-102d-86b0-7a5022ba4115",
    "date_positive_hiv_test_confirmed": "dce12b4f-30ab-102d-86b0-7a5022ba4115",
    "treatment_supporter_telephone_number": "dce17480-30ab-102d-86b0-7a5022ba4115",
    "transferred_out": "dd27a783-30ab-102d-86b0-7a5022ba4115",
    "tuberculosis_treatment_stop_date": "dd2adde2-30ab-102d-86b0-7a5022ba4115",
    "current_arv_regimen": "dd2b0b4d-30ab-102d-86b0-7a5022ba4115",
    "art_duration": "9ce522a8-cd6a-4254-babb-ebeb48b8ce2f",
    "current_art_duration": "171de3f4-a500-46f6-8098-8097561dfffb",
    "antenatal_number": "38460266-6bcd-47e8-844c-649d34323810",
    "mid_upper_arm_circumference_code": "5f86d19d-9546-4466-89c0-6f80c101191b",
    "district_tuberculosis_number": "67e9ec2f-4c72-408b-8122-3706909d77ec",
    "opportunistic_infection": "b498df96-0e4a-4eaf-9b42-dacf9e486cba",
    "trimethoprim_days_dispensed": "8352c896-3799-406d-a512-da4d3b04288b",
    "other_medications_dispensed": "b04eaf95-77c9-456a-99fb-f668f58a9386",
    "arv_regimen_days_dispensed": "7593ede6-6574-4326-a8a6-3d742e843659",
    "trimethoprim_dosage": "38801143-01ac-4328-b0e1-a7b23c84c8a3",
    "ar_regimen_dose": "b0e53f0a-eaca-49e6-b663-d0df61601b70",
    "nutrition_support_and_infant_feeding": "8531d1a7-9793-4c62-adab-f6716cf9fabb",
    "baseline_regimen": "c3332e8d-2548-4ad6-931d-6855692694a3",
    "baseline_weight": "900b8fd9-2039-4efc-897b-9b8ce37396f5",
    "baseline_stage": "39243cef-b375-44b1-9e79-cbf21bd10878",
    "baseline_cd4": "c17bd9df-23e6-4e65-ba42-eb6d9250ca3f",
    "baseline_pregnancy": "b253be65-0155-4b43-ad15-88bc797322c9",
    "name_of_family_member": "e96d0880-e80e-4088-9787-bb2623fd46af",
    "age_of_family_member": "4049d989-b99e-440d-8f70-c222aa9fe45c",
    "family_member_set": "8452e0ac-a83a-428e-bfda-21cb39eef79f",
    "hiv_test": "ddcd8aad-9085-4a88-a411-f19521be4785",
    "hiv_test_facility": "89d3ee61-7c74-4537-b199-4026bd6a3f67",
    "other_side_effects": "d4f4c0e7-06f5-4aa6-a218-17b1f97c5a44",
    "other_tests_ordered": "79447e7c-9778-4b5d-b665-cd63e9035aa5",
    "care_entry_point_set": "cabfa0e9-ddae-438b-a052-6d5c97164242",
    "treatment_supporter_tel_no": "201d5b56-2420-4be0-92bc-69cd40ef291b",
    "other_reason_for_missing_arv": "d14ea061-e36f-40df-ab8c-bd8f933a9e0a",
    "current_regimen_other": "97c48198-3cf7-4892-a3e6-d61fb1125882",
    "treatment_supporter_name": "23e28311-3c17-4137-8eee-69860621b80b",
    "cd4_classification_for_infants": "d4595dd1-753c-4049-bfeb-c370e4b8d80c",
    "baseline_regimen_start_date": "ab505422-26d9-41f1-a079-c3d222000440",
    "baseline_regimen_set": "e525c286-74b2-4e30-84ac-c4d5f07c503c",
    "transfer_out_date": "fc1b1e96-4afb-423b-87e5-bb80d451c967",
    "transfer_out_set": "f233b5e7-163c-495f-9102-8d916a5de833",
    "health_education_disclosure": "8bdff534-6b4b-44ca-bc88-d088b3b53431",
    "other_referral_ordered": "9028e51b-0c27-4b72-bde6-fadba72d1396",
    "age_in_months": "143bcb27-c68b-470d-be1d-35d50c5c8161",
    "test_result_type": "e3977d09-5f8b-40a1-804e-28b427c0e7a4",
    "lab_result_txt": "bfd0ac71-cd88-47a3-a320-4fc2e6f5993f",
    "lab_result_set": "9deeba77-cc1b-47ef-b4ab-84b22fb527f3",
    "counselling_session_type": "b92b1777-4356-49b2-9c83-a799680dc7d4",
    "cotrim_given": "c3d744f6-00ef-4774-b9a7-d33c58f5b014",
    "eid_visit_1_appointment_date": "72339c21-ed50-450d-8adb-20ae62f15265",
    "feeding_status_at_eid_visit_1": "151283c0-8ef7-442f-8b03-3d7382a9d9cd",
    "counselling_approach": "ff820a28-1adf-4530-bf27-537bfa9ce0b2",
    "current_hiv_test_result": "3d292447-d7df-417f-8a71-e53e869ec89d",
    "results_received_as_a_couple": "2aa9f0c1-3f7e-49cd-86ee-baac0d2d5f2d",
    "tb_suspect": "b80f04a4-1559-42fd-8923-f8a6d2456a04",
    "baseline_lactating": "ab7bb4db-1a54-4225-b71c-d8e138b471e9",
    "inh_dosage": "be211d29-1507-4e2e-9906-4bfeae4ddc1f",
    "inh_days_dispensed": "7b3e0567-a354-426b-bbc5-3525ac3456c9",
    "age_unit": "33b18e88-0eb9-48f0-8023-2e90caad4469",
    "syphilis_test_result": "275a6f72-b8a4-4038-977a-727552f69cb8",
    "syphilis_test_result_for_partner": "d8bc9915-ed4b-4df9-9458-72ca1bc2cd06",
    "ctx_given_at_eid_visit_1": "5372410c-4268-4a39-a60e-cc4a28363e83",
    "nvp_given_at_eid_visit_1": "7148f054-fa36-4746-8791-63ea32c06e1c",
    "eid_visit_1_muac": "d257300b-6aa4-483a-8ab2-acdf13b844e0",
    "medication_duration": "159368AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "clinical_impression_comment": "159395AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "reason_for_appointment": "160288AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "medication_history": "160741AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "quantity_of_medication": "160856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "tb_with_rifampin_resistance_checking": "162202AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "specimen_sources": "162476AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "eid_immunisation_codes": "c74ce384-60ce-4740-9bdd-9c2a673b5a7d",
    "clinical_assessment_codes": "f0826902-a6d1-4abb-9783-1aeec0dea9d3",
    "refiil_of_art_for_the_mother": "958de62d-c1ae-4262-bf19-14671b3094a2",
    "development_milestone": "e10fdb37-c7bf-4d71-b588-dc68f011596d",
    "pre_test_counseling_done": "193039f1-c378-4d81-bb72-653b66c69914",
    "hct_entry_point": "720a1e85-ea1c-4f7b-a31e-cb896978df79",
    "linked_to_care": "3d620422-0641-412e-ab31-5e45b98bc459",
    "estimated_gestational_age": "0b995cb8-7d0d-46c0-bd1a-bd322387c870",
    "eid_concept_type": "046b5fd4-3840-409b-9edb-5e05f2d81bd6",
    "hiv_viral_load_date": "0b434cfa-b11c-4d14-aaa2-9aed6ca2da88",
    "relationship_to_patient": "164352AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "other_reason_for_appointment": "e17524f4-4445-417e-9098-ecdd134a6b81",
    "nutrition_assessment": "165050AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "art_pill_balance": "ed19c180-c4ac-4549-8310-60bffca1575b",
    "differentiated_service_delivery": "73312fee-c321-11e8-a355-529269fb1459",
    "stable_in_dsdm": "cc183c11-0f94-4992-807c-84f33095ce37",
    "reason_for_testing": "2afe1128-c3f6-4b35-b119-d17b9b9958ed",
    "previous_hiv_tests_date": "34c917f0-356b-40d0-b3d1-cf609517b5fc",
    "milligram_per_meter_squared": "46648b1d-b099-433b-8f9c-3815ff1e0a0f",
    "hiv_testing_service_delivery_model": "46648b1d-b099-433b-8f9c-3815ff1e0a0f",
    "hiv_syphillis_duo": "16091701-69b8-4bc7-82b3-b1726cf5a5df",
    "prevention_services_received": "73686a14-b55c-4b10-916d-fda2046b803f",
    "hiv_first_time_tester": "2766c090-c057-44f2-98f0-691b6d0336dc",
    "previous_hiv_test_results": "49ba801d-b6ff-47cd-8d29-e0ac8649cb7d",
    "results_received_as_individual": "3437ae80-bcc5-41e2-887e-d56999a1b467",
    "health_education_setting": "2d5a0641-ef12-4101-be76-533d4ba651df",
    "health_edu_intervation_approaches": "eb7c1c34-59e5-46d5-beba-626694badd54",
    "health_education_depression_status": "fe9a6bfc-b0db-4bf3-bab6-a8800dd93ded",
    "ovc_screening": "c2f9c9f3-3e46-456c-9f17-7bb23c473f1b",
    "art_preparation_readiness": "47502ce3-fc55-41e6-a61c-54a4404dd0e1",
    "ovc_assessment": "cb07b087-effb-4679-9e1c-5bcc506b5599",
    "phdp_components": "d788b8df-f25d-49e7-b946-bf5fe2d9407c",
    "tpt_start_date": "483939c7-79ba-4ca4-8c3e-346488c97fc7",
    "tpt_completion_date": "813e21e7-4ccb-4fe9-aaab-3c0e40b6e356",
    "advanced_disease_status": "17def5f6-d6b4-444b-99ed-40eb05d2c4f8",
    "family_member_hiv_status": "1f98a7e6-4d0a-4008-a6f7-4ec118f08983",
    "tpt_status": "37d4ac43-b3b4-4445-b63b-e3acf47c8910",
    "rpr_test_results": "d462b4f6-fb37-4e19-8617-e5499626c234",
    "crag_test_results": "43c33e93-90ff-406b-b7b2-9c655b2a561a",
    "tb_lam_results": "066b84a0-e18f-4cdd-a0d7-189454f4c7a4",
    "gender_based_violance": "23a37400-f855-405b-9268-cb2d25b97f54",
    "dapsone_ctx_medset": "93b2347e-e590-11e9-81b4-2a2ae2dbcce4",
    "tuberculosis_medication_set": "a202ecd2-3ae8-4b67-8c4f-779a6ce9d7ff",
    "fluconazole_medication_set": "651dc8a3-8600-40c9-a082-8403282e23bc",
    "cervical_cancer_screening": "5029d903-51ba-4c44-8745-e97f320739b6",
    "intention_to_conceive": "ede98e0d-0e04-49c6-b6bd-902ad759a084",
    "viral_load_test": "1eb05918-f50c-4cad-a827-3c78f296a10a",
    "genexpert_test": "2cf5644d-73a7-42f2-b18c-f773f40b648c",
    "tb_microscopy_results": "215d1c92-43f4-4aee-9875-31047f30132c",
    "tb_microscopy_test": "5faffe89-397e-470c-a32b-e07d962d01b3",
    "tb_lam": "5827cc4c-8b35-483e-a460-9b76885cf9de",
    "rpr_test": "67dbf985-6156-45f0-8b10-9ca6ba3053fd",
    "crag_test": "daebb483-4eef-42b5-9380-7dbfec2ef6ce",
    "arv_med_set": "ffe9b82c-d341-47a9-a7ef-89c0f5abba97",
    "quantity_unit": "dfc50562-da6a-4ce2-ab80-43c8f2d64d6f",
    "tpt_side_effects": "23a6dc6e-ac16-4fa6-8029-155522548d04",
    "split_into_drugs": "3df0131c-2747-4d77-9972-44268259f4a8",
    "lab_number": "0f998893-ab24-4ee4-922a-f197ac5fd6e6",
    "other_drug_dispensed_set": "7c9bde8d-a5a7-473f-99d5-4991dc6feb01",
    "test": "472b6d0f-3f63-4647-8a5c-8223dd1207f5",
    "test_result": "2cab2216-1aec-49d2-919b-d910bae973fb",
    "other_tests": "e529e61d-9937-42a2-9157-5cb0aac8cf05",
    "refill_point_code": "7a22cfcb-a272-4eff-968c-5e9467125a7b",
    "next_return_date_at_facility": "f6c456f7-1ab4-4b4d-a3b4-e7417c81002a"
  }
}]}';

  CALL sp_extract_report_metadata(@report_data, 'mamba_dim_concept_metadata');

  -- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_concept_metadata_update  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_concept_metadata_update;

CREATE PROCEDURE sp_mamba_dim_concept_metadata_update()
BEGIN
-- $BEGIN

-- Update the Concept datatypes
UPDATE mamba_dim_concept_metadata md
    INNER JOIN mamba_dim_concept c
    ON md.concept_uuid = c.uuid
SET md.concept_datatype = c.datatype
WHERE md.concept_metadata_id > 0;

-- Update to True if this field is an obs answer to an obs Question
UPDATE mamba_dim_concept_metadata md
    INNER JOIN mamba_dim_concept c
    ON md.concept_uuid = c.uuid
    INNER JOIN mamba_dim_concept_answer ca
    ON ca.answer_concept = c.external_concept_id
SET md.concept_answer_obs = 1
WHERE md.concept_metadata_id > 0;

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_concept_metadata  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_concept_metadata;

CREATE PROCEDURE sp_mamba_dim_concept_metadata()
BEGIN
-- $BEGIN

CALL sp_mamba_dim_concept_metadata_create();
CALL sp_mamba_dim_concept_metadata_insert();
CALL sp_mamba_dim_concept_metadata_update();

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_person_create  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_person_create;

CREATE PROCEDURE sp_mamba_dim_person_create()
BEGIN
-- $BEGIN

CREATE TABLE mamba_dim_person
(
    person_id          INT                             NOT NULL AUTO_INCREMENT,
    external_person_id INT,
    birthdate          CHAR(255) CHARACTER SET UTF8MB4 NULL,
    gender             CHAR(255) CHARACTER SET UTF8MB4 NULL,
    PRIMARY KEY (person_id)
);
create index mamba_dim_person_external_person_id_index
    on mamba_dim_person (external_person_id);

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_person_insert  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_person_insert;

CREATE PROCEDURE sp_mamba_dim_person_insert()
BEGIN
-- $BEGIN

INSERT INTO mamba_dim_person (external_person_id,
                              birthdate,
                              gender)
SELECT psn.person_id AS external_person_id,
       psn.birthdate AS birthdate,
       psn.gender    AS gender
FROM person psn;

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_person  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_person;

CREATE PROCEDURE sp_mamba_dim_person()
BEGIN
-- $BEGIN

CALL sp_mamba_dim_person_create();
CALL sp_mamba_dim_person_insert();

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_person_name_create  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_person_name_create;

CREATE PROCEDURE sp_mamba_dim_person_name_create()
BEGIN
-- $BEGIN

CREATE TABLE mamba_dim_person_name
(
    person_name_id          INT                             NOT NULL AUTO_INCREMENT,
    external_person_name_id INT,
    external_person_id      INT,
    given_name              CHAR(255) CHARACTER SET UTF8MB4 NULL,
    PRIMARY KEY (person_name_id)
);
CREATE INDEX mamba_dim_person_name_external_person_id_index
    ON mamba_dim_person_name (external_person_id);
-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_person_name_insert  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_person_name_insert;

CREATE PROCEDURE sp_mamba_dim_person_name_insert()
BEGIN
-- $BEGIN

INSERT INTO mamba_dim_person_name (external_person_name_id,
                                   external_person_id,
                                   given_name)
SELECT pn.person_name_id AS external_person_name_id,
       pn.person_id      AS external_person_id,
       pn.given_name     AS given_name
FROM person_name pn;

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_person_name  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_person_name;

CREATE PROCEDURE sp_mamba_dim_person_name()
BEGIN
-- $BEGIN

CALL sp_mamba_dim_person_name_create();
CALL sp_mamba_dim_person_name_insert();

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_person_address_create  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_person_address_create;

CREATE PROCEDURE sp_mamba_dim_person_address_create()
BEGIN
-- $BEGIN

CREATE TABLE mamba_dim_person_address
(
    person_address_id          INT                             NOT NULL AUTO_INCREMENT,
    external_person_address_id INT,
    external_person_id         INT,
    city_village               CHAR(255) CHARACTER SET UTF8MB4 NULL,
    county_district            CHAR(255) CHARACTER SET UTF8MB4 NULL,
    address1                   CHAR(255) CHARACTER SET UTF8MB4 NULL,
    address2                   CHAR(255) CHARACTER SET UTF8MB4 NULL,
    PRIMARY KEY (person_address_id)
);
create index mamba_dim_person_address_external_person_id_index
    on mamba_dim_person_address (external_person_id);

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_person_address_insert  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_person_address_insert;

CREATE PROCEDURE sp_mamba_dim_person_address_insert()
BEGIN
-- $BEGIN

INSERT INTO mamba_dim_person_address (external_person_address_id,
                                      external_person_id,
                                      city_village,
                                      county_district,
                                      address1,
                                      address2)
SELECT pa.person_address_id AS external_person_address_id,
       pa.person_id         AS external_person_id,
       pa.city_village      AS city_village,
       pa.county_district   AS county_district,
       pa.address1          AS address1,
       pa.address2          AS address2
FROM person_address pa;

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_dim_person_address  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_dim_person_address;

CREATE PROCEDURE sp_mamba_dim_person_address()
BEGIN
-- $BEGIN

CALL sp_mamba_dim_person_address_create();
CALL sp_mamba_dim_person_address_insert();

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_dim_client_create  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_dim_client_create;

CREATE PROCEDURE sp_dim_client_create()
BEGIN
-- $BEGIN
CREATE TABLE dim_client
(
    id            INT                             NOT NULL AUTO_INCREMENT,
    client_id     INT,
    date_of_birth DATE                            NULL,
    age           INT,
    sex           CHAR(255) CHARACTER SET UTF8MB4 NULL,
    county        CHAR(255) CHARACTER SET UTF8MB4 NULL,
    sub_county    CHAR(255) CHARACTER SET UTF8MB4 NULL,
    ward          CHAR(255) CHARACTER SET UTF8MB4 NULL,
    PRIMARY KEY (id)
);
-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_dim_client_insert  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_dim_client_insert;

CREATE PROCEDURE sp_dim_client_insert()
BEGIN
-- $BEGIN

INSERT INTO dim_client (client_id,
                        date_of_birth,
                        age,
                        sex,
                        county,
                        sub_county,
                        ward)
SELECT `psn`.`person_id`                             AS `client_id`,
       `psn`.`birthdate`                             AS `date_of_birth`,
       timestampdiff(YEAR, `psn`.`birthdate`, now()) AS `age`,
       (CASE `psn`.`gender`
            WHEN 'M' THEN 'Male'
            WHEN 'F' THEN 'Female'
            ELSE '_'
           END)                                      AS `sex`,
       `pa`.`county_district`                        AS `county`,
       `pa`.`city_village`                           AS `sub_county`,
       `pa`.`address1`                               AS `ward`
FROM ((`mamba_dim_person` `psn`
    LEFT JOIN `mamba_dim_person_name` `pn` on ((`psn`.`external_person_id` = `pn`.`external_person_id`)))
    LEFT JOIN `mamba_dim_person_address` `pa` on ((`psn`.`external_person_id` = `pa`.`external_person_id`)));


-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_dim_client_update  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_dim_client_update;

CREATE PROCEDURE sp_dim_client_update()
BEGIN
-- $BEGIN

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_dim_client  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_dim_client;

CREATE PROCEDURE sp_dim_client()
BEGIN
-- $BEGIN

CALL sp_dim_client_create();
CALL sp_dim_client_insert();
CALL sp_dim_client_update();

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_z_encounter_obs  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_z_encounter_obs;

CREATE PROCEDURE sp_mamba_z_encounter_obs()
BEGIN
-- $BEGIN

CREATE TABLE mamba_z_encounter_obs
(
    obs_question_uuid    CHAR(38) CHARACTER SET UTF8MB4,
--    obs_answer_uuid      CHAR(38) CHARACTER SET UTF8MB4,
    obs_value_coded_uuid CHAR(38) CHARACTER SET UTF8MB4,
    encounter_type_uuid  CHAR(38) CHARACTER SET UTF8MB4
)
SELECT o.encounter_id         AS encounter_id,
       o.person_id            AS person_id,
       o.obs_datetime         AS obs_datetime,
       o.concept_id           AS obs_question_concept_id,
       o.value_text           AS obs_value_text,
       o.value_numeric        AS obs_value_numeric,
       o.value_coded          AS obs_value_coded,
       o.value_datetime       AS obs_value_datetime,
       o.value_complex        AS obs_value_complex,
       o.value_drug           AS obs_value_drug,
       et.encounter_type_uuid AS encounter_type_uuid,
       NULL                   AS obs_question_uuid,
--       NULL                   AS obs_answer_uuid,
       NULL                   AS obs_value_coded_uuid
FROM obs o
         INNER JOIN mamba_dim_encounter e
                    ON o.encounter_id = e.external_encounter_id
         INNER JOIN mamba_dim_encounter_type et
                    ON e.external_encounter_type_id = et.external_encounter_type_id
WHERE et.encounter_type_uuid
          IN (SELECT DISTINCT(md.encounter_type_uuid)
              FROM mamba_dim_concept_metadata md);

CREATE INDEX mamba_z_encounter_obs_encounter_id_type_uuid_person_id_index
    ON mamba_z_encounter_obs (encounter_id, encounter_type_uuid, person_id);

CREATE INDEX mamba_z_encounter_obs_encounter_type_uuid_index
    ON mamba_z_encounter_obs (encounter_type_uuid);

CREATE INDEX mamba_z_encounter_obs_question_concept_id_index
    ON mamba_z_encounter_obs (obs_question_concept_id);

CREATE INDEX mamba_z_encounter_obs_value_coded_index
    ON mamba_z_encounter_obs (obs_value_coded);

CREATE INDEX mamba_z_encounter_obs_value_coded_uuid_index
    ON mamba_z_encounter_obs (obs_value_coded_uuid);

CREATE INDEX mamba_z_encounter_obs_question_uuid_index
    ON mamba_z_encounter_obs (obs_question_uuid);

-- update obs question UUIDs
UPDATE mamba_z_encounter_obs z
    INNER JOIN mamba_dim_concept c
    ON z.obs_question_concept_id = c.external_concept_id
SET z.obs_question_uuid = c.uuid
WHERE TRUE;

-- update obs_value_coded (UUIDs & values)
UPDATE mamba_z_encounter_obs z
    INNER JOIN mamba_dim_concept_name cn
    ON z.obs_value_coded = cn.external_concept_id
    INNER JOIN mamba_dim_concept c
    ON z.obs_value_coded = c.external_concept_id
SET z.obs_value_text       = cn.concept_name,
    z.obs_value_coded_uuid = c.uuid
WHERE z.obs_value_coded IS NOT NULL;

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_mamba_z_tables  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_mamba_z_tables;

CREATE PROCEDURE sp_mamba_z_tables()
BEGIN
-- $BEGIN

CALL sp_mamba_z_encounter_obs;

-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_data_processing_flatten  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_data_processing_flatten;

CREATE PROCEDURE sp_data_processing_flatten()
BEGIN
-- $BEGIN
-- CALL sp_xf_system_drop_all_tables_in_schema($target_database);
CALL sp_xf_system_drop_all_tables_in_schema();

CALL sp_mamba_dim_concept_datatype;

CALL sp_mamba_dim_concept_answer;

CALL sp_mamba_dim_concept_name;

CALL sp_mamba_dim_concept;

CALL sp_mamba_dim_encounter_type;

CALL sp_mamba_dim_encounter;

CALL sp_mamba_dim_concept_metadata;

CALL sp_mamba_dim_person;

CALL sp_mamba_dim_person_name;

CALL sp_mamba_dim_person_address;

CALL sp_dim_client;

CALL sp_mamba_z_tables;

CALL sp_flat_encounter_table_create_all;

CALL sp_flat_encounter_table_insert_all;
-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_data_processing_derived_covid  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_data_processing_derived_covid;

CREATE PROCEDURE sp_data_processing_derived_covid()
BEGIN
-- $BEGIN
CALL sp_dim_client_covid;
CALL sp_fact_encounter_covid;
-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_data_processing_derived_hiv_art  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_data_processing_derived_hiv_art;

CREATE PROCEDURE sp_data_processing_derived_hiv_art()
BEGIN
-- $BEGIN
-- CALL sp_dim_client_hiv_hts;
CALL sp_fact_encounter_hiv_art;
-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_data_processing_etl  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_data_processing_etl;

CREATE PROCEDURE sp_data_processing_etl()
BEGIN
-- $BEGIN
-- add base folder SP here --
-- CALL sp_data_processing_derived_hts();

CALL sp_data_processing_flatten();

CALL sp_data_processing_derived_hiv_art();
-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_fact_encounter_hiv_art_create  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_encounter_hiv_art_create;

CREATE PROCEDURE sp_fact_encounter_hiv_art_create()
BEGIN
-- $BEGIN
CREATE TABLE mamba_fact_encounter_hiv_art_card
(
    id                                   INT AUTO_INCREMENT,
    encounter_id                         INT NULL,
    client_id                            INT NULL,
    encounter_date                       DATE NULL,

    hemoglobin                           CHAR(255) CHARACTER SET UTF8MB4 NULL,
    malnutrition                         CHAR(255) CHARACTER SET UTF8MB4 NULL,
    method_of_family_planning            CHAR(255) CHARACTER SET UTF8MB4 NULL,
    oedema                               CHAR(255) CHARACTER SET UTF8MB4 NULL,
    cd4_panel                            CHAR(255) CHARACTER SET UTF8MB4 NULL,
    cd4_percent                          CHAR(255) CHARACTER SET UTF8MB4 NULL,
    hiv_viral_load                       CHAR(255) CHARACTER SET UTF8MB4 NULL,
    historical_drug_start_date           CHAR(255) CHARACTER SET UTF8MB4 NULL,
    historical_drug_stop_date            CHAR(255) CHARACTER SET UTF8MB4 NULL,
    current_drugs_used                   CHAR(255) CHARACTER SET UTF8MB4 NULL,
    tests_ordered                        CHAR(255) CHARACTER SET UTF8MB4 NULL,
    number_of_weeks_pregnant             CHAR(255) CHARACTER SET UTF8MB4 NULL,
    medication_orders                    CHAR(255) CHARACTER SET UTF8MB4 NULL,
    viral_load_qualitative               CHAR(255) CHARACTER SET UTF8MB4 NULL,
    hepatitis_b_test_qualitative         CHAR(255) CHARACTER SET UTF8MB4 NULL,
    mid_upper_arm_circumference          CHAR(255) CHARACTER SET UTF8MB4 NULL,
    medication_strength                  CHAR(255) CHARACTER SET UTF8MB4 NULL,
    register_serial_number               CHAR(255) CHARACTER SET UTF8MB4 NULL,
    duration_units                       CHAR(255) CHARACTER SET UTF8MB4 NULL,
    systolic_blood_pressure              CHAR(255) CHARACTER SET UTF8MB4 NULL,
    diastolic_blood_pressure             CHAR(255) CHARACTER SET UTF8MB4 NULL,
    pulse                                CHAR(255) CHARACTER SET UTF8MB4 NULL,
    temperature                          CHAR(255) CHARACTER SET UTF8MB4 NULL,
    weight                               CHAR(255) CHARACTER SET UTF8MB4 NULL,
    height                               CHAR(255) CHARACTER SET UTF8MB4 NULL,
    return_visit_date                    CHAR(255) CHARACTER SET UTF8MB4 NULL,
    respiratory_rate                     CHAR(255) CHARACTER SET UTF8MB4 NULL,
    head_circumference                   CHAR(255) CHARACTER SET UTF8MB4 NULL,
    cd4_count                            CHAR(255) CHARACTER SET UTF8MB4 NULL,
    estimated_date_of_confinement        CHAR(255) CHARACTER SET UTF8MB4 NULL,
    pmtct                                CHAR(255) CHARACTER SET UTF8MB4 NULL,
    pregnant                             CHAR(255) CHARACTER SET UTF8MB4 NULL,
    scheduled_patient_visit              CHAR(255) CHARACTER SET UTF8MB4 NULL,
    entry_point_into_hiv_care            CHAR(255) CHARACTER SET UTF8MB4 NULL,
    who_hiv_clinical_stage               CHAR(255) CHARACTER SET UTF8MB4 NULL,
    name_of_location_transferred_to      CHAR(255) CHARACTER SET UTF8MB4 NULL,
    tuberculosis_status                  CHAR(255) CHARACTER SET UTF8MB4 NULL,
    tuberculosis_treatment_start_date    CHAR(255) CHARACTER SET UTF8MB4 NULL,
    adherence_to_cotrim                  CHAR(255) CHARACTER SET UTF8MB4 NULL,
    arv_adherence_assessment_code        CHAR(255) CHARACTER SET UTF8MB4 NULL,
    reason_for_missing_arv               CHAR(255) CHARACTER SET UTF8MB4 NULL,
    medication_or_other_side_effects     CHAR(255) CHARACTER SET UTF8MB4 NULL,
    history_of_functional_status         CHAR(255) CHARACTER SET UTF8MB4 NULL,
    body_weight                          CHAR(255) CHARACTER SET UTF8MB4 NULL,
    family_planning_status               CHAR(255) CHARACTER SET UTF8MB4 NULL,
    symptom_diagnosis                    CHAR(255) CHARACTER SET UTF8MB4 NULL,
    address                              CHAR(255) CHARACTER SET UTF8MB4 NULL,
    date_positive_hiv_test_confirmed     CHAR(255) CHARACTER SET UTF8MB4 NULL,
    treatment_supporter_telephone_number CHAR(255) CHARACTER SET UTF8MB4 NULL,
    transferred_out                      CHAR(255) CHARACTER SET UTF8MB4 NULL,
    tuberculosis_treatment_stop_date     CHAR(255) CHARACTER SET UTF8MB4 NULL,
    current_arv_regimen                  CHAR(255) CHARACTER SET UTF8MB4 NULL,
    art_duration                         CHAR(255) CHARACTER SET UTF8MB4 NULL,
    current_art_duration                 CHAR(255) CHARACTER SET UTF8MB4 NULL,
    antenatal_number                     CHAR(255) CHARACTER SET UTF8MB4 NULL,
    mid_upper_arm_circumference_code     CHAR(255) CHARACTER SET UTF8MB4 NULL,
    district_tuberculosis_number         CHAR(255) CHARACTER SET UTF8MB4 NULL,
    opportunistic_infection              CHAR(255) CHARACTER SET UTF8MB4 NULL,
    trimethoprim_days_dispensed          CHAR(255) CHARACTER SET UTF8MB4 NULL,
    other_medications_dispensed          CHAR(255) CHARACTER SET UTF8MB4 NULL,
    arv_regimen_days_dispensed           CHAR(255) CHARACTER SET UTF8MB4 NULL,
    trimethoprim_dosage                  CHAR(255) CHARACTER SET UTF8MB4 NULL,
    ar_regimen_dose                      CHAR(255) CHARACTER SET UTF8MB4 NULL,
    nutrition_support_and_infant_feeding CHAR(255) CHARACTER SET UTF8MB4 NULL,
    baseline_regimen                     CHAR(255) CHARACTER SET UTF8MB4 NULL,
    baseline_weight                      CHAR(255) CHARACTER SET UTF8MB4 NULL,
    baseline_stage                       CHAR(255) CHARACTER SET UTF8MB4 NULL,
    baseline_cd4                         CHAR(255) CHARACTER SET UTF8MB4 NULL,
    baseline_pregnancy                   CHAR(255) CHARACTER SET UTF8MB4 NULL,
    name_of_family_member                CHAR(255) CHARACTER SET UTF8MB4 NULL,
    age_of_family_member                 CHAR(255) CHARACTER SET UTF8MB4 NULL,
    family_member_set                    CHAR(255) CHARACTER SET UTF8MB4 NULL,
    hiv_test                             CHAR(255) CHARACTER SET UTF8MB4 NULL,
    hiv_test_facility                    CHAR(255) CHARACTER SET UTF8MB4 NULL,
    other_side_effects                   CHAR(255) CHARACTER SET UTF8MB4 NULL,
    other_tests_ordered                  CHAR(255) CHARACTER SET UTF8MB4 NULL,
    care_entry_point_set                 CHAR(255) CHARACTER SET UTF8MB4 NULL,
    treatment_supporter_tel_no           CHAR(255) CHARACTER SET UTF8MB4 NULL,
    other_reason_for_missing_arv         CHAR(255) CHARACTER SET UTF8MB4 NULL,
    current_regimen_other                CHAR(255) CHARACTER SET UTF8MB4 NULL,
    treatment_supporter_name             CHAR(255) CHARACTER SET UTF8MB4 NULL,
    cd4_classification_for_infants       CHAR(255) CHARACTER SET UTF8MB4 NULL,
    baseline_regimen_start_date          CHAR(255) CHARACTER SET UTF8MB4 NULL,
    baseline_regimen_set                 CHAR(255) CHARACTER SET UTF8MB4 NULL,
    transfer_out_date                    CHAR(255) CHARACTER SET UTF8MB4 NULL,
    transfer_out_set                     CHAR(255) CHARACTER SET UTF8MB4 NULL,
    health_education_disclosure          CHAR(255) CHARACTER SET UTF8MB4 NULL,
    other_referral_ordered               CHAR(255) CHARACTER SET UTF8MB4 NULL,
    age_in_months                        CHAR(255) CHARACTER SET UTF8MB4 NULL,
    test_result_type                     CHAR(255) CHARACTER SET UTF8MB4 NULL,
    lab_result_txt                       CHAR(255) CHARACTER SET UTF8MB4 NULL,
    lab_result_set                       CHAR(255) CHARACTER SET UTF8MB4 NULL,
    counselling_session_type             CHAR(255) CHARACTER SET UTF8MB4 NULL,
    cotrim_given                         CHAR(255) CHARACTER SET UTF8MB4 NULL,
    eid_visit_1_appointment_date         CHAR(255) CHARACTER SET UTF8MB4 NULL,
    feeding_status_at_eid_visit_1        CHAR(255) CHARACTER SET UTF8MB4 NULL,
    counselling_approach                 CHAR(255) CHARACTER SET UTF8MB4 NULL,
    current_hiv_test_result              CHAR(255) CHARACTER SET UTF8MB4 NULL,
    results_received_as_a_couple         CHAR(255) CHARACTER SET UTF8MB4 NULL,
    tb_suspect                           CHAR(255) CHARACTER SET UTF8MB4 NULL,
    baseline_lactating                   CHAR(255) CHARACTER SET UTF8MB4 NULL,
    inh_dosage                           CHAR(255) CHARACTER SET UTF8MB4 NULL,
    inh_days_dispensed                   CHAR(255) CHARACTER SET UTF8MB4 NULL,
    age_unit                             CHAR(255) CHARACTER SET UTF8MB4 NULL,
    syphilis_test_result                 CHAR(255) CHARACTER SET UTF8MB4 NULL,
    syphilis_test_result_for_partner     CHAR(255) CHARACTER SET UTF8MB4 NULL,
    ctx_given_at_eid_visit_1             CHAR(255) CHARACTER SET UTF8MB4 NULL,
    nvp_given_at_eid_visit_1             CHAR(255) CHARACTER SET UTF8MB4 NULL,
    eid_visit_1_muac                     CHAR(255) CHARACTER SET UTF8MB4 NULL,
    medication_duration                  CHAR(255) CHARACTER SET UTF8MB4 NULL,
    clinical_impression_comment          CHAR(255) CHARACTER SET UTF8MB4 NULL,
    reason_for_appointment               CHAR(255) CHARACTER SET UTF8MB4 NULL,
    medication_history                   CHAR(255) CHARACTER SET UTF8MB4 NULL,
    quantity_of_medication               CHAR(255) CHARACTER SET UTF8MB4 NULL,
    tb_with_rifampin_resistance_checking CHAR(255) CHARACTER SET UTF8MB4 NULL,
    specimen_sources                     CHAR(255) CHARACTER SET UTF8MB4 NULL,
    eid_immunisation_codes               CHAR(255) CHARACTER SET UTF8MB4 NULL,
    clinical_assessment_codes            CHAR(255) CHARACTER SET UTF8MB4 NULL,
    refiil_of_art_for_the_mother         CHAR(255) CHARACTER SET UTF8MB4 NULL,
    development_milestone                CHAR(255) CHARACTER SET UTF8MB4 NULL,
    pre_test_counseling_done             CHAR(255) CHARACTER SET UTF8MB4 NULL,
    hct_entry_point                      CHAR(255) CHARACTER SET UTF8MB4 NULL,
    linked_to_care                       CHAR(255) CHARACTER SET UTF8MB4 NULL,
    estimated_gestational_age            CHAR(255) CHARACTER SET UTF8MB4 NULL,
    eid_concept_type                     CHAR(255) CHARACTER SET UTF8MB4 NULL,
    hiv_viral_load_date                  CHAR(255) CHARACTER SET UTF8MB4 NULL,
    relationship_to_patient              CHAR(255) CHARACTER SET UTF8MB4 NULL,
    other_reason_for_appointment         CHAR(255) CHARACTER SET UTF8MB4 NULL,
    nutrition_assessment                 CHAR(255) CHARACTER SET UTF8MB4 NULL,
    art_pill_balance                     CHAR(255) CHARACTER SET UTF8MB4 NULL,
    differentiated_service_delivery      CHAR(255) CHARACTER SET UTF8MB4 NULL,
    stable_in_dsdm                       CHAR(255) CHARACTER SET UTF8MB4 NULL,
    reason_for_testing                   CHAR(255) CHARACTER SET UTF8MB4 NULL,
    previous_hiv_tests_date              CHAR(255) CHARACTER SET UTF8MB4 NULL,
    milligram_per_meter_squared          CHAR(255) CHARACTER SET UTF8MB4 NULL,
    hiv_testing_service_delivery_model   CHAR(255) CHARACTER SET UTF8MB4 NULL,
    hiv_syphillis_duo                    CHAR(255) CHARACTER SET UTF8MB4 NULL,
    prevention_services_received         CHAR(255) CHARACTER SET UTF8MB4 NULL,
    hiv_first_time_tester                CHAR(255) CHARACTER SET UTF8MB4 NULL,
    previous_hiv_test_results            CHAR(255) CHARACTER SET UTF8MB4 NULL,
    results_received_as_individual       CHAR(255) CHARACTER SET UTF8MB4 NULL,
    health_education_setting             CHAR(255) CHARACTER SET UTF8MB4 NULL,
    health_edu_intervation_approaches    CHAR(255) CHARACTER SET UTF8MB4 NULL,
    health_education_depression_status   CHAR(255) CHARACTER SET UTF8MB4 NULL,
    ovc_screening                        CHAR(255) CHARACTER SET UTF8MB4 NULL,
    art_preparation_readiness            CHAR(255) CHARACTER SET UTF8MB4 NULL,
    ovc_assessment                       CHAR(255) CHARACTER SET UTF8MB4 NULL,
    phdp_components                      CHAR(255) CHARACTER SET UTF8MB4 NULL,
    tpt_start_date                       CHAR(255) CHARACTER SET UTF8MB4 NULL,
    tpt_completion_date                  CHAR(255) CHARACTER SET UTF8MB4 NULL,
    advanced_disease_status              CHAR(255) CHARACTER SET UTF8MB4 NULL,
    family_member_hiv_status             CHAR(255) CHARACTER SET UTF8MB4 NULL,
    tpt_status                           CHAR(255) CHARACTER SET UTF8MB4 NULL,
    rpr_test_results                     CHAR(255) CHARACTER SET UTF8MB4 NULL,
    crag_test_results                    CHAR(255) CHARACTER SET UTF8MB4 NULL,
    tb_lam_results                       CHAR(255) CHARACTER SET UTF8MB4 NULL,
    gender_based_violance                CHAR(255) CHARACTER SET UTF8MB4 NULL,
    dapsone_ctx_medset                   CHAR(255) CHARACTER SET UTF8MB4 NULL,
    tuberculosis_medication_set          CHAR(255) CHARACTER SET UTF8MB4 NULL,
    fluconazole_medication_set           CHAR(255) CHARACTER SET UTF8MB4 NULL,
    cervical_cancer_screening            CHAR(255) CHARACTER SET UTF8MB4 NULL,
    intention_to_conceive                CHAR(255) CHARACTER SET UTF8MB4 NULL,
    viral_load_test                      CHAR(255) CHARACTER SET UTF8MB4 NULL,
    genexpert_test                       CHAR(255) CHARACTER SET UTF8MB4 NULL,
    tb_microscopy_results                CHAR(255) CHARACTER SET UTF8MB4 NULL,
    tb_microscopy_test                   CHAR(255) CHARACTER SET UTF8MB4 NULL,
    tb_lam                               CHAR(255) CHARACTER SET UTF8MB4 NULL,
    rpr_test                             CHAR(255) CHARACTER SET UTF8MB4 NULL,
    crag_test                            CHAR(255) CHARACTER SET UTF8MB4 NULL,
    arv_med_set                          CHAR(255) CHARACTER SET UTF8MB4 NULL,
    quantity_unit                        CHAR(255) CHARACTER SET UTF8MB4 NULL,
    tpt_side_effects                     CHAR(255) CHARACTER SET UTF8MB4 NULL,
    split_into_drugs                     CHAR(255) CHARACTER SET UTF8MB4 NULL,
    lab_number                           CHAR(255) CHARACTER SET UTF8MB4 NULL,
    other_drug_dispensed_set             CHAR(255) CHARACTER SET UTF8MB4 NULL,
    test                                 CHAR(255) CHARACTER SET UTF8MB4 NULL,
    test_result                          CHAR(255) CHARACTER SET UTF8MB4 NULL,
    other_tests                          CHAR(255) CHARACTER SET UTF8MB4 NULL,
    refill_point_code                    CHAR(255) CHARACTER SET UTF8MB4 NULL,
    next_return_date_at_facility         CHAR(255) CHARACTER SET UTF8MB4 NULL,
    PRIMARY KEY (id)
);

CREATE INDEX
    mamba_fact_encounter_hiv_art_encounter_id_index ON mamba_fact_encounter_hiv_art_card (encounter_id);
CREATE INDEX
    mamba_fact_encounter_hiv_art_client_id_index ON mamba_fact_encounter_hiv_art_card (client_id);
-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_fact_encounter_hiv_art_insert  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_encounter_hiv_art_insert;

CREATE PROCEDURE sp_fact_encounter_hiv_art_insert()
BEGIN
-- $BEGIN
INSERT INTO mamba_fact_encounter_hiv_art (encounter_id,
                                          client_id,
                                          encounter_date,
                                          hemoglobin, malnutrition, method_of_family_planning, oedema, cd4_panel,
                                          cd4_percent, hiv_viral_load, historical_drug_start_date,
                                          historical_drug_stop_date, current_drugs_used, tests_ordered,
                                          number_of_weeks_pregnant, medication_orders, viral_load_qualitative,
                                          hepatitis_b_test_qualitative, mid_upper_arm_circumference,
                                          medication_strength, register_serial_number, duration_units,
                                          systolic_blood_pressure, diastolic_blood_pressure, pulse, temperature, weight,
                                          height, return_visit_date, respiratory_rate, head_circumference, cd4_count,
                                          estimated_date_of_confinement, pmtct, pregnant, scheduled_patient_visit,
                                          entry_point_into_hiv_care, who_hiv_clinical_stage,
                                          name_of_location_transferred_to, tuberculosis_status,
                                          tuberculosis_treatment_start_date, adherence_to_cotrim,
                                          arv_adherence_assessment_code, reason_for_missing_arv,
                                          medication_or_other_side_effects, history_of_functional_status, body_weight,
                                          family_planning_status, symptom_diagnosis, address,
                                          date_positive_hiv_test_confirmed, treatment_supporter_telephone_number,
                                          transferred_out, tuberculosis_treatment_stop_date, current_arv_regimen,
                                          art_duration, current_art_duration, antenatal_number,
                                          mid_upper_arm_circumference_code, district_tuberculosis_number,
                                          opportunistic_infection, trimethoprim_days_dispensed,
                                          other_medications_dispensed, arv_regimen_days_dispensed, trimethoprim_dosage,
                                          ar_regimen_dose, nutrition_support_and_infant_feeding, baseline_regimen,
                                          baseline_weight, baseline_stage, baseline_cd4, baseline_pregnancy,
                                          name_of_family_member, age_of_family_member, family_member_set, hiv_test,
                                          hiv_test_facility, other_side_effects, other_tests_ordered,
                                          care_entry_point_set, treatment_supporter_tel_no,
                                          other_reason_for_missing_arv, current_regimen_other, treatment_supporter_name,
                                          cd4_classification_for_infants, baseline_regimen_start_date,
                                          baseline_regimen_set, transfer_out_date, transfer_out_set,
                                          health_education_disclosure, other_referral_ordered, age_in_months,
                                          test_result_type, lab_result_txt, lab_result_set, counselling_session_type,
                                          cotrim_given, eid_visit_1_appointment_date, feeding_status_at_eid_visit_1,
                                          counselling_approach, current_hiv_test_result, results_received_as_a_couple,
                                          tb_suspect, baseline_lactating, inh_dosage, inh_days_dispensed, age_unit,
                                          syphilis_test_result, syphilis_test_result_for_partner,
                                          ctx_given_at_eid_visit_1, nvp_given_at_eid_visit_1, eid_visit_1_muac,
                                          medication_duration, clinical_impression_comment, reason_for_appointment,
                                          medication_history, quantity_of_medication,
                                          tb_with_rifampin_resistance_checking, specimen_sources,
                                          eid_immunisation_codes, clinical_assessment_codes,
                                          refiil_of_art_for_the_mother, development_milestone, pre_test_counseling_done,
                                          hct_entry_point, linked_to_care, estimated_gestational_age, eid_concept_type,
                                          hiv_viral_load_date, relationship_to_patient, other_reason_for_appointment,
                                          nutrition_assessment, art_pill_balance, differentiated_service_delivery,
                                          stable_in_dsdm, reason_for_testing, previous_hiv_tests_date,
                                          milligram_per_meter_squared, hiv_testing_service_delivery_model,
                                          hiv_syphillis_duo, prevention_services_received, hiv_first_time_tester,
                                          previous_hiv_test_results, results_received_as_individual,
                                          health_education_setting, health_edu_intervation_approaches,
                                          health_education_depression_status, ovc_screening, art_preparation_readiness,
                                          ovc_assessment, phdp_components, tpt_start_date, tpt_completion_date,
                                          advanced_disease_status, family_member_hiv_status, tpt_status,
                                          rpr_test_results, crag_test_results, tb_lam_results, gender_based_violance,
                                          dapsone_ctx_medset, tuberculosis_medication_set, fluconazole_medication_set,
                                          cervical_cancer_screening, intention_to_conceive, viral_load_test,
                                          genexpert_test, tb_microscopy_results, tb_microscopy_test, tb_lam, rpr_test,
                                          crag_test, arv_med_set, quantity_unit, tpt_side_effects, split_into_drugs,
                                          lab_number, other_drug_dispensed_set, test, test_result, other_tests,
                                          refill_point_code, next_return_date_at_facility)
SELECT encounter_id,
       client_id,
       encounter_date,
       hemoglobin,
       malnutrition,
       method_of_family_planning,
       oedema,
       cd4_panel,
       cd4_percent,
       hiv_viral_load,
       historical_drug_start_date,
       historical_drug_stop_date,
       current_drugs_used,
       tests_ordered,
       number_of_weeks_pregnant,
       medication_orders,
       viral_load_qualitative,
       hepatitis_b_test_qualitative,
       mid_upper_arm_circumference,
       medication_strength,
       register_serial_number,
       duration_units,
       systolic_blood_pressure,
       diastolic_blood_pressure,
       pulse,
       temperature,
       weight,
       height,
       return_visit_date,
       respiratory_rate,
       head_circumference,
       cd4_count,
       estimated_date_of_confinement,
       pmtct,
       pregnant,
       scheduled_patient_visit,
       entry_point_into_hiv_care,
       who_hiv_clinical_stage,
       name_of_location_transferred_to,
       tuberculosis_status,
       tuberculosis_treatment_start_date,
       adherence_to_cotrim,
       arv_adherence_assessment_code,
       reason_for_missing_arv,
       medication_or_other_side_effects,
       history_of_functional_status,
       body_weight,
       family_planning_status,
       symptom_diagnosis,
       address,
       date_positive_hiv_test_confirmed,
       treatment_supporter_telephone_number,
       transferred_out,
       tuberculosis_treatment_stop_date,
       current_arv_regimen,
       art_duration,
       current_art_duration,
       antenatal_number,
       mid_upper_arm_circumference_code,
       district_tuberculosis_number,
       opportunistic_infection,
       trimethoprim_days_dispensed,
       other_medications_dispensed,
       arv_regimen_days_dispensed,
       trimethoprim_dosage,
       ar_regimen_dose,
       nutrition_support_and_infant_feeding,
       baseline_regimen,
       baseline_weight,
       baseline_stage,
       baseline_cd4,
       baseline_pregnancy,
       name_of_family_member,
       age_of_family_member,
       family_member_set,
       hiv_test,
       hiv_test_facility,
       other_side_effects,
       other_tests_ordered,
       care_entry_point_set,
       treatment_supporter_tel_no,
       other_reason_for_missing_arv,
       current_regimen_other,
       treatment_supporter_name,
       cd4_classification_for_infants,
       baseline_regimen_start_date,
       baseline_regimen_set,
       transfer_out_date,
       transfer_out_set,
       health_education_disclosure,
       other_referral_ordered,
       age_in_months,
       test_result_type,
       lab_result_txt,
       lab_result_set,
       counselling_session_type,
       cotrim_given,
       eid_visit_1_appointment_date,
       feeding_status_at_eid_visit_1,
       counselling_approach,
       current_hiv_test_result,
       results_received_as_a_couple,
       tb_suspect,
       baseline_lactating,
       inh_dosage,
       inh_days_dispensed,
       age_unit,
       syphilis_test_result,
       syphilis_test_result_for_partner,
       ctx_given_at_eid_visit_1,
       nvp_given_at_eid_visit_1,
       eid_visit_1_muac,
       medication_duration,
       clinical_impression_comment,
       reason_for_appointment,
       medication_history,
       quantity_of_medication,
       tb_with_rifampin_resistance_checking,
       specimen_sources,
       eid_immunisation_codes,
       clinical_assessment_codes,
       refiil_of_art_for_the_mother,
       development_milestone,
       pre_test_counseling_done,
       hct_entry_point,
       linked_to_care,
       estimated_gestational_age,
       eid_concept_type,
       hiv_viral_load_date,
       relationship_to_patient,
       other_reason_for_appointment,
       nutrition_assessment,
       art_pill_balance,
       differentiated_service_delivery,
       stable_in_dsdm,
       reason_for_testing,
       previous_hiv_tests_date,
       milligram_per_meter_squared,
       hiv_testing_service_delivery_model,
       hiv_syphillis_duo,
       prevention_services_received,
       hiv_first_time_tester,
       previous_hiv_test_results,
       results_received_as_individual,
       health_education_setting,
       health_edu_intervation_approaches,
       health_education_depression_status,
       ovc_screening,
       art_preparation_readiness,
       ovc_assessment,
       phdp_components,
       tpt_start_date,
       tpt_completion_date,
       advanced_disease_status,
       family_member_hiv_status,
       tpt_status,
       rpr_test_results,
       crag_test_results,
       tb_lam_results,
       gender_based_violance,
       dapsone_ctx_medset,
       tuberculosis_medication_set,
       fluconazole_medication_set,
       cervical_cancer_screening,
       intention_to_conceive,
       viral_load_test,
       genexpert_test,
       tb_microscopy_results,
       tb_microscopy_test,
       tb_lam,
       rpr_test,
       crag_test,
       arv_med_set,
       quantity_unit,
       tpt_side_effects,
       split_into_drugs,
       lab_number,
       other_drug_dispensed_set,
       test,
       test_result,
       other_tests,
       refill_point_code,
       next_return_date_at_facility
FROM mamba_flat_encounter_art_card as fu;
-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_fact_encounter_hiv_art_update  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_encounter_hiv_art_update;

CREATE PROCEDURE sp_fact_encounter_hiv_art_update()
BEGIN
-- $BEGIN
-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_fact_encounter_hiv_art  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_encounter_hiv_art;

CREATE PROCEDURE sp_fact_encounter_hiv_art()
BEGIN
-- $BEGIN
CALL sp_fact_encounter_hiv_art_create();
CALL sp_fact_encounter_hiv_art_insert();
CALL sp_fact_encounter_hiv_art_update();
-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_fact_encounter_hiv_art_query  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_encounter_hiv_art_query;
CREATE PROCEDURE sp_fact_encounter_hiv_art_query(IN START_DATE
                                                     DATETIME, END_DATE DATETIME)
BEGIN
    SELECT hemoglobin,
           malnutrition,
           method_of_family_planning,
           oedema,
           cd4_panel,
           cd4_percent,
           hiv_viral_load,
           historical_drug_start_date,
           historical_drug_stop_date,
           current_drugs_used,
           tests_ordered,
           number_of_weeks_pregnant,
           medication_orders,
           viral_load_qualitative,
           hepatitis_b_test_qualitative,
           mid_upper_arm_circumference,
           medication_strength,
           register_serial_number,
           duration_units,
           systolic_blood_pressure,
           diastolic_blood_pressure,
           pulse,
           temperature,
           weight,
           height,
           return_visit_date,
           respiratory_rate,
           head_circumference,
           cd4_count,
           estimated_date_of_confinement,
           pmtct,
           pregnant,
           scheduled_patient_visit,
           entry_point_into_hiv_care,
           who_hiv_clinical_stage,
           name_of_location_transferred_to,
           tuberculosis_status,
           tuberculosis_treatment_start_date,
           adherence_to_cotrim,
           arv_adherence_assessment_code,
           reason_for_missing_arv,
           medication_or_other_side_effects,
           history_of_functional_status,
           body_weight,
           family_planning_status,
           symptom_diagnosis,
           address,
           date_positive_hiv_test_confirmed,
           treatment_supporter_telephone_number,
           transferred_out,
           tuberculosis_treatment_stop_date,
           current_arv_regimen,
           art_duration,
           current_art_duration,
           antenatal_number,
           mid_upper_arm_circumference_code,
           district_tuberculosis_number,
           opportunistic_infection,
           trimethoprim_days_dispensed,
           other_medications_dispensed,
           arv_regimen_days_dispensed,
           trimethoprim_dosage,
           ar_regimen_dose,
           nutrition_support_and_infant_feeding,
           baseline_regimen,
           baseline_weight,
           baseline_stage,
           baseline_cd4,
           baseline_pregnancy,
           name_of_family_member,
           age_of_family_member,
           family_member_set,
           hiv_test,
           hiv_test_facility,
           other_side_effects,
           other_tests_ordered,
           care_entry_point_set,
           treatment_supporter_tel_no,
           other_reason_for_missing_arv,
           current_regimen_other,
           treatment_supporter_name,
           cd4_classification_for_infants,
           baseline_regimen_start_date,
           baseline_regimen_set,
           transfer_out_date,
           transfer_out_set,
           health_education_disclosure,
           other_referral_ordered,
           age_in_months,
           test_result_type,
           lab_result_txt,
           lab_result_set,
           counselling_session_type,
           cotrim_given,
           eid_visit_1_appointment_date,
           feeding_status_at_eid_visit_1,
           counselling_approach,
           current_hiv_test_result,
           results_received_as_a_couple,
           tb_suspect,
           baseline_lactating,
           inh_dosage,
           inh_days_dispensed,
           age_unit,
           syphilis_test_result,
           syphilis_test_result_for_partner,
           ctx_given_at_eid_visit_1,
           nvp_given_at_eid_visit_1,
           eid_visit_1_muac,
           medication_duration,
           clinical_impression_comment,
           reason_for_appointment,
           medication_history,
           quantity_of_medication,
           tb_with_rifampin_resistance_checking,
           specimen_sources,
           eid_immunisation_codes,
           clinical_assessment_codes,
           refiil_of_art_for_the_mother,
           development_milestone,
           pre_test_counseling_done,
           hct_entry_point,
           linked_to_care,
           estimated_gestational_age,
           eid_concept_type,
           hiv_viral_load_date,
           relationship_to_patient,
           other_reason_for_appointment,
           nutrition_assessment,
           art_pill_balance,
           differentiated_service_delivery,
           stable_in_dsdm,
           reason_for_testing,
           previous_hiv_tests_date,
           milligram_per_meter_squared,
           hiv_testing_service_delivery_model,
           hiv_syphillis_duo,
           prevention_services_received,
           hiv_first_time_tester,
           previous_hiv_test_results,
           results_received_as_individual,
           health_education_setting,
           health_edu_intervation_approaches,
           health_education_depression_status,
           ovc_screening,
           art_preparation_readiness,
           ovc_assessment,
           phdp_components,
           tpt_start_date,
           tpt_completion_date,
           advanced_disease_status,
           family_member_hiv_status,
           tpt_status,
           rpr_test_results,
           crag_test_results,
           tb_lam_results,
           gender_based_violance,
           dapsone_ctx_medset,
           tuberculosis_medication_set,
           fluconazole_medication_set,
           cervical_cancer_screening,
           intention_to_conceive,
           viral_load_test,
           genexpert_test,
           tb_microscopy_results,
           tb_microscopy_test,
           tb_lam,
           rpr_test,
           crag_test,
           arv_med_set,
           quantity_unit,
           tpt_side_effects,
           split_into_drugs,
           lab_number,
           other_drug_dispensed_set,
           test,
           test_result,
           other_tests,
           refill_point_code,
           next_return_date_at_facility
    FROM mamba_fact_encounter_hiv_art

    WHERE hivart.encounter_date >= START_DATE
      AND hivart.encounter_dat <= END_DATE;
END //

DELIMITER ;


        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_data_processing_derived_hiv_art  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_data_processing_derived_hiv_art;

CREATE PROCEDURE sp_data_processing_derived_hiv_art()
BEGIN
-- $BEGIN
-- CALL sp_dim_client_hiv_hts;
CALL sp_fact_encounter_hiv_art;
-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_dim_client_covid_create  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_dim_client_covid_create;

CREATE PROCEDURE sp_dim_client_covid_create()
BEGIN
-- $BEGIN
CREATE TABLE dim_client_covid
(
    id            INT auto_increment,
    client_id     INT           NULL,
    date_of_birth DATE          NULL,
    ageattest     INT           NULL,
    sex           NVARCHAR(50)  NULL,
    county        NVARCHAR(255) NULL,
    sub_county    NVARCHAR(255) NULL,
    ward          NVARCHAR(255) NULL,
    PRIMARY KEY (id)
);
-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_dim_client_covid_insert  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_dim_client_covid_insert;

CREATE PROCEDURE sp_dim_client_covid_insert()
BEGIN
-- $BEGIN
INSERT INTO dim_client_covid (client_id,
                              date_of_birth,
                              ageattest,
                              sex,
                              county,
                              sub_county,
                              ward)
SELECT c.client_id,
       date_of_birth,
       DATEDIFF(CAST(cd.order_date AS DATE), CAST(date_of_birth as DATE)) / 365 as ageattest,
       sex,
       county,
       sub_county,
       ward
FROM dim_client c
         INNER JOIN flat_encounter_covid cd
                    ON c.client_id = cd.client_id;
-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_dim_client_covid_update  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_dim_client_covid_update;

CREATE PROCEDURE sp_dim_client_covid_update()
BEGIN
-- $BEGIN
-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_dim_client_covid  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_dim_client_covid;

CREATE PROCEDURE sp_dim_client_covid()
BEGIN
-- $BEGIN
CALL sp_dim_client_covid_create();
CALL sp_dim_client_covid_insert();
CALL sp_dim_client_covid_update();
-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_fact_encounter_covid_create  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_encounter_covid_create;

CREATE PROCEDURE sp_fact_encounter_covid_create()
BEGIN
-- $BEGIN
CREATE TABLE IF NOT EXISTS fact_encounter_covid
(
    encounter_id                      INT           NULL,
    client_id                         INT           NULL,
    covid_test                        NVARCHAR(255) NULL,
    order_date                        DATE          NULL,
    result_date                       DATE          NULL,
    date_assessment                   DATE          NULL,
    assessment_presentation           NVARCHAR(255) NULL,
    assessment_contact_case           INT           NULL,
    assessment_entry_country          INT           NULL,
    assessment_travel_out_country     INT           NULL,
    assessment_follow_up              INT           NULL,
    assessment_voluntary              INT           NULL,
    assessment_quarantine             INT           NULL,
    assessment_symptomatic            INT           NULL,
    assessment_surveillance           INT           NULL,
    assessment_health_worker          INT           NULL,
    assessment_frontline_worker       INT           NULL,
    assessment_rdt_confirmatory       INT           NULL,
    assessment_post_mortem            INT           NULL,
    assessment_other                  INT           NULL,
    date_onset_symptoms               DATE          NULL,
    symptom_cough                     INT           NULL,
    symptom_headache                  INT           NULL,
    symptom_red_eyes                  INT           NULL,
    symptom_sneezing                  INT           NULL,
    symptom_diarrhoea                 INT           NULL,
    symptom_sore_throat               INT           NULL,
    symptom_tiredness                 INT           NULL,
    symptom_chest_pain                INT           NULL,
    symptom_joint_pain                INT           NULL,
    symptom_loss_smell                INT           NULL,
    symptom_loss_taste                INT           NULL,
    symptom_runny_nose                INT           NULL,
    symptom_fever_chills              INT           NULL,
    symptom_muscular_pain             INT           NULL,
    symptom_general_weakness          INT           NULL,
    symptom_shortness_breath          INT           NULL,
    symptom_nausea_vomiting           INT           NULL,
    symptom_abdominal_pain            INT           NULL,
    symptom_irritability_confusion    INT           NULL,
    symptom_disturbance_consciousness INT           NULL,
    symptom_other                     INT           NULL,
    comorbidity_present               INT           NULL,
    comorbidity_tb                    INT           NULL,
    comorbidity_liver                 INT           NULL,
    comorbidity_renal                 INT           NULL,
    comorbidity_diabetes              INT           NULL,
    comorbidity_hiv_aids              INT           NULL,
    comorbidity_malignancy            INT           NULL,
    comorbidity_chronic_lung          INT           NULL,
    comorbidity_hypertension          INT           NULL,
    comorbidity_former_smoker         INT           NULL,
    comorbidity_cardiovascular        INT           NULL,
    comorbidity_current_smoker        INT           NULL,
    comorbidity_immunodeficiency      INT           NULL,
    comorbidity_chronic_neurological  INT           NULL,
    comorbidity_other                 INT           NULL,
    diagnostic_pcr_test               NVARCHAR(255) NULL,
    diagnostic_pcr_result             NVARCHAR(255) NULL,
    rapid_antigen_test                NVARCHAR(255) NULL,
    rapid_antigen_result              NVARCHAR(255) NULL,
    long_covid_description            NVARCHAR(255) NULL,
    patient_outcome                   NVARCHAR(255) NULL,
    date_recovered                    DATE          NULL,
    date_died                         DATE          NULL
);
-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_fact_encounter_covid_insert  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_encounter_covid_insert;

CREATE PROCEDURE sp_fact_encounter_covid_insert()
BEGIN
-- $BEGIN
INSERT INTO fact_encounter_covid (encounter_id,
                                  client_id,
                                  covid_test,
                                  order_date,
                                  result_date,
                                  date_assessment,
                                  assessment_presentation,
                                  assessment_contact_case,
                                  assessment_entry_country,
                                  assessment_travel_out_country,
                                  assessment_follow_up,
                                  assessment_voluntary,
                                  assessment_quarantine,
                                  assessment_symptomatic,
                                  assessment_surveillance,
                                  assessment_health_worker,
                                  assessment_frontline_worker,
                                  assessment_rdt_confirmatory,
                                  assessment_post_mortem,
                                  assessment_other,
                                  date_onset_symptoms,
                                  symptom_cough,
                                  symptom_headache,
                                  symptom_red_eyes,
                                  symptom_sneezing,
                                  symptom_diarrhoea,
                                  symptom_sore_throat,
                                  symptom_tiredness,
                                  symptom_chest_pain,
                                  symptom_joint_pain,
                                  symptom_loss_smell,
                                  symptom_loss_taste,
                                  symptom_runny_nose,
                                  symptom_fever_chills,
                                  symptom_muscular_pain,
                                  symptom_general_weakness,
                                  symptom_shortness_breath,
                                  symptom_nausea_vomiting,
                                  symptom_abdominal_pain,
                                  symptom_irritability_confusion,
                                  symptom_disturbance_consciousness,
                                  symptom_other,
                                  comorbidity_present,
                                  comorbidity_tb,
                                  comorbidity_liver,
                                  comorbidity_renal,
                                  comorbidity_diabetes,
                                  comorbidity_hiv_aids,
                                  comorbidity_malignancy,
                                  comorbidity_chronic_lung,
                                  comorbidity_hypertension,
                                  comorbidity_former_smoker,
                                  comorbidity_cardiovascular,
                                  comorbidity_current_smoker,
                                  comorbidity_immunodeficiency,
                                  comorbidity_chronic_neurological,
                                  comorbidity_other,
                                  diagnostic_pcr_test,
                                  diagnostic_pcr_result,
                                  rapid_antigen_test,
                                  rapid_antigen_result,
                                  long_covid_description,
                                  patient_outcome,
                                  date_recovered,
                                  date_died)
SELECT encounter_id,
       client_id,
       covid_test,
       cast(order_date AS DATE)          order_date,
       cast(result_date AS DATE)         result_date,
       cast(date_assessment AS DATE)     date_assessment,
       assessment_presentation,
       assessment_contact_case,
       assessment_entry_country,
       assessment_travel_out_country,
       assessment_follow_up,
       assessment_voluntary,
       assessment_quarantine,
       assessment_symptomatic,
       assessment_surveillance,
       assessment_health_worker,
       assessment_frontline_worker,
       assessment_rdt_confirmatory,
       assessment_post_mortem,
       assessment_other,
       cast(date_onset_symptoms AS DATE) date_onset_symptoms,
       symptom_cough,
       symptom_headache,
       symptom_red_eyes,
       symptom_sneezing,
       symptom_diarrhoea,
       symptom_sore_throat,
       symptom_tiredness,
       symptom_chest_pain,
       symptom_joint_pain,
       symptom_loss_smell,
       symptom_loss_taste,
       symptom_runny_nose,
       symptom_fever_chills,
       symptom_muscular_pain,
       symptom_general_weakness,
       symptom_shortness_breath,
       symptom_nausea_vomiting,
       symptom_abdominal_pain,
       symptom_irritability_confusion,
       symptom_disturbance_consciousness,
       symptom_other,
       CASE
           WHEN comorbidity_present IN ('Yes', 'True') THEN 1
           WHEN comorbidity_present IN ('False', 'No') THEN 0
           END AS                        comorbidity_present,
       comorbidity_tb,
       comorbidity_liver,
       comorbidity_renal,
       comorbidity_diabetes,
       comorbidity_hiv_aids,
       comorbidity_malignancy,
       comorbidity_chronic_lung,
       comorbidity_hypertension,
       comorbidity_former_smoker,
       comorbidity_cardiovascular,
       comorbidity_current_smoker,
       comorbidity_immunodeficiency,
       comorbidity_chronic_neurological,
       comorbidity_other,
       diagnostic_pcr_test,
       diagnostic_pcr_result,
       rapid_antigen_test,
       rapid_antigen_result,
       long_covid_description,
       patient_outcome,
       cast(date_recovered AS DATE)      date_recovered,
       cast(date_died AS DATE)           date_died
FROM flat_encounter_covid;
-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_fact_encounter_covid_update  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_encounter_covid_update;

CREATE PROCEDURE sp_fact_encounter_covid_update()
BEGIN
-- $BEGIN
-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_fact_encounter_covid  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_encounter_covid;

CREATE PROCEDURE sp_fact_encounter_covid()
BEGIN
-- $BEGIN
CALL sp_fact_encounter_covid_create();
CALL sp_fact_encounter_covid_insert();
CALL sp_fact_encounter_covid_update();
-- $END
END //

DELIMITER ;

        
-- ---------------------------------------------------------------------------------------------
-- ----------------------  sp_data_processing_derived_covid  ----------------------------
-- ---------------------------------------------------------------------------------------------

DELIMITER //

DROP PROCEDURE IF EXISTS sp_data_processing_derived_covid;

CREATE PROCEDURE sp_data_processing_derived_covid()
BEGIN
-- $BEGIN
CALL sp_dim_client_covid;
CALL sp_fact_encounter_covid;
-- $END
END //

DELIMITER ;

