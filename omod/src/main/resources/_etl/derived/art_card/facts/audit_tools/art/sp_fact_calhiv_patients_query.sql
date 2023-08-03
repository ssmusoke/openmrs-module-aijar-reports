DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_audit_tool_art_query;
CREATE PROCEDURE sp_fact_audit_tool_art_query()
BEGIN
    SELECT *
    FROM mamba_fact_audit_tool_art_patients audit_tool;
END //

DELIMITER ;




