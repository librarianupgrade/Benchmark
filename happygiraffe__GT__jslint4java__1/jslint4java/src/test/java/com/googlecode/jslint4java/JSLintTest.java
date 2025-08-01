
package com.googlecode.jslint4java;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * @author dom
 */
public class JSLintTest {

	private static final String EXPECTED_SEMICOLON = "Expected ';' and instead saw '(end)'.";
	private JSLint lint = null;

	// Check that the issues list matches zero or more reasons.
	private void assertIssues(List<Issue> issues, String... reasons) {
		assertThat(issues, is(notNullValue()));
		String msg = "Actual issues: " + issues;
		assertThat(msg, issues.size(), is(reasons.length));
		for (int i = 0; i < reasons.length; i++) {
			assertThat(issues.get(i).getReason(), is(reasons[i]));
		}
	}

	// small helper function.
	private JSLintResult lint(Reader reader) throws IOException {
		return lint.lint("-", reader);
	}

	// small helper function.
	private JSLintResult lint(String source) {
		return lint.lint("-", source);
	}

	@Before
	public void setUp() throws IOException {
		lint = new JSLintBuilder().fromDefault();
		// Turn off a few options.  These used to be the default.
		lint.addOption(Option.SLOPPY);
	}

	@Test
	public void testAccurateColumnNumbers() {
		List<Issue> issues = lint("var foo = 1").getIssues();
		// ....................... 123456789012
		assertIssues(issues, EXPECTED_SEMICOLON);
		assertThat(issues.get(0).getCharacter(), is(12));
	}

	@Test
	public void testAccurateLineNumbers() {
		List<Issue> issues = lint("var foo = 1").getIssues();
		assertIssues(issues, EXPECTED_SEMICOLON);
		assertThat(issues.get(0).getLine(), is(1));
	}

	/**
	 * See what information we can return about a single function.
	 * @throws Exception
	 */
	@Test
	public void testDataFunctions() throws Exception {
		lint.addOption(Option.UNDEF);
		lint.addOption(Option.WHITE);
		JSLintResult result = lint("var z = 5; function foo(x) {var y = x + z; alert(y); return y; }");
		assertIssues(result.getIssues());
		List<JSFunction> functions = result.getFunctions();
		assertThat(functions.size(), is(1));
		JSFunction f1 = functions.get(0);
		assertThat(f1.getName(), is("foo"));
		assertThat(f1.getLine(), is(1));
		assertThat(f1.getParams().size(), is(1));
		assertThat(f1.getParams().get(0), is("x"));
		// TODO: how to test getClosure()?
		assertThat(f1.getVars().size(), is(1));
		assertThat(f1.getVars().get(0), is("y"));
		// TODO: test getException()
		// TODO: test getOuter()
		// TODO: test getUnused()
		assertThat(f1.getUndef().size(), is(1));
		assertThat(f1.getUndef().get(0), is("alert"));
		assertThat(f1.getGlobal().size(), is(1));
		assertThat(f1.getGlobal().get(0), is("z"));
		// TODO: test getLabel()
	}

	@Test
	public void testDataGlobals() throws Exception {
		JSLintResult result = lint("var foo = 12;");
		assertTrue(result.getIssues().isEmpty());
		assertThat(result.getGlobals(), hasItem("foo"));
	}

	@Test
	public void testDataJsonness() throws Exception {
		JSLintResult result = lint("{\"a\":100}");
		assertIssues(result.getIssues());
		assertTrue(result.isJson());
	}

	@Test
	public void testDataUrls() throws Exception {
		JSLintResult result = lint("<html><body><a href='http://example.com'>e.g.</a>" + "</body></html>");
		assertIssues(result.getIssues());
		List<String> urls = result.getUrls();
		assertThat(urls.size(), is(1));
		assertThat(urls.get(0), is("http://example.com"));
	}

	@Test
	public void testEmptySource() throws Exception {
		assertIssues(lint("").getIssues());
	}

	@Test
	public void testGetEdition() throws Exception {
		String edition = lint.getEdition();
		assertThat(edition, is(notNullValue()));
		String dateRe = "^\\d\\d\\d\\d-\\d\\d-\\d\\d$";
		assertTrue(edition + " matches " + dateRe, edition.matches(dateRe));
	}

	// Issue 16.
	@Test
	public void testGlobalName() throws Exception {
		String src = "/*global name: true */\nname = \"fred\";";
		assertIssues(lint(src).getIssues());
	}

	@Test
	public void testLintReader() throws Exception {
		Reader reader = new StringReader("var foo = 1");
		List<Issue> issues = lint(reader).getIssues();
		assertIssues(issues, EXPECTED_SEMICOLON);
	}

	@Test
	public void testMaxErr() throws Exception {
		lint.addOption(Option.WHITE, "false");
		lint.addOption(Option.UNDEF, "false");
		lint.addOption(Option.MAXERR, "2");
		// Just some nasty thing I threw together. :)
		JSLintResult result = lint("if (foo=42) {\n  println(\"bother\")\n}\n");
		assertIssues(result.getIssues(), "'foo' was used before it was defined.",
				"Missing space between 'foo' and '='.", "Too many errors. (25% scanned).");
	}

