package org.openmrs.module.ugandaemrreports.reports;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.TBDatasetDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Health Unit TB Register Report
 * */

@Component
public class SetupTBRegister extends UgandaEMRDataExportManager {
	
	
	@Autowired
	private DataFactory df;
	
	@Override
	public String getDescription() {
		return "Health Unit TB Register";
	}
	
	@Override
	public String getName() {
		return "096a - Health Unit TB Register";
	}
	
	@Override
	public String getUuid() {
		return "00002c21-2c46-4ea1-9993-4ea63c3d7e06";
	}
	
	@Override
	public String getVersion() {
		return "0.1";
	}
	
	/**
     * @return the uuid for the report design for exporting to Excel
     */
	@Override
	public String getExcelDesignUuid() {
		return "4cffa2ab-daa6-4895-9930-50835bfaa29f";
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
		ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "096aHealthUnitTBRegister.xls");
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:8,dataset:TB");
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
		
		TBDatasetDefinition dsd = new TBDatasetDefinition();
		dsd.setName(getName());
		dsd.setParameters(getParameters());
		rd.addDataSetDefinition("TB", Mapped.mapStraightThrough(dsd));
		return rd;
	}
}
