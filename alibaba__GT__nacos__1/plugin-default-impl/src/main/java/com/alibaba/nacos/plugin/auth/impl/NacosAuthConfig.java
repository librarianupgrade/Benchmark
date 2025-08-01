/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.plugin.auth.impl;

import com.alibaba.nacos.auth.config.AuthConfigs;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.code.ControllerMethodsCache;
import com.alibaba.nacos.plugin.auth.impl.authenticate.AuthenticationNamagerDelegator;
import com.alibaba.nacos.plugin.auth.impl.authenticate.DefaultAuthenticationManager;
import com.alibaba.nacos.plugin.auth.impl.authenticate.IAuthenticationManager;
import com.alibaba.nacos.plugin.auth.impl.authenticate.LdapAuthenticationManager;
import com.alibaba.nacos.plugin.auth.impl.constant.AuthSystemTypes;
import com.alibaba.nacos.plugin.auth.impl.filter.JwtAuthenticationTokenFilter;
import com.alibaba.nacos.plugin.auth.impl.roles.NacosRoleServiceImpl;
import com.alibaba.nacos.plugin.auth.impl.token.TokenManagerDelegate;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUserDetailsServiceImpl;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;

import javax.annotation.PostConstruct;

/**
 * Spring security config.
 *
 * @author Nacos
 */
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class NacosAuthConfig extends WebSecurityConfigurerAdapter {

	private static final String SECURITY_IGNORE_URLS_SPILT_CHAR = ",";

	private static final String LOGIN_ENTRY_POINT = "/v1/auth/login";

	private static final String TOKEN_BASED_AUTH_ENTRY_POINT = "/v1/auth/**";

	private static final String DEFAULT_ALL_PATH_PATTERN = "/**";

	private static final String PROPERTY_IGNORE_URLS = "nacos.security.ignore.urls";

	private final Environment env;

	private final TokenManagerDelegate tokenProvider;

	private final AuthConfigs authConfigs;

	private final NacosUserDetailsServiceImpl userDetailsService;

	private final LdapAuthenticationProvider ldapAuthenticationProvider;

	private final ControllerMethodsCache methodsCache;

	public NacosAuthConfig(Environment env, TokenManagerDelegate tokenProvider, AuthConfigs authConfigs,
			NacosUserDetailsServiceImpl userDetailsService,
			ObjectProvider<LdapAuthenticationProvider> ldapAuthenticationProvider,
			ControllerMethodsCache methodsCache) {

		this.env = env;
		this.tokenProvider = tokenProvider;
		this.authConfigs = authConfigs;
		this.userDetailsService = userDetailsService;
		this.ldapAuthenticationProvider = ldapAuthenticationProvider.getIfAvailable();
		this.methodsCache = methodsCache;

	}

	/**
	 * Init.
	 */
	@PostConstruct
	public void init() {
		methodsCache.initClassMethod("com.alibaba.nacos.plugin.auth.impl.controller");
	}

	@Bean(name = BeanIds.AUTHENTICATION_MANAGER)
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Override
	public void configure(WebSecurity web) {

		String ignoreUrls = null;
		if (AuthSystemTypes.NACOS.name().equalsIgnoreCase(authConfigs.getNacosAuthSystemType())) {
			ignoreUrls = DEFAULT_ALL_PATH_PATTERN;
		} else if (AuthSystemTypes.LDAP.name().equalsIgnoreCase(authConfigs.getNacosAuthSystemType())) {
			ignoreUrls = DEFAULT_ALL_PATH_PATTERN;
		}
		if (StringUtils.isBlank(authConfigs.getNacosAuthSystemType())) {
			ignoreUrls = env.getProperty(PROPERTY_IGNORE_URLS, DEFAULT_ALL_PATH_PATTERN);
		}
		if (StringUtils.isNotBlank(ignoreUrls)) {
			for (String each : ignoreUrls.trim().split(SECURITY_IGNORE_URLS_SPILT_CHAR)) {
				web.ignoring().antMatchers(each.trim());
			}
		}
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		if (AuthSystemTypes.NACOS.name().equalsIgnoreCase(authConfigs.getNacosAuthSystemType())) {
			auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
		} else if (AuthSystemTypes.LDAP.name().equalsIgnoreCase(authConfigs.getNacosAuthSystemType())) {
			auth.authenticationProvider(ldapAuthenticationProvider);
		}
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		if (StringUtils.isBlank(authConfigs.getNacosAuthSystemType())) {
			http.csrf().disable().cors()// We don't need CSRF for JWT based authentication
					.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
					.authorizeRequests().requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
					.antMatchers(LOGIN_ENTRY_POINT).permitAll().and().authorizeRequests()
					.antMatchers(TOKEN_BASED_AUTH_ENTRY_POINT).authenticated().and().exceptionHandling()
					.authenticationEntryPoint(new JwtAuthenticationEntryPoint());
			// disable cache
			http.headers().cacheControl();

			http.addFilterBefore(new JwtAuthenticationTokenFilter(tokenProvider),
					UsernamePasswordAuthenticationFilter.class);
		}
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	@Primary
	public IAuthenticationManager authenticationManager(
			ObjectProvider<LdapAuthenticationManager> ldapAuthenticatoinManagerObjectProvider,
			ObjectProvider<DefaultAuthenticationManager> defaultAuthenticationManagers, AuthConfigs authConfigs) {
		return new AuthenticationNamagerDelegator(defaultAuthenticationManagers,
				ldapAuthenticatoinManagerObjectProvider, authConfigs);
	}

	@Bean
	public IAuthenticationManager defaultAuthenticationManager(NacosUserDetailsServiceImpl userDetailsService,
			TokenManagerDelegate jwtTokenManager, NacosRoleServiceImpl roleService) {
		return new DefaultAuthenticationManager(userDetailsService, jwtTokenManager, roleService);
	}
}
