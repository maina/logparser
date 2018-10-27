package com.wallethub.logparser;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import com.wallethub.logparser.model.AccessLog;

public class DbHelper {
	private static EntityManagerFactory entityManagerFactory;
	private static EntityManager entityManager;

	private static EntityManagerFactory getEntityManagerFactoryInstance() {
		if (entityManagerFactory == null) {
			entityManagerFactory = Persistence.createEntityManagerFactory("com.wallethub.logparser");
		}
		return entityManagerFactory;
	}

	private static EntityManager getEntityManager() {

		if (entityManager == null) {
			entityManager = getEntityManagerFactoryInstance().createEntityManager();
		}

		return entityManager;

	}

	/**
	 * persist logs to db
	 * 
	 * @param accessLogs
	 */
	public static void saveAccessLogs(List<AccessLog> accessLogs) {
		try {
			if (accessLogs != null && !accessLogs.isEmpty()) {
				getEntityManager().getTransaction().begin();

				if (accessLogs.size() == 1) {
					getEntityManager().persist(accessLogs.get(0));
				} else {

					for (AccessLog log : accessLogs) {
						log.setLastUpdatedAt(new Date());
						getEntityManager().persist(log);
					}

				}
				getEntityManager().getTransaction().commit();

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			getEntityManager().close();

		}
	}

	public static void selectLogsByIp(String ip) {
		String query = "select ip from accesslog where ip='" + ip + "'";
	}

	/**
	 * Check if a file has been processed already in a simple way though- based on
	 * path
	 * 
	 * @param path
	 * @return
	 */
	public static boolean checkifFileAlreadyProcessed(String path) {
		List<AccessLog> result = getEntityManager().createNativeQuery("select * from ACCESSLOG").getResultList();
		return result != null && !result.isEmpty();
	}

	public static List<Object[]> selectLogsBetweenDates(String startDate, String endDate, Integer threshold) {

		String query = "select a.*, CASE WHEN total > 200 THEN 'Hourly limit exceeded' WHEN total >500 THEN 'Dialy limit exceeded' ELSE '' END as comment from (SELECT ip,count(ip) as total FROM accesslog  where LOGDATE BETWEEN '"
				+ startDate + "' AND '" + endDate + "' group by ip) a  where total>" + threshold + ";";
		EntityTransaction et = getEntityManager().getTransaction();
		et.begin();
		List<Object[]> result = getEntityManager().createNativeQuery(query).getResultList();

		// insert into blocked ip tables
		String insert = "INSERT INTO blocked_ips (ip,total,comment) " + query;
		getEntityManager().createNativeQuery(insert).executeUpdate();
		et.commit();
		return result;
	}

}
