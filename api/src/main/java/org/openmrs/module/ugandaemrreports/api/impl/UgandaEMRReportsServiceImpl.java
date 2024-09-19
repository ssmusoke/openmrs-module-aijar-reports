package org.openmrs.module.ugandaemrreports.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.logic.op.In;
import org.openmrs.module.ugandaemrreports.api.UgandaEMRReportsService;
import org.openmrs.module.ugandaemrreports.api.db.hibernate.HibernateUgandaEMRReportsDAO;
import org.openmrs.module.ugandaemrreports.model.Dashboard;
import org.openmrs.module.ugandaemrreports.model.DashboardReportObject;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.reporting.ReportObjectWrapper;


import java.util.List;
import java.util.Set;

/**
 * It is a default implementation of {@link UgandaEMRReportsService}.
 */


public class UgandaEMRReportsServiceImpl extends BaseOpenmrsService implements UgandaEMRReportsService {

	protected final Log log = LogFactory.getLog(this.getClass());

	private HibernateUgandaEMRReportsDAO dao;

	/**
	 * @return the dao
	 */
	public HibernateUgandaEMRReportsDAO getDao() {
		return dao;
	}

	/**
	 * @param dao the dao to set
	 */
	public void setDao(HibernateUgandaEMRReportsDAO dao) {
		this.dao = dao;
	}


	@Override
	public List<DashboardReportObject> getAllDashboardReportObjects() throws APIException {
		return  dao.getAllDashboardReportObjects();
	}

	@Override
	public DashboardReportObject getDashboardReportObjectByUUID(String uuid) throws APIException {
		return dao.getDashboardReportObjectByUUID(uuid);
	}

	@Override
	public DashboardReportObject saveDashboardReportObject(DashboardReportObject dashboardReportObject) throws APIException {
		return dao.saveDashboardReportObject(dashboardReportObject);
	}

	@Override
	public DashboardReportObject getDashboardReportObjectById(Integer id) throws APIException {
		return  dao. getDashboardReportObjectById( id);
	}

	@Override
	public Dashboard saveDashboard(Dashboard dashboard) throws APIException {
		return  dao.saveDashboard(dashboard);
	}

	@Override
	public Dashboard getDashboardByUUID(String uniqueId) throws APIException {
		return dao.getDashboardByUUID(uniqueId);
	}

	@Override
	public Dashboard getDashboardById(Integer id) throws APIException {
		return dao.getDashboardById(id);
	}

	@Override
	public List<Dashboard> getAllDashboards() throws APIException {
		return dao.getAllDashboards();
	}

	@Override
	public void executeFlatteningScript() {
		dao.executeFlatteningScript();
	}

	@Override
	public List<ReportObjectWrapper> getPatientSearches(String type) {
		return dao.getReportObjects(type);
	}

	@Override
	public PatientSearch getPatientSearchByUuid(String uuid) {
		return dao.getPatientSearchByUuid(uuid);
	}

	@Override
	public Cohort getPatientCurrentlyInProgram(String programUuid) {
		return dao.getPatientCurrentlyInPrograms(programUuid);
	}

	@Override
	public List<Integer> getObsConceptsFromEncounters(EncounterType encounterType) {
        return dao.getObsConceptsFromEncounters(encounterType);
	}

}
