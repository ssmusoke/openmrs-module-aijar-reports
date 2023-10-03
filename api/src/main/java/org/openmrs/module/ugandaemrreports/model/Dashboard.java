package org.openmrs.module.ugandaemrreports.model;


import org.openmrs.BaseOpenmrsData;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity(name = "ugandaemrreports.Dashboard")
@Table(name = "reporting_dashboard")
public class Dashboard extends BaseOpenmrsData implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "dashboard_id", length = 11)
    private int dashboard_id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String  description;

    @ManyToMany
    @JoinTable(name = "reporting_dashboard_reports",
            joinColumns = @JoinColumn(name = "dashboard_id"),
            inverseJoinColumns = @JoinColumn(name = "dashboard_report_id"))
    private List<DashboardReportObject> items;



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


    @Override
    public Integer getId() {
        return dashboard_id;
    }

    @Override
    public void setId(Integer id) {
        this.dashboard_id = id;
    }

    public List<DashboardReportObject> getItems() {
        return items;
    }

    public void setItems(List<DashboardReportObject> items) {
        this.items = items;
    }
}
