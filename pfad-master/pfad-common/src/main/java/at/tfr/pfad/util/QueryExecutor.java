package at.tfr.pfad.util;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.hibernate.Query;
import org.hibernate.transform.AliasToEntityMapResultTransformer;

import at.tfr.pfad.ConfigurationType;
import at.tfr.pfad.model.Configuration;

@Stateless
public class QueryExecutor implements Serializable {

	@Inject
	private EntityManager em;
	
	public List<Map<String, Object>> execute(Configuration config) {
		return execute(config.getCvalue(), ConfigurationType.nativeQuery.equals(config.getType()));
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> execute(String query, boolean nativeQuery) {
		
		Query q;
		if (nativeQuery) {
			q = em.createNativeQuery(query).unwrap(Query.class);
		} else {
			q = em.createQuery(query).unwrap(Query.class);
		}
		
		q.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
		List<Map<String,Object>> list = q.list();
		
		return list;
	}
	
}
