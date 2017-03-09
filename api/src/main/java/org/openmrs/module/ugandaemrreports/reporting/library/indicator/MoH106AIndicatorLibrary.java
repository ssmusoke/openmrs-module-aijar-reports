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

import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.ugandaemrreports.reporting.library.cohort.ARTCohortLibrary;
import org.openmrs.module.ugandaemrreports.reporting.library.cohort.CommonCohortLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.openmrs.module.ugandaemrreports.reporting.utils.EmrReportingUtils.cohortIndicator;
import static org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils.map;


/**
 * Library of HIV related indicator definitions for the HMIS 106A report.
 *
 * All indicators require parameters ${startDate} and ${endDate}
 */
@Component
public class MoH106AIndicatorLibrary {

	@Autowired
	private ARTCohortLibrary artCohortLibrary;
	
	@Autowired
	private CommonCohortLibrary commonCohortLibrary;

	/**
	 * Cumulative number of new patients enrolled in HIV care as of the last day of the last quarter
	 * @return the indicator
	 */
	public CohortIndicator cumulativePatientsEnrolledInCareAtTheEndOfLastQuarter() {
		return cohortIndicator("Cumulative Number of Patients Ever Enrolled In Care at this facility at the end of the previous quarter",
				map(artCohortLibrary.enrolledInCare(), "onOrBefore=${startDate-1d}"));
	}
	/**
	 * Cumulative number of new patients enrolled in HIV care as of a specified date
	 * @return the indicator
	 */
	public CohortIndicator cumulativePatientsEnrolledInCareAtTheEndOfReportingQuarter() {
		return cohortIndicator("Cumulative Number of Patients Ever Enrolled In Care at this facility at the end of the reporting quarter (row1+row2)",
				map(artCohortLibrary.enrolledInCare(), "onOrBefore=${endDate}"));
	}
	
	/**
	 * Number of new patients enrolled in HIV care (excluding transfer in)
	 * @return the indicator
	 */
	public CohortIndicator newPatientsEnrolledInCare() {
		return cohortIndicator("No. of new patients enrolled in HIV care at this facility during the reporting quarter (Exclude Transfer In)",
				map(artCohortLibrary.enrolledInCareForPeriodWithoutTransferIn(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	/**
	 * Number of pregnant and lactating women enrolled into care in quarter
	 * @return the indicator
	 */
	public CohortIndicator pregnantAndLactatingWomenEnrolledInQuarter() {
		return cohortIndicator("No. pregnant and lactating women enrolled into care during the reporting quarter (Subset of"
				+ " row 2 above)",
				map(artCohortLibrary.pregnantAndLactatingWomenEnrolledIntoCare(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	/**
	 * Number of clients transferred in while aready in HIV care
	 * @return the indicator
	 */
	public CohortIndicator transferInAlreadyEnrolledInHIVCare() {
		return cohortIndicator("No. of persons already enrolled in HIV care who transferred in from another facility during the quarter",
				map(commonCohortLibrary.transferredIn(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	public CohortIndicator activeClientsOnPreART(){
		return cohortIndicator("No. of active clients active on pre-ART care in the quarter",
				map(artCohortLibrary.activeClientOnPreART(), "onOrBefore=${endDate}"));
	}
	public CohortIndicator cumulativeClientsStartedOnARTAtEndOfPreviousQuarter(){
		return cohortIndicator("Cumulative No. of clients ever started on ART at this facility at the end of the previous quarter",
				map(artCohortLibrary.clientOnART(), "onOrBefore=${startDate-1d}"));
	}
	public CohortIndicator newClientStartedOnART(){
		return cohortIndicator("No. of new clients started on ART at this facility during the quarter",
				map(artCohortLibrary.clientOnART(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	
	/**
	 * HMIS 106A - 1A #18 - No. of pregnant and lactating women started on ART at this facility during the quarter (Subset of row 16 above)
	 * @return
	 */
	public CohortIndicator newPregnantOrLactatingClientStartedOnART(){
		return cohortIndicator("No. of pregnant and lactating women started on ART at this facility during the quarter (Subset of row 16 above)",
				map(artCohortLibrary.pregnantOrLactatingAtARTStart(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	/**
	 *
	 * HMIS 106A - 1A #20
	 *
	 * Clients on First Line Regimen
	 * @return
	 */
	public CohortIndicator clientsOnFirstLineRegimen() {
		return cohortIndicator("No. active on ART on 1st line ARV regimen",
				map(artCohortLibrary.clientOnFirstLineRegimen(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	
	/**
	 *
	 * HMIS 106A - 1A #21
	 *
	 * Clients on Second line ART Regimen
	 * @return
	 */
	
	public CohortIndicator clientsOnSecondLineRegimen() {
		return cohortIndicator("No. active on ART on 2nd line ARV regimen",
				map(artCohortLibrary.clientOnSecondLineRegimen(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	
	/**
	 *
	 * HMIS 106A - 1A #22
	 *
	 * Clients on Second line ART Regimen
	 * @return
	 */
	
	public CohortIndicator clientsOnThirdLineRegimen() {
		return cohortIndicator("No. active on ART on 3rd line or higher ARV regimen",
				map(artCohortLibrary.clientonThirdLineRegimen(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	
	
	
	
}