/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.datasource.unpooled;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.ibatis.io.Resources;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public class UnpooledDataSource implements DataSource {

	private ClassLoader driverClassLoader;
	private Properties driverProperties;
	private static Map<String, Driver> registeredDrivers = new ConcurrentHashMap<String, Driver>();

	private String driver;
	private String url;
	private String username;
	private String password;

	private Boolean autoCommit;
	private Integer defaultTransactionIsolationLevel;

	static {
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			registeredDrivers.put(driver.getClass().getName(), driver);
		}
	}

	public UnpooledDataSource() {
	}

	public UnpooledDataSource(String driver, String url, String username, String password) {
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
	}

	public UnpooledDataSource(String driver, String url, Properties driverProperties) {
		this.driver = driver;
		this.url = url;
		this.driverProperties = driverProperties;
	}

	public UnpooledDataSource(ClassLoader driverClassLoader, String driver, String url, String username,
			String password) {
		this.driverClassLoader = driverClassLoader;
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
	}

	public UnpooledDataSource(ClassLoader driverClassLoader, String driver, String url, Properties driverProperties) {
		this.driverClassLoader = driverClassLoader;
		this.driver = driver;
		this.url = url;
		this.driverProperties = driverProperties;
	}

	public Connection getConnection() throws SQLException {
		return doGetConnection(username, password);
	}

	public Connection getConnection(String username, String password) throws SQLException {
		return doGetConnection(username, password);
	}

	public void setLoginTimeout(int loginTimeout) throws SQLException {
		DriverManager.setLoginTimeout(loginTimeout);
	}

	public int getLoginTimeout() throws SQLException {
		return DriverManager.getLoginTimeout();
	}

	public void setLogWriter(PrintWriter logWriter) throws SQLException {
		DriverManager.setLogWriter(logWriter);
	}

	public PrintWriter getLogWriter() throws SQLException {
		return DriverManager.getLogWriter();
	}

	public ClassLoader getDriverClassLoader() {
		return driverClassLoader;
	}

	public void setDriverClassLoader(ClassLoader driverClassLoader) {
		this.driverClassLoader = driverClassLoader;
	}

	public Properties getDriverProperties() {
		return driverProperties;
	}

	public void setDriverProperties(Properties driverProperties) {
		this.driverProperties = driverProperties;
	}

	public String getDriver() {
		return driver;
	}

	public synchronized void setDriver(String driver) {
		this.driver = driver;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean isAutoCommit() {
		return autoCommit;
	}

	public void setAutoCommit(Boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	public Integer getDefaultTransactionIsolationLevel() {
		return defaultTransactionIsolationLevel;
	}

	public void setDefaultTransactionIsolationLevel(Integer defaultTransactionIsolationLevel) {
		this.defaultTransactionIsolationLevel = defaultTransactionIsolationLevel;
	}

	private Connection doGetConnection(String username, String password) throws SQLException {
		Properties props = new Properties();
		if (driverProperties != null) {
			props.putAll(driverProperties);
		}
		if (username != null) {
			props.setProperty("user", username);
		}
		if (password != null) {
			props.setProperty("password", password);
		}
		return doGetConnection(props);
	}

	private Connection doGetConnection(Properties properties) throws SQLException {
		initializeDriver();
		Connection connection = DriverManager.getConnection(url, properties);
		configureConnection(connection);
		return connection;
	}

	private synchronized void initializeDriver() throws SQLException {
		if (!registeredDrivers.containsKey(driver)) {
			Class<?> driverType;
			try {
				if (driverClassLoader != null) {
					driverType = Class.forName(driver, true, driverClassLoader);
				} else {
					driverType = Resources.classForName(driver);
				}
				// DriverManager requires the driver to be loaded via the system ClassLoader.
				// http://www.kfu.com/~nsayer/Java/dyn-jdbc.html
				Driver driverInstance = (Driver) driverType.newInstance();
				DriverManager.registerDriver(new DriverProxy(driverInstance));
				registeredDrivers.put(driver, driverInstance);
			} catch (Exception e) {
				throw new SQLException("Error setting driver on UnpooledDataSource. Cause: " + e);
			}
		}
	}

	private void configureConnection(Connection conn) throws SQLException {
		if (autoCommit != null && autoCommit != conn.getAutoCommit()) {
			conn.setAutoCommit(autoCommit);
		}
		if (defaultTransactionIsolationLevel != null) {
			conn.setTransactionIsolation(defaultTransactionIsolationLevel);
		}
	}

	private static class DriverProxy implements Driver {
		private Driver driver;

		DriverProxy(Driver d) {
			this.driver = d;
		}

		public boolean acceptsURL(String u) throws SQLException {
			return this.driver.acceptsURL(u);
		}

		public Connection connect(String u, Properties p) throws SQLException {
			return this.driver.connect(u, p);
		}

		public int getMajorVersion() {
			return this.driver.getMajorVersion();
		}

		public int getMinorVersion() {
			return this.driver.getMinorVersion();
		}

		public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
			return this.driver.getPropertyInfo(u, p);
		}

		public boolean jdbcCompliant() {
			return this.driver.jdbcCompliant();
		}

		public Logger getParentLogger() {
			return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		}
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new SQLException(getClass().getName() + " is not a wrapper.");
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	public Logger getParentLogger() {
		return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); // requires JDK version 1.6
	}

}
