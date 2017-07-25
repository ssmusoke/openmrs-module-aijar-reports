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

import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.GlobalPropertyParametersDatasetDefinition;
import org.openmrs.module.ugandaemrreports.library.Moh105IndicatorLibrary;
import org.openmrs.module.ugandaemrreports.reporting.library.dimension.CommonReportDimensionLibrary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reporting.utils.ColumnParameters;
import org.openmrs.module.ugandaemrreports.reporting.utils.EmrReportingUtils;
import org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Nicholas Ingosi on 7/25/17.
 */
@Component
public class Setup105Section5Report extends UgandaEMRDataExportManager  {

    @Autowired
    private CommonReportDimensionLibrary dimensionLibrary;

    @Autowired
    private Moh105IndicatorLibrary indicatorLibrary;
    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "f4578b22-7129-11e7-8fd0-507b9dc4c741";
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        List<ReportDesign> l = new ArrayList<ReportDesign>();
        l.add(buildReportDesign(reportDefinition));
        return l;
    }

    /**
     * Build the report design for the specified report, this allows a user to override the report design by adding properties and other metadata to the report design
     *
     * @param reportDefinition
     * @return The report design
     */
    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "HMIS 105-5-6.xls");
    }

    @Override
    public String getUuid() {
        return "54d3f8fa-712a-11e7-a3df-507b9dc4c741";
    }

    @Override
    public String getName() {
        return "HMIS 105 Section 5 To 6";
    }

    @Override
    public String getDescription() {
        return "Health Unit Outpatient Monthly Report, HMIS 105 Section 5 To 6";
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());


        //connect the report definition to the datasets
        rd.addDataSetDefinition("S", Mapped.mapStraightThrough(settings()));
        rd.addDataSetDefinition("M", Mapped.mapStraightThrough(smc()));

        return rd;
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public List<Parameter> getParameters() {
        return Arrays.asList(
                new Parameter("startDate", "Start Date", Date.class),
                new Parameter("endDate", "End Date", Date.class)
        );
    }

    protected DataSetDefinition settings() {
        GlobalPropertyParametersDatasetDefinition cst = new GlobalPropertyParametersDatasetDefinition();
        cst.setName("S");
        cst.setGp("ugandaemr.dhis2.organizationuuid");
        return cst;
    }

    protected DataSetDefinition smc(){
        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();

        dsd.setParameters(getParameters());
        dsd.setName("M");
        dsd.addDimension("age", ReportUtils.map(dimensionLibrary.standardAgeGroupsForSmc(), "onDate=${endDate}"));
        dsd.addDimension("gender", ReportUtils.map(dimensionLibrary.gender()));

        ColumnParameters mUnder2 = new ColumnParameters("m<2", "<2", "gender=M|age=<2");
        ColumnParameters m2Under5 = new ColumnParameters("m2<5", "2<5", "gender=M|age=2<5");
        ColumnParameters m5Under15 = new ColumnParameters("m5<15", "5<15", "gender=M|age=5<15");
        ColumnParameters m15To49 = new ColumnParameters("m15-49", "15-49", "gender=M|age=15-49");
        ColumnParameters m49Plus = new ColumnParameters("m49+", "49+", "gender=M|age=49+");
        ColumnParameters mTotals = new ColumnParameters("mTotals", "Total", "");

        List<ColumnParameters> allColumns = Arrays.asList(mUnder2, m2Under5, m5Under15, m15To49, m49Plus, mTotals);
        String params = "startDate=${startDate},endDate=${endDate}";

        dsd.addColumn("S1", "Expected Number of SMC's Performed(Monthly Targets)", ReportUtils.map(indicatorLibrary.expectedNumberOfSmcPerfomed(), params), "");
        EmrReportingUtils.addRow(dsd, "S2FS", "Number of male circumcised - Facility/Surgical", ReportUtils.map(indicatorLibrary.facilityAndSurgicalSmc(), params), allColumns, Arrays.asList("01", "02", "03", "04", "05", "06"));
        EmrReportingUtils.addRow(dsd, "S2FD", "Number of male circumcised - Facility/Device", ReportUtils.map(indicatorLibrary.facilityAndDeviceSmc(), params), allColumns, Arrays.asList("01", "02", "03", "04", "05", "06"));
        EmrReportingUtils.addRow(dsd, "S2OS", "Number of male circumcised - Outreach/Surgical", ReportUtils.map(indicatorLibrary.outreachAndSurgicalSmc(), params), allColumns, Arrays.asList("01", "02", "03", "04", "05", "06"));
        EmrReportingUtils.addRow(dsd, "S2OD", "Number of male circumcised - Outreach/Device", ReportUtils.map(indicatorLibrary.outreachAndDeviceSmc(), params), allColumns, Arrays.asList("01", "02", "03", "04", "05", "06"));
        dsd.addColumn("S3HP", "SMC Clients Counseled, Tested/HIV+", ReportUtils.map(indicatorLibrary.counseledAndTestedWithResuls(Dictionary.getConcept("dc866728-30ab-102d-86b0-7a5022ba4115")), params), "");
        dsd.addColumn("S3HN", "SMC Clients Counseled, Tested/HIV-", ReportUtils.map(indicatorLibrary.counseledAndTestedWithResuls(Dictionary.getConcept("dc85aa72-30ab-102d-86b0-7a5022ba4115")), params), "");
        dsd.addColumn("S3HT", "Total SSMC Clients Counseled, Tested", ReportUtils.map(indicatorLibrary.counseledAndTested(), params), "");
        return dsd;
    }

}
