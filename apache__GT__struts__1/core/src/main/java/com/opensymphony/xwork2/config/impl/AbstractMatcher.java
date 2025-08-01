/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.opensymphony.xwork2.config.impl;

import com.opensymphony.xwork2.util.PatternMatcher;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p> Matches patterns against pre-compiled wildcard expressions pulled from
 * target objects. It uses the wildcard matcher from the Apache Cocoon
 * project. Patterns will be matched in the order they were added. The first
 * match wins, so more specific patterns should be defined before less specific
 * patterns.
 *
 * @since 2.1
 */
public abstract class AbstractMatcher<E> implements Serializable {

	private static final Logger LOG = LogManager.getLogger(AbstractMatcher.class);
	private static final Pattern WILDCARD_PATTERN = Pattern.compile("\\{(.)}");

	/**
	 * <p> Handles all wildcard pattern matching. </p>
	 */
	PatternMatcher<Object> wildcard;

	/**
	 * <p> The compiled patterns and their associated target objects </p>
	 */
	List<Mapping<E>> compiledPatterns = new ArrayList<>();

	/**
	 * This flag controls if passed named params should be appended
	 * to the map in {@link #replaceParameters(Map, Map)}
	 * and will be accessible in {@link com.opensymphony.xwork2.config.entities.ResultConfig}.
	 * If set to false, the named parameters won't be appended.
	 * <p>
	 * This behaviour is controlled by {@link org.apache.struts2.StrutsConstants#STRUTS_MATCHER_APPEND_NAMED_PARAMETERS}
	 *
	 * @since 2.5.23
	 * See WW-5065
	 */
	private final boolean appendNamedParameters;

	public AbstractMatcher(PatternMatcher<?> helper, boolean appendNamedParameters) {
		this.wildcard = (PatternMatcher<Object>) helper;
		this.appendNamedParameters = appendNamedParameters;
	}

	/**
	 * Creates a matcher with {@link #appendNamedParameters} set to true to keep backward compatibility
	 *
	 * @param helper an instance of {@link PatternMatcher}
	 * @deprecated use @{link {@link AbstractMatcher(PatternMatcher, boolean)} instead
	 */
	@Deprecated
	public AbstractMatcher(PatternMatcher<?> helper) {
		this(helper, true);
	}

	/**
	 * <p>
	 * Finds and precompiles the wildcard patterns. Patterns will be evaluated
	 * in the order they were added. Only patterns that actually contain a
	 * wildcard will be compiled.
	 * </p>
	 *
	 * <p>
	 * Patterns can optionally be matched "loosely". When the end of the pattern
	 * matches \*[^*]\*$ (wildcard, no wildcard, wildcard), if the pattern
	 * fails, it is also matched as if the last two characters didn't exist. The
	 * goal is to support the legacy "*!*" syntax, where the "!*" is optional.
	 * </p>
	 *
	 * @param name       The pattern
	 * @param target     The object to associate with the pattern
	 * @param looseMatch To loosely match wildcards or not
	 */
	public void addPattern(String name, E target, boolean looseMatch) {

		Object pattern;

		if (!wildcard.isLiteral(name)) {
			if (looseMatch && (name.length() > 0) && (name.charAt(0) == '/')) {
				name = name.substring(1);
			}

			LOG.debug("Compiling pattern '{}'", name);

			pattern = wildcard.compilePattern(name);
			compiledPatterns.add(new Mapping<>(name, pattern, target));

			if (looseMatch) {
				int lastStar = name.lastIndexOf('*');
				if (lastStar > 1 && lastStar == name.length() - 1) {
					if (name.charAt(lastStar - 1) != '*') {
						pattern = wildcard.compilePattern(name.substring(0, lastStar - 1));
						compiledPatterns.add(new Mapping<>(name, pattern, target));
					}
				}
			}
		}
	}

	public void freeze() {
		compiledPatterns = Collections.unmodifiableList(new ArrayList<>());
	}

