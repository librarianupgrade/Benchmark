package org.json.simple.parser;

/**
 * ParseException explains why and where the error occurs in source JSON text.
 *
 * @author FangYidong<fangyidong       @       yahoo.com.cn>
 */
public class ParseException extends Exception {
	private static final long serialVersionUID = -7880698968187728548L;

	public static final int ERROR_UNEXPECTED_CHAR = 0;
	public static final int ERROR_UNEXPECTED_TOKEN = 1;
	public static final int ERROR_UNEXPECTED_EXCEPTION = 2;

	private int errorType;
	private Object unexpectedObject;
	private int position;

	public ParseException(int errorType) {
		this(-1, errorType, null);
	}

	public ParseException(int errorType, Object unexpectedObject) {
		this(-1, errorType, unexpectedObject);
	}

	public ParseException(int position, int errorType, Object unexpectedObject) {
		this.position = position;
		this.errorType = errorType;
		this.unexpectedObject = unexpectedObject;
	}

	public int getErrorType() {
		return errorType;
	}

	public void setErrorType(int errorType) {
		this.errorType = errorType;
	}

	/**
	 * @return The character position (starting with 0) of the input where the error occurs.
	 * @see org.json.simple.parser.JSONParser#getPosition()
	 */
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * @return One of the following base on the value of errorType:
	 * ERROR_UNEXPECTED_CHAR		java.lang.Character
	 * ERROR_UNEXPECTED_TOKEN		org.json.simple.parser.Yytoken
	 * ERROR_UNEXPECTED_EXCEPTION	java.lang.Exception
	 * @see org.json.simple.parser.Yytoken
	 */
	public Object getUnexpectedObject() {
		return unexpectedObject;
	}

	public void setUnexpectedObject(Object unexpectedObject) {
		this.unexpectedObject = unexpectedObject;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		switch (errorType) {
		case ERROR_UNEXPECTED_CHAR:
			sb.append("Unexpected character (").append(unexpectedObject).append(") at position ").append(position)
					.append(".");
			break;
		case ERROR_UNEXPECTED_TOKEN:
			sb.append("Unexpected token ").append(unexpectedObject).append(" at position ").append(position)
					.append(".");
			break;
		case ERROR_UNEXPECTED_EXCEPTION:
			sb.append("Unexpected exception at position ").append(position).append(": ").append(unexpectedObject);
			break;
		default:
			sb.append("Unkown error at position ").append(position).append(".");
			break;
		}
		return sb.toString();
	}
}
