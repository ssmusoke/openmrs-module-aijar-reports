package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.HCTDataExportDatasetDefinition;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Health Unit TB Register Report
 * */

@Component
public class SetUpHTCDataExportReport extends UgandaEMRDataExportManager {
	
	
	@Autowired
	private DataFactory df;
	
	@Override
	public String getDescription() {
		return "HIV COUNSELLING and Testing (HCT) Data Export" ;
	}
	
	@Override
	public String getName() {
		return "HIV COUNSELLING and Testing (HCT) Data Export";
	}
	
	@Override
	public String getUuid() {
		return "76d3cbdb-7b81-423e-8a53-cc3773f2c456";
	}
	
	@Override
	public String getVersion() {
		return "2.3";
	}
	
	/**
     * @return the uuid for the report design for exporting to Excel
     */
	@Override
	public String getExcelDesignUuid() {
		return "7cfd0c6b-9112-45e9-9a3e-cfc7c213944f";
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
//		l.add(buildExcelReportDesign(reportDefinition));
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
		ReportDesign rd = createCSVDesign(getExcelDesignUuid(), reportDefinition);
		return rd;
	}

	public ReportDesign buildExcelReportDesign(ReportDefinition reportDefinition) {
		ReportDesign rd = createExcelTemplateDesign("435a7170-142c-44b4-a3b3-a4da5a5fa7fb", reportDefinition, "HCTDataExportReport.xls");
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:3,dataset:HCTDATAEXPORT");
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

		HCTDataExportDatasetDefinition dsd = new HCTDataExportDatasetDefinition();
		dsd.setName(getName());
		dsd.setParameters(getParameters());
		rd.addDataSetDefinition("HCTDATAEXPORT", Mapped.mapStraightThrough(dsd));
		return rd;
	}
}
