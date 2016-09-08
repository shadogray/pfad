package at.tfr.pfad.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;

import at.tfr.pfad.dao.ActivityRepository;
import at.tfr.pfad.dao.BookingRepository;
import at.tfr.pfad.dao.MemberRepository;
import at.tfr.pfad.dao.PaymentRepository;

public class EndpointBase<T> {

	@Inject
	protected EntityManager em;
	@Inject
	protected MemberRepository memberRepo;
	@Inject
	protected PaymentRepository paymentRepo;
	@Inject
	protected BookingRepository bookingRepo;
	@Inject
	protected ActivityRepository activityRepo;

	public Map<String, Object> createHints(EntityManager em, @SuppressWarnings("rawtypes") Class<T> rootType, String... nodes) {
		EntityGraph<T> graph = em.createEntityGraph(rootType);
		Stream.of(nodes).forEach((node) -> graph.addAttributeNodes(node));
		return createHints(em, graph);
	}

	public static Map<String, Object> createHints(EntityManager em, String graphName, String... nodes) {
		@SuppressWarnings("rawtypes")
		EntityGraph graph = em.createEntityGraph(graphName);
		Stream.of(nodes).forEach((node) -> graph.addAttributeNodes(node));
		return createHints(em, graph);
	}

	public static Map<String, Object> createHints(EntityManager em, @SuppressWarnings("rawtypes") EntityGraph graph) {
		Map<String, Object> hints = new HashMap<>();
		hints.put("javax.persistence.fetchgraph", graph);
		return hints;
}	
}
