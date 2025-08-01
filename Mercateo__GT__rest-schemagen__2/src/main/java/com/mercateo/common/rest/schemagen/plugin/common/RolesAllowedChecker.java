package com.mercateo.common.rest.schemagen.plugin.common;

import static java.util.Objects.requireNonNull;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.server.model.AnnotatedMethod;

import com.mercateo.common.rest.schemagen.link.ScopeMethod;
import com.mercateo.common.rest.schemagen.plugin.MethodCheckerForLink;

/**
 * copied from {@link RolesAllowedDynamicFeature}
 * 
 * @author joerg.adler
 *
 */
public class RolesAllowedChecker implements MethodCheckerForLink {

	private SecurityContext securityContext;

	public RolesAllowedChecker(SecurityContext securityContext) {
		this.securityContext = requireNonNull(securityContext);
	}

	@Override
	public boolean test(ScopeMethod scopeMethod) {

		AnnotatedMethod am = new AnnotatedMethod(scopeMethod.getInvokedMethod());

		// DenyAll on the method take precedence over RolesAllowed and PermitAll
		if (am.isAnnotationPresent(DenyAll.class)) {
			return false;
		}

		// RolesAllowed on the method takes precedence over PermitAll
		RolesAllowed ra = am.getAnnotation(RolesAllowed.class);
		if (ra != null) {
			return checkRoles(ra.value());
		}

		// PermitAll takes precedence over RolesAllowed on the class
		if (am.isAnnotationPresent(PermitAll.class)) {
			// Do nothing.
			return true;
		}

		// DenyAll can't be attached to classes

		// RolesAllowed on the class takes precedence over PermitAll
		ra = scopeMethod.getInvokedClass().getAnnotation(RolesAllowed.class);
		if (ra != null) {
			return checkRoles(ra.value());
		}
		return true;
	}

	private boolean checkRoles(String[] rolesAllowed) {
		for (String role : rolesAllowed) {
			if (securityContext.isUserInRole(role)) {
				return true;
			}
		}
		return false;
	}
}
