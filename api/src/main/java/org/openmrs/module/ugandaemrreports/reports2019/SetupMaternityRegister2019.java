package org.openmrs.module.ugandaemrreports.reports2019;

import org.openmrs.Concept;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.data.converter.*;
import org.openmrs.module.ugandaemrreports.definition.data.definition.CalculationDataDefinition;
import org.openmrs.module.ugandaemrreports.library.BasePatientDataLibrary;
import org.openmrs.module.ugandaemrreports.library.Cohorts;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.reporting.calculation.MaternityEncounterDateCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.ProviderNameCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.AgeLimitCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.PersonAddressCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.WhoCd4VLCalculation;
import org.openmrs.module.ugandaemrreports.reporting.dataset.definition.SharedDataDefintion;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reports.UgandaEMRDataExportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Integrated Maternity Register Report
 */

@Component
public class SetupMaternityRegister2019 extends UgandaEMRDataExportManager {


	@Autowired
	private DataFactory df;
	@Autowired
	SharedDataDefintion sdd;

	@Autowired
	BasePatientDataLibrary basePatientDataLibrary;




	@Override
	public String getExcelDesignUuid() {
		return "28ee759f-4da7-46f6-8fff-7e1ad795660e ";
	}


	@Override
	public String getDescription() {
		return "Integrated Maternity Register 2019";
	}

	@Override
	public String getName() {
		return "Integrated Maternity Register 2019";
	}

	@Override
	public String getUuid() {
		return "694cb788-c3d1-406a-be2d-65e322f71205";
	}

	@Override
	public String getVersion() {
		return "1.8";
	}

	/**
	 * @return the uuid for the report design for exporting to Excel
	 */

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
		l.add(buildExcel(reportDefinition));
		l.add(buildReportDesign(reportDefinition));
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


	public ReportDesign buildExcel(ReportDefinition reportDefinition) {
		ReportDesign rd = createExcelTemplateDesign("8d271abd-e55d-4e5c-a9a9-495b6115fec4", reportDefinition, "MaternityRegister2019.xls");

		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:8-12,dataset:Maternity");
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
		rd.addDataSetDefinition("Maternity", Mapped.mapStraightThrough(dataSetDefinition()));
		return rd;
	}



