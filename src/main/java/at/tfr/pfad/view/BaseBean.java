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
import javax.validation.Validator;

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
	@Inject
	protected Validator validator;
	@Inject
	protected PageSizeBean pageSize;
	protected int page;
	protected long count;

	public BaseBean() {
		super();
	}

	public boolean isAdmin() {
		return sessionBean.isAdmin();
	}
	
	public boolean isGruppe() {
		return sessionBean.isGruppe();
	}

	public boolean isLeiter() {
		return sessionBean.isLeiter();
	}

	public boolean isKassier() {
		return sessionBean.isKassier();
	}

	public boolean isVorstand() {
		return sessionBean.isVorstand();
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
	
	public int getPageSize() {
		return pageSize.getPageSize();
	}
	
	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public long getCount() {
		return this.count;
	}
}