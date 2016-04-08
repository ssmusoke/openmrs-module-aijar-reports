package org.openmrs.module.aijarreports.reports;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.aijarreports.library.ARTClinicCohortDefinitionLibrary;
import org.openmrs.module.aijarreports.library.BasePatientDataLibrary;
import org.openmrs.module.aijarreports.library.DataFactory;
import org.openmrs.module.aijarreports.library.HIVPatientDataLibrary;
import org.openmrs.module.aijarreports.metadata.CommonReportMetadata;
import org.openmrs.module.aijarreports.metadata.HIVMetadata;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Daily Appointments List report
 */
@Component
public class SetupARTCarePatientExportList extends AijarDataExportManager {
	@Autowired
	private DataFactory df;

	@Autowired
	ARTClinicCohortDefinitionLibrary hivCohorts;

	@Autowired
	private CommonReportMetadata commonMetadata;

	@Autowired
	private HIVMetadata hivMetadata;

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
		l.add(ReportManagerUtil.createCsvReportDesign(getExcelDesignUuid(), reportDefinition));
		return l;
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
		rd.addDataSetDefinition(getName(), Mapped.mapStraightThrough(dsd));

		// rows are patients with a next appointment date obs in the given date range
		CohortDefinition rowFilter = hivCohorts.getPatientsWithAppointmentOnDate();
		dsd.addRowFilter(Mapped.mapStraightThrough(rowFilter)) ;

		// columns to include
		addColumn(dsd, "ID", hivPatientData.getClinicNumber());
		addColumn(dsd, "Family Name", builtInPatientData.getPreferredFamilyName());
		addColumn(dsd, "Given Name", builtInPatientData.getPreferredGivenName());
		addColumn(dsd, "Gender", builtInPatientData.getGender());
		addColumn(dsd, "Date of Birth", builtInPatientData.getBirthdate());
		addColumn(dsd, "Current Age", builtInPatientData.getAgeAtEnd());
		addColumn(dsd, "Date Enrolled", hivPatientData.getEnrollmentDate());
		addColumn(dsd, "ART Start Date", hivPatientData.getARTStartDate());

		return rd;
	}

	@Override
	public String getVersion() {
		return "0.1";
	}
}
