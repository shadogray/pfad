package at.tfr.pfad;

import java.io.Serializable;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.logging.Logger;
import org.joda.time.DateTime;

@Singleton
public class ProcessorBean implements Serializable {

	@Inject
	private EntityManager entityManager;
	private Logger log = Logger.getLogger(getClass());

	@Schedule(hour = "*", minute = "0", second = "0")
	public void doBackup() {
		try {
			String backupName = "pfad_" + new DateTime().toString("yyyy.mm.dd_HH") + ".zip";
			int result = entityManager.createNativeQuery("backup to '" + backupName + "';").executeUpdate();
			log.info("executed Backup to: " + backupName + ", result=" + result);
		} catch (Throwable e) {
			log.warn("cannot execute backup: " + e, e);
		}
	}

}
