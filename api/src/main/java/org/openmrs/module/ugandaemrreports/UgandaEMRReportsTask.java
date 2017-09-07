package org.openmrs.module.ugandaemrreports;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrreports.library.UgandaEMRReporting;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.sql.Connection;
import java.util.Date;

import static org.openmrs.module.ugandaemrreports.library.UgandaEMRReporting.summarizeObs;

/**
 * Created by carapai on 25/08/2017.
 */
public class UgandaEMRReportsTask extends AbstractTask {

    public void execute() {
        String response = summarizeObs();
        System.out.println(response);
    }
}