/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.view;

import java.io.Serializable;
import java.util.Date;

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
	@Inject
	protected SessionBean sessionBean;

	public BaseBean() {
		super();
	}

	public boolean isAdmin() {
		return sessionContext.isCallerInRole(Role.admin.name());
	}
	
	public boolean isGruppe() {
		return sessionContext.isCallerInRole(Role.gruppe.name());
	}

	public boolean isLeiter() {
		return sessionContext.isCallerInRole(Role.leiter.name());
	}

	public boolean isKassier() {
		return sessionContext.isCallerInRole(Role.kassier.name());
	}

	public boolean isVorstand() {
		return sessionContext.isCallerInRole(Role.vorstand.name());
	}

	public SessionContext getSessionContext() {
		return sessionContext;
	}
	
	public boolean isViewAllowed() {
		return true;
	}
	public abstract boolean isUpdateAllowed();
	public boolean isDeleteAllowed() {
		return isAdmin();
	}
	
	public boolean isRegistrationEnd() {
		return sessionBean.getRegistrationEndDate() != null && sessionBean.getRegistrationEndDate().before(new Date());
	}
	
	public Conversation getConversation() {
		return conversation;
	}
}