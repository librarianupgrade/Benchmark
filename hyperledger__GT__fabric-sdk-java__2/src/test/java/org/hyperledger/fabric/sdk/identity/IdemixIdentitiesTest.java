/*
 *
 *  Copyright IBM Corp. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.hyperledger.fabric.sdk.identity;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.milagro.amcl.FP256BN.BIG;
import org.apache.milagro.amcl.FP256BN.ECP;
import org.bouncycastle.util.io.pem.PemReader;
import org.hyperledger.fabric.protos.common.MspPrincipal;
import org.hyperledger.fabric.protos.idemix.Idemix;
import org.hyperledger.fabric.protos.msp.Identities;
import org.hyperledger.fabric.protos.msp.MspConfigPackage;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.idemix.IdemixCredential;
import org.hyperledger.fabric.sdk.idemix.IdemixIssuerPublicKey;
import org.hyperledger.fabric.sdk.idemix.IdemixPseudonym;
import org.hyperledger.fabric.sdk.idemix.IdemixSignature;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for IdemixIdentity and IdemixSigningIdentity
 */
public class IdemixIdentitiesTest {

	// Test resources with crypto material generated by the idemixgen tool (in go)
	private static final String TEST_PATH = "src/test/fixture/IdemixIdentitiesTest/";
	private static final String USER_PATH = "/user/";
	private static final String VERIFIER_PATH = "/msp/";
	private static final String MSP1Broken = "MSP1Broken";
	private static final String MSP1OU1 = "MSP1OU1";
	private static final String OU1 = "OU1";
	private static final String OU2 = "OU2";
	private static final String MSP1OU1Admin = "MSP1OU1Admin";
	private static final String MSP1OU2 = "MSP1OU2";
	private static final String MSP1Verifier = "MSP1Verifier";
	private static final String MSP2OU1 = "MSP2OU1";
	private static final String SIGNER_CONFIG = "SignerConfig";
	private static final String REVOCATION_PUBLIC_KEY = "RevocationPublicKey";
	private static final String IPK_CONFIG = "IssuerPublicKey";

	private static IdemixCredential cred = null;
	private static Idemix.CredentialRevocationInformation cri = null;
	private static IdemixIssuerPublicKey ipk = null;
	private static PublicKey revocationPk = null;
	private static BIG sk = null;
	private static IdemixPseudonym nym = null;
	private static ECP nymPublic = null;
	private static IdemixSignature proof = null;
	private static IdemixSigningIdentity signingIdentity = null;

	private static byte[] message = { 1, 2, 3, 4 };
	private static byte[] sigTest = { 1, 2, 3, 4 };

	// Setup using a happy path
	@BeforeClass
	public static void setup() {

		// Parse crypto material from files
		MspConfigPackage.IdemixMSPSignerConfig signerConfig = null;
		try {
			signerConfig = readIdemixMSPConfig(TEST_PATH + MSP1OU1 + USER_PATH, SIGNER_CONFIG);
		} catch (Exception e) {
			fail("Unexpected exception while reading signerconfig: " + e.getMessage());
		}
		assertNotNull(signerConfig);

		try {
			revocationPk = readIdemixRevocationPublicKey(TEST_PATH + MSP1OU1 + VERIFIER_PATH, REVOCATION_PUBLIC_KEY);
		} catch (Exception e) {
			fail("Unexpected exception while reading revocation public key: " + e.getMessage());
		}
		assertNotNull(revocationPk);

		Idemix.IssuerPublicKey ipkProto = null;
		try {
			ipkProto = readIdemixIssuerPublicKey(TEST_PATH + MSP1OU1 + VERIFIER_PATH, IPK_CONFIG);
		} catch (IOException e1) {
			fail("Unexpected exception while reading revocation public key" + e1.getMessage());
		}

		ipk = new IdemixIssuerPublicKey(ipkProto);
		assertTrue(ipk.check());

		sk = BIG.fromBytes(signerConfig.getSk().toByteArray());

		Idemix.Credential credProto = null;
		try {
			credProto = Idemix.Credential.parseFrom(signerConfig.getCred());
		} catch (InvalidProtocolBufferException e) {
			fail("Could not parse a credential");
		}

		assertNotNull(credProto);

		cred = new IdemixCredential(credProto);

		try {
			cri = Idemix.CredentialRevocationInformation.parseFrom(signerConfig.getCredentialRevocationInformation());
		} catch (InvalidProtocolBufferException e) {
			fail("failed to extract cri from signer config: " + e.getMessage());
		}
		assertNotNull(cri);

		try {
			signingIdentity = new IdemixSigningIdentity(ipk, revocationPk, MSP1OU1, sk, cred, cri, OU1,
					IdemixRoles.MEMBER.getValue());
		} catch (CryptoException | InvalidArgumentException e) {
			fail("Could not create Idemix Signing Identity" + e.getMessage());
		}

		assertNotNull(signingIdentity);

		nym = signingIdentity.getNym();

		nymPublic = nym.getNym();

		proof = signingIdentity.getProof();

	}

