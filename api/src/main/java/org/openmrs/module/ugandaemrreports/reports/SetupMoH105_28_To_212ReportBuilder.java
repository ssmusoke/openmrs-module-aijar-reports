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
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.GlobalPropertyParametersDatasetDefinition;
import org.openmrs.module.ugandaemrreports.library.Moh105IndicatorLibrary;
import org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 */
@Component
public class SetupMoH105_28_To_212ReportBuilder extends UgandaEMRDataExportManager {

    @Autowired
    private Moh105IndicatorLibrary indicatorLibrary;
    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "eb36290b-61ef-4db4-82b5-c1af3d6c19b5";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "HMIS_105-2.8-2.12.xls");
    }

    @Override
    public String getUuid() {
        return "9401662d-e715-4370-bfc7-048825dd8107";
    }

    @Override
    public String getName() {
        return "HMIS 105 Section 2.8 To 2.12";
    }

    @Override
    public String getDescription() {
        return "Health Unit Outpatient Monthly Report, HMIS 105 Section 2.8 To 2.12";
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
        rd.addDataSetDefinition("T", Mapped.mapStraightThrough(tetanusImmunizations()));

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

    protected DataSetDefinition tetanusImmunizations() {
        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
        dsd.setParameters(getParameters());
        dsd.setName("T");
        
        String params = "startDate=${startDate},endDate=${endDate}";
        
        dsd.addColumn("T1PS","T1: T1-Dose 1 - pregnant - static", ReportUtils.map(indicatorLibrary.tetanusImmunizationsDone(1, true),params), "");
        dsd.addColumn("T2PS","T2-Dose 2 - pregnant - static", ReportUtils.map(indicatorLibrary.tetanusImmunizationsDone(2, true),params), "");
        dsd.addColumn("T3PS","T3-Dose 3 - pregnant - static", ReportUtils.map(indicatorLibrary.tetanusImmunizationsDone(3, true), params), "");
        dsd.addColumn("T4PS","T4-Dose 4 - pregnant - static", ReportUtils.map(indicatorLibrary.tetanusImmunizationsDone(4, true), params), "");
        dsd.addColumn("T5PS","T5-Dose 5 - pregnant - static", ReportUtils.map(indicatorLibrary.tetanusImmunizationsDone(5, true), params), "");

        dsd.addColumn("T1NPS","T1: T1-Dose 1 - non-pregnant - static", ReportUtils.map(indicatorLibrary.tetanusImmunizationsDone(1, false),params), "");
        dsd.addColumn("T2NPS","T2-Dose 2 - non-pregnant - static", ReportUtils.map(indicatorLibrary.tetanusImmunizationsDone(2, false),params), "");
        dsd.addColumn("T3NPS","T3-Dose 3 - non-pregnant - static", ReportUtils.map(indicatorLibrary.tetanusImmunizationsDone(3, false), params), "");
        dsd.addColumn("T4NPS","T4-Dose 4 - non-pregnant - static", ReportUtils.map(indicatorLibrary.tetanusImmunizationsDone(4, false), params), "");
        dsd.addColumn("T5NPS","T5-Dose 5 - non-pregnant - static", ReportUtils.map(indicatorLibrary.tetanusImmunizationsDone(5, false), params), "");
        
        return dsd;
    }    

    protected DataSetDefinition settings() {
        GlobalPropertyParametersDatasetDefinition cst = new GlobalPropertyParametersDatasetDefinition();
        cst.setName("S");
        cst.setGp("ugandaemr.dhis2.organizationuuid");
        return cst;
    }




}
