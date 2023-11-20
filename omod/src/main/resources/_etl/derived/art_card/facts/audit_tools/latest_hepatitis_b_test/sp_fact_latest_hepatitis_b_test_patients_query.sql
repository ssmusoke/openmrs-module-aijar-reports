DELIMITER //

DROP PROCEDURE IF EXISTS sp_fact_patient_latest_hepatitis_b_test_query;
CREATE PROCEDURE sp_fact_patient_latest_hepatitis_b_test_query()
BEGIN
    SELECT *
    FROM mamba_fact_patients_latest_hepatitis_b_test;
END //

DELIMITER ;




