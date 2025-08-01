package com.ruoyi.common.core.utils.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 图片处理工具类
 *
 * @author ruoyi
 */
public class ImageUtils {
	private static final Logger log = LoggerFactory.getLogger(ImageUtils.class);

	public static byte[] getImage(String imagePath) {
		InputStream is = getFile(imagePath);
		try {
			return IOUtils.toByteArray(is);
		} catch (Exception e) {
			log.error("图片加载异常 {}", e);
			return null;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	public static InputStream getFile(String imagePath) {
		try {
			byte[] result = readFile(imagePath);
			result = Arrays.copyOf(result, result.length);
			return new ByteArrayInputStream(result);
		} catch (Exception e) {
			log.error("获取图片异常 {}", e);
		}
		return null;
	}

	/**
	 * 读取文件为字节数据
	 * 
	 * @param key 地址
	 * @return 字节数据
	 */
	public static byte[] readFile(String url) {
		InputStream in = null;
		ByteArrayOutputStream baos = null;
		try {
			// 网络地址
			URL urlObj = new URL(url);
			URLConnection urlConnection = urlObj.openConnection();
			urlConnection.setConnectTimeout(30 * 1000);
			urlConnection.setReadTimeout(60 * 1000);
			urlConnection.setDoInput(true);
			in = urlConnection.getInputStream();
			return IOUtils.toByteArray(in);
		} catch (Exception e) {
			log.error("访问文件异常 {}", e);
			return null;
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(baos);
		}
	}
}