	// Test creating a signing identity with MSP1Verifier (should fail)
	@Test(expected = IOException.class)
	public void testIdemixSigningIdentityVerifier() throws IOException {
		try {
			createIdemixSigningIdentity(MSP1Verifier);
		} catch (CryptoException | InvalidArgumentException | InvalidKeySpecException | NoSuchAlgorithmException e) {
			/* If exception throw test fails */ }

	}

	// Test creating a signing identity with MSP1Broken (should fail)
	@Test(expected = IOException.class)
	public void testIdemixSigningIdentityBroken() throws IOException {
		try {
			createIdemixSigningIdentity(MSP1Broken);
		} catch (CryptoException | InvalidArgumentException | InvalidKeySpecException | NoSuchAlgorithmException e) {
			fail("Unexpected Exception" + e.getMessage());
		}

	}

	// Test creating a signer config
	@Test
	public void testIdemixMSPSignerConfigSuccess() {
		MspConfigPackage.IdemixMSPSignerConfig signerConfig = null;
		try {
			signerConfig = readIdemixMSPConfig(TEST_PATH + MSP1OU1 + USER_PATH, SIGNER_CONFIG);
		} catch (InvalidProtocolBufferException e) {
			fail("Unexpected IPBException" + e.getMessage());
		} catch (IOException e) {
			fail("Unexpected IOException" + e.getMessage());
		}
		assertNotNull(signerConfig);
	}

	// Test creating a signing identity from null input
	@Test(expected = InvalidArgumentException.class)
	public void testIdemixSigningIdentityInputNullPk() throws InvalidArgumentException {
		try {
			new IdemixSigningIdentity(null, revocationPk, MSP1OU1, sk, cred, cri, OU1, IdemixRoles.MEMBER.getValue());
		} catch (CryptoException e) {
			fail("Unexpected Crypto exception");
		}
	}

	@Test(expected = InvalidArgumentException.class)
	public void testIdemixSigningIdentityInputNullRevPk() throws InvalidArgumentException, CryptoException {
		new IdemixSigningIdentity(ipk, null, MSP1OU1, sk, cred, cri, OU1, IdemixRoles.MEMBER.getValue());
	}

	@Test(expected = InvalidArgumentException.class)
	public void testIdemixSigningIdentityInputNullMsp() throws InvalidArgumentException, CryptoException {
		new IdemixSigningIdentity(ipk, revocationPk, null, sk, cred, cri, OU1, IdemixRoles.MEMBER.getValue());
	}

	@Test(expected = InvalidArgumentException.class)
	public void testIdemixSigningIdentityInputEmptymsp() throws InvalidArgumentException, CryptoException {
		new IdemixSigningIdentity(ipk, revocationPk, "", sk, cred, cri, OU1, IdemixRoles.MEMBER.getValue());
	}

