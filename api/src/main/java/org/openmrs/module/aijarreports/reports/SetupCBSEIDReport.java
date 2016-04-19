/*
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
package org.openmrs.module.aijarreports.reports;

import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.MultiParameterDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class SetupCBSEIDReport extends AijarDataExportManager {

    public SetupCBSEIDReport() {
    }

    @Override
    public String getUuid() {
        return "167cf668-0715-488b-b159-d5f391774088";
    }

    @Override
    public String getName() {
        return "CBS EID Indicators";
    }

    @Override
    public String getDescription() {
        return "CBS indicators for HIV Exposed Infants";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        return l;
    }

    @Override
    public ReportDefinition constructReportDefinition() {

        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        MultiParameterDataSetDefinition multiPeriodDsd = new MultiParameterDataSetDefinition();
        multiPeriodDsd.setParameters(getParameters());
        rd.addDataSetDefinition("EID_Services", Mapped.mapStraightThrough(multiPeriodDsd));

        // Base Data Set Definition

        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
        dsd.setParameters(getParameters());
        multiPeriodDsd.setBaseDefinition(dsd);

        // Underlying cohorts


        return rd;
    }

    protected void addColumn(CohortIndicatorDataSetDefinition dsd, String name, String label, CohortDefinition cohortDefinition) {
        CohortIndicator ci = new CohortIndicator();
        ci.setType(CohortIndicator.IndicatorType.COUNT);
        ci.setParameters(cohortDefinition.getParameters());
        ci.setCohortDefinition(Mapped.mapStraightThrough(cohortDefinition));
        dsd.addColumn(name, label, Mapped.mapStraightThrough(ci), "");
    }

    @Override
    public String getExcelDesignUuid() {
        return "b98ab976-9c9d-4a28-9760-ac3119cbef58";
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        ReportDesign design = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "CBSEIDReport.xls");
        design.addPropertyValue("repeatingSections", "sheet:1,column:5,dataset:EID_Services");
        return Arrays.asList(design);
    }

    @Override
    public String getVersion() {
        return "0.1";
    }
}
