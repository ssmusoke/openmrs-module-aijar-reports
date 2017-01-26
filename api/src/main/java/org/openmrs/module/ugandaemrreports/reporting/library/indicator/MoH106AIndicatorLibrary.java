/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.ugandaemrreports.reporting.library.indicator;

import org.openmrs.Concept;
import org.openmrs.Program;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.ugandaemrreports.reporting.library.cohort.ARTCohortLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.openmrs.module.ugandaemrreports.reporting.utils.EmrReportingUtils.cohortIndicator;
import static org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils.map;


/**
 * Library of HIV related indicator definitions. All indicators require parameters ${startDate} and ${endDate}
 */
@Component
public class MoH106AIndicatorLibrary {


	@Autowired
	private ARTCohortLibrary cohorts;

	/**
	 * Number of new patients enrolled in HIV care (excluding transfers)
	 * @return the indicator
	 */
	/**
	 * Number of patients who are on Cotrimoxazole prophylaxis
	 * @return the indicator
	 */
	public CohortIndicator cumulativePatientsEnrolledInCare() {
		return cohortIndicator("Cumulative Number of Patients Ever Enrolled In Care", map(cohorts.enrolledInCareOnOrBefore(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}

}