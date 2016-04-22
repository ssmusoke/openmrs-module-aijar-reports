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

import org.openmrs.logic.op.In;
import org.openmrs.module.aijarreports.library.DataFactory;
import org.openmrs.module.aijarreports.library.EIDCohortDefinitionLibrary;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.library.BuiltInCohortDefinitionLibrary;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class SetupCBSEIDReport extends AijarDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    private EIDCohortDefinitionLibrary eidCohorts;

    @Autowired
    private BuiltInCohortDefinitionLibrary builtInCohorts;


    public SetupCBSEIDReport() {
    }

    @Override
    public String getUuid() {
        return "167cf668-0715-488b-b159-d5f391774099";
    }

    @Override
    public String getName() {
        return "CBS EID Cohort Report";
    }

    @Override
    public String getDescription() {
        return "CBS Cohort for HIV Exposed Infants";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(new Parameter("startYear", "Start Year", Integer.class));
        l.add(new Parameter("startMonth", "Start Month", Integer.class));
        l.add(new Parameter("monthsBefore", "Finishing", Integer.class));
        return l;
    }

    @Override
    public ReportDefinition constructReportDefinition() {

        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());
        rd.setBaseCohortDefinition(Mapped.mapStraightThrough(eidCohorts.getAllEIDPatients()));

        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
        dsd.setParameters(getParameters());
        rd.addDataSetDefinition("cohort", Mapped.mapStraightThrough(dsd));

        CohortDefinition males = builtInCohorts.getMales();
        CohortDefinition females = builtInCohorts.getFemales();
        addIndicator(dsd, "1m", "Males", males);
        addIndicator(dsd, "1f", "Females", females);

        return rd;
    }


    public void addIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        CohortIndicator ci = new CohortIndicator();
        ci.addParameters(dsd.getParameters());
        ci.setType(CohortIndicator.IndicatorType.COUNT);
        ci.setCohortDefinition(Mapped.mapStraightThrough(cohortDefinition));
        dsd.addColumn(key, label, Mapped.mapStraightThrough(ci), "");
    }


    @Override
    public String getExcelDesignUuid() {
        return "b98ab976-9c9d-4a28-9760-ac3119cbef44";
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        ReportDesign design = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "CBSEIDCohortReport.xls");
        return Arrays.asList(design);
    }

    @Override
    public String getVersion() {
        return "0.1";
    }
}
