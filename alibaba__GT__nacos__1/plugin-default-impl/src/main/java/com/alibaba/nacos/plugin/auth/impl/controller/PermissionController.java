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

package com.alibaba.nacos.plugin.auth.impl.controller;

import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.common.model.RestResultUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.config.server.model.Page;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.impl.constant.AuthConstants;
import com.alibaba.nacos.plugin.auth.impl.persistence.PermissionInfo;
import com.alibaba.nacos.plugin.auth.impl.roles.NacosRoleServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Permission operation controller.
 *
 * @author nkorange
 * @since 1.2.0
 */
@RestController
@RequestMapping("/v1/auth/permissions")
public class PermissionController {

	@Autowired
	private NacosRoleServiceImpl nacosRoleService;

	/**
	 * Query permissions of a role.
	 *
	 * @param role     the role
	 * @param pageNo   page index
	 * @param pageSize page size
	 * @return permission of a role
	 */
	@GetMapping(params = "search=accurate")
	@Secured(resource = AuthConstants.CONSOLE_RESOURCE_NAME_PREFIX + "permissions", action = ActionTypes.READ)
	public Object getPermissions(@RequestParam int pageNo, @RequestParam int pageSize,
			@RequestParam(name = "role", defaultValue = StringUtils.EMPTY) String role) {
		return nacosRoleService.getPermissionsFromDatabase(role, pageNo, pageSize);
	}

	/**
	 * Fuzzy Query permissions of a role.
	 *
	 * @param role     the role
	 * @param pageNo   page index
	 * @param pageSize page size
	 * @return permission of a role
	 */
	@GetMapping(params = "search=blur")
	@Secured(resource = AuthConstants.CONSOLE_RESOURCE_NAME_PREFIX + "permissions", action = ActionTypes.READ)
	public Page<PermissionInfo> fuzzySearchPermission(@RequestParam int pageNo, @RequestParam int pageSize,
			@RequestParam(name = "role", defaultValue = StringUtils.EMPTY) String role) {
		return nacosRoleService.findPermissionsLike4Page(role, pageNo, pageSize);
	}

	/**
	 * Add a permission to a role.
	 *
	 * @param role     the role
	 * @param resource the related resource
	 * @param action   the related action
	 * @return ok if succeed
	 */
	@PostMapping
	@Secured(resource = AuthConstants.CONSOLE_RESOURCE_NAME_PREFIX + "permissions", action = ActionTypes.WRITE)
	public Object addPermission(@RequestParam String role, @RequestParam String resource, @RequestParam String action) {
		nacosRoleService.addPermission(role, resource, action);
		return RestResultUtils.success("add permission ok!");
	}

	/**
	 * Delete a permission from a role.
	 *
	 * @param role     the role
	 * @param resource the related resource
	 * @param action   the related action
	 * @return ok if succeed
	 */
	@DeleteMapping
	@Secured(resource = AuthConstants.CONSOLE_RESOURCE_NAME_PREFIX + "permissions", action = ActionTypes.WRITE)
	public Object deletePermission(@RequestParam String role, @RequestParam String resource,
			@RequestParam String action) {
		nacosRoleService.deletePermission(role, resource, action);
		return RestResultUtils.success("delete permission ok!");
	}
}
