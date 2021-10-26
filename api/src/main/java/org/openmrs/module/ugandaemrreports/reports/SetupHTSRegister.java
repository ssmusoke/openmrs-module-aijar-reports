package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.HTSDatasetDefinition;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * HTS Register Report
 * */

@Component
public class SetupHTSRegister extends UgandaEMRDataExportManager {
	
	
	@Autowired
	private DataFactory df;
	
	@Override
	public String getDescription() {
		return "HIV  Testing Services (HTS)Register" ;
	}
	
	@Override
	public String getName() {
		return "HIV  Testing Services (HTS)Register";
	}
	
	@Override
	public String getUuid() {
		return "aa758901-000b-42b7-afee-bbb88854282b";
	}
	
	@Override
	public String getVersion() {
		return "0.8.5";
	}
	
	/**
     * @return the uuid for the report design for exporting to Excel
     */
	@Override
	public String getExcelDesignUuid() {
		return "52f87d1e-e15a-4d9d-b33a-a44d8319ad3d";
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
		l.add(buildExcelReportDesign(reportDefinition));
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
		ReportDesign rd = createExcelTemplateDesign("79a2e2c2-d744-4a56-bcc4-8b6718f99f28", reportDefinition, "HCTRegister.xls");
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:4-8,dataset:HTS");
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

		HTSDatasetDefinition dsd = new HTSDatasetDefinition();
		dsd.setName(getName());
		dsd.setParameters(getParameters());
		rd.addDataSetDefinition("HTS", Mapped.mapStraightThrough(dsd));
		return rd;
	}
}
