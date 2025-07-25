/**
 * Copyright (c) 2015-2022, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jboot.web.attachment;

import java.io.File;
import java.io.InputStream;

/**
 * @author michael yang (fuhai999@gmail.com)
 */
public interface AttachmentContainer {

	/**
	 * 保存文件
	 *
	 * @param file
	 * @return 返回文件的相对路径
	 */
	String saveFile(File file);

	/**
	 * 保存文件
	 *
	 * @param file
	 * @return 返回文件的相对路径
	 */
	String saveFile(File file, String toRelativePath);

	/**
	 * 保存文件
	 *
	 * @param inputStream
	 * @return
	 */
	String saveFile(InputStream inputStream, String toRelativePath);

	/**
	 * 删除文件
	 *
	 * @param relativePath
	 * @return
	 */
	boolean deleteFile(String relativePath);

	/**
	 * 通过相对路径获取文件
	 *
	 * @param relativePath
	 * @return
	 */
	File getFile(String relativePath);

	/**
	 * 通过一个文件，获取其相对路径
	 *
	 * @param file
	 * @return
	 */
	String getRelativePath(File file);

}
