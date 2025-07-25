package com.github.davidmoten.rx.jdbc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Subscription;
import rx.util.functions.Func1;
import rx.util.functions.Functions;

/**
 * Utility methods.
 */
public final class Util {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private Util() {
		// prevent instantiation
	}

	/**
	 * Logger.
	 */
	private static final Logger log = LoggerFactory.getLogger(Util.class);

	/**
	 * Count the number of JDBC parameters in a sql statement.
	 * 
	 * @param sql
	 * @return
	 */
	static int parametersCount(String sql) {
		// TODO account for ? characters in string constants
		return countOccurrences(sql, '?');
	}

	/**
	 * Returns the number of occurrences of a character in a string.
	 * 
	 * @param haystack
	 * @param needle
	 * @return
	 */
	private static int countOccurrences(String haystack, char needle) {
		int count = 0;
		for (int i = 0; i < haystack.length(); i++) {
			if (haystack.charAt(i) == needle)
				count++;
		}
		return count;
	}

	/**
	 * Cancels then closes a {@link PreparedStatement} and logs exceptions
	 * without throwing. Does nothing if ps is null.
	 * 
	 * @param ps
	 */
	static void closeQuietly(PreparedStatement ps) {
		try {
			if (ps != null && !ps.isClosed()) {
				try {
					ps.cancel();
					log.debug("cancelled " + ps);
				} catch (SQLException e) {
					log.debug(e.getMessage());
				}
				ps.close();
				log.debug("closed " + ps);
			}
		} catch (SQLException e) {
			log.debug(e.getMessage(), e);
		} catch (RuntimeException e) {
			log.debug(e.getMessage(), e);
		}
	}

