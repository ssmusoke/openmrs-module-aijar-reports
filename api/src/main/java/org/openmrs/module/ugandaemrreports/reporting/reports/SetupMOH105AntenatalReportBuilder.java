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
package org.openmrs.module.ugandaemrreports.reporting.reports;

import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.Moh105IndicatorLibrary;
import org.openmrs.module.ugandaemrreports.reporting.library.dimension.CommonReportDimensionLibrary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reporting.utils.ColumnParameters;
import org.openmrs.module.ugandaemrreports.reporting.utils.EmrReportingUtils;
import org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils;
import org.openmrs.module.ugandaemrreports.reports.UgandaEMRDataExportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Nicholas Ingosi on 5/23/17.
 */
@Component
public class SetupMOH105AntenatalReportBuilder extends UgandaEMRDataExportManager {

    @Autowired
    private CommonReportDimensionLibrary dimensionLibrary;

    @Autowired
    private Moh105IndicatorLibrary indicatorLibrary;
    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "57616c8e-3f92-11e7-aeaa-507b9dc4c741";
    }

    /**
     * Build the report design for the specified report, this allows a user to override the report design by adding properties and other metadata to the report design
     *
     * @param reportDefinition
     * @return The report design
     */
    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "105AntenatalAReport.xls");
    }

    @Override
    public String getUuid() {
        return "66c0d746-3f92-11e7-9b20-507b9dc4c741";
    }

    @Override
    public String getName() {
        return "HMIS 105 MCH - ANTENATAL";
    }

    @Override
    public String getDescription() {
        return "Health Unit Outpatient Monthly Report, Maternal and Child Health 2.1 Antenatal";
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
        dsd.addDimension("age", ReportUtils.map(dimensionLibrary.standardAgeGroupsForAnc(), "onDate=${endDate}"));
        dsd.addDimension("gender", ReportUtils.map(dimensionLibrary.gender()));

        //define columns
        ColumnParameters female10To19 = new ColumnParameters("f10to19", "10-19", "gender=F|age=<11");
        ColumnParameters female20To24 = new ColumnParameters("f20to24", "20-24", "gender=F|age=<21");
        ColumnParameters female25Plus = new ColumnParameters("f25plus", ">=25", "gender=F|age=25+");
        ColumnParameters femaleTotals = new ColumnParameters("fTotals", "Total", "");

        //buils list of columns
        List<ColumnParameters> allColumns = Arrays.asList(female10To19, female20To24, female25Plus, femaleTotals);
        List<ColumnParameters> noTotalsColumns = Arrays.asList(female10To19, female20To24, female25Plus);

        String params = "startDate=${startDate},endDate=${endDate}";
        //start building the columns for the report
        EmrReportingUtils.addRow(dsd, "A1", "A1: ANC 1st Visit for women", ReportUtils.map(indicatorLibrary.anc1stVisit(), params), allColumns, Arrays.asList("01","02","03","04"));
        EmrReportingUtils.addRow(dsd, "A2", "A2: ANC 4th Visit for women", ReportUtils.map(indicatorLibrary.anc4thVisit(), params), noTotalsColumns, Arrays.asList("01","02","03"));
        dsd.addColumn("A3", "A3: ANC 4+ visits for Women", ReportUtils.map(indicatorLibrary.anc4thPlusVisit(), params), "");
        EmrReportingUtils.addRow(dsd, "A4", "A4- Total ANC visits (new clients + Re-attendances)", ReportUtils.map(indicatorLibrary.totalAncVisits(), params), noTotalsColumns, Arrays.asList("01","02","03"));
        dsd.addColumn("A5", "A5: Referrals to ANC unit - Total", ReportUtils.map(indicatorLibrary.referalToAncUnitTotal(), params), "");
        dsd.addColumn("A5", "A5: Referrals to ANC unit - Community services", ReportUtils.map(indicatorLibrary.referalToAncUnitFromCommunityServices(), params), "");
        dsd.addColumn("A6", "A6: Referrals from ANC unit - Total", ReportUtils.map(indicatorLibrary.referalFromAncUnitTotal(), params), "");
        dsd.addColumn("A6", "A5: Referrals form ANC unit - FSG", ReportUtils.map(indicatorLibrary.referalFromAncUniFsg(), params), "");
        EmrReportingUtils.addRow(dsd, "A7", "A7: First dose IPT (IPT1)", ReportUtils.map(indicatorLibrary.iptDose(Dictionary.getConcept("0192ca59-b647-4f88-b07e-8fda991ba6d6")), params), noTotalsColumns, Arrays.asList("01", "02", "03"));
        EmrReportingUtils.addRow(dsd, "A8", "A8: Second dose IPT (IPT2)", ReportUtils.map(indicatorLibrary.iptDose(Dictionary.getConcept("f1d5afce-8dfe-4d2d-b24b-051815d61848")), params), noTotalsColumns, Arrays.asList("01", "02", "03"));
        dsd.addColumn("A9", "A9: Pregnant Women receiving Iron/Folic Acid on ANC 1 st Visit", ReportUtils.map(indicatorLibrary.pregnantAndReceivingIronOrFolicAcidAnc1stVisit(), params), "");
        dsd.addColumn("A10", "A10: Pregnant Women receiving free LLINs", ReportUtils.map(indicatorLibrary.pregnantAndReceivingServices(Dictionary.getConcept("3e7bb52c-e6ae-4a0b-bce0-3b36286e8658"), Dictionary.getConcept("1065AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), params), "");
        dsd.addColumn("A11", "A11: Pregnant Women tested for syphilis", ReportUtils.map(indicatorLibrary.pregnantAndTestedForSyphilis(), params), "");
        dsd.addColumn("A12", "A12: Pregnant Women tested positive for syphilis", ReportUtils.map(indicatorLibrary.pregnantAndReceivingServices(Dictionary.getConcept("275a6f72-b8a4-4038-977a-727552f69cb8"), Dictionary.getConcept("fe247560-8db6-4664-a6bc-e3b873b9b10a")), params), "");
        EmrReportingUtils.addRow(dsd, "A13", "A13: Pregnant Women newly tested for HIV this pregnancy (TR & TRR)", ReportUtils.map(indicatorLibrary.pregnantWomenNewlyTestedForHivThisPregnancyTRAndTRR(), params), noTotalsColumns, Arrays.asList("01", "02", "03"));
        EmrReportingUtils.addRow(dsd, "A14", "A14: Pregnant Women tested HIV+ for 1st time this pregnancy (TRR) at any visit", ReportUtils.map(indicatorLibrary.pregnantWomenNewlyTestedForHivThisPregnancyTRR(), params), noTotalsColumns, Arrays.asList("01", "02", "03"));
        dsd.addColumn("A15-CD4", "A15: HIV+ Pregnant women assessed by CD4", ReportUtils.map(indicatorLibrary.hivPositiveAndAccessedWithCd4WhoStage(Dictionary.getConcept("dcbcba2c-30ab-102d-86b0-7a5022ba4115")), params), "");
        dsd.addColumn("A15-WHO", "A15: HIV+ Pregnant women assessed by WHO", ReportUtils.map(indicatorLibrary.hivPositiveAndAccessedWithCd4WhoStage(Dictionary.getConcept("dcdff274-30ab-102d-86b0-7a5022ba4115")), params), "");
        dsd.addColumn("A16", "HIV+ Pregnant Women initiated on ART for EMTCT (ART)", ReportUtils.map(indicatorLibrary.hivPositiveInitiatedART(), params), "");
        dsd.addColumn("A17-T", "Pregnant women who knew status before 1st ANC total (TRK+TRRK)", ReportUtils.map(indicatorLibrary.pregnantTrkTrrk(), params), "");
        dsd.addColumn("A17-TRRk", "Pregnant women who knew status before 1st ANC total TRRK", ReportUtils.map(indicatorLibrary.pregnantTrrk(), params), "");
        dsd.addColumn("A18", "Pregnant Women already on ART before 1 ANC (ART-K)", ReportUtils.map(indicatorLibrary.alreadyOnARTK(), params), "");
        dsd.addColumn("A19", "Pregnant Women re-tested later in pregnancy (TR+&TRR+)", ReportUtils.map(indicatorLibrary.retestedTrTrrPlus(), params), "");
        dsd.addColumn("A20", "Pregnant Women testing HIV+ on a retest (TRR+)", ReportUtils.map(indicatorLibrary.retestedTrrPlus(), params), "");
        dsd.addColumn("A21", "Pregnant Women initiated on Cotrimoxazole", ReportUtils.map(indicatorLibrary.initiatedOnCtx(), params), "");
        dsd.addColumn("A22-T", "Male partners received HIV test results in eMTCT - Total", ReportUtils.map(indicatorLibrary.malePatinersRecievedHivResultTotal(), params), "");
        dsd.addColumn("A22-HIV+", "Male partners received HIV test results in eMTCT - HIV+", ReportUtils.map(indicatorLibrary.malePatinersRecievedHivResultHivPositive(), params), "");

        //connect the report definition to the dsd
        rd.addDataSetDefinition("2.1-indicators", Mapped.mapStraightThrough(dsd));

        return rd;
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
}
