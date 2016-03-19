package at.tfr.pfad;

import java.io.Serializable;

import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.Schedule;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.logging.Logger;
import org.joda.time.DateTime;

import at.tfr.pfad.dao.ConfigurationRepository;
import at.tfr.pfad.model.Configuration;

@Named
@Singleton
@Remote
public class ProcessorBean implements Serializable {

	@PersistenceContext(unitName = "pfad")
	private EntityManager entityManager;
	private Logger log = Logger.getLogger(getClass());
	@Resource
	private SessionContext sessionContext;
	@Inject
	private ConfigurationRepository configRepo;

	@Schedule(persistent = false, hour = "*", minute = "*", second = "*/5")
	public void doBackup() {
		try {
			Configuration test = configRepo.findOptionalByCkey("test");
			if (test == null || !Boolean.valueOf(test.getCvalue())) {
				String backupName = "pfad_" + new DateTime().toString("yyyy.MM.dd_HH") + ".zip";
				int result = entityManager.createNativeQuery("backup to '" + backupName + "';").executeUpdate();
				log.info("executed Backup to: " + backupName + ", result=" + result);
			} else {
				log.info("Test-Mode, not dumping DB: " + test);
			}
		} catch (Throwable e) {
			log.warn("cannot execute backup: " + e, e);
		}
	}

	public SessionContext getSessionContext() {
		return sessionContext;
	}
}
