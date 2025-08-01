package com.ruoyi.gateway.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

/**
 * 聚合系统接口
 * 
 * @author ruoyi
 */
@Component
public class SwaggerProvider implements SwaggerResourcesProvider {
	/**
	 * Swagger2默认的url后缀
	 */
	public static final String SWAGGER2URL = "/v2/api-docs";
	/**
	 * 网关路由
	 */
	@Autowired
	private RouteLocator routeLocator;

	@Autowired
	private GatewayProperties gatewayProperties;

	/**
	 * 聚合其他服务接口
	 * 
	 * @return
	 */
	@Override
	public List<SwaggerResource> get() {
		List<SwaggerResource> resourceList = new ArrayList<>();
		List<String> routes = new ArrayList<>();
		// 获取网关中配置的route
		routeLocator.getRoutes().subscribe(route -> routes.add(route.getId()));
		gatewayProperties.getRoutes().stream().filter(routeDefinition -> routes.contains(routeDefinition.getId()))
				.forEach(routeDefinition -> routeDefinition.getPredicates().stream()
						.filter(predicateDefinition -> "Path".equalsIgnoreCase(predicateDefinition.getName()))
						.filter(predicateDefinition -> !"ruoyi-auth".equalsIgnoreCase(routeDefinition.getId()))
						.forEach(predicateDefinition -> resourceList
								.add(swaggerResource(routeDefinition.getId(), predicateDefinition.getArgs()
										.get(NameUtils.GENERATED_NAME_PREFIX + "0").replace("/**", SWAGGER2URL)))));
		return resourceList;
	}

	private SwaggerResource swaggerResource(String name, String location) {
		SwaggerResource swaggerResource = new SwaggerResource();
		swaggerResource.setName(name);
		swaggerResource.setLocation(location);
		swaggerResource.setSwaggerVersion("2.0");
		return swaggerResource;
	}
}
