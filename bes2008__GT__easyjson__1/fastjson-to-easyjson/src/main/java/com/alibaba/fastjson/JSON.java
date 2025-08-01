/*
 *  Copyright 2019 the original author or authors.
 *
 *  Licensed under the LGPL, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at  http://www.gnu.org/licenses/lgpl-3.0.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ParseProcess;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.IOUtils;
import com.alibaba.fastjson.util.TypeUtils;
import com.jn.easyjson.core.JSONBuilder;
import com.jn.easyjson.core.JSONBuilderProvider;
import com.jn.easyjson.core.JsonTreeNode;
import com.jn.easyjson.core.node.JsonArrayNode;
import com.jn.langx.util.reflect.type.Types;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

/**
 * This is the main class for using Fastjson. You usually call these two methods {@link #toJSONString(Object)} and {@link #parseObject(String, Class)}.
 * <p>
 * <p>Here is an example of how fastjson is used for a simple Class:
 * <p>
 * <pre>
 * Model model = new Model();
 * String json = JSON.toJSONString(model); // serializes model to Json
 * Model model2 = JSON.parseObject(json, Model.class); // deserializes json into model2
 * </pre>
 * <p>
 * <p>If the object that your are serializing/deserializing is a {@code ParameterizedType}
 * (i.e. contains at least one type parameter and may be an array) then you must use the
 * {@link #toJSONString(Object)} or {@link #parseObject(String, Type, Feature[])} method.  Here is an
 * example for serializing and deserialing a {@code ParameterizedType}:
 * <p>
 * <pre>
 * String json = "[{},...]";
 * Type listType = new TypeReference&lt;List&lt;Model&gt;&gt;() {}.getType();
 * List&lt;Model&gt; modelList = JSON.parseObject(json, listType);
 * </pre>
 *
 * @author wenshao[szujobs@hotmail.com]
 * @see com.alibaba.fastjson.TypeReference
 */
@SuppressWarnings({ "all" })
public abstract class JSON implements JSONStreamAware, JSONAware {
	public static TimeZone defaultTimeZone = TimeZone.getDefault();
	public static Locale defaultLocale = Locale.getDefault();
	public static String DEFAULT_TYPE_KEY = "@type";
	static final SerializeFilter[] emptyFilters = new SerializeFilter[0];
	public static String DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static int DEFAULT_PARSER_FEATURE;
	public static int DEFAULT_GENERATE_FEATURE;

	static {
		int features = 0;
		features |= Feature.AutoCloseSource.getMask();
		features |= Feature.InternFieldNames.getMask();
		features |= Feature.UseBigDecimal.getMask();
		features |= Feature.AllowUnQuotedFieldNames.getMask();
		features |= Feature.AllowSingleQuotes.getMask();
		features |= Feature.AllowArbitraryCommas.getMask();
		features |= Feature.SortFeidFastMatch.getMask();
		features |= Feature.IgnoreNotMatch.getMask();
		DEFAULT_PARSER_FEATURE = features;
	}

	static {
		int features = 0;
		features |= SerializerFeature.QuoteFieldNames.getMask();
		features |= SerializerFeature.SkipTransientField.getMask();
		features |= SerializerFeature.WriteEnumUsingName.getMask();
		features |= SerializerFeature.SortField.getMask();

		DEFAULT_GENERATE_FEATURE = features;

		config(IOUtils.DEFAULT_PROPERTIES);
	}

	private static void config(Properties properties) {
		{
			String featuresProperty = properties.getProperty("fastjson.serializerFeatures.MapSortField");
			int mask = SerializerFeature.MapSortField.getMask();
			if ("true".equals(featuresProperty)) {
				DEFAULT_GENERATE_FEATURE |= mask;
			} else if ("false".equals(featuresProperty)) {
				DEFAULT_GENERATE_FEATURE &= ~mask;
			}
		}

		{
			if ("true".equals(properties.getProperty("parser.features.NonStringKeyAsString"))) {
				DEFAULT_PARSER_FEATURE |= Feature.NonStringKeyAsString.getMask();
			}
		}

		{
			if ("true".equals(properties.getProperty("parser.features.ErrorOnEnumNotMatch"))
					|| "true".equals(properties.getProperty("fastjson.parser.features.ErrorOnEnumNotMatch"))) {
				DEFAULT_PARSER_FEATURE |= Feature.ErrorOnEnumNotMatch.getMask();
			}
		}

	}

