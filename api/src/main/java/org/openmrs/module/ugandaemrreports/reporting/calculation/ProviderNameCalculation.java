package org.openmrs.module.ugandaemrreports.reporting.calculation;

import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.ugandaemrreports.reporting.cohort.Filters;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ProviderNameCalculation extends AbstractPatientCalculation  {
    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> map, PatientCalculationContext context) {
        CalculationResultMap ret = new CalculationResultMap();
        Set<Integer> male = Filters.alive(cohort, context);
        EncounterService service = Context.getEncounterService();
        EncounterRole role = service.getEncounterRoleByUuid("a0b03050-c99b-11e0-9572-0800200c9a66");
        CalculationResultMap encounter = Calculations.lastEncounter(MetadataUtils.existing(EncounterType.class, "244da86d-f80e-48fe-aba9-067f241905ee"), cohort, context);
        for(Integer ptId: male){
            String provider = "";
            Encounter enc = EmrCalculationUtils.encounterResultForPatient(encounter, ptId);
            if(enc != null){
                Set<EncounterProvider> providerSet = enc.getEncounterProviders();
                for(EncounterProvider encounterProvider:providerSet){
                    if(encounterProvider.getEncounterRole().equals(role)){
                        provider = encounterProvider.getProvider().getPerson().getPersonName().getFullName();
                    }
                }
            }
            ret.put(ptId, new SimpleResult(provider, this));
        }
        return ret;
    }
}
