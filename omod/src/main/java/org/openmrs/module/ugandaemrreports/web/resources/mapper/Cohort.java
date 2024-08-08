package org.openmrs.module.ugandaemrreports.web.resources.mapper;

import java.util.List;
import java.util.Map;

public class Cohort {
    private String clazz;
    private String uuid;
    private String name;

    private String type;
    private String description;
    private List<Map<String, Object>> parameters;

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

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

    public List<Map<String, Object>> getParameters() {
        return parameters;
    }

    public void setParameters(List<Map<String, Object>> parameters) {
        this.parameters = parameters;
    }

    public String getType() {return type;}

    public void setType(String type) { this.type = type;}
}
