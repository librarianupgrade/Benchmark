/**
 * Copyright (c) 2015-2022, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.support.shiro.processer;

/**
 * Shiro 的认证处理器
 * 用于对每个 controller 的 每个方法进行认证
 *
 * 每个 shiro 注解，都有一个对于的 Processer，比如 注解 @RequiresGuest 的处理器为 ShiroRequiresGuestProcesser.java
 */
public interface IShiroAuthorizeProcesser {

	public AuthorizeResult authorize();

}
