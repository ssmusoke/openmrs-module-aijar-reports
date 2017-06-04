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

import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.CommonCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.Moh105CohortLibrary;
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
 * Created by @Moshonk on 06/04/17.
 */
@Component
public class SetupMOH105FPReportBuilder extends UgandaEMRDataExportManager {

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
        return "75cf89c0-62ae-4ea8-a666-308a4e050554";
    }

    /**
     * Build the report design for the specified report, this allows a user to override the report design by adding properties and other metadata to the report design
     *
     * @param reportDefinition
     * @return The report design
     */
    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "105FamilyPlanningMethods.xls");
    }

    @Override
    public String getUuid() {
        return "8790164d-77a7-49c8-9a4c-644100fdd849";
    }

    @Override
    public String getName() {
        return "HMIS 105 MCH - 2.5 FAMILY PLANNING METHODS";
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
        //dsd.addDimension("gender", ReportUtils.map(dimensionLibrary.gender()));

        dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        dsd.addParameter(new Parameter("endDate", "End Date", Date.class));

        //start building the columns for the report
        addColumns(dsd, "F1", "F1-Oral : Lo-Femenal", indicatorLibrary.oralLofemenalFamilyPlanningUsers());
        addColumns(dsd, "F2", "F2-Oral: Microgynon", indicatorLibrary.oralMicrogynonFamilyPlanningUsers());
        addColumns(dsd, "F3", "F3-Oral: Ovrette", indicatorLibrary.oralOvretteFamilyPlanningUsers());
        addColumns(dsd, "F4", "F4-Oral: Others", indicatorLibrary.oralOtherFamilyPlanningUsers());
        addColumns(dsd, "F5", "F5-Female condoms", indicatorLibrary.femaleCondomsFamilyPlanningUsers());
        addColumns(dsd, "F6", "F6-Male condoms", indicatorLibrary.maleCondomsFamilyPlanningUsers());
        addColumns(dsd, "F7", "F7-IUDs", indicatorLibrary.iudFamilyPlanningUsers());
        addColumns(dsd, "F8", "F8-Injectable", indicatorLibrary.injectableFamilyPlanningUsers());
        addColumns(dsd, "F9", "F9-Natural", indicatorLibrary.naturalFamilyPlanningUsers());
        addColumns(dsd, "F10", "F10-Other methods", indicatorLibrary.otherFamilyPlanningUsers());
        addColumns(dsd, "Total", "Total family planning users", indicatorLibrary.allFamilyPlanningUsers());
        addIndicator(dsd, "F11", "F11: Number HIV+ FP users", indicatorLibrary.hivPositiveFamilyPlanningUsers(), "");
                        
        //connect the report definition to the dsd
        rd.addDataSetDefinition("2.5-indicators", Mapped.mapStraightThrough(dsd));

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
