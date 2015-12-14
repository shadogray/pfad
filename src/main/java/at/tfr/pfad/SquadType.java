/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package at.tfr.pfad;

public enum SquadType {
	WIWO, GUSP, CAEX, RARO;

	public String getKey(Sex sex) {
		switch (this) {
		case WIWO:
			return sex == Sex.W ? "WI" : "WO";
		case GUSP:
			return sex == Sex.W ? "GU" : "SP";
		case CAEX:
			return sex == Sex.W ? "CA" : "EX";
		case RARO:
			return sex == Sex.W ? "RA" : "RO";
		}
		return "";
	}
}