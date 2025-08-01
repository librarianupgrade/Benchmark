/*
 * Copyright (c) 2010-2017, b3log.org & hacpai.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.solo.service;

import java.util.Date;
import java.util.List;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.model.User;
import org.b3log.solo.AbstractTestCase;
import org.b3log.solo.model.ArchiveDate;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * {@link ArchiveDateQueryService} test case.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Sep 11, 2012
 */
public class ArchiveDateQueryServiceTestCase extends AbstractTestCase {

	/**
	 * Init.
	 * 
	 * @throws Exception exception
	 */
	@Test
	public void init() throws Exception {
		final InitService initService = getInitService();

		final JSONObject requestJSONObject = new JSONObject();
		requestJSONObject.put(User.USER_EMAIL, "test@gmail.com");
		requestJSONObject.put(User.USER_NAME, "Admin");
		requestJSONObject.put(User.USER_PASSWORD, "pass");

		initService.init(requestJSONObject);

		final UserQueryService userQueryService = getUserQueryService();
		Assert.assertNotNull(userQueryService.getUserByEmail("test@gmail.com"));
	}

	/**
	 * Get Archive Dates.
	 * 
	 * @throws Exception exception
	 */
	@Test(dependsOnMethods = "init")
	public void getArchiveDates() throws Exception {
		final ArchiveDateQueryService archiveDateQueryService = getArchiveDateQueryService();

		final List<JSONObject> archiveDates = archiveDateQueryService.getArchiveDates();

		Assert.assertNotNull(archiveDates);
		Assert.assertEquals(archiveDates.size(), 1);
	}

	/**
	 * Get By Archive Date String.
	 * 
	 * @throws Exception exception
	 */
	@Test(dependsOnMethods = "init")
	public void getByArchiveDateString() throws Exception {
		final ArchiveDateQueryService archiveDateQueryService = getArchiveDateQueryService();

		final String archiveDateString = DateFormatUtils.format(new Date(), "yyyy/MM");
		final JSONObject result = archiveDateQueryService.getByArchiveDateString(archiveDateString);
		Assert.assertNotNull(result);
		Assert.assertEquals(result.getJSONObject(ArchiveDate.ARCHIVE_DATE).getLong(ArchiveDate.ARCHIVE_TIME),
				DateUtils.parseDate(archiveDateString, new String[] { "yyyy/MM" }).getTime());
	}
}
