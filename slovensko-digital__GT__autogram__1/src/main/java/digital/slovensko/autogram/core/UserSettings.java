package digital.slovensko.autogram.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.prefs.Preferences;

public class UserSettings {
	private String driver;
	private int slotIndex;
	private boolean en319132;
	private boolean plainXmlEnabled;
	private boolean signIndividually;
	private boolean correctDocumentDisplay;
	private boolean signaturesValidity;
	private boolean pdfaCompliance;
	private boolean serverEnabled;
	private boolean expiredCertsEnabled;
	private List<String> trustedList;
	private String customKeystorePath;
	private String tsaServer;
	private boolean tsaEnabled;
	private String customTsaServer;
	private boolean bulkEnabled;
	private int pdfDpi;

	public static UserSettings load() {
		var prefs = Preferences.userNodeForPackage(UserSettings.class);

		var settings = new UserSettings();
		settings.setDriver(prefs.get("DRIVER", ""));
		settings.setSlotIndex(prefs.getInt("SLOT_INDEX", -1));
		settings.setEn319132(prefs.getBoolean("EN319132", false));
		settings.setBulkEnabled(prefs.getBoolean("BULK_ENABLED", false));
		settings.setPlainXmlEnabled(prefs.getBoolean("PLAIN_XML_ENABLED", false));
		settings.setSignIndividually(prefs.getBoolean("SIGN_INDIVIDUALLY", true));
		settings.setCorrectDocumentDisplay(prefs.getBoolean("CORRECT_DOCUMENT_DISPLAY", true));
		settings.setSignaturesValidity(prefs.getBoolean("SIGNATURES_VALIDITY", true));
		settings.setPdfaCompliance(prefs.getBoolean("PDFA_COMPLIANCE", true));
		settings.setServerEnabled(prefs.getBoolean("SERVER_ENABLED", true));
		settings.setExpiredCertsEnabled(prefs.getBoolean("EXPIRED_CERTS_ENABLED", false));
		settings.setTrustedList(prefs.get("TRUSTED_LIST", "SK,CZ,AT,PL,HU,BE,NL,BG"));
		settings.setCustomKeystorePath(prefs.get("CUSTOM_KEYSTORE_PATH", ""));
		settings.setTsaServer(prefs.get("TSA_SERVER",
				"http://tsa.belgium.be/connect,http://ts.quovadisglobal.com/eu,http://tsa.sep.bg"));
		settings.setCustomTsaServer(prefs.get("CUSTOM_TSA_SERVER", ""));
		settings.setTsaEnabled(prefs.getBoolean("TSA_ENABLE", false));
		settings.setPdfDpi(prefs.getInt("PDF_DPI", 100));

		return settings;
	}

	public void setTsaServer(String str) {

	}

	public void save() {
		var prefs = Preferences.userNodeForPackage(UserSettings.class);

		prefs.put("DRIVER", driver == null ? "" : driver);
		prefs.putInt("SLOT_INDEX", slotIndex);
		prefs.putBoolean("EN319132", en319132);
		prefs.putBoolean("BULK_ENABLED", bulkEnabled);
		prefs.putBoolean("PLAIN_XML_ENABLED", plainXmlEnabled);
		prefs.putBoolean("SIGN_INDIVIDUALLY", signIndividually);
		prefs.putBoolean("CORRECT_DOCUMENT_DISPLAY", correctDocumentDisplay);
		prefs.putBoolean("SIGNATURES_VALIDITY", signaturesValidity);
		prefs.putBoolean("PDFA_COMPLIANCE", pdfaCompliance);
		prefs.putBoolean("SERVER_ENABLED", serverEnabled);
		prefs.putBoolean("EXPIRED_CERTS_ENABLED", expiredCertsEnabled);
		prefs.put("TRUSTED_LIST", String.join(",", trustedList));
		prefs.put("CUSTOM_KEYSTORE_PATH", customKeystorePath);
		prefs.put("TSA_SERVER", tsaServer);
		prefs.put("CUSTOM_TSA_SERVER", customTsaServer);
		prefs.putBoolean("TSA_ENABLE", tsaEnabled);
		prefs.putInt("PDF_DPI", pdfDpi);
	}

	private void setTrustedList(String trustedList) {
		this.trustedList = trustedList == null ? new ArrayList<>() : new ArrayList<>(List.of(trustedList.split(",")));
	}

	public String getDefaultDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public boolean isPlainXmlEnabled() {
		return plainXmlEnabled;
	}

	public void setPlainXmlEnabled(boolean value) {
		this.plainXmlEnabled = value;
	}

	public boolean isEn319132() {
		return en319132;
	}

	public void setEn319132(boolean en319132) {
		this.en319132 = en319132;
	}

	public boolean isSignIndividually() {
		return signIndividually;
	}

	public void setSignIndividually(boolean signIndividually) {
		this.signIndividually = signIndividually;
	}

	public boolean isCorrectDocumentDisplay() {
		return correctDocumentDisplay;
	}

	public void setCorrectDocumentDisplay(boolean correctDocumentDisplay) {
		this.correctDocumentDisplay = correctDocumentDisplay;
	}

	public boolean isSignaturesValidity() {
		return signaturesValidity;
	}

	public void setSignaturesValidity(boolean signaturesValidity) {
		this.signaturesValidity = signaturesValidity;
	}

	public boolean isPdfaCompliance() {
		return pdfaCompliance;
	}

	public void setPdfaCompliance(boolean pdfaCompliance) {
		this.pdfaCompliance = pdfaCompliance;
	}

	public boolean isServerEnabled() {
		return serverEnabled;
	}

	public void setServerEnabled(boolean serverEnabled) {
		this.serverEnabled = serverEnabled;
	}

	public boolean isExpiredCertsEnabled() {
		return expiredCertsEnabled;
	}

	public void setExpiredCertsEnabled(boolean expiredCertsEnabled) {
		this.expiredCertsEnabled = expiredCertsEnabled;
	}

	public List<String> getTrustedList() {
		return trustedList;
	}

	public void addToTrustedList(String country) {
		trustedList.add(country);
	}

	public void removeFromTrustedList(String country) {
		trustedList.remove(country);
	}

	public String getCustomKeystorePath() {
		return customKeystorePath;
	}

	public void setCustomKeystorePath(String value) {
		customKeystorePath = value;
	}

	public String getTsaServer() {
		return tsaServer;
	}

	public String getCustomTsaServer() {
		return customTsaServer;
	}

	public void setCustomTsaServer(String value) {
		customTsaServer = value;
	}

	public boolean getTsaEnabled() {
		return tsaEnabled;
	}

	public void setTsaEnabled(boolean value) {
		tsaEnabled = value;
	}

	public void setBulkEnabled(boolean value) {
		bulkEnabled = value;
	}

	public void setSlotIndex(int value) {
		slotIndex = value;
	}

	public boolean isBulkEnabled() {
		return bulkEnabled;
	}

	public int getPdfDpi() {
		return pdfDpi;
	}

	public void setPdfDpi(int value) {
		pdfDpi = value;
	}
}