	/**
	 * config default type key
	 *
	 * @since 1.2.14
	 */
	public static void setDefaultTypeKey(String typeKey) {
		DEFAULT_TYPE_KEY = typeKey;
		ParserConfig.global.symbolTable.addSymbol(typeKey, 0, typeKey.length(), typeKey.hashCode(), true);
	}

	public static Object parse(String text) {
		return parse(text, DEFAULT_PARSER_FEATURE);
	}

	/**
	 * @since 1.2.38
	 */
	public static Object parse(String text, ParserConfig config) {
		return parse(text, config, DEFAULT_PARSER_FEATURE);
	}

	/**
	 * @since 1.2.38
	 */
	public static Object parse(String text, ParserConfig config, int features) {
		if (text == null) {
			return null;
		}
		return JsonMapper.fromJsonTreeNode(getJsonBuilder(DEFAULT_GENERATE_FEATURE).build().fromJson(text));
	}

	private static JSONBuilder getJsonBuilder(int features, SerializerFeature... features2) {
		JSONBuilder jsonBuilder = JSONBuilderProvider.create();
		if (features2 != null) {
			for (SerializerFeature feature : features2) {
				features |= feature.getMask();
			}
		}
		boolean serializeNulls = (SerializerFeature.WriteMapNullValue.getMask() & features) != 0;
		jsonBuilder.serializeNulls(serializeNulls);
		boolean serializeEnumUsingToString = (SerializerFeature.WriteEnumUsingToString.getMask() & features) != 0;
		jsonBuilder.serializeEnumUsingToString(serializeEnumUsingToString);
		boolean serializeEnumUsingName = (SerializerFeature.WriteEnumUsingName.getMask() & features) != 0;
		jsonBuilder.serializeEnumUsingValue(!serializeEnumUsingName);
		boolean skipTransientField = (SerializerFeature.SkipTransientField.getMask() & features) != 0;
		if (skipTransientField) {
			jsonBuilder.excludeFieldsWithAppendModifiers(Modifier.TRANSIENT);
		}
		boolean prettyFormat = (SerializerFeature.PrettyFormat.getMask() & features) != 0;
		jsonBuilder.prettyFormat(prettyFormat);
		return jsonBuilder;
	}

	public static Object parse(String text, int features) {
		return parse(text, ParserConfig.getGlobalInstance(), features);
	}

	public static Object parse(byte[] input, Feature... features) {
		char[] chars = allocateChars(input.length);
		int len = IOUtils.decodeUTF8(input, 0, input.length, chars);
		if (len < 0) {
			return null;
		}
		return parse(new String(chars, 0, len), features);
	}

	public static Object parse(byte[] input, int off, int len, CharsetDecoder charsetDecoder, Feature... features) {
		if (input == null || input.length == 0) {
			return null;
		}

		int featureValues = DEFAULT_PARSER_FEATURE;
		for (Feature feature : features) {
			featureValues = Feature.config(featureValues, feature, true);
		}

		return parse(input, off, len, charsetDecoder, featureValues);
	}

	public static Object parse(byte[] input, int off, int len, CharsetDecoder charsetDecoder, int features) {
		charsetDecoder.reset();

		int scaleLength = (int) (len * (double) charsetDecoder.maxCharsPerByte());
		char[] chars = allocateChars(scaleLength);

		ByteBuffer byteBuf = ByteBuffer.wrap(input, off, len);
		CharBuffer charBuf = CharBuffer.wrap(chars);
		IOUtils.decode(charsetDecoder, byteBuf, charBuf);
		String text = charBuf.toString();
		return parse(text, features);
	}

	public static Object parse(String text, Feature... features) {
		int featureValues = DEFAULT_PARSER_FEATURE;
		for (Feature feature : features) {
			featureValues = Feature.config(featureValues, feature, true);
		}

		return parse(text, featureValues);
	}

	public static JSONObject parseObject(String text, Feature... features) {
		return parseObject(text);
	}

	public static JSONObject parseObject(String text) {
		return (JSONObject) JsonMapper.fromJsonTreeNode(JSONBuilderProvider.simplest().fromJson(text));
	}

