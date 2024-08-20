package org.openmrs.module.ugandaemrreports.api.db.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.Condition;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.Cohort;
import org.openmrs.module.ugandaemrreports.api.db.UgandaEMRReportsDAO;
import org.openmrs.module.ugandaemrreports.model.Dashboard;
import org.openmrs.module.ugandaemrreports.model.DashboardReportObject;
import org.openmrs.report.ReportConstants;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.reporting.PatientSearchReportObject;
import org.openmrs.reporting.ReportObjectWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 */

@Repository("ugandaemrreports.HibernateUgandaEMRReportsDAO")
public class HibernateUgandaEMRReportsDAO implements UgandaEMRReportsDAO {

	protected final Log log = LogFactory.getLog(this.getClass());

	@Autowired
	DbSessionFactory sessionFactory;

	/**
	 * @return the sessionFactory
	 */
	private DbSession getSession() {
		return sessionFactory.getCurrentSession();
	}

	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public List<DashboardReportObject> getAllDashboardReportObjects() {
		return (List<DashboardReportObject>) getSession().createCriteria(DashboardReportObject.class).list();
	}

	/**
	 * @see org.openmrs.module.ugandaemrreports.api.UgandaEMRReportsService#saveDashboardReportObject(DashboardReportObject) (org.openmrs.module.ugandaemrreports.model.DashboardReportObject)
	 */
	public DashboardReportObject getDashboardReportObjectByUUID(String uuid) {
		return (DashboardReportObject) getSession().createCriteria(DashboardReportObject.class).add(Restrictions.eq("uuid", uuid))
				.uniqueResult();
	}

	/**
	 * @see org.openmrs.module.ugandaemrreports.api.UgandaEMRReportsService#saveDashboardReportObject(DashboardReportObject) (org.openmrs.module.ugandaemrrepots.model.DashboardReportObject)
	 */
	public DashboardReportObject saveDashboardReportObject(DashboardReportObject dashboardReportObject) {
		getSession().saveOrUpdate(dashboardReportObject);
		return dashboardReportObject;
	}

	public DashboardReportObject getDashboardReportObjectById(Integer id) {
		return (DashboardReportObject) getSession().createCriteria(DashboardReportObject.class).add(Restrictions.eq("dashboard_report_id", id))
				.uniqueResult();
	}



	public List<Dashboard> getAllDashboards() {
		return (List<Dashboard>) getSession().createCriteria(Dashboard.class).list();
	}

	/**
	 * @see org.openmrs.module.ugandaemrreports.api.UgandaEMRReportsService#saveDashboard(Dashboard) (org.openmrs.module.ugandaemrreports.model.Dashboard)
	 */
	public Dashboard getDashboardByUUID(String uuid) {
		return (Dashboard) getSession().createCriteria(Dashboard.class).add(Restrictions.eq("uuid", uuid))
				.uniqueResult();
	}

	/**
	 * @see org.openmrs.module.ugandaemrreports.api.UgandaEMRReportsService#saveDashboard(Dashboard) (org.openmrs.module.ugandaemrrepots.model.Dashboard)
	 */
	public Dashboard saveDashboard(Dashboard dashboard) {
		getSession().saveOrUpdate(dashboard);
		return dashboard;
	}

	public Dashboard getDashboardById(Integer id) {
		return (Dashboard) getSession().createCriteria(Dashboard.class).add(Restrictions.eq("dashboard_id", id))
				.uniqueResult();
	}


	@Override
	public void executeFlatteningScript() {
		sessionFactory.getCurrentSession().createSQLQuery("CALL sp_mamba_data_processing_etl()").executeUpdate();

	}

