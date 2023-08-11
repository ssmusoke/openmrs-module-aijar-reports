package org.openmrs.module.ugandaemrreports.reports;


import org.openmrs.Cohort;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.ReportingAuditToolDataSetDefinition;
import org.openmrs.module.ugandaemrreports.library.ARTClinicCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.reporting.dataset.definition.SharedDataDefintion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Care and Treatment Audit Tool
 */
@Component
public class SetupHIVCareAndTreatmentAuditToolLite extends UgandaEMRDataExportManager {

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
		return "e70e30bb-a3a4-4c07-a9f6-45ed4009f092";
	}

	@Override
	public String getUuid() {
		return "2017e2e0-fc1c-4ba2-8816-92f822290c84";
	}

	@Override
	public String getName() {
		return "HIV Care and Treatment Service Quality Assessment Tool Lite Version";
	}

	@Override
	public String getDescription() {
		return "Patient list with details of services received for continuous service quality improvement";
	}

	@Override
	public List<Parameter> getParameters() {
		List<Parameter> l = new ArrayList<Parameter>();
		l.add(new Parameter("startDate", "From:", Date.class));
		l.add(new Parameter("endDate", "To:", Date.class));
		l.add(new Parameter("cohortList", "Cohort:", String.class));
		return l;
	}

	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		List<ReportDesign> l = new ArrayList<ReportDesign>();
		l.add(buildExcelReportDesign(reportDefinition));
		return l;
	}
	@Override
	public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
		return null;
	}

	/**
	 * Build the report design for the specified report, this allows a user to override the report design by adding
	 * properties and other metadata to the report design
	 *
	 * @param reportDefinition
	 * @return The report design
	 */

	public ReportDesign buildExcelReportDesign(ReportDefinition reportDefinition) {
		ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "HIV_AUDIT_TOOL_Lite.xls");
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

		ReportingAuditToolDataSetDefinition dataSetDefinition = new ReportingAuditToolDataSetDefinition();
		dataSetDefinition.setName(getName());
		dataSetDefinition.setParameters(getParameters());
		rd.addDataSetDefinition("A", Mapped.mapStraightThrough(dataSetDefinition));
		return rd;
	}

	@Override
	public String getVersion() {
		return "0.2.0.3";
	}
}
