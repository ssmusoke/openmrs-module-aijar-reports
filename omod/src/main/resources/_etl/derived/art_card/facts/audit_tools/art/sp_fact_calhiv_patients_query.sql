DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_audit_tool_art_query;
CREATE PROCEDURE sp_fact_audit_tool_art_query(IN id_list VARCHAR(255))
BEGIN
    SELECT *
    FROM mamba_fact_audit_tool_art_patients audit_tool where client_id in (id_list);
END //

DELIMITER ;




