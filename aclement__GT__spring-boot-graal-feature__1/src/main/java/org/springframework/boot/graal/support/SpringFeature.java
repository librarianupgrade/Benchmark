/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.boot.graal.support;

import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

import org.graalvm.nativeimage.hosted.Feature;

import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.hosted.ResourcesFeature;
import com.oracle.svm.reflect.hosted.ReflectionFeature;
import com.oracle.svm.reflect.proxy.hosted.DynamicProxyFeature;

@AutomaticFeature
public class SpringFeature implements Feature {

	private ReflectionHandler reflectionHandler;

	private DynamicProxiesHandler dynamicProxiesHandler;

	private ResourcesHandler resourcesHandler;

	private InitializationHandler buildTimeInitializationHandler;

	public SpringFeature() {
		System.out.println(
				"███████╗██████╗ ██████╗ ██╗███╗   ██╗ ██████╗     ██████╗  ██████╗  ██████╗ ████████╗     ██████╗ ██████╗  █████╗  █████╗ ██╗     \n"
						+ "██╔════╝██╔══██╗██╔══██╗██║████╗  ██║██╔════╝     ██╔══██╗██╔═══██╗██╔═══██╗╚══██╔══╝    ██╔════╝ ██╔══██╗██╔══██╗██╔══██╗██║     \n"
						+ "███████╗██████╔╝██████╔╝██║██╔██╗ ██║██║  ███╗    ██████╔╝██║   ██║██║   ██║   ██║       ██║  ███╗██████╔╝███████║███████║██║     \n"
						+ "╚════██║██╔═══╝ ██╔══██╗██║██║╚██╗██║██║   ██║    ██╔══██╗██║   ██║██║   ██║   ██║       ██║   ██║██╔══██╗██╔══██║██╔══██║██║     \n"
						+ "███████║██║     ██║  ██║██║██║ ╚████║╚██████╔╝    ██████╔╝╚██████╔╝╚██████╔╝   ██║       ╚██████╔╝██║  ██║██║  ██║██║  ██║███████╗\n"
						+ "╚══════╝╚═╝     ╚═╝  ╚═╝╚═╝╚═╝  ╚═══╝ ╚═════╝     ╚═════╝  ╚═════╝  ╚═════╝    ╚═╝        ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝╚══════╝\n"
						+ "                                                                                                                                  ");
		reflectionHandler = new ReflectionHandler();
		dynamicProxiesHandler = new DynamicProxiesHandler();
		resourcesHandler = new ResourcesHandler(reflectionHandler);
		buildTimeInitializationHandler = new InitializationHandler();
	}

	public boolean isInConfiguration(IsInConfigurationAccess access) {
		return true;
	}

	public List<Class<? extends Feature>> getRequiredFeatures() {
		List<Class<? extends Feature>> fs = new ArrayList<>();
		fs.add(DynamicProxyFeature.class); // Ensures DynamicProxyRegistry available
		fs.add(ResourcesFeature.class); // Ensures ResourcesRegistry available
		fs.add(ReflectionFeature.class); // Ensures RuntimeReflectionSupport available
		return fs;
	}

	public void duringSetup(DuringSetupAccess access) {
		reflectionHandler.register(access);
		dynamicProxiesHandler.register(access);
	}

	public void beforeAnalysis(BeforeAnalysisAccess access) {
		resourcesHandler.register(access);
		buildTimeInitializationHandler.register(access);
		// TODO who requires this, is it a netty thing?
		try {
			access.registerAsUnsafeAccessed(Buffer.class.getDeclaredField("address"));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

}
