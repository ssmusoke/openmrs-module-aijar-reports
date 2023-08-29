package org.openmrs.module.ugandaemrreports.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.ugandaemrreports.api.UgandaEMRReportsService;
import org.openmrs.module.ugandaemrreports.api.db.UgandaEMRReportsDAO;
import org.openmrs.module.ugandaemrreports.api.db.hibernate.HibernateUgandaEMRReportsDAO;
import org.openmrs.module.ugandaemrreports.model.DashboardReportObject;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
