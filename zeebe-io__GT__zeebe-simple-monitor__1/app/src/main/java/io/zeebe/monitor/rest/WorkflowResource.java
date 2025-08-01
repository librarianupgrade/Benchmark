/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
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
package io.zeebe.monitor.rest;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.commands.DeployWorkflowCommandStep1.DeployWorkflowCommandBuilderStep2;
import io.zeebe.monitor.zeebe.ZeebeConnectionService;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/workflows")
public class WorkflowResource {

	@Autowired
	private ZeebeConnectionService connections;

	@RequestMapping(path = "/{workflowKey}", method = RequestMethod.POST)
	public void createWorkflowInstance(@PathVariable("workflowKey") long workflowKey, @RequestBody String payload) {

		connections.getClient().newCreateInstanceCommand().workflowKey(workflowKey).payload(payload).send().join();
	}

	@RequestMapping(path = "/", method = RequestMethod.POST)
	public void uploadModel(@RequestBody DeploymentDto deployment) throws UnsupportedEncodingException {

		final ZeebeClient client = connections.getClient();

		final List<FileDto> files = deployment.getFiles();
		if (files.isEmpty()) {
			throw new RuntimeException("no resources to deploy");
		}

		final FileDto firstFile = files.get(0);

		final DeployWorkflowCommandBuilderStep2 cmd = client.newDeployCommand().addResourceBytes(firstFile.getContent(),
				firstFile.getFilename());

		for (FileDto file : files.subList(1, files.size())) {
			cmd.addResourceBytes(file.getContent(), file.getFilename());
		}

		cmd.send().join();
	}
}
