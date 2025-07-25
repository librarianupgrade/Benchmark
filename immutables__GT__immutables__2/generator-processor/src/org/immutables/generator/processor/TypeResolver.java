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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import org.immutables.generator.processor.ImmutableTrees.ApplyExpression;
import org.immutables.generator.processor.ImmutableTrees.AssignGenerator;
import org.immutables.generator.processor.ImmutableTrees.BoundAccessExpression;
import org.immutables.generator.processor.ImmutableTrees.ForIterationAccessExpression;
import org.immutables.generator.processor.ImmutableTrees.ForStatement;
import org.immutables.generator.processor.ImmutableTrees.Identifier;
import org.immutables.generator.processor.ImmutableTrees.IterationGenerator;
import org.immutables.generator.processor.ImmutableTrees.LetStatement;
import org.immutables.generator.processor.ImmutableTrees.Parameter;
import org.immutables.generator.processor.ImmutableTrees.ResolvedType;
import org.immutables.generator.processor.ImmutableTrees.SimpleAccessExpression;
import org.immutables.generator.processor.ImmutableTrees.Template;
import org.immutables.generator.processor.ImmutableTrees.TransformGenerator;
import org.immutables.generator.processor.ImmutableTrees.TypeDeclaration;
import org.immutables.generator.processor.ImmutableTrees.Unit;
import org.immutables.generator.processor.Trees.AccessExpression;
import org.immutables.generator.processor.Trees.Expression;
import org.immutables.generator.processor.Trees.TemplatePart;
import org.immutables.generator.processor.Trees.TypeDeclaration.Kind;
import org.immutables.generator.processor.Trees.TypeIdentifier;
import org.immutables.generator.processor.Trees.TypeReference;
import static com.google.common.base.Preconditions.checkArgument;

public final class TypeResolver {
	static final String ITERATION_ACCESS_VARIABLE = "for";

	private final SwissArmyKnife knife;

	public TypeResolver(SwissArmyKnife knife) {
		this.knife = knife;
	}

	public static class TypingException extends RuntimeException {
		TypingException(String message) {
			super(message);
		}
	}

	public Unit resolve(Unit unit) {
		return new Transformer(new Scope()).toUnit(new ForIterationAccessTransformer().toUnit(unit));
	}

	private enum InferencePurpose {
		/** T from T. */
		ASSIGN,
		/** T from T[]. */
		ITERATE,
		/** T[] from T. */
		COLLECT;
	}

	private class Scope {
		final Map<String, TypeMirror> locals = Maps.newLinkedHashMap();

		Scope nest() {
			Scope nested = new Scope();
			nested.locals.putAll(locals);
			return nested;
		}

		Trees.TypeReference declare(Trees.TypeDeclaration type, Trees.Identifier name) {
			if (isDeclared(name)) {
				throw new TypingException(String.format("Redeclaration of local %s", name));
			}
			return declare(resolve(type), name);
		}

		/**
		 * Declare template or invokable. There's no {{@code isDeclared} check because we potentially
		 * might allow to define several templates with the same name but different types of arguments,
		 * to be resolved at runtime. (akin to multimethods). Might need to check if the same
		 * combination of parameters was already used.
		 * @param name identifier
		 * @return resolved type
		 */
		Trees.TypeReference declareInvokable(Trees.Identifier name) {
			return declare(knife.accessors.invokableType, name);
		}

		Trees.TypeReference declareForIterationAccess(Trees.Identifier name) {
			return declare(knife.accessors.iterationType, name);
		}

		boolean isDeclared(Trees.Identifier name) {
			return locals.containsKey(name.value());
		}

		Trees.TypeReference declare(TypeMirror type, Trees.Identifier name) {
			locals.put(name.value(), type);
			return ResolvedType.of(type);
		}

		TypeMirror resolve(Trees.TypeDeclaration type) {
			TypeMirror resolved = knife.imports.get(type.type().value());
			if (resolved == null) {
				throw new TypingException(String.format("Could not resolve %s simple type", type));
			}
			if (type.kind() == Trees.TypeDeclaration.Kind.ITERABLE) {
				resolved = makeIterableTypeOf(resolved);
			}
			return resolved;
		}

		DeclaredType makeIterableTypeOf(TypeMirror resolved) {
			return knife.types.getDeclaredType(knife.accessors.iterableElement, resolved);
		}

		BoundAccessExpression resolveAccess(Trees.AccessExpression expression) {
			if (expression instanceof BoundAccessExpression) {
				return (BoundAccessExpression) expression;
			}

			try {

				BoundAccessExpression.Builder builder = BoundAccessExpression.builder().addAllPath(expression.path());

				Accessors.BoundAccess accessor = null;
				for (Trees.Identifier identifier : expression.path()) {
					accessor = bindAccess(accessor, identifier.value());
					builder.addAccessor(accessor);
				}

				return builder.build();

			} catch (Exception ex) {
				RuntimeException exception = new RuntimeException("Path " + expression.path() + ": " + ex.getMessage());
				exception.setStackTrace(ex.getStackTrace());
				throw exception;
			}
		}

