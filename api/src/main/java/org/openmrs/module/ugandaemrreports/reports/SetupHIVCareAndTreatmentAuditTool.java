package org.openmrs.module.ugandaemrreports.reports;


import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.ReportingAuditToolDataSetDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.ReportingComprehensiveAuditToolDataSetDefinition;
import org.openmrs.module.ugandaemrreports.library.ARTClinicCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.reporting.dataset.definition.SharedDataDefintion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Care and Treatment Audit Tool
 */
@Component
public class SetupHIVCareAndTreatmentAuditTool extends UgandaEMRDataExportManager {

	@Autowired
	ARTClinicCohortDefinitionLibrary hivCohorts;

	@Autowired
	private DataFactory df;

	@Autowired
	SharedDataDefintion sdd;
	
	/**
	 * @return the uuid for the report design for exporting to Excel
	 */
	@Override
	public String getExcelDesignUuid() {
		return "a899c5a7-17d4-4c47-a431-89d8d44f5768";
	}

	@Override
	public String getUuid() {
		return "b5cbec70-3dcf-4b4b-99f9-45e0f0a9a23b";
	}

	@Override
	public String getName() {
		return "HIV Care and Treatment Service Quality Assessment Tool ";
	}

	@Override
	public String getDescription() {
		return "Patient list with details of services received for continuous service quality improvement";
	}

	@Override
	public List<Parameter> getParameters() {
		List<Parameter> l = new ArrayList<Parameter>();
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
		ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "HIV_AUDIT_TOOL.xls");
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:2,dataset:A");
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

		ReportingComprehensiveAuditToolDataSetDefinition dataSetDefinition = new ReportingComprehensiveAuditToolDataSetDefinition();
		dataSetDefinition.setName(getName());
		dataSetDefinition.setParameters(getParameters());
		rd.addDataSetDefinition("A", Mapped.mapStraightThrough(dataSetDefinition));
		return rd;
	}

	@Override
	public String getVersion() {
		return "0.1.4";
	}
}
