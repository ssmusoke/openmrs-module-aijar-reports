package org.openmrs.module.ugandaemrreports.web.resources.mapper;

import java.util.List;

public class DataExportMapper {
    private Cohort cohort;
    private List<Column> columns;

    public Cohort getCohort() {
        return cohort;
    }

    public void setCohort(Cohort cohort) {
        this.cohort = cohort;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }
}




