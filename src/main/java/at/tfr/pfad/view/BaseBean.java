/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.view;

import java.io.Serializable;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.enterprise.context.Conversation;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.jboss.logging.Logger;

public abstract class BaseBean implements Serializable {

	protected Logger log = Logger.getLogger(getClass());
	
	@Inject
	protected transient Conversation conversation;
	@PersistenceContext(unitName = "pfad", type = PersistenceContextType.EXTENDED)
	protected transient EntityManager entityManager;
	@Resource
	protected SessionContext sessionContext;

	public BaseBean() {
		super();
	}

	public boolean isAdmin() {
		return sessionContext.isCallerInRole(Roles.admin.name());
	}
	
	public boolean isGruppe() {
		return sessionContext.isCallerInRole(Roles.gruppe.name());
	}

	public boolean isLeiter() {
		return sessionContext.isCallerInRole(Roles.leiter.name());
	}

	public SessionContext getSessionContext() {
		return sessionContext;
	}
	
	public abstract boolean isUpdateAllowed();
	public boolean isDeleteAllowed() {
		return isAdmin();
	}
	
}