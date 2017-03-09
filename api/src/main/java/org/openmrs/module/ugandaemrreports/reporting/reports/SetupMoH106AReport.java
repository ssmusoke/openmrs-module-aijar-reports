package org.openmrs.module.ugandaemrreports.reporting.reports;

import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.reporting.library.dimension.CommonReportDimensionLibrary;
import org.openmrs.module.ugandaemrreports.reporting.library.indicator.MoH106AIndicatorLibrary;
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
 * MOH 106a
 */
@Component
public class SetupMoH106AReport extends UgandaEMRDataExportManager {

    @Autowired
    MoH106AIndicatorLibrary moH106AIndicatorLibrary;
    @Autowired
    CommonReportDimensionLibrary commonReportDimensionLibrary;

    @Override
    public String getExcelDesignUuid() {
        return "e951d73e-dbb9-48bf-bddb-aad127aff609";
    }

    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "106A1AReport.xls");
    }

    @Override
    public String getUuid() {
        return "c80ee1d9-9648-4749-bedc-03cc61b74399";
    }

    @Override
    public String getName() {
        return "MOH 106A Refactored";
    }

    @Override
    public String getDescription() {
        return "MOH 106A Refactored";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(new Parameter("startDate", "Start date (Start of quarter)", Date.class));
        l.add(new Parameter("endDate", "End date (End of quarter)", Date.class));
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
        dsd.addDimension("age", ReportUtils.map(commonReportDimensionLibrary.get106AgeGroups(), "effectiveDate=${endDate}"));
        dsd.addDimension("mohage", ReportUtils.map(commonReportDimensionLibrary.getMOHDefinedChildrenAndAdultAgeGroups(), "effectiveDate=${endDate}"));
        dsd.addDimension("gender", ReportUtils.map(commonReportDimensionLibrary.gender()));


        ColumnParameters colInfantsMale = new ColumnParameters("a", "<2 Male", "age=<2|gender=M");
        ColumnParameters colInfantsFemale = new ColumnParameters("b", "<2 Female", "age=<2|gender=F");

        ColumnParameters colChildrenMale = new ColumnParameters("c", "2-<5 Male", "age=2-<5|gender=M");
        ColumnParameters colChildrenFemale = new ColumnParameters("d", "2-<5 Female", "age=2-<5|gender=F");

        ColumnParameters colAdolescentsMale = new ColumnParameters("e", "5-14 Male", "age=5-14|gender=M");
        ColumnParameters colAdolescentsFemale = new ColumnParameters("f", "5-14 Female", "age=5-14|gender=F");

        ColumnParameters colAdultsMale = new ColumnParameters("g", "15+ Male", "age=15+|gender=M");
        ColumnParameters colAdultsFemale = new ColumnParameters("h", "15+ Female", "age=15+|gender=F");

        ColumnParameters colTotal = new ColumnParameters("i", "Total", "");

        List<ColumnParameters> allColumns = Arrays.asList(colInfantsMale, colInfantsFemale, colChildrenMale, colChildrenFemale, colAdolescentsMale, colAdolescentsFemale, colAdultsMale, colAdultsFemale, colTotal);
        List<ColumnParameters> adultFemaleColumns = Arrays.asList(colAdultsFemale, colTotal);
        
        ColumnParameters colChildren = new ColumnParameters("a", "< 15", "mohage=<15");
        ColumnParameters colAdult = new ColumnParameters("b", "15+", "mohage=15+");
        List<ColumnParameters> mohAgeColumns = Arrays.asList(colChildren, colAdult, colTotal);

        EmrReportingUtils.addRow(dsd,
                "1",
                moH106AIndicatorLibrary.cumulativePatientsEnrolledInCareAtTheEndOfLastQuarter().getName(),
                ReportUtils.map(moH106AIndicatorLibrary.cumulativePatientsEnrolledInCareAtTheEndOfLastQuarter(), params),
                allColumns);
        EmrReportingUtils.addRow(dsd,
                "2",
                moH106AIndicatorLibrary.newPatientsEnrolledInCare().getName(),
                ReportUtils.map(moH106AIndicatorLibrary.newPatientsEnrolledInCare(), params),
                allColumns);
        EmrReportingUtils.addRow(dsd,
                "3",
                moH106AIndicatorLibrary.pregnantAndLactatingWomenEnrolledInQuarter().getName(),
                ReportUtils.map(moH106AIndicatorLibrary.pregnantAndLactatingWomenEnrolledInQuarter(), params),
                adultFemaleColumns);
        EmrReportingUtils.addRow(dsd,
                "5",
                moH106AIndicatorLibrary.cumulativePatientsEnrolledInCareAtTheEndOfReportingQuarter().getName(),
                ReportUtils.map(moH106AIndicatorLibrary.cumulativePatientsEnrolledInCareAtTheEndOfReportingQuarter(), params),
                allColumns);
        EmrReportingUtils.addRow(dsd,
                "6",
                moH106AIndicatorLibrary.transferInAlreadyEnrolledInHIVCare().getName(),
                ReportUtils.map(moH106AIndicatorLibrary.transferInAlreadyEnrolledInHIVCare(), params),
                allColumns);
        EmrReportingUtils.addRow(dsd,
                "7",
                moH106AIndicatorLibrary.activeClientsOnPreART().getName(),
                ReportUtils.map(moH106AIndicatorLibrary.activeClientsOnPreART(), params),
                mohAgeColumns);
        EmrReportingUtils.addRow(dsd,
                "15",
                moH106AIndicatorLibrary.cumulativeClientsStartedOnARTAtEndOfPreviousQuarter().getName(),
                ReportUtils.map(moH106AIndicatorLibrary.cumulativeClientsStartedOnARTAtEndOfPreviousQuarter(), params),
                allColumns);
        EmrReportingUtils.addRow(dsd,
                "16",
                moH106AIndicatorLibrary.newClientStartedOnART().getName(),
                ReportUtils.map(moH106AIndicatorLibrary.newClientStartedOnART(), params),
                allColumns);
        EmrReportingUtils.addRow(dsd,
                "18",
                moH106AIndicatorLibrary.newPregnantOrLactatingClientStartedOnART().getName(),
                ReportUtils.map(moH106AIndicatorLibrary.newPregnantOrLactatingClientStartedOnART(), params),
                adultFemaleColumns);
        EmrReportingUtils.addRow(dsd,
                "20",
                moH106AIndicatorLibrary.clientsOnFirstLineRegimen().getName(),
                ReportUtils.map(moH106AIndicatorLibrary.clientsOnFirstLineRegimen(), params),
                allColumns);
        EmrReportingUtils.addRow(dsd,
                "21",
                moH106AIndicatorLibrary.clientsOnSecondLineRegimen().getName(),
                ReportUtils.map(moH106AIndicatorLibrary.clientsOnSecondLineRegimen(), params),
                allColumns);
        EmrReportingUtils.addRow(dsd,
                "22",
                moH106AIndicatorLibrary.clientsOnThirdLineRegimen().getName(),
                ReportUtils.map(moH106AIndicatorLibrary.clientsOnThirdLineRegimen(), params),
                allColumns);

        rd.addDataSetDefinition("indicators", Mapped.mapStraightThrough(dsd));
        return rd;
    }

    @Override
    public String getVersion() {
        return "0.2.2";
    }
}