	/**
	 * <pre>
	 * String jsonStr = "[{\"id\":1001,\"name\":\"Jobs\"}]";
	 * List&lt;Model&gt; models = JSON.parseObject(jsonStr, new TypeReference&lt;List&lt;Model&gt;&gt;() {});
	 * </pre>
	 *
	 * @param text     json string
	 * @param type     type refernce
	 * @param features
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parseObject(String text, TypeReference<T> type, Feature... features) {
		return (T) parseObject(text, type.type, ParserConfig.global, DEFAULT_PARSER_FEATURE, features);
	}

	/**
	 * This method deserializes the specified Json into an object of the specified class. It is not
	 * suitable to use if the specified class is a generic type since it will not have the generic
	 * type information because of the Type Erasure feature of Java. Therefore, this method should not
	 * be used if the desired type is a generic type. Note that this method works fine if the any of
	 * the fields of the specified object are generics, just the object itself should not be a
	 * generic type. For the cases when the object is of generic type, invoke
	 * {@link #parseObject(String, Type, Feature[])}. If you have the Json in a {@link InputStream} instead of
	 * a String, use {@link #parseObject(InputStream, Type, Feature[])} instead.
	 *
	 * @param json     the string from which the object is to be deserialized
	 * @param clazz    the class of T
	 * @param features parser features
	 * @return an object of type T from the string
	 * classOfT
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parseObject(String json, Class<T> clazz, Feature... features) {
		return (T) parseObject(json, (Type) clazz, ParserConfig.global, null, DEFAULT_PARSER_FEATURE, features);
	}

	@SuppressWarnings("unchecked")
	public static <T> T parseObject(String text, Class<T> clazz, ParseProcess processor, Feature... features) {
		return (T) parseObject(text, (Type) clazz, ParserConfig.global, processor, DEFAULT_PARSER_FEATURE, features);
	}

	/**
	 * This method deserializes the specified Json into an object of the specified type. This method
	 * is useful if the specified object is a generic type. For non-generic objects, use
	 * {@link #parseObject(String, Class, Feature[])} instead. If you have the Json in a {@link InputStream} instead of
	 * a String, use {@link #parseObject(InputStream, Type, Feature[])} instead.
	 *
	 * @param <T>  the type of the desired object
	 * @param json the string from which the object is to be deserialized
	 * @param type The specific genericized type of src. You can obtain this type by using the
	 *             {@link com.alibaba.fastjson.TypeReference} class. For example, to get the type for
	 *             {@code Collection<Foo>}, you should use:
	 *             <pre>
	 *                                                                                                 Type type = new TypeReference&lt;Collection&lt;Foo&gt;&gt;(){}.getType();
	 *                                                                                                 </pre>
	 * @return an object of type T from the string
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parseObject(String json, Type type, Feature... features) {
		return (T) parseObject(json, type, ParserConfig.global, DEFAULT_PARSER_FEATURE, features);
	}

	@SuppressWarnings("unchecked")
	public static <T> T parseObject(String input, Type clazz, ParseProcess processor, Feature... features) {
		return (T) parseObject(input, clazz, ParserConfig.global, processor, DEFAULT_PARSER_FEATURE, features);
	}

	@SuppressWarnings("unchecked")
	public static <T> T parseObject(String input, Type clazz, int featureValues, Feature... features) {
		if (input == null) {
			return null;
		}
		return parseObject(input, clazz);
	}

	/**
	 * @since 1.2.11
	 */
	public static <T> T parseObject(String input, Type clazz, ParserConfig config, Feature... features) {
		return parseObject(input, clazz, config, null, DEFAULT_PARSER_FEATURE, features);
	}

	public static <T> T parseObject(String input, Type clazz, ParserConfig config, int featureValues,
			Feature... features) {
		return parseObject(input, clazz, config, null, featureValues, features);
	}

	@SuppressWarnings("unchecked")
	public static <T> T parseObject(String input, Type clazz, ParserConfig config, ParseProcess processor,
			int featureValues, Feature... features) {
		if (input == null) {
			return null;
		}
		return getJsonBuilder(JSON.DEFAULT_GENERATE_FEATURE).build().fromJson(input, clazz);

	}

	@SuppressWarnings("unchecked")
	public static <T> T parseObject(byte[] bytes, Type clazz, Feature... features) {
		return (T) parseObject(bytes, 0, bytes.length, IOUtils.UTF8, clazz, features);
	}

