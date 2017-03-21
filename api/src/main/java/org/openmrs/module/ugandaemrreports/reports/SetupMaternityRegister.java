package org.openmrs.module.ugandaemrreports.reports;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.MaternityDatasetDefinition;
import org.openmrs.module.ugandaemrreports.library.BasePatientDataLibrary;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.metadata.CommonReportMetadata;
import org.springframework.beans.factory.annotation.Autowired;

public class SetupMaternityRegister extends UgandaEMRDataExportManager {
	
	
	@Autowired
	private DataFactory df;
	
	@Autowired
	private CommonReportMetadata commonMetadata;
	
	@Autowired
	private BuiltInPatientDataLibrary builtInPatientData;
	
	@Autowired
	private BasePatientDataLibrary basePatientData;
	
	@Override
	public String getDescription() {
		return "Integrated Maternity Register";
	}
	
	@Override
	public String getName() {
		return "Integrated Maternity Register";
	}
	
	@Override
	public String getUuid() {
		return "d14ba9bd-cff4-45f6-a390-bf9b0bf0f40b";
	}
	
	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return "0.1";
	}
	
	@Override
	public String getExcelDesignUuid() {
		return "98e9202d-8c00-415f-9882-43917181f023";
	}
	
	@Override
	public List<Parameter> getParameters() {
		List<Parameter> l = new ArrayList<Parameter>();
		l.add(df.getStartDateParameter());
		return l;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		List<ReportDesign> l = new ArrayList<ReportDesign>();
		l.add(buildReportDesign(reportDefinition));
		return l;
	}
	
	/**
	 * Build the report design for the specified report, this allows a user to override the report
	 * design by adding properties and other metadata to the report design
	 *
	 * @param reportDefinition
	 * @return The report design
	 */
	@Override
	
	public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
		ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "MaternityRegister.xls");
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:8,dataset:Maternity");
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
		
		MaternityDatasetDefinition dsd = new MaternityDatasetDefinition();
		dsd.setName(getName());
		dsd.setParameters(getParameters());
		rd.addDataSetDefinition("Maternity", Mapped.mapStraightThrough(dsd));
		return rd;
	}
}
