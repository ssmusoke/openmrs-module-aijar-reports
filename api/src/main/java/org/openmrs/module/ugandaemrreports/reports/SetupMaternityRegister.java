package org.openmrs.module.ugandaemrreports.reports;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.MaternityDatasetDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Integrated Maternity Register Report
 * */

@Component
public class SetupMaternityRegister extends UgandaEMRDataExportManager {
	
	
	@Autowired
	private DataFactory df;
	
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
		return "3607d6cb-e0cd-4296-8b4e-cd2ec8fd190c";
	}
	
	@Override
	public String getVersion() {
		return "1.3";
	}
	
	/**
     * @return the uuid for the report design for exporting to Excel
     */
	@Override
	public String getExcelDesignUuid() {
		return "3506427c-9d4f-40f3-ac4b-dff6455cfc3c";
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
		l.add(buildExcel(reportDefinition));
		return l;
	}
	@Override
	public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
		ReportDesign rd = createCSVDesign(getExcelDesignUuid(), reportDefinition);

		return rd;
	}
	
	/**
	 * Build the report design for the specified report, this allows a user to override the report
	 * design by adding properties and other metadata to the report design
	 *
	 * @param reportDefinition
	 * @return The report design
	 */
	public ReportDesign buildExcel(ReportDefinition reportDefinition) {
		ReportDesign rd = createExcelTemplateDesign("35f7fab3-3c8c-45a1-a309-69421f91e579", reportDefinition, "MaternityRegister.xls");
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
