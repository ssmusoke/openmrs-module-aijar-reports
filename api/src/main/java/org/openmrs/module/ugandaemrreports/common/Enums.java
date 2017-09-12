package org.openmrs.module.ugandaemrreports.common;

/**
 */
public class Enums {

    public static enum ValueType {
        VALUE_TEXT,
        VALUE_DATE_TIME,
        VALUE_NUMERIC;

        private ValueType() {
        }
    }

    public static enum PeriodInterval {
        BEFORE,
        AFTER;

        private PeriodInterval() {
        }
    }

    public static enum Period {

        MONTHLY("MONTH"),
        QUARTERLY("QUARTER"),
        YEARLY("YEAR");

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

    public static enum DataFor {
        DEAD,
        ENCOUNTER,
        APPOINTMENT;

        private DataFor() {
        }
    }
}
