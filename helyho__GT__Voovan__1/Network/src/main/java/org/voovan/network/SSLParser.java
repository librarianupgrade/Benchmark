package org.voovan.network;

import org.voovan.tools.buffer.ByteBufferChannel;
import org.voovan.tools.buffer.TByteBuffer;
import org.voovan.tools.exception.MemoryReleasedException;
import org.voovan.tools.log.Logger;

import javax.net.ssl.*;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * SSL 解析器
 * 1.握手信息
 * 2.报文信息
 *
 * @author helyho
 * <p>
 * Voovan Framework.
 * WebSite: https://github.com/helyho/Voovan
 * Licence: Apache v2 License
 */
public class SSLParser {
	private SSLEngine engine;
	private ByteBuffer appData;
	private ByteBuffer netData;
	private IoSession session;
	boolean handShakeDone = false;
	private ByteBufferChannel sslByteBufferChannel;

	/**
	 * 构造函数
	 *
	 * @param engine  SSLEngine对象
	 * @param session session 对象
	 */
	public SSLParser(SSLEngine engine, IoSession session) {
		this.engine = engine;
		this.session = session;
		this.appData = buildAppBuffer();
		this.netData = buildNetBuffer();
		sslByteBufferChannel = new ByteBufferChannel(session.socketContext().getReadBufferSize());
	}

	public ByteBufferChannel getSSlByteBufferChannel() {
		return sslByteBufferChannel;
	}

	/**
	 * 判断握手是否完成
	 *
	 * @return 握手是否完成
	 */
	public boolean isHandShakeDone() {
		return handShakeDone;
	}

	/**
	 * 获取 SSLEngine
	 *
	 * @return SSLEngine 对象
	 */
	public SSLEngine getSSLEngine() {
		return engine;
	}

	public ByteBuffer buildNetBuffer() {
		SSLSession sslSession = engine.getSession();
		int newBufferMax = sslSession.getPacketBufferSize();
		return TByteBuffer.allocateDirect(newBufferMax);
	}

	public ByteBuffer buildAppBuffer() {
		SSLSession sslSession = engine.getSession();
		int newBufferMax = sslSession.getPacketBufferSize();
		return TByteBuffer.allocateDirect(newBufferMax);
	}

	/**
	 * 清理缓冲区
	 */
	private void clearBuffer() {
		appData.clear();
		netData.clear();
	}

	/**
	 * 打包并发送数据
	 *
	 * @param buffer 需要的数据缓冲区
	 * @return 返回成功执行的最后一个或者失败的那个 SSLEnginResult
	 * @throws SSLException IO 异常
	 */
	public synchronized SSLEngineResult warp(ByteBuffer buffer) throws SSLException {
		if (session.isConnected()) {
			SSLEngineResult engineResult = null;

			do {
				synchronized (netData) {
					if (!TByteBuffer.isReleased(netData)) {
						netData.clear();
						engineResult = engine.wrap(buffer, netData);

						netData.flip();
						if (session.isConnected() && engineResult.bytesProduced() > 0 && netData.limit() > 0) {
							session.sendToBuffer(netData);
						}
						netData.clear();
					} else {
						return null;
					}
				}
			} while (engineResult.getStatus() == Status.OK && buffer.hasRemaining());

			return engineResult;
		} else {
			return null;
		}
	}

	/**
	 * 处理握手 Warp;
	 *
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	private synchronized HandshakeStatus doHandShakeWarp() throws IOException {

		if (!session.isConnected()) {
			return null;
		}

		try {
			clearBuffer();
			appData.flip();
			if (warp(appData) == null) {
				return null;
			}
			//如果有 HandShake Task 则执行
			HandshakeStatus handshakeStatus = runDelegatedTasks();
			return handshakeStatus;
		} catch (SSLException e) {
			Logger.error("HandShakeWarp error:", e);
			return null;
		}
	}

	/**
	 * 解包数据
	 *
	 * @param netBuffer 接受解包数据的缓冲区
	 * @param appBuffer 接受解包后数据的缓冲区
	 * @throws SSLException SSL 异常
	 * @return SSLEngineResult 对象
	 */
	public synchronized SSLEngineResult unwarp(ByteBuffer netBuffer, ByteBuffer appBuffer) throws SSLException {
		if (session.isConnected()) {
			SSLEngineResult engineResult = null;

			if (!TByteBuffer.isReleased(appBuffer)) {
				engineResult = engine.unwrap(netBuffer, appBuffer);
			} else {
				return null;
			}

			return engineResult;
		} else {
			return null;
		}
	}

	/**
	 * 处理握手 Unwarp;
	 *
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	private synchronized HandshakeStatus doHandShakeUnwarp() throws IOException {
		HandshakeStatus handshakeStatus = null;
		SSLEngineResult engineResult = null;

		clearBuffer();

		if (sslByteBufferChannel.isReleased()) {
			throw new IOException("Socket is disconnect");
		}

		if (sslByteBufferChannel.size() > 0) {
			ByteBuffer byteBuffer = sslByteBufferChannel.getByteBuffer();

			try {

				engineResult = unwarp(byteBuffer, appData);

				if (engineResult == null) {
					return null;
				}

				switch (engineResult.getStatus()) {
				case OK: {
					return engine.getHandshakeStatus();
				}

				default: {
					Logger.error(new SSLHandshakeException("Handshake failed: " + engineResult.getStatus()));
					session.close();
					break;
				}

				}

			} finally {
				sslByteBufferChannel.compact();
			}

		}

		return handshakeStatus == null ? engine.getHandshakeStatus() : handshakeStatus;
	}

	/**
	 * 执行委派任务
	 *
	 * @throws Exception
	 */
	private synchronized HandshakeStatus runDelegatedTasks() {
		if (handShakeDone == false) {
			if (engine.getHandshakeStatus() == HandshakeStatus.NEED_TASK) {
				Runnable runnable;
				while ((runnable = engine.getDelegatedTask()) != null) {
					runnable.run();
				}
			}
			return engine.getHandshakeStatus();
		}
		return null;
	}

