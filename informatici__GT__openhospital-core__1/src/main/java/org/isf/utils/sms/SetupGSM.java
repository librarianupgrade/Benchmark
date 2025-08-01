/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2020 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
 *
 * Open Hospital is a free and open source software for healthcare data management.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.isf.utils.sms;

import java.awt.HeadlessException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.isf.generaldata.ConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**
 * @author Mwithi
 */
public class SetupGSM extends JFrame implements SerialPortEventListener {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(SetupGSM.class);
	private static final String FILE_PROPERTIES = "GSM.properties";

	private Properties props;
	private CommPortIdentifier portId = null;
	private Enumeration<?> portList = null;
	private SerialPort serialPort = null;
	private InputStream inputStream;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new SetupGSM();
		System.exit(0);
	}

	public SetupGSM() {

		props = ConfigurationProperties.loadPropertiesFile(FILE_PROPERTIES, LOGGER);

		String model = props.getProperty("GMM");

		portList = CommPortIdentifier.getPortIdentifiers();

		while (portList.hasMoreElements()) {

			portId = (CommPortIdentifier) portList.nextElement();

			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				//if (portId.getName().equals("COM25")) {

				System.out.println("Port found: " + portId.getName() + " "
						+ (portId.getPortType() == CommPortIdentifier.PORT_SERIAL ? "SERIAL" : "PARALLEL"));

				try {
					serialPort = (SerialPort) portId.open("SmsSender", 10);
					serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);
					serialPort.addEventListener(this);
					serialPort.notifyOnDataAvailable(true);

					OutputStream outputStream = serialPort.getOutputStream();
					if (outputStream != null) {
						System.out.println("Output stream OK");
					} else {
						System.out.println("Output stream not found");
					}

					inputStream = serialPort.getInputStream();
					byte[] command = model.getBytes();
					outputStream.write(command);

					Thread.sleep(5000);

				} catch (PortInUseException e) {
					LOGGER.error("Port in use.");
				} catch (Exception exception) {
					LOGGER.error("Failed to open port '{}'", portId.getName());
					LOGGER.error(exception.getMessage(), exception);
				} finally {
					serialPort.close();
				}
			}
		}
		System.out.println("End.");
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		SerialPort serialPort = (SerialPort) event.getSource();
		String port = serialPort.getName();
		StringBuffer sb = new StringBuffer();
		byte[] buffer = new byte[1];
		try {
			while (inputStream.available() > 0) {
				int len = inputStream.read(buffer);
				sb.append(new String(buffer));
			}
			String answer = sb.toString();
			if (confirm(port, answer) == JOptionPane.YES_OPTION) {
				save(port);
				System.exit(0);
			}
		} catch (IOException ioException) {
			LOGGER.error(ioException.getMessage(), ioException);
		}
	}

	/**
	 * @param port
	 * @param answer
	 * @return
	 * @throws HeadlessException
	 */
	private int confirm(String port, String answer) throws HeadlessException {
		try {
			int ok = answer.indexOf("OK");
			if (ok > 0) {
				answer = answer.substring(2, ok - 3);
			} else {
				return JOptionPane.NO_OPTION;
			}
		} catch (Exception exception) {
			LOGGER.error(exception.getMessage(), exception);
			LOGGER.error("outofbound: '{}'", answer);
		}
		System.out.println(answer.trim());

		int option = JOptionPane.showConfirmDialog(this, "Found modem: " + answer + " on port " + port + "\nConfirm?");
		return option;
	}

	/**
	 * @param port
	 */
	private void save(String port) {
		FileOutputStream out;
		StringBuilder comment = new StringBuilder(" Configuration file for SMS Sender GSM\n");
		comment.append(" PORT = COMx (Windows) or /dev/ttyUSBx (Linux)");
		try {
			out = new FileOutputStream("GSM.properties");
			props.setProperty("PORT", port);
			props.store(out, comment.toString());
			out.close();
		} catch (IOException ioException) {
			LOGGER.error(ioException.getMessage(), ioException);
		}
	}
}
