/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.ugandaemrreports.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.GlobalPropertyParametersDatasetDefinition;
import org.openmrs.module.ugandaemrreports.library.Moh105IndicatorLibrary;
import org.openmrs.module.ugandaemrreports.reporting.library.dimension.CommonReportDimensionLibrary;
import org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 */
@Component
public class SetupMOH105Section1Report extends UgandaEMRDataExportManager {

    @Autowired
    private CommonReportDimensionLibrary dimensionLibrary;

    @Autowired
    private Moh105IndicatorLibrary indicatorLibrary;
    /**
     * @return the uuid for the report design for exporting to Excel
     */
    
    private static final String PARAMS = "startDate=${startDate},endDate=${endDate}";

    @Override
    public String getExcelDesignUuid() {
        return "1ef6634b-3467-470b-9db9-61283ecc539b";
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        return Arrays.asList(buildReportDesign(reportDefinition));
    }    
    
    /**
     * Build the report design for the specified report, this allows a user to override the report design by adding properties and other metadata to the report design
     *
     * @param reportDefinition
     * @return The report design
     */
    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "HMIS_105-1.xls");
    }

    @Override
    public String getUuid() {
        return "85b9a42b-d16b-4b3c-992f-adbb806f0b00";
    }

    @Override
    public String getName() {
        return "HMIS 105 - SECTION 1";
    }

    @Override
    public String getDescription() {
        return "Health Unit Outpatient Monthly Report, Section 1";
    }


    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        //connect the report definition to the dsd
        rd.addDataSetDefinition("S", Mapped.mapStraightThrough(settings()));
        rd.addDataSetDefinition("D", Mapped.mapStraightThrough(opdDiagnosis()));
        
        return rd;
    }

    protected DataSetDefinition opdDiagnosis() {
        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
        dsd.setParameters(getParameters());
        dsd.addDimension("age", ReportUtils.map(dimensionLibrary.standardAgeGroupsForOutPatient(), "effectiveDate=${endDate}"));
        dsd.addDimension("age1", ReportUtils.map(dimensionLibrary.drugUseAgeGroups(), "effectiveDate=${endDate}"));
        dsd.addDimension("gender", ReportUtils.map(dimensionLibrary.gender()));

        dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        dsd.addParameter(new Parameter("endDate", "End Date", Date.class));

        //start building the columns for the report
        addRowWithColumns(dsd, "1.3.1.1","1. Acute Flaccid Paralysis", indicatorLibrary.opdAcuteFlaccidParalysisDiagnosis());
        addRowWithColumns(dsd, "1.3.1.2","2. Animal Bites", indicatorLibrary.opdAnimalBitesDiagnosis());
        addRowWithColumns(dsd, "1.3.1.3","3. Cholera", indicatorLibrary.opdCholeraDiagnosis());
        addRowWithColumns(dsd, "1.3.1.4","4. Dysentery", indicatorLibrary.opdDysenteryDiagnosis());
        addRowWithColumns(dsd, "1.3.1.5","5. Guinea Worm", indicatorLibrary.opdGuineaWormDiagnosis());
        addRowWithColumns(dsd, "1.3.1.6T","6. Malaria -Total", indicatorLibrary.opdMalariaTotalDiagnosis());
        addRowWithColumns(dsd, "1.3.1.6C","6. Malaria -Confirmed", indicatorLibrary.opdMalariaConfirmedDiagnosis());
        addRowWithColumns(dsd, "1.3.1.7","7. Measles", indicatorLibrary.opdMeaslesDiagnosis());
        addRowWithColumns(dsd, "1.3.1.8","8. Bacterial Meningitis", indicatorLibrary.opdBacterialMeningitisDiagnosis());
        addRowWithColumns(dsd, "1.3.1.9","9. Neonatal tetanus", indicatorLibrary.opdNeonatalTetanusDiagnosis());
        addRowWithColumns(dsd, "1.3.1.10","10. Plague", indicatorLibrary.opdPlagueDiagnosis());
        addRowWithColumns(dsd, "1.3.1.11","11. Yellow Fever", indicatorLibrary.opdYellowFeverDiagnosis());
        addRowWithColumns(dsd, "1.3.1.12","12. Other Viral Hemorrhagic Fevers", indicatorLibrary.opdOtherViralHemorrhagicFeversDiagnosis());
        addRowWithColumns(dsd, "1.3.1.13","13. Severe Acute Respiratory Infection", indicatorLibrary.opdSevereAcuteRespiratoryInfectionDiagnosis());
        addRowWithColumns(dsd, "1.3.1.14","14. Adverse Events Following Immunization", indicatorLibrary.opdAdverseEventsFollowingImmunizationDiagnosis());
        addRowWithColumns(dsd, "1.3.1.15","15. Typhoid Fever", indicatorLibrary.opdTyphoidFeverDiagnosis());
        addRowWithColumns(dsd, "1.3.1.16","16. Presumptive MDR TB cases", indicatorLibrary.opdPresumptiveMdrTbCasesDiagnosis());
        addRowWithColumns(dsd, "1.3.1.INF","Other Emerging infectious Diseases", indicatorLibrary.opdOtherEmergingInfectiousDiseasesDiagnosis());
        addRowWithColumns(dsd, "1.3.2.17","17. Acute Diarrhoea", indicatorLibrary.opdAcuteDiarrhoeaDiagnosis());
        addRowWithColumns(dsd, "1.3.2.18","18. Persistent Diarrhoea", indicatorLibrary.opdPersistentDiarrhoeaDiagnosis());
        addRowWithColumns(dsd, "1.3.2.19","19. Urethral discharges", indicatorLibrary.opdUrethralDischargesDiagnosis());
        addRowWithColumns(dsd, "1.3.2.20","20. Genital ulcers", indicatorLibrary.opdGenitalUlcersDiagnosis());
        addRowWithColumns(dsd, "1.3.2.21","21. Sexually Transmitted Infection due to Sexual Gender Based Violence", indicatorLibrary.opdSexuallyTransmittedInfectionDueToSexualGenderBasedViolenceDiagnosis());
        addRowWithColumns(dsd, "1.3.2.22","22. Other Sexually Transmitted Infections", indicatorLibrary.opdOtherSexuallyTransmittedInfectionsDiagnosis());
        addRowWithColumns(dsd, "1.3.2.23","23. Urinary Tract Infections", indicatorLibrary.opdUrinaryTractInfectionsDiagnosis());
        addRowWithColumns(dsd, "1.3.2.24","24. Intestinal Worms", indicatorLibrary.opdIntestinalWormsDiagnosis());
        addRowWithColumns(dsd, "1.3.2.25","25. Hematological Meningitis", indicatorLibrary.opdHematologicalMeningitisDiagnosis());
        addRowWithColumns(dsd, "1.3.2.26","26. Other types of meningitis", indicatorLibrary.opdOtherTypesOfMeningitisDiagnosis());
        addRowWithColumns(dsd, "1.3.2.27","27. No pneumonia - Cough or cold", indicatorLibrary.opdNoPneumoniaCoughOrColdDiagnosis());
        addRowWithColumns(dsd, "1.3.2.28","28. Pneumonia", indicatorLibrary.opdPneumoniaDiagnosis());
        addRowWithColumns(dsd, "1.3.2.29","29. Skin Diseases", indicatorLibrary.opdSkinDiseasesDiagnosis());
        addRowWithColumns(dsd, "1.3.2.30","30. New TB cases diagnosed - Bacteriologically confirmed", indicatorLibrary.opdNewTbCasesDiagnosedBacteriologicallyConfirmedDiagnosis());
        addRowWithColumns(dsd, "1.3.2.128","128. New TB cases diagnosed - Clinically Diagnosed", indicatorLibrary.opdNewTbCasesDiagnosedClinicallyDiagnosedDiagnosis());
        addRowWithColumns(dsd, "1.3.2.129","129. New TB cases diagnosed - EPTB", indicatorLibrary.opdNewTbCasesDiagnosedEptbDiagnosis());
        addRowWithColumns(dsd, "1.3.2.31","31. Leprosy", indicatorLibrary.opdLeprosyDiagnosis());
        addRowWithColumns(dsd, "1.3.2.32","32. Tuberculosis MDR/XDR cases started on treatment", indicatorLibrary.tuberculosisMdrXdrCasesStartedOnTreatment());
        addRowWithColumns(dsd, "1.3.2.33","33. Tetanus", indicatorLibrary.opdTetanusDiagnosis());
        addRowWithColumns(dsd, "1.3.2.34","34. Sleeping sickness", indicatorLibrary.opdSleepingSicknessDiagnosis());
        addRowWithColumns(dsd, "1.3.2.35","35. Pelvic Inflammatory Disease", indicatorLibrary.opdPelvicInflammatoryDiseaseDiagnosis());
        addRowWithColumns(dsd, "1.3.2.36","36. Brucellosis", indicatorLibrary.opdBrucellosisDiagnosis());
        addRowWithColumns(dsd, "1.3.3.37","37. Neonatal Sepsis (0-7days)", indicatorLibrary.opdNeonatalSepsis07DaysDiagnosis());
        addRowWithColumns(dsd, "1.3.3.38","38. Neonatal Sepsis (8-28days)", indicatorLibrary.opdNeonatalSepsis828DaysDiagnosis());
        addRowWithColumns(dsd, "1.3.3.39","39. Neonatal Pneumonia", indicatorLibrary.opdNeonatalPneumoniaDiagnosis());
        addRowWithColumns(dsd, "1.3.3.40","40. Neonatal Meningitis", indicatorLibrary.opdNeonatalMeningitisDiagnosis());
        addRowWithColumns(dsd, "1.3.3.41","41. Neonatal Jaundice", indicatorLibrary.opdNeonatalJaundiceDiagnosis());
        addRowWithColumns(dsd, "1.3.3.42","42. Premature baby (as a condition for management)", indicatorLibrary.opdPrematureBabyAsAConditionForManagementDiagnosis());
        addRowWithColumns(dsd, "1.3.3.43","43. Other Neonatal Conditions", indicatorLibrary.opdOtherNeonatalConditionsDiagnosis());
        addRowWithColumns(dsd, "1.3.4.44","44. Sickle Cell Anaemia", indicatorLibrary.opdSickleCellAnaemiaDiagnosis());
        addRowWithColumns(dsd, "1.3.4.45","45. Other types of Anaemia", indicatorLibrary.opdOtherTypesOfAnaemiaDiagnosis());
        addRowWithColumns(dsd, "1.3.4.46","46. Gastro-Intestinal Disorders (non-Infective)", indicatorLibrary.opdGastroIntestinalDisordersNonInfectiveDiagnosis());
        addRowWithColumns(dsd, "1.3.4.47","47. Pain Requiring Palliative Care", indicatorLibrary.opdPainRequiringPalliativeCareDiagnosis());
        addRowWithColumns(dsd, "1.3.4.48","48. Dental Caries", indicatorLibrary.opdDentalCariesDiagnosis());
        addRowWithColumns(dsd, "1.3.4.49","49. Gingivitis", indicatorLibrary.opdGingivitisDiagnosis());
        addRowWithColumns(dsd, "1.3.4.50","50. HIV-Oral lesions", indicatorLibrary.opdHivOralLesionsDiagnosis());
        addRowWithColumns(dsd, "1.3.4.51","51. Oral Cancers", indicatorLibrary.opdOralCancersDiagnosis());
        addRowWithColumns(dsd, "1.3.4.52","52. Other Oral Conditions", indicatorLibrary.opdOtherOralConditionsDiagnosis());
        addRowWithColumns(dsd, "1.3.4.53","53. Otitis media", indicatorLibrary.opdOtitisMediaDiagnosis());
        addRowWithColumns(dsd, "1.3.4.54","54. Hearing loss", indicatorLibrary.opdHearingLossDiagnosis());
        addRowWithColumns(dsd, "1.3.4.55","55. Other ENT conditions", indicatorLibrary.opdOtherEntConditionsDiagnosis());
        addRowWithColumns(dsd, "1.3.4.56","56. Ophthalmia neonatorum", indicatorLibrary.opdOphthalmiaNeonatorumDiagnosis());
        addRowWithColumns(dsd, "1.3.4.57","57. Cataracts", indicatorLibrary.opdCataractsDiagnosis());
        addRowWithColumns(dsd, "1.3.4.58","58. Refractive errors", indicatorLibrary.opdRefractiveErrorsDiagnosis());
        addRowWithColumns(dsd, "1.3.4.59","59. Glaucoma", indicatorLibrary.opdGlaucomaDiagnosis());
        addRowWithColumns(dsd, "1.3.4.60","60. Trachoma", indicatorLibrary.opdTrachomaDiagnosis());
        addRowWithColumns(dsd, "1.3.4.61","61. Tumors", indicatorLibrary.opdTumorsDiagnosis());
        addRowWithColumns(dsd, "1.3.4.62","62. Blindness", indicatorLibrary.opdBlindnessDiagnosis());
        addRowWithColumns(dsd, "1.3.4.63","63. Diabetic Retinopathy", indicatorLibrary.opdDiabeticRetinopathyDiagnosis());
        addRowWithColumns(dsd, "1.3.4.64","64. Other eye conditions", indicatorLibrary.opdOtherEyeConditionsDiagnosis());
        addRowWithColumns(dsd, "1.3.4.65","65. Bipolar disorders", indicatorLibrary.opdBipolarDisordersDiagnosis());
        addRowWithColumns(dsd, "1.3.4.66","66. Depression", indicatorLibrary.opdDepressionDiagnosis());
        addRowWithColumns(dsd, "1.3.4.67","67. Epilepsy", indicatorLibrary.opdEpilepsyDiagnosis());
        addRowWithColumns(dsd, "1.3.4.68","68. Dementia", indicatorLibrary.opdDementiaDiagnosis());
        addRowWithColumns(dsd, "1.3.4.69","69. Childhood Mental Disorders", indicatorLibrary.opdChildhoodMentalDisordersDiagnosis());
        addRowWithColumns(dsd, "1.3.4.70","70. Schizophrenia", indicatorLibrary.opdSchizophreniaDiagnosis());
        addRowWithColumns(dsd, "1.3.4.71","71. HIV related psychosis", indicatorLibrary.opdHivRelatedPsychosisDiagnosis());
        addRowWithColumns(dsd, "1.3.4.72","72. Anxiety disorders", indicatorLibrary.opdAnxietyDisordersDiagnosis());
        addRowWithColumns(dsd, "1.3.4.73","73. Alcohol abuse", indicatorLibrary.opdAlcoholAbuseDiagnosis());
        addRowWithColumns(dsd, "1.3.4.74","74. Drug abuse", indicatorLibrary.opdDrugAbuseDiagnosis());
        addRowWithColumns(dsd, "1.3.4.75","75. Other Mental Health Conditions", indicatorLibrary.opdOtherMentalHealthConditionsDiagnosis());
        addRowWithColumns(dsd, "1.3.4.76","76. Asthma", indicatorLibrary.opdAsthmaDiagnosis());
        addRowWithColumns(dsd, "1.3.4.77","77. Chronic Obstructive Pulmonary Disease (COPD)", indicatorLibrary.opdChronicObstructivePulmonaryDiseaseCopdDiagnosis());
        addRowWithColumns(dsd, "1.3.4.78","78. Cancer Cervix", indicatorLibrary.opdCancerCervixDiagnosis());
        addRowWithColumns(dsd, "1.3.4.79","79. Cancer Prostate", indicatorLibrary.opdCancerProstateDiagnosis());
        addRowWithColumns(dsd, "1.3.4.80","80. Cancer Breast", indicatorLibrary.opdCancerBreastDiagnosis());
        addRowWithColumns(dsd, "1.3.4.81","81. Cancer Lung", indicatorLibrary.opdCancerLungDiagnosis());
        addRowWithColumns(dsd, "1.3.4.82","82. Cancer Liver", indicatorLibrary.opdCancerLiverDiagnosis());
        addRowWithColumns(dsd, "1.3.4.83","83. Cancer Colon", indicatorLibrary.opdCancerColonDiagnosis());
        addRowWithColumns(dsd, "1.3.4.84","84. Kaposis Sarcoma", indicatorLibrary.opdKaposisSarcomaDiagnosis());
        addRowWithColumns(dsd, "1.3.4.85","85. Cancer Others", indicatorLibrary.opdCancerOthersDiagnosis());
        addRowWithColumns(dsd, "1.3.4.86","86. Cardiovascular Accident", indicatorLibrary.opdCardiovascularAccidentDiagnosis());
        addRowWithColumns(dsd, "1.3.4.87","87. Hypertension", indicatorLibrary.opdHypertensionDiagnosis());
        addRowWithColumns(dsd, "1.3.4.88","88. Heart failure", indicatorLibrary.opdHeartFailureDiagnosis());
        addRowWithColumns(dsd, "1.3.4.89","89. Ischemic Heart Diseases", indicatorLibrary.opdIschemicHeartDiseasesDiagnosis());
        addRowWithColumns(dsd, "1.3.4.124","124. Rheumatic Heart Diseases", indicatorLibrary.opdRheumaticHeartDiseasesDiagnosis());
        addRowWithColumns(dsd, "1.3.4.90","90. Chronic Heart Diseases", indicatorLibrary.opdChronicHeartDiseasesDiagnosis());
        addRowWithColumns(dsd, "1.3.4.91","91. Other Cardiovascular Diseases", indicatorLibrary.opdOtherCardiovascularDiseasesDiagnosis());
        addRowWithColumns(dsd, "1.3.4.92","92. Diabetes mellitus", indicatorLibrary.opdDiabetesMellitusDiagnosis());
        addRowWithColumns(dsd, "1.3.4.93","93. Thyroid Disease", indicatorLibrary.opdThyroidDiseaseDiagnosis());
        addRowWithColumns(dsd, "1.3.4.94","94. Other Endocrine and Metabolic Diseases", indicatorLibrary.opdOtherEndocrineAndMetabolicDiseasesDiagnosis());
        addRowWithColumns(dsd, "1.3.4.95","95. Severe Acute Malnutrition with oedema", indicatorLibrary.opdSevereAcuteMalnutritionWithOedemaDiagnosis());
        addRowWithColumns(dsd, "1.3.4.123","123. Severe Acute Malnutrition Without oedema", indicatorLibrary.opdSevereAcuteMalnutritionWithoutOedemaDiagnosis());
        addRowWithColumns(dsd, "1.3.4.96","96. Mild Acute Malnutrition", indicatorLibrary.opdMildAcuteMalnutritionDiagnosis());
        addRowWithColumns(dsd, "1.3.4.97","97. Jaw injuries", indicatorLibrary.opdJawInjuriesDiagnosis());
        addRowWithColumns(dsd, "1.3.4.98","98. Injuries- Road traffic Accidents", indicatorLibrary.opdInjuriesRoadTrafficAccidentsDiagnosis());
        addRowWithColumns(dsd, "1.3.4.99","99. Injuries due to motorcycle(boda-boda)", indicatorLibrary.opdInjuriesDueToMotorcyclebodaBodaDiagnosis());
        addRowWithColumns(dsd, "1.3.4.100","100. Injuries due to Gender based violence", indicatorLibrary.opdInjuriesDueToGenderBasedViolenceDiagnosis());
        addRowWithColumns(dsd, "1.3.4.101","101. Injuries (Trauma due to other causes)", indicatorLibrary.opdInjuriesTraumaDueToOtherCausesDiagnosis());
        addRowWithColumns(dsd, "1.3.4.102","102. Animal bites Domestic", indicatorLibrary.opdAnimalBitesDomesticDiagnosis());
        addRowWithColumns(dsd, "1.3.4.125","125. Animal bites Wild", indicatorLibrary.opdAnimalBitesWildDiagnosis());
        addRowWithColumns(dsd, "1.3.4.126","126. Animal bites Insects", indicatorLibrary.opdAnimalBitesInsectsDiagnosis());
        addRowWithColumns(dsd, "1.3.4.127","127. Animal bites (Suspected Rabies)", indicatorLibrary.opdAnimalBitesSuspectedRabiesDiagnosis());
        addRowWithColumns(dsd, "1.3.4.103","103. Snake bites", indicatorLibrary.opdSnakeBitesDiagnosis());
        addRowWithColumns(dsd, "1.3.5.104","104. Tooth extractions", indicatorLibrary.opdToothExtractionsDiagnosis());
        addRowWithColumns(dsd, "1.3.5.105","105. Dental Fillings", indicatorLibrary.opdDentalFillingsDiagnosis());
        addRowWithColumns(dsd, "1.3.5.106","106. Other Minor Operations", indicatorLibrary.opdOtherMinorOperationsDiagnosis());
        addRowWithColumns(dsd, "1.3.6.107","107. Leishmaniasis", indicatorLibrary.opdLeishmaniasisDiagnosis());
        addRowWithColumns(dsd, "1.3.6.108","108. Lymphatic Filariasis (hydrocele)", indicatorLibrary.opdLymphaticFilariasisHydroceleDiagnosis());
        addRowWithColumns(dsd, "1.3.6.109","109. Lymphatic Filariasis (Lympoedema)", indicatorLibrary.opdLymphaticFilariasisLympoedemaDiagnosis());
        addRowWithColumns(dsd, "1.3.6.110","110. Urinary Schistosomiasis", indicatorLibrary.opdUrinarySchistosomiasisDiagnosis());
        addRowWithColumns(dsd, "1.3.6.111","111. Intestinal Schistosomiasis", indicatorLibrary.opdIntestinalSchistosomiasisDiagnosis());
        addRowWithColumns(dsd, "1.3.6.112","112. Onchocerciasis", indicatorLibrary.opdOnchocerciasisDiagnosis());
        addRowWithColumns(dsd, "1.3.7.113","113. Abortions due to Gender-Based Violence (GBV)", indicatorLibrary.opdAbortionsDueToGenderBasedViolenceGbvDiagnosis());
        addRowWithColumns(dsd, "1.3.7.114","114. Abortions due to other causes", indicatorLibrary.opdAbortionsDueToOtherCausesDiagnosis());
        addRowWithColumns(dsd, "1.3.7.115","115. Malaria in pregnancy", indicatorLibrary.opdMalariaInPregnancyDiagnosis());
        addRowWithColumns(dsd, "1.3.7.116","116. High blood pressure in pregnancy", indicatorLibrary.opdHighBloodPressureInPregnancyDiagnosis());
        addRowWithColumns(dsd, "1.3.7.117","117. Obstructed labour", indicatorLibrary.opdObstructedLabourDiagnosis());
        addRowWithColumns(dsd, "1.3.7.118","118. Puerperal sepsis", indicatorLibrary.opdPuerperalSepsisDiagnosis());
        addRowWithColumns(dsd, "1.3.7.119","119. Haemorrhage related to pregnancy", indicatorLibrary.opdHaemorrhageRelatedToPregnancyDiagnosis());
        addRowWithColumns(dsd, "1.3.8.120","120. Other diagnoses", indicatorLibrary.opdOtherDiagnosesDiagnosis());
        addRowWithColumns(dsd, "1.3.8.121","121. Deaths in OPD", indicatorLibrary.opdDeathsInOpdDiagnosis());
        addRowWithColumns(dsd, "1.3.8.122","122. All others", indicatorLibrary.opdAllOthersDiagnosis());
        addRowWithColumns(dsd, "ALL_DIAGNOSES","All diagnoses", indicatorLibrary.allDiagnoses());
        addRowWithDrugUseColumns(dsd, "1.3.9.R1","R1-Alcohol use", indicatorLibrary.alcoholUsers());
        addRowWithDrugUseColumns(dsd, "1.3.9.R2","R2-Tobacco use", indicatorLibrary.tobaccoUsers());
        addRowWithColumns(dsd, "1.3.10.B1","B1-Severely Underweight (BMI<16)", indicatorLibrary.severelyUnderweightBmi());
        addRowWithColumns(dsd, "1.3.10.B2","B2-Underweight (16<=BMI <18.5)", indicatorLibrary.underweightBmi());
        addRowWithColumns(dsd, "1.3.10.B3","B3-Normal (18.5<= BMI <=25)", indicatorLibrary.normalBmi());
        addRowWithColumns(dsd, "1.3.10.B4","B4-Over weight (25< BMI <=30", indicatorLibrary.overweightBmi());
        addRowWithColumns(dsd, "1.3.10.B5","B5-Obese ( BMI>30)", indicatorLibrary.obeseBmi());
        addRowWithColumns(dsd, "1.2T", "1.2 OUTPATIENT REFERRALS - Referrals to unit", indicatorLibrary.referralsToOPDUnit());
        addRowWithColumns(dsd, "1.2F", "1.2 OUTPATIENT REFERRALS - Referrals from unit", indicatorLibrary.referralFromOPDUnit());
        addRowWithColumns(dsd, "1.1N", "1.1 OUTPATIENT ATTENDANCE - New Attendance", indicatorLibrary.newOutPatientAttendance());
        addRowWithColumns(dsd, "1.1R", "1.1 OUTPATIENT ATTENDANCE - Re-attendance", indicatorLibrary.repeatOutPatientAttendance());
        addRowWithColumns(dsd, "1.1T", "1.1 OUTPATIENT ATTENDANCE - Total attendance", indicatorLibrary.totalOutPatientAttendance());

        return dsd;
    }
    
    public void addRowWithColumns(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortIndicator cohortIndicator) {
   	
    	addIndicator(dsd, key + "aM", label + " (Between 0 and 28 Days) Male", cohortIndicator, "gender=M|age=Between0And28Days");
        addIndicator(dsd, key + "aF", label + " (Between 0 and 28 Days) Female", cohortIndicator, "gender=F|age=Between0And28Days");
        addIndicator(dsd, key + "bM", label + " (Between 29 Days and 4 Years) Male", cohortIndicator, "gender=M|age=Between29DaysAnd4Yrs");
        addIndicator(dsd, key + "bF", label + " (Between 29 Days and 4 Years) Female", cohortIndicator, "gender=F|age=Between29DaysAnd4Yrs");
        addIndicator(dsd, key + "cM", label + " (Between 5 and 59 Years) Male", cohortIndicator, "gender=M|age=Between5And59Yrs");
        addIndicator(dsd, key + "cF", label + " (Between 5 and 59 Years) Female", cohortIndicator, "gender=F|age=Between5And59Yrs");
        addIndicator(dsd, key + "dM", label + " (>=60) Male", cohortIndicator, "gender=M|age=GreaterOrEqualTo60Yrs");
        addIndicator(dsd, key + "dF", label + " (>=60) Female", cohortIndicator, "gender=F|age=GreaterOrEqualTo60Yrs");
        addIndicator(dsd, key + "e", label + " (Total) ", cohortIndicator, "");
       
    }

    public void addRowWithDrugUseColumns(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortIndicator cohortIndicator) {
       	
        addIndicator(dsd, key + "aM", label + " (Between 10 Years and 19 Years) Male", cohortIndicator, "gender=M|age1=Between10And19Yrs");
        addIndicator(dsd, key + "aF", label + " (Between 10 Years and 19 Years) Female", cohortIndicator, "gender=F|age1=Between10And19Yrs");
        addIndicator(dsd, key + "bM", label + " (Between 20 Years and 24 Years) Male", cohortIndicator, "gender=M|age1=Between20And24Yrs");
        addIndicator(dsd, key + "bF", label + " (Between 20 Years and 24 Years) Female", cohortIndicator, "gender=F|age1=Between20And24Yrs");
        addIndicator(dsd, key + "cM", label + " (>=25) Male", cohortIndicator, "gender=M|age=GreaterOrEqualTo25Yrs");
        addIndicator(dsd, key + "cF", label + " (>=25) Female", cohortIndicator, "gender=F|age=GreaterOrEqualTo25Yrs");
        addIndicator(dsd, key + "d", label + " (Total) ", cohortIndicator, "");
       
    }
    
    public void addIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortIndicator cohortIndicator, String dimensionOptions) {
        dsd.addColumn(key, label, ReportUtils.map(cohortIndicator, PARAMS), dimensionOptions);
    }
        
    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(new Parameter("startDate", "Start Date", Date.class));
        l.add(new Parameter("endDate", "End Date", Date.class));
        return l;
    }
    
    protected DataSetDefinition settings() {
        GlobalPropertyParametersDatasetDefinition cst = new GlobalPropertyParametersDatasetDefinition();
        cst.setName("S");
        cst.setGp("ugandaemr.dhis2.organizationuuid");
        return cst;
    }    
}