	@Test(expected = InvalidArgumentException.class)
	public void testIdemixSigningIdentityInputNullSk() throws InvalidArgumentException, CryptoException {
		new IdemixSigningIdentity(ipk, revocationPk, MSP1OU1, null, cred, cri, OU1, IdemixRoles.MEMBER.getValue());
	}

	@Test(expected = InvalidArgumentException.class)
	public void testIdemixSigningIdentityInputNullCri() throws InvalidArgumentException, CryptoException {
		new IdemixSigningIdentity(ipk, revocationPk, MSP1OU1, sk, cred, null, OU1, IdemixRoles.MEMBER.getValue());
	}

	@Test(expected = InvalidArgumentException.class)
	public void testIdemixSigningIdentityInputNullCred() throws InvalidArgumentException, CryptoException {
		new IdemixSigningIdentity(ipk, revocationPk, MSP1OU1, sk, null, cri, OU1, IdemixRoles.MEMBER.getValue());
	}

	// Test Signing and Verification with Signing Identity
	@Test(expected = InvalidArgumentException.class)
	public void testSigningNullMsg() throws InvalidArgumentException, CryptoException {
		testSigning(signingIdentity, message, null, true);
	}

	@Test(expected = InvalidArgumentException.class)
	public void testSigningNullSig() throws InvalidArgumentException, CryptoException {
		testSigning(signingIdentity, null, sigTest, true);
	}

	@Test(expected = InvalidArgumentException.class)
	public void testSigningNullMsgSig() throws InvalidArgumentException, CryptoException {
		testSigning(signingIdentity, null, null, true);
	}

	@Test
	public void testSigningSuccess() throws InvalidArgumentException, CryptoException {
		assertTrue(testSigning(signingIdentity, message, null, false));
	}

	@Test
	public void testSerializingAndDeserializingIdentity() {
		Identities.SerializedIdentity proto = signingIdentity.createSerializedIdentity();
		assertNotNull(proto);

		Identities.SerializedIdemixIdentity idemixProto = null;
		try {
			idemixProto = Identities.SerializedIdemixIdentity.parseFrom(proto.getIdBytes());
		} catch (InvalidProtocolBufferException e) {
			fail("Could not parse Idemix Serialized Identity" + e.getMessage());
		}
		if (idemixProto != null) {
			new ECP(BIG.fromBytes(idemixProto.getNymX().toByteArray()),
					BIG.fromBytes(idemixProto.getNymY().toByteArray()));
			idemixProto.getOu().toByteArray();
			idemixProto.getRole().toByteArray();
			try {
				new IdemixSignature(Idemix.Signature.parseFrom(idemixProto.getProof().toByteArray()));
			} catch (InvalidProtocolBufferException e) {
				fail("Cannot deserialize proof" + e.getMessage());
			}
		}

		try {
			new IdemixIdentity(proto);
		} catch (CryptoException | InvalidArgumentException e) {
			fail("Cannot create Idemix Identity from Proto" + e.getMessage());
		}

	}

	// Test creating IdemixIdentity

	@Test(expected = InvalidArgumentException.class)
	public void testIdemixIdentityInputNull() throws InvalidArgumentException {
		try {
			new IdemixIdentity(null);
		} catch (CryptoException e) {
			fail("Unexpected Crypto exception " + e.getMessage());
		}
	}

	@Test(expected = InvalidArgumentException.class)
	public void testIdemixIdentityInputNullMsp() throws InvalidArgumentException {
		new IdemixIdentity(null, ipk, nymPublic, OU1, IdemixRoles.MEMBER.getValue(), proof);
	}

	@Test(expected = InvalidArgumentException.class)
	public void testIdemixIdentityInputNullNym() throws InvalidArgumentException {
		new IdemixIdentity(MSP1OU1, ipk, null, OU1, IdemixRoles.MEMBER.getValue(), proof);
	}

