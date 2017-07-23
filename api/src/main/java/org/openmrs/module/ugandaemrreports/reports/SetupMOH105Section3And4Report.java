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
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.Moh105IndicatorLibrary;
import org.openmrs.module.ugandaemrreports.reporting.library.dimension.CommonReportDimensionLibrary;
import org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by @Moshonk on 06/04/17.
 */
@Component
public class SetupMOH105Section3And4Report extends UgandaEMRDataExportManager {

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
        return "868df6cf-9a17-4b77-a224-61dcbf0ef8f7";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "HMIS 105-3-4.xls");
    }

    @Override
    public String getUuid() {
        return "fe02c34a-5c67-453b-93e9-4946e7bd8c59";
    }

    @Override
    public String getName() {
        return "HMIS 105 - SECTION 3-4";
    }

    @Override
    public String getDescription() {
        return "Health Unit Outpatient Monthly Report, Section 3 & 4";
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
        dsd.addDimension("age", ReportUtils.map(dimensionLibrary.htcAgeGroups(), "effectiveDate=${endDate}"));
        dsd.addDimension("gender", ReportUtils.map(dimensionLibrary.gender()));

        dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        dsd.addParameter(new Parameter("endDate", "End Date", Date.class));

        //start building the columns for the report
        addRowWithColumns(dsd, "4.H1","H1-Number of Individuals counseled", indicatorLibrary.individualsCounselled());
        addRowWithColumns(dsd, "4.H2","H2-Number of Individuals tested", indicatorLibrary.individualsTested());
        addRowWithColumns(dsd, "4.H3","H3-Number of Individuals who received HIV test results", indicatorLibrary.individualsWhoReceivedHIVTestResults());
        addRowWithColumns(dsd, "4.H4","H4- Number of individuals who received HIV results in the last 12months", indicatorLibrary.individualsWhoReceivedHIVTestResultsInLast12Months());
        addRowWithColumns(dsd, "4.H5","H5-Number of individuals tested for the first time", indicatorLibrary.individualsTestedForTheFirstTime());
        addRowWithColumns(dsd, "4.H6","H6-Number of Individuals who tested HIV positive", indicatorLibrary.individualsWhoTestedHivPositive());
        addRowWithColumns(dsd, "4.H7","H7-HIV positive individuals with presumptive TB", indicatorLibrary.individualsWhoTestedHivPositiveAndWithPresumptiveTb());
        addRowWithColumns(dsd, "4.H8","H8-Number of Individuals tested more than twice in the last 12 months", indicatorLibrary.individualsTestedMoreThanTwiceInLast12Months());
        addRowWithColumns(dsd, "4.H9","H9-Number of individuals who were Counseled and Tested together as a Couple", indicatorLibrary.individualsTestedAndReceivedResultsAsACouple());
        addRowWithColumns(dsd, "4.H10","H10-Number of individuals who were Tested and Received results together as a Couple", indicatorLibrary.individualsTestedAndReceivedResultsAsACouple());
        addRowWithColumns(dsd, "4.H11","H11-Number of couples with Concordant positive results", indicatorLibrary.couplesWithConcordantPositiveResults());
        addRowWithColumns(dsd, "4.H12","H12- Number of couples with Discordant results", indicatorLibrary.couplesWithDiscordantResults());
        addRowWithColumns(dsd, "4.H13","H13-Individuals counseled and tested for PEP", indicatorLibrary.individualsCounselledAndTestedForPep());
        addRowWithColumns(dsd, "4.H14","H14-Number of individuals tested as MARPS", indicatorLibrary.individualsCounselledAndTestedMarps());
        addRowWithColumns(dsd, "4.H15","H15-Number of positive individuals who tested at an early stage (CD4>500μ)", indicatorLibrary.hivPositiveIndividualsTestedAtAnEarlyStage());
        addRowWithColumns(dsd, "4.H16","H16-Number of clients who have been linked to care", indicatorLibrary.clientsLinkedToCare());
        
        //connect the report definition to the dsd
        rd.addDataSetDefinition("Section3-4", Mapped.mapStraightThrough(dsd));

        return rd;
    }

    public void addRowWithColumns(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortIndicator cohortIndicator) {
   	
    	addIndicator(dsd, key + "aM", label + " (Between 18 Months and 4 Years) Male", cohortIndicator, "gender=M|age=Between18MonthsAnd4Years");
        addIndicator(dsd, key + "aF", label + " (Between 18 Months and 4 Years) Female", cohortIndicator, "gender=F|age=Between18MonthsAnd4Years");
        addIndicator(dsd, key + "bM", label + " (Between 5 and 9 Years) Male", cohortIndicator, "gender=M|age=Between5And9Yrs");
        addIndicator(dsd, key + "bF", label + " (Between 5 and 9 Years) Female", cohortIndicator, "gender=F|age=Between5And9Yrs");
        addIndicator(dsd, key + "cM", label + " (Between 10 and 14 Years) Male", cohortIndicator, "gender=M|age=Between10And14Yrs");
        addIndicator(dsd, key + "cF", label + " (Between 10 and 14 Years) Female", cohortIndicator, "gender=F|age=Between10And14Yrs");
        addIndicator(dsd, key + "dM", label + " (Between 15 and 18 Years) Male", cohortIndicator, "gender=M|age=Between15And18Yrs");
        addIndicator(dsd, key + "dF", label + " (Between 15 and 18 Years) Female", cohortIndicator, "gender=F|age=Between15And18Yrs");
        addIndicator(dsd, key + "eM", label + " (Between 19 and 49 Years) Male", cohortIndicator, "gender=M|age=Between19And49Yrs");
        addIndicator(dsd, key + "eF", label + " (Between 19 and 49 Years) Female", cohortIndicator, "gender=F|age=Between19And49Yrs");
        addIndicator(dsd, key + "fM", label + " (>49) Male", cohortIndicator, "gender=M|age=GreaterThan49Yrs");
        addIndicator(dsd, key + "fF", label + " (>49) Female", cohortIndicator, "gender=F|age=GreaterThan49Yrs");
        addIndicator(dsd, key + "g", label + " (Total) ", cohortIndicator, "");
       
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
}
