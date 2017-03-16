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
	 *
	 * HMIS 106A - 1A #1
	 *
	 * @return the indicator
	 */
	public CohortIndicator cumulativePatientsEnrolledInCareAtTheEndOfLastQuarter() {
		return cohortIndicator("Cumulative Number of Patients Ever Enrolled In Care at this facility at the end of the previous quarter",
				map(artCohortLibrary.enrolledInCareAtEndOfPeriod(), "onOrBefore=${startDate-1d}"));
	}
	/**
	 *
	 * HMIS 106A - 1A #2
	 *
	 * @return the indicator
	 */
	public CohortIndicator newPatientsEnrolledInCare() {
		return cohortIndicator("No. of new patients enrolled in HIV care at this facility during the reporting quarter (Exclude Transfer In)",
				map(artCohortLibrary.enrolledInCareForPeriodWithoutTransferIn(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	
	/**
	 * HMIS 106A - 1A #3
	 *
	 * @return the indicator
	 */
	public CohortIndicator pregnantAndLactatingWomenEnrolledInQuarter() {
		return cohortIndicator("No. pregnant and lactating women enrolled into care during the reporting quarter (Subset of"
						+ " row 2 above)",
				map(artCohortLibrary.pregnantAndLactatingWomenEnrolledIntoCare(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	
	/**
	 * HMIS 106A - 1A #4
	 *
	 * @return the indicator
	 */
	public CohortIndicator clientsStartedOnINHPropphylaxis() {
		return cohortIndicator("No. of clients started on INH Prophylaxis during the reporting quarter (subset of row 2 above))",
				map(artCohortLibrary.startedINHProphylaxis(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	
	/**
	 *
	 * HMIS 106A - 1A #5
	 *
	 * @return the indicator
	 */
	public CohortIndicator cumulativePatientsEnrolledInCareAtTheEndOfReportingQuarter() {
		return cohortIndicator("Cumulative Number of Patients Ever Enrolled In Care at this facility at the end of the reporting quarter (row1+row2)",
				map(artCohortLibrary.enrolledInCareAtEndOfPeriod(), "onOrBefore=${endDate}"));
	}
	
	/**
	 * HMIS 106A - 1A #6
	 *
	 * @return the indicator
	 */
	public CohortIndicator transferInAlreadyEnrolledInHIVCare() {
		return cohortIndicator("No. of persons already enrolled in HIV care who transferred in from another facility during the quarter",
				map(commonCohortLibrary.transferredIn(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	/**
	 *
	 * HMIS 106A - 1A #7
	 *
	 * @return the indicator
	 */
	public CohortIndicator activeClientsOnPreART(){
		return cohortIndicator("No. of active clients active on pre-ART care in the quarter",
				map(artCohortLibrary.activeClientOnPreART(), "onOrBefore=${endDate}"));
	}
	/**
	 * HMIS 106A - 1A #8
	 *
	 * @return the indicator
	 */
	public CohortIndicator preARTClientsWhoGotCPTOnLastVisit() {
		return cohortIndicator("No. active on pre-ART Care who received CPT/Dapsone on their last visit in the quarter",
				map(artCohortLibrary.preARTClientsWhoGotCPTOnLastVisit(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	/**
	 * HMIS 106A - 1A #9
	 *
	 * @return the indicator
	 */
	public CohortIndicator preARTClientsAssessedForTBAtLastVisit() {
		return cohortIndicator("No. active on pre-ART care who were assessed for TB on last visit in the quarter",
				map(artCohortLibrary.preARTClientsAssessedForTBAtLastVisit(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	
	/**
	 * HMIS 106A - 1A #10
	 *
	 * @return the indicator
	 */
	public CohortIndicator preARTClientsDiagnosedWithTB() {
		return cohortIndicator("No. active on pre-ART care who were diagnosed with TB in the quarter",
				map(artCohortLibrary.preARTClientsDiagnosedWithTBInQuarter(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	/**
	 * HMIS 106A - 1A #11
	 *
	 * @return the indicator
	 */
	public CohortIndicator preARTClientsStartedOnTBTreatment() {
		return cohortIndicator("No. active on pre-ART care started on anti-TB treatment during the quarter",
				map(artCohortLibrary.preARTClientsStartedOnTBTreatmentInQuarter(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	
	/**
	 * HMIS 106A - 1A #12
	 *
	 * @return the indicator
	 */
	public CohortIndicator preARTClientsAssessedForMalnutrition() {
		return cohortIndicator("No. active on pre-ART assesed for Malnutrition in their visit in the quarter",
				map(artCohortLibrary.preARTClientsAssessedForMalnutrition(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	
	/**
	 * HMIS 106A - 1A #15
	 *
	 * @return the indicator
	 */
	public CohortIndicator cumulativeClientsStartedOnARTAtEndOfPreviousQuarter(){
		return cohortIndicator("Cumulative No. of clients ever started on ART at this facility at the end of the previous quarter",
				map(artCohortLibrary.clientStartingART(), "onOrBefore=${startDate-1d}"));
	}
	/**
	 * HMIS 106A - 1A #16
	 *
	 * @return the indicator
	 */
	public CohortIndicator newClientStartedOnART(){
		return cohortIndicator("No. of new clients started on ART at this facility during the quarter",
				map(artCohortLibrary.clientStartingART(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	
	/**
	 * HMIS 106A - 1A #18
	 *
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
	 * @return
	 */
	public CohortIndicator clientsOnThirdLineRegimen() {
		return cohortIndicator("No. active on ART on 3rd line or higher ARV regimen",
				map(artCohortLibrary.clientonThirdLineRegimen(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	
	/**
	 * HMIS 106A - 1A #23
	 *
	 * @return the indicator
	 */
	public CohortIndicator ARTClientsWhoGotCPTOnLastVisit() {
		return cohortIndicator("No. active on ART who received CPT/Dapsone on their last visit in the quarter",
				map(artCohortLibrary.ARTClientsWhoGotCPTOnLastVisit(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	/**
	 * HMIS 106A - 1A #24
	 *
	 * @return the indicator
	 */
	public CohortIndicator ARTClientsAssessedForTBAtLastVisit() {
		return cohortIndicator("No. active on ART who were assessed for TB on last visit in the quarter",
				map(artCohortLibrary.ARTClientsAssessedForTBAtLastVisit(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	
	/**
	 * HMIS 106A - 1A #25
	 *
	 * @return the indicator
	 */
	public CohortIndicator ARTClientsDiagnosedWithTB() {
		return cohortIndicator("No. active on ART who were diagnosed with TB in the quarter",
				map(artCohortLibrary.ARTClientsDiagnosedWithTBInQuarter(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	/**
	 * HMIS 106A - 1A #26
	 *
	 * @return the indicator
	 */
	public CohortIndicator ARTClientsStartedOnTBTreatment() {
		return cohortIndicator("No. active on ART started on anti-TB treatment during the quarter",
				map(artCohortLibrary.ARTClientsStartedOnTBTreatmentInQuarter(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	
	/**
	 * HMIS 106A - 1A #27
	 *
	 * @return the indicator
	 */
	public CohortIndicator ARTClientsOnTBTreatment() {
		return cohortIndicator("Total No. active on ART and on TB treatment during the quarter",
				map(artCohortLibrary.ARTClientsOnTBTreatmentInQuarter(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
	}
	
	
}