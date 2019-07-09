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
import org.openmrs.module.ugandaemrreports.definition.data.converter.PatientIdentifierConverter;
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


	
	/**
     * @return the uuid for the report design for exporting to Excel
     */
	@Override
	public String getExcelDesignUuid() {
		return "7faea756-d161-49e9-963c-d5696e89cbe9";
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
		ReportDesign rd = createExcelTemplateDesign("8d711174-7454-4094-a560-00ab51f50ac8", reportDefinition, "HTSRegister_2019.xls");
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
		PersonAttributeType maritalStatus = Context.getPersonService().getPersonAttributeTypeByUuid("8d871f2a-c2cc-11de-8d13-0010c6dffd0f");
		PatientIdentifierType NIN = Context.getPatientService().getPatientIdentifierTypeByUuid("f0c16a6d-dc5f-4118-a803-616d0075d282");

		//identifier
		PatientIdentifierType preARTNo = MetadataUtils.existing(PatientIdentifierType.class, "e1731641-30ab-102d-86b0-7a5022ba4115");
		DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
		DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(preARTNo.getName(), preARTNo), identifierFormatter);


		dsd.addColumn("NIN", new PatientIdentifierDataDefinition("NIN",NIN), "", new PatientIdentifierConverter());
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
		dsd.addColumn("htsModel", sdd.definition("htsModel",  getConcept("720a1e85-ea1c-4f7b-a31e-cb896978df79")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("htsApproach", sdd.definition("htsApproach",  getConcept("ff820a28-1adf-4530-bf27-537bfa9ce0b2")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("entrypoint", sdd.definition("entrypoint",  getConcept("720a1e85-ea1c-4f7b-a31e-cb896978df79")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("testingPoints", sdd.definition("testingPoints",  getConcept("4f4e6d1d-4343-42cc-ba47-2319b8a84369")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("reason", sdd.definition("reason",  getConcept("2afe1128-c3f6-4b35-b119-d17b9b9958ed")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("special", sdd.definition("special",  getConcept("927563c5-cb91-4536-b23c-563a72d3f829")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("firstTime",sdd.definition("firstTime",  getConcept("d6522d62-093d-4157-a9d3-9359d1a33480")), "onOrAfter=${startDate},onOrBefore=${endDate}", new FirstTimeHIVTESTConverter());
		dsd.addColumn("morethan12months", sdd.definition("morethan12months",  getConcept("8037192e-8f0c-4af3-ad8d-ccd1dd6880ba")), "onOrAfter=${startDate},onOrBefore=${endDate}", new TestedForMoreThanOnceInLast12MonthsConverter());
		dsd.addColumn("pre-testConseling",sdd.definition("pre-testConseling",  getConcept("193039f1-c378-4d81-bb72-653b66c69914")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("tested", sdd.definition("tested",  getConcept("3d292447-d7df-417f-8a71-e53e869ec89d")), "onOrAfter=${startDate},onOrBefore=${endDate}", new TestedForHIVDataConverter());
		dsd.addColumn("receivedTestresults", sdd.definition("receivedTestresults",  getConcept("ad2884a2-830f-4ca8-bc1e-1e1fd2df0f81")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("HIVresults", sdd.definition("HIVresults",  getConcept("3d292447-d7df-417f-8a71-e53e869ec89d")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("finalResults",sdd.definition("finalResults",  getConcept("dca0a383-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("refLabResults", sdd.definition("refLabResults",  getConcept("dca0a383-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("infectionResults", sdd.definition("infectionResults",  getConcept("dca0a383-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("alreadyKnownHIV+", sdd.definition("alreadyKnownHIV+",  getConcept("dca0a383-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("coupletesting", sdd.definition("coupletesting",  getConcept("dca0a383-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("coupleResultsReceived", sdd.definition("coupleResultsReceived",  getConcept("2aa9f0c1-3f7e-49cd-86ee-baac0d2d5f2d")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("discordantResults",sdd.definition("discordantResults",  getConcept("dca0a383-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("corcodantPostive", sdd.definition("corcodantPostive",  getConcept("dca0a383-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("TBCase", sdd.definition("TBCase",  getConcept("dca0a383-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("referedForTBServices", sdd.definition("referedForTBServices",  getConcept("dca0a383-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("refferedTonrollment", sdd.definition("refferedTonrollment",  getConcept("dca0a383-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("RegNo", sdd.definition("RegNo",  getConcept("164985AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());





//
//		//start adding columns here
//		dsd.addColumn("Client No", sdd.definition("Client No",  getConcept("38460266-6bcd-47e8-844c-649d34323810")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
//		dsd.addColumn("Name of Client", new PreferredNameDataDefinition(), (String) null);
//		dsd.addColumn("Village+Parish", villageParish(), "onDate=${endDate}", new CalculationResultDataConverter());
//		dsd.addColumn("Phone Number", new PersonAttributeDataDefinition("Phone Number", phoneNumber), "", new PersonAttributeDataConverter());
//		dsd.addColumn("Age-10-19yrs", age(10,19), "onDate=${endDate}", new CalculationResultDataConverter());
//		dsd.addColumn("Age-20-24yrs", age(20,24), "onDate=${endDate}", new CalculationResultDataConverter());
//		dsd.addColumn("Age-25+yrs", age(25,200), "onDate=${endDate}", new CalculationResultDataConverter());
//		dsd.addColumn("ANC Visit", sdd.definition("ANC Visit",  getConcept("801b8959-4b2a-46c0-a28f-f7d3fc8b98bb")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
//		dsd.addColumn("Gravida", sdd.definition("Gravida",  getConcept("dcc39097-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
//		dsd.addColumn("Parity", sdd.definition("Parity",  getConcept("1053AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
//		dsd.addColumn("Gestational Age", sdd.definition("Gestational Age",  getConcept("dca0a383-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
//		dsd.addColumn("ANC1 Timing", sdd.definition("ANC1 Timing",  getConcept("3a862ab6-7601-4412-b626-d373c1d4a51e")), "onOrAfter=${startDate},onOrBefore=${endDate}", new Anc1TimingDataConverter());
//		dsd.addColumn("EDD", sdd.definition("EDD", getConcept("dcc033e5-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
//		dsd.addColumn("Weight", sdd.definition("Weight",  getConcept("5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
//		dsd.addColumn("Height", sdd.definition("Height",  getConcept("5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
//		dsd.addColumn("MUAC", sdd.definition("MUAC",  getConcept("5f86d19d-9546-4466-89c0-6f80c101191b")), "onOrAfter=${startDate},onOrBefore=${endDate}", new MUACDataConverter());
//		dsd.addColumn("INR NO", sdd.definition("INR NO",  getConcept("b644c29c-9bb0-447e-9f73-2ae89496a709")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
//		dsd.addColumn("BP", bloodPressure(), "onDate=${endDate}", new CalculationResultDataConverter());
//		dsd.addColumn("EMTCT codesW", sdd.definition("EMTCT codesW", getConcept("d5b0394c-424f-41db-bc2f-37180dcdbe74")), "onOrAfter=${startDate},onOrBefore=${endDate}", new EmctCodesDataConverter());
//		dsd.addColumn("EMTCT codesP", sdd.definition("EMTCT codesP", getConcept("62a37075-fc2a-4729-8950-b9fae9")), "onOrAfter=${startDate},onOrBefore=${endDate}", new EmctCodesDataConverter());
//		dsd.addColumn("Diagnosis", sdd.definition("Diagnosis", getConcept("1284AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
//		dsd.addColumn("WHO", sdd.definition("WHO", getConcept("dcdff274-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new WHODataConverter());
//		dsd.addColumn("CD4", whoCd4Vl("dcbcba2c-30ab-102d-86b0-7a5022ba4115", "159376AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), "onDate=${endDate}", new CalculationResultDataConverter());
//		dsd.addColumn("VL", whoCd4Vl("dc8d83e3-30ab-102d-86b0-7a5022ba4115", "0b434cfa-b11c-4d14-aaa2-9aed6ca2da88"), "onDate=${endDate}", new CalculationResultDataConverter());
//		dsd.addColumn("ARVs drugs", sdd.definition("ARVs drugs", getConcept("a615f932-26ee-449c-8e20-e50a15232763")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ARVsDataConverter());
//		dsd.addColumn("Pre-ART No", identifierDef, "");
//		dsd.addColumn("IYCF", sdd.definition("IYCF", getConcept("5d993591-9334-43d9-a208-11b10adfad85")), "onOrAfter=${startDate},onOrBefore=${endDate}", new IYCFDataConverter());
//		dsd.addColumn("MNC", sdd.definition("MNC", getConcept("af7dccfd-4692-4e16-bd74-5ac4045bb6bf")), "onOrAfter=${startDate},onOrBefore=${endDate}", new MNCDataConverter());
//		dsd.addColumn("TB Status", sdd.definition("TB Status", getConcept("dce02aa1-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
//		dsd.addColumn("Haemoglobin", sdd.definition("Haemoglobin", getConcept("dc548e89-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
//		dsd.addColumn("Syphilis testW", sdd.definition("Syphilis testW", getConcept("275a6f72-b8a4-4038-977a-727552f69cb8")), "onOrAfter=${startDate},onOrBefore=${endDate}", new SyphilisTestDataConverter());
//		dsd.addColumn("Syphilis testP", sdd.definition("Syphilis testP", getConcept("d8bc9915-ed4b-4df9-9458-72ca1bc2cd06")), "onOrAfter=${startDate},onOrBefore=${endDate}", new SyphilisTestDataConverter());
//		dsd.addColumn("FPC", sdd.definition("FPC", getConcept("0815c786-5994-49e4-aa07-28b662b0e428")), "onOrAfter=${startDate},onOrBefore=${endDate}", new FpcDataConverter());
//		dsd.addColumn("TT", sdd.definition("TT", getConcept("39217e3d-6a39-4679-bf56-f0954a7ffdb8")), "onOrAfter=${startDate},onOrBefore=${endDate}", new TetanusDataConverter());
//		dsd.addColumn("IPT/CTX", sdd.definition("IPT/CTX", getConcept("1da3cb98-59d8-4bfd-b0bb-c9c1bcd058c6")), "onOrAfter=${startDate},onOrBefore=${endDate}", new IptCtxDataConverter());
//		dsd.addColumn("Free LLIN", sdd.definition("Free LLIN", getConcept("3e7bb52c-e6ae-4a0b-bce0-3b36286e8658")), "onOrAfter=${startDate},onOrBefore=${endDate}", new FreeLlinDataConverter());
//		dsd.addColumn("Mebendazole", sdd.definition("Mebendazole", getConcept("9d6abbc4-707a-4ec7-a32a-4090b1c3af87")), "onOrAfter=${startDate},onOrBefore=${endDate}", new MebendazoleDataConverter());
//		dsd.addColumn("Iron given", ironGiven(), "onDate=${endDate}", new CalculationResultDataConverter());
//		dsd.addColumn("Folic acid given", folicAcidGiven(), "onDate=${endDate}", new CalculationResultDataConverter());
//		dsd.addColumn("Other treatments", sdd.definition("Other treatments", getConcept("2aa72406-436e-490d-8aa4-d5336148204f")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
//		dsd.addColumn("Referal In/Out", referal(), "onDate=${endDate}", new CalculationResultDataConverter());
//		dsd.addColumn("Risk Factor/Complications", sdd.definition("Risk Factor/Complications", getConcept("120186AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());

		return dsd;
	}

	@Override
	public String getVersion() {
		return "0.2.0.5";
	}
}
