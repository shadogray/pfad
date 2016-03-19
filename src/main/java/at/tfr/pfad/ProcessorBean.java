package at.tfr.pfad;

import java.io.Serializable;
import java.net.URL;

import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.Schedule;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.jboss.logging.Logger;
import org.joda.time.DateTime;

@Named
@Singleton
@Remote
public class ProcessorBean implements Serializable {

	@PersistenceContext(unitName = "pfad")
	private EntityManager entityManager;
	private Logger log = Logger.getLogger(getClass());
	@Resource
	private SessionContext sessionContext;

	@Schedule(persistent = false, hour = "*", minute = "0", second = "0")
	public void doBackup() {
		try {
			URL testUrl = Thread.currentThread().getContextClassLoader().getResource("/test.properties");
			if (testUrl == null) {
				String backupName = "pfad_" + new DateTime().toString("yyyy.MM.dd_HH") + ".zip";
				int result = entityManager.createNativeQuery("backup to '" + backupName + "';").executeUpdate();
				log.info("executed Backup to: " + backupName + ", result=" + result);
			}
		} catch (Throwable e) {
			log.warn("cannot execute backup: " + e, e);
		}
	}

	public SessionContext getSessionContext() {
		return sessionContext;
	}
}
