/*
 * Copyright 2017 Adobe Systems Incorporated
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
package com.adobe.cq.testing.junit.rules;

import org.apache.sling.testing.junit.rules.SlingRule;
import org.apache.sling.testing.junit.rules.instance.Instance;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Base rule to be used on every test.
 * It chains the {@link SlingRule}.
 */
public class CQRule implements TestRule {
	public final SlingRule slingBaseRule;

	protected TestRule cqRuleChain;

	public CQRule(Instance... instances) {
		this.slingBaseRule = new SlingRule(instances);
		this.cqRuleChain = RuleChain.outerRule(slingBaseRule);
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return cqRuleChain.apply(base, description);
	}
}
