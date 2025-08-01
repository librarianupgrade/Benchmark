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
package org.apache.servicecomb.demo.multiple.client;

import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.multiple.a.client.AClient;
import org.apache.servicecomb.demo.multiple.b.client.BClient;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.springboot.starter.EnableServiceComb;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableServiceComb
@ComponentScan(basePackages = { "org.apache.servicecomb.demo.multiple.a.client",
		"org.apache.servicecomb.demo.multiple.b.client" })
public class MultipleClient {
	public static void main(String[] args) throws Exception {
		new SpringApplicationBuilder(MultipleClient.class).web(WebApplicationType.NONE).run(args);

		runTest();
	}

	public static void runTest() {
		AClient aClient = BeanUtils.getContext().getBean(AClient.class);
		BClient bClient = BeanUtils.getContext().getBean(BClient.class);

		aClient.run();
		bClient.run();

		TestMgr.summary();
	}
}
