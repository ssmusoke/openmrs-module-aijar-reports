package org.openmrs.module.ugandaemrreports.common;

/**
 * Created by carapai on 09/01/2017.
 */
public class Enums {

    public static enum ValueType {
        VALUE_TEXT,
        VALUE_DATE_TIME,
        VALUE_NUMERIC;

        ValueType() {
        }
    }

    public static enum PeriodInterval {
        BEFORE,
        AFTER,
        ON;

        PeriodInterval() {
        }
    }

    public static enum UgandaEMRJoiner {
        AND,
        OR,
        IN;
    }

    public static enum Period {

        MONTHLY("MONTH"),
        QUARTERLY("QUARTER"),
        YEARLY("YEAR"),
        WEEKLY("WEEK");

        private final String name;

        Period(String s) {
            name = s;
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
