package org.openmrs.module.ugandaemrreports.model;


import org.hibernate.annotations.Type;
import org.openmrs.BaseOpenmrsData;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity(name = "ugandaemrreports.DashboardReportObject")
@Table(name = "reporting_dashboard_report_object")
public class DashboardReportObject extends BaseOpenmrsData implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "dashboard_report_id", length = 11)
    private int dashboard_report_id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String  description;

    @Column(name = "type")
    private String  type;

    @Column(name = "x_values")
    private String  rows;

    @Column(name = "y_values")
    private String  columns;

    @Column(name = "aggregator")
    private String  aggregator;

    @Column(name = "report_request_object", length = 1000000)
    @Type(type="text")
    private String report_request_object;

    @ManyToMany(mappedBy = "items")
    private Set<Dashboard> dashboards;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReport_request_object() {
        return report_request_object;
    }

    public void setReport_request_object(String report_request_object) {
        this.report_request_object = report_request_object;
    }

    @Override
    public Integer getId() {
        return dashboard_report_id;
    }

    @Override
    public void setId(Integer id) {
        this.dashboard_report_id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRows() {
        return rows;
    }

    public void setRows(String rows) {
        this.rows = rows;
    }

    public String getColumns() {
        return columns;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

    public String getAggregator() {
        return aggregator;
    }

    public void setAggregator(String aggregator) {
        this.aggregator = aggregator;
    }

    public Set<Dashboard> getDashboards() {
        return dashboards;
    }

    public void setDashboards(Set<Dashboard> dashboards) {
        this.dashboards = dashboards;
    }


}
