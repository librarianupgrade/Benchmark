package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.*;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public abstract class AbstractDateDeserializer extends ContextObjectDeserializer implements ObjectDeserializer {

	@Override
	public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
		return deserialze(parser, clazz, fieldName, null, 0);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName, String format, int features) {
		JSONLexer lexer = parser.lexer;

		Object val;
		if (lexer.token() == JSONToken.LITERAL_INT) {
			long millis = lexer.longValue();
			lexer.nextToken(JSONToken.COMMA);
			if ("unixtime".equals(format)) {
				millis *= 1000;
			}
			val = millis;
		} else if (lexer.token() == JSONToken.LITERAL_STRING) {
			String strVal = lexer.stringVal();

			if (format != null) {
				SimpleDateFormat simpleDateFormat = null;
				try {
					simpleDateFormat = new SimpleDateFormat(format, parser.lexer.getLocale());
				} catch (IllegalArgumentException ex) {
					if (format.contains("T")) {
						String fromat2 = format.replaceAll("T", "'T'");
						try {
							simpleDateFormat = new SimpleDateFormat(fromat2, parser.lexer.getLocale());
						} catch (IllegalArgumentException e2) {
							throw ex;
						}
					}
				}

				if (JSON.defaultTimeZone != null) {
					simpleDateFormat.setTimeZone(parser.lexer.getTimeZone());
				}

				try {
					val = simpleDateFormat.parse(strVal);
				} catch (ParseException ex) {
					val = null;
					// skip
				}

				if (val == null && JSON.defaultLocale == Locale.CHINA) {
					try {
						simpleDateFormat = new SimpleDateFormat(format, Locale.US);
					} catch (IllegalArgumentException ex) {
						if (format.contains("T")) {
							String fromat2 = format.replaceAll("T", "'T'");
							try {
								simpleDateFormat = new SimpleDateFormat(fromat2, parser.lexer.getLocale());
							} catch (IllegalArgumentException e2) {
								throw ex;
							}
						}
					}
					simpleDateFormat.setTimeZone(parser.lexer.getTimeZone());

					try {
						val = simpleDateFormat.parse(strVal);
					} catch (ParseException ex) {
						val = null;
						// skip
					}
				}

				if (val == null) {
					if (format.equals("yyyy-MM-dd'T'HH:mm:ss.SSS") //
							&& strVal.length() == 19) {
						try {
							SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", JSON.defaultLocale);
							df.setTimeZone(JSON.defaultTimeZone);
							val = df.parse(strVal);
						} catch (ParseException ex2) {
							// skip
							val = null;
						}
					} else {
						// skip
						val = null;
					}
				}
			} else {
				val = null;
			}

			if (val == null) {
				val = strVal;
				lexer.nextToken(JSONToken.COMMA);

				if (lexer.isEnabled(Feature.AllowISO8601DateFormat)) {
					JSONScanner iso8601Lexer = new JSONScanner(strVal);
					if (iso8601Lexer.scanISO8601DateIfMatch()) {
						val = iso8601Lexer.getCalendar().getTime();
					}
					iso8601Lexer.close();
				}
			}
		} else if (lexer.token() == JSONToken.NULL) {
			lexer.nextToken();
			val = null;
		} else if (lexer.token() == JSONToken.LBRACE) {
			lexer.nextToken();

			String key;
			if (lexer.token() == JSONToken.LITERAL_STRING) {
				key = lexer.stringVal();

				if (JSON.DEFAULT_TYPE_KEY.equals(key)) {
					lexer.nextToken();
					parser.accept(JSONToken.COLON);

					String typeName = lexer.stringVal();
					Class<?> type = parser.getConfig().checkAutoType(typeName, null, lexer.getFeatures());
					if (type != null) {
						clazz = type;
					}

					parser.accept(JSONToken.LITERAL_STRING);
					parser.accept(JSONToken.COMMA);
				}

				lexer.nextTokenWithColon(JSONToken.LITERAL_INT);
			} else {
				throw new JSONException("syntax error");
			}

			long timeMillis;
			if (lexer.token() == JSONToken.LITERAL_INT) {
				timeMillis = lexer.longValue();
				lexer.nextToken();
			} else {
				throw new JSONException("syntax error : " + lexer.tokenName());
			}

			val = timeMillis;

			parser.accept(JSONToken.RBRACE);
		} else if (parser.getResolveStatus() == DefaultJSONParser.TypeNameRedirect) {
			parser.setResolveStatus(DefaultJSONParser.NONE);
			parser.accept(JSONToken.COMMA);

			if (lexer.token() == JSONToken.LITERAL_STRING) {
				if (!"val".equals(lexer.stringVal())) {
					throw new JSONException("syntax error");
				}
				lexer.nextToken();
			} else {
				throw new JSONException("syntax error");
			}

			parser.accept(JSONToken.COLON);

			val = parser.parse();

			parser.accept(JSONToken.RBRACE);
		} else {
			val = parser.parse();
		}

		return (T) cast(parser, clazz, fieldName, val);
	}

	protected abstract <T> T cast(DefaultJSONParser parser, Type clazz, Object fieldName, Object value);
}