	@Test
	public void testMaxLen() {
		lint.addOption(Option.MAXLEN, "1");
		JSLintResult result = lint("var foo = 42;");
		assertIssues(result.getIssues(), "Line too long.");
	}

	@Test
	public void testNoProblems() throws IOException {
		List<Issue> problems = lint("var foo = 1;").getIssues();
		assertIssues(problems);
	}

	@Test
	public void testNullSource() throws Exception {
		List<Issue> issues = lint((String) null).getIssues();
		assertIssues(issues);
	}

	@Test
	public void testOneProblem() throws IOException {
		// There is a missing semicolon here.
		List<Issue> problems = lint("var foo = 1").getIssues();
		assertIssues(problems, EXPECTED_SEMICOLON);
	}

	/**
	 * We're testing this so that we know arrays get passed into JavaScript
	 * correctly.
	 */
	@Test
	public void testPredefOption() throws Exception {
		lint.addOption(Option.PREDEF, "foo,bar");
		lint.addOption(Option.UNDEF);
		List<Issue> issues = lint("foo(bar(42));").getIssues();
		assertIssues(issues);
	}

	@Test
	public void testProperties() {
		// issue 42: beware numeric keys…
		JSLintResult result = lint("var obj = {\"a\": 1, \"b\": 42, 3: \"c\"};");
		assertIssues(result.getIssues());
		Set<String> properties = result.getProperties();
		assertThat(properties.size(), is(3));
		assertThat(properties, hasItems("a", "b", "3"));
	}

	@Test
	public void testReportErrorsOnly() throws Exception {
		String html = lint.report("var foo = 42", true);
		assertThat(html, containsString("<cite><address>"));
		assertThat(html, containsString(EXPECTED_SEMICOLON));
	}

	@Test
	public void testReportFull() throws Exception {
		String html = lint.report("var foo = 42;");
		assertThat(html, containsString("<dt>global</dt><dd>foo</dd>"));
	}

	@Test
	public void testReportInResult() throws Exception {
		String html = lint("var foo = 42").getReport();
		assertThat(html, containsString("<cite><address>"));
		assertThat(html, containsString(EXPECTED_SEMICOLON));
	}

	@Test
	public void testResetOptions() throws Exception {
		String eval_js = "eval('1');";
		lint.addOption(Option.EVIL);
		lint.resetOptions();
		List<Issue> issues = lint(eval_js).getIssues();
		assertIssues(issues, "eval is evil.");

	}

	@Test
	public void testSetOption() throws Exception {
		String eval_js = "eval('1');";
		// should be disallowed by default.
		List<Issue> issues = lint(eval_js).getIssues();
		assertIssues(issues, "eval is evil.");
		// Now should be a problem.
		lint.addOption(Option.EVIL);
		issues = lint(eval_js).getIssues();
		assertIssues(issues);
	}

	@Test
	public void testSetOptionWithArgument() throws Exception {
		// This should only pass when indent=2.
		String js = "var x = 0;\nif (!x) {\n  x = 1;\n}";
		lint.addOption(Option.WHITE);
		lint.addOption(Option.INDENT, "2");
		List<Issue> issues = lint(js).getIssues();
		assertIssues(issues);
	}

	/**
	 * issue 62: tabs getting munged. The root cause here is that JSLint expands
	 * tabs to spaces. It does this on the basis of the <i>indent</i> option, at
	 * initialisation time (if you have a /*jslint comment to alter the ident,
	 * it doesn't affect the tab expansion).
	 *
	 * <p>
	 * Now, it turns out that jslint.com always defaults <i>indent</i> to four.
	 * We have no default, so it gets set to zero. That means that the tab
	 * expansion value gets set to "". Which means {@code var\ti} ends up as
	 * {@code vari}. In order to avoid this, we need to ensure we always pass in
	 * a default of four.
	 *
	 * <p>
	 * Just to make life even more interesting, I forgot that we set
	 * "default options" in setUp (including <i>indent</i>) which meant the
	 * behaviour didn't show the first time.
	 */
	@Test
	public void testTabSanity() {
		// We need to turn on undefined variable checking here.
		lint.resetOptions();
		lint.addOption(Option.SLOPPY);
		// This is coming out as "vari"...
		String js = "var\ti = 0;\n";
		JSLintResult result = lint(js);
		assertIssues(result.getIssues());
	}

	// http://code.google.com/p/jslint4java/issues/detail?id=1
	@Test
	public void testUnableToContinue() throws Exception {
		// This isn't the originally reported problem, but it tickles the
		// "can't continue" message.
		List<Issue> issues = lint("\"").getIssues();
		assertIssues(issues, "Unclosed string.", "Stopping. (100% scanned).");
	}

	/**
	 * Normally, these only get reported as part of .data(), but JSLint treats them as errors.
	 */
	@Test
	public void testUnusedWarnings() {
		lint.addOption(Option.WARNINGS);
		String js = "function foo(a, b) {\n    return a;\n}";
		List<Issue> issues = lint(js).getIssues();
		assertIssues(issues, "Unused variable: 'b' in foo.");
	}
}
