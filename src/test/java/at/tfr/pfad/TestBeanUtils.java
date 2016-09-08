package at.tfr.pfad;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;

import org.junit.Test;

import at.tfr.pfad.dao.Beans;
import at.tfr.pfad.dao.SimpleMember;
import at.tfr.pfad.model.Member;

public class TestBeanUtils {

	@Test
	public void testBeanCopy() throws Exception {
		
		Member orig = new Member();
		orig.setName("Name");
		orig.setParents(new HashSet<Member>());
		orig.getParents().add(new Member());

		SimpleMember clone = Beans.copyProperties(orig, new SimpleMember());
		assertEquals("copy failed", orig.getName(), clone.getName());
	}
	
}
