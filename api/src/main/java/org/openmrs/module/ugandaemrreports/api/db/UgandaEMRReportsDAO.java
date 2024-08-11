package org.openmrs.module.ugandaemrreports.api.db;

import org.openmrs.Program;
import org.openmrs.Cohort;
import org.openmrs.module.ugandaemrreports.api.UgandaEMRReportsService;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.reporting.ReportObjectWrapper;


import java.util.List;

/**
 * Database methods for {@link UgandaEMRReportsService}.
 */
public interface UgandaEMRReportsDAO {
    void executeFlatteningScript();

    List<ReportObjectWrapper> getReportObjects(String type);
    PatientSearch getPatientSearchByUuid(String uuid);

    Cohort getPatientCurrentlyInPrograms(String uuid);

}
