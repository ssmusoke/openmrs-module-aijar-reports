package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Export of all ART patients enrolled into care until a specified date
 */
@Component
public class SetupARTCarePatientExportList extends UgandaEMRDataExportManager {

	@Autowired
	ARTClinicCohortDefinitionLibrary hivCohorts;

	@Autowired
	private DataFactory df;

	@Autowired
	private BuiltInPatientDataLibrary builtInPatientData;

	@Autowired
	private HIVPatientDataLibrary hivPatientData;

	@Autowired
	private BasePatientDataLibrary basePatientData;
	
	/**
	 * @return the uuid for the report design for exporting to Excel
	 */
	@Override
	public String getExcelDesignUuid() {
		return "d8b3db94-f36e-4dcd-b58e-f7197d2a9dce";
	}

	@Override
	public String getUuid() {
		return "0272907f-f2bf-437f-8bf9-8c4cb0a983fa";
	}

	@Override
	public String getName() {
		return "ART Care Patient List";
	}

	@Override
	public String getDescription() {
		return "A cumulative list of patients who are enrolled into care at the clinic to the specified dates";
	}

	@Override
	public List<Parameter> getParameters() {
		List<Parameter> l = new ArrayList<Parameter>();
		l.add(df.getEndDateParameter());
		return l;
	}

	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		List<ReportDesign> l = new ArrayList<ReportDesign>();
		l.add(buildReportDesign(reportDefinition));
		return l;
	}

	/**
	 * Build the report design for the specified report, this allows a user to override the report design by adding
	 * properties and other metadata to the report design
	 *
	 * @param reportDefinition
	 * @return The report design
	 */
	@Override
	public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
		ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "FacilityARTPatientExport.xls");
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:4,dataset:PatientExport");
		props.put("sortWeight", "5000");
		rd.setProperties(props);
		return rd;
	}

	@Override
	public ReportDefinition constructReportDefinition() {
		ReportDefinition rd = new ReportDefinition();
		rd.setUuid(getUuid());
		rd.setName(getName());
		rd.setDescription(getDescription());
		rd.setParameters(getParameters());

		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName(getName());
		dsd.setParameters(getParameters());
		rd.addDataSetDefinition("PatientExport", Mapped.mapStraightThrough(dsd));
		
		dsd.addSortCriteria("Date Enrolled", SortCriteria.SortDirection.ASC);
		// rows are patients with a next appointment date obs in the given date range

		CohortDefinition patientsInCare = Cohorts.getPatientsWhoEnrolledInCareUntilDate();
		dsd.addRowFilter(Mapped.mapStraightThrough(patientsInCare));

		// columns to include
		addColumn(dsd, "ID", hivPatientData.getClinicNumber());
		addColumn(dsd, "Family Name", builtInPatientData.getPreferredFamilyName());
		addColumn(dsd, "Given Name", builtInPatientData.getPreferredGivenName());
		addColumn(dsd, "Gender", builtInPatientData.getGender());
		addColumn(dsd, "Date of Birth", builtInPatientData.getBirthdate());
		addColumn(dsd, "Age", hivPatientData.getAgeDuringPeriod());
		addColumn(dsd,"Parish",df.getPreferredAddress("address4"));
		addColumn(dsd,"Village",df.getPreferredAddress("address5"));
		addColumn(dsd, "Telephone", basePatientData.getTelephone());
		addColumn(dsd, "Date Enrolled", hivPatientData.getEnrollmentDate());
		addColumn(dsd, "ART Start Date", hivPatientData.getARTStartDate());
		addColumn(dsd, "Last Regimen Pickup Date", hivPatientData.getLastRegimenPickupDate()); // when the patient last got ART drugs, N/A for lost to follow up and those
		// not in ART
		addColumn(dsd, "ARV Duration", hivPatientData.getARVDuration());
		addColumn(dsd, "Expected Return Date", hivPatientData.getExpectedReturnDate());
		addColumn(dsd, "Current Regimen", hivPatientData.getCurrentRegimen());
		addColumn(dsd, "Current Regimen Date", hivPatientData.getCurrentRegimenDate());
		addColumn(dsd, "Start Regimen", hivPatientData.getStartRegimen());
		addColumn(dsd, "Start Regimen Date", hivPatientData.getStartRegimenDate());
		addColumn(dsd, "CD4 at Enrollment", hivPatientData.getCD4AtEnrollment());
		addColumn(dsd, "Baseline CD4", hivPatientData.getBaselineCD4());
		addColumn(dsd, "CD4 at 6 months", hivPatientData.getCD4At6months());


		return rd;
	}

	@Override
	public String getVersion() {
		return "0.8";
	}
}
