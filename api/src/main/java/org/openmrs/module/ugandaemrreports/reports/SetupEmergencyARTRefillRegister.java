package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.EmergencyARTRefillDatasetDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.HCTDatasetDefinition;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Emergency ART Refill Register Report
 * */

@Component
public class SetupEmergencyARTRefillRegister extends UgandaEMRDataExportManager {
	
	
	@Autowired
	private DataFactory df;
	
	@Override
	public String getDescription() {
		return "Emergency ART Refill Register" ;
	}
	
	@Override
	public String getName() {
		return "Emergency ART Refill Register";
	}
	
	@Override
	public String getUuid() {
		return "9f523291-3ddf-4286-aeee-db0f3687150a";
	}
	

	/**
     * @return the uuid for the report design for exporting to Excel
     */
	@Override
	public String getExcelDesignUuid() {
		return "df299e64-d7fb-4ab4-859e-d31c7aec90bc";
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
		ReportDesign rd = createExcelTemplateDesign("56a1e5e0-7423-4026-8900-e2fedd491469", reportDefinition, "Emergency_ARTRefill.xls");
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:3-4,dataset:DATASET");
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

		EmergencyARTRefillDatasetDefinition dsd = new EmergencyARTRefillDatasetDefinition();
		dsd.setName(getName());
		dsd.setParameters(getParameters());
		rd.addDataSetDefinition("DATASET", Mapped.mapStraightThrough(dsd));
		return rd;
	}

	@Override
	public String getVersion() {
		return "0.3";
	}

}
