package at.tfr.pfad.dao;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;

import at.tfr.pfad.model.Payment;

@Repository
public abstract class PaymentRepository implements EntityRepository<Payment, Long>{

}
