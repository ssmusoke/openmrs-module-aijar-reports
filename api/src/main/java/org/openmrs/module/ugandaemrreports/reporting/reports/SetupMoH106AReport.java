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
 * Created by carapai on 25/01/2017.
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
        return "MOH 106A";
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

        EmrReportingUtils.addRow(dsd,
                "1",
                moH106AIndicatorLibrary.cumulativePatientsEnrolledInCare().getName(),
                ReportUtils.map(moH106AIndicatorLibrary.cumulativePatientsEnrolledInCare(), params),
                allColumns);

        rd.addDataSetDefinition("indicators", Mapped.mapStraightThrough(dsd));
        return rd;
    }

    @Override
    public String getVersion() {
        return "0.2";
    }
}
