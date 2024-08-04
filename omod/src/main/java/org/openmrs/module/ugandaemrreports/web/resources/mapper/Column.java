package org.openmrs.module.ugandaemrreports.web.resources.mapper;

import java.util.List;

public class Column {
    private String label;
    private String type;
    private String expression;
    private int modifier = 1;
    private List<String> extras;

    public Column() {
    }

    public Column(String label, String type, String expression) {
        this.label = label;
        this.type = type;
        this.expression = expression;
    }

    public Column(String label, String type, String expression, int modifier, List<String> extras) {
        this.label = label;
        this.type = type;
        this.expression = expression;
        this.modifier = modifier;
        this.extras = extras;
    }

    public Column(String label, String type, String expression, int modifier) {
        this.label = label;
        this.type = type;
        this.expression = expression;
        this.modifier = modifier;
    }

    public Column(String expression, String label, String type, List<String> extras) {
        this.expression = expression;
        this.label = label;
        this.type = type;
        this.extras = extras;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public int getModifier() {
        return modifier;
    }

    public void setModifier(int modifier) {
        this.modifier = modifier;
    }

    public List<String> getExtras() {
        return extras;
    }

    public void setExtras(List<String> extras) {
        this.extras = extras;
    }
}
