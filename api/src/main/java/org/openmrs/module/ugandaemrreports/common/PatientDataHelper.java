package org.openmrs.module.ugandaemrreports.common;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.RelationshipType;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrreports.metadata.CommonReportMetadata;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;

/**
 * Created by carapai on 11/05/2016.
 */
public class PatientDataHelper {

	protected Log log = LogFactory.getLog(this.getClass());

	private Map<String, EncounterType> encounterTypeCache = new HashMap<String, EncounterType>();

	private Map<String, RelationshipType> relTypeCache = new HashMap<String, RelationshipType>();

	private CommonReportMetadata commonMetadata = new CommonReportMetadata();

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

	public String getBirthdate(Patient p) {
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

	public Obs getLatestObs(Patient p, String concept, List<EncounterType> onlyInEncountersOfType, Date endDate) {
		Concept c = commonMetadata.getConcept(concept);
		List<Encounter> encounters = null;
		if (onlyInEncountersOfType != null) {
			EncounterService es = Context.getEncounterService();
			encounters = es.getEncounters(p, null, null, endDate, null, onlyInEncountersOfType, null, false);
		}
		ObsService os = Context.getObsService();
		List<Obs> l = os.getObservations(Arrays.asList((Person) p), encounters, Arrays.asList(c), null, null, null, null, 1,
				null, null, endDate, false);
		if (l.isEmpty()) {
			return null;
		}
		return l.get(0);
	}

	public Obs getEarliestObs(Patient p, String concept, List<EncounterType> onlyInEncountersOfType, Date endDate) {
		Concept c = commonMetadata.getConcept(concept);
		List<Encounter> encs = null;
		if (onlyInEncountersOfType != null) {
			encs = Context.getEncounterService().getEncounters(p, null, null, endDate, null, onlyInEncountersOfType, null,
					false);
		}
		ObsService os = Context.getObsService();
		List<Obs> l = os.getObservations(Arrays.asList((Person) p), encs, Arrays.asList(c), null, null, null, null, null,
				null, null, endDate, false);
		Map<Date, Obs> m = new TreeMap<Date, Obs>();
		for (Obs o : l) {
			m.put(o.getObsDatetime(), o);
		}
		if (m.isEmpty()) {
			return null;
		}
		return m.values().iterator().next();
	}
}
