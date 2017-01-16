package at.tfr.pfad.model;

import java.util.Collections;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class TestDataModel {

	@Inject
	private BookingDataModel bdm;
	
	@Test
	public void testLoadData() throws Exception {
		
		bdm.load(0, 10, Collections.emptyList(), Collections.emptyMap());
		
	}
	
}
