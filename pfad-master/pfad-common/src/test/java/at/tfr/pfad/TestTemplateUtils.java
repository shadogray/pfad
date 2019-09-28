package at.tfr.pfad;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import at.tfr.pfad.model.Squad;
import at.tfr.pfad.util.TemplateUtils;
import junit.framework.Assert;

@RunWith(CdiTestRunner.class)
public class TestTemplateUtils {

	Squad trupp = new Squad();
	{
		trupp.setName("TestTrupp");
	}
	
	@Inject
	private TemplateUtils templateUtils;
	
	@Test
	public void testReplaceMap() {
		Map<String,Object> beans = new HashMap<>();
		beans.put("trupp", trupp);
		beans.put("NULL", null);
		
		String res = templateUtils.replace("${trupp.name}", beans);
		Assert.assertEquals("replace failed: "+res, trupp.getName(), res);
		
		res = templateUtils.replace("${truppX.name}", beans);
		Assert.assertNull("replace failed: "+res, res);

		res = templateUtils.replace("${truppX.name}", beans, "DEFAULT");
		Assert.assertEquals("replace failed: "+res, "DEFAULT", res);

		res = templateUtils.replace("${NULL}", beans, "DEFAULT");
		Assert.assertEquals("replace failed: "+res, "null", res);
		
		res = templateUtils.replace("${NULL.name}", beans, "DEFAULT");
		Assert.assertEquals("replace failed: "+res, "DEFAULT", res);
	}

}