	@Test(expected = InvalidArgumentException.class)
	public void testIdemixIdentityInputNullOu() throws InvalidArgumentException {
		new IdemixIdentity(MSP1OU1, ipk, nymPublic, null, IdemixRoles.MEMBER.getValue(), proof);
	}

	@Test(expected = InvalidArgumentException.class)
	public void testIdemixIdentityInputNullProof() throws InvalidArgumentException {
		new IdemixIdentity(MSP1OU1, ipk, nymPublic, OU1, IdemixRoles.MEMBER.getValue(), null);
	}

	@Test(expected = InvalidArgumentException.class)
	public void testIdemixIdentityInputNullIpk() throws InvalidArgumentException {
		new IdemixIdentity(MSP1OU1, null, nymPublic, OU1, IdemixRoles.MEMBER.getValue(), proof);
	}

	@Test
	public void testIdemixIdentity() {
		try {
			new IdemixIdentity(MSP1OU1, ipk, nymPublic, OU1, IdemixRoles.MEMBER.getValue(), proof);
		} catch (InvalidArgumentException e) {
			fail("Unexpected Invalid Argument exception" + e.getMessage());
		}
	}

	// Test creating different signing identities
	@Test
	public void testSigningIdentityMSP1OU1Admin() {
		assertTrue(testCreatingSigningIdentityAndSign(MSP1OU1Admin));
	}

	@Test
	public void testSigningIdentityMSP1OU2() {
		assertTrue(testCreatingSigningIdentityAndSign(MSP1OU2));
	}

	@Test
	public void testSigningIdentityMSP2OU1() {
		assertTrue(testCreatingSigningIdentityAndSign(MSP2OU1));
	}

	// Helper functions

	/**
	 * Helper function to create a Signing Identity and sign with it
	 *
	 * @param mspId
	 * @return
	 */
	public boolean testCreatingSigningIdentityAndSign(String mspId) {

		boolean b = false;

		IdemixSigningIdentity signingIdentityTest = null;
		try {
			signingIdentityTest = createIdemixSigningIdentity(mspId);
		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage());
		}

		assertNotNull(signingIdentityTest);

		// Test signing using this identity
		try {
			b = testSigning(signingIdentityTest, message, null, false);
		} catch (CryptoException | InvalidArgumentException e) {
			fail("Unexpected exception: " + e.getMessage());
		}

