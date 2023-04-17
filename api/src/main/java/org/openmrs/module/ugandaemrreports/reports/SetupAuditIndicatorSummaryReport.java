package org.openmrs.module.ugandaemrreports.reports;


import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.ARTClinicCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.reporting.dataset.definition.SharedDataDefintion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Care and Treatment Audit Tool
 */
@Component
public class SetupAuditIndicatorSummaryReport extends UgandaEMRDataExportManager {

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
		return "2502025e-b04a-44c5-aa7e-26d178e4d002";
	}

	@Override
	public String getUuid() {
		return "d6aecd70-f997-45ab-b43d-cfb270c0b4e1";
	}

	@Override
	public String getName() {
		return "Indicator Summary Report ";
	}

	@Override
	public String getDescription() {
		return "Audit Indicators Summary";
	}

	@Override
	public List<Parameter> getParameters() {
		List<Parameter> l = new ArrayList<Parameter>();
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
		return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "AuditSummaryReport.xls");

	}

	@Override
	public ReportDefinition constructReportDefinition() {
		ReportDefinition rd = new ReportDefinition();

		rd.setUuid(getUuid());
		rd.setName(getName());
		rd.setDescription(getDescription());
		rd.setParameters(getParameters());

		CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();

		dsd.setParameters(getParameters());
		rd.addDataSetDefinition("TX", Mapped.mapStraightThrough(dsd));

		String activeQuery= "SELECT PATTIENT_NO from reporting_audit_tool_hiv where DAYS_LOST<=0";
		String activeMAQuery = "SELECT PATTIENT_NO from reporting_audit_tool_hiv where DAYS_LOST>=1 and DAYS_LOST <=7";
		String lostQuery = "SELECT PATTIENT_NO from reporting_audit_tool_hiv where DAYS_LOST>=8 and DAYS_LOST <=28";
		String LTFPQuery = "SELECT PATTIENT_NO from reporting_audit_tool_hiv where DAYS_LOST> 28 ";

		String indexTestingNotEligble = "select PATTIENT_NO from reporting_audit_tool_hiv where DAYS_LOST <=28 and CHILD_AGE IS NULL";
		String indexTestingEligble = "select PATTIENT_NO from reporting_audit_tool_hiv where DAYS_LOST <=28 and CHILD_AGE <> CHILD_KNOWN";
		String tested_children = "select PATTIENT_NO from reporting_audit_tool_hiv where DAYS_LOST <=28 and CHILD_AGE = CHILD_KNOWN and CHILD_AGE IS NOT NULL";

		String partnerTestingNotEligble = "select PATTIENT_NO from reporting_audit_tool_hiv where DAYS_LOST <=28 and PARTNER_AGE IS NULL";
		String partnerTestingEligble = "select PATTIENT_NO from reporting_audit_tool_hiv where DAYS_LOST <=28 and PARTNER_AGE <> PARTNER_KNOWN";
		String partner_tested = "select PATTIENT_NO from reporting_audit_tool_hiv where DAYS_LOST <=28 and PARTNER_AGE = PARTNER_KNOWN and PARTNER_AGE IS NOT NULL";

		String dtgTransition = "select PATTIENT_NO from reporting_audit_tool_hiv where DAYS_LOST <=28 and Current_regimen like '%DTG%'";

		String mmd = "select PATTIENT_NO from reporting_audit_tool_hiv where (Age <15 and Prescription_Duration >84) or (Age >=15 and Prescription_Duration >168)";
		String VL_eligible_for_sampling = "SELECT PATTIENT_NO from reporting_audit_tool_hiv where DAYS_LOST<=28 and TIMESTAMPDIFF(MONTH , Art_Start_Date, CURRENT_DATE) >=6";
		String updated_vl = "SELECT PATTIENT_NO from reporting_audit_tool_hiv where DAYS_LOST<=28 and (Age <=19 and TIMESTAMPDIFF(MONTH , VL_Date, CURRENT_DATE) <6) or (Age > 19 and TIMESTAMPDIFF(MONTH , VL_Date, CURRENT_DATE) <12)";

		String updated_date_vl ="SELECT PATTIENT_NO from reporting_audit_tool_hiv where DAYS_LOST<=28 and ((Age <=19 and TIMESTAMPDIFF(MONTH , VL_Date, CURRENT_DATE) <6) \n" +
				"    or (Age > 19 and TIMESTAMPDIFF(MONTH , VL_Date, CURRENT_DATE) <12) or (TIMESTAMPDIFF(MONTH,NEW_BLED_DATE,CURRENT_DATE)<3 and NEW_BLED_DATE is not NULL))";

		String vlSupressed = "select PATTIENT_NO from reporting_audit_tool_hiv where DAYS_LOST <=28 and VL_Quantitative >=0 and VL_Quantitative< 50";
		String vlLowViremia = "select PATTIENT_NO from reporting_audit_tool_hiv where DAYS_LOST <=28 and VL_Quantitative >=50 and VL_Quantitative< 999";
		String vlUnSuppressed = "select PATTIENT_NO from reporting_audit_tool_hiv where DAYS_LOST <=28 and VL_Quantitative > 999";

		String iacInitiated ="select PATTIENT_NO from reporting_audit_tool_hiv where DAYS_LOST <=28 and VL_Quantitative >999 and IAC >1";

		String hivdr_eligible= "select PATTIENT_NO from reporting_audit_tool_hiv where DAYS_LOST <=28 and VL_Quantitative >999 and HIVDR_date is not null";

		String sample_collected ="select PATTIENT_NO from reporting_audit_tool_hiv where DAYS_LOST <=28 and VL_Quantitative >999 and HIVDR_SAMPLE_COLLECTED is not null";
		String pss_eligible ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where Age <20 and DAYS_LOST<=28 ";
		String pss_received ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where Age <20 and DAYS_LOST<=28 and HEALTH_EDUC_DATE is not null";
		String fp_eligible ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where Age >11 and DAYS_LOST<=28 ";
		String fp_received ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where Age >10 and DAYS_LOST<=28 and Family_Planning is not null ";
		String tpt_completed ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and Last_TPT_Status ='Treatment complete'";
		String tpt_onINH ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and Last_TPT_Status ='CURRENTLY ON INH PROPHYLAXIS FOR TB'";
		String tpt_sideEffects ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and Last_TPT_Status ='TOXICITY OR SIDE-EFFECTS'";
		String tpt_defaulted ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and Last_TPT_Status ='Defaulted'";
		String tpt_neverInitiated ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and Last_TPT_Status ='NEVER'";
		String tb_screened ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and TB_Status is not null";
		String tb_treatment_completed ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and TB_Status ='TB Treatment Completed'";
		String tb_diagnosed_clinically ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and TB_Status ='TB Diagnosed - Clinically diagnosed'";
		String tb_diagnosed_LAM="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and TB_Status ='TB Diagnosed - TB LAM'";
		String tb_suspect ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and TB_Status ='Suspect TB - referred or sputum sent'";
		String tb_onTreatment ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and TB_Status ='Currently on TB treatment'";
		String tb_noSigns ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and TB_Status ='No signs or symptoms of TB'";
		String cacx_eligible ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and Age >=15 and Age <=49 and Gender='F'";
		String cacx_screened ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and Age >=15 and Age <=49 and Gender='F' and CACX_STATUS is not null";
		String hepb_screened ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and HEP_B_Status is not null";
		String hepb_neg ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and HEP_B_Status='NEGATIVE'";
		String hepb_pos ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and HEP_B_Status='POSITIVE'";
		String syphillis_screened ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and Sphillis_Status is not null";
		String syphillis_noSymptoms ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and Sphillis_Status='No clinical Symptoms and Signs'";
		String syphillis_nonReactive ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and Sphillis_Status='Non-reactive for syphilis'";
		String syphillis_Reactive ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and Sphillis_Status='Reactive for syphilis but not yet treated'";
		String syphillis_ReactiveAndTreatmentGiven ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and Sphillis_Status='Reactive for syphilis and given treatment'";
		String AhivDiseaseAssessed ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and Advanced_Disease is not null";

		String OVCEligible ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and Age <18";
		String OVCScreened ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and Age <18 and OVC_SCREENING is not null";

		String OVCEnrollement ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and Age <18 and OVC_ENROLLMENT is not null";
		String OVCEnrolled ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and Age <18 and OVC_ENROLLMENT ='Enrolled'";
		String had_pss_visit ="SELECT PATTIENT_NO FROM reporting_audit_tool_hiv where  DAYS_LOST<=28 and HEALTH_EDUC_DATE is not null";



		SqlCohortDefinition activeSqlCohortDefinition = new SqlCohortDefinition(activeQuery);
		SqlCohortDefinition activeMASqlCohortDefinition = new SqlCohortDefinition(activeMAQuery);
		SqlCohortDefinition LostSqlCohortDefinition = new SqlCohortDefinition(lostQuery);
		SqlCohortDefinition LTFPSqlCohortDefinition = new SqlCohortDefinition(LTFPQuery);
		SqlCohortDefinition indexTestingNotEligibleSqlCohortDefinition = new SqlCohortDefinition(indexTestingNotEligble);
		SqlCohortDefinition indexTestingEligibleSqlCohortDefinition = new SqlCohortDefinition(indexTestingEligble);
		SqlCohortDefinition testedChildrenSqlCohortDefinition = new SqlCohortDefinition(tested_children);

		SqlCohortDefinition onDTG = new SqlCohortDefinition(dtgTransition);

		SqlCohortDefinition partnerTestingNotEligibleCohortDefinition = new SqlCohortDefinition(partnerTestingNotEligble);
		SqlCohortDefinition partnerTestingEligibleCohortDefinition = new SqlCohortDefinition(partnerTestingEligble);
		SqlCohortDefinition partnerTestedCohortDefinition = new SqlCohortDefinition(partner_tested);

		SqlCohortDefinition mmdCohortDefinition = new SqlCohortDefinition(mmd);
		SqlCohortDefinition vlSamplingCohortDefinition = new SqlCohortDefinition(VL_eligible_for_sampling);
		SqlCohortDefinition vlSampleUpTodateCohortDefinition = new SqlCohortDefinition(updated_vl);
		SqlCohortDefinition vldateUpdated = new SqlCohortDefinition(updated_date_vl);

		SqlCohortDefinition vlSuppressedCohortDefinition = new SqlCohortDefinition(vlSupressed);
		SqlCohortDefinition vlLowViremiaCohortDefinition = new SqlCohortDefinition(vlLowViremia);
		SqlCohortDefinition vlUnsupressedCohortDefinition = new SqlCohortDefinition(vlUnSuppressed);
		SqlCohortDefinition iacInitiatedCohortDefinition = new SqlCohortDefinition(iacInitiated);
		SqlCohortDefinition hivdrELigibleCohortDefinition = new SqlCohortDefinition(hivdr_eligible);
		SqlCohortDefinition hivdrSampleCollectedCohortDefinition = new SqlCohortDefinition(sample_collected);
		SqlCohortDefinition pssEligibleCohortDefinition = new SqlCohortDefinition(pss_eligible);
		SqlCohortDefinition pssreceived = new SqlCohortDefinition(pss_received);
		SqlCohortDefinition familyPlanningEligible = new SqlCohortDefinition(fp_eligible);
		SqlCohortDefinition familyPlanningReceived = new SqlCohortDefinition(fp_received);
		SqlCohortDefinition completedTPT = new SqlCohortDefinition(tpt_completed);
		SqlCohortDefinition onINH = new SqlCohortDefinition(tpt_onINH);
		SqlCohortDefinition stoppedTPT = new SqlCohortDefinition(tpt_sideEffects);
		SqlCohortDefinition defaultedTPT = new SqlCohortDefinition(tpt_defaulted);
		SqlCohortDefinition neverInitiatedOnTPT = new SqlCohortDefinition(tpt_neverInitiated);

		SqlCohortDefinition screenForTb = new SqlCohortDefinition(tb_screened);
		SqlCohortDefinition noSignsForTB = new SqlCohortDefinition(tb_noSigns);
		SqlCohortDefinition TBSuspect = new SqlCohortDefinition(tb_suspect);
		SqlCohortDefinition TBOnTreatment = new SqlCohortDefinition(tb_onTreatment);
		SqlCohortDefinition TBClincallyDiagnosed = new SqlCohortDefinition(tb_diagnosed_clinically);
		SqlCohortDefinition TB_TBLAMDiagnosed = new SqlCohortDefinition(tb_diagnosed_LAM);
		SqlCohortDefinition TB_TreatmentCompleted = new SqlCohortDefinition(tb_treatment_completed);
		SqlCohortDefinition CacxEligible = new SqlCohortDefinition(cacx_eligible);
		SqlCohortDefinition CacxScreened = new SqlCohortDefinition(cacx_screened);
		SqlCohortDefinition HepBScreened = new SqlCohortDefinition(hepb_screened);
		SqlCohortDefinition HepBNeg = new SqlCohortDefinition(hepb_neg);
		SqlCohortDefinition HepBPos = new SqlCohortDefinition(hepb_pos);
		SqlCohortDefinition SyhpillisScreened = new SqlCohortDefinition(syphillis_screened);
		SqlCohortDefinition SyhpillisNoSymptoms = new SqlCohortDefinition(syphillis_noSymptoms);
		SqlCohortDefinition SyhpillisNonReactive = new SqlCohortDefinition(syphillis_nonReactive);
		SqlCohortDefinition SyhpillisReactive = new SqlCohortDefinition(syphillis_Reactive);
		SqlCohortDefinition SyhpillisTreatmentGiven = new SqlCohortDefinition(syphillis_ReactiveAndTreatmentGiven);
		SqlCohortDefinition AssessedForAHIVDisease= new SqlCohortDefinition(AhivDiseaseAssessed);
		SqlCohortDefinition OVC_Eligible= new SqlCohortDefinition(OVCEligible);
		SqlCohortDefinition OVC_Screened= new SqlCohortDefinition(OVCScreened);
		SqlCohortDefinition OVC_EnrollmentEligible= new SqlCohortDefinition(OVCEnrollement);
		SqlCohortDefinition OVC_Enrolled =  new SqlCohortDefinition(OVCEnrolled);
		SqlCohortDefinition pss_visit=  new SqlCohortDefinition(had_pss_visit);

		Helper.addIndicator(dsd,"LTFU","lost to followup ",LTFPSqlCohortDefinition,"");
		Helper.addIndicator(dsd,"TXCURR","TX Curr",activeSqlCohortDefinition,"");
		Helper.addIndicator(dsd,"TXML","TX ml",LostSqlCohortDefinition,"");
		Helper.addIndicator(dsd,"MA","MA",activeMASqlCohortDefinition,"");

		Helper.addIndicator(dsd,"NO_CHILD","NO CHILD",indexTestingNotEligibleSqlCohortDefinition,"");
		Helper.addIndicator(dsd,"TESTING_ELIGIBLE","TESTING ELIGIBLE",indexTestingEligibleSqlCohortDefinition,"");
		Helper.addIndicator(dsd,"TESTED","TESTED",testedChildrenSqlCohortDefinition,"");

		Helper.addIndicator(dsd,"NO_PARTNER","NO PARTNER",partnerTestingNotEligibleCohortDefinition,"");
		Helper.addIndicator(dsd,"PARTNER_ELIGIBLE","PARTNER ELIGIBLE",partnerTestingEligibleCohortDefinition,"");
		Helper.addIndicator(dsd,"PARTNER","PARTNER TESTED",partnerTestedCohortDefinition,"");


		Helper.addIndicator(dsd,"ON_DTG","on DTG",onDTG,"");
		Helper.addIndicator(dsd,"MMD","PARTNER TESTED",mmdCohortDefinition,"");
		Helper.addIndicator(dsd,"VL_SAMPLING","VL sampling",vlSamplingCohortDefinition,"");
		Helper.addIndicator(dsd,"VL_UPDATED","VL updated",vlSampleUpTodateCohortDefinition,"");
		Helper.addIndicator(dsd,"VL_SAMPLE_COLLECTED","VL date updated",vldateUpdated,"");
		Helper.addIndicator(dsd,"VL_SUPPRESSED","VL SUPPRESSED",vlSuppressedCohortDefinition,"");
		Helper.addIndicator(dsd,"VL_LOW","VL Low",vlLowViremiaCohortDefinition,"");
		Helper.addIndicator(dsd,"VL_UNSUPRESSED","VL Unsuppressed",vlUnsupressedCohortDefinition,"");
		Helper.addIndicator(dsd,"iac","iac initiated",iacInitiatedCohortDefinition,"");
		Helper.addIndicator(dsd,"hivdr_eligible","hivdr eligible",hivdrELigibleCohortDefinition,"");
		Helper.addIndicator(dsd,"hivdr_sample","hivdr sample collected",hivdrSampleCollectedCohortDefinition,"");
		Helper.addIndicator(dsd,"pss_eligible","pss eligible ",pssEligibleCohortDefinition,"");
		Helper.addIndicator(dsd,"pss_received","pss received ",pssreceived,"");
		Helper.addIndicator(dsd,"fp_eligible", "fp eligible ",familyPlanningEligible,"");
		Helper.addIndicator(dsd,"fp_received", "fp received",familyPlanningReceived,"");
		Helper.addIndicator(dsd,"tpt_complete", "tpt complete",completedTPT,"");
		Helper.addIndicator(dsd,"tpt_onINH", "tpt on inh",onINH,"");
		Helper.addIndicator(dsd,"tpt_stopped", "tpt stopped",stoppedTPT,"");
		Helper.addIndicator(dsd,"tpt_defaulted", "tpt defaulted",defaultedTPT,"");
		Helper.addIndicator(dsd,"tpt_never", "never initiated",neverInitiatedOnTPT,"");
		Helper.addIndicator(dsd,"screenForTb", "screenForTb",screenForTb,"");
		Helper.addIndicator(dsd,"noSignsForTB", "noSignsForTB",noSignsForTB,"");
		Helper.addIndicator(dsd,"TBSuspect", "TBSuspect",TBSuspect,"");
		Helper.addIndicator(dsd,"TB_TreatmentCompleted", "TB_TreatmentCompleted",TB_TreatmentCompleted,"");
		Helper.addIndicator(dsd,"TBOnTreatment", "TBOnTreatment",TBOnTreatment,"");
		Helper.addIndicator(dsd,"TBClincallyDiagnosed", "TBClincallyDiagnosed",TBClincallyDiagnosed,"");
		Helper.addIndicator(dsd,"TB_TBLAMDiagnosed", "TB_TBLAMDiagnosed",TB_TBLAMDiagnosed,"");
		Helper.addIndicator(dsd,"CacxEligible", "CacxEligible",CacxEligible,"");
		Helper.addIndicator(dsd,"CacxScreened", "CacxScreened",CacxScreened,"");
		Helper.addIndicator(dsd,"HepBScreened", "HepBScreened",HepBScreened,"");
		Helper.addIndicator(dsd,"HepBNeg", "HepBNeg",HepBNeg,"");
		Helper.addIndicator(dsd,"HepBPos", "HepBPos",HepBPos,"");
		Helper.addIndicator(dsd,"SyhpillisScreened", "SyhpillisScreened",SyhpillisScreened,"");
		Helper.addIndicator(dsd,"SyhpillisNoSymptoms", "SyhpillisNoSymptoms",SyhpillisNoSymptoms,"");
		Helper.addIndicator(dsd,"SyhpillisNonReactive", "SyhpillisNonReactive",SyhpillisNonReactive,"");
		Helper.addIndicator(dsd,"SyhpillisReactive", "SyhpillisReactive",SyhpillisReactive,"");
		Helper.addIndicator(dsd,"SyhpillisTreatmentGiven", "SyhpillisTreatmentGiven",SyhpillisTreatmentGiven,"");
		Helper.addIndicator(dsd,"AssessedForAHIVDisease", "AssessedForAHIVDisease",AssessedForAHIVDisease,"");
		Helper.addIndicator(dsd,"OVC_Eligible", "OVC_Eligible",OVC_Eligible,"");
		Helper.addIndicator(dsd,"OVC_Screened", "OVC_Screened",OVC_Screened,"");
		Helper.addIndicator(dsd,"OVC_EnrollmentEligible", "OVC_EnrollmentEligible",OVC_EnrollmentEligible,"");
		Helper.addIndicator(dsd,"OVC_EnrollmentEligible", "OVC_EnrollmentEligible",OVC_EnrollmentEligible,"");
		Helper.addIndicator(dsd,"OVC_Enrolled", "OVC_Enrolled",OVC_Enrolled,"");
		Helper.addIndicator(dsd,"pss_visit", "pss_visit",pss_visit,"");


		return rd;
	}

	@Override
	public String getVersion() {
		return "0.2.3";
	}
}
