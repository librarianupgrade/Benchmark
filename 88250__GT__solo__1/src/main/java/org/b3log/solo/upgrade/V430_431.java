/*
 * Solo - A small and beautiful blogging system written in Java.
 * Copyright (c) 2010-present, b3log.org
 *
 * Solo is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *         http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package org.b3log.solo.upgrade;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.repository.Transaction;
import org.b3log.solo.model.Option;
import org.b3log.solo.repository.OptionRepository;
import org.json.JSONObject;

/**
 * Upgrade script from v4.3.0 to v4.3.1.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Sep 9, 2020
 * @since 4.3.1
 */
public final class V430_431 {

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LogManager.getLogger(V430_431.class);

	/**
	 * Performs upgrade from v4.3.0 to v4.3.1.
	 *
	 * @throws Exception upgrade fails
	 */
	public static void perform() throws Exception {
		final String fromVer = "4.3.0";
		final String toVer = "4.3.1";

		LOGGER.log(Level.INFO, "Upgrading from version [" + fromVer + "] to version [" + toVer + "]....");

		final BeanManager beanManager = BeanManager.getInstance();
		final OptionRepository optionRepository = beanManager.getReference(OptionRepository.class);

		try {
			final Transaction transaction = optionRepository.beginTransaction();

			final JSONObject versionOpt = optionRepository.get(Option.ID_C_VERSION);
			versionOpt.put(Option.OPTION_VALUE, toVer);
			optionRepository.update(Option.ID_C_VERSION, versionOpt);

			transaction.commit();

			LOGGER.log(Level.INFO, "Upgraded from version [" + fromVer + "] to version [" + toVer + "] successfully");
		} catch (final Exception e) {
			LOGGER.log(Level.ERROR, "Upgrade failed!", e);
			throw new Exception("Upgrade failed from version [" + fromVer + "] to version [" + toVer + "]");
		}
	}
}
