package org.openmrs.module.ugandaemrreports.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.ugandaemrreports.metadata.CommonReportMetadata;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 */
public class PatientDataHelper {

    protected Log log = LogFactory.getLog(this.getClass());

    public void addCol(DataSetRow row, String label, Object value) {
        if (value == null) {
            value = "";
        }
        DataSetColumn c = new DataSetColumn(label, label, value.getClass());
        row.addColumnValue(c, value);
    }
}
