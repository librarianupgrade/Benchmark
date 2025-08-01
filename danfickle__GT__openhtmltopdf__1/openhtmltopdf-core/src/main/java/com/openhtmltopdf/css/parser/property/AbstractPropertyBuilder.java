/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package com.openhtmltopdf.css.parser.property;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.w3c.dom.css.CSSPrimitiveValue;

import com.openhtmltopdf.css.constants.CSSName;
import com.openhtmltopdf.css.constants.IdentValue;
import com.openhtmltopdf.css.parser.CSSParseException;
import com.openhtmltopdf.css.parser.PropertyValue;
import com.openhtmltopdf.css.sheet.PropertyDeclaration;

public abstract class AbstractPropertyBuilder implements PropertyBuilder {
	public List buildDeclarations(CSSName cssName, List values, int origin, boolean important) {
		return buildDeclarations(cssName, values, origin, important, true);
	}

	protected void checkValueCount(CSSName cssName, int expected, int found) {
		if (expected != found) {
			throw new CSSParseException(
					"Found " + found + " value(s) for " + cssName + " when " + expected + " value(s) were expected",
					-1);
		}
	}

	protected void checkValueCount(CSSName cssName, int min, int max, int found) {
		if (!(found >= min && found <= max)) {
			throw new CSSParseException("Found " + found + " value(s) for " + cssName + " when between " + min + " and "
					+ max + " value(s) were expected", -1);
		}
	}

	protected void checkIdentType(CSSName cssName, CSSPrimitiveValue value) {
		if (value.getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT) {
			throw new CSSParseException("Value for " + cssName + " must be an identifier", -1);
		}
	}

	protected void checkIdentOrURIType(CSSName cssName, CSSPrimitiveValue value) {
		int type = value.getPrimitiveType();
		if (type != CSSPrimitiveValue.CSS_IDENT && type != CSSPrimitiveValue.CSS_URI) {
			throw new CSSParseException("Value for " + cssName + " must be an identifier or a URI", -1);
		}
	}

	protected void checkIdentOrColorType(CSSName cssName, CSSPrimitiveValue value) {
		int type = value.getPrimitiveType();
		if (type != CSSPrimitiveValue.CSS_IDENT && type != CSSPrimitiveValue.CSS_RGBCOLOR) {
			throw new CSSParseException("Value for " + cssName + " must be an identifier or a color", -1);
		}
	}

	protected void checkIdentOrIntegerType(CSSName cssName, CSSPrimitiveValue value) {
		int type = value.getPrimitiveType();
		if ((type != CSSPrimitiveValue.CSS_IDENT && type != CSSPrimitiveValue.CSS_NUMBER)
				|| (type == CSSPrimitiveValue.CSS_NUMBER
						&& (int) value.getFloatValue(CSSPrimitiveValue.CSS_NUMBER) != Math
								.round(value.getFloatValue(CSSPrimitiveValue.CSS_NUMBER)))) {
			throw new CSSParseException("Value for " + cssName + " must be an identifier or an integer", -1);
		}
	}

	protected void checkInteger(CSSName cssName, CSSPrimitiveValue value) {
		int type = value.getPrimitiveType();
		if (type != CSSPrimitiveValue.CSS_NUMBER || (type == CSSPrimitiveValue.CSS_NUMBER
				&& (int) value.getFloatValue(CSSPrimitiveValue.CSS_NUMBER) != Math
						.round(value.getFloatValue(CSSPrimitiveValue.CSS_NUMBER)))) {
			throw new CSSParseException("Value for " + cssName + " must be an integer", -1);
		}
	}

	protected void checkIdentOrLengthType(CSSName cssName, CSSPrimitiveValue value) {
		int type = value.getPrimitiveType();
		if (type != CSSPrimitiveValue.CSS_IDENT && !isLength(value)) {
			throw new CSSParseException("Value for " + cssName + " must be an identifier or a length", -1);
		}
	}

	protected void checkIdentOrNumberType(CSSName cssName, CSSPrimitiveValue value) {
		int type = value.getPrimitiveType();
		if (type != CSSPrimitiveValue.CSS_IDENT && type != CSSPrimitiveValue.CSS_NUMBER) {
			throw new CSSParseException("Value for " + cssName + " must be an identifier or a length", -1);
		}
	}

	protected void checkIdentLengthOrPercentType(CSSName cssName, CSSPrimitiveValue value) {
		int type = value.getPrimitiveType();
		if (type != CSSPrimitiveValue.CSS_IDENT && !isLength(value) && type != CSSPrimitiveValue.CSS_PERCENTAGE) {
			throw new CSSParseException("Value for " + cssName + " must be an identifier, length, or percentage", -1);
		}
	}

