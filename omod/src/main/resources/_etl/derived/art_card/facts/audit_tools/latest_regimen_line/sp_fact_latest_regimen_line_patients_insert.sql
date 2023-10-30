-- $BEGIN
INSERT INTO mamba_fact_patients_latest_regimen_line(client_id,
                                                    regimen)
SELECT DISTINCT pp.patient_id, program_workflow_state.concept_id AS line
FROM patient_state
         INNER JOIN program_workflow_state
                    ON patient_state.state = program_workflow_state.program_workflow_state_id
         INNER JOIN program_workflow ON program_workflow_state.program_workflow_id =
                                        program_workflow.program_workflow_id
         INNER JOIN program ON program_workflow.program_id = program.program_id
         INNER JOIN patient_program pp
                    ON patient_state.patient_program_id = pp.patient_program_id AND
                       program_workflow.concept_id = 166214 AND
                       patient_state.end_date IS NULL;
-- $END