package mongoose.client.operations.i18n;

import webfx.framework.shared.operation.HasOperationCode;
import webfx.framework.client.operations.i18n.ChangeLanguageRequest;
import webfx.framework.client.operations.i18n.ChangeLanguageRequestEmitterImpl;

/**
 * @author Bruno Salmon
 */
public final class ChangeLanguageToEnglishRequest extends ChangeLanguageRequest implements HasOperationCode {

	private static final String OPERATION_CODE = "ChangeLanguageToEnglish";

	public ChangeLanguageToEnglishRequest() {
		super("en");
	}

	@Override
	public Object getOperationCode() {
		return OPERATION_CODE;
	}

	public static final class ProvidedEmitter extends ChangeLanguageRequestEmitterImpl {
		public ProvidedEmitter() {
			super(ChangeLanguageToEnglishRequest::new);
		}
	}
}
