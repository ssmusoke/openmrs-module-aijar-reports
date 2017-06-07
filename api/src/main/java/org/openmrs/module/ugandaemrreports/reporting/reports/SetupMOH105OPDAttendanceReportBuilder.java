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
 * Created by @Moshonk on 06/04/17.
 */
@Component
public class SetupMOH105OPDAttendanceReportBuilder extends UgandaEMRDataExportManager {

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
        return "20d38f0f-d718-4c83-b255-fe961e1b7339";
    }

    /**
     * Build the report design for the specified report, this allows a user to override the report design by adding properties and other metadata to the report design
     *
     * @param reportDefinition
     * @return The report design
     */
    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "105OPDAttendance-1.1.xls");
    }

    @Override
    public String getUuid() {
        return "6ff4e2c1-e272-4735-bbeb-0cb44e58f6de";
    }

    @Override
    public String getName() {
        return "HMIS 105 - SECTION 1.1: OPD ATTENDANCE";
    }

    @Override
    public String getDescription() {
        return "Health Unit Outpatient Monthly Report, OPD Attendances Section 1.1";
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
        dsd.addDimension("age", ReportUtils.map(dimensionLibrary.standardAgeGroupsForOutPatient(), "effectiveDate=${endDate}"));
        dsd.addDimension("gender", ReportUtils.map(dimensionLibrary.gender()));

        dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        dsd.addParameter(new Parameter("endDate", "End Date", Date.class));

        //start building the columns for the report
        addColumns(dsd, "1.1N", "1.1 OUTPATIENT ATTENDANCE - New Attendance", indicatorLibrary.newOutPatientAttendance());
        addColumns(dsd, "1.1R", "1.1 OUTPATIENT ATTENDANCE - Re-attendance", indicatorLibrary.repeatOutPatientAttendance());
        addColumns(dsd, "1.1T", "1.1 OUTPATIENT ATTENDANCE - Total attendance", indicatorLibrary.totalOutPatientAttendance());
                        
        //connect the report definition to the dsd
        rd.addDataSetDefinition("1.1-OutPatientAttendance", Mapped.mapStraightThrough(dsd));

        return rd;
    }

    public void addColumns(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortIndicator cohortIndicator) {
        addIndicator(dsd, key + "aM", label + " (Between 0 and 28 Days) Male", cohortIndicator, "gender=M|age=Between0And28Days");
        addIndicator(dsd, key + "aF", label + " (Between 0 and 28 Days) Female", cohortIndicator, "gender=F|age=Between0And28Days");
        addIndicator(dsd, key + "bM", label + " (Between 29 Days and 4 Years) Male", cohortIndicator, "gender=M|age=Between29DaysAnd4Yrs");
        addIndicator(dsd, key + "bF", label + " (Between 29 Days and 4 Years) Female", cohortIndicator, "gender=F|age=Between29DaysAnd4Yrs");
        addIndicator(dsd, key + "cM", label + " (Between 5 and 59 Years) Male", cohortIndicator, "gender=M|age=Between5And59Yrs");
        addIndicator(dsd, key + "cF", label + " (Between 5 and 59 Years) Female", cohortIndicator, "gender=F|age=Between5And59Yrs");
        addIndicator(dsd, key + "dM", label + " (>=60) Male", cohortIndicator, "gender=M|age=GreaterOrEqualTo60Yrs");
        addIndicator(dsd, key + "dF", label + " (>=60) Female", cohortIndicator, "gender=F|age=GreaterOrEqualTo60Yrs");
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
