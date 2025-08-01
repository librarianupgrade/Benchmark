/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.struts2.el.parser;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.el.ELException;

import org.apache.struts2.el.lang.EvaluationContext;

/**
 * @author Jacob Hookom [jacob@hookom.net]
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: markt $
 */
public final class AstNegative extends SimpleNode {
	public AstNegative(int id) {
		super(id);
	}

	public Class getType(EvaluationContext ctx) throws ELException {
		return Number.class;
	}

	public Object getValue(EvaluationContext ctx) throws ELException {
		Object obj = this.children[0].getValue(ctx);

		if (obj == null) {
			return new Long(0);
		}
		if (obj instanceof BigDecimal) {
			return ((BigDecimal) obj).negate();
		}
		if (obj instanceof BigInteger) {
			return ((BigInteger) obj).negate();
		}
		if (obj instanceof String) {
			if (isStringFloat((String) obj)) {
				return new Double(-Double.parseDouble((String) obj));
			}
			return new Long(-Long.parseLong((String) obj));
		}
		if (obj instanceof Long) {
			return new Long(-((Long) obj).longValue());
		}
		if (obj instanceof Double) {
			return new Double(-((Double) obj).doubleValue());
		}
		if (obj instanceof Integer) {
			return new Integer(-((Integer) obj).intValue());
		}
		if (obj instanceof Float) {
			return new Float(-((Float) obj).floatValue());
		}
		if (obj instanceof Short) {
			return new Short((short) -((Short) obj).shortValue());
		}
		if (obj instanceof Byte) {
			return new Byte((byte) -((Byte) obj).byteValue());
		}
		Long num = (Long) coerceToNumber(obj, Long.class);
		return new Long(-num.longValue());
	}
}