	/**
	 * @since 1.2.11
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parseObject(byte[] bytes, int offset, int len, Charset charset, Type clazz,
			Feature... features) {
		return (T) parseObject(bytes, offset, len, charset, clazz, ParserConfig.global, null, DEFAULT_PARSER_FEATURE,
				features);
	}

	/**
	 * @since 1.2.55
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parseObject(byte[] bytes, Charset charset, Type clazz, ParserConfig config,
			ParseProcess processor, int featureValues, Feature... features) {
		return (T) parseObject(bytes, 0, bytes.length, charset, clazz, config, processor, featureValues, features);
	}

	/**
	 * @since 1.2.55
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parseObject(byte[] bytes, int offset, int len, Charset charset, Type clazz, ParserConfig config,
			ParseProcess processor, int featureValues, Feature... features) {
		if (charset == null) {
			charset = IOUtils.UTF8;
		}

		String strVal;
		if (charset == IOUtils.UTF8) {
			char[] chars = allocateChars(bytes.length);
			int chars_len = IOUtils.decodeUTF8(bytes, offset, len, chars);
			if (chars_len < 0) {
				return null;
			}
			strVal = new String(chars, 0, chars_len);
		} else {
			if (len < 0) {
				return null;
			}
			strVal = new String(bytes, offset, len, charset);
		}
		return (T) parseObject(strVal, clazz, config, processor, featureValues, features);
	}

	@SuppressWarnings("unchecked")
	public static <T> T parseObject(byte[] input, //
			int off, //
			int len, //
			CharsetDecoder charsetDecoder, //
			Type clazz, //
			Feature... features) {
		charsetDecoder.reset();

		int scaleLength = (int) (len * (double) charsetDecoder.maxCharsPerByte());
		char[] chars = allocateChars(scaleLength);

		ByteBuffer byteBuf = ByteBuffer.wrap(input, off, len);
		CharBuffer charByte = CharBuffer.wrap(chars);
		IOUtils.decode(charsetDecoder, byteBuf, charByte);

		return (T) parseObject(charByte.toString(), clazz, features);
	}

	@SuppressWarnings("unchecked")
	public static <T> T parseObject(char[] input, int length, Type clazz, Feature... features) {
		if (input == null || input.length == 0) {
			return null;
		}
		return parseObject(new String(input), clazz);
	}

	/**
	 * @since 1.2.11
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parseObject(InputStream is, //
			Type type, //
			Feature... features) throws IOException {
		return (T) parseObject(is, IOUtils.UTF8, type, features);
	}

	/**
	 * @since 1.2.11
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parseObject(InputStream is, //
			Charset charset, //
			Type type, //
			Feature... features) throws IOException {
		return (T) parseObject(is, charset, type, ParserConfig.global, features);
	}

	/**
	 * @since 1.2.55
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parseObject(InputStream is, //
			Charset charset, //
			Type type, //
			ParserConfig config, //
			Feature... features) throws IOException {
		return (T) parseObject(is, charset, type, config, null, DEFAULT_PARSER_FEATURE, features);
	}

	/**
	 * @since 1.2.55
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parseObject(InputStream is, //
			Charset charset, //
			Type type, //
			ParserConfig config, //
			ParseProcess processor, //
			int featureValues, //
			Feature... features) throws IOException {
		if (charset == null) {
			charset = IOUtils.UTF8;
		}

		byte[] bytes = allocateBytes(1024 * 64);
		int offset = 0;
		for (;;) {
			int readCount = is.read(bytes, offset, bytes.length - offset);
			if (readCount == -1) {
				break;
			}
			offset += readCount;
			if (offset == bytes.length) {
				byte[] newBytes = new byte[bytes.length * 3 / 2];
				System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
				bytes = newBytes;
			}
		}

		return (T) parseObject(bytes, 0, offset, charset, type, config, processor, featureValues, features);
	}

	public static <T> T parseObject(String text, Class<T> clazz) {
		return parseObject(text, clazz, new Feature[0]);
	}

	public static JSONArray parseArray(String text) {
		if (text == null) {
			return null;
		}

		JsonTreeNode node = getJsonBuilder(DEFAULT_GENERATE_FEATURE).build().fromJson(text);
		JsonArrayNode arrayNode = null;
		if (node.isJsonArrayNode()) {
			arrayNode = node.getAsJsonArrayNode();
		} else {
			arrayNode = new JsonArrayNode();
			arrayNode.add(node);
		}
		return (JSONArray) JsonMapper.fromJsonTreeNode(node);
	}

	public static <T> List<T> parseArray(String text, Class<T> clazz) {
		if (text == null) {
			return null;
		}
		return getJsonBuilder(JSON.DEFAULT_GENERATE_FEATURE).build().fromJson(text,
				Types.getListParameterizedType(clazz));
	}

	public static List<Object> parseArray(String text, Type[] types) {
		if (text == null) {
			return null;
		}

		List<Object> list = null;
		// TODO adapter
		return list;
	}

	/**
	 * This method serializes the specified object into its equivalent Json representation. Note that this method works fine if the any of the object fields are of generic type,
	 * just the object itself should not be of a generic type. If you want to write out the object to a
	 * {@link Writer}, use {@link #writeJSONString(Writer, Object, SerializerFeature[])} instead.
	 *
	 * @param object the object for which json representation is to be created setting for fastjson
	 * @return Json representation of {@code object}.
	 */
	public static String toJSONString(Object object) {
		return toJSONString(object, emptyFilters);
	}

