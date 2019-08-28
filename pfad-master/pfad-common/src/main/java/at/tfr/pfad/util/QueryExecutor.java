package at.tfr.pfad.util;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.hibernate.Query;
import org.hibernate.transform.BasicTransformerAdapter;

import at.tfr.pfad.ConfigurationType;
import at.tfr.pfad.dao.ConfigurationRepository;
import at.tfr.pfad.model.Configuration;

@Stateless
public class QueryExecutor implements Serializable {

	@Inject
	private EntityManager em;
	@Inject
	private ConfigurationRepository configRepo;
	private String securityFilter = "(?)password";
	
	@PostConstruct
	private void init() {
		securityFilter = configRepo.getValue("querySecurityFilter", securityFilter);
	}
	
	public List<List<Entry<String, Object>>> execute(Configuration config) {
		return execute(config.getCvalue(), ConfigurationType.nativeQuery.equals(config.getType()));
	}
	
	@SuppressWarnings("unchecked")
	public List<List<Entry<String, Object>>> execute(String query, boolean nativeQuery) {
		
		if (query != null && query.matches("(?)password")) {
			throw new SecurityException("security check failed");
		}
		Query q;
		if (nativeQuery) {
			q = em.createNativeQuery(query).unwrap(Query.class);
		} else {
			q = em.createQuery(query).unwrap(Query.class);
		}
		
		q.setResultTransformer(new AliasTransformer());
		List<List<Entry<String,Object>>> list = q.list();
		
		return list;
	}
	
	class AliasTransformer extends BasicTransformerAdapter {
		@Override
		public Object transformTuple(Object[] tuple, String[] aliases) {
			Map<String,Object> result = new LinkedHashMap<>(tuple.length);
			for ( int i=0; i<tuple.length; i++ ) {
				String alias = aliases[i];
				result.put( alias != null ? alias : Integer.toString(i), tuple[i] );
			}
			return result.entrySet().stream().collect(Collectors.toList());
		}
	}
}
