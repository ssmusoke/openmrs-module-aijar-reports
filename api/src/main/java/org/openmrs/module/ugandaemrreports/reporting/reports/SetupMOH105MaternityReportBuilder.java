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
 * Created by @Moshonk on 5/28/17.
 */
@Component
public class SetupMOH105MaternityReportBuilder extends UgandaEMRDataExportManager {

    @Autowired
    private CommonReportDimensionLibrary dimensionLibrary;

    @Autowired
    private Moh105IndicatorLibrary indicatorLibrary;
    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "07ff0ea1-a0f1-44a0-937c-c840ad91101f";
    }

    /**
     * Build the report design for the specified report, this allows a user to override the report design by adding properties and other metadata to the report design
     *
     * @param reportDefinition
     * @return The report design
     */
    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "105MaternityReport.xls");
    }

    @Override
    public String getUuid() {
        return "4d13d575-7b80-40c8-b451-1ab2ae980d84";
    }

    @Override
    public String getName() {
        return "HMIS 105 MCH - 2.2 MATERNITY";
    }

    @Override
    public String getDescription() {
        return "Health Unit Outpatient Monthly Report, Maternal and Child Health 2.2 Maternity";
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
        dsd.addDimension("age", ReportUtils.map(dimensionLibrary.standardAgeGroupsForMaternity(), "effectiveDate=${endDate}"));
        dsd.addDimension("gender", ReportUtils.map(dimensionLibrary.gender()));

        dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        dsd.addParameter(new Parameter("endDate", "End Date", Date.class));

        //define columns
        //ColumnParameters female10To19 = new ColumnParameters("f10to19", "10-19", "gender=F|age=10-19");
        //ColumnParameters female20To24 = new ColumnParameters("f20to24", "20-24", "gender=F|age=20-24");
        //ColumnParameters female25Plus = new ColumnParameters("f25plus", ">=25", "gender=F|age=25+");
        //ColumnParameters femaleTotals = new ColumnParameters("fTotals", "Total", "");

        //buils list of columns
        //List<ColumnParameters> allColumns = Arrays.asList(femaleTotals, female10To19, female20To24, female25Plus);
        //List<ColumnParameters> noTotalsColumns = Arrays.asList(female10To19, female20To24, female25Plus);

        String params = "startDate=${startDate},endDate=${endDate}";
        //start building the columns for the report
        dsd.addColumn("M1", "M1: Admissions", ReportUtils.map(indicatorLibrary.maternityAdmissions(), params), "");
        dsd.addColumn("M2", "M2: Referrals to maternity unit", ReportUtils.map(indicatorLibrary.referralsToMaternityUnit(), params), "");
        dsd.addColumn("M3", "M3: Maternity referrals out", ReportUtils.map(indicatorLibrary.maternityReferralsOut(), params), "");
        dsd.addColumn("M4 Total", "M4: Deliveries in unit - Total", ReportUtils.map(indicatorLibrary.deliveriesInUnit(), params), "");
        dsd.addColumn("M4 10-19", "M4: Deliveries in unit - 10-19", ReportUtils.map(indicatorLibrary.deliveriesInUnit10To19(), params), "");
        dsd.addColumn("M4 20-14", "M4: Deliveries in unit - 20-24", ReportUtils.map(indicatorLibrary.deliveriesInUnit20To24(), params), "");
        dsd.addColumn("M4 >=15", "M4: Deliveries in unit - >=25", ReportUtils.map(indicatorLibrary.deliveriesInUnit25AndAbove(), params), "");
        dsd.addColumn("M4 Fresh Still Birth", "M4: Fresh Still Birth", ReportUtils.map(indicatorLibrary.freshStillBirthDeliveries(), params), "");
        dsd.addColumn("M4 Macerated still birth", "M4: Macerated still birth", ReportUtils.map(indicatorLibrary.maceratedStillBirthDeliveries(), params), "");
        dsd.addColumn("M4 Live births", "M4: Live births", ReportUtils.map(indicatorLibrary.liveBirthDeliveries(), params), "");
        dsd.addColumn("M4 Pre-Term births", "M4: Pre-Term births", ReportUtils.map(indicatorLibrary.pretermBirthDeliveries(), params), "");
        dsd.addColumn("M5 - 1st", "M5: Women tested for HIV in labour - 1st time this Pregnancy", ReportUtils.map(indicatorLibrary.womenTestedForHivInLabourFirstTimePregnancy(), params), "");
        dsd.addColumn("M5 - retest", "M5: Women tested for HIV in labour - retest this Pregnancy", ReportUtils.map(indicatorLibrary.womenTestedForHivInLabourRetestThisPregnancy(), params), "");
        dsd.addColumn("M6 - 1st", "M6: Women testing HIV+ in labour - 1st time this Pregnancy", ReportUtils.map(indicatorLibrary.womenTestingHivPositiveInLabourFirstTimePregnancy(), params), "");
        dsd.addColumn("M6 - retest", "M6: Women testing HIV+ in labour - retest this Pregnancy", ReportUtils.map(indicatorLibrary.womenTestingHivPositiveInLabourRetestThisPregnancy(), params), "");
        dsd.addColumn("M7", "M7: HIV+ women initiating ART in maternity", ReportUtils.map(indicatorLibrary.hivPositiveWomenInitiatingArtInMaternity(), params), "");
        dsd.addColumn("M8 - Total", "M8: Deliveries to HIV+ women in unit - Total", ReportUtils.map(indicatorLibrary.deliveriesTohivPositiveWomen(), params), "");
        dsd.addColumn("M8 - Live births", "M8: Deliveries to HIV+ women in unit - Live births", ReportUtils.map(indicatorLibrary.liveBirthDeliveriesTohivPositiveWomen(), params), "");
        dsd.addColumn("M9", "M9: HIV-exposed babies given ARVs", ReportUtils.map(indicatorLibrary.hivExposedBabiesGivenArvs(), params), "");
        dsd.addColumn("M10 - Total", "M10: No. of mothers who initiated breastfeeding within the 1st hour after delivery - Total", ReportUtils.map(indicatorLibrary.initiatedBreastfeedingWithinFirstHourAfterDelivery(), params), "");
        dsd.addColumn("M10 - HIV+", "M10: No. of mothers who initiated breastfeeding within the 1st hour after delivery - HIV+", ReportUtils.map(indicatorLibrary.initiatedBreastfeedingWithinFirstHourAfterDeliveryAndHivPositive(), params), "");
        dsd.addColumn("M11", "M11: Babies born with low birth weight (<2.5kg)", ReportUtils.map(indicatorLibrary.babiesBornWithLowBirthWeight(), params), "");
        dsd.addColumn("M12", "M12: Live babies", ReportUtils.map(indicatorLibrary.liveBabies(), params), "");
        dsd.addColumn("M13", "M13: Babies born with defect", ReportUtils.map(indicatorLibrary.babiesBornWithDefect(), params), "");
        dsd.addColumn("M15", "M15: Newborn deaths (0-7 days)", ReportUtils.map(indicatorLibrary.newBornDeaths(), params), "");
        dsd.addColumn("M16 10-19", "M16: Maternal deaths 10-19", ReportUtils.map(indicatorLibrary.maternalDeaths10To19(), params), "");
        dsd.addColumn("M16 20-24", "M16: Maternal deaths 20-14", ReportUtils.map(indicatorLibrary.maternalDeaths20To24(), params), "");
        dsd.addColumn("M16 >=25", "M16: Maternal deaths >=25", ReportUtils.map(indicatorLibrary.maternalDeaths25AndAbove(), params), "");
        dsd.addColumn("M18", "M18: Birth asphyxia", ReportUtils.map(indicatorLibrary.birthAsphyxia(), params), "");
        dsd.addColumn("M19", "M19: No. of babies who received PNC at 6 hours", ReportUtils.map(indicatorLibrary.babiesReceivedPncAt6Hours(), params), "");
                        
        //connect the report definition to the dsd
        rd.addDataSetDefinition("2.2-indicators", Mapped.mapStraightThrough(dsd));

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