	@Override
	public List<ReportObjectWrapper> getReportObjects(String type) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ReportObjectWrapper.class);
		criteria.add(Restrictions.eq("type", type));
		criteria.add(Restrictions.eq("voided", false));
        return (List<ReportObjectWrapper>) criteria.list();
	}

	@Override
	public PatientSearch getPatientSearchByUuid(String uuid) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ReportObjectWrapper.class);
		criteria.add(Restrictions.eq("type", ReportConstants.REPORT_OBJECT_TYPE_PATIENTSEARCH));
		criteria.add(Restrictions.eq("uuid", uuid));
		criteria.add(Restrictions.eq("voided", false));
		ReportObjectWrapper wrapper =(ReportObjectWrapper) criteria.uniqueResult();
		AbstractReportObject abstractReportObject = wrapper.getReportObject();
		if (abstractReportObject.getReportObjectId() == null) {
			abstractReportObject.setReportObjectId(wrapper.getReportObjectId());
		}

        return ((PatientSearchReportObject) abstractReportObject).getPatientSearch();

	}

	@Override
	public Cohort getPatientCurrentlyInPrograms(String programUuid) {
		String sb =String.format("SELECT  p.patient_id\n" +
				"FROM patient p\n" +
				"         INNER JOIN patient_program pp ON p.patient_id = pp.patient_id\n" +
				"         INNER JOIN program prog ON pp.program_id = prog.program_id\n" +
				"WHERE prog.uuid = '%s'\n" +
				"  AND pp.date_completed IS NULL",programUuid);

		log.debug("query: " + sb);
		Query query = sessionFactory.getCurrentSession().createSQLQuery(sb.toString());
		return new Cohort(query.list());
	}

	@Override
	public Map<Integer, String> getPatientsConditionsStatus(org.openmrs.cohort.Cohort patients, Concept codedCondition) {
		Map<Integer, String> ret = new HashMap<Integer, String>();


		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Condition.class);
		criteria.setCacheMode(CacheMode.IGNORE);


		// only restrict on patient ids if some were passed in
		if (patients != null)
			criteria.add(Restrictions.in("patient.personId", patients.getMemberIds()));


		criteria.add(Expression.eq("condition.coded", codedCondition));
		criteria.add(Expression.eq("voided", false));

		criteria.addOrder(org.hibernate.criterion.Order.desc("onsetDate"));
		long start = System.currentTimeMillis();
		List<Condition> conditions = criteria.list();


		log.debug("Took: " + (System.currentTimeMillis() - start) + " ms to run the patient/obs query");

		// set up the return map
		for (Condition c : conditions) {
			int ptId = c.getPatient().getPatientId();

			String status = c.getClinicalStatus().toString();
			ret.put(ptId,status);
		}


		return ret;
	}

	@Override
	public Set<Concept> getAllConditions() {
		Set<Concept> ret = new HashSet<>();
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Condition.class);
		criteria.setCacheMode(CacheMode.IGNORE);
		criteria.add(Expression.eq("voided", false));


		long start = System.currentTimeMillis();
		List<Condition> conditions = criteria.list();
		log.debug("Took: " + (System.currentTimeMillis() - start) + " ms to run the patient/obs query");

		// set up the return map
		for (Condition c : conditions) {
			Concept concept = c.getCondition().getCoded();

			if(concept !=null){
				ret.add(concept);
			}

		}
		return ret;
	}


	@Override
	public Map<Integer,Object> getLatestPatientAppointmentsScheduled(org.openmrs.cohort.Cohort patients, int limit){
		Map<Integer, Object> ret = new HashMap<Integer, Object>();
		Query query = sessionFactory.getCurrentSession().createSQLQuery(
				"select patient_id, start_date_time from patient_appointment where voided = false and patient_id in (:patientIds) order by start_date_time DESC ");

		if (!patients.getMemberIds().isEmpty())
			query.setParameterList("patientIds", patients.getMemberIds());
		query.setCacheMode(CacheMode.IGNORE);

		List<Object[]> temp = query.list();

		long now = System.currentTimeMillis();
		for (Object[] results : temp) {
			Integer ptId = (Integer) results[0];
			Object apptDate = results[1];

			if (!ret.containsKey(ptId))
				ret.put(ptId, apptDate);
		}
		return ret;
	}
}
