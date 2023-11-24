package org.openmrs.module.ugandaemrreports.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.checkerframework.checker.units.qual.C;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.report.util.ReportUtil;
import org.openmrs.module.ugandaemrreports.api.UgandaEMRReportsService;
import org.openmrs.module.ugandaemrreports.api.db.hibernate.HibernateUgandaEMRReportsDAO;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.apache.commons.lang.StringUtils.isBlank;


public class MambaTask extends AbstractTask {
    Log log = LogFactory.getLog(MambaTask.class);

    @Override
    public void execute() {

        try {
            Date todayDate = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            String strLastRunDate = Context.getAdministrationService()
                    .getGlobalPropertyObject("ugandaemrreports.mamba.flatten.last.successful.run.date").getPropertyValue();
            if (!isBlank(strLastRunDate)) {
                Date gpLastRunDate = null;
                try {
                    gpLastRunDate = new SimpleDateFormat("yyyy-MM-dd").parse(strLastRunDate);
                }
                catch (ParseException e) {
                    log.info("Error parsing last successful Run date " + strLastRunDate + e);
                    log.error(e);
                    return;
                }
                if (dateFormat.format(gpLastRunDate).equals(dateFormat.format(todayDate))) {
                    log.info("Last successful submission was today"
                            + System.lineSeparator());
                    return;
                }
            }

            log.info("Mamba Flatten Started");
            UgandaEMRReportsService ugandaEMRReportsService = Context.getService(UgandaEMRReportsService.class);
            ugandaEMRReportsService.executeFlatteningScript();
            log.info("Mamba Flatten Completed");

            ReportUtil.updateGlobalProperty("ugandaemrreports.mamba.flatten.last.successful.run.date",
                    dateTimeFormat.format(todayDate));
            log.info("Recency data has been sent to central server");
        }
        catch (Exception e) {
            log.error(e.fillInStackTrace());
        }
    }

}