	/**
	 * <p> Matches the path against the compiled wildcard patterns. </p>
	 *
	 * @param potentialMatch The portion of the request URI for selecting a config.
	 * @return The action config if matched, else null
	 */
	public E match(String potentialMatch) {
		E config = null;

		if (compiledPatterns.size() > 0) {
			LOG.debug("Attempting to match '{}' to a wildcard pattern, {} available", potentialMatch,
					compiledPatterns.size());

			Map<String, String> vars = new LinkedHashMap<>();
			for (Mapping<E> m : compiledPatterns) {
				if (wildcard.match(vars, potentialMatch, m.getPattern())) {
					LOG.debug("Value matches pattern '{}'", m.getOriginalPattern());
					config = convert(potentialMatch, m.getTarget(), vars);
					break;
				}
			}
		}

		return config;
	}

	/**
	 * <p> Clones the target object and its children, replacing various
	 * properties with the values of the wildcard-matched strings. </p>
	 *
	 * @param path The requested path
	 * @param orig The original object
	 * @param vars A Map of wildcard-matched strings
	 * @return A cloned object with appropriate properties replaced with
	 * wildcard-matched values
	 */
	protected abstract E convert(String path, E orig, Map<String, String> vars);

	/**
	 * <p>Replaces parameter values</p>
	 *
	 * @param orig The original parameters with placeholder values
	 * @param vars A Map of wildcard-matched strings
	 * @return map with replaced parameters
	 */
	protected Map<String, String> replaceParameters(Map<String, String> orig, Map<String, String> vars) {
		Map<String, String> map = new LinkedHashMap<>();

		//this will set the group index references, like {1}
		for (Map.Entry<String, String> entry : orig.entrySet()) {
			map.put(entry.getKey(), convertParam(entry.getValue(), vars));
		}

		if (appendNamedParameters) {
			LOG.debug("Appending named parameters to the result map");
			//the values map will contain entries like name->"Lex Luthor" and 1->"Lex Luthor"
			//now add the non-numeric values
			for (Map.Entry<String, String> entry : vars.entrySet()) {
				if (!NumberUtils.isCreatable(entry.getKey())) {
					map.put(entry.getKey(), entry.getValue());
				}
			}
		}

		return map;
	}

	/**
	 * <p> Inserts into a value wildcard-matched strings where specified
	 * with the {x} syntax.  If a wildcard-matched value isn't found, the
	 * replacement token is turned into an empty string.
	 * </p>
	 *
	 * @param val  The value to convert
	 * @param vars A Map of wildcard-matched strings
	 * @return The new value
	 */
	protected String convertParam(String val, Map<String, String> vars) {
		if (val == null) {
			return null;
		}

		Matcher wildcardMatcher = WILDCARD_PATTERN.matcher(val);

		StringBuffer result = new StringBuffer();
		while (wildcardMatcher.find()) {
			wildcardMatcher.appendReplacement(result, vars.getOrDefault(wildcardMatcher.group(1), ""));
		}
		wildcardMatcher.appendTail(result);

		return result.toString();
	}

	/**
	 * <p> Stores a compiled wildcard pattern and the object it came
	 * from. </p>
	 */
	private static class Mapping<E> implements Serializable {
		/**
		 * <p> The original pattern. </p>
		 */
		private final String original;

		/**
		 * <p> The compiled pattern. </p>
		 */
		private final Object pattern;

		/**
		 * <p> The original object. </p>
		 */
		private final E config;

		/**
		 * <p> Contructs a read-only Mapping instance. </p>
		 *
		 * @param original The original pattern
		 * @param pattern  The compiled pattern
		 * @param config   The original object
		 */
		public Mapping(String original, Object pattern, E config) {
			this.original = original;
			this.pattern = pattern;
			this.config = config;
		}

		/**
		 * <p> Gets the compiled wildcard pattern. </p>
		 *
		 * @return The compiled pattern
		 */
		public Object getPattern() {
			return this.pattern;
		}

		/**
		 * <p> Gets the object that contains the pattern. </p>
		 *
		 * @return The associated object
		 */
		public E getTarget() {
			return this.config;
		}

		/**
		 * <p> Gets the original wildcard pattern. </p>
		 *
		 * @return The original pattern
		 */
		public String getOriginalPattern() {
			return this.original;
		}
	}
}
