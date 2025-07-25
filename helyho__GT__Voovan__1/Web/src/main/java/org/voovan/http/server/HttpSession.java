package org.voovan.http.server;

import org.voovan.http.message.packet.Cookie;
import org.voovan.http.server.context.WebContext;
import org.voovan.http.server.context.WebServerConfig;
import org.voovan.network.IoSession;
import org.voovan.tools.TString;
import org.voovan.tools.collection.Attributes;
import org.voovan.tools.reflect.annotation.NotSerialization;

/**
 * WebServer session 类
 *
 * @author helyho
 *
 * Voovan Framework.
 * WebSite: https://github.com/helyho/Voovan
 * Licence: Apache v2 License
 */
public class HttpSession extends Attributes {
	private String id;
	private int maxInactiveInterval;

	@NotSerialization
	private transient SessionManager sessionManager;

	@NotSerialization
	private transient IoSession socketSession;

	/**
	 * 构造函数
	 *
	 * @param config  WEB服务配置对象
	 * @param sessionManager Session管理器
	 * @param socketSession   Socket会话对象
	 */
	public HttpSession(WebServerConfig config, SessionManager sessionManager, IoSession socketSession) {
		// ID的创建转义到 save 方法中.在保存时才创建 ID
		this.id = TString.generateId(this);
		int sessionTimeout = config.getSessionTimeout();
		if (sessionTimeout <= 0) {
			sessionTimeout = 30;
		}
		this.maxInactiveInterval = sessionTimeout * 60;
		this.sessionManager = sessionManager;
		this.socketSession = socketSession;
	}

	/**
	 * 用于从会话池中取出的会话实例化
	 * @param sessionManager Session管理器
	 * @param socketSession Socket会话对象
	 */
	public void init(SessionManager sessionManager, IoSession socketSession) {
		this.sessionManager = sessionManager;
		this.socketSession = socketSession;
	}

	/**
	 * 获取 socket 会话对象
	 * @return socket 会话对象
	 */
	public IoSession getSocketSession() {
		return socketSession;
	}

	/**
	 * 设置 socket 会话对象
	 * @param socketSession socket 会话对象
	 */
	public void setSocketSession(IoSession socketSession) {
		this.socketSession = socketSession;
	}

	/**
	 * 刷新 Session 的超时时间
	 *
	 * @return HTTP-Session 对象
	 */
	public HttpSession refresh() {
		sessionManager.getSessionContainer().setTTL(this.id, maxInactiveInterval);
		return this;
	}

	/**
	 * 获取 Session 管理器
	 * @return Session 管理器
	 */
	protected SessionManager getSessionManager() {
		return sessionManager;
	}

	/**
	 * 设置Session 管理器
	 * @param sessionManager Session 管理器
	 */
	protected void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	/**
	 * 获取 Session ID
	 *
	 * @return   Session ID
	 */
	public String getId() {
		return id;
	}

	/**
	 * 获取最大不活动时间
	 *
	 * @return 最大活动时间, 单位: 毫秒
	 */
	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	/**
	 * 设置最大不活动时间
	 *
	 * @param maxInactiveInterval 最大不活动时间, 单位: 毫秒
	 */
	public void setMaxInactiveInterval(int maxInactiveInterval) {
		this.maxInactiveInterval = maxInactiveInterval;
		setModifyed(true);
	}

	/**
	 * 保存 Session
	 */
	public void save() {
		if (isModifyed() && sessionManager != null) {
			sessionManager.saveSession(this);
			setModifyed(false);
		}
	}

	/**
	 * 释放 Session
	 */
	public void release() {
		sessionManager.removeSession(this);
	}

	/**
	 * 绑定当前 Session 到一个 http 请求响应对
	 * @param request   请求对象
	 * @param response  响应对象
	 */
	public void attach(HttpRequest request, HttpResponse response) {
		if (!this.attributes().isEmpty()) {
			Cookie sessionCookie = request.getCookie(WebContext.getSessionName());
			if (sessionCookie == null) {
				//创建 Cookie
				sessionCookie = Cookie.newInstance(request, "/", WebContext.getSessionName(), this.getId(),
						this.maxInactiveInterval * 60, true);

				//响应增加Session 对应的 Cookie
				response.cookies().add(sessionCookie);
			}
			//判断 Cookie 中的 session 和 WebServer 中的 session 是否一样, 不一样则更新成 Web 服务的 Session
			else if (!sessionCookie.getValue().equals(this.getId())) {
				sessionCookie = Cookie.newInstance(request, "/", WebContext.getSessionName(), this.getId(),
						this.maxInactiveInterval * 60, true);
				response.cookies().add(sessionCookie);
			}

			//刷新 session
			refresh();
			this.save();
		} else {
			sessionManager.removeSession(this);
		}
	}

	/**
	 * 关闭会话对象
	 */
	public void close() {
		socketSession.close();
	}
}
