package at.tfr.pfad;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;

import org.junit.Test;

import at.tfr.pfad.dao.Beans;
import at.tfr.pfad.model.Member;
import at.tfr.pfad.svc.MemberDao;

public class TestBeanUtils {

	@Test
	public void testBeanCopy() throws Exception {
		
		Member orig = new Member();
		orig.setName("Name");
		orig.setParents(new HashSet<Member>());
		orig.getParents().add(new Member());

		MemberDao clone = Beans.copyProperties(orig, new MemberDao());
		assertEquals("copy failed", orig.getName(), clone.getName());
	}
	
}