	public static String toJSONString(Object object, SerializerFeature... features) {
		return toJSONString(object, DEFAULT_GENERATE_FEATURE, features);
	}

	/**
	 * @since 1.2.11
	 */
	public static String toJSONString(Object object, int defaultFeatures, SerializerFeature... features) {
		return getJsonBuilder(defaultFeatures, features).build().toJson(object);
	}

	/**
	 * @since 1.1.14
	 */
	public static String toJSONStringWithDateFormat(Object object, String dateFormat, SerializerFeature... features) {
		return toJSONStringWithDateFormat(object, dateFormat, JSON.DEFAULT_GENERATE_FEATURE, features);
	}

	public static String toJSONStringWithDateFormat(Object object, String dateFormat, int defaultFeatures,
			SerializerFeature... features) {
		return getJsonBuilder(defaultFeatures, features).serializeDateUsingPattern(dateFormat).build().toJson(object);
	}

	public static String toJSONString(Object object, SerializeFilter filter, SerializerFeature... features) {
		return toJSONString(object, SerializeConfig.globalInstance, new SerializeFilter[] { filter }, null,
				DEFAULT_GENERATE_FEATURE, features);
	}

	public static String toJSONString(Object object, SerializeFilter[] filters, SerializerFeature... features) {
		return toJSONString(object, SerializeConfig.globalInstance, filters, null, DEFAULT_GENERATE_FEATURE, features);
	}

	public static byte[] toJSONBytes(Object object, SerializerFeature... features) {
		return toJSONBytes(object, DEFAULT_GENERATE_FEATURE, features);
	}

	public static byte[] toJSONBytes(Object object, SerializeFilter filter, SerializerFeature... features) {
		return toJSONBytes(object, SerializeConfig.globalInstance, new SerializeFilter[] { filter },
				DEFAULT_GENERATE_FEATURE, features);
	}

	/**
	 * @since 1.2.11
	 */
	public static byte[] toJSONBytes(Object object, int defaultFeatures, SerializerFeature... features) {
		return toJSONBytes(object, SerializeConfig.globalInstance, defaultFeatures, features);
	}

	public static String toJSONString(Object object, SerializeConfig config, SerializerFeature... features) {
		return toJSONString(object, config, (SerializeFilter) null, features);
	}

	public static String toJSONString(Object object, //
			SerializeConfig config, //
			SerializeFilter filter, //
			SerializerFeature... features) {
		return toJSONString(object, config, new SerializeFilter[] { filter }, null, DEFAULT_GENERATE_FEATURE, features);
	}

	public static String toJSONString(Object object, //
			SerializeConfig config, //
			SerializeFilter[] filters, //
			SerializerFeature... features) {
		return toJSONString(object, config, filters, null, DEFAULT_GENERATE_FEATURE, features);
	}

	/**
	 * @return
	 * @since 1.2.9
	 */
	public static String toJSONString(Object object, //
			SerializeConfig config, //
			SerializeFilter[] filters, //
			String dateFormat, //
			int defaultFeatures, //
			SerializerFeature... features) {
		return toJSONStringWithDateFormat(object, dateFormat, defaultFeatures, features);
	}

