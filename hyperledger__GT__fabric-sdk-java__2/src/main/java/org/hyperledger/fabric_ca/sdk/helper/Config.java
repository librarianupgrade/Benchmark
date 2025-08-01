/*
 *  Copyright 2016, 2017 IBM, DTCC, Fujitsu Australia Software Technology, IBM - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hyperledger.fabric_ca.sdk.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;

/**
 * Config allows for a global config of the toolkit. Central location for all
 * toolkit configuration defaults. Has a local config file that can override any
 * property defaults. Config file can be relocated via a system property
 * "org.hyperledger.fabric.sdk.configuration". Any property can be overridden
 * with environment variable and then overridden
 * with a java system property. Property hierarchy goes System property
 * overrides environment variable which overrides config file for default values specified here.
 */

public class Config {
	private static final Log logger = LogFactory.getLog(Config.class);

	private static final String BASE_PROP = "org.hyperledger.fabric_ca.sdk.";

	private static final String DEFAULT_CONFIG = "config.properties";
	public static final String ORG_HYPERLEDGER_FABRIC_SDK_CONFIGURATION = BASE_PROP + "configuration";
	public static final String SECURITY_LEVEL = BASE_PROP + "security_level";
	public static final String HASH_ALGORITHM = BASE_PROP + "hash_algorithm";
	public static final String CACERTS = "cacerts";
	public static final String PROPOSAL_WAIT_TIME = BASE_PROP + "proposal.wait.time";

	public static final String ASYMMETRIC_KEY_TYPE = BASE_PROP + "crypto.asymmetric_key_type";
	public static final String KEY_AGREEMENT_ALGORITHM = BASE_PROP + "crypto.key_agreement_algorithm";
	public static final String SYMMETRIC_KEY_TYPE = BASE_PROP + "crypto.symmetric_key_type";
	public static final String SYMMETRIC_KEY_BYTE_COUNT = BASE_PROP + "crypto.symmetric_key_byte_count";
	public static final String SYMMETRIC_ALGORITHM = BASE_PROP + "crypto.symmetric_algorithm";
	public static final String MAC_KEY_BYTE_COUNT = BASE_PROP + "crypto.mac_key_byte_count";
	public static final String CERTIFICATE_FORMAT = BASE_PROP + "crypto.certificate_format";
	public static final String SIGNATURE_ALGORITHM = BASE_PROP + "crypto.default_signature_algorithm";
	public static final String MAX_LOG_STRING_LENGTH = BASE_PROP + "log.stringlengthmax";
	public static final String LOGGERLEVEL = BASE_PROP + "loglevel"; // ORG_HYPERLEDGER_FABRIC_CA_SDK_LOGLEVEL=TRACE,DEBUG

	public static final String CONNECTION_REQUEST_TIMEOUT = BASE_PROP + "connection.connection_request_timeout"; //ORG_HYPERLEDGER_FABRIC_CA_SDK_CONNECTION_CONNECTION_REQUEST_TIMEOUT
	public static final String CONNECT_TIMEOUT = BASE_PROP + "connection.connect_timeout"; //ORG_HYPERLEDGER_FABRIC_CA_SDK_CONNECTION_CONNECT_TIMEOUT
	public static final String SOCKET_TIMEOUT = BASE_PROP + "connection.socket_timeout"; //ORG_HYPERLEDGER_FABRIC_CA_SDK_CONNECTION_SOCKET_TIMEOUT

	private static Config config;
	private static final Properties sdkProperties = new Properties();

	private Config() {
		File loadFile;
		FileInputStream configProps;

		try {
			loadFile = new File(System.getProperty(ORG_HYPERLEDGER_FABRIC_SDK_CONFIGURATION, DEFAULT_CONFIG))
					.getAbsoluteFile();
			logger.debug(String.format("Loading configuration from %s and it is present: %b", loadFile.toString(),
					loadFile.exists()));
			configProps = new FileInputStream(loadFile);
			sdkProperties.load(configProps);

		} catch (IOException e) {
			logger.warn(
					String.format("Failed to load any configuration from: %s. Using toolkit defaults", DEFAULT_CONFIG));
		} finally {

			// Default values
			defaultProperty(ASYMMETRIC_KEY_TYPE, "EC");
			defaultProperty(KEY_AGREEMENT_ALGORITHM, "ECDH");
			defaultProperty(SYMMETRIC_KEY_TYPE, "AES");
			defaultProperty(SYMMETRIC_KEY_BYTE_COUNT, "32");
			defaultProperty(SYMMETRIC_ALGORITHM, "AES/CFB/NoPadding");
			defaultProperty(MAC_KEY_BYTE_COUNT, "32");
			defaultProperty(CERTIFICATE_FORMAT, "X.509");
			defaultProperty(SIGNATURE_ALGORITHM, "SHA256withECDSA");
			defaultProperty(SECURITY_LEVEL, "256");
			defaultProperty(HASH_ALGORITHM, "SHA2");

			defaultProperty(CONNECTION_REQUEST_TIMEOUT, "-1");
			defaultProperty(CONNECT_TIMEOUT, "-1");
			defaultProperty(SOCKET_TIMEOUT, "-1");

			// TODO remove this once we have implemented MSP and get the peer certs from the channel
			defaultProperty(CACERTS, "/genesisblock/peercacert.pem");

			defaultProperty(PROPOSAL_WAIT_TIME, "12000");

			defaultProperty(MAX_LOG_STRING_LENGTH, "64");

			defaultProperty(LOGGERLEVEL, null);

			final String inLogLevel = sdkProperties.getProperty(LOGGERLEVEL);

			if (null != inLogLevel) {

				org.apache.log4j.Level setTo = null;

				switch (inLogLevel) {

				case "TRACE":
					setTo = org.apache.log4j.Level.TRACE;
					break;

				case "DEBUG":
					setTo = org.apache.log4j.Level.DEBUG;
					break;

				case "INFO":
					setTo = Level.INFO;
					break;

				case "WARN":
					setTo = Level.WARN;
					break;

				case "ERROR":
					setTo = Level.ERROR;
					break;

				default:
					setTo = Level.INFO;
					break;

				}

				if (null != setTo) {
					org.apache.log4j.Logger.getLogger("org.hyperledger.fabric_ca").setLevel(setTo);
				}

			}

		}

	}

