package com.ruoyi.file.config;

import java.io.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 通用映射配置
 * 
 * @author ruoyi
 */
@Configuration
public class ResourcesConfig implements WebMvcConfigurer {
	/**
	 * 上传文件存储在本地的根路径
	 */
	@Value("${file.path}")
	private String localFilePath;

	/**
	 * 资源映射路径 前缀
	 */
	@Value("${file.prefix}")
	public String localFilePrefix;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		/** 本地文件上传路径 */
		registry.addResourceHandler(localFilePrefix + "/**")
				.addResourceLocations("file:" + localFilePath + File.separator);
	}
}