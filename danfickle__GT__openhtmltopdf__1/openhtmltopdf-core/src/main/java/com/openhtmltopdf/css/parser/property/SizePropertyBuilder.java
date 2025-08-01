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
import java.util.List;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import com.openhtmltopdf.css.constants.CSSName;
import com.openhtmltopdf.css.constants.IdentValue;
import com.openhtmltopdf.css.parser.CSSParseException;
import com.openhtmltopdf.css.parser.PropertyValue;
import com.openhtmltopdf.css.sheet.PropertyDeclaration;

public class SizePropertyBuilder extends AbstractPropertyBuilder {
	private static final CSSName[] ALL = { CSSName.FS_PAGE_ORIENTATION, CSSName.FS_PAGE_HEIGHT, CSSName.FS_PAGE_WIDTH };

	public List buildDeclarations(CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
		List result = new ArrayList(3);
		checkValueCount(cssName, 1, 2, values.size());

		if (values.size() == 1) {
			PropertyValue value = (PropertyValue) values.get(0);

			checkInheritAllowed(value, inheritAllowed);

			if (value.getCssValueType() == CSSValue.CSS_INHERIT) {
				return checkInheritAll(ALL, values, origin, important, inheritAllowed);
			} else if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
				PageSize pageSize = PageSize.getPageSize(value.getStringValue());
				if (pageSize != null) {
					result.add(new PropertyDeclaration(CSSName.FS_PAGE_ORIENTATION, new PropertyValue(IdentValue.AUTO),
							important, origin));
					result.add(
							new PropertyDeclaration(CSSName.FS_PAGE_WIDTH, pageSize.getPageWidth(), important, origin));
					result.add(new PropertyDeclaration(CSSName.FS_PAGE_HEIGHT, pageSize.getPageHeight(), important,
							origin));
					return result;
				}

				IdentValue ident = checkIdent(cssName, value);
				if (ident == IdentValue.LANDSCAPE || ident == IdentValue.PORTRAIT) {
					result.add(new PropertyDeclaration(CSSName.FS_PAGE_ORIENTATION, value, important, origin));
					result.add(new PropertyDeclaration(CSSName.FS_PAGE_WIDTH, new PropertyValue(IdentValue.AUTO),
							important, origin));
					result.add(new PropertyDeclaration(CSSName.FS_PAGE_HEIGHT, new PropertyValue(IdentValue.AUTO),
							important, origin));
					return result;
				} else if (ident == IdentValue.AUTO) {
					result.add(new PropertyDeclaration(CSSName.FS_PAGE_ORIENTATION, value, important, origin));
					result.add(new PropertyDeclaration(CSSName.FS_PAGE_WIDTH, value, important, origin));
					result.add(new PropertyDeclaration(CSSName.FS_PAGE_HEIGHT, value, important, origin));
					return result;
				} else {
					throw new CSSParseException("Identifier " + ident + " is not a valid value for " + cssName, -1);
				}
			} else if (isLength(value)) {
				if (value.getFloatValue() < 0.0f) {
					throw new CSSParseException("A page dimension may not be negative", -1);
				}

				result.add(new PropertyDeclaration(CSSName.FS_PAGE_ORIENTATION, new PropertyValue(IdentValue.AUTO),
						important, origin));
				result.add(new PropertyDeclaration(CSSName.FS_PAGE_WIDTH, value, important, origin));
				result.add(new PropertyDeclaration(CSSName.FS_PAGE_HEIGHT, value, important, origin));

				return result;
			} else {
				throw new CSSParseException("Value for " + cssName + " must be a length or identifier", -1);
			}
		} else { /* values.size == 2 */
			PropertyValue value1 = (PropertyValue) values.get(0);
			PropertyValue value2 = (PropertyValue) values.get(1);

			checkInheritAllowed(value2, false);

			if (isLength(value1) && isLength(value2)) {
				if (value1.getFloatValue() < 0.0f) {
					throw new CSSParseException("A page dimension may not be negative", -1);
				}

				if (value2.getFloatValue() < 0.0f) {
					throw new CSSParseException("A page dimension may not be negative", -1);
				}

				result.add(new PropertyDeclaration(CSSName.FS_PAGE_ORIENTATION, new PropertyValue(IdentValue.AUTO),
						important, origin));
				result.add(new PropertyDeclaration(CSSName.FS_PAGE_WIDTH, value1, important, origin));
				result.add(new PropertyDeclaration(CSSName.FS_PAGE_HEIGHT, value2, important, origin));

				return result;
			} else if (value1.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT
					&& value2.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
				if (value2.getStringValue().equals("landscape") || value2.getStringValue().equals("portrait")) {
					PropertyValue temp = value1;
					value1 = value2;
					value2 = temp;
				}

				if (!(value1.toString().equals("landscape") || value1.toString().equals("portrait"))) {
					throw new CSSParseException("Value " + value1 + " is not a valid page orientation", -1);
				}

				result.add(new PropertyDeclaration(CSSName.FS_PAGE_ORIENTATION, value1, important, origin));

				PageSize pageSize = PageSize.getPageSize(value2.getStringValue());
				if (pageSize == null) {
					throw new CSSParseException("Value " + value1 + " is not a valid page size", -1);
				}

				result.add(new PropertyDeclaration(CSSName.FS_PAGE_WIDTH, pageSize.getPageWidth(), important, origin));
				result.add(
						new PropertyDeclaration(CSSName.FS_PAGE_HEIGHT, pageSize.getPageHeight(), important, origin));

				return result;
			} else {
				throw new CSSParseException("Invalid value for size property", -1);
			}
		}
	}
}
