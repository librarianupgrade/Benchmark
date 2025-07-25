/**
 * Copyright (c) 2015-2022, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.web.directive;

import com.jfinal.kit.LogKit;
import com.jfinal.template.Env;
import com.jfinal.template.expr.ast.ExprList;
import com.jfinal.template.io.Writer;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.OutputDirectiveFactory;
import com.jfinal.template.stat.Scope;
import com.jfinal.template.stat.ast.Output;
import io.jboot.Jboot;

/**
 * 主要作用：在生产环境下，忽略模板引擎的错误输出。
 */
public class JbootOutputDirectiveFactory extends OutputDirectiveFactory {

	public static final JbootOutputDirectiveFactory me = new JbootOutputDirectiveFactory();

	private boolean ignoreTemplateException = !Jboot.isDevMode();

	public boolean isIgnoreTemplateException() {
		return ignoreTemplateException;
	}

	public void setIgnoreTemplateException(boolean ignoreTemplateException) {
		this.ignoreTemplateException = ignoreTemplateException;
	}

	@Override
	public Output getOutputDirective(ExprList exprList, Location location) {
		return new JbootOutput(exprList, location);
	}

	public static class JbootOutput extends Output {

		public JbootOutput(ExprList exprList, Location location) {
			super(exprList, location);
		}

		@Override
		public void exec(Env env, Scope scope, Writer writer) {
			try {
				super.exec(env, scope, writer);
			} catch (Exception e) {
				if (me.ignoreTemplateException) {
					LogKit.error(e.toString(), e);
				} else {
					throw e;
				}
			}
		}
	}
}
