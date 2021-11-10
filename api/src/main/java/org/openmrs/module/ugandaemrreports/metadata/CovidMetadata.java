package org.openmrs.module.ugandaemrreports.metadata;

import org.openmrs.*;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.ugandaemrreports.library.CommonDimensionLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Metadata definitions for the COVID related reporting
 */
@Component("covidMetadata")
public class CovidMetadata extends ReportMetadata {

    @Autowired
    CommonDimensionLibrary commonDimensionLibrary;

    public Concept getHIVCommobiityQuestion() { return getConcept("dce0e02a-30ab-102d-86b0-7a5022ba4115"); }

    public Concept getCommobiityQuestion() { return getConcept("9af7f2e7-9fc2-4e53-81a5-9f76f0676282"); }

    public Concept getHIVAnswer() { return getConcept("dc9ae9d0-30ab-102d-86b0-7a5022ba4115"); }

    public Concept getTBComorbidty() { return getConcept("dc6527eb-30ab-102d-86b0-7a5022ba4115"); }

    public Concept getDiabetesComorbidty() { return getConcept("6c372f79-a43d-49d0-a4de-3859df4e3752"); }

    public Concept getHyperTensionComorbidty() { return getConcept("117399AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"); }



    public List<EncounterType> getIACEncounters() {
        List<EncounterType> l = new ArrayList<EncounterType>();
        l.add(MetadataUtils.existing(EncounterType.class, "38cb2232-30fc-4b1f-8df1-47c795771ee9"));
        return l;
    }
}
