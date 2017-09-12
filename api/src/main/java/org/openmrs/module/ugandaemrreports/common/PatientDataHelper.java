package org.openmrs.module.ugandaemrreports.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.ugandaemrreports.metadata.CommonReportMetadata;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 */
public class PatientDataHelper {

    protected Log log = LogFactory.getLog(this.getClass());

    private Map<String, EncounterType> encounterTypeCache = new HashMap<String, EncounterType>();

    private Map<String, RelationshipType> relTypeCache = new HashMap<String, RelationshipType>();

    private CommonReportMetadata commonMetadata = new CommonReportMetadata();

    private HIVMetadata hivMetadata = new HIVMetadata();


    // Data Set Utilities

    public void addCol(DataSetRow row, String label, Object value) {
        if (value == null) {
            value = "";
        }
        DataSetColumn c = new DataSetColumn(label, label, value.getClass());
        row.addColumnValue(c, value);
    }

    // Demographics

    public String getGivenName(Patient p) {
        return p.getGivenName();
    }

    public String getFamilyName(Patient p) {
        return p.getFamilyName();
    }

    public String getBirthDate(Patient p) {
        return formatYmd(p.getBirthdate());
    }

    public String getGender(Patient p) {
        return p.getGender();
    }

    public String formatYmd(Date d) {
        if (d == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(d);
    }

    public List<Encounter> getEncounters(Patient p, Map<String, Object> args) {

        EncounterService es = Context.getEncounterService();
        Location loc = null;
        Date fromDate = null;
        Date toDate = null;
        Collection<Form> enteredViaForms = null;
        Collection<EncounterType> encounterTypes = null;
        Collection<Provider> providers = null;
        Collection<VisitType> visitTypes = null;
        Collection<Visit> visits = null;

        Boolean sort = null;
        Integer howMany = null;

        if (args.containsKey("loc")) {
            loc = (Location) args.get("loc");
        }

        if (args.containsKey("fromDate")) {
            fromDate = (Date) args.get("fromDate");
        }

        if (args.containsKey("toDate")) {
            toDate = (Date) args.get("toDate");
        }

        if (args.containsKey("enteredViaForms")) {
            enteredViaForms = (List<Form>) args.get("enteredViaForms");
        }

        if (args.containsKey("encounterTypes")) {
            encounterTypes = (List<EncounterType>) args.get("encounterTypes");
        }

        if (args.containsKey("providers")) {
            providers = (List<Provider>) args.get("providers");
        }

        if (args.containsKey("visitTypes")) {
            visitTypes = (List<VisitType>) args.get("visitTypes");
        }

        if (args.containsKey("visits")) {
            visits = (List<Visit>) args.get("visits");
        }

        if (args.containsKey("howMany")) {
            howMany = (Integer) args.get("howMany");
        }

        if (args.containsKey("sort")) {
            sort = (Boolean) args.get("sort");
        }

        List<Encounter> encounters = es.getEncounters(p, loc, fromDate, toDate, enteredViaForms, encounterTypes, providers, visitTypes, visits, false);

        if (sort) {
            Map<Date, Encounter> m = new TreeMap<Date, Encounter>();
            for (Encounter encounter : encounters) {
                m.put(encounter.getEncounterDatetime(), encounter);
            }
            if (m.isEmpty()) {
                return null;
            }
            if (howMany != null) {
                return Helper.slice(new ArrayList<Encounter>(m.values()), 0, howMany);
            }
            return new ArrayList<Encounter>(m.values());
        } else {
            if (howMany != null) {
                return Helper.slice(encounters, 0, howMany);
            }
            return encounters;
        }
    }

    public List<Obs> getObs(Patient p, String concept, Map<String, Object> args, List<Encounter> encounters) {

        List<Concept> answers = null;
        List<String> sort = null;
        Integer mostRecentN = null;
        Integer obsGroupId = null;
        Date fromDate = null;
        Date toDate = null;

        if (args.containsKey("answers")) {
            answers = (List<Concept>) args.get("answers");
        }

        if (args.containsKey("sort")) {
            sort = (List<String>) args.get("sort");
        }

        if (args.containsKey("mostRecentN")) {
            mostRecentN = (Integer) args.get("mostRecentN");
        }

        if (args.containsKey("obsGroupId")) {
            obsGroupId = (Integer) args.get("obsGroupId");
        }

        if (args.containsKey("fromDate")) {
            fromDate = (Date) args.get("fromDate");
        }

        if (args.containsKey("toDate")) {
            toDate = (Date) args.get("toDate");
        }

        List<Concept> questions = commonMetadata.getConceptList(concept);

        ObsService os = Context.getObsService();

        List<Obs> l = os.getObservations(Arrays.asList((Person) p), encounters, questions, answers, null, null, sort, mostRecentN, obsGroupId, fromDate, toDate, false);
        if (l.isEmpty()) {
            return null;
        }
        return l;
    }

    private List<Encounter> getSummaryPages(Patient p) {
        Map<String, Object> args = new HashMap<String, Object>();

        args.put("encounterTypes", Arrays.asList(hivMetadata.getARTSummaryEncounter()));
        args.put("sort", Boolean.TRUE);
        args.put("howMany", new Integer(1));
        return getEncounters(p, args);
    }

    public HashMap<Integer, Date> getBaselineClinicalStage(Patient p) {
        HashMap<Integer, Date> baselineClinicalStage = new HashMap<Integer, Date>();
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("sort", Arrays.asList("obsDatetime ASC"));
        args.put("answers", hivMetadata.getBaselineClinicalStages());
        List<Encounter> encounters = getSummaryPages(p);
        List<Obs> obs = getObs(p, "99083", args, encounters);

        Obs o = getFirstObs(obs);
        if (o != null) {
            Integer conceptId = o.getValueCoded().getConceptId();
            Date date = o.getObsDatetime();

            HashMap<Integer, Integer> concepts = new HashMap<Integer, Integer>();
            concepts.put(1204, 1);
            concepts.put(1205, 2);
            concepts.put(1206, 3);
            concepts.put(1207, 4);

            if (conceptId != null) {
                baselineClinicalStage.put(concepts.get(conceptId), date);
            }
        }
        return baselineClinicalStage;
    }

    private List<Obs> sortObs(List<Obs> obs) {
        TreeMap<Date, Obs> emptyMap = new TreeMap<Date, Obs>();
        if (obs != null) {
            if (obs.size() > 0) {
                for (Obs o : obs) {
                    emptyMap.put(o.getObsDatetime(), o);
                }
            }
        }

        return new ArrayList<Obs>(emptyMap.values());
    }

    private Obs getFirstObs(List<Obs> obs) {
        TreeMap<Date, Obs> emptyMap = new TreeMap<Date, Obs>();
        if (obs != null) {
            if (obs.size() > 0) {
                for (Obs o : obs) {
                    emptyMap.put(o.getObsDatetime(), o);
                }
            }
        }
        if (emptyMap.size() > 0) {
            return emptyMap.entrySet().iterator().next().getValue();
        }
        return null;
    }

    private String h(String s) {
        return ("".equals(s) || s == null ? "&nbsp;" : s);
    }
}
