package at.tfr.pfad;

import java.io.Serializable;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.logging.Logger;
import org.joda.time.DateTime;

@Singleton
public class ProcessorBean implements Serializable {

	@PersistenceContext(unitName = "pfad")
	private EntityManager entityManager;
	private Logger log = Logger.getLogger(getClass());

	@Schedule(persistent = false, hour = "*", minute = "0", second = "0")
	public void doBackup() {
		try {
			String backupName = "pfad_" + new DateTime().toString("yyyy.MM.dd_HH") + ".zip";
			int result = entityManager.createNativeQuery("backup to '" + backupName + "';").executeUpdate();
			log.info("executed Backup to: " + backupName + ", result=" + result);
		} catch (Throwable e) {
			log.warn("cannot execute backup: " + e, e);
		}
	}

}
