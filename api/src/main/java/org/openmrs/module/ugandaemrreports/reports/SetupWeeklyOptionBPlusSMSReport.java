package org.openmrs.module.ugandaemrreports.reports;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.Moh105IndicatorLibrary;
import org.openmrs.module.ugandaemrreports.reporting.library.dimension.CommonReportDimensionLibrary;
import org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * MOH 106a
 */
@Component
public class SetupWeeklyOptionBPlusSMSReport extends UgandaEMRDataExportManager {

    @Autowired
    Moh105IndicatorLibrary moh105IndicatorLibrary;
    @Autowired
    CommonReportDimensionLibrary commonReportDimensionLibrary;

    @Override
    public String getExcelDesignUuid() {
        return "1a31fc40-029c-495e-b2a0-2d52e1ad7d9b";
    }

    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "WeeklyOptionBPlusSMS.xls");
    }

    @Override
    public String getUuid() {
        return "09c76cd3-4585-4e1c-b3f7-78e2a5726d6d";
    }

    @Override
    public String getName() {
        return "OptionB+ Weekly SMS Report";
    }

    @Override
    public String getDescription() {
        return "Option B+ Weekly SMS Report";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(new Parameter("startDate", "Start Date", Date.class));
        l.add(new Parameter("endDate", "End Date", Date.class));
        return l;
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        String params = "startDate=${startDate},endDate=${endDate}";

        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
        dsd.setParameters(getParameters());
    
        dsd.addColumn("Total No ANC Visits 1", "1a", ReportUtils.map(moh105IndicatorLibrary.anc1stVisit(),params),  (String) null);
        dsd.addColumn("Total No ANC Tested", "2b", ReportUtils.map(moh105IndicatorLibrary.pregnantWomenNewlyTestedForHivThisPregnancyTRAndTRR(),params),  (String) null);
        dsd.addColumn("Total No ANC Tested Positive", "3c", ReportUtils.map(moh105IndicatorLibrary.pregnantWomenNewlyTestedForHivThisPregnancyAndTestedHIVPositive(),params),  (String) null);
        dsd.addColumn("Total No ANC Known Positive", "4d", ReportUtils.map(moh105IndicatorLibrary.pregnantTrkTrrk(),params),  (String) null);
        dsd.addColumn("Total Initiating OptionB+", "5e", ReportUtils.map(moh105IndicatorLibrary.hivPositiveInitiatedART(),params),  (String) null);
        dsd.addColumn("Total ANC 1 on ART Before", "6f", ReportUtils.map(moh105IndicatorLibrary.alreadyOnARTK(),params),  (String) null);
        dsd.addColumn("Total Missed Appointments", "7g", ReportUtils.map(moh105IndicatorLibrary.missedANCAppointment(),params),  (String) null);
    
        rd.addDataSetDefinition("indicators", Mapped.mapStraightThrough(dsd));
        return rd;
    }

    @Override
    public String getVersion() {
        return "0.1";
    }
}