	/**
	 * @deprecated
	 */
	public static String toJSONStringZ(Object object, SerializeConfig mapping, SerializerFeature... features) {
		return toJSONString(object, mapping, emptyFilters, null, 0, features);
	}

	/**
	 * @since 1.2.42
	 */
	public static byte[] toJSONBytes(Object object, SerializeConfig config, SerializerFeature... features) {
		return toJSONBytes(object, config, emptyFilters, DEFAULT_GENERATE_FEATURE, features);
	}

	/**
	 * @since 1.2.11
	 */
	public static byte[] toJSONBytes(Object object, SerializeConfig config, int defaultFeatures,
			SerializerFeature... features) {
		return toJSONBytes(object, config, emptyFilters, defaultFeatures, features);
	}

	/**
	 * @since 1.2.42
	 */
	public static byte[] toJSONBytes(Object object, SerializeFilter[] filters, SerializerFeature... features) {
		return toJSONBytes(object, SerializeConfig.globalInstance, filters, DEFAULT_GENERATE_FEATURE, features);
	}

	public static byte[] toJSONBytes(Object object, SerializeConfig config, SerializeFilter filter,
			SerializerFeature... features) {
		return toJSONBytes(object, config, new SerializeFilter[] { filter }, DEFAULT_GENERATE_FEATURE, features);
	}

	/**
	 * @since 1.2.42
	 */
	public static byte[] toJSONBytes(Object object, SerializeConfig config, SerializeFilter[] filters,
			int defaultFeatures, SerializerFeature... features) {
		return toJSONBytes(object, config, filters, null, defaultFeatures, features);
	}

	/**
	 * @since 1.2.55
	 */
	public static byte[] toJSONBytes(Object object, SerializeConfig config, SerializeFilter[] filters,
			String dateFormat, int defaultFeatures, SerializerFeature... features) {
		return toJSONBytes(IOUtils.UTF8, object, config, filters, dateFormat, defaultFeatures, features);
	}

	/**
	 * @since 1.2.55
	 */
	public static byte[] toJSONBytes(Charset charset, //
			Object object, //
			SerializeConfig config, //
			SerializeFilter[] filters, //
			String dateFormat, //
			int defaultFeatures, //
			SerializerFeature... features) {
		return toJSONStringWithDateFormat(object, dateFormat, defaultFeatures, features).getBytes(charset);
	}

	public static String toJSONString(Object object, boolean prettyFormat) {
		if (!prettyFormat) {
			return toJSONString(object);
		}

		return toJSONString(object, SerializerFeature.PrettyFormat);
	}

	/**
	 * @deprecated use writeJSONString
	 */
	public static void writeJSONStringTo(Object object, Writer writer, SerializerFeature... features) {
		writeJSONString(writer, object, features);
	}

	/**
	 * This method serializes the specified object into its equivalent json representation.
	 *
	 * @param writer   Writer to which the json representation needs to be written
	 * @param object   the object for which json representation is to be created setting for fastjson
	 * @param features serializer features
	 * @since 1.2.11
	 */
	public static void writeJSONString(Writer writer, Object object, SerializerFeature... features) {
		writeJSONString(writer, object, JSON.DEFAULT_GENERATE_FEATURE, features);
	}

