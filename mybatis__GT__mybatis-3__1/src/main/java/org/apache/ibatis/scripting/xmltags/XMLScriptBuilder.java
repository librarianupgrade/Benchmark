/*
 *    Copyright 2009-2014 the original author or authors.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.builder.BaseBuilder;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.session.Configuration;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Clinton Begin
 */
public class XMLScriptBuilder extends BaseBuilder {

	private XNode context;
	private boolean isDynamic;
	private Class<?> parameterType;

	public XMLScriptBuilder(Configuration configuration, XNode context) {
		this(configuration, context, null);
	}

	public XMLScriptBuilder(Configuration configuration, XNode context, Class<?> parameterType) {
		super(configuration);
		this.context = context;
		this.parameterType = parameterType;
	}

	public SqlSource parseScriptNode() {
		List<SqlNode> contents = parseDynamicTags(context);
		MixedSqlNode rootSqlNode = new MixedSqlNode(contents);
		SqlSource sqlSource = null;
		if (isDynamic) {
			sqlSource = new DynamicSqlSource(configuration, rootSqlNode);
		} else {
			sqlSource = new RawSqlSource(configuration, rootSqlNode, parameterType);
		}
		return sqlSource;
	}

	private List<SqlNode> parseDynamicTags(XNode node) {
		List<SqlNode> contents = new ArrayList<SqlNode>();
		NodeList children = node.getNode().getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			XNode child = node.newXNode(children.item(i));
			if (child.getNode().getNodeType() == Node.CDATA_SECTION_NODE
					|| child.getNode().getNodeType() == Node.TEXT_NODE) {
				String data = child.getStringBody("");
				TextSqlNode textSqlNode = new TextSqlNode(data);
				if (textSqlNode.isDynamic()) {
					contents.add(textSqlNode);
					isDynamic = true;
				} else {
					contents.add(new StaticTextSqlNode(data));
				}
			} else if (child.getNode().getNodeType() == Node.ELEMENT_NODE) { // issue #628
				String nodeName = child.getNode().getNodeName();
				NodeHandler handler = nodeHandlers.get(nodeName);
				if (handler == null) {
					throw new BuilderException("Unknown element <" + nodeName + "> in SQL statement.");
				}
				handler.handleNode(child, contents);
				isDynamic = true;
			}
		}
		return contents;
	}

	private Map<String, NodeHandler> nodeHandlers = new HashMap<String, NodeHandler>() {
		private static final long serialVersionUID = 7123056019193266281L;

		{
			put("trim", new TrimHandler());
			put("where", new WhereHandler());
			put("set", new SetHandler());
			put("foreach", new ForEachHandler());
			put("if", new IfHandler());
			put("choose", new ChooseHandler());
			put("when", new IfHandler());
			put("otherwise", new OtherwiseHandler());
			put("bind", new BindHandler());
		}
	};

	private interface NodeHandler {
		void handleNode(XNode nodeToHandle, List<SqlNode> targetContents);
	}

	private class BindHandler implements NodeHandler {
		public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
			final String name = nodeToHandle.getStringAttribute("name");
			final String expression = nodeToHandle.getStringAttribute("value");
			final VarDeclSqlNode node = new VarDeclSqlNode(name, expression);
			targetContents.add(node);
		}
	}

	private class TrimHandler implements NodeHandler {
		public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
			List<SqlNode> contents = parseDynamicTags(nodeToHandle);
			MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
			String prefix = nodeToHandle.getStringAttribute("prefix");
			String prefixOverrides = nodeToHandle.getStringAttribute("prefixOverrides");
			String suffix = nodeToHandle.getStringAttribute("suffix");
			String suffixOverrides = nodeToHandle.getStringAttribute("suffixOverrides");
			TrimSqlNode trim = new TrimSqlNode(configuration, mixedSqlNode, prefix, prefixOverrides, suffix,
					suffixOverrides);
			targetContents.add(trim);
		}
	}

	private class WhereHandler implements NodeHandler {
		public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
			List<SqlNode> contents = parseDynamicTags(nodeToHandle);
			MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
			WhereSqlNode where = new WhereSqlNode(configuration, mixedSqlNode);
			targetContents.add(where);
		}
	}

	private class SetHandler implements NodeHandler {
		public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
			List<SqlNode> contents = parseDynamicTags(nodeToHandle);
			MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
			SetSqlNode set = new SetSqlNode(configuration, mixedSqlNode);
			targetContents.add(set);
		}
	}

	private class ForEachHandler implements NodeHandler {
		public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
			List<SqlNode> contents = parseDynamicTags(nodeToHandle);
			MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
			String collection = nodeToHandle.getStringAttribute("collection");
			String item = nodeToHandle.getStringAttribute("item");
			String index = nodeToHandle.getStringAttribute("index");
			String open = nodeToHandle.getStringAttribute("open");
			String close = nodeToHandle.getStringAttribute("close");
			String separator = nodeToHandle.getStringAttribute("separator");
			ForEachSqlNode forEachSqlNode = new ForEachSqlNode(configuration, mixedSqlNode, collection, index, item,
					open, close, separator);
			targetContents.add(forEachSqlNode);
		}
	}

	private class IfHandler implements NodeHandler {
		public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
			List<SqlNode> contents = parseDynamicTags(nodeToHandle);
			MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
			String test = nodeToHandle.getStringAttribute("test");
			IfSqlNode ifSqlNode = new IfSqlNode(mixedSqlNode, test);
			targetContents.add(ifSqlNode);
		}
	}

	private class OtherwiseHandler implements NodeHandler {
		public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
			List<SqlNode> contents = parseDynamicTags(nodeToHandle);
			MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
			targetContents.add(mixedSqlNode);
		}
	}

	private class ChooseHandler implements NodeHandler {
		public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
			List<SqlNode> whenSqlNodes = new ArrayList<SqlNode>();
			List<SqlNode> otherwiseSqlNodes = new ArrayList<SqlNode>();
			handleWhenOtherwiseNodes(nodeToHandle, whenSqlNodes, otherwiseSqlNodes);
			SqlNode defaultSqlNode = getDefaultSqlNode(otherwiseSqlNodes);
			ChooseSqlNode chooseSqlNode = new ChooseSqlNode(whenSqlNodes, defaultSqlNode);
			targetContents.add(chooseSqlNode);
		}

		private void handleWhenOtherwiseNodes(XNode chooseSqlNode, List<SqlNode> ifSqlNodes,
				List<SqlNode> defaultSqlNodes) {
			List<XNode> children = chooseSqlNode.getChildren();
			for (XNode child : children) {
				String nodeName = child.getNode().getNodeName();
				NodeHandler handler = nodeHandlers.get(nodeName);
				if (handler instanceof IfHandler) {
					handler.handleNode(child, ifSqlNodes);
				} else if (handler instanceof OtherwiseHandler) {
					handler.handleNode(child, defaultSqlNodes);
				}
			}
		}

		private SqlNode getDefaultSqlNode(List<SqlNode> defaultSqlNodes) {
			SqlNode defaultSqlNode = null;
			if (defaultSqlNodes.size() == 1) {
				defaultSqlNode = defaultSqlNodes.get(0);
			} else if (defaultSqlNodes.size() > 1) {
				throw new BuilderException("Too many default (otherwise) elements in choose statement.");
			}
			return defaultSqlNode;
		}
	}

}
