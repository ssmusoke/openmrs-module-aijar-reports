package org.openmrs.module.ugandaemrreports;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrreports.library.UgandaEMRReporting;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.sql.Connection;
import java.util.Date;

/**
 * Created by carapai on 25/08/2017.
 */
public class UgandaEMRReportsTask extends AbstractTask {

    public void execute() {
		try {
			Context.openSession();
			Connection connection = UgandaEMRReporting.sqlConnection();
			String lastDenormalizationDate = UgandaEMRReporting.getGlobalProperty("ugandaemrreports.lastDenormalizationDate");
			String lastSummarizationDate = UgandaEMRReporting.getGlobalProperty("ugandaemrreports.lastSummarizationDate");

			if (StringUtils.isBlank(lastDenormalizationDate)) {
				lastDenormalizationDate = "1900-01-01 00:00:00";
			}

			if (StringUtils.isBlank(lastSummarizationDate)) {
				lastSummarizationDate = "1900-01-01 00:00:00";
			}

			UgandaEMRReporting.normalizeObs(lastDenormalizationDate, connection, 100000);
			int response = UgandaEMRReporting.summarizeObs(UgandaEMRReporting.obsSummaryMonthQuery(lastSummarizationDate), connection);

			Date now = new Date();
			String newDate = UgandaEMRReporting.DEFAULT_DATE_FORMAT.format(now);

			UgandaEMRReporting.setGlobalProperty("ugandaemrreports.lastDenormalizationDate", newDate);
			UgandaEMRReporting.setGlobalProperty("ugandaemrreports.lastSummarizationDate", newDate);

			Context.closeSession();
		}
		catch (Exception e) {
			System.out.println("Error occured");
		}
		
	}
}