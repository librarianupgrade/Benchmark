package org.voovan.tools.exception;

/**
 * 内存已被释放的异常
 *
 * @author: helyho
 * Voovan Framework.
 * WebSite: https://github.com/helyho/Voovan
 * Licence: Apache v2 License
 */
public class RocksMapException extends RuntimeException {

	public RocksMapException() {
		super();
	}

	public RocksMapException(String message) {
		super(message);
	}

	public RocksMapException(String message, Throwable cause) {
		super(message, cause);
	}

	public RocksMapException(Throwable cause) {
		super(cause);
	}
}
