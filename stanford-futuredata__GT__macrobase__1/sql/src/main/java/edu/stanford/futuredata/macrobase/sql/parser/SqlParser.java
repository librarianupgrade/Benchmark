/*
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
package edu.stanford.futuredata.macrobase.sql.parser;

import static java.util.Objects.requireNonNull;

import edu.stanford.futuredata.macrobase.SqlBaseBaseListener;
import edu.stanford.futuredata.macrobase.SqlBaseLexer;
import edu.stanford.futuredata.macrobase.SqlBaseParser;
import edu.stanford.futuredata.macrobase.sql.tree.Expression;
import edu.stanford.futuredata.macrobase.sql.tree.Node;
import edu.stanford.futuredata.macrobase.sql.tree.Statement;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;
import javax.inject.Inject;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.TerminalNode;

public class SqlParser {

	private static final BaseErrorListener ERROR_LISTENER = new BaseErrorListener() {
		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
				String message, RecognitionException e) {
			throw new ParsingException(message, e, line, charPositionInLine);
		}
	};

	private final EnumSet<IdentifierSymbol> allowedIdentifierSymbols;

	public SqlParser() {
		this(new SqlParserOptions());
	}

	@Inject
	public SqlParser(SqlParserOptions options) {
		requireNonNull(options, "options is null");
		allowedIdentifierSymbols = EnumSet.copyOf(options.getAllowedIdentifierSymbols());
	}

	public Statement createStatement(String sql) {
		return (Statement) invokeParser("statement", sql, SqlBaseParser::singleStatement);
	}

	public Expression createExpression(String expression) {
		return (Expression) invokeParser("expression", expression, SqlBaseParser::singleExpression);
	}

	private Node invokeParser(String name, String sql, Function<SqlBaseParser, ParserRuleContext> parseFunction) {
		try {
			SqlBaseLexer lexer = new SqlBaseLexer(new CaseInsensitiveStream(new ANTLRInputStream(sql)));
			CommonTokenStream tokenStream = new CommonTokenStream(lexer);
			SqlBaseParser parser = new SqlBaseParser(tokenStream);

			parser.addParseListener(new PostProcessor(Arrays.asList(parser.getRuleNames())));

			lexer.removeErrorListeners();
			lexer.addErrorListener(ERROR_LISTENER);

			parser.removeErrorListeners();
			parser.addErrorListener(ERROR_LISTENER);

			ParserRuleContext tree;
			try {
				// first, try parsing with potentially faster SLL mode
				parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
				tree = parseFunction.apply(parser);
			} catch (ParseCancellationException ex) {
				// if we fail, parse with LL mode
				tokenStream.seek(0); // rewind input stream
				parser.reset();

				parser.getInterpreter().setPredictionMode(PredictionMode.LL);
				tree = parseFunction.apply(parser);
			}

			return new AstBuilder().visit(tree);
		} catch (StackOverflowError e) {
			throw new ParsingException(name + " is too large (stack overflow while parsing)");
		}
	}

	private class PostProcessor extends SqlBaseBaseListener {

		private final List<String> ruleNames;

		public PostProcessor(List<String> ruleNames) {
			this.ruleNames = ruleNames;
		}

		@Override
		public void exitUnquotedIdentifier(SqlBaseParser.UnquotedIdentifierContext context) {
			String identifier = context.IDENTIFIER().getText();
			for (IdentifierSymbol identifierSymbol : EnumSet.complementOf(allowedIdentifierSymbols)) {
				char symbol = identifierSymbol.getSymbol();
				if (identifier.indexOf(symbol) >= 0) {
					throw new ParsingException("identifiers must not contain '" + identifierSymbol.getSymbol() + "'",
							null, context.IDENTIFIER().getSymbol().getLine(),
							context.IDENTIFIER().getSymbol().getCharPositionInLine());
				}
			}
		}

		@Override
		public void exitBackQuotedIdentifier(SqlBaseParser.BackQuotedIdentifierContext context) {
			Token token = context.BACKQUOTED_IDENTIFIER().getSymbol();
			throw new ParsingException(
					"backquoted identifiers are not supported; use double quotes to quote identifiers", null,
					token.getLine(), token.getCharPositionInLine());
		}

		@Override
		public void exitDigitIdentifier(SqlBaseParser.DigitIdentifierContext context) {
			Token token = context.DIGIT_IDENTIFIER().getSymbol();
			throw new ParsingException(
					"identifiers must not start with a digit; surround the identifier with double quotes", null,
					token.getLine(), token.getCharPositionInLine());
		}

		@Override
		public void exitNonReserved(SqlBaseParser.NonReservedContext context) {
			// we can't modify the tree during rule enter/exit event handling unless we're dealing with a terminal.
			// Otherwise, ANTLR gets confused an fires spurious notifications.
			if (!(context.getChild(0) instanceof TerminalNode)) {
				int rule = ((ParserRuleContext) context.getChild(0)).getRuleIndex();
				throw new AssertionError(
						"nonReserved can only contain tokens. Found nested rule: " + ruleNames.get(rule));
			}

			// replace nonReserved words with IDENT tokens
			context.getParent().removeLastChild();

			Token token = (Token) context.getChild(0).getPayload();
			context.getParent().addChild(new CommonToken(new Pair<>(token.getTokenSource(), token.getInputStream()),
					SqlBaseLexer.IDENTIFIER, token.getChannel(), token.getStartIndex(), token.getStopIndex()));
		}
	}
}
