package org.openmrs.module.ugandaemrreports.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.ugandaemrreports.api.UgandaEMRReportsService;
import org.openmrs.module.ugandaemrreports.api.db.UgandaEMRReportsDAO;

/**
 * It is a default implementation of {@link UgandaEMRReportsService}.
 */
public class UgandaEMRReportsServiceImpl extends BaseOpenmrsService implements UgandaEMRReportsService {

	protected final Log log = LogFactory.getLog(this.getClass());

	private UgandaEMRReportsDAO dao;

	/**
	 * @return the dao
	 */
	public UgandaEMRReportsDAO getDao() {
		return dao;
	}

	/**
	 * @param dao the dao to set
	 */
	public void setDao(UgandaEMRReportsDAO dao) {
		this.dao = dao;
	}
}
