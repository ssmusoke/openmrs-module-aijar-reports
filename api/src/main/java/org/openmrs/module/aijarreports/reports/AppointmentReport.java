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

import org.openmrs.module.aijarreports.library.DataFactory;
import org.openmrs.module.reporting.common.SortCriteria.SortDirection;
import org.openmrs.module.reporting.data.encounter.library.BuiltInEncounterDataLibrary;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppointmentReport extends AijarDataExportManager {

    @Autowired
    private BuiltInPatientDataLibrary builtInPatientData;

    public AppointmentReport() {
    }

    @Override
    public String getUuid() {
        return "20007447-1db6-4464-90b4-2e442a7e4828";
    }

    @Override
    public String getName() {
        return "Appointment Report";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ReportDefinition constructReportDefinition() {

        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        PatientDataSetDefinition dsd = new PatientDataSetDefinition();
        dsd.setName(getName());
        dsd.setParameters(getParameters());
        rd.addDataSetDefinition(getName(), Mapped.mapStraightThrough(dsd));

        dsd.addSortCriteria("PID", SortDirection.ASC);

        // Rows are patients who have a next appointment date obs date in the given date range, associated with the given location
        /*CohortDefinition rowFilter = df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDateAtLocation(commonMetadata.getAppointmentDateConcept(), null);
        dsd.addRowFilter(Mapped.mapStraightThrough(rowFilter));*/

        // Columns to include

        addColumn(dsd, "PID", builtInPatientData.getPatientId());
        /*addColumn(dsd, "HCC Number", hivPatientData.getHccNumberAtLocation());
        addColumn(dsd, "ARV Number", hivPatientData.getArvNumberAtLocation());
        addColumn(dsd, "Chronic Care Number", chronicCarePatientData.getChronicCareNumberAtLocation());*/
        addColumn(dsd, "Given name", builtInPatientData.getPreferredGivenName());
        addColumn(dsd, "Last name", builtInPatientData.getPreferredFamilyName());
        addColumn(dsd, "M/F", builtInPatientData.getGender());
        /*addColumn(dsd, "Birthdate", basePatientData.getBirthdate());
        addColumn(dsd, "Current Age (yr)", basePatientData.getAgeAtEndInYears());
        addColumn(dsd, "Current Age (mth)", basePatientData.getAgeAtEndInMonths());
        addColumn(dsd, "Village", basePatientData.getVillage());
        addColumn(dsd, "TA", basePatientData.getTraditionalAuthority());
        addColumn(dsd, "District", basePatientData.getDistrict());
        addColumn(dsd, "VHW", basePatientData.getChwOrGuardian());
        addColumn(dsd, "Appointment Date", basePatientData.getAppointmentDatesAtLocationDuringPeriod());*/

        return rd;
    }

    @Override
    public String getExcelDesignUuid() {
        return "a50cfb0c-ee2e-46dc-a4e8-c2bf4f7811ea";
    }


    @Override
    public String getVersion() {
        return "1.0";
    }
}
