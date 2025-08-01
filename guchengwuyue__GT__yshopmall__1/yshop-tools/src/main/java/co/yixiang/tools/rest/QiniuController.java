/**
* Copyright (C) 2018-2020
* All rights reserved, Designed By www.yixiang.co
* 注意：
* 本软件为www.yixiang.co开发研制
*/
package co.yixiang.tools.rest;

import co.yixiang.logging.aop.log.Log;
import co.yixiang.tools.domain.QiniuConfig;
import co.yixiang.tools.domain.QiniuContent;
import co.yixiang.tools.service.QiNiuService;
import co.yixiang.tools.service.dto.QiniuQueryCriteria;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 发送邮件
 * @author 郑杰
 * @date 2018/09/28 6:55:53
 */
@Slf4j
@RestController
@RequestMapping("/api/qiNiuContent")
@Api(tags = "工具：七牛云存储管理")
public class QiniuController {

	private final QiNiuService qiNiuService;

	public QiniuController(QiNiuService qiNiuService) {
		this.qiNiuService = qiNiuService;
	}

	@GetMapping(value = "/config")
	public ResponseEntity<Object> get() {
		return new ResponseEntity<>(qiNiuService.find(), HttpStatus.OK);
	}

	@Log("配置七牛云存储")
	@ApiOperation("配置七牛云存储")
	@PutMapping(value = "/config")
	public ResponseEntity<Object> emailConfig(@Validated @RequestBody QiniuConfig qiniuConfig) {

		qiNiuService.update(qiniuConfig);
		qiNiuService.update(qiniuConfig.getType());
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Log("导出数据")
	@ApiOperation("导出数据")
	@GetMapping(value = "/download")
	public void download(HttpServletResponse response, QiniuQueryCriteria criteria) throws IOException {
		qiNiuService.downloadList(qiNiuService.queryAll(criteria), response);
	}

	@Log("查询文件")
	@ApiOperation("查询文件")
	@GetMapping
	public ResponseEntity<Object> getRoles(QiniuQueryCriteria criteria, Pageable pageable) {
		return new ResponseEntity<>(qiNiuService.queryAll(criteria, pageable), HttpStatus.OK);
	}

	@Log("上传文件")
	@ApiOperation("上传文件")
	@PostMapping
	public ResponseEntity<Object> upload(@RequestParam MultipartFile file) {
		QiniuContent qiniuContent = qiNiuService.upload(file, qiNiuService.find());
		Map<String, Object> map = new HashMap<>(3);
		map.put("id", qiniuContent.getId());
		map.put("errno", 0);
		map.put("data", new String[] { qiniuContent.getUrl() });
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Log("同步七牛云数据")
	@ApiOperation("同步七牛云数据")
	@PostMapping(value = "/synchronize")
	public ResponseEntity<Object> synchronize() {

		qiNiuService.synchronize(qiNiuService.find());
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Log("下载文件")
	@ApiOperation("下载文件")
	@GetMapping(value = "/download/{id}")
	public ResponseEntity<Object> download(@PathVariable Long id) {

		Map<String, Object> map = new HashMap<>(1);
		map.put("url", qiNiuService.download(qiNiuService.findByContentId(id), qiNiuService.find()));
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	@Log("删除文件")
	@ApiOperation("删除文件")
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Object> delete(@PathVariable Long id) {

		qiNiuService.delete(qiNiuService.findByContentId(id), qiNiuService.find());
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Log("删除多张图片")
	@ApiOperation("删除多张图片")
	@DeleteMapping
	public ResponseEntity<Object> deleteAll(@RequestBody Long[] ids) {

		qiNiuService.deleteAll(ids, qiNiuService.find());
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
