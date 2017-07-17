package org.openmrs.module.ugandaemrreports.common;

/**
 * Created by carapai on 09/01/2017.
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
        AFTER,
        ON;
        private PeriodInterval() {
        }
    }

    public static enum UgandaEMRJoiner {
        AND,
        OR,
        IN;

        private UgandaEMRJoiner() {
        }
    }

    public static enum Period {

        MONTHLY("MONTH"),
        QUARTERLY("QUARTER"),
        YEARLY("YEAR"),
        WEEKLY("WEEK");

        private final String name;

        private Period(String s) {
            name = s;
        }

        public boolean equalsName(String otherName) {
            return otherName != null && name.equals(otherName);
        }

        public String toString() {
            return this.name;
        }
    }
}
