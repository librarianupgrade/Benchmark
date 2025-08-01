package com.ruoyi.file.utils;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.exception.file.FileNameLengthLimitExceededException;
import com.ruoyi.common.core.exception.file.FileSizeLimitExceededException;
import com.ruoyi.common.core.exception.file.InvalidExtensionException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.IdUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.utils.file.MimeTypeUtils;

/**
 * 文件上传工具类
 * 
 * @author ruoyi
 */
public class FileUploadUtils {
	/**
	 * 默认大小 50M
	 */
	public static final long DEFAULT_MAX_SIZE = 50 * 1024 * 1024;

	/**
	 * 默认的文件名最大长度 100
	 */
	public static final int DEFAULT_FILE_NAME_LENGTH = 100;

	/**
	 * 根据文件路径上传
	 *
	 * @param baseDir 相对应用的基目录
	 * @param file 上传的文件
	 * @return 文件名称
	 * @throws IOException
	 */
	public static final String upload(String baseDir, MultipartFile file) throws IOException {
		try {
			return upload(baseDir, file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
		} catch (Exception e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	/**
	 * 文件上传
	 *
	 * @param baseDir 相对应用的基目录
	 * @param file 上传的文件
	 * @param allowedExtension 上传文件类型
	 * @return 返回上传成功的文件名
	 * @throws FileSizeLimitExceededException 如果超出最大大小
	 * @throws FileNameLengthLimitExceededException 文件名太长
	 * @throws IOException 比如读写文件出错时
	 * @throws InvalidExtensionException 文件校验异常
	 */
	public static final String upload(String baseDir, MultipartFile file, String[] allowedExtension)
			throws FileSizeLimitExceededException, IOException, FileNameLengthLimitExceededException,
			InvalidExtensionException {
		int fileNamelength = file.getOriginalFilename().length();
		if (fileNamelength > FileUploadUtils.DEFAULT_FILE_NAME_LENGTH) {
			throw new FileNameLengthLimitExceededException(FileUploadUtils.DEFAULT_FILE_NAME_LENGTH);
		}

		assertAllowed(file, allowedExtension);

		String fileName = extractFilename(file);

		File desc = getAbsoluteFile(baseDir, fileName);
		file.transferTo(desc);
		String pathFileName = getPathFileName(fileName);
		return pathFileName;
	}

	/**
	 * 编码文件名
	 */
	public static final String extractFilename(MultipartFile file) {
		String fileName = file.getOriginalFilename();
		String extension = getExtension(file);
		fileName = DateUtils.datePath() + "/" + IdUtils.fastUUID() + "." + extension;
		return fileName;
	}

	private static final File getAbsoluteFile(String uploadDir, String fileName) throws IOException {
		File desc = new File(uploadDir + File.separator + fileName);

		if (!desc.exists()) {
			if (!desc.getParentFile().exists()) {
				desc.getParentFile().mkdirs();
			}
		}
		return desc.isAbsolute() ? desc : desc.getAbsoluteFile();
	}

	private static final String getPathFileName(String fileName) throws IOException {
		String pathFileName = "/" + fileName;
		return pathFileName;
	}

	/**
	 * 文件大小校验
	 *
	 * @param file 上传的文件
	 * @throws FileSizeLimitExceededException 如果超出最大大小
	 * @throws InvalidExtensionException 文件校验异常
	 */
	public static final void assertAllowed(MultipartFile file, String[] allowedExtension)
			throws FileSizeLimitExceededException, InvalidExtensionException {
		long size = file.getSize();
		if (DEFAULT_MAX_SIZE != -1 && size > DEFAULT_MAX_SIZE) {
			throw new FileSizeLimitExceededException(DEFAULT_MAX_SIZE / 1024 / 1024);
		}

		String fileName = file.getOriginalFilename();
		String extension = getExtension(file);
		if (allowedExtension != null && !isAllowedExtension(extension, allowedExtension)) {
			if (allowedExtension == MimeTypeUtils.IMAGE_EXTENSION) {
				throw new InvalidExtensionException.InvalidImageExtensionException(allowedExtension, extension,
						fileName);
			} else if (allowedExtension == MimeTypeUtils.FLASH_EXTENSION) {
				throw new InvalidExtensionException.InvalidFlashExtensionException(allowedExtension, extension,
						fileName);
			} else if (allowedExtension == MimeTypeUtils.MEDIA_EXTENSION) {
				throw new InvalidExtensionException.InvalidMediaExtensionException(allowedExtension, extension,
						fileName);
			} else if (allowedExtension == MimeTypeUtils.VIDEO_EXTENSION) {
				throw new InvalidExtensionException.InvalidVideoExtensionException(allowedExtension, extension,
						fileName);
			} else {
				throw new InvalidExtensionException(allowedExtension, extension, fileName);
			}
		}
	}

	/**
	 * 判断MIME类型是否是允许的MIME类型
	 *
	 * @param extension 上传文件类型
	 * @param allowedExtension 允许上传文件类型
	 * @return true/false
	 */
	public static final boolean isAllowedExtension(String extension, String[] allowedExtension) {
		for (String str : allowedExtension) {
			if (str.equalsIgnoreCase(extension)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取文件名的后缀
	 * 
	 * @param file 表单文件
	 * @return 后缀名
	 */
	public static final String getExtension(MultipartFile file) {
		String extension = FilenameUtils.getExtension(file.getOriginalFilename());
		if (StringUtils.isEmpty(extension)) {
			extension = MimeTypeUtils.getExtension(file.getContentType());
		}
		return extension;
	}
}