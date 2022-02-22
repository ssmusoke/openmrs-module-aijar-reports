package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.data.person.definition.BirthdateDataDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.data.converter.ObsDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.RegimenLineConverter;
import org.openmrs.module.ugandaemrreports.definition.data.definition.ActiveInPeriodDataDefinition;
import org.openmrs.module.ugandaemrreports.library.ARTClinicCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.BasePatientDataLibrary;
import org.openmrs.module.ugandaemrreports.library.Cohorts;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.HIVPatientDataLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Care and Treatment Audit Tool
 */
@Component
public class SetupARTCareAndTreatmentAuditTool extends UgandaEMRDataExportManager {

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
		return "b483c304-793d-11c0-a478-f0e40e519e5b";
	}

	@Override
	public String getUuid() {
		return "138140ef-1dd2-11b2-96d8-6cab73c93472";
	}

	@Override
	public String getName() {
		return "HIV Care and Treatment Service Quality Assessment Tool";
	}

	@Override
	public String getDescription() {
		return "Patient list with details of services received for continuous service quality improvement";
	}

	@Override
	public List<Parameter> getParameters() {
		List<Parameter> l = new ArrayList<Parameter>();
		l.add(df.getStartDateParameter());
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
		ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "CareAndTreatmentAuditTool.xls");
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:3,dataset:ClientAuditTool");
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
		rd.addDataSetDefinition("ClientAuditTool", Mapped.mapStraightThrough(dsd));
		
		CohortDefinition patientsInCare = Cohorts.getPatientsWhoEnrolledInCareUntilDate();
		dsd.addRowFilter(Mapped.mapStraightThrough(patientsInCare));

		// columns to include
		addColumn(dsd, "ID", hivPatientData.getClinicNumber());
		addColumn(dsd, "Family Name", builtInPatientData.getPreferredFamilyName());
		addColumn(dsd, "Given Name", builtInPatientData.getPreferredGivenName());
		addColumn(dsd, "Gender", builtInPatientData.getGender());
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter("MMM dd,yyyy"));
		addColumn(dsd, "Age", hivPatientData.getAgeDuringPeriod());
		addColumn(dsd,"Parish",df.getPreferredAddress("address4"));
		addColumn(dsd,"Village",df.getPreferredAddress("address5"));
		addColumn(dsd, "Telephone", basePatientData.getTelephone());
		addColumn(dsd, "ART Start Date", hivPatientData.getARTStartDate());
		addColumn(dsd, "Current Regimen", hivPatientData.getCurrentRegimen());
		addColumn(dsd, "Current Regimen Date", hivPatientData.getCurrentRegimenDate());
		addColumn(dsd, "Start Regimen", hivPatientData.getStartRegimen());
		addColumn(dsd, "Weight", hivPatientData.getWeight());
		addColumn(dsd, "Start Regimen Date", hivPatientData.getStartRegimenDate());
		addColumn(dsd, "VL Date", hivPatientData.getViralLoadDate());
		addColumn(dsd,"VL Qualitative",hivPatientData.getVLQualitativeByEndDate());
		addColumn(dsd, "VL Quantitative",  hivPatientData.getCurrentViralLoad());
		addColumn(dsd, "Prescription Duration", hivPatientData.getARVDuration());
		addColumn(dsd, "Last Visit Date", hivPatientData.getLastVisitDate());
		addColumn(dsd, "Next Appointment Date", hivPatientData.getExpectedReturnDate());
		dsd.addColumn("IsActiveCurrentMonth", new ActiveInPeriodDataDefinition(), "startDate=${startDate},endDate=${endDate}", new ObsDataConverter());
		dsd.addColumn("IsActiveMonth1", new ActiveInPeriodDataDefinition(), "startDate=${startDate}-1m,endDate=${endDate}-1m", new ObsDataConverter());
		dsd.addColumn("IsActiveMonth2", new ActiveInPeriodDataDefinition(), "startDate=${startDate}-2m,endDate=${endDate}-2m", new ObsDataConverter());
		dsd.addColumn("IsActiveMonth3", new ActiveInPeriodDataDefinition(), "startDate=${startDate}-3m,endDate=${endDate}-3m", new ObsDataConverter());
		dsd.addColumn("IsActiveMonth4", new ActiveInPeriodDataDefinition(), "startDate=${startDate}-4m,endDate=${endDate}-4m", new ObsDataConverter());
		dsd.addColumn("IsActiveMonth5", new ActiveInPeriodDataDefinition(), "startDate=${startDate}-5m,endDate=${endDate}-5m", new ObsDataConverter());
		return rd;
	}

	@Override
	public String getVersion() {
		return "1.0.1";
	}
}
