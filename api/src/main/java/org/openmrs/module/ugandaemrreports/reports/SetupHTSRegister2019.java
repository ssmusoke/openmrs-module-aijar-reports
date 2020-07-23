package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.Concept;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.AgeConverter;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.data.person.definition.*;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.data.converter.*;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.reporting.dataset.definition.SharedDataDefintion;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Health Unit TB Register Report
 * */

@Component
public class SetupHTSRegister2019 extends UgandaEMRDataExportManager {
	
	
	@Autowired
	private DataFactory df;

	@Autowired
	SharedDataDefintion sdd;

	@Autowired
	private BuiltInPatientDataLibrary builtInPatientData;
	
	@Override
	public String getDescription() {
		return "HIV COUNSELLING and Testing (HCT) Register 2019" ;
	}
	
	@Override
	public String getName() {
		return "HIV COUNSELLING and Testing (HCT) Register 2019";
	}
	
	@Override
	public String getUuid() {
		return "9021b6b4-da42-4bd5-a4a9-ad92523d7bf5";
	}

	public String getCSVDesignUuid()
	{
		return "8d711174-7454-4094-a560-00ab51f50ac9";
	}


	/**
     * @return the uuid for the report design for exporting to Excel
     */
	@Override
	public String getExcelDesignUuid() {
		return "7faea756-d161-49e9-963c-d5696e89cbe5";
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
		ReportDesign rd = createCSVDesign(getCSVDesignUuid(), reportDefinition);
		return rd;
	}