	/**
	 * 进行 SSL 握手
	 * @return true: 握手完成, false: 握手未完成
	 */
	public synchronized boolean doHandShake() {
		try {
			engine.beginHandshake();
			int handShakeCount = 0;
			HandshakeStatus handshakeStatus = engine.getHandshakeStatus();
			while (!handShakeDone && handShakeCount < 20) {
				handShakeCount++;
				if (handshakeStatus == null) {
					throw new SSLException("doHandShake: Socket is disconnect");
				}

				switch (handshakeStatus) {
				case NEED_TASK:
					handshakeStatus = runDelegatedTasks();
					break;
				case NEED_WRAP:
					handshakeStatus = doHandShakeWarp();
					session.flush();
					break;
				case NEED_UNWRAP:
					if (isEnoughToUnwarp()) {
						handshakeStatus = doHandShakeUnwarp();
					} else {
						//如果不可解包, 则直接返回, 等待下一次数据接收
						return false;
					}
					break;
				case FINISHED:
					handshakeStatus = engine.getHandshakeStatus();
					handShakeDone = true;
					break;
				case NOT_HANDSHAKING:
					//对于粘包数据的处理
					if (sslByteBufferChannel.size() > 0) {
						try {
							unwarpByteBufferChannel();
						} finally {
							sslByteBufferChannel.compact();
						}
					}

					handShakeDone = true;
					break;
				default:
					break;
				}
			}
		} catch (Exception e) {
			Logger.error("SSLParser.doHandShake error:", e);
		}

		return handShakeDone;
	}

	/**
	 * 读取SSL消息到缓冲区
	 *
	 * @return 接收数据大小
	 * @throws IOException IO异常
	 */
	public synchronized int unwarpByteBufferChannel() throws IOException {
		ByteBufferChannel appByteBufferChannel = session.getReadByteBufferChannel();

		if (!isEnoughToUnwarp()) {
			return 0;
		}

		int readSize = 0;

		if (session.isConnected() && sslByteBufferChannel.size() > 0) {
			SSLEngineResult engineResult = null;

			try {
				while (true) {
					appData.clear();

					ByteBuffer sslByteBuffer = sslByteBufferChannel.getByteBuffer();
					;

					try {
						engineResult = unwarp(sslByteBuffer, appData);
					} finally {
						sslByteBufferChannel.compact();
					}

					if (engineResult == null) {
						throw new SSLException("unWarpByteBufferChannel: Socket is disconnect");
					}

					if (engineResult.getStatus() == Status.OK) {
						appData.flip();
						appByteBufferChannel.writeEnd(appData);

						if (sslByteBuffer.remaining() == 0) {
							break;
						} else {
							continue;
						}
					}

					if (engineResult.getStatus() != Status.OK) {
						break;
					}
				}
			} catch (MemoryReleasedException e) {
				if (!session.isConnected()) {
					throw new SSLException("unWarpByteBufferChannel ", e);
				}
			}
		}
		return readSize;
	}

	/**
	 * 释放方法
	 */
	public void release() {
		TByteBuffer.release(netData);
		TByteBuffer.release(appData);
		sslByteBufferChannel.release();
	}

	/**
	 <pre>
	 record type (1 byte)
	 /
	 /    version (1 byte major, 1 byte minor)
	 /    /
	 /    /         length (2 bytes)
	 /    /         /
	 +----+----+----+----+----+
	 |    |    |    |    |    |
	 |    |    |    |    |    | TLS Record header
	 +----+----+----+----+----+
	
	
	 Record Type Values       dec      hex
	 -------------------------------------
	 CHANGE_CIPHER_SPEC        20     0x14
	 ALERT                     21     0x15
	 HANDSHAKE                 22     0x16
	 APPLICATION_DATA          23     0x17
	
	
	 Version Values            dec     hex
	 -------------------------------------
	 SSL 3.0                   3,0  0x0300
	 TLS 1.0                   3,1  0x0301
	 TLS 1.1                   3,2  0x0302
	 TLS 1.2                   3,3  0x0303
	
	 ref:http://blog.fourthbit.com/2014/12/23/traffic-analysis-of-an-ssl-slash-tls-session/
	 </pre>
	 */
	private boolean isEnoughToUnwarp() throws SSLException {
		ByteBuffer src = sslByteBufferChannel.getByteBuffer();
		try {
			if (src.remaining() < 5) {
				return false;
			}
			int pos = src.position();
			// TLS - Check ContentType
			int type = src.get(pos) & 0xff;
			if (type < 20 || type > 23) {
				throw new SSLException("Not SSL package");
			}
			// TLS - Check ProtocolVersion
			int majorVersion = src.get(pos + 1) & 0xff;
			int minorVersion = src.get(pos + 2) & 0xff;
			int packetLength = src.getShort(pos + 3) & 0xffff;
			if (majorVersion != 3 || minorVersion < 1) {
				// NOT TLS (i.e. SSLv2,3 or bad data)
				throw new SSLException("Not TLS protocol");
			}
			int len = packetLength + 5;
			if (src.remaining() < len) {
				return false;
			}
			return true;
		} finally {
			sslByteBufferChannel.compact();
		}
	}
}
