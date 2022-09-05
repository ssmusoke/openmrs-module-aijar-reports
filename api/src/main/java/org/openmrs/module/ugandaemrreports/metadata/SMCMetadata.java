package org.openmrs.module.ugandaemrreports.metadata;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Metadata definitions for the HIV related reporting
 */
@Component("smcMetadata")
public class SMCMetadata extends ReportMetadata {


    public Concept getCircumcisionDate() {
        return getConcept("46bfb245-35ac-496d-8977-e01bbd165039");
    }

    public List<EncounterType> getSMCEncounterType() {
        List<EncounterType> l = new ArrayList<EncounterType>();
        l.add(MetadataUtils.existing(EncounterType.class, "244da86d-f80e-48fe-aba9-067f241905ee"));
        return l;
    }

    public List<EncounterType> getSMCFollowupEncounterType() {
        List<EncounterType> l = new ArrayList<EncounterType>();
        l.add(MetadataUtils.existing(EncounterType.class, "d0f9e0b7-f336-43bd-bf50-0a7243857fa6"));
        return l;
    }

}
