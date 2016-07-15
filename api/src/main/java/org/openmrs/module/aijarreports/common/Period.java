package org.openmrs.module.aijarreports.common;

/**
 * Created by carapai on 12/05/2016.
 */
public enum Period {

    MONTHLY ("MONTH"),
    QUARTERLY ("QUARTER"),
    YEARLY ("YEAR");

    private final String name;

    private Period(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
