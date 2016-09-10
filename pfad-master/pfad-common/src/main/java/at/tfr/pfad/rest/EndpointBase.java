package at.tfr.pfad.rest;

import javax.inject.Inject;
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

}
