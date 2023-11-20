package org.openmrs.module.ugandaemrreports.api.db;

import org.openmrs.module.ugandaemrreports.api.UgandaEMRReportsService;

/**
 * Database methods for {@link UgandaEMRReportsService}.
 */
public interface UgandaEMRReportsDAO {
    void executeFlatteningScript();
	
}
