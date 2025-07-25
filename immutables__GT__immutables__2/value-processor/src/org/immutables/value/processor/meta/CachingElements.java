/*
   Copyright 2015 Immutables Authors and Contributors

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
package org.immutables.value.processor.meta;

import com.google.common.collect.Lists;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import org.immutables.generator.Delegated;

/**
 * Some annotation processors have {@code javax.lang.model} being implemented using relatively
 * expensive conversions from internal model. When some properties are being queried again and
 * again, annotation mirrors or enclosed elements are worth to store. Implementations wrappers cache
 * some properties eagerly and some lazily.
 */
public final class CachingElements {
	private CachingElements() {
	}

	public static Element asCaching(Element element) {
		if (element instanceof Caching) {
			return element;
		}
		return new CachingElement(element);
	}

	public static TypeElement asCaching(TypeElement element) {
		if (element instanceof Caching) {
			return element;
		}
		return new CachingTypeElement(element);
	}

	public static <E extends Element> E getDelegate(E element) {
		return Delegated.unwrap(element);
	}

	public static <E extends AnnotationMirror> E getDelegate(E element) {
		return Delegated.unwrap(element);
	}

	public static PackageElement asCaching(PackageElement element) {
		if (element instanceof Caching) {
			return element;
		}
		return new CachingPackageElement(element);
	}

	public static ExecutableElement asCaching(ExecutableElement element) {
		if (element instanceof Caching) {
			return element;
		}
		return new CachingExecutableElement(element);
	}

	public static AnnotationMirror asCaching(AnnotationMirror mirror) {
		if (mirror instanceof Caching) {
			return mirror;
		}
		return new CachingAnnotationMirror(mirror);
	}

	public static boolean equals(Element left, Element right) {
		return getDelegate(left).equals(getDelegate(right));
	}

	private static List<AnnotationMirror> asCaching(List<? extends AnnotationMirror> mirrors) {
		List<AnnotationMirror> cachingMirrors = Lists.newArrayListWithCapacity(mirrors.size());
		for (AnnotationMirror mirror : mirrors) {
			cachingMirrors.add(new CachingAnnotationMirror(mirror));
		}
		return cachingMirrors;
	}

	private interface Caching extends Delegated {
	}

	private static class CachingExecutableElement extends CachingElement implements ExecutableElement {
		private final ExecutableElement delegate;
		private final TypeMirror returnType;
		// not volatile, it's ok to have some additional instances at race condition
		private List<? extends VariableElement> parameters;
		// not volatile, it's ok to have some additional instances at race condition
		private List<? extends TypeParameterElement> typeParameters;
		// not volatile, it's ok to have some additional instances at race condition
		private AnnotationValue defaultValue;

		CachingExecutableElement(ExecutableElement delegate) {
			super(delegate);
			this.delegate = delegate;
			this.returnType = delegate.getReturnType();
		}

		@Override
		public List<? extends VariableElement> getParameters() {
			List<? extends VariableElement> ps = parameters;
			if (ps == null) {
				ps = delegate.getParameters();
				parameters = ps;
			}
			return ps;
		}

		@Override
		public List<? extends TypeParameterElement> getTypeParameters() {
			List<? extends TypeParameterElement> tps = typeParameters;
			if (tps == null) {
				tps = delegate.getTypeParameters();
				typeParameters = tps;
			}
			return tps;
		}

		@Override
		public TypeMirror getReturnType() {
			return returnType;
		}

		@Override
		public List<? extends TypeMirror> getThrownTypes() {
			return delegate.getThrownTypes();
		}

		@Override
		public AnnotationValue getDefaultValue() {
			AnnotationValue dv = defaultValue;
			if (dv == null) {
				dv = delegate.getDefaultValue();
				defaultValue = dv;
			}
			return dv;
		}

		@Override
		public boolean isVarArgs() {
			return delegate.isVarArgs();
		}

		@SuppressWarnings("unused")
		public boolean isDefault() {
			return delegate.isDefault();
		}

		@SuppressWarnings("unused")
		public TypeMirror getReceiverType() {
			return delegate.getReceiverType();
		}
	}

	private static class CachingPackageElement extends CachingElement implements PackageElement {
		private final PackageElement delegate;
		private final Name qualifiedName;

		CachingPackageElement(PackageElement delegate) {
			super(delegate);
			this.delegate = delegate;
			this.qualifiedName = delegate.getQualifiedName();
		}

		@Override
		public Name getQualifiedName() {
			return qualifiedName;
		}

		@Override
		public boolean isUnnamed() {
			return delegate.isUnnamed();
		}
	}

	private static class CachingTypeElement extends CachingElement implements TypeElement {
		private final TypeElement delegate;
		private final Name qualifiedName;
		// not volatile, it's ok to have some additional instances at race condition
		private List<? extends TypeParameterElement> typeParameters;

		CachingTypeElement(TypeElement delegate) {
			super(delegate);
			this.delegate = delegate;
			this.qualifiedName = delegate.getQualifiedName();
		}

		@Override
		public NestingKind getNestingKind() {
			return delegate.getNestingKind();
		}

		@Override
		public Name getQualifiedName() {
			return qualifiedName;
		}

		@Override
		public TypeMirror getSuperclass() {
			return delegate.getSuperclass();
		}

