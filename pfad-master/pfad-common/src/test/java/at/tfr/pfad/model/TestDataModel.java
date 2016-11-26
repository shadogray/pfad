package at.tfr.pfad.model;

import javax.inject.Inject;

import org.ajax4jsf.model.SequenceRange;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import at.tfr.pfad.model.BookingDataModel;

@RunWith(CdiTestRunner.class)
public class TestDataModel {

	@Inject
	private BookingDataModel bdm;
	
	@Test
	public void testLoadData() throws Exception {
		
		bdm.getRowData(new SequenceRange(0, 10));
		
	}
	
}