	/**
	 * getConfig return back singleton for SDK configuration.
	 *
	 * @return Global configuration
	 */
	public static Config getConfig() {
		if (null == config) {
			config = new Config();
		}
		return config;

	}

	/**
	 * getProperty return back property for the given value.
	 *
	 * @param property
	 * @return String value for the property
	 */
	private String getProperty(String property) {

		String ret = sdkProperties.getProperty(property);

		if (null == ret) {
			logger.warn(String.format("No configuration value found for '%s'", property));
		}
		return ret;
	}

	private static void defaultProperty(String key, String value) {

		String ret = System.getProperty(key);
		if (ret != null) {
			sdkProperties.put(key, ret);
		} else {
			String envKey = key.toUpperCase().replaceAll("\\.", "_");
			ret = System.getenv(envKey);
			if (null != ret) {
				sdkProperties.put(key, ret);
			} else {
				if (null == sdkProperties.getProperty(key) && value != null) {
					sdkProperties.put(key, value);
				}

			}

		}
	}

	/**
	 * Get the configured security level. The value determines the elliptic curve used to generate keys.
	 *
	 * @return the security level.
	 */
	public int getSecurityLevel() {

		return Integer.parseInt(getProperty(SECURITY_LEVEL));

	}

	/**
	 * Get the name of the configured hash algorithm, used for digital signatures.
	 *
	 * @return the hash algorithm name.
	 */
	public String getHashAlgorithm() {
		return getProperty(HASH_ALGORITHM);

	}

	public String[] getPeerCACerts() {
		return getProperty(CACERTS).split("'");
	}

	/**
	 * Returns the timeout for a single proposal request to endorser in milliseconds.
	 *
	 * @return the timeout for a single proposal request to endorser in milliseconds
	 */
	public long getProposalWaitTime() {
		return Long.parseLong(getProperty(PROPOSAL_WAIT_TIME));
	}

	public String getAsymmetricKeyType() {
		return getProperty(ASYMMETRIC_KEY_TYPE);
	}

	public String getKeyAgreementAlgorithm() {
		return getProperty(KEY_AGREEMENT_ALGORITHM);
	}

	public String getSymmetricKeyType() {
		return getProperty(SYMMETRIC_KEY_TYPE);
	}

	public int getSymmetricKeyByteCount() {
		return Integer.parseInt(getProperty(SYMMETRIC_KEY_BYTE_COUNT));
	}

	public String getSymmetricAlgorithm() {
		return getProperty(SYMMETRIC_ALGORITHM);
	}

	public int getMACKeyByteCount() {
		return Integer.parseInt(getProperty(MAC_KEY_BYTE_COUNT));
	}

	public String getCertificateFormat() {
		return getProperty(CERTIFICATE_FORMAT);
	}

	public String getSignatureAlgorithm() {
		return getProperty(SIGNATURE_ALGORITHM);
	}

	public int maxLogStringLength() {
		return Integer.parseInt(getProperty(MAX_LOG_STRING_LENGTH));
	}

	/**
	 * milliseconds used when requesting a connection.
	 *
	 * @return
	 */

	public int getConnectionRequestTimeout() {
		return Integer.parseInt(getProperty(CONNECTION_REQUEST_TIMEOUT));
	}

	/**
	 * Determines the timeout in milliseconds until a connection is established.
	 * @return
	 */
	public int getConnectTimeout() {
		return Integer.parseInt(getProperty(CONNECT_TIMEOUT));
	}

	/**
	 * Defines the socket timeout (SO_TIMEOUT) in milliseconds, which is the timeout for waiting for data
	 * @return
	 */
	public int getSocketTimeout() {

		return Integer.parseInt(getProperty(SOCKET_TIMEOUT));
	}
}
