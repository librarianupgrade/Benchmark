/*
   Copyright 2014 Immutables Authors and Contributors

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.immutables.generator.processor;

import org.immutables.trees.ast.Extractions;
import org.immutables.generator.processor.TreesAst.SimpleAccessExpression;
import org.immutables.generator.processor.TreesAst.ApplyExpression;
import org.immutables.generator.processor.TreesAst.AssignGenerator;
import org.immutables.generator.processor.TreesAst.Comment;
import org.immutables.generator.processor.TreesAst.Else;
import org.immutables.generator.processor.TreesAst.ElseIf;
import org.immutables.generator.processor.TreesAst.For;
import org.immutables.generator.processor.TreesAst.ForEnd;
import org.immutables.generator.processor.TreesAst.ForIterationAccessExpression;
import org.immutables.generator.processor.TreesAst.Identifier;
import org.immutables.generator.processor.TreesAst.If;
import org.immutables.generator.processor.TreesAst.IfEnd;
import org.immutables.generator.processor.TreesAst.InvokableDeclaration;
import org.immutables.generator.processor.TreesAst.Invoke;
import org.immutables.generator.processor.TreesAst.InvokeEnd;
import org.immutables.generator.processor.TreesAst.InvokeString;
import org.immutables.generator.processor.TreesAst.IterationGenerator;
import org.immutables.generator.processor.TreesAst.Let;
import org.immutables.generator.processor.TreesAst.LetEnd;
import org.immutables.generator.processor.TreesAst.Newline;
import org.immutables.generator.processor.TreesAst.Parameter;
import org.immutables.generator.processor.TreesAst.StringLiteral;
import org.immutables.generator.processor.TreesAst.Template;
import org.immutables.generator.processor.TreesAst.TextBlock;
import org.immutables.generator.processor.TreesAst.TextFragment;
import org.immutables.generator.processor.TreesAst.TransformGenerator;
import org.immutables.generator.processor.TreesAst.TypeDeclaration;
import org.immutables.generator.processor.TreesAst.TypeIdentifier;
import org.immutables.generator.processor.TreesAst.Unit;
import org.immutables.generator.processor.TreesAst.ValueDeclaration;
import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.DontLabel;
import org.parboiled.annotations.ExplicitActionsOnly;
import org.parboiled.annotations.MemoMismatches;
import org.parboiled.annotations.SuppressNode;
import org.parboiled.annotations.SuppressSubnodes;

@ExplicitActionsOnly
public class Parser extends BaseParser<Object> {

	public Rule Unit() {
		return Sequence(Unit.builder(), Spacing(), OneOrMore(
				Sequence(FirstOf(OpeningDirective(Comment()), TemplateDirective()), Unit.addParts(), Spacing())), EOI,
				Unit.build());
	}

	Rule Comment() {
		return Sequence(COMMENT, Optional(TextBlock(), Extractions.popped()), Comment.of());
	}

	Rule TextBlock() {
		return Sequence(TextBlock.builder(), TextFragment(), TextBlock.addParts(TextFragment.of()),
				ZeroOrMore(Sequence(Newline(), TextBlock.addParts(Newline.of()), TextFragment(),
						TextBlock.addParts(TextFragment.of()))),
				TextBlock.build());
	}

	Rule TemplateDirective() {
		return Sequence(Template.builder(), TemplateStart(), TemplateBody(), TemplateEnd(), Template.build());
	}

	Rule TemplateStart() {
		return OpeningDirective(Template());
	}

	Rule TemplateEnd() {
		return ClosingDirective(TEMPLATE);
	}

	Rule TemplateBody() {
		return Sequence(TextBlock(), Template.addParts(),
				ZeroOrMore(Sequence(Directive(), Template.addParts(), TextBlock(), Template.addParts())));
	}

	Rule DirectiveStart() {
		return OpeningDirective(
				FirstOf(Comment(), Let(), If(), ElseIf(), Else(), For(), InvokeString(), InvokeStart()));
	}

	Rule InvokeString() {
		return Sequence(StringLiteral(), InvokeString.of());
	}

	Rule InvokeEnd() {
		return Sequence(AccessExpression(), InvokeEnd.of());
	}

	Rule InvokeStart() {
		return Sequence(Invoke.builder(), AccessExpression(), Invoke.access(), Optional(Expression(), Invoke.invoke()),
				Invoke.build());
	}

	Rule DirectiveEnd() {
		return ClosingDirective(FirstOf(IfEnd(), LetEnd(), ForEnd(), InvokeEnd()));
	}

	Rule IfEnd() {
		return Sequence(IF, IfEnd.of());
	}

	Rule LetEnd() {
		return Sequence(LET, LetEnd.of());
	}

	Rule ForEnd() {
		return Sequence(FOR, ForEnd.of());
	}

	Rule Directive() {
		return FirstOf(DirectiveEnd(), DirectiveStart());
	}

	Rule OpeningDirective(Rule directive) {
		return Sequence("[", directive, "]");
	}

	Rule ClosingDirective(Rule directive) {
		return Sequence("[/", directive, "]");
	}

	Rule Parens(Rule expression) {
		return Sequence(Literal("("), expression, Literal(")"));
	}

	Rule AccessExpression() {
		return Sequence(SimpleAccessExpression.builder(), Identifier(), SimpleAccessExpression.addPath(),
				ZeroOrMore(Sequence(DOT, Identifier(), SimpleAccessExpression.addPath())),
				SimpleAccessExpression.build());
	}

	Rule GeneratorDeclaration() {
		return FirstOf(TransformGenerator(), AssignGenerator(), IterationGenerator());
	}

	Rule IterationGenerator() {
		return Sequence(IterationGenerator.builder(), ValueDeclaration(), IterationGenerator.declaration(), IN,
				Expression(), IterationGenerator.from(), Optional(IF, Expression(), IterationGenerator.condition()),
				IterationGenerator.build());
	}

	Rule AssignGenerator() {
		return Sequence(AssignGenerator.builder(), ValueDeclaration(), AssignGenerator.declaration(), ASSIGN,
				Expression(), AssignGenerator.from(), AssignGenerator.build());
	}

	Rule TransformGenerator() {
		return Sequence(TransformGenerator.builder(), ValueDeclaration(), TransformGenerator.declaration(), ASSIGN,
				Expression(), TransformGenerator.transform(), FOR, ValueDeclaration(),
				TransformGenerator.varDeclaration(), IN, Expression(), TransformGenerator.from(),
				Optional(IF, Expression(), TransformGenerator.condition()), TransformGenerator.build());
	}

	@DontLabel
	Rule DisambiguatedExpression() {
		return FirstOf(Parens(ApplyExpression()), ForIterationAccessExpression(), AccessExpression(), StringLiteral());
	}

	Rule ForIterationAccessExpression() {
		return Sequence(FOR, DOT, AccessExpression(), ForIterationAccessExpression.of());
	}

	Rule Expression() {
		return FirstOf(Parens(ApplyExpression()), ApplyExpression());
	}

	Rule ApplyExpression() {
		return Sequence(ApplyExpression.builder(), OneOrMore(DisambiguatedExpression(), ApplyExpression.addParams()),
				ApplyExpression.build());
	}

	Rule If() {
		return Sequence(IF, If.builder(), Expression(), If.condition(), If.build());
	}

	Rule ElseIf() {
		return Sequence(ELSE, IF, ElseIf.builder(), Expression(), ElseIf.condition(), ElseIf.build());
	}

	Rule Else() {
		return Sequence(ELSE, Else.of());
	}

	Rule For() {
		return FirstOf(Sequence(FOR, DOT, Invoke.builder(), AccessExpression(), ForIterationAccessExpression.of(),
				Invoke.access(), Invoke.build()), Sequence(FOR, For.builder(), ForDeclaration(), For.build()));
	}

	Rule ForDeclaration() {
		return Sequence(GeneratorDeclaration(), For.addDeclaration(),
				ZeroOrMore(Sequence(COMMA, GeneratorDeclaration(), For.addDeclaration())));
	}

	Rule Let() {
		return Sequence(LET, Let.builder(), InvokableDeclaration(), Let.declaration(), Let.build());
	}

	Rule ValueDeclaration() {
		return Sequence(ValueDeclaration.builder(), Optional(Type(), ValueDeclaration.type()), Name(),
				ValueDeclaration.name(), ValueDeclaration.build());
	}

	Rule InvokableDeclaration() {
		return Sequence(InvokableDeclaration.builder(), Name(), InvokableDeclaration.name(),
				ZeroOrMore(ParameterDeclaration()), InvokableDeclaration.build());
	}

	Rule ParameterDeclaration() {
		return Sequence(Parameter.builder(), Type(), Parameter.type(), Name(), Parameter.name(), Parameter.build(),
				InvokableDeclaration.addParameters());
	}

	Rule Template() {
		return Sequence(TEMPLATE, Optional(PUBLIC, Template.isPublic(true)), InvokableDeclaration(),
				Template.declaration());
	}

	Rule Name() {
		return Identifier();
	}

	Rule Type() {
		return Sequence(TypeDeclaration.builder(), TypeIdentifer(), TypeDeclaration.type(),
				Optional(Ellipsis(), TypeDeclaration.kind(Trees.TypeDeclaration.Kind.ITERABLE)),
				TypeDeclaration.build());
	}

	Rule COMMENT = Literal("--");
	Rule ASSIGN = Literal("=");
	Rule DOT = Literal(".");
	Rule COMMA = Literal(",");
	Rule ELLIPSIS = Literal("...");
	Rule IN = Literal(KEYWORD_IN);
	Rule FOR = Literal(KEYWORD_FOR);
	Rule LET = Literal(KEYWORD_LET);
	Rule IF = Literal(KEYWORD_IF);
	Rule ELSE = Literal(KEYWORD_ELSE);
	Rule TEMPLATE = Literal(KEYWORD_TEMPLATE);
	Rule PUBLIC = Literal(KEYWORD_PUBLIC);

	private static final String KEYWORD_IN = "in";
	private static final String KEYWORD_FOR = "for";
	private static final String KEYWORD_LET = "let";
	private static final String KEYWORD_IF = "if";
	private static final String KEYWORD_ELSE = "else";
	private static final String KEYWORD_TEMPLATE = "template";
	private static final String KEYWORD_PUBLIC = "public";

	@MemoMismatches
	@SuppressNode
	Rule Keyword() {
		return Sequence(FirstOf(KEYWORD_IN, KEYWORD_FOR, KEYWORD_LET, KEYWORD_IF, KEYWORD_ELSE, KEYWORD_TEMPLATE,
				KEYWORD_PUBLIC), TestNot(LetterOrDigit()));
	}

	@DontLabel
	@SuppressSubnodes
	Rule Literal(String string) {
		return Sequence(String(string), Spacing());
	}

	@SuppressSubnodes
	@MemoMismatches
	Rule Identifier() {
		return Sequence(TestNot(Keyword()),
				Sequence(Sequence(IdentifierStartLetter(), ZeroOrMore(LetterOrDigit())), Identifier.of()), Spacing());
	}

	@SuppressSubnodes
	@MemoMismatches
	Rule StringLiteral() {
		return Sequence("'", ZeroOrMore(NoneOf("'")), StringLiteral.of(), "'", Spacing());
	}

	@SuppressSubnodes
	@MemoMismatches
	Rule TypeIdentifer() {
		return Sequence(TestNot(Keyword()),
				Sequence(Sequence(TypeStartLetter(), ZeroOrMore(LetterOrDigit())), TypeIdentifier.of()), Spacing());
	}

	@SuppressSubnodes
	Rule Newline() {
		return FirstOf("\n", "\n\r");
	}

	@SuppressSubnodes
	Rule TextFragment() {
		return ZeroOrMore(NoneOf("[]\n\r"));
	}

	@SuppressSubnodes
	Rule Ellipsis() {
		return Sequence(ELLIPSIS, Spacing());
	}

	Rule IdentifierStartLetter() {
		return FirstOf(CharRange('a', 'z'), '_');
	}

	Rule TypeStartLetter() {
		return CharRange('A', 'Z');
	}

	@MemoMismatches
	Rule Letter() {
		return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), '_', '$');
	}

	@MemoMismatches
	Rule LetterOrDigit() {
		return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), CharRange('0', '9'), '_', '$');
	}

	@SuppressNode
	Rule Spacing() {
		return ZeroOrMore(AnyOf(" \n\r\f\t"));
	}
}
