/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.provider.pojo.instance;

import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.provider.pojo.PojoConst;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

public class TestSpringInstanceFactory {

	@Test
	public void testInitException() {

		SpringInstanceFactory lSpringInstanceFactory = new SpringInstanceFactory();
		try (MockedStatic<BeanUtils> beanUtilsMockedStatic = Mockito.mockStatic(BeanUtils.class)) {
			beanUtilsMockedStatic.when(BeanUtils::getContext).thenReturn(Mockito.mock(ApplicationContext.class));
			beanUtilsMockedStatic.when(() -> BeanUtils.getBean(Mockito.anyString())).thenReturn(null);
			try {
				lSpringInstanceFactory.create("TestSpringInstanceFactory");
			} catch (Error e) {
				Assertions.assertEquals("Fail to find bean:TestSpringInstanceFactory", e.getMessage());
			}
		}
	}

	@Test
	public void testInit() {

		SpringInstanceFactory lSpringInstanceFactory = new SpringInstanceFactory();
		try (MockedStatic<BeanUtils> beanUtilsMockedStatic = Mockito.mockStatic(BeanUtils.class)) {
			beanUtilsMockedStatic.when(BeanUtils::getContext).thenReturn(Mockito.mock(ApplicationContext.class));
			beanUtilsMockedStatic.when(() -> BeanUtils.getBean(Mockito.anyString())).thenReturn(new Object());

			lSpringInstanceFactory.create("org.apache.servicecomb.provider.pojo.instance.TestPojoInstanceFactory");
			Assertions.assertEquals(PojoConst.SPRING, lSpringInstanceFactory.getImplName());
		}
	}
}
