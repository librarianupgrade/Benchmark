/*
 * Solo - A small and beautiful blogging system written in Java.
 * Copyright (c) 2010-present, b3log.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.b3log.solo.processor.console;

import org.b3log.latke.Keys;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.http.advice.ProcessAdvice;
import org.b3log.latke.http.advice.RequestProcessAdviceException;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.model.Role;
import org.b3log.latke.model.User;
import org.b3log.solo.util.Solos;
import org.json.JSONObject;

/**
 * The common auth check before advice for admin console.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.3, Feb 7, 2019
 * @since 2.9.5
 */
@Singleton
public class ConsoleAuthAdvice extends ProcessAdvice {

	@Override
	public void doAdvice(final RequestContext context) throws RequestProcessAdviceException {
		final JSONObject currentUser = Solos.getCurrentUser(context.getRequest(), context.getResponse());
		if (null == currentUser) {
			final JSONObject exception401 = new JSONObject();
			exception401.put(Keys.MSG, "Unauthorized to request [" + context.requestURI() + "], please signin");
			exception401.put(Keys.STATUS_CODE, 401);

			throw new RequestProcessAdviceException(exception401);
		}

		final String userRole = currentUser.optString(User.USER_ROLE);
		if (Role.VISITOR_ROLE.equals(userRole)) {
			final JSONObject exception403 = new JSONObject();
			exception403.put(Keys.MSG, "Forbidden to request [" + context.requestURI() + "]");
			exception403.put(Keys.STATUS_CODE, 403);

			throw new RequestProcessAdviceException(exception403);
		}
	}
}
