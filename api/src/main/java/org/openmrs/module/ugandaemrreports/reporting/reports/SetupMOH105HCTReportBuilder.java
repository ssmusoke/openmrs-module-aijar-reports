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

import java.util.ArrayList;
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
import org.openmrs.module.ugandaemrreports.reports.UgandaEMRDataExportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by @Moshonk on 06/05/17.
 */
@Component
public class SetupMOH105HCTReportBuilder extends UgandaEMRDataExportManager {

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
        return "be281402-9756-45fd-9f34-b9a7d908e09c";
    }

    /**
     * Build the report design for the specified report, this allows a user to override the report design by adding properties and other metadata to the report design
     *
     * @param reportDefinition
     * @return The report design
     */
    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "105HIVCounsellingTesting.xls");
    }

    @Override
    public String getUuid() {
        return "09c92c8b-2a36-4af4-afdc-e4b151ac040b";
    }

    @Override
    public String getName() {
        return "HMIS 105 - SECTION 4: HIV/AIDS COUNSELING AND TESTING (HCT)";
    }

    @Override
    public String getDescription() {
        return "Health Unit Outpatient Monthly Report, HIV/AIDS Counselling and Testing (HHCT)";
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
        //dsd.addDimension("gender", ReportUtils.map(dimensionLibrary.gender()));

        dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        dsd.addParameter(new Parameter("endDate", "End Date", Date.class));

        //start building the columns for the report
        addIndicator(dsd, "H1", "H1-Number of Individuals counseled", indicatorLibrary.individualsCounselled(), "");
        addIndicator(dsd, "H2", "H2-Number of Individuals tested", indicatorLibrary.individualsTested(), "");
        addIndicator(dsd, "H3", "H3-Number of Individuals who received HIV test results", indicatorLibrary.individualsWhoReceivedHIVTestResults(), "");
        addIndicator(dsd, "H4", "H4- Number of individuals who received HIV results in the last 12months", indicatorLibrary.individualsWhoReceivedHIVTestResultsInLast12Months(), "");
        addIndicator(dsd, "H5", "H5 – Number of individuals tested for the first time", indicatorLibrary.individualsTestedForTheFirstTime(), "");
        addIndicator(dsd, "H6", "H6-Number of Individuals who tested HIV positive", indicatorLibrary.individualsWhoTestedHivPositive(), "");
        addIndicator(dsd, "H7", "H7-HIV positive individuals with presumptive TB", indicatorLibrary.individualsWhoTestedHivPositiveAndWithPresumptiveTb(), "");
        addIndicator(dsd, "H8", "H8-Number of Individuals tested more than twice in the last 12 months", indicatorLibrary.individualsTestedMoreThanTwiceInLast12Months(), "");
        addIndicator(dsd, "H9", "H9-Number of individuals who were Counseled and Tested together as a Couple", indicatorLibrary.individualsCounseledAndTestedAsCouple(), "");
        addIndicator(dsd, "H10", "H10-Number of individuals who were Tested and Received results together as a Couple", indicatorLibrary.individualsTestedAndReceivedResultsAsACouple(), "");
        addIndicator(dsd, "H11", "H11-Number of couples with Concordant positive results", indicatorLibrary.couplesWithConcordantPositiveResults(), "");
        addIndicator(dsd, "H12", "H12- Number of couples with Discordant results", indicatorLibrary.couplesWithDiscordantResults(), "");
        addIndicator(dsd, "H13", "H13-Individuals counseled and tested for PEP", indicatorLibrary.individualsCounselledAndTestedForPep(), "");
        addIndicator(dsd, "H14", "H14-Number of individuals tested as MARPS", indicatorLibrary.individualsCounselledAndTestedMarps(), "");
        addIndicator(dsd, "H15", "H15-Number of positive individuals who tested at an early stage (CD4>500μ)", indicatorLibrary.hivPositiveIndividualsTestedAtAnEarlyStage(), "");
        addIndicator(dsd, "H16", "H16-Number of clients who have been linked to care", indicatorLibrary.clientsLinkedToCare(), "");
                        
        //connect the report definition to the dsd
        rd.addDataSetDefinition("SECTION 4-indicators", Mapped.mapStraightThrough(dsd));

        return rd;
    }

    public void addColumns(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortIndicator cohortIndicator) {
        addIndicator(dsd, key + "a", label + " (Between 10 and 19)", cohortIndicator, "age=Between10And19");
        addIndicator(dsd, key + "b", label + " (Between 20 and 14)", cohortIndicator, "age=Between20And24");
        addIndicator(dsd, key + "c", label + " (>= 25)", cohortIndicator, "age=GreaterOrEqualTo25");
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
