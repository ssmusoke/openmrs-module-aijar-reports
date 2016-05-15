package org.openmrs.module.aijarreports.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.aijarreports.api.AijarReportsService;
import org.openmrs.module.aijarreports.api.db.AijarReportsDAO;

/**
 * It is a default implementation of {@link AijarReportsService}.
 */
public class AijarReportsServiceImpl extends BaseOpenmrsService implements AijarReportsService {

	protected final Log log = LogFactory.getLog(this.getClass());

	private AijarReportsDAO dao;

	/**
	 * @return the dao
	 */
	public AijarReportsDAO getDao() {
		return dao;
	}

	/**
	 * @param dao the dao to set
	 */
	public void setDao(AijarReportsDAO dao) {
		this.dao = dao;
	}
}
