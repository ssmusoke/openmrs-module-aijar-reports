package org.openmrs.module.ugandaemrreports.definition.data.definition;
import org.openmrs.Person;
import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;

@Caching(
        strategy = ConfigurationPropertyCachingStrategy.class
)
public class PersonUUIDDataDefinition extends BaseDataDefinition implements PatientDataDefinition {
    public static final long serialVersionUID = 1L;

    public PersonUUIDDataDefinition() {
    }

    public Class<?> getDataType() {
        return Person.class;
    }
}

