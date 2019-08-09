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
import org.openmrs.module.reporting.data.person.definition.AgeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.data.converter.ObsDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.PersonAttributeDataConverter;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.reporting.dataset.definition.SharedDataDefintion;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * HTS Client card export Report
 * */

@Component
public class SetUpHTSClientCardDataExportReport2019 extends UgandaEMRDataExportManager {


	@Autowired
	private DataFactory df;

	@Autowired
	SharedDataDefintion sdd;

	@Autowired
	private BuiltInPatientDataLibrary builtInPatientData;
	
	@Override
	public String getDescription() {
		return "Data Export for 2019 Version of the HTS Client Card" ;
	}
	
	@Override
	public String getName() {
		return "HTS Client Card Data Export 2019";
	}
	
	@Override
	public String getUuid() {
		return "96e0926d-1606-4de6-943f-cb036bdc15ad";
	}
	
	@Override
	public String getVersion() {
		return "3.0.0";
	}
	
	/**
     * @return the uuid for the report design for exporting to Excel
     */
	@Override
	public String getExcelDesignUuid() {
		return "bba9f107-467c-4fa7-8339-b246c44107b2";
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


	@Override
	public ReportDefinition constructReportDefinition() {

		ReportDefinition rd = new ReportDefinition();
		rd.setUuid(getUuid());
		rd.setName(getName());
		rd.setDescription(getDescription());
		rd.setParameters(getParameters());
		rd.addDataSetDefinition("HTSCARDDATAEXPORT", Mapped.mapStraightThrough(dataSetDefinition()));
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



		
		dsd.addColumn("Visit Date", df.getHTSVisitDate(), (String)null);
		dsd.addColumn("NIN", identifierDefNIN, "");
		dsd.addColumn("Serial No.", sdd.definition("serialNo",  getConcept("1646AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("District",df.getPreferredAddress("countyDistrict") ,(String)null);
		dsd.addColumn("Sub County",df.getPreferredAddress("address3") ,(String)null);
		dsd.addColumn("Parish",df.getPreferredAddress("address4") ,(String)null);
		dsd.addColumn("Village",df.getPreferredAddress("address5") ,(String)null);
		dsd.addColumn("Family Name", builtInPatientData.getPreferredFamilyName(),(String)null);
		dsd.addColumn("Given Name", builtInPatientData.getPreferredGivenName(),(String)null);
		dsd.addColumn("Age", new AgeDataDefinition(), "", new AgeConverter("{y}"));
		dsd.addColumn("Sex", new GenderDataDefinition(), (String) null);
		dsd.addColumn("Marital Status", new PersonAttributeDataDefinition("MaritalStatus", maritalStatus), "", new PersonAttributeDataConverter());
		dsd.addColumn("PhoneNumber", new PersonAttributeDataDefinition("Phone Number", phoneNumber), "", new PersonAttributeDataConverter());
		dsd.addColumn("Accompanied By", sdd.definition("accompanied by",  getConcept("dc911cc1-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("HTS Delivery Model", sdd.definition("htsModel",  getConcept("46648b1d-b099-433b-8f9c-3815ff1e0a0f")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("HTS Approach", sdd.definition("htsApproach",  getConcept("ff820a28-1adf-4530-bf27-537bfa9ce0b2")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Health Facility Entry Point", sdd.definition("entrypoint",  getConcept("720a1e85-ea1c-4f7b-a31e-cb896978df79")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Community Testing Entry Point", sdd.definition("testingPoints",  getConcept("4f4e6d1d-4343-42cc-ba47-2319b8a84369")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Reason For Testing", sdd.definition("reason",  getConcept("2afe1128-c3f6-4b35-b119-d17b9b9958ed")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Special Category", sdd.definition("special",  getConcept("927563c5-cb91-4536-b23c-563a72d3f829")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Testing For HIV the First Time",sdd.definition("firstTime",  getConcept("2766c090-c057-44f2-98f0-691b6d0336dc")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Last HIV Test Date",sdd.definition("lastHIVTestDate",  getConcept("34c917f0-356b-40d0-b3d1-cf609517b5fc")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Previous Test Results",sdd.definition("previousTestResults",  getConcept("49ba801d-b6ff-47cd-8d29-e0ac8649cb7d")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("No. of Times Tested in Last 12 Months",sdd.definition("timesTested",  getConcept("8037192e-8f0c-4af3-ad8d-ccd1dd6880ba")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("No. of Sexual Partners Last 12 Months",sdd.definition("noOfSexualPartners",  getConcept("f1a6ede9-052e-4707-9cd8-a77fdeb2a02b")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Partner Tested Before",sdd.definition("Partner Tested Before",  getConcept("adc0b1a1-39cf-412b-9ab0-28ec0f731220")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Most Recent Results For Partner",sdd.definition("PartnerResults",  getConcept("ee802cf2-295b-4297-b53c-205f794294a5")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Pre-test Conselling Done",sdd.definition("pre-testConseling",  getConcept("193039f1-c378-4d81-bb72-653b66c69914")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Counseled As", sdd.definition("counseled as",  getConcept("b92b1777-4356-49b2-9c83-a799680dc7d4")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("HIV Test Results", sdd.definition("HIVresults",  getConcept("3d292447-d7df-417f-8a71-e53e869ec89d")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Syphillis Duo Results",sdd.definition("syphillis duo results",  getConcept("16091701-69b8-4bc7-82b3-b1726cf5a5df")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Consented For Recency Testing", sdd.definition("consent",  getConcept("0698a45b-771c-4d11-84ff-095598c8883c")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Recency Results", sdd.definition("recency Results",  getConcept("141520BBBBBBBBBBBBBBBBBBBBBBBBBBBBBB")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Results Received As Individual", sdd.definition("individualResultsRecived",  getConcept("3437ae80-bcc5-41e2-887e-d56999a1b467")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Results Received as Couple", sdd.definition("coupleResultsReceived",  getConcept("2aa9f0c1-3f7e-49cd-86ee-baac0d2d5f2d")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Client Has Presumptive TB", sdd.definition("TBCase",  getConcept("b80f04a4-1559-42fd-8923-f8a6d2456a04")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Presumptive TB Case referred For TB Services", sdd.definition("referedForTBServices",  getConcept("c5da115d-f6a3-4d13-b182-c2e982a3a796")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Prevention Services Received", sdd.definition("preventionServices",  getConcept("73686a14-b55c-4b10-916d-fda2046b803f")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Client referred to HIV care", sdd.definition("refferedTonrollment",  getConcept("3d620422-0641-412e-ab31-5e45b98bc459")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
		dsd.addColumn("Place of referral", sdd.definition("referralPlace",  getConcept("dce015bb-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());

		return dsd;
	}
}