		return b;
	}

	/**
	 * Helper function for testing signing
	 *
	 * @param signIdentity
	 * @return
	 * @throws InvalidArgumentException
	 */
	public boolean testSigning(IdemixSigningIdentity signIdentity, byte[] msg, byte[] sigInput, boolean useInputSig)
			throws CryptoException, InvalidArgumentException {

		byte[] sig = signIdentity.sign(msg);
		byte[] otherMsg = { 1, 1, 1, 1 };

		if (useInputSig) {
			assertFalse(signIdentity.verifySignature(otherMsg, sigInput));
			return signIdentity.verifySignature(msg, sigInput);
		} else {
			assertFalse(signIdentity.verifySignature(otherMsg, sig));
			return signIdentity.verifySignature(msg, sig);
		}
	}

	/**
	 * Helper function to create IdemixSigningIdentity from a file generated by idemixgen go tool
	 *
	 * @param mspId
	 * @return IdemixSigningIdentity object
	 * @throws IOException
	 * @throws InvalidProtocolBufferException
	 */
	private IdemixSigningIdentity createIdemixSigningIdentity(String mspId) throws CryptoException,
			InvalidArgumentException, IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		MspConfigPackage.IdemixMSPSignerConfig signerConfig = null;
		signerConfig = readIdemixMSPConfig(TEST_PATH + mspId + USER_PATH, SIGNER_CONFIG);
		assertNotNull(signerConfig);

		Idemix.IssuerPublicKey ipkProto = readIdemixIssuerPublicKey(TEST_PATH + mspId + VERIFIER_PATH, IPK_CONFIG);
		IdemixIssuerPublicKey ipk = new IdemixIssuerPublicKey(ipkProto);
		assertTrue(ipk.check());

		PublicKey revPk = readIdemixRevocationPublicKey(TEST_PATH + mspId + VERIFIER_PATH, REVOCATION_PUBLIC_KEY);

		BIG sk = BIG.fromBytes(signerConfig.getSk().toByteArray());

		Idemix.Credential credProto = Idemix.Credential.parseFrom(signerConfig.getCred());

		assertNotNull(credProto);

		IdemixCredential cred = new IdemixCredential(credProto);

		Idemix.CredentialRevocationInformation cri = Idemix.CredentialRevocationInformation
				.parseFrom(signerConfig.getCredentialRevocationInformation());

		return new IdemixSigningIdentity(ipk, revPk, mspId, sk, cred, cri,
				signerConfig.getOrganizationalUnitIdentifier(), signerConfig.getRole());
	}

	/**
	 * Helper function: parse Idemix MSP Signer config (is part of the MSPConfig proto) from path
	 *
	 * @param configPath
	 * @param id
	 * @return IdemixMSPSignerConfig proto
	 */
	public static MspConfigPackage.IdemixMSPSignerConfig readIdemixMSPConfig(String configPath, String id)
			throws IOException {

		Path path = Paths.get(configPath + id);
		byte[] data = Files.readAllBytes(path);
		MspConfigPackage.IdemixMSPSignerConfig signerConfig = MspConfigPackage.IdemixMSPSignerConfig.parseFrom(data);
		return signerConfig;
	}

	/**
	 * Parse Idemix issuer public key from the config file
	 *
	 * @param configPath
	 * @param id
	 * @return Idemix IssuerPublicKey proto
	 */
	public static Idemix.IssuerPublicKey readIdemixIssuerPublicKey(String configPath, String id) throws IOException {

		Path path = Paths.get(configPath + id);
		byte[] data = Files.readAllBytes(path);

		return Idemix.IssuerPublicKey.parseFrom(data);
	}

	/**
	 * Parse Idemix long-term revocation public key from the config file
	 *
	 * @param configPath
	 * @param id
	 * @return the long-term revocation public key
	 */
	public static PublicKey readIdemixRevocationPublicKey(String configPath, String id)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		Path path = Paths.get(configPath + id);
		byte[] data = Files.readAllBytes(path);

		String pem = new String(data, StandardCharsets.UTF_8);
		byte[] der = convertPemToDer(pem);
		return KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(der));
	}

	private static byte[] convertPemToDer(String pem) throws IOException {
		PemReader pemReader = new PemReader(new StringReader(pem));
		return pemReader.readPemObject().getContent();
	}

	/**
	 * Test for IdemixRoles bitmasking
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIdemixRoles() {
		IdemixRoles[] roles = { IdemixRoles.ADMIN, IdemixRoles.CLIENT };
		int role = IdemixRoles.getRoleMask(roles);

		assertTrue(IdemixRoles.checkRole(role, IdemixRoles.ADMIN));
		assertFalse(IdemixRoles.checkRole(role, IdemixRoles.PEER));
		assertFalse(IdemixRoles.checkRole(role, IdemixRoles.MEMBER));
		assertTrue(IdemixRoles.checkRole(role, IdemixRoles.CLIENT));

		assertEquals(IdemixRoles.getIdemixRoleFromMSPRole(MspPrincipal.MSPRole.MSPRoleType.MEMBER),
				IdemixRoles.MEMBER.getValue());
		assertEquals(IdemixRoles.getMSPRoleFromIdemixRole(IdemixRoles.ADMIN.getValue()),
				MspPrincipal.MSPRole.MSPRoleType.ADMIN);

		// Throws exception illegal argument
		IdemixRoles.getMSPRoleFromIdemixRole(100);
		IdemixRoles.getIdemixRoleFromMSPRole(-1);
	}
}