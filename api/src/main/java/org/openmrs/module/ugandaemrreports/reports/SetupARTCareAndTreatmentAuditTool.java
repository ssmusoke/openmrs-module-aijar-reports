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
	private BuiltInPatientDataLibrary builtInPatientData;

	@Autowired
	private HIVPatientDataLibrary hivPatientData;

	@Autowired
	private BasePatientDataLibrary basePatientData;

	@Autowired
	private HIVMetadata hivMetadata;

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
		l.add(df.getStartDateParameter());
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
		ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "CareAndTreatmentAuditTool.xls");
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:3,dataset:ClientAuditTool");
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

		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName(getName());
		dsd.setParameters(getParameters());
		rd.addDataSetDefinition("ClientAuditTool", Mapped.mapStraightThrough(dsd));
		
		CohortDefinition patientsInCare = Cohorts.getPatientsWhoEnrolledInCareUntilDate();
		dsd.addRowFilter(Mapped.mapStraightThrough(patientsInCare));

		// columns to include
		addColumn(dsd, "ID", hivPatientData.getClinicNumber());
		addColumn(dsd, "Family Name", builtInPatientData.getPreferredFamilyName());
		addColumn(dsd, "Given Name", builtInPatientData.getPreferredGivenName());
		addColumn(dsd, "Gender", builtInPatientData.getGender());
		dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter("MMM dd,yyyy"));
		addColumn(dsd, "Age", hivPatientData.getAgeDuringPeriod());
		addColumn(dsd,"Parish",df.getPreferredAddress("address4"));
		addColumn(dsd,"Village",df.getPreferredAddress("address5"));
		addColumn(dsd, "Telephone", basePatientData.getTelephone());
		addColumn(dsd, "ART Start Date", hivPatientData.getARTStartDate());
		addColumn(dsd, "Current Regimen", hivPatientData.getCurrentRegimen());
		addColumn(dsd, "Current Regimen Date", hivPatientData.getCurrentRegimenDate());
		addColumn(dsd, "Start Regimen", hivPatientData.getStartRegimen());
		addColumn(dsd, "Weight", hivPatientData.getWeight());
		addColumn(dsd, "Start Regimen Date", hivPatientData.getStartRegimenDate());
		addColumn(dsd, "VL Date", hivPatientData.getViralLoadDate());
		addColumn(dsd,"VL Qualitative",hivPatientData.getVLQualitativeByEndDate());
		addColumn(dsd, "VL Quantitative",  hivPatientData.getCurrentViralLoad());
		addColumn(dsd, "Prescription Duration", hivPatientData.getARVDuration());
		addColumn(dsd, "Last Visit Date", hivPatientData.getLastVisitDate());
		addColumn(dsd, "Next Appointment Date", hivPatientData.getExpectedReturnDate());
		addColumn(dsd, "IsActiveCurrentMonth",getActiveInPeriodDataDefinition(null));
		addColumn(dsd,"IsActiveMonth1", getActiveInPeriodDataDefinition("1m"));
		addColumn(dsd,"IsActiveMonth2", getActiveInPeriodDataDefinition("2m"));
		addColumn(dsd,"IsActiveMonth3", getActiveInPeriodDataDefinition("3m"));
		addColumn(dsd,"IsActiveMonth4", getActiveInPeriodDataDefinition("4m"));
		addColumn(dsd,"IsActiveMonth5", getActiveInPeriodDataDefinition("5m"));
		addColumn(dsd,"HIVDR_Date",df.getObsByEndDate(getConcept("b913c0d9-f279-4e43-bb8e-3d1a4cf1ad4d"), hivMetadata.getIACEncounters(), TimeQualifier.LAST, new ObsValueConverter()));
		addColumn(dsd,"CurrentRegimenLine", hivPatientData.getRegimenLine());
		addColumn(dsd,"CurrentRegimenLineStartDate", hivPatientData.getRegimenLineStartDate());
		addColumn(dsd,"TPT Start Date",hivPatientData.getTPTInitiationDate());
		addColumn(dsd,"TPT End Date",hivPatientData.getTPTCompletionDate());
		addColumn(dsd,"Last TPT Status",hivPatientData.getTPTLastTPTStatus());
		addColumn(dsd,"TB Status",df.getObsByEndDate(hivMetadata.getTBStatus(), hivMetadata.getARTEncounterPageEncounterType(), TimeQualifier.LAST, new ObsValueConverter()));
		addColumn(dsd,"1st IAC Date",basePatientData.getIAC(0,"date"));
		addColumn(dsd,"2nd IAC Date",basePatientData.getIAC(1,"date"));
		addColumn(dsd,"3rd IAC Date",basePatientData.getIAC(2,"date"));
		addColumn(dsd,"4th IAC Date",basePatientData.getIAC(3,"date"));
		addColumn(dsd,"5th IAC Date",basePatientData.getIAC(4,"date"));
		addColumn(dsd,"6th IAC Date",basePatientData.getIAC(5,"date"));
		dsd.addColumn("Pregnant", sdd.definition("pregnant", getConcept("dcda5179-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        addColumn(dsd,"Client Status",hivPatientData.getClientStatus());
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
		return "1.1.1";
	}
}