		Accessors.BoundAccess bindAccess(@Nullable Accessors.BoundAccess previous, String name) {
			return previous != null ? knife.binder.bind(previous.type, name)
					: knife.binder.bindLocalOrThis(knife.type.asType(), name, locals);
		}

		Trees.ValueDeclaration inferType(Trees.ValueDeclaration declaration, Trees.Expression expression,
				InferencePurpose inferenceKind) {

			if (expression instanceof BoundAccessExpression) {
				BoundAccessExpression scopeBoundAccess = (BoundAccessExpression) expression;
				Accessors.BoundAccess lastAccess = Iterables.getLast(asBoundAccess(scopeBoundAccess.accessor()));

				if (inferenceKind == InferencePurpose.ITERATE) {
					if (!lastAccess.isContainer()) {
						throw new TypingException(String.format("Not iterable type '%s'%n\tin expression '%s'",
								lastAccess.type, scopeBoundAccess.path()));
					}
				}

				if (declaration.type().isPresent()) {
					return declaration.withType(resolveDeclared(declaration.type().get(), declaration.name()))
							.withContainedType(ResolvedType.of(resolveType(declaration.type().get(), false)));
				}

				if (inferenceKind == InferencePurpose.ITERATE) {
					TypeMirror resolved = lastAccess.containedType;
					return declaration.withType(declare(resolved, declaration.name()))
							.withContainedType(ResolvedType.of(resolved));
				} else if (inferenceKind == InferencePurpose.COLLECT) {
					TypeMirror resolved = knife.accessors.wrapIterable(lastAccess.type);
					return declaration.withType(declare(resolved, declaration.name()))
							.withContainedType(ResolvedType.of(lastAccess.type));
				} else {
					return declaration.withType(declare(lastAccess.type, declaration.name()))
							.withContainedType(ResolvedType.of(lastAccess.type));
				}
			}

			if (declaration.type().isPresent()) {
				return declaration.withType(resolveDeclared(declaration.type().get(), declaration.name()))
						.withContainedType(ResolvedType.of(resolveType(declaration.type().get(), false)));
			}

			throw new TypingException(
					String.format("Value should be typed %s%n\texpression '%s'", declaration.name(), expression));
		}

		private TypeReference resolveDeclared(TypeReference typeReference, Trees.Identifier name) {
			return declare(resolveType(typeReference, true), name);
		}

		private TypeMirror resolveType(TypeReference typeReference, boolean wrapIterable) {
			Preconditions.checkState(typeReference instanceof TypeDeclaration);
			TypeDeclaration typeDeclaration = (TypeDeclaration) typeReference;
			TypeIdentifier type = typeDeclaration.type();
			@Nullable
			TypeMirror resolved = knife.imports.get(type.value());
			if (resolved == null) {
				throw new TypingException(String.format("Could not resolve declared type '%s'", typeDeclaration));
			}
			if (wrapIterable && typeDeclaration.kind() == Kind.ITERABLE) {
				resolved = knife.accessors.wrapIterable(resolved);
			}
			return resolved;
		}
	}

	private static final class ForIterationAccessTransformer extends TreesTransformer {
		@Override
		protected Expression asExpression(ForIterationAccessExpression expression) {
			return SimpleAccessExpression.builder().addPath(Identifier.of(ITERATION_ACCESS_VARIABLE))
					.addAllPath(expression.access().path()).build();
		}
	}

	private static final class Transformer extends TreesTransformer {
		private final Scope scope;

		public Transformer(Scope scope) {
			this.scope = scope;
		}

		@Override
		public Unit toUnit(Unit unit) {
			for (Template template : Iterables.filter(unit.parts(), Template.class)) {
				scope.declareInvokable(template.declaration().name());
			}
			return super.toUnit(unit);
		}

		@Override
		public AssignGenerator toAssignGenerator(AssignGenerator value) {
			AssignGenerator generator = super.toAssignGenerator(value);
			return generator.withDeclaration(
					scope.inferType(generator.declaration(), generator.from(), InferencePurpose.ASSIGN));
		}

		@Override
		protected TemplatePart asTemplatePart(LetStatement statement) {
			scope.declareInvokable(statement.declaration().name());
			return super.asTemplatePart(statement);
		}

		@Override
		public IterationGenerator toIterationGenerator(IterationGenerator value) {
			IterationGenerator generator = super.toIterationGenerator(value);

			return generator
					.withDeclaration(
							scope.inferType(generator.declaration(), generator.from(), InferencePurpose.ITERATE))
					.withCondition(asIterationGeneratorConditionAfterDeclaration(generator, generator.condition()));
		}

