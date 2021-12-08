package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.PatientIdentifierType;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.AgeConverter;
import org.openmrs.module.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.AgeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.BirthdateDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.data.converter.CalculationResultDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.ObsDataConverter;
import org.openmrs.module.ugandaemrreports.definition.data.definition.CalculationDataDefinition;
import org.openmrs.module.ugandaemrreports.library.BasePatientDataLibrary;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.EIDCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.HIVPatientDataLibrary;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.calculation.eid.ExposedInfantMotherCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.eid.ExposedInfantMotherPhoneNumberCalculation;
import org.openmrs.module.ugandaemrreports.reporting.dataset.definition.SharedDataDefintion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * EID Audit Tool
 */
@Component
public class SetupEIDAuditTool extends UgandaEMRDataExportManager {
	
	@Autowired
	private DataFactory df;
	
	@Autowired
	SharedDataDefintion sdd;
	
	@Autowired
	EIDCohortDefinitionLibrary eidCohortDefinitionLibrary;
	
	@Autowired
	HIVMetadata hivMetadata;

	@Autowired
	private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

	@Autowired
	private HIVPatientDataLibrary hivPatientData;

	@Override
	public String getExcelDesignUuid() {
		return "5530f615-1422-14de-82c3-8f1e135a7f6c";
	}
	
	@Override
	public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
		ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "EIDDueForFirstPCR.xls");
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:8,dataset:PCR");
		props.put("sortWeight", "5000");
		rd.setProperties(props);
		return rd;
	}
	
	@Override
	public String getUuid() {
		return "13814069-1dd2-11b2-b6fe-eeb59a9a73fa";
	}
	
	@Override
	public String getName() {
		return "HIV Exposed Infants Service Quality Assessment Tool";
	}
	
	@Override
	public String getDescription() {
		return "HIV Exposed Infant list with details of services received for continuous service quality improvement";
	}
	
	@Override
	public ReportDefinition constructReportDefinition() {
		ReportDefinition rd = new ReportDefinition();
		
		rd.setUuid(getUuid());
		rd.setName(getName());
		rd.setDescription(getDescription());
		rd.addParameters(getParameters());
		rd.addDataSetDefinition("EIDAuditTool", Mapped.mapStraightThrough(constructDataSetDefinition()));
		return rd;
	}
	
	
	@Override
	public String getVersion() {
		return "1.0.0";
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
	
	private DataSetDefinition constructDataSetDefinition() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("EIDAuditTool");
		dsd.addParameters(getParameters());
		CohortDefinition enrolledInTheQuarter = hivCohortDefinitionLibrary.getEnrolledInCareBetweenDates();
		CohortDefinition eidDueForFirstPCR = df.getPatientsNotIn(eidCohortDefinitionLibrary.getExposedInfantsDueForFirstPCR(),enrolledInTheQuarter);
		dsd.addRowFilter(eidDueForFirstPCR, "startDate=${startDate},endDate=${endDate}");
		
		//identifier
		// TODO: Standardize this as a external method that takes the UUID of the PatientIdentifier
		PatientIdentifierType exposedInfantNo = MetadataUtils.existing(PatientIdentifierType.class, "2c5b695d-4bf3-452f-8a7c-fe3ee3432ffe");
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(exposedInfantNo.getName(), exposedInfantNo), identifierFormatter);
		
		dsd.addColumn("EID No", identifierDef, (String) null);
		dsd.addColumn("Infant Name", new PreferredNameDataDefinition(), (String) null);
		dsd.addColumn("Birth Date", new BirthdateDataDefinition(), "", new BirthdateConverter("MMM d, yyyy"));
		dsd.addColumn("Age", new AgeDataDefinition(), "", new AgeConverter("{m}"));
		dsd.addColumn("Sex", new GenderDataDefinition(), (String) null);
		dsd.addColumn("Mother Name", new CalculationDataDefinition("Mother Name", new ExposedInfantMotherCalculation()), "", new CalculationResultDataConverter());
		dsd.addColumn("Mother Phone", new CalculationDataDefinition("Mother Phone", new ExposedInfantMotherPhoneNumberCalculation()), "", new CalculationResultDataConverter());
		dsd.addColumn("Mother ART No", sdd.definition("Mother ART No",  hivMetadata.getExposedInfantMotherARTNumber()), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		addColumn(dsd,"Parish",df.getPreferredAddress("address4"));
		addColumn(dsd,"Village",df.getPreferredAddress("address5"));
		addColumn(dsd, "Enrollment Date", hivPatientData.getEIDEnrollmentDate());
		addColumn(dsd, "Last Visit Date", hivPatientData.getLastEIDEncounterVisitDate());
		addColumn(dsd, "Next Appointment Date", hivPatientData.getExpectedEIDEncounterReturnDate());

		return dsd;
	}
	
}
