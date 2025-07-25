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
package org.immutables.value.processor.meta;

import com.google.common.base.Optional;
import java.lang.annotation.Annotation;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import org.immutables.value.Value;

@Value.Immutable(builder = false)
public abstract class Reporter {

	@Value.Parameter
	abstract Messager messager();

	abstract Optional<Element> element();

	abstract Optional<AnnotationMirror> annotation();

	public abstract Reporter withAnnotation(AnnotationMirror mirror);

	public abstract Reporter withElement(Element mirror);

	public static Reporter from(ProcessingEnvironment processing) {
		return ImmutableReporter.of(processing.getMessager());
	}

	public Reporter forAnnotation(Class<? extends Annotation> annotationType) {
		return annotationNamed(annotationType.getSimpleName());
	}

	public Reporter annotationNamed(String simpleName) {
		if (element().isPresent()) {
			for (AnnotationMirror mirror : element().get().getAnnotationMirrors()) {
				if (mirror.getAnnotationType().asElement().getSimpleName().contentEquals(simpleName)) {
					return withAnnotation(mirror);
				}
			}
		}
		return this;
	}

	public void error(String message, Object... parameters) {
		reportMessage(Diagnostic.Kind.ERROR, message, parameters);
	}

	public void warning(String message, Object... parameters) {
		warning(About.ANY, message, parameters);
	}

	public enum About {
		ANY, FROM, SUBTYPE, UNTYPE, INCOMPAT
	}

	public void warning(About warn, String message, Object... parameters) {
		SuppressedWarnings suppressed = SuppressedWarnings.forElement(getElement(), false, false);
		if (suppressed.immutables) {
			return;
		}
		switch (warn) {
		case FROM:
			if (suppressed.from) {
				return;
			}
			break;
		case SUBTYPE:
			if (suppressed.subtype) {
				return;
			}
			break;
		case UNTYPE:
			if (suppressed.untype) {
				return;
			}
			break;
		case INCOMPAT:
			if (suppressed.incompat) {
				return;
			}
			break;
		default:
			break;
		}
		if (warn == About.ANY) {
			message = "(immutables) " + message;
		} else {
			message = "(immutables:" + warn.name().toLowerCase() + ") " + message;
		}
		reportMessage(Diagnostic.Kind.MANDATORY_WARNING, message, parameters);
	}

	private void reportMessage(Diagnostic.Kind messageKind, String message, Object... parameters) {
		String formattedMessage = String.format(message, parameters);
		if (element().isPresent() && annotation().isPresent()) {
			messager().printMessage(messageKind, formattedMessage, getElement(), getAnnotation());
		} else if (element().isPresent()) {
			messager().printMessage(messageKind, formattedMessage, getElement());
		} else {
			messager().printMessage(messageKind, formattedMessage);
		}
	}

	private AnnotationMirror getAnnotation() {
		return CachingElements.getDelegate(annotation().get());
	}

	private Element getElement() {
		return CachingElements.getDelegate(element().get());
	}
}
