package org.openmrs.module.ugandaemrreports.library;

import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.ugandaemrreports.common.Enums;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.DHIS2PeriodDatasetDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.EMRVersionDatasetDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.GlobalPropertyParametersDatasetDefinition;

import java.util.Date;

/**
 * Created by carapai on 24/11/2017.
 */
public class CommonDatasetLibrary {

    public static DataSetDefinition period() {
        DHIS2PeriodDatasetDefinition dhis2PeriodDatasetDefinition = new DHIS2PeriodDatasetDefinition();
        dhis2PeriodDatasetDefinition.setName("P");
        dhis2PeriodDatasetDefinition.setPeriodType(Enums.Period.MONTHLY);
        dhis2PeriodDatasetDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
        return dhis2PeriodDatasetDefinition;
    }

    public static DataSetDefinition settings() {
        GlobalPropertyParametersDatasetDefinition cst = new GlobalPropertyParametersDatasetDefinition();
        cst.setName("S");
        cst.setGp("ugandaemr.dhis2.organizationuuid");
        return cst;
    }

    public static DataSetDefinition getUgandaEMRVersion(){
        EMRVersionDatasetDefinition dsd= new EMRVersionDatasetDefinition();
        return dsd;
    }
}
