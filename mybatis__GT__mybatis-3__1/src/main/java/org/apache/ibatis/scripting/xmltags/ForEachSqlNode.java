/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.scripting.xmltags;

import java.util.Map;

import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.session.Configuration;

/**
 * @author Clinton Begin
 */
public class ForEachSqlNode implements SqlNode {
	public static final String ITEM_PREFIX = "__frch_";

	private ExpressionEvaluator evaluator;
	private String collectionExpression;
	private SqlNode contents;
	private String open;
	private String close;
	private String separator;
	private String item;
	private String index;
	private Configuration configuration;

	public ForEachSqlNode(Configuration configuration, SqlNode contents, String collectionExpression, String index,
			String item, String open, String close, String separator) {
		this.evaluator = new ExpressionEvaluator();
		this.collectionExpression = collectionExpression;
		this.contents = contents;
		this.open = open;
		this.close = close;
		this.separator = separator;
		this.index = index;
		this.item = item;
		this.configuration = configuration;
	}

	public boolean apply(DynamicContext context) {
		Map<String, Object> bindings = context.getBindings();
		final Iterable<?> iterable = evaluator.evaluateIterable(collectionExpression, bindings);
		if (!iterable.iterator().hasNext()) {
			return true;
		}
		boolean first = true;
		applyOpen(context);
		int i = 0;
		for (Object o : iterable) {
			DynamicContext oldContext = context;
			if (first) {
				context = new PrefixedContext(context, "");
			} else {
				if (separator != null) {
					context = new PrefixedContext(context, separator);
				} else {
					context = new PrefixedContext(context, "");
				}
			}
			int uniqueNumber = context.getUniqueNumber();
			if (o instanceof Map.Entry) { // Issue #709 
				@SuppressWarnings("unchecked")
				Map.Entry<Object, Object> mapEntry = (Map.Entry<Object, Object>) o;
				applyIndex(context, mapEntry.getKey(), uniqueNumber);
				applyItem(context, mapEntry.getValue(), uniqueNumber);
			} else {
				applyIndex(context, i, uniqueNumber);
				applyItem(context, o, uniqueNumber);
			}
			contents.apply(new FilteredDynamicContext(configuration, context, index, item, uniqueNumber));
			if (first)
				first = !((PrefixedContext) context).isPrefixApplied();
			context = oldContext;
			i++;
		}
		applyClose(context);
		return true;
	}

	private void applyIndex(DynamicContext context, Object o, int i) {
		if (index != null) {
			context.bind(index, o);
			context.bind(itemizeItem(index, i), o);
		}
	}

	private void applyItem(DynamicContext context, Object o, int i) {
		if (item != null) {
			context.bind(item, o);
			context.bind(itemizeItem(item, i), o);
		}
	}

	private void applyOpen(DynamicContext context) {
		if (open != null) {
			context.appendSql(open);
		}
	}

	private void applyClose(DynamicContext context) {
		if (close != null) {
			context.appendSql(close);
		}
	}

	private static String itemizeItem(String item, int i) {
		return new StringBuilder(ITEM_PREFIX).append(item).append("_").append(i).toString();
	}

	private static class FilteredDynamicContext extends DynamicContext {
		private DynamicContext delegate;
		private int index;
		private String itemIndex;
		private String item;

		public FilteredDynamicContext(Configuration configuration, DynamicContext delegate, String itemIndex,
				String item, int i) {
			super(configuration, null);
			this.delegate = delegate;
			this.index = i;
			this.itemIndex = itemIndex;
			this.item = item;
		}

		@Override
		public Map<String, Object> getBindings() {
			return delegate.getBindings();
		}

		@Override
		public void bind(String name, Object value) {
			delegate.bind(name, value);
		}

		@Override
		public String getSql() {
			return delegate.getSql();
		}

		@Override
		public void appendSql(String sql) {
			GenericTokenParser parser = new GenericTokenParser("#{", "}", new TokenHandler() {
				public String handleToken(String content) {
					String newContent = content.replaceFirst("^\\s*" + item + "(?![^.,:\\s])",
							itemizeItem(item, index));
					if (itemIndex != null && newContent.equals(content)) {
						newContent = content.replaceFirst("^\\s*" + itemIndex + "(?![^.,:\\s])",
								itemizeItem(itemIndex, index));
					}
					return new StringBuilder("#{").append(newContent).append("}").toString();
				}
			});

			delegate.appendSql(parser.parse(sql));
		}

		@Override
		public int getUniqueNumber() {
			return delegate.getUniqueNumber();
		}

	}

	private class PrefixedContext extends DynamicContext {
		private DynamicContext delegate;
		private String prefix;
		private boolean prefixApplied;

		public PrefixedContext(DynamicContext delegate, String prefix) {
			super(configuration, null);
			this.delegate = delegate;
			this.prefix = prefix;
			this.prefixApplied = false;
		}

		public boolean isPrefixApplied() {
			return prefixApplied;
		}

		@Override
		public Map<String, Object> getBindings() {
			return delegate.getBindings();
		}

		@Override
		public void bind(String name, Object value) {
			delegate.bind(name, value);
		}

		@Override
		public void appendSql(String sql) {
			if (!prefixApplied && sql != null && sql.trim().length() > 0) {
				delegate.appendSql(prefix);
				prefixApplied = true;
			}
			delegate.appendSql(sql);
		}

		@Override
		public String getSql() {
			return delegate.getSql();
		}

		@Override
		public int getUniqueNumber() {
			return delegate.getUniqueNumber();
		}
	}

}