		@Override
		public List<? extends TypeMirror> getInterfaces() {
			return delegate.getInterfaces();
		}

		@Override
		public List<? extends TypeParameterElement> getTypeParameters() {
			List<? extends TypeParameterElement> tps = typeParameters;
			if (tps == null) {
				tps = delegate.getTypeParameters();
				typeParameters = tps;
			}
			return tps;
		}
	}

	private static class CachingElement implements Element, Caching {
		private final Element delegate;
		private final ElementKind kind;
		private final Name simpleName;
		private final Set<Modifier> modifiers;
		// not volatile, it's ok to have some additional instances at race condition
		private Element enclosingElement;
		// not volatile, it's ok to have some additional instances at race condition
		private List<? extends Element> enclosedElements;
		// not volatile, it's ok to have some additional instances at race condition
		private List<? extends AnnotationMirror> annotationMirrors;

		CachingElement(Element delegate) {
			this.delegate = delegate;
			this.kind = delegate.getKind();
			this.simpleName = delegate.getSimpleName();
			this.modifiers = delegate.getModifiers();
		}

		@Override
		public Element delegate() {
			return delegate;
		}

		@Override
		public List<? extends AnnotationMirror> getAnnotationMirrors() {
			List<? extends AnnotationMirror> ms = annotationMirrors;
			if (ms == null) {
				ms = asCaching(delegate.getAnnotationMirrors());
				annotationMirrors = ms;
			}
			return ms;
		}

		@Override
		public Set<Modifier> getModifiers() {
			return modifiers;
		}

		@Override
		public Name getSimpleName() {
			return simpleName;
		}

		@Override
		public Element getEnclosingElement() {
			Element e = enclosingElement;
			if (e == null) {
				e = delegate.getEnclosingElement();
				enclosingElement = e;
			}
			return e;
		}

		@Override
		public List<? extends Element> getEnclosedElements() {
			List<? extends Element> es = enclosedElements;
			if (es == null) {
				es = delegate.getEnclosedElements();
				enclosedElements = es;
			}
			return es;
		}

		@Override
		public TypeMirror asType() {
			return delegate.asType();
		}

		@Override
		public ElementKind getKind() {
			return kind;
		}

		@Override
		public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
			return delegate.getAnnotation(annotationType);
		}

		@Override
		public boolean equals(Object obj) {
			return delegate.equals(obj);
		}

		@Override
		public int hashCode() {
			return delegate.hashCode();
		}

		@Override
		public <R, P> R accept(ElementVisitor<R, P> v, P p) {
			return delegate.accept(v, p);
		}

		@SuppressWarnings("unused")
		public <A extends Annotation> A[] getAnnotationsByType(Class<A> type) {
			return delegate.getAnnotationsByType(type);
		}

		@Override
		public String toString() {
			return delegate.toString();
		}
	}

	private static class CachingAnnotationMirror implements AnnotationMirror, Caching {
		private final AnnotationMirror delegate;
		// not volatile, it's ok to have some additional instances at race condition
		private Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues;
		private final DeclaredType annotationType;

		CachingAnnotationMirror(AnnotationMirror delegate) {
			this.delegate = delegate;
			this.annotationType = new CachingDeclaredType(delegate.getAnnotationType());
		}

		@Override
		public DeclaredType getAnnotationType() {
			return annotationType;
		}

		@Override
		public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
			Map<? extends ExecutableElement, ? extends AnnotationValue> vs = elementValues;
			if (vs == null) {
				vs = delegate.getElementValues();
				elementValues = vs;
			}
			return vs;
		}

		@Override
		public AnnotationMirror delegate() {
			return delegate;
		}

		@Override
		public String toString() {
			return delegate.toString();
		}
	}

	private static class CachingDeclaredType implements DeclaredType, Caching {
		private final DeclaredType delegate;
		private final TypeKind kind;
		private final Element element;

		CachingDeclaredType(DeclaredType delegate) {
			this.delegate = delegate;
			this.kind = delegate.getKind();
			this.element = new CachingTypeElement((TypeElement) delegate.asElement());
		}

		@Override
		public DeclaredType delegate() {
			return delegate;
		}

		@Override
		public Element asElement() {
			return element;
		}

		@Override
		public TypeKind getKind() {
			return kind;
		}

		@Override
		public <R, P> R accept(TypeVisitor<R, P> v, P p) {
			return delegate.accept(v, p);
		}

		@Override
		public TypeMirror getEnclosingType() {
			return delegate.getEnclosingType();
		}

		@Override
		public List<? extends TypeMirror> getTypeArguments() {
			return delegate.getTypeArguments();
		}

		@SuppressWarnings("unused")
		public <A extends Annotation> A getAnnotation(Class<A> type) {
			return delegate.getAnnotation(type);
		}

		@SuppressWarnings("unused")
		public <A extends Annotation> A[] getAnnotationsByType(Class<A> type) {
			return delegate.getAnnotationsByType(type);
		}

		@SuppressWarnings("unused")
		public List<? extends AnnotationMirror> getAnnotationMirrors() {
			return delegate.getAnnotationMirrors();
		}

		@Override
		public String toString() {
			return delegate.toString();
		}
	}
}
