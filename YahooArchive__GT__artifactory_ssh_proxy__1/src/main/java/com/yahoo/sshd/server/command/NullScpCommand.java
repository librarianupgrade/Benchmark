/*
 * Copyright 2014 Yahoo! Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the License); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an AS IS BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.yahoo.sshd.server.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yahoo.sshd.server.logging.SshRequestLog;

/**
 * This class exists as a dummy test so we can test the speed of doing nothing.
 * 
 * @author areese
 * 
 */
public class NullScpCommand extends AbstractScpCommand {
	protected static final Logger LOGGER = LoggerFactory.getLogger(NullScpCommand.class);

	public NullScpCommand(String args) {
		super(args);
	}

	@Override
	protected SshRequestLog getSshRequestLog() {
		return null;
	}
}
