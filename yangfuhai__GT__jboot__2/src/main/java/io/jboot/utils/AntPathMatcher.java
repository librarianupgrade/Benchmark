package io.jboot.utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AntPathMatcher {

	/**
	 * Default path separator: "/".
	 */
	public static final String DEFAULT_PATH_SEPARATOR = "/";

	private static final int CACHE_TURNOFF_THRESHOLD = 65536;

	private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{[^/]+?}");

	private static final char[] WILDCARD_CHARS = { '*', '?', '{' };

	private String pathSeparator;

	private PathSeparatorPatternCache pathSeparatorPatternCache;

	private boolean caseSensitive = true;

	private boolean trimTokens = false;

	private volatile Boolean cachePatterns;

	private final Map<String, String[]> tokenizedPatternCache = new ConcurrentHashMap<>(256);

	final Map<String, AntPathStringMatcher> stringMatcherCache = new ConcurrentHashMap<>(256);

	/**
	 * Create a new instance with the {@link #DEFAULT_PATH_SEPARATOR}.
	 */
	public AntPathMatcher() {
		this.pathSeparator = DEFAULT_PATH_SEPARATOR;
		this.pathSeparatorPatternCache = new PathSeparatorPatternCache(DEFAULT_PATH_SEPARATOR);
	}

	/**
	 * A convenient, alternative constructor to use with a custom path separator.
	 *
	 * @param pathSeparator the path separator to use, must not be {@code null}.
	 */
	public AntPathMatcher(String pathSeparator) {
		this.pathSeparator = pathSeparator;
		this.pathSeparatorPatternCache = new PathSeparatorPatternCache(pathSeparator);
	}

	/**
	 * Set the path separator to use for pattern parsing.
	 * <p>Default is "/", as in Ant.
	 */
	public void setPathSeparator(String pathSeparator) {
		this.pathSeparator = (pathSeparator != null ? pathSeparator : DEFAULT_PATH_SEPARATOR);
		this.pathSeparatorPatternCache = new PathSeparatorPatternCache(this.pathSeparator);
	}

	/**
	 * Specify whether to perform pattern matching in a case-sensitive fashion.
	 * <p>Default is {@code true}. Switch this to {@code false} for case-insensitive matching.     *
	 */
	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	/**
	 * Specify whether to trim tokenized paths and patterns.
	 * <p>Default is {@code false}.
	 */
	public void setTrimTokens(boolean trimTokens) {
		this.trimTokens = trimTokens;
	}

	/**
	 * Specify whether to cache parsed pattern metadata for patterns passed
	 * into this matcher's {@link #match} method. A value of {@code true}
	 * activates an unlimited pattern cache; a value of {@code false} turns
	 * the pattern cache off completely.
	 * <p>Default is for the cache to be on, but with the variant to automatically
	 * turn it off when encountering too many patterns to cache at runtime
	 * (the threshold is 65536), assuming that arbitrary permutations of patterns
	 * are coming in, with little chance for encountering a recurring pattern.
	 *
	 * @see #getStringMatcher(String)
	 */
	public void setCachePatterns(boolean cachePatterns) {
		this.cachePatterns = cachePatterns;
	}

	private void deactivatePatternCache() {
		this.cachePatterns = false;
		this.tokenizedPatternCache.clear();
		this.stringMatcherCache.clear();
	}

	public boolean isPattern(String path) {
		if (path == null) {
			return false;
		}
		boolean uriVar = false;
		for (int i = 0; i < path.length(); i++) {
			char c = path.charAt(i);
			if (c == '*' || c == '?') {
				return true;
			}
			if (c == '{') {
				uriVar = true;
				continue;
			}
			if (c == '}' && uriVar) {
				return true;
			}
		}
		return false;
	}

	public boolean match(String pattern, String path) {
		return doMatch(pattern, path, true, null);
	}

	public boolean matchStart(String pattern, String path) {
		return doMatch(pattern, path, false, null);
	}

	/**
	 * Actually match the given {@code path} against the given {@code pattern}.
	 *
	 * @param pattern   the pattern to match against
	 * @param path      the path to test
	 * @param fullMatch whether a full pattern match is required (else a pattern match
	 *                  as far as the given base path goes is sufficient)
	 * @return {@code true} if the supplied {@code path} matched, {@code false} if it didn't
	 */
	protected boolean doMatch(String pattern, String path, boolean fullMatch,
			Map<String, String> uriTemplateVariables) {

		if (path == null || path.startsWith(this.pathSeparator) != pattern.startsWith(this.pathSeparator)) {
			return false;
		}

		String[] pattDirs = tokenizePattern(pattern);
		if (fullMatch && this.caseSensitive && !isPotentialMatch(path, pattDirs)) {
			return false;
		}

		String[] pathDirs = tokenizePath(path);
		int pattIdxStart = 0;
		int pattIdxEnd = pattDirs.length - 1;
		int pathIdxStart = 0;
		int pathIdxEnd = pathDirs.length - 1;

		// Match all elements up to the first **
		while (pattIdxStart <= pattIdxEnd && pathIdxStart <= pathIdxEnd) {
			String pattDir = pattDirs[pattIdxStart];
			if ("**".equals(pattDir)) {
				break;
			}
			if (!matchStrings(pattDir, pathDirs[pathIdxStart], uriTemplateVariables)) {
				return false;
			}
			pattIdxStart++;
			pathIdxStart++;
		}

		if (pathIdxStart > pathIdxEnd) {
			// Path is exhausted, only match if rest of pattern is * or **'s
			if (pattIdxStart > pattIdxEnd) {
				return (pattern.endsWith(this.pathSeparator) == path.endsWith(this.pathSeparator));
			}
			if (!fullMatch) {
				return true;
			}
			if (pattIdxStart == pattIdxEnd && pattDirs[pattIdxStart].equals("*") && path.endsWith(this.pathSeparator)) {
				return true;
			}
			for (int i = pattIdxStart; i <= pattIdxEnd; i++) {
				if (!pattDirs[i].equals("**")) {
					return false;
				}
			}
			return true;
		} else if (pattIdxStart > pattIdxEnd) {
			// String not exhausted, but pattern is. Failure.
			return false;
		} else if (!fullMatch && "**".equals(pattDirs[pattIdxStart])) {
			// Path start definitely matches due to "**" part in pattern.
			return true;
		}

		// up to last '**'
		while (pattIdxStart <= pattIdxEnd && pathIdxStart <= pathIdxEnd) {
			String pattDir = pattDirs[pattIdxEnd];
			if (pattDir.equals("**")) {
				break;
			}
			if (!matchStrings(pattDir, pathDirs[pathIdxEnd], uriTemplateVariables)) {
				return false;
			}
			pattIdxEnd--;
			pathIdxEnd--;
		}
		if (pathIdxStart > pathIdxEnd) {
			// String is exhausted
			for (int i = pattIdxStart; i <= pattIdxEnd; i++) {
				if (!pattDirs[i].equals("**")) {
					return false;
				}
			}
			return true;
		}

		while (pattIdxStart != pattIdxEnd && pathIdxStart <= pathIdxEnd) {
			int patIdxTmp = -1;
			for (int i = pattIdxStart + 1; i <= pattIdxEnd; i++) {
				if (pattDirs[i].equals("**")) {
					patIdxTmp = i;
					break;
				}
			}
			if (patIdxTmp == pattIdxStart + 1) {
				// '**/**' situation, so skip one
				pattIdxStart++;
				continue;
			}
			// Find the pattern between padIdxStart & padIdxTmp in str between
			// strIdxStart & strIdxEnd
			int patLength = (patIdxTmp - pattIdxStart - 1);
			int strLength = (pathIdxEnd - pathIdxStart + 1);
			int foundIdx = -1;

			strLoop: for (int i = 0; i <= strLength - patLength; i++) {
				for (int j = 0; j < patLength; j++) {
					String subPat = pattDirs[pattIdxStart + j + 1];
					String subStr = pathDirs[pathIdxStart + i + j];
					if (!matchStrings(subPat, subStr, uriTemplateVariables)) {
						continue strLoop;
					}
				}
				foundIdx = pathIdxStart + i;
				break;
			}

			if (foundIdx == -1) {
				return false;
			}

			pattIdxStart = patIdxTmp;
			pathIdxStart = foundIdx + patLength;
		}

		for (int i = pattIdxStart; i <= pattIdxEnd; i++) {
			if (!pattDirs[i].equals("**")) {
				return false;
			}
		}

		return true;
	}

	private boolean isPotentialMatch(String path, String[] pattDirs) {
		if (!this.trimTokens) {
			int pos = 0;
			for (String pattDir : pattDirs) {
				int skipped = skipSeparator(path, pos, this.pathSeparator);
				pos += skipped;
				skipped = skipSegment(path, pos, pattDir);
				if (skipped < pattDir.length()) {
					return (skipped > 0 || (pattDir.length() > 0 && isWildcardChar(pattDir.charAt(0))));
				}
				pos += skipped;
			}
		}
		return true;
	}

	private int skipSegment(String path, int pos, String prefix) {
		int skipped = 0;
		for (int i = 0; i < prefix.length(); i++) {
			char c = prefix.charAt(i);
			if (isWildcardChar(c)) {
				return skipped;
			}
			int currPos = pos + skipped;
			if (currPos >= path.length()) {
				return 0;
			}
			if (c == path.charAt(currPos)) {
				skipped++;
			}
		}
		return skipped;
	}

	private int skipSeparator(String path, int pos, String separator) {
		int skipped = 0;
		while (path.startsWith(separator, pos + skipped)) {
			skipped += separator.length();
		}
		return skipped;
	}

	private boolean isWildcardChar(char c) {
		for (char candidate : WILDCARD_CHARS) {
			if (c == candidate) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Tokenize the given path pattern into parts, based on this matcher's settings.
	 * <p>Performs caching based on {@link #setCachePatterns}, delegating to
	 * {@link #tokenizePath(String)} for the actual tokenization algorithm.
	 *
	 * @param pattern the pattern to tokenize
	 * @return the tokenized pattern parts
	 */
	protected String[] tokenizePattern(String pattern) {
		String[] tokenized = null;
		Boolean cachePatterns = this.cachePatterns;
		if (cachePatterns == null || cachePatterns.booleanValue()) {
			tokenized = this.tokenizedPatternCache.get(pattern);
		}
		if (tokenized == null) {
			tokenized = tokenizePath(pattern);
			if (cachePatterns == null && this.tokenizedPatternCache.size() >= CACHE_TURNOFF_THRESHOLD) {
				// Try to adapt to the runtime situation that we're encountering:
				// There are obviously too many different patterns coming in here...
				// So let's turn off the cache since the patterns are unlikely to be reoccurring.
				deactivatePatternCache();
				return tokenized;
			}
			if (cachePatterns == null || cachePatterns.booleanValue()) {
				this.tokenizedPatternCache.put(pattern, tokenized);
			}
		}
		return tokenized;
	}

	/**
	 * Tokenize the given path into parts, based on this matcher's settings.
	 *
	 * @param path the path to tokenize
	 * @return the tokenized path parts
	 */
	protected String[] tokenizePath(String path) {
		return StringUtils.tokenizeToStringArray(path, this.pathSeparator, this.trimTokens, true);
	}

	/**
	 * Test whether or not a string matches against a pattern.
	 *
	 * @param pattern the pattern to match against (never {@code null})
	 * @param str     the String which must be matched against the pattern (never {@code null})
	 * @return {@code true} if the string matches against the pattern, or {@code false} otherwise
	 */
	private boolean matchStrings(String pattern, String str, Map<String, String> uriTemplateVariables) {

		return getStringMatcher(pattern).matchStrings(str, uriTemplateVariables);
	}

	/**
	 * Build or retrieve an {@link AntPathStringMatcher} for the given pattern.
	 * <p>The default implementation checks this AntPathMatcher's internal cache
	 * (see {@link #setCachePatterns}), creating a new AntPathStringMatcher instance
	 * if no cached copy is found.
	 * <p>When encountering too many patterns to cache at runtime (the threshold is 65536),
	 * it turns the default cache off, assuming that arbitrary permutations of patterns
	 * are coming in, with little chance for encountering a recurring pattern.
	 * <p>This method may be overridden to implement a custom cache strategy.
	 *
	 * @param pattern the pattern to match against (never {@code null})
	 * @return a corresponding AntPathStringMatcher (never {@code null})
	 * @see #setCachePatterns
	 */
	protected AntPathStringMatcher getStringMatcher(String pattern) {
		AntPathStringMatcher matcher = null;
		Boolean cachePatterns = this.cachePatterns;
		if (cachePatterns == null || cachePatterns.booleanValue()) {
			matcher = this.stringMatcherCache.get(pattern);
		}
		if (matcher == null) {
			matcher = new AntPathStringMatcher(pattern, this.caseSensitive);
			if (cachePatterns == null && this.stringMatcherCache.size() >= CACHE_TURNOFF_THRESHOLD) {
				// Try to adapt to the runtime situation that we're encountering:
				// There are obviously too many different patterns coming in here...
				// So let's turn off the cache since the patterns are unlikely to be reoccurring.
				deactivatePatternCache();
				return matcher;
			}
			if (cachePatterns == null || cachePatterns.booleanValue()) {
				this.stringMatcherCache.put(pattern, matcher);
			}
		}
		return matcher;
	}

	/**
	 * Given a pattern and a full path, determine the pattern-mapped part. <p>For example: <ul>
	 * <li>'{@code /docs/cvs/commit.html}' and '{@code /docs/cvs/commit.html} -> ''</li>
	 * <li>'{@code /docs/*}' and '{@code /docs/cvs/commit} -> '{@code cvs/commit}'</li>
	 * <li>'{@code /docs/cvs/*.html}' and '{@code /docs/cvs/commit.html} -> '{@code commit.html}'</li>
	 * <li>'{@code /docs/**}' and '{@code /docs/cvs/commit} -> '{@code cvs/commit}'</li>
	 * <li>'{@code /docs/**\/*.html}' and '{@code /docs/cvs/commit.html} -> '{@code cvs/commit.html}'</li>
	 * <li>'{@code /*.html}' and '{@code /docs/cvs/commit.html} -> '{@code docs/cvs/commit.html}'</li>
	 * <li>'{@code *.html}' and '{@code /docs/cvs/commit.html} -> '{@code /docs/cvs/commit.html}'</li>
	 * <li>'{@code *}' and '{@code /docs/cvs/commit.html} -> '{@code /docs/cvs/commit.html}'</li> </ul>
	 * <p>Assumes that {@link #match} returns {@code true} for '{@code pattern}' and '{@code path}', but
	 * does <strong>not</strong> enforce this.
	 */

	public String extractPathWithinPattern(String pattern, String path) {
		if (pattern == null) {
			return "";
		}
		String[] patternParts = StringUtils.tokenizeToStringArray(pattern, this.pathSeparator, this.trimTokens, true);
		String[] pathParts = StringUtils.tokenizeToStringArray(path, this.pathSeparator, this.trimTokens, true);
		StringBuilder builder = new StringBuilder();
		boolean pathStarted = false;

		for (int segment = 0; segment < patternParts.length; segment++) {
			String patternPart = patternParts[segment];
			if (patternPart.indexOf('*') > -1 || patternPart.indexOf('?') > -1) {
				for (; segment < pathParts.length; segment++) {
					if (pathStarted || (segment == 0 && !pattern.startsWith(this.pathSeparator))) {
						builder.append(this.pathSeparator);
					}
					builder.append(pathParts[segment]);
					pathStarted = true;
				}
			}
		}

		return builder.toString();
	}

	public Map<String, String> extractUriTemplateVariables(String pattern, String path) {
		Map<String, String> variables = new LinkedHashMap<>();
		boolean result = doMatch(pattern, path, true, variables);
		if (!result) {
			throw new IllegalStateException("Pattern \"" + pattern + "\" is not a match for \"" + path + "\"");
		}
		return variables;
	}

	/**
	 * Given a full path, returns a {@link Comparator} suitable for sorting patterns in order of
	 * explicitness.
	 * <p>This {@code Comparator} will {@linkplain List#sort(Comparator) sort}
	 * a list so that more specific patterns (without URI templates or wild cards) come before
	 * generic patterns. So given a list with the following patterns, the returned comparator
	 * will sort this list so that the order will be as indicated.
	 * <ol>
	 * <li>{@code /hotels/new}</li>
	 * <li>{@code /hotels/{hotel}}</li>
	 * <li>{@code /hotels/*}</li>
	 * </ol>
	 * <p>The full path given as parameter is used to test for exact matches. So when the given path
	 * is {@code /hotels/2}, the pattern {@code /hotels/2} will be sorted before {@code /hotels/1}.
	 *
	 * @param path the full path to use for comparison
	 * @return a comparator capable of sorting patterns in order of explicitness
	 */

	public Comparator<String> getPatternComparator(String path) {
		return new AntPatternComparator(path);
	}

	/**
	 * Tests whether or not a string matches against a pattern via a {@link Pattern}.
	 * <p>The pattern may contain special characters: '*' means zero or more characters; '?' means one and
	 * only one character; '{' and '}' indicate a URI template pattern. For example <tt>/users/{user}</tt>.
	 */
	protected static class AntPathStringMatcher {

		private static final Pattern GLOB_PATTERN = Pattern.compile("\\?|\\*|\\{((?:\\{[^/]+?}|[^/{}]|\\\\[{}])+?)}");

		private static final String DEFAULT_VARIABLE_PATTERN = "((?s).*)";

		private final String rawPattern;

		private final boolean caseSensitive;

		private final boolean exactMatch;

		private final Pattern pattern;

		private final List<String> variableNames = new ArrayList<>();

		public AntPathStringMatcher(String pattern) {
			this(pattern, true);
		}

		public AntPathStringMatcher(String pattern, boolean caseSensitive) {
			this.rawPattern = pattern;
			this.caseSensitive = caseSensitive;
			StringBuilder patternBuilder = new StringBuilder();
			Matcher matcher = GLOB_PATTERN.matcher(pattern);
			int end = 0;
			while (matcher.find()) {
				patternBuilder.append(quote(pattern, end, matcher.start()));
				String match = matcher.group();
				if ("?".equals(match)) {
					patternBuilder.append('.');
				} else if ("*".equals(match)) {
					patternBuilder.append(".*");
				} else if (match.startsWith("{") && match.endsWith("}")) {
					int colonIdx = match.indexOf(':');
					if (colonIdx == -1) {
						patternBuilder.append(DEFAULT_VARIABLE_PATTERN);
						this.variableNames.add(matcher.group(1));
					} else {
						String variablePattern = match.substring(colonIdx + 1, match.length() - 1);
						patternBuilder.append('(');
						patternBuilder.append(variablePattern);
						patternBuilder.append(')');
						String variableName = match.substring(1, colonIdx);
						this.variableNames.add(variableName);
					}
				}
				end = matcher.end();
			}
			// No glob pattern was found, this is an exact String match
			if (end == 0) {
				this.exactMatch = true;
				this.pattern = null;
			} else {
				this.exactMatch = false;
				patternBuilder.append(quote(pattern, end, pattern.length()));
				this.pattern = (this.caseSensitive ? Pattern.compile(patternBuilder.toString())
						: Pattern.compile(patternBuilder.toString(), Pattern.CASE_INSENSITIVE));
			}
		}

		private String quote(String s, int start, int end) {
			if (start == end) {
				return "";
			}
			return Pattern.quote(s.substring(start, end));
		}

		/**
		 * Main entry point.
		 *
		 * @return {@code true} if the string matches against the pattern, or {@code false} otherwise.
		 */
		public boolean matchStrings(String str, Map<String, String> uriTemplateVariables) {
			if (this.exactMatch) {
				return this.caseSensitive ? this.rawPattern.equals(str) : this.rawPattern.equalsIgnoreCase(str);
			} else if (this.pattern != null) {
				Matcher matcher = this.pattern.matcher(str);
				if (matcher.matches()) {
					if (uriTemplateVariables != null) {
						if (this.variableNames.size() != matcher.groupCount()) {
							throw new IllegalArgumentException("The number of capturing groups in the pattern segment "
									+ this.pattern + " does not match the number of URI template variables it defines, "
									+ "which can occur if capturing groups are used in a URI template regex. "
									+ "Use non-capturing groups instead.");
						}
						for (int i = 1; i <= matcher.groupCount(); i++) {
							String name = this.variableNames.get(i - 1);
							String value = matcher.group(i);
							uriTemplateVariables.put(name, value);
						}
					}
					return true;
				}
			}
			return false;
		}

	}

	/**
	 * The default {@link Comparator} implementation returned by
	 * {@link #getPatternComparator(String)}.
	 * <p>In order, the most "generic" pattern is determined by the following:
	 * <ul>
	 * <li>if it's null or a capture all pattern (i.e. it is equal to "/**")</li>
	 * <li>if the other pattern is an actual match</li>
	 * <li>if it's a catch-all pattern (i.e. it ends with "**"</li>
	 * <li>if it's got more "*" than the other pattern</li>
	 * <li>if it's got more "{foo}" than the other pattern</li>
	 * <li>if it's shorter than the other pattern</li>
	 * </ul>
	 */
	protected static class AntPatternComparator implements Comparator<String> {

		private final String path;

		public AntPatternComparator(String path) {
			this.path = path;
		}

		/**
		 * Compare two patterns to determine which should match first, i.e. which
		 * is the most specific regarding the current path.
		 *
		 * @return a negative integer, zero, or a positive integer as pattern1 is
		 * more specific, equally specific, or less specific than pattern2.
		 */

		public int compare(String pattern1, String pattern2) {
			PatternInfo info1 = new PatternInfo(pattern1);
			PatternInfo info2 = new PatternInfo(pattern2);

			if (info1.isLeastSpecific() && info2.isLeastSpecific()) {
				return 0;
			} else if (info1.isLeastSpecific()) {
				return 1;
			} else if (info2.isLeastSpecific()) {
				return -1;
			}

			boolean pattern1EqualsPath = pattern1.equals(this.path);
			boolean pattern2EqualsPath = pattern2.equals(this.path);
			if (pattern1EqualsPath && pattern2EqualsPath) {
				return 0;
			} else if (pattern1EqualsPath) {
				return -1;
			} else if (pattern2EqualsPath) {
				return 1;
			}

			if (info1.isPrefixPattern() && info2.isPrefixPattern()) {
				return info2.getLength() - info1.getLength();
			} else if (info1.isPrefixPattern() && info2.getDoubleWildcards() == 0) {
				return 1;
			} else if (info2.isPrefixPattern() && info1.getDoubleWildcards() == 0) {
				return -1;
			}

			if (info1.getTotalCount() != info2.getTotalCount()) {
				return info1.getTotalCount() - info2.getTotalCount();
			}

			if (info1.getLength() != info2.getLength()) {
				return info2.getLength() - info1.getLength();
			}

			if (info1.getSingleWildcards() < info2.getSingleWildcards()) {
				return -1;
			} else if (info2.getSingleWildcards() < info1.getSingleWildcards()) {
				return 1;
			}

			if (info1.getUriVars() < info2.getUriVars()) {
				return -1;
			} else if (info2.getUriVars() < info1.getUriVars()) {
				return 1;
			}

			return 0;
		}

		/**
		 * Value class that holds information about the pattern, e.g. number of
		 * occurrences of "*", "**", and "{" pattern elements.
		 */
		private static class PatternInfo {

			private final String pattern;

			private int uriVars;

			private int singleWildcards;

			private int doubleWildcards;

			private boolean catchAllPattern;

			private boolean prefixPattern;

			private Integer length;

			public PatternInfo(String pattern) {
				this.pattern = pattern;
				if (this.pattern != null) {
					initCounters();
					this.catchAllPattern = this.pattern.equals("/**");
					this.prefixPattern = !this.catchAllPattern && this.pattern.endsWith("/**");
				}
				if (this.uriVars == 0) {
					this.length = (this.pattern != null ? this.pattern.length() : 0);
				}
			}

			protected void initCounters() {
				int pos = 0;
				if (this.pattern != null) {
					while (pos < this.pattern.length()) {
						if (this.pattern.charAt(pos) == '{') {
							this.uriVars++;
							pos++;
						} else if (this.pattern.charAt(pos) == '*') {
							if (pos + 1 < this.pattern.length() && this.pattern.charAt(pos + 1) == '*') {
								this.doubleWildcards++;
								pos += 2;
							} else if (pos > 0 && !this.pattern.substring(pos - 1).equals(".*")) {
								this.singleWildcards++;
								pos++;
							} else {
								pos++;
							}
						} else {
							pos++;
						}
					}
				}
			}

			public int getUriVars() {
				return this.uriVars;
			}

			public int getSingleWildcards() {
				return this.singleWildcards;
			}

			public int getDoubleWildcards() {
				return this.doubleWildcards;
			}

			public boolean isLeastSpecific() {
				return (this.pattern == null || this.catchAllPattern);
			}

			public boolean isPrefixPattern() {
				return this.prefixPattern;
			}

			public int getTotalCount() {
				return this.uriVars + this.singleWildcards + (2 * this.doubleWildcards);
			}

			/**
			 * Returns the length of the given pattern, where template variables are considered to be 1 long.
			 */
			public int getLength() {
				if (this.length == null) {
					this.length = (this.pattern != null
							? VARIABLE_PATTERN.matcher(this.pattern).replaceAll("#").length()
							: 0);
				}
				return this.length;
			}
		}
	}

	/**
	 * A simple cache for patterns that depend on the configured path separator.
	 */
	private static class PathSeparatorPatternCache {

		private final String endsOnWildCard;

		private final String endsOnDoubleWildCard;

		public PathSeparatorPatternCache(String pathSeparator) {
			this.endsOnWildCard = pathSeparator + "*";
			this.endsOnDoubleWildCard = pathSeparator + "**";
		}

		public String getEndsOnWildCard() {
			return this.endsOnWildCard;
		}

		public String getEndsOnDoubleWildCard() {
			return this.endsOnDoubleWildCard;
		}
	}

}

abstract class StringUtils {

	private static final String[] EMPTY_STRING_ARRAY = {};

	private static final String FOLDER_SEPARATOR = "/";

	private static final String WINDOWS_FOLDER_SEPARATOR = "\\";

	private static final String TOP_PATH = "..";

	private static final String CURRENT_PATH = ".";

	private static final char EXTENSION_SEPARATOR = '.';

	public static String[] tokenizeToStringArray(String str, String delimiters) {
		return tokenizeToStringArray(str, delimiters, true, true);
	}

	public static String[] tokenizeToStringArray(String str, String delimiters, boolean trimTokens,
			boolean ignoreEmptyTokens) {

		if (str == null) {
			return EMPTY_STRING_ARRAY;
		}

		StringTokenizer st = new StringTokenizer(str, delimiters);
		List<String> tokens = new ArrayList<>();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (trimTokens) {
				token = token.trim();
			}
			if (!ignoreEmptyTokens || token.length() > 0) {
				tokens.add(token);
			}
		}
		return toStringArray(tokens);
	}

	/**
	 * Copy the given {@link Collection} into a {@code String} array.
	 * <p>The {@code Collection} must contain {@code String} elements only.
	 *
	 * @param collection the {@code Collection} to copy
	 *                   (potentially {@code null} or empty)
	 * @return the resulting {@code String} array
	 */
	public static String[] toStringArray(Collection<String> collection) {
		return ((collection != null && collection.size() != 0) ? collection.toArray(EMPTY_STRING_ARRAY)
				: EMPTY_STRING_ARRAY);
	}

}