	private DataSetDefinition dataSetDefinition() {
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		dsd.setName("Maternity");
		dsd.addParameters(getParameters());
		dsd.addRowFilter(Cohorts.genderAndHasAncEncounter(true, false, "a9f11592-22e7-45fc-904d-dfe24cb1fc67"), "startDate=${startDate},endDate=${endDate}");



		dsd.addColumn("DOA", getEncounterDate(), "onDate=${endDate}", new CalculationResultDataConverter());
		dsd.addColumn("Admission Number",sdd.definition("Admission Number",sdd.getConcept("1646AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("ANC and Ref No", sdd.definition("ANC and Ref No", sdd.getConcept("c7231d96-34d8-4bf7-a509-c810f75e3329")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("IPD No",sdd.definition("IPD No",sdd.getConcept("1646AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("NIN", sdd.getNationalIDNumber(), "");
		dsd.addColumn("Client Name", new PreferredNameDataDefinition(), (String) null);
		dsd.addColumn("Village", basePatientDataLibrary.getVillage(),(String)null);
		dsd.addColumn("Parish", basePatientDataLibrary.getParish(),(String)null);
		dsd.addColumn("County", basePatientDataLibrary.getCounty(),(String)null);
		dsd.addColumn("District", basePatientDataLibrary.getDistrict(),(String)null);
		dsd.addColumn("Nationality", new PersonAttributeDataDefinition("Nationality", sdd.getPatientNationality()), "", new NationalityPersonalAttributeDataConverter());
		dsd.addColumn("Phone No", new PersonAttributeDataDefinition("Phone Number", sdd.getPhoneNumber()), "", new PersonAttributeDataConverter());
		dsd.addColumn("Age", sdd.getAgeDataDefinition(10, 200), "onDate=${endDate}", new CalculationResultDataConverter());
		dsd.addColumn("Gravida", sdd.definition("Gravida", sdd.getConcept("dcc39097-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Parity", sdd.definition("Parity", sdd.getConcept("1053AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Weeks of Gestation", sdd.definition("Weeks of Gestation", sdd.getConcept("0b995cb8-7d0d-46c0-bd1a-bd322387c870")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Preterm", sdd.definition("Preterm", sdd.getConcept("161033AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Reason for Admission", sdd.definition("Reason for Admission", sdd.getConcept("03967f0d-70a7-4c65-b2d8-4b3ad6b2a3d8")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("WHO Clinical Stage", sdd.definition("WHO Clinical Stage", sdd.getConcept("dcdff274-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new WHODataConverter());
		dsd.addColumn("CD4", sdd.getWHOCD4ViralLoadCalculation("dcbcba2c-30ab-102d-86b0-7a5022ba4115", "159376AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), "onDate=${endDate}", new CalculationResultDataConverter());
		dsd.addColumn("Viral Load", sdd.getWHOCD4ViralLoadCalculation("dc8d83e3-30ab-102d-86b0-7a5022ba4115", "0b434cfa-b11c-4d14-aaa2-9aed6ca2da88"), "onDate=${endDate}", new CalculationResultDataConverter());
		dsd.addColumn("eMTCT W", sdd.definition("EMTCT codesW", sdd.getConcept("d5b0394c-424f-41db-bc2f-37180dcdbe74")), "onOrAfter=${startDate},onOrBefore=${endDate}", new EmctCodesDataConverter());
		dsd.addColumn("eMTCT P", sdd.definition("EMTCT codesP", sdd.getConcept("62a37075-fc2a-4729-8950-b9fae9b22cfb")), "onOrAfter=${startDate},onOrBefore=${endDate}", new EmctCodesDataConverter());
		dsd.addColumn("ART Number", sdd.definition("ART Number", sdd.getConcept("105ef9de-ad90-4c08-bcd5-ab48f74f6287")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("CTX", sdd.definition("CTX", sdd.getConcept("791cc131-8f5f-4795-a1bc-196241a007b7")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("SyphilisW", sdd.definition("SyphilisW", sdd.getConcept("791cc131-8f5f-4795-a1bc-196241a007b7")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Diagnosis", sdd.definition("Diagnosis", sdd.getConcept("1284AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new FinalDiagnosisDataConverter());
		dsd.addColumn("Mode of Delivery", sdd.definition("Mode of Delivery", sdd.getConcept("dcc3ac63-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Date of Delivery", sdd.definition("Date Of Deliverly", sdd.getConcept("5599AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new Anc1TimingDataConverter());
		dsd.addColumn("Time of Delivery", sdd.definition("Time of Delivery", sdd.getConcept("f9573837-6a9b-4f2f-9298-aecf27b15fb3")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Live Birth", sdd.definition("Live Birth", sdd.getConcept("f9573837-6a9b-4f2f-9298-aecf27b15fb3")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Management of 3rd stage labour", sdd.definition("Management of 3rd stage labour", sdd.getConcept("425458af-d1f7-40e4-a672-7d9e38aedb3c")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ManagementStageLabourConverter());
		dsd.addColumn("Complications", sdd.definition("Complications", sdd.getConcept("425458af-d1f7-40e4-a672-7d9e38aedb3c")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ManagementStageLabourConverter());
		dsd.addColumn("Other Treatment", sdd.definition("Other Treatment", sdd.getConcept("0aea6828-2328-4038-9cd9-680dbdf29790")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("First Minute", sdd.definition("First Minute", sdd.getConcept("056ee92e-3104-4529-8f83-70580e0f4501")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Fifth Minute", sdd.definition("Fifth Minute", sdd.getConcept("015ab92b-c5de-40cc-903d-21612bf4d9ab")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Sex", sdd.definition("Sex", sdd.getConcept("1587AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new SexofBabyDataConverter());
		dsd.addColumn("Breathing At Birth", sdd.definition("Breathing At Birth", sdd.getConcept("dc958e5c-ab9b-4c0c-b02d-d136b7505754")), "onOrAfter=${startDate},onOrBefore=${endDate}", new BreathingStatusAtBirthDataConverter());
		dsd.addColumn("Skin Contact", sdd.definition("Skin Contact", sdd.getConcept("be4b37fc-4197-4a4e-913d-0d8f3babdffd")), "onOrAfter=${startDate},onOrBefore=${endDate}", new SkinContactDataConverter());
		dsd.addColumn("Source of Warmth", sdd.definition("Source of Warmth", sdd.getConcept("921aed8f-bfc4-481d-a7cb-70a91a3cc733")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Breastfed", sdd.definition("Breastfed", sdd.getConcept("a4063d62-a936-4a26-9c1c-a0fb279a71b1")), "onOrAfter=${startDate},onOrBefore=${endDate}", new BreastFeedingDataConverter());
		dsd.addColumn("Medication", sdd.definition("Medication", sdd.getConcept("673d99cc-79a9-45a6-ba84-ecd3e9e793ef")), "onOrAfter=${startDate},onOrBefore=${endDate}", new RoutineMedicationDataConverter());
		dsd.addColumn("Baby Weight", sdd.definition("Baby Weight", sdd.getConcept("dcce847a-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Risk Status", sdd.definition("Risk Status", sdd.getConcept("a6037516-7c28-48ac-83c4-98ab4a032fa3")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("NVP Given", sdd.definition("NVP Given", sdd.getConcept("1e4dbd48-e261-417c-a360-831c99982c56")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Duration", sdd.definition("Duration", sdd.getConcept("3897f06e-39db-4e52-aafe-5b651f95d6b7")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());

		dsd.addColumn("PolioAndBCG", sdd.definition("PolioAndBCG", sdd.getConcept("dc918618-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter ());
		dsd.addColumn("Family Planning Method", sdd.definition("Family Planning Method", sdd.getConcept("dc7620b3-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new FamilyPlanningmethodDataConverter());
		dsd.addColumn("Treatment offered", sdd.definition("Treatment offered", sdd.getConcept("0f0da305-90ca-4b5f-87a4-1d284d01197a")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Final Diagnosis", sdd.definition("Final Diagnosis", sdd.getConcept("ed9f9b24-b356-4a42-8356-426c74d0aaf9")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Delivered By", sdd.getNameofProvideratDelivery(), "onDate=${endDate}", new CalculationResultDataConverter());
		dsd.addColumn("Transefered By", sdd.getNameofProvideratDelivery(), "onDate=${endDate}", new CalculationResultDataConverter());
		dsd.addColumn("Bleeding", sdd.definition("Bleeding", sdd.getConcept("f0d21d70-986e-43ed-b72b-98c693c15f84")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("PNC at 6 Baby", sdd.definition("PNC at 6 Baby", sdd.getConcept("93ca1215-5346-4fde-8905-84e930d9f1c1")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("LLINs Given", sdd.definition("LLINs Given", sdd.getConcept("165025AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Condition of Baby", sdd.definition("Condition of Baby", sdd.getConcept("a5638850-0cb4-4ce8-8e87-96fc073de25d")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Final DiagnosisM", sdd.definition("Final DiagnosisM", sdd.getConcept("29253d22-531f-42c2-a4e9-a597d4a9308b")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("IYCF", sdd.definition("IYCF", sdd.getConcept("5d993591-9334-43d9-a208-11b10adfad85")), "onOrAfter=${startDate},onOrBefore=${endDate}", new IYCFDataConverter());
		dsd.addColumn("IYCF Option", sdd.definition("Infant Feeding Option", sdd.getConcept("dc9a00a2-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new InfantfeedingOptionDataConverter());
		dsd.addColumn("Counselling at Discharge", sdd.definition("Counselling at Discharge", sdd.getConcept("3095d500-4a3e-4cf3-ab96-6f406e0d371d")), "onOrAfter=${startDate},onOrBefore=${endDate}", new CounsellingAtdisChargeDataConverter());
		dsd.addColumn("Condition of Mother At Discharge", sdd.definition("Condition of Mother At Discharge", sdd.getConcept("e87431db-b49e-4ab6-93ee-a3bd6c616a94")), "onOrAfter=${startDate},onOrBefore=${endDate}", new CounsellingAtdisChargeDataConverter());
		dsd.addColumn("Date of Discharge", sdd.definition("Date of Discharge", sdd.getConcept("ff31a419-0eb1-45fc-920a-77b4a3481e00")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Name of Person Discharging", sdd.getNameofProvideratDelivery(), "onDate=${endDate}", new CalculationResultDataConverter());

		dsd.addColumn("Weight", sdd.definition("Weight", sdd.getConcept("5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Height", sdd.definition("Height", sdd.getConcept("5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("MUAC", sdd.definition("MUAC", sdd.getConcept("5f86d19d-9546-4466-89c0-6f80c101191b")), "onOrAfter=${startDate},onOrBefore=${endDate}", new MUACDataConverter());


		dsd.addColumn("ARVs drugs", sdd.definition("ARVs drugs", sdd.getConcept("a615f932-26ee-449c-8e20-e50a15232763")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ARVsDataConverter());
		dsd.addColumn("Vit A", sdd.definition("Vit A", sdd.getConcept("88ec2c8b-eb7b-4595-8612-1871568507a5")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("INR", sdd.definition("INR", sdd.getConcept("b644c29c-9bb0-447e-9f73-2ae89496a709")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("PNC at 6 Baby", sdd.definition("PNC at 6 Baby", sdd.getConcept("93ca1215-5346-4fde-8905-84e930d9f1c1")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Delivery", sdd.definition("Delivery", sdd.getConcept("161033AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new DeliveryDataConverter());


		return dsd;
	}
	public DataDefinition getEncounterDate() {
		CalculationDataDefinition cd = new CalculationDataDefinition("DOA", new MaternityEncounterDateCalculation());
		cd.addParameter(new Parameter("onDate", "On Date", Date.class));
		return cd;
	}

}

