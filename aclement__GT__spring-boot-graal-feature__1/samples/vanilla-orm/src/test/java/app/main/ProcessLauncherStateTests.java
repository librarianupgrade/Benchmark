/*
 * Copyright 2016-2017 the original author or authors.
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

package app.main;

import java.net.URL;

import app.main.CaptureSystemOutput.OutputCapture;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Dave Syer
 *
 */
public class ProcessLauncherStateTests {

	@Test
	@CaptureSystemOutput
	public void vanilla(OutputCapture output) throws Exception {
		// System.setProperty("bench.args", "-verbose:class");
		ProcessLauncherState state = new ProcessLauncherState("target") {
			@Override
			public void run() throws Exception {
				super.run();
				try {
					new URL("http://localhost:8080/owners").getContent();
				} catch (Exception e) {
					// ignore
				}
			}
		};
		state.addArgs("-Dinitialize=true");
		state.setMainClass(SampleApplication.class.getName());
		// state.setProfiles("intg");
		state.before();
		state.run();
		state.after();
		assertThat(output.toString()).contains("Benchmark app started");
		assertThat(state.getHeap()).isGreaterThan(0);
		assertThat(state.getClasses()).isGreaterThan(7000);
	}

}
