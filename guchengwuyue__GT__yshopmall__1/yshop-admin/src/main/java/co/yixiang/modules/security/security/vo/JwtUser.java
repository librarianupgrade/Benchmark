/**
* Copyright (C) 2018-2020
* All rights reserved, Designed By www.yixiang.co
* 注意：
* 本软件为www.yixiang.co开发研制
*/
package co.yixiang.modules.security.security.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * @author hupeng
 * @date 2018-11-23
 */
@Getter
@AllArgsConstructor
public class JwtUser implements UserDetails {

	private final Long id;

	private final String username;

	private final String nickName;

	private final String sex;

	@JsonIgnore
	private final String password;

	private final String avatar;

	private final String email;

	private final String phone;

	private final String dept;

	private final String job;

	@JsonIgnore
	private final Collection<GrantedAuthority> authorities;

	private final boolean enabled;

	private Timestamp createTime;

	@JsonIgnore
	private final Date lastPasswordResetDate;

	@JsonIgnore
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@JsonIgnore
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@JsonIgnore
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@JsonIgnore
	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public Collection getRoles() {
		return authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
	}
}
