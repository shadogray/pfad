package at.tfr.pfad.view;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.apache.commons.lang3.StringUtils;

import at.tfr.pfad.model.Activity_;
import at.tfr.pfad.model.Booking;
import at.tfr.pfad.model.Booking_;
import at.tfr.pfad.model.Member_;
import at.tfr.pfad.model.Squad_;

public class BookingDataModel extends DataModel<Booking, BookingUI> {

	public BookingDataModel() {
		uiClass = BookingUI.class;
		entityClass = Booking.class;
	}

	public BookingDataModel(Class<BookingUI> uiClass, Class<Booking> entityClass) {
		super(uiClass, entityClass);
	}

	@Override
	public List<BookingUI> convertToUiBean(List<Booking> list) {
		return list.stream().map(b->new BookingUI(b)).collect(Collectors.toList());
	}
	
	@Override
	protected Predicate createFilterCriteriaForField(final String propertyName, final Object filterValue) {
		if (!(filterValue instanceof String) || StringUtils.isBlank((String)filterValue)) {
			return null;
		}
		switch(propertyName) {
		case "member":
			return cb.or(cb.like(cb.lower(root.join(Booking_.member).get(Member_.name)), "%"+filterValue+"%".toLowerCase()),
					cb.like(cb.lower(root.join(Booking_.member).get(Member_.vorname)), "%"+filterValue+"%".toLowerCase()));
		case "activity":
			return cb.like(cb.lower(root.join(Booking_.activity).get(Activity_.name)), "%"+filterValue+"%".toLowerCase());
		case "squad":
			return cb.like(cb.lower(root.join(Booking_.member).get(Member_.trupp).get(Squad_.name)), "%"+filterValue+"%".toLowerCase());
		}
		return super.createFilterCriteriaForField(propertyName, filterValue);
	}
	
	@Override
	protected Path getPathForOrder(String propertyName) {
		switch(propertyName) {
		case "member":
			return root.join(Booking_.member).get(Member_.name);
		case "activity":
			return root.join(Booking_.activity).get(Activity_.name);
		case "squad":
			return root.join(Booking_.member).get(Member_.trupp).get(Squad_.name);
		}
		return super.getPathForOrder(propertyName);
	}
}
