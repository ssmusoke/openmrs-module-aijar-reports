package org.openmrs.module.ugandaemrreports.definition.data.definition;

import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.ugandaemrreports.common.DeathDate;

/**
 * Created by carapai on 15/09/2016.
 */
public class DeathDateDataDefinition extends BaseDataDefinition implements PersonDataDefinition {

    public static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     */
    public DeathDateDataDefinition() {
        super();
    }

    /**
     * Constructor to populate name only
     */
    public DeathDateDataDefinition(String name) {
        super(name);
    }

    @Override
    public Class<?> getDataType() {
        return DeathDate.class;
    }
}
