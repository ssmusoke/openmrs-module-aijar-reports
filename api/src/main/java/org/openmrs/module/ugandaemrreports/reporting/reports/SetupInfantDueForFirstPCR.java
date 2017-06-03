package org.openmrs.module.ugandaemrreports.reporting.reports;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.openmrs.PatientIdentifierType;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.AgeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.BirthdateDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.data.person.definition.RelationshipsForPersonDataDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.Cohorts;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.EIDCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.reporting.dataset.definition.SharedDataDefintion;
import org.openmrs.module.ugandaemrreports.reports.UgandaEMRDataExportManager;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Infants due for 1st PCR which is at 6 weeks of age
 */
@Component
public class SetupInfantDueForFirstPCR extends UgandaEMRDataExportManager {
	
	@Autowired
	private DataFactory df;
	
	@Autowired
	SharedDataDefintion sdd;
	
	@Autowired
	EIDCohortDefinitionLibrary eidCohortDefinitionLibrary;
	
	@Override
	public String getExcelDesignUuid() {
		return "fd9327b4-9e4a-4afc-95c7-842361018510";
	}
	
	@Override
	public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
		ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "ANCRegister.xls");
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:7,dataset:PCR");
		props.put("sortWeight", "5000");
		rd.setProperties(props);
		return rd;
	}
	
	@Override
	public String getUuid() {
		return "47bbb9e4-a160-47f7-a547-619fcc5dff0d";
	}
	
	@Override
	public String getName() {
		return "Infants Due for 1st DNA PCR";
	}
	
	@Override
	public String getDescription() {
		return "Infants Due for 1st DNA PCR at 6 weeks of age";
	}
	
	@Override
	public ReportDefinition constructReportDefinition() {
		ReportDefinition rd = new ReportDefinition();
		
		rd.setUuid(getUuid());
		rd.setName(getName());
		rd.setDescription(getDescription());
		rd.addParameters(getParameters());
		rd.addDataSetDefinition("PCR", Mapped.mapStraightThrough(constructDataSetDefinition()));
		return rd;
	}
	
	
	@Override
	public String getVersion() {
		return "0.1";
	}
	
	@Override
	public List<Parameter> getParameters() {
		List<Parameter> l = new ArrayList<Parameter>();
		l.add(df.getEndDateParameter());
		return l;
	}
	
	private DataSetDefinition constructDataSetDefinition() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("PCR");
		dsd.addParameters(getParameters());
		dsd.addRowFilter(eidCohortDefinitionLibrary.getExposedInfantsDueForFirstPCR(), "onOrBefore=${endDate},endDate=${endDate},effectiveDate=${endDate},startDate=${endDate}");
		
		//identifier
		// TODO: Standardize this as a external method that takes the UUID of the PatientIdentifier
		PatientIdentifierType exposedInfantNo = MetadataUtils.existing(PatientIdentifierType.class, "2c5b695d-4bf3-452f-8a7c-fe3ee3432ffe");
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(exposedInfantNo.getName(), exposedInfantNo), identifierFormatter);
		
		dsd.addColumn("EID No", identifierDef, (String) null);
		dsd.addColumn("Infant Name", new PreferredNameDataDefinition(), (String) null);
		dsd.addColumn("Sex", new GenderDataDefinition(), (String) null);
		dsd.addColumn("Birth Date", new BirthdateDataDefinition(), "effectiveDate=${endDate}");
		dsd.addColumn("Age", new AgeDataDefinition(), "effectiveDate=${endDate}");
		dsd.addColumn("1stPCRDueDate", new AgeDataDefinition(), "effectiveDate=${endDate}");
		
		return dsd;
	}
	
	private RelationshipsForPersonDataDefinition getMother() {
		RelationshipsForPersonDataDefinition mother = new RelationshipsForPersonDataDefinition();
		
		return mother;
	}
	
}
