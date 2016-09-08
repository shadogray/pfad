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
			return Sex.W.equals(sex) ? "WI" : "WÖ";
		case GUSP:
			return Sex.W.equals(sex) ? "GU" : "SP";
		case CAEX:
			return Sex.W.equals(sex) ? "CA" : "EX";
		case RARO:
			return Sex.W.equals(sex) ? "RA" : "RO";
		}
		return "";
	}
}