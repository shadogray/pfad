package at.tfr.pfad.dao;

import java.io.Serializable;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;

import at.tfr.pfad.model.Configuration;

@Repository
public abstract class ConfigurationRepository implements EntityRepository<Configuration, Serializable>{

	public abstract Configuration findOptionalByCkey(String key);
	
}