		@Override
		public TransformGenerator toTransformGenerator(TransformGenerator value) {
			TransformGenerator generator = super.toTransformGenerator(value);

			// first we resolve/inference type for intermetiate iteration var,
			// then we resolve condition and transform expressions
			generator = generator
					.withVarDeclaration(
							scope.inferType(generator.varDeclaration(), generator.from(), InferencePurpose.ITERATE))
					.withCondition(asTransformGeneratorConditionAfterDeclaration(generator, generator.condition()))
					.withTransform(asTransformGeneratorTransformAfterDeclaration(generator, generator.transform()));

			// Only after transform expression is resolved, we could infer type for whole declaration
			return generator.withDeclaration(
					scope.inferType(generator.declaration(), generator.transform(), InferencePurpose.COLLECT));
		}

		private Optional<Expression> asIterationGeneratorConditionAfterDeclaration(IterationGenerator generator,
				Optional<Expression> condition) {
			if (condition.isPresent()) {
				// Calling actual transformation
				return Optional.of(super.asIterationGeneratorCondition(generator, condition.get()));
			}
			return Optional.absent();
		}

		private Optional<Expression> asTransformGeneratorConditionAfterDeclaration(TransformGenerator generator,
				Optional<Expression> condition) {
			if (condition.isPresent()) {
				// Calling actual transformation
				return Optional.of(super.asTransformGeneratorCondition(generator, condition.get()));
			}
			return Optional.absent();
		}

		private Expression asTransformGeneratorTransformAfterDeclaration(TransformGenerator generator,
				Expression condition) {
			return super.asTransformGeneratorTransform(generator, condition);
		}

		/** We prevent transformation here to manually do it after variable declaration is done. */
		@Override
		protected Expression asIterationGeneratorCondition(IterationGenerator value, Expression element) {
			return simplifyExpression(element);
		}

		/** We prevent transformation here to manually do it after variable declaration is done. */
		@Override
		protected Expression asTransformGeneratorCondition(TransformGenerator value, Expression element) {
			return simplifyExpression(element);
		}

		/** We prevent transformation here to manually do it after variable declaration is done. */
		@Override
		protected Expression asTransformGeneratorTransform(TransformGenerator value, Expression element) {
			return simplifyExpression(element);
		}

		@Override
		public Parameter toParameter(Parameter parameter) {
			return parameter.withType(scope.declare((Trees.TypeDeclaration) parameter.type(), parameter.name()));
		}

		/** Overriden to specify order in which we process declaration first, and then parts. */
		@Override
		public Template toTemplate(Template template) {
			try {
				Scope nestedScope = scope.nest();
				Transformer nested = new Transformer(nestedScope);
				return template.withDeclaration(nested.asTemplateDeclaration(template, template.declaration()))
						.withParts(nested.asTemplatePartsElements(template, template.parts()));
			} catch (RuntimeException ex) {
				throw new RuntimeException("In template " + template.declaration().name() + ": " + ex.getMessage(), ex);
			}
		}

		/** Overriden to specify order in which we process declaration first, and then parts. */
		@Override
		public LetStatement toLetStatement(LetStatement statement) {
			Transformer nested = new Transformer(scope.nest());
			return statement.withDeclaration(nested.asLetStatementDeclaration(statement, statement.declaration()))
					.withParts(nested.asLetStatementPartsElements(statement, statement.parts()));
		}

		/** Overriden to specify order in which we process declaration first, and then parts. */
		@Override
		public ForStatement toForStatement(ForStatement statement) {
			Scope nestedScope = scope.nest();
			nestedScope.declareForIterationAccess(Identifier.of(ITERATION_ACCESS_VARIABLE));
			Transformer nested = new Transformer(nestedScope);
			return statement
					.withDeclaration(nested.asForStatementDeclarationElements(statement, statement.declaration()))
					.withParts(nested.asForStatementPartsElements(statement, statement.parts()));
		}

		@Override
		protected Iterable<TemplatePart> asForStatementPartsElements(ForStatement value,
				List<TemplatePart> collection) {
			return super.asForStatementPartsElements(value, collection);
		}

		/**
		 * Resolve accesors and types on {@link SimpleAccessExpression}, turning it into
		 * {@link BoundAccessExpression}
		 */
		@Override
		protected AccessExpression asAccessExpression(SimpleAccessExpression value) {
			return scope.resolveAccess(value);
		}

		@Override
		protected Expression asExpression(ApplyExpression value) {
			return simplifyExpression(super.asExpression(value));
		}

		@Override
		protected Expression asExpression(SimpleAccessExpression value) {
			return scope.resolveAccess(value);
		}

		private Expression simplifyExpression(Trees.Expression expression) {
			if (expression instanceof ApplyExpression) {
				ImmutableList<Trees.Expression> params = ((ApplyExpression) expression).params();
				if (params.size() == 1) {
					return params.get(0);
				}
			}
			return expression;
		}
	}

	public static ImmutableList<Accessors.BoundAccess> asBoundAccess(Iterable<?> iterable) {
		for (Object object : iterable) {
			checkArgument(object instanceof Accessors.BoundAccess);
		}
		return FluentIterable.from(iterable).filter(Accessors.BoundAccess.class).toList();
	}
}