	/**
	 * @since 1.2.11
	 */
	public static void writeJSONString(Writer writer, Object object, int defaultFeatures,
			SerializerFeature... features) {
		String string = toJSONString(object, defaultFeatures, features);
		try {
			writer.write(string);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * write object as json to OutputStream
	 *
	 * @param os       output stream
	 * @param object
	 * @param features serializer features
	 * @throws IOException
	 * @since 1.2.11
	 */
	public static final int writeJSONString(OutputStream os, //
			Object object, //
			SerializerFeature... features) throws IOException {
		return writeJSONString(os, object, DEFAULT_GENERATE_FEATURE, features);
	}

	/**
	 * @since 1.2.11
	 */
	public static final int writeJSONString(OutputStream os, //
			Object object, //
			int defaultFeatures, //
			SerializerFeature... features) throws IOException {
		return writeJSONString(os, //
				IOUtils.UTF8, //
				object, //
				SerializeConfig.globalInstance, //
				null, //
				null, //
				defaultFeatures, //
				features);
	}

	public static final int writeJSONString(OutputStream os, //
			Charset charset, //
			Object object, //
			SerializerFeature... features) throws IOException {
		return writeJSONString(os, //
				charset, //
				object, //
				SerializeConfig.globalInstance, //
				null, //
				null, //
				DEFAULT_GENERATE_FEATURE, //
				features);
	}

	public static final int writeJSONString(OutputStream os, //
			Charset charset, //
			Object object, //
			SerializeConfig config, //
			SerializeFilter[] filters, //
			String dateFormat, //
			int defaultFeatures, //
			SerializerFeature... features) throws IOException {
		String str = toJSONStringWithDateFormat(object, dateFormat, defaultFeatures, features);
		os.write(str.getBytes(charset));
		return str.length();
	}

	// ======================================
	@Override
	public String toString() {
		return toJSONString();
	}

	@Override
	public String toJSONString() {
		return toJSONString(this);
	}

	/**
	 * @since 1.2.57
	 */
	public String toString(SerializerFeature... features) {
		return toJSONString(this, features);
	}

	@Override
	public void writeJSONString(Appendable appendable) {
		String str = toString();
		try {
			appendable.append(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method serializes the specified object into its equivalent representation as a tree of
	 * {@link JSONObject}s.
	 */
	public static Object toJSON(Object javaObject) {
		return toJSON(javaObject, SerializeConfig.globalInstance);
	}

	/**
	 * @deprecated
	 */
	public static Object toJSON(Object javaObject, ParserConfig parserConfig) {
		return toJSON(javaObject, SerializeConfig.globalInstance);
	}

	@SuppressWarnings("unchecked")
	public static Object toJSON(Object javaObject, SerializeConfig config) {
		return JsonMapper.fromJsonTreeNode(JsonMapper.toJsonTreeNode(javaObject));
	}

	public static <T> T toJavaObject(JSON json, Class<T> clazz) {
		return TypeUtils.cast(json, clazz, ParserConfig.getGlobalInstance());
	}

	/**
	 * @since 1.2.9
	 */
	public <T> T toJavaObject(Class<T> clazz) {
		return TypeUtils.cast(this, clazz, ParserConfig.getGlobalInstance());
	}

	/**
	 * @since 1.2.33
	 */
	public <T> T toJavaObject(Type type) {
		return TypeUtils.cast(this, type, ParserConfig.getGlobalInstance());
	}

	/**
	 * @since 1.2.33
	 */
	public <T> T toJavaObject(TypeReference typeReference) {
		Type type = typeReference != null ? typeReference.getType() : null;
		return TypeUtils.cast(this, type, ParserConfig.getGlobalInstance());
	}

	private final static ThreadLocal<byte[]> bytesLocal = new ThreadLocal<byte[]>();

	private static byte[] allocateBytes(int length) {
		byte[] chars = bytesLocal.get();

		if (chars == null) {
			if (length <= 1024 * 64) {
				chars = new byte[1024 * 64];
				bytesLocal.set(chars);
			} else {
				chars = new byte[length];
			}
		} else if (chars.length < length) {
			chars = new byte[length];
		}

		return chars;
	}

	private final static ThreadLocal<char[]> charsLocal = new ThreadLocal<char[]>();

	private static char[] allocateChars(int length) {
		char[] chars = charsLocal.get();

		if (chars == null) {
			if (length <= 1024 * 64) {
				chars = new char[1024 * 64];
				charsLocal.set(chars);
			} else {
				chars = new char[length];
			}
		} else if (chars.length < length) {
			chars = new char[length];
		}

		return chars;
	}

	public static boolean isValid(String str) {
		try {
			return parse(str) != null;
		} catch (Throwable ex) {
			return false;
		}
	}

	public static boolean isValidObject(String str) {
		JsonTreeNode node = getJsonBuilder(DEFAULT_GENERATE_FEATURE).build().fromJson(str);
		return node != null && node.isJsonObjectNode();
	}

	public static boolean isValidArray(String str) {
		JsonTreeNode node = getJsonBuilder(DEFAULT_GENERATE_FEATURE).build().fromJson(str);
		return node != null && node.isJsonArrayNode();
	}

	public static <T> void handleResovleTask(DefaultJSONParser parser, T value) {
		// NOOP
	}

	public final static String VERSION = "1.2.58";
}
