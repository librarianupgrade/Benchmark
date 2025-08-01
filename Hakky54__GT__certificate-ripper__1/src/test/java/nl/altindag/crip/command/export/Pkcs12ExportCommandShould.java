/*
 * Copyright 2021 Thunderberry.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.altindag.crip.command.export;

import nl.altindag.crip.command.FileBaseTest;
import nl.altindag.ssl.util.KeyStoreUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

import static nl.altindag.crip.IOTestUtils.getResource;
import static org.assertj.core.api.Assertions.assertThat;

class Pkcs12ExportCommandShould extends FileBaseTest {

	@Test
	void exportMultipleCertificateFromChainToACustomFilename() throws IOException, KeyStoreException {
		KeyStore expectedTruststore = KeyStoreUtils.loadKeyStore(
				getResource("reference-files/pkcs12/server-one/truststore.p12"), "changeit".toCharArray());

		cmd.execute("export", "pkcs12", "--url=https://localhost:8443",
				"--destination=" + TEMP_DIRECTORY.toAbsolutePath().resolve("my-truststore.p12"));

		List<Path> files = Files.walk(TEMP_DIRECTORY, 1).filter(Files::isRegularFile).collect(Collectors.toList());

		assertThat(files).hasSize(1);
		assertThat(consoleCaptor.getStandardOutput()).contains("Exported certificates to " + files.get(0));
		assertThat(files).allMatch(path -> path.toString().endsWith("my-truststore.p12"));

		KeyStore truststore = KeyStoreUtils.loadKeyStore(files.get(0), "changeit".toCharArray());

		int certificateCounter = 0;
		Enumeration<String> aliases = truststore.aliases();
		while (aliases.hasMoreElements()) {
			certificateCounter++;
			String alias = aliases.nextElement();
			Certificate certificate = truststore.getCertificate(alias);
			Certificate expectedCertificate = expectedTruststore.getCertificate(alias);
			assertThat(certificate).isEqualTo(expectedCertificate);
		}

		assertThat(certificateCounter).isEqualTo(2);
	}

}