	/**
	 * Closes a {@link Connection} and logs exceptions without throwing. Does
	 * nothing if connection is null.
	 * 
	 * @param connection
	 */
	static void closeQuietly(Connection connection) {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
				log.debug("closed " + connection);
			}
		} catch (SQLException e) {
			log.debug(e.getMessage(), e);
		} catch (RuntimeException e) {
			log.debug(e.getMessage(), e);
		}
	}

	/**
	 * Closes a {@link Connection} only if the connection is in auto commit mode
	 * and logs exceptions without throwing. Does nothing if connection is null.
	 * 
	 * @param connection
	 */
	static void closeQuietlyIfAutoCommit(Connection connection) {
		try {
			if (connection != null && !connection.isClosed() && connection.getAutoCommit())
				closeQuietly(connection);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Commits a {@link Connection} and logs exceptions without throwing.
	 * 
	 * @param connection
	 */
	static void commit(Connection connection) {
		if (connection != null)
			try {
				connection.commit();
				log.debug("committed");
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
	}

	/**
	 * Rolls back a {@link Connection} and logs exceptions without throwing.
	 * 
	 * @param connection
	 */
	static void rollback(Connection connection) {
		if (connection != null)
			try {
				connection.rollback();
				log.debug("rolled back");
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
	}

	/**
	 * Closes a {@link ResultSet} and logs exceptions without throwing.
	 * 
	 * @param rs
	 */
	static void closeQuietly(ResultSet rs) {
		try {
			if (rs != null && !rs.isClosed()) {
				rs.close();
				log.debug("closed " + rs);
			}
		} catch (SQLException e) {
			log.debug(e.getMessage(), e);
		} catch (RuntimeException e) {
			log.debug(e.getMessage(), e);
		}
	}

	/**
	 * Returns true if and only if {@link Connection} is in auto commit mode.
	 * 
	 * @param con
	 * @return
	 */
	static boolean isAutoCommit(Connection con) {
		try {
			return con.getAutoCommit();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the empty list whenever called.
	 */
	static Func1<Integer, List<Parameter>> TO_EMPTY_PARAMETER_LIST = new Func1<Integer, List<Parameter>>() {
		@Override
		public List<Parameter> call(Integer n) {
			return Collections.emptyList();
		};
	};

	/**
	 * Returns a constant value.
	 * 
	 * @param s
	 * @return
	 */
	static <R, S> Func1<R, S> constant(final S s) {
		return new Func1<R, S>() {
			@Override
			public S call(R t1) {
				return s;
			}
		};
	}

	/**
	 * Returns a function that converts the ResultSet column values into
	 * parameters to the constructor (with number of parameters equals the
	 * number of columns) of type <code>cls</code> then returns an instance of
	 * type <code>cls</code>.
	 * 
	 * @param cls
	 * @return
	 */
	static <T> Func1<ResultSet, T> autoMap(final Class<T> cls) {
		return new Func1<ResultSet, T>() {
			@Override
			public T call(ResultSet rs) {
				return autoMap(rs, cls);
			}
		};
	}

	/**
	 * Converts the ResultSet column values into parameters to the constructor
	 * (with number of parameters equals the number of columns) of type
	 * <code>T</code> then returns an instance of type <code>T</code>.
	 * 
	 * @param cls
	 *            the class of the resultant instance
	 * @return an automapped instance
	 */
	static <T> T autoMap(ResultSet rs, Class<T> cls) {
		try {
			int n = rs.getMetaData().getColumnCount();
			for (Constructor<?> c : cls.getDeclaredConstructors()) {
				if (n == c.getParameterTypes().length) {
					return autoMap(rs, cls, c);
				}
			}
			throw new RuntimeException("constructor with number of parameters=" + n + "  not found in " + cls);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Converts the ResultSet column values into parameters to the given
	 * constructor (with number of parameters equals the number of columns) of
	 * type <code>T</code> then returns an instance of type <code>T</code>.
	 * 
	 * @param rs
	 *            the result set row
	 * @param cls
	 *            type of instance to instantiate
	 * @param c
	 *            constructor to use for instantiation
	 * @return automapped instance
	 */
	private static <T> T autoMap(ResultSet rs, Class<T> cls, Constructor<?> c) {
		Class<?>[] types = c.getParameterTypes();
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < types.length; i++) {
			list.add(autoMap(getObject(rs, types[i], i + 1), types[i]));
		}
		try {
			return newInstance(c, list);
		} catch (RuntimeException e) {
			throw new RuntimeException("problem with parameters=" + getTypeInfo(list) + ", rs types=" + getRowInfo(rs),
					e);
		}
	}

	private static String getTypeInfo(List<Object> list) {

		StringBuilder s = new StringBuilder();
		for (Object o : list) {
			if (s.length() > 0)
				s.append(", ");
			if (o == null)
				s.append("null");
			else {
				s.append(o.getClass().getName());
				s.append("=");
				s.append(o);
			}
		}
		return s.toString();
	}

	private static String getRowInfo(ResultSet rs) {
		StringBuilder s = new StringBuilder();
		try {
			ResultSetMetaData md = rs.getMetaData();
			for (int i = 1; i <= md.getColumnCount(); i++) {
				String name = md.getColumnName(i);
				String type = md.getColumnClassName(i);
				if (s.length() > 0)
					s.append(", ");
				s.append(name);
				s.append("=");
				s.append(type);
			}
		} catch (SQLException e1) {
			throw new RuntimeException(e1);
		}
		return s.toString();
	}

	/**
	 * 
	 * @param c
	 *            constructor to use
	 * @param parameters
	 *            constructor parameters
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static <T> T newInstance(Constructor<?> c, List<Object> parameters) {
		try {
			return (T) c.newInstance(parameters.toArray());
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Converts from java.sql Types to common java types like java.util.Date and
	 * numeric types.
	 * 
	 * @param o
	 * @param cls
	 * @return
	 */
	static Object autoMap(Object o, Class<?> cls) {
		if (o == null)
			return o;
		else if (cls.isAssignableFrom(o.getClass())) {
			return o;
		} else {
			if (o instanceof java.sql.Date) {
				java.sql.Date d = (java.sql.Date) o;
				if (cls.isAssignableFrom(Long.class))
					return d.getTime();
				else if (cls.isAssignableFrom(BigInteger.class))
					return BigInteger.valueOf(d.getTime());
				else
					return o;
			} else if (o instanceof java.sql.Timestamp) {
				Timestamp t = (java.sql.Timestamp) o;
				if (cls.isAssignableFrom(Long.class))
					return t.getTime();
				else if (cls.isAssignableFrom(BigInteger.class))
					return BigInteger.valueOf(t.getTime());
				else
					return o;
			} else if (o instanceof java.sql.Time) {
				Time t = (java.sql.Time) o;
				if (cls.isAssignableFrom(Long.class))
					return t.getTime();
				else if (cls.isAssignableFrom(BigInteger.class))
					return BigInteger.valueOf(t.getTime());
				else
					return o;
			} else if (o instanceof Blob && cls.isAssignableFrom(byte[].class)) {
				return toBytes((Blob) o);
			} else if (o instanceof Clob && cls.isAssignableFrom(String.class)) {
				return toString((Clob) o);
			} else if (o instanceof BigInteger && cls.isAssignableFrom(Long.class)) {
				return ((BigInteger) o).longValue();
			} else if (o instanceof BigInteger && cls.isAssignableFrom(Integer.class)) {
				return ((BigInteger) o).intValue();
			} else if (o instanceof BigInteger && cls.isAssignableFrom(Double.class)) {
				return ((BigInteger) o).doubleValue();
			} else if (o instanceof BigInteger && cls.isAssignableFrom(Float.class)) {
				return ((BigInteger) o).floatValue();
			} else if (o instanceof BigInteger && cls.isAssignableFrom(Short.class)) {
				return ((BigInteger) o).shortValue();
			} else if (o instanceof BigInteger && cls.isAssignableFrom(BigDecimal.class)) {
				return new BigDecimal((BigInteger) o);
			} else if (o instanceof BigDecimal && cls.isAssignableFrom(Double.class)) {
				return ((BigDecimal) o).doubleValue();
			} else if (o instanceof BigDecimal && cls.isAssignableFrom(Integer.class)) {
				return ((BigDecimal) o).toBigInteger().intValue();
			} else if (o instanceof BigDecimal && cls.isAssignableFrom(Float.class)) {
				return ((BigDecimal) o).floatValue();
			} else if (o instanceof BigDecimal && cls.isAssignableFrom(Short.class)) {
				return ((BigDecimal) o).toBigInteger().shortValue();
			} else if (o instanceof BigDecimal && cls.isAssignableFrom(Long.class)) {
				return ((BigDecimal) o).toBigInteger().longValue();
			} else if (o instanceof BigDecimal && cls.isAssignableFrom(BigInteger.class)) {
				return ((BigDecimal) o).toBigInteger();
			}

			else
				return o;
		}
	}

	public static <T> Object getObject(final ResultSet rs, Class<T> cls, int i) {
		try {
			final int type = rs.getMetaData().getColumnType(i);
			// TODO java.util.Calendar support
			// TODO XMLGregorian Calendar support
			if (type == Types.DATE)
				return rs.getDate(i, Calendar.getInstance());
			else if (type == Types.TIME)
				return rs.getTime(i, Calendar.getInstance());
			else if (type == Types.TIMESTAMP)
				return rs.getTimestamp(i, Calendar.getInstance());
			else if (type == Types.CLOB && cls.equals(String.class)) {
				return toString(rs.getClob(i));
			} else if (type == Types.CLOB && Reader.class.isAssignableFrom(cls)) {
				Clob c = rs.getClob(i);
				Reader r = c.getCharacterStream();
				return createFreeOnCloseReader(c, r);
			} else if (type == Types.BLOB && cls.equals(byte[].class)) {
				return toBytes(rs.getBlob(i));
			} else if (type == Types.BLOB && InputStream.class.isAssignableFrom(cls)) {
				final Blob b = rs.getBlob(i);
				final InputStream is = rs.getBlob(i).getBinaryStream();
				return createFreeOnCloseInputStream(b, is);
			} else
				return rs.getObject(i);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the bytes of a {@link Blob} and frees the blob resource.
	 * 
	 * @param b
	 *            blob
	 * @return
	 */
	private static byte[] toBytes(Blob b) {
		try {
			InputStream is = b.getBinaryStream();
			byte[] result = IOUtils.toByteArray(is);
			is.close();
			b.free();
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Returns the String of a {@link Clob} and frees the clob resource.
	 * 
	 * @param c
	 * @return
	 */
	private static String toString(Clob c) {
		try {
			Reader reader = c.getCharacterStream();
			String result = IOUtils.toString(reader);
			reader.close();
			c.free();
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Automatically frees the blob (<code>blob.free()</code>) once the blob
	 * {@link InputStream} is closed.
	 * 
	 * @param blob
	 * @param is
	 * @return
	 */
	private static InputStream createFreeOnCloseInputStream(final Blob blob, final InputStream is) {
		return new InputStream() {

			@Override
			public int read() throws IOException {
				return is.read();
			}

			@Override
			public void close() throws IOException {
				try {
					is.close();
				} finally {
					try {
						blob.free();
					} catch (SQLException e) {
						log.debug(e.getMessage());
					}
				}
			}
		};
	}

	/**
	 * Automatically frees the clob (<code>Clob.free()</code>) once the clob
	 * Reader is closed.
	 * 
	 * @param clob
	 * @param reader
	 * @return
	 */
	private static Reader createFreeOnCloseReader(final Clob clob, final Reader reader) {
		return new Reader() {

			@Override
			public void close() throws IOException {
				try {
					reader.close();
				} finally {
					try {
						clob.free();
					} catch (SQLException e) {
						log.debug(e.getMessage());
					}
				}
			}

			@Override
			public int read(char[] cbuf, int off, int len) throws IOException {
				return reader.read(cbuf, off, len);
			}
		};
	}

	static void setParameters(PreparedStatement ps, List<Parameter> params) throws SQLException {
		for (int i = 1; i <= params.size(); i++) {
			Object o = params.get(i - 1).getValue();
			if (o == null)
				ps.setObject(i, null);
			else {
				Class<?> cls = o.getClass();
				if (Clob.class.isAssignableFrom(cls)) {
					setClob(ps, i, o, cls);
				} else if (Blob.class.isAssignableFrom(cls)) {
					setBlob(ps, i, o, cls);
				} else if (Calendar.class.isAssignableFrom(cls)) {
					Calendar cal = (Calendar) o;
					Timestamp t = new java.sql.Timestamp(cal.getTimeInMillis());
					ps.setTimestamp(i, t, cal);
				} else if (Time.class.isAssignableFrom(cls)) {
					Calendar cal = Calendar.getInstance();
					ps.setTime(i, (Time) o, cal);
				} else if (Timestamp.class.isAssignableFrom(cls)) {
					Calendar cal = Calendar.getInstance();
					ps.setTimestamp(i, (Timestamp) o, cal);
				} else if (java.sql.Date.class.isAssignableFrom(cls)) {
					Calendar cal = Calendar.getInstance();
					ps.setDate(i, (java.sql.Date) o, cal);
				} else if (java.util.Date.class.isAssignableFrom(cls)) {
					Calendar cal = Calendar.getInstance();
					java.util.Date date = (java.util.Date) o;
					ps.setTimestamp(i, new java.sql.Timestamp(date.getTime()), cal);
				} else
					try {
						ps.setObject(i, o);
					} catch (SQLException e) {
						log.debug(e.getMessage() + " when setting ps.setObject(" + i + "," + o + ")");
						throw e;
					}
			}
		}
	}

	private static void setBlob(PreparedStatement ps, int i, Object o, Class<?> cls) throws SQLException {
		final InputStream is;
		if (o instanceof byte[]) {
			is = new ByteArrayInputStream((byte[]) o);
		} else if (o instanceof InputStream)
			is = (InputStream) o;
		else
			throw new RuntimeException("cannot insert parameter of type " + cls + " into blob column " + i);
		Blob c = ps.getConnection().createBlob();
		OutputStream os = c.setBinaryStream(1);
		copy(is, os);
		ps.setBlob(i, c);
	}

	private static void setClob(PreparedStatement ps, int i, Object o, Class<?> cls) throws SQLException {
		final Reader r;
		if (o instanceof String)
			r = new StringReader((String) o);
		else if (o instanceof Reader)
			r = (Reader) o;
		else
			throw new RuntimeException("cannot insert parameter of type " + cls + " into clob column " + i);
		Clob c = ps.getConnection().createClob();
		Writer w = c.setCharacterStream(1);
		copy(r, w);
		ps.setClob(i, c);
	}

	/**
	 * Copies a {@link Reader} to a {@link Writer}.
	 * 
	 * @param input
	 * @param output
	 * @return
	 */
	private static int copy(Reader input, Writer output) {
		try {
			return IOUtils.copy(input, output);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Copies an {@link InputStream} to an {@link OutputStream}.
	 * 
	 * @param input
	 * @param output
	 * @return
	 */
	private static int copy(InputStream input, OutputStream output) {
		try {
			return IOUtils.copy(input, output);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns a function that reads a {@link Reader} into a String.
	 */
	public static final Func1<Reader, String> READER_TO_STRING = new Func1<Reader, String>() {
		@Override
		public String call(Reader r) {
			try {
				return IOUtils.toString(r);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	};

	public static final <T> Func1<T, T> println() {
		return new Func1<T, T>() {
			@Override
			public T call(T t) {
				System.out.println(t);
				return t;
			}
		};
	}

	/**
	 * The first sequence will be emitted in its entirety and ignored before o2
	 * starts emitting.
	 * 
	 * @param o1
	 * @param o2
	 * @return
	 */
	@SuppressWarnings("unchecked")
	static <T> Observable<T> concatButIgnoreFirstSequence(Observable<?> o1, Observable<T> o2) {
		return Observable.concat((Observable<T>) o1.filter(Functions.alwaysFalse()), o2);
	}

	/**
	 * Create an rx {@link Subscription} that cancels the given
	 * {@link Cancellable} on unsubscribe.
	 * 
	 * @param cancellable
	 * @return
	 */
	static Subscription createSubscription(final Cancellable cancellable) {
		return new Subscription() {
			@Override
			public void unsubscribe() {
				cancellable.cancel();
			}
		};
	}

}
