-- $BEGIN
INSERT INTO mamba_fact_encounter_hiv_art (encounter_id,
                                          client_id,
                                          return_date,
                                          current_regimen,
                                          who_stage,
                                          no_of_days,
                                          no_of_pills,
                                          tb_status,
                                          dsdm,
                                          pregnant,
                                          emtct)
SELECT fu.encounter_id,
       fu.client_id,
       fu.return_date,
       fu.current_regimen,
       fu.who_stage,
       fu.no_of_days,
       fu.no_of_pills,
       fu.tb_status,
       fu.dsdm,
       fu.pregnant,
       fu.emtct
FROM mamba_flat_encounter_art_card as fu;
-- $END