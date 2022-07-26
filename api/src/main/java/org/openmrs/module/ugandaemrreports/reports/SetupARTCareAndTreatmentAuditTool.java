package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.reporting.data.converter.ObsValueConverter;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
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
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.CQIHIVAdultToolDataSetDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.dataset.definition.SharedDataDefintion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary.getConcept;

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
	SharedDataDefintion sdd;
	
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
		ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "HIVAdultTool_dashboard.xls");
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

		CQIHIVAdultToolDataSetDefinition dataSetDefinition = new CQIHIVAdultToolDataSetDefinition();
		dataSetDefinition.setName(getName());
		dataSetDefinition.setParameters(getParameters());
		rd.addDataSetDefinition("A", Mapped.mapStraightThrough(dataSetDefinition));
		return rd;
	}

	public PatientDataDefinition getActiveInPeriodDataDefinition(String pastPeriod) {
		ActiveInPeriodDataDefinition def = new ActiveInPeriodDataDefinition();
		def.addParameter(new Parameter("startDate", "Start Date", Date.class));
		def.addParameter(new Parameter("endDate", "End Date", Date.class));
		if(pastPeriod!=null){
			return df.createPatientDataDefinition(def, new ObsDataConverter(), "startDate=startDate-"+pastPeriod+",endDate=endDate-"+pastPeriod);
		}else{
			return df.createPatientDataDefinition(def, new ObsValueConverter(), "startDate=startDate,endDate=endDate");
		}
	}

	@Override
	public String getVersion() {
		return "1.2.9.5.7";
	}
}