	protected void checkLengthOrPercentType(CSSName cssName, CSSPrimitiveValue value) {
		int type = value.getPrimitiveType();
		if (!isLength(value) && type != CSSPrimitiveValue.CSS_PERCENTAGE) {
			throw new CSSParseException("Value for " + cssName + " must be a length or percentage", -1);
		}
	}

	protected void checkLengthType(CSSName cssName, CSSPrimitiveValue value) {
		if (!isLength(value)) {
			throw new CSSParseException("Value for " + cssName + " must be a length", -1);
		}
	}

	protected void checkNumberType(CSSName cssName, CSSPrimitiveValue value) {
		if (value.getPrimitiveType() != CSSPrimitiveValue.CSS_NUMBER) {
			throw new CSSParseException("Value for " + cssName + " must be a number", -1);
		}
	}

	protected void checkAngleType(CSSName cssName, CSSPrimitiveValue value) {
		if (value.getPrimitiveType() != CSSPrimitiveValue.CSS_DEG
				&& value.getPrimitiveType() != CSSPrimitiveValue.CSS_RAD
				&& value.getPrimitiveType() != CSSPrimitiveValue.CSS_GRAD) {
			throw new CSSParseException("Value for " + cssName + "must be an angle (degrees, radians or grads)", -1);
		}
	}

	protected void checkStringType(CSSName cssName, CSSPrimitiveValue value) {
		if (value.getPrimitiveType() != CSSPrimitiveValue.CSS_STRING) {
			throw new CSSParseException("Value for " + cssName + " must be a string", -1);
		}
	}

	protected void checkIdentOrString(CSSName cssName, CSSPrimitiveValue value) {
		short type = value.getPrimitiveType();
		if (type != CSSPrimitiveValue.CSS_STRING && type != CSSPrimitiveValue.CSS_IDENT) {
			throw new CSSParseException("Value for " + cssName + " must be an identifier or string", -1);
		}
	}

	protected void checkIdentLengthNumberOrPercentType(CSSName cssName, CSSPrimitiveValue value) {
		int type = value.getPrimitiveType();
		if (type != CSSPrimitiveValue.CSS_IDENT && !isLength(value) && type != CSSPrimitiveValue.CSS_PERCENTAGE
				&& type != CSSPrimitiveValue.CSS_NUMBER) {
			throw new CSSParseException("Value for " + cssName + " must be an identifier, length, or percentage", -1);
		}
	}

	protected boolean isLength(CSSPrimitiveValue value) {
		int unit = value.getPrimitiveType();
		return unit == CSSPrimitiveValue.CSS_EMS || unit == CSSPrimitiveValue.CSS_EXS
				|| unit == CSSPrimitiveValue.CSS_PX || unit == CSSPrimitiveValue.CSS_IN
				|| unit == CSSPrimitiveValue.CSS_CM || unit == CSSPrimitiveValue.CSS_MM
				|| unit == CSSPrimitiveValue.CSS_PT || unit == CSSPrimitiveValue.CSS_PC
				|| (unit == CSSPrimitiveValue.CSS_NUMBER && value.getFloatValue(CSSPrimitiveValue.CSS_IN) == 0.0f);
	}

	protected void checkValidity(CSSName cssName, BitSet validValues, IdentValue value) {
		if (!validValues.get(value.FS_ID)) {
			throw new CSSParseException("Ident " + value + " is an invalid or unsupported value for " + cssName, -1);
		}
	}

	protected IdentValue checkIdent(CSSName cssName, CSSPrimitiveValue value) {
		IdentValue result = IdentValue.valueOf(value.getStringValue());
		if (result == null) {
			throw new CSSParseException("Value " + value.getStringValue() + " is not a recognized identifier", -1);
		}
		((PropertyValue) value).setIdentValue(result);
		return result;
	}

	protected PropertyDeclaration copyOf(PropertyDeclaration decl, CSSName newName) {
		return new PropertyDeclaration(newName, decl.getValue(), decl.isImportant(), decl.getOrigin());
	}

	protected void checkInheritAllowed(CSSPrimitiveValue value, boolean inheritAllowed) {
		if (value.getCssValueType() == CSSPrimitiveValue.CSS_INHERIT && !inheritAllowed) {
			throw new CSSParseException("Invalid use of inherit", -1);
		}
	}

	protected List checkInheritAll(CSSName[] all, List values, int origin, boolean important, boolean inheritAllowed) {
		if (values.size() == 1) {
			CSSPrimitiveValue value = (CSSPrimitiveValue) values.get(0);
			checkInheritAllowed(value, inheritAllowed);
			if (value.getCssValueType() == CSSPrimitiveValue.CSS_INHERIT) {
				List result = new ArrayList(all.length);
				for (int i = 0; i < all.length; i++) {
					result.add(new PropertyDeclaration(all[i], value, important, origin));
				}
				return result;
			}
		}

		return null;
	}
}