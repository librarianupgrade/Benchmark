/**
 * Copyright (c) 2015-2020, Michael Yang 杨福海 (fuhai999@gmail.com).
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
package io.jboot.app.config;

import io.jboot.utils.StrUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigUtil {

	public static <T> T newInstance(Class<T> clazz) {
		try {
			Constructor constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			return (T) constructor.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<ConfigPara> parseParas(String string) {
		if (StrUtil.isBlank(string)) {
			return null;
		}
		List<ConfigPara> paras = new LinkedList<>();
		char[] chars = string.toCharArray();
		ConfigPara para = null;
		int index = 0;
		boolean hasDefaultValue = false;
		for (char c : chars) {
			//第一个字符是 '{' 会出现 ArrayIndexOutOfBoundsException 错误
			if (c == '{' && index > 0 && chars[index - 1] == '$' && para == null) {
				para = new ConfigPara();
				hasDefaultValue = false;
				para.setStart(index - 1);
			} else if (c == '}' && para != null) {
				para.setEnd(index);
				paras.add(para);
				para = null;
			} else if (para != null) {
				if (c == ':' && !hasDefaultValue) {
					hasDefaultValue = true;
				} else if (hasDefaultValue) {
					para.appendToDefaultValue(c);
				} else {
					para.appendToKey(c);
				}
			}
			index++;
		}
		return paras;
	}

	public static String parseValue(String value) {
		List<ConfigPara> paras = parseParas(value);
		if (paras == null || paras.size() == 0) {
			return value;
		}

		StringBuilder newString = new StringBuilder(value.length());
		int index = 0;
		for (ConfigPara para : paras) {
			if (para.getStart() > index) {
				newString.append(value, index, para.getStart());
			}

			String configValue = JbootConfigManager.me().getConfigValue(para.getKey());
			configValue = StrUtil.isNotBlank(configValue) ? configValue : para.getDefaultValue();
			newString.append(configValue);
			index = para.getEnd() + 1;
		}

		if (index < value.length()) {
			newString.append(value, index, value.length());
		}

		return newString.toString();
	}

	public static List<Method> getClassSetMethods(Class clazz) {
		List<Method> setMethods = new ArrayList<>();
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			if (method.getName().startsWith("set") && method.getName().length() > 3
					&& Character.isUpperCase(method.getName().charAt(3)) && method.getParameterCount() == 1
					&& Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers())) {

				setMethods.add(method);
			}
		}
		return setMethods;
	}

	public static String firstCharToLowerCase(String str) {
		char firstChar = str.charAt(0);
		if (firstChar >= 'A' && firstChar <= 'Z') {
			char[] arr = str.toCharArray();
			arr[0] += ('a' - 'A');
			return new String(arr);
		}
		return str;
	}

	public static boolean isBlank(String str) {
		if (str == null) {
			return true;
		}

		for (int i = 0, len = str.length(); i < len; i++) {
			if (str.charAt(i) > ' ') {
				return false;
			}
		}
		return true;
	}

	public static boolean isNotBlank(Object str) {
		return str == null ? false : !isBlank(str.toString());
	}

	public static boolean areNotBlank(String... strs) {
		if (strs == null || strs.length == 0) {
			return false;
		}

		for (String string : strs) {
			if (isBlank(string)) {
				return false;
			}
		}
		return true;
	}

	public static String map2string(Map map) {
		if (map == null || map.isEmpty()) {
			return "{ }";
		}

		StringJoiner joiner = new StringJoiner(", ", "{ ", " }");
		for (Object key : map.keySet()) {
			joiner.add(key + "='" + map.get(key) + "'");
		}
		return joiner.toString();
	}

	private static String rootClassPath;

	public static String getRootClassPath() {
		if (rootClassPath == null) {
			try {
				String path = getClassLoader().getResource("").toURI().getPath();
				rootClassPath = new File(path).getAbsolutePath();
			} catch (Exception e) {
				try {
					String path = ConfigUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
					path = java.net.URLDecoder.decode(path, "UTF-8");
					if (path.endsWith(File.separator)) {
						path = path.substring(0, path.length() - 1);
					}
					/**
					 * Fix path 带有文件名
					 */
					if (path.endsWith(".jar")) {
						path = path.substring(0, path.lastIndexOf("/") + 1);
					}
					rootClassPath = path;
				} catch (UnsupportedEncodingException e1) {
					throw new RuntimeException(e1);
				}
			}
		}
		return rootClassPath;
	}

	public static ClassLoader getClassLoader() {
		ClassLoader ret = Thread.currentThread().getContextClassLoader();
		return ret != null ? ret : ConfigUtil.class.getClassLoader();
	}

	public static void doNothing(Throwable ex) {
	}

	public static final Object convert(Class<?> convertClass, String s, Type genericType) {

		if (convertClass == String.class || s == null) {
			return s;
		}

		if (convertClass == Integer.class || convertClass == int.class) {
			return Integer.parseInt(s);
		} else if (convertClass == Long.class || convertClass == long.class) {
			return Long.parseLong(s);
		} else if (convertClass == Double.class || convertClass == double.class) {
			return Double.parseDouble(s);
		} else if (convertClass == Float.class || convertClass == float.class) {
			return Float.parseFloat(s);
		} else if (convertClass == Boolean.class || convertClass == boolean.class) {
			String value = s.toLowerCase();
			if ("1".equals(value) || "true".equals(value)) {
				return Boolean.TRUE;
			} else if ("0".equals(value) || "false".equals(value)) {
				return Boolean.FALSE;
			} else {
				throw new RuntimeException("Can not parse to boolean type of value: " + s);
			}
		} else if (convertClass == java.math.BigDecimal.class) {
			return new java.math.BigDecimal(s);
		} else if (convertClass == java.math.BigInteger.class) {
			return new java.math.BigInteger(s);
		} else if (convertClass == byte[].class) {
			return s.getBytes();
		} else if (Map.class.isAssignableFrom(convertClass)) {
			if (!s.contains(":") || !genericClassCheck(genericType)) {
				return null;
			} else {
				Map map = convertClass == ConcurrentHashMap.class ? new ConcurrentHashMap() : new HashMap();
				String[] strings = s.split(",");
				for (String kv : strings) {
					int indexOf = kv.indexOf(":");
					if (indexOf > 0 && indexOf < kv.trim().length() - 1) {
						map.put(kv.substring(0, indexOf).trim(), kv.substring(indexOf + 1).trim());
					}
				}
				return map;
			}
		} else if (List.class.isAssignableFrom(convertClass)) {
			if (genericClassCheck(genericType)) {
				List list = LinkedList.class == convertClass ? new LinkedList() : new ArrayList();
				String[] strings = s.split(",");
				for (String s1 : strings) {
					if (s != null && s1.trim().length() > 0) {
						list.add(s1.trim());
					}
				}
				return list;
			} else {
				return null;
			}
		} else if (Set.class.isAssignableFrom(convertClass)) {
			if (genericClassCheck(genericType)) {
				Set set = LinkedHashSet.class == convertClass ? new LinkedHashSet() : new HashSet();
				String[] strings = s.split(",");
				for (String s1 : strings) {
					if (s != null && s1.trim().length() > 0) {
						set.add(s1.trim());
					}
				}
				return set;
			} else {
				return null;
			}
		} else if (convertClass.isArray() && convertClass.getComponentType() == String.class) {
			List<String> list = new LinkedList();
			String[] strings = s.split(",");
			if (strings != null && strings.length > 0) {
				for (String s1 : strings) {
					if (s1 != null && s1.trim().length() != 0) {
						list.add(s1.trim());
					}
				}
			}
			return list.toArray(new String[0]);
		} else if (Class.class == convertClass) {
			try {
				return Class.forName(s, false, Thread.currentThread().getContextClassLoader());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		throw new RuntimeException(
				convertClass.getName() + " can not be converted, please use other type in your config class!");

	}

	/**
	 * 对泛型类型进行检测，只支持 String 类型的泛型，或者不是泛型才会支持
	 *
	 * @param type
	 * @return
	 */
	private static boolean genericClassCheck(Type type) {
		if (type instanceof ParameterizedType) {
			for (Type at : ((ParameterizedType) type).getActualTypeArguments()) {
				if (String.class != at) {
					return false;
				}
			}
		}
		return true;
	}

}
