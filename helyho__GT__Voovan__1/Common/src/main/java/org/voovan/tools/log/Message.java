package org.voovan.tools.log;

import org.voovan.tools.*;
import org.voovan.tools.json.JSON;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * 日志消息对象
 * 
 * @author helyho
 *
 * Voovan Framework.
 * WebSite: https://github.com/helyho/Voovan
 * Licence: Apache v2 License
 */
public class Message {
	private final static int LOGGER_CLASS_HASH = Logger.class.getCanonicalName().hashCode();

	private String level;
	private Object message;
	private Object[] args;
	private Throwable throwable;
	private Thread thread;
	private Date timestamp;
	private long runTime;

	private Map<String, String> tokens;
	private StackTraceElement stackTraceElement;

	public Message() {
		if (LoggerStatic.HAS_STACK) {
			stackTraceElement = currentStackLine();
		}

		this.timestamp = new Date();
		this.runTime = TPerformance.getRuningTime() / 1000;
	}

	public Message(String level, Object message, Object[] args, Throwable throwable) {
		this.level = level;
		this.message = message;
		this.args = args;
		this.throwable = throwable;
		this.thread = Thread.currentThread();

		if (LoggerStatic.HAS_STACK) {
			stackTraceElement = currentStackLine();
		}

		this.timestamp = new Date();
		this.runTime = TPerformance.getRuningTime() / 1000;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public Object getMessage() {
		return message;
	}

	public void setMessage(Object message) {
		this.message = message;
	}

	public Map<String, String> getTokens() {
		return tokens;
	}

	public void setTokens(Map<String, String> tokens) {
		this.tokens = tokens;
	}

	public Thread getThread() {
		return thread;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}

	public StackTraceElement getStackTraceElement() {
		return stackTraceElement;
	}

	public void setStackTraceElement(StackTraceElement stackTraceElement) {
		this.stackTraceElement = stackTraceElement;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public long getRunTime() {
		return runTime;
	}

	public void setRunTime(long runTime) {
		this.runTime = runTime;
	}

	public String format() {
		message = TObject.nullDefault(message, "");

		if (!(message instanceof String)) {
			Function<Object, String> jsonFormat = LoggerStatic.JSON_FORMAT ? JSON::toJSONWithFormat : JSON::toJSON;
			message = jsonFormat.apply(message);
		}

		if (throwable != null) {
			//构造栈信息输出
			String stackMessage = "";
			do {
				stackMessage = stackMessage + throwable.getClass().getCanonicalName() + ": " + throwable.getMessage()
						+ TFile.getLineSeparator()
						+ TString.indent(TEnv.getStackElementsMessage(throwable.getStackTrace()), 4)
						+ TFile.getLineSeparator();
				throwable = throwable.getCause();

			} while (throwable != null);

			message = (message.toString().isEmpty() ? "" : (message + " => ")) + stackMessage;
		}

		if (args != null && args.length > 0) {

			for (int i = 0; i < args.length; i++) {
				if (!(args[i] instanceof String))
					args[i] = JSON.toJSON(args[i]);
			}

			message = TString.tokenReplace((String) message, args);
		}

		return (String) message;
	}

	/**
	 * 获得当前栈元素信息
	 * @return 栈信息元素
	 */
	public static StackTraceElement currentStackLine() {
		StackTraceElement[] stackTraceElements = TEnv.getStackElements();

		StackTraceElement stackTraceElement = null;
		for (int i = 6; i < stackTraceElements.length; i++) {
			stackTraceElement = stackTraceElements[i];
			if (stackTraceElement.getClassName().endsWith("Logger")) {
				continue;
			} else {
				break;
			}
		}

		return stackTraceElement;
	}

	public static Message newInstance(String level, Object message, Object[] args, Throwable throwable) {
		return new Message(level, message, args, throwable);
	}
}