	public ReportDesign buildExcelReportDesign(ReportDefinition reportDefinition) {
		ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "HTSRegister_2019.xls");
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
		rd.addDataSetDefinition("HTS", Mapped.mapStraightThrough(dataSetDefinition()));
		return rd;
	}
	private Concept getConcept(String uuid) {
		return Dictionary.getConcept(uuid);
	}

	private CohortDefinition getPatientWithHCTEncounterDuringPeriod(){
		return df.getAnyEncounterOfTypesBetweenDates(
				Arrays.asList(Dictionary.getEncounterType("264daIZd-f80e-48fe-nba9-P37f2W1905Pv")));
	}
	private DataSetDefinition dataSetDefinition() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("HTS");
		dsd.addParameters(getParameters());
		dsd.addRowFilter(Mapped.mapStraightThrough(getPatientWithHCTEncounterDuringPeriod()));


		//start constructing of the dataset
		PersonAttributeType phoneNumber = Context.getPersonService().getPersonAttributeTypeByUuid("14d4f066-15f5-102d-96e4-000c29c2a5d7");
		PersonAttributeType maritalStatus = Context.getPersonService().getPersonAttributeTypeByUuid("dce0c134-30ab-102d-86b0-7a5022ba4115");
		PatientIdentifierType NIN = MetadataUtils.existing(PatientIdentifierType.class,"f0c16a6d-dc5f-4118-a803-616d0075d282");

		//identifier
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDefNIN = new ConvertedPatientDataDefinition("identifier",
				new PatientIdentifierDataDefinition(NIN.getName(), NIN), identifierFormatter);



		dsd.addColumn("NIN",identifierDefNIN,"");
		dsd.addColumn("serialNo", sdd.definition("serialNo",  getConcept("1646AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("surName", builtInPatientData.getPreferredFamilyName(),(String)null);
		dsd.addColumn("givenName", builtInPatientData.getPreferredGivenName(),(String)null);
		dsd.addColumn("Age", new AgeDataDefinition(), "", new AgeConverter("{y}"));
		dsd.addColumn("Sex", new GenderDataDefinition(), (String) null);
		dsd.addColumn("MaritalStatus", new PersonAttributeDataDefinition("MaritalStatus", maritalStatus), "", new PersonAttributeDataConverter());
		dsd.addColumn("District",df.getPreferredAddress("countyDistrict") ,(String)null);
		dsd.addColumn("subCounty",df.getPreferredAddress("address3") ,(String)null);
		dsd.addColumn("parish",df.getPreferredAddress("address4") ,(String)null);
		dsd.addColumn("village",df.getPreferredAddress("address5") ,(String)null);
		dsd.addColumn("phoneNumber", new PersonAttributeDataDefinition("phoneNumber", phoneNumber), "", new PersonAttributeDataConverter());
		dsd.addColumn("htsModel", sdd.definition("htsModel",  getConcept("46648b1d-b099-433b-8f9c-3815ff1e0a0f")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("htsApproach", sdd.definition("htsApproach",  getConcept("ff820a28-1adf-4530-bf27-537bfa9ce0b2")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataInUppercaseConverter());
		dsd.addColumn("entrypoint", sdd.definition("entrypoint",  getConcept("720a1e85-ea1c-4f7b-a31e-cb896978df79")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("testingPoints", sdd.definition("testingPoints",  getConcept("4f4e6d1d-4343-42cc-ba47-2319b8a84369")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("reason", sdd.definition("reason",  getConcept("2afe1128-c3f6-4b35-b119-d17b9b9958ed")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataInUppercaseConverter());
		dsd.addColumn("special", sdd.definition("special",  getConcept("927563c5-cb91-4536-b23c-563a72d3f829")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("firstTime",sdd.definition("firstTime",  getConcept("2766c090-c057-44f2-98f0-691b6d0336dc")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("morethan12months", sdd.definition("morethan12months",  getConcept("8037192e-8f0c-4af3-ad8d-ccd1dd6880ba")), "onOrAfter=${startDate},onOrBefore=${endDate}", new TestedForMoreThanOnceInLast12MonthsConverter());
		dsd.addColumn("pre-testConseling",sdd.definition("pre-testConseling",  getConcept("193039f1-c378-4d81-bb72-653b66c69914")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("tested", sdd.definition("tested",  getConcept("3d292447-d7df-417f-8a71-e53e869ec89d")), "onOrAfter=${startDate},onOrBefore=${endDate}", new TestedForHIVDataConverter());
		dsd.addColumn("receivedTestresults", sdd.definition("receivedTestresults",  getConcept("3437ae80-bcc5-41e2-887e-d56999a1b467")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataInUppercaseConverter());
		dsd.addColumn("HIVresults", sdd.definition("HIVresults",  getConcept("3d292447-d7df-417f-8a71-e53e869ec89d")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("infectionResults", sdd.definition("infectionResults",  getConcept("141520BBBBBBBBBBBBBBBBBBBBBBBBBBBBBB")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("alreadyKnownHIV+", sdd.definition("alreadyKnownHIV+",  getConcept("49ba801d-b6ff-47cd-8d29-e0ac8649cb7d")), "onOrAfter=${startDate},onOrBefore=${endDate}", new AlreadyKnownHIVPositiveConverter());
		dsd.addColumn("coupletesting", sdd.definition("coupletesting",  getConcept("b92b1777-4356-49b2-9c83-a799680dc7d4")), "onOrAfter=${startDate},onOrBefore=${endDate}", new CounseledAsCoupleConverter());
		dsd.addColumn("coupleResultsReceived", sdd.definition("coupleResultsReceived",  getConcept("2aa9f0c1-3f7e-49cd-86ee-baac0d2d5f2d")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataInUppercaseConverter());
		dsd.addColumn("discordantResults",sdd.definition("discordantResults",  getConcept("94a5bd0a-b79d-421e-ab71-8e382eed100f")), "onOrAfter=${startDate},onOrBefore=${endDate}", new DiscordantCoupleResultsConverter());
		dsd.addColumn("corcodantPostive", sdd.definition("corcodantPostive",  getConcept("94a5bd0a-b79d-421e-ab71-8e382eed100f")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ConcordantCoupleResultsConverter());
		dsd.addColumn("TBCase", sdd.definition("TBCase",  getConcept("b80f04a4-1559-42fd-8923-f8a6d2456a04")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataInUppercaseConverter());
		dsd.addColumn("referedForTBServices", sdd.definition("referedForTBServices",  getConcept("c5da115d-f6a3-4d13-b182-c2e982a3a796")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("refferedToEnrollment", sdd.definition("refferedTonrollment",  getConcept("3d620422-0641-412e-ab31-5e45b98bc459")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataInUppercaseConverter());





		return dsd;
	}

	@Override
	public String getVersion() {
		return "3.0.1";
	}
}
