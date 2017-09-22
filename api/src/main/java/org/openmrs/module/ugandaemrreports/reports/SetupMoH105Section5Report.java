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

import org.openmrs.Concept;
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

import static org.openmrs.module.ugandaemrreports.library.CommonDatasetLibrary.period;
import static org.openmrs.module.ugandaemrreports.library.CommonDatasetLibrary.settings;

/**
 * 
 */
@Component
public class SetupMoH105Section5Report extends UgandaEMRDataExportManager  {

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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "HMIS_105-5-6.xls");
    }

    @Override
    public String getUuid() {
        return "54d3f8fa-712a-11e7-a3df-507b9dc4c741";
    }

    @Override
    public String getName() {
        return "HMIS 105 Section 5: HCT";
    }

    @Override
    public String getDescription() {
        return "Health Unit Outpatient Monthly Report, HMIS 105 Section 5: HCT";
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
        rd.addDataSetDefinition("P", Mapped.mapStraightThrough(period()));
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

    protected DataSetDefinition smc(){
        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();

        dsd.setParameters(getParameters());
        dsd.setName("M");
        dsd.addDimension("age", ReportUtils.map(dimensionLibrary.standardAgeGroupsForSmc(), "onDate=${endDate}"));
        dsd.addDimension("site", ReportUtils.map(dimensionLibrary.siteType(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
        dsd.addDimension("method", ReportUtils.map(dimensionLibrary.procedureMethod(), "onOrAfter=${startDate},onOrBefore=${endDate}"));

        ColumnParameters fsUnder2 = new ColumnParameters("FS<2", "FS<2", "site=F|age=<2|method=S");
        ColumnParameters fs2Under5 = new ColumnParameters("FS2<5", "FS2<5", "site=F|age=2<5|method=S");
        ColumnParameters fs5Under15 = new ColumnParameters("FS5<15", "FS5<15", "site=F|age=5<15|method=S");
        ColumnParameters fs15To49 = new ColumnParameters("FS15-49", "FS15-49", "site=F|age=15-49|method=S");
        ColumnParameters fs49Plus = new ColumnParameters("FS49+", "FS49+", "site=F|age=49+|method=S");
        
        ColumnParameters fdUnder2 = new ColumnParameters("FD<2", "FD<2", "site=F|age=<2|method=D");
        ColumnParameters fd2Under5 = new ColumnParameters("FD2<5", "FD2<5", "site=F|age=2<5|method=D");
        ColumnParameters fd5Under15 = new ColumnParameters("FD5<15", "FD5<15", "site=F|age=5<15|method=D");
        ColumnParameters fd15To49 = new ColumnParameters("FD15-49", "FD15-49", "site=F|age=15-49|method=D");
        ColumnParameters fd49Plus = new ColumnParameters("FD49+", "FD49+", "site=F|age=49+|method=D");
        
        ColumnParameters osUnder2 = new ColumnParameters("OS<2", "OS<2", "site=O|age=<2|method=S");
        ColumnParameters os2Under5 = new ColumnParameters("OS2<5", "OS2<5", "site=O|age=2<5|method=S");
        ColumnParameters os5Under15 = new ColumnParameters("OS5<15", "OS5<15", "site=O|age=5<15|method=S");
        ColumnParameters os15To49 = new ColumnParameters("OS15-49", "OS15-49", "site=O|age=15-49|method=S");
        ColumnParameters os49Plus = new ColumnParameters("OS49+", "OS49+", "site=O|age=49+|method=S");
        
        ColumnParameters odUnder2 = new ColumnParameters("OD<2", "OD<2", "site=O|age=<2|method=D");
        ColumnParameters od2Under5 = new ColumnParameters("OD2<5", "OD2<5", "site=O|age=2<5|method=D");
        ColumnParameters od5Under15 = new ColumnParameters("OD5<15", "OD5<15", "site=O|age=5<15|method=D");
        ColumnParameters od15To49 = new ColumnParameters("OD15-49", "OD15-49", "site=O|age=15-49|method=D");
        ColumnParameters od49Plus = new ColumnParameters("OD49+", "OD49+", "site=O|age=49+|method=D");
        
        
        ColumnParameters foTotals = new ColumnParameters("mTotals", "Total", "");

        List<ColumnParameters> allColumns = new ArrayList<ColumnParameters>();
        allColumns.add(fsUnder2);
        allColumns.add(fs2Under5);
        allColumns.add(fs5Under15); 
        allColumns.add(fs15To49); 
        allColumns.add(fs49Plus);
        allColumns.add(fdUnder2);
        allColumns.add(fd2Under5);
        allColumns.add(fd5Under15);
        allColumns.add(fd15To49);
        allColumns.add(fd49Plus);
        allColumns.add(osUnder2);
        allColumns.add(os2Under5);
        allColumns.add(os5Under15);
        allColumns.add(os15To49);
        allColumns.add(os49Plus);
        allColumns.add(odUnder2); 
        allColumns.add(od2Under5); 
        allColumns.add(od5Under15); 
        allColumns.add(od15To49); 
        allColumns.add(od49Plus);
        allColumns.add(foTotals);
        
        String params = "startDate=${startDate},endDate=${endDate}";

        Concept forceps = Dictionary.getConcept("0308bd0a-0e28-4c62-acbd-5ea969c296db");
        Concept other = Dictionary.getConcept("dcd68a88-30ab-102d-86b0-7a5022ba4115");
       

        dsd.addColumn("S1", "Expected Number of SMC's Performed(Monthly Targets)", ReportUtils.map(indicatorLibrary.expectedNumberOfSmcPerfomed(), params), "");
        EmrReportingUtils.addRow(dsd, "S2", "Number of male circumcised", ReportUtils.map(indicatorLibrary.expectedNumberOfSmcPerfomed(), params), allColumns, Arrays.asList("01", "02", "03", "04", "05", "06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21"));
        dsd.addColumn("S3HP", "SMC Clients Counseled, Tested/HIV+", ReportUtils.map(indicatorLibrary.counseledAndTestedWithResuls(Dictionary.getConcept("dc866728-30ab-102d-86b0-7a5022ba4115")), params), "");
        dsd.addColumn("S3HN", "SMC Clients Counseled, Tested/HIV-", ReportUtils.map(indicatorLibrary.counseledAndTestedWithResuls(Dictionary.getConcept("dc85aa72-30ab-102d-86b0-7a5022ba4115")), params), "");
        dsd.addColumn("S3HT", "Total SSMC Clients Counseled, Tested", ReportUtils.map(indicatorLibrary.counseledAndTested(), params), "");
        dsd.addColumn("S448H", "First follow up visit within 48 Hours", ReportUtils.map(indicatorLibrary.smcFollowUps(2), params), "");
        dsd.addColumn("S47D", "Second follow up visit within 7 Days", ReportUtils.map(indicatorLibrary.smcFollowUps(7), params), "");
        dsd.addColumn("S47DB", "Further Follow up visit Beyonds 7 Days", ReportUtils.map(indicatorLibrary.smcFollowUps(8), params), "");
        dsd.addColumn("S5M", "Moderate", ReportUtils.map(indicatorLibrary.circumcisedAndExperiencedAdverseEvents(Dictionary.getConcept("ba7ae66b-8108-45b6-a34d-e842cf31c623")), params), "");
        dsd.addColumn("S5S", "Moderate", ReportUtils.map(indicatorLibrary.circumcisedAndExperiencedAdverseEvents(Dictionary.getConcept("44f95fcb-1054-466f-906d-45a41ef07297")), params), "");
        dsd.addColumn("S5T", "Moderate", ReportUtils.map(indicatorLibrary.circumcisedAndExperiencedAdverseEvents(), params), "");
        dsd.addColumn("S6SC", "Surgical SMC", ReportUtils.map(indicatorLibrary.clientsCircumcisedWithSurgicalTechnique(), params),"");
        dsd.addColumn("S6DC", "Surgical SMC", ReportUtils.map(indicatorLibrary.clientsCircumcisedWithTechnique(forceps), params),"");
        dsd.addColumn("S6OT", "Surgical SMC", ReportUtils.map(indicatorLibrary.clientsCircumcisedWithTechnique(other), params),"");
        
        return dsd;
    }

}