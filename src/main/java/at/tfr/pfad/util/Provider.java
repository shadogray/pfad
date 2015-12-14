/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad.util;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

public class Provider {

	@PersistenceUnit(unitName = "pfad")
	private EntityManagerFactory entityManagerFactory;

	@Produces
	@RequestScoped
	public EntityManager getEntityManager() {
		return entityManagerFactory.createEntityManager();
	}
	
}
