package cn.cerc.ui.page.qrcode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 将目前的类定义成一个websocket服务器端,
 * 注解的值将被用于监听用户连接的终端访问URL地址,客户端可以通过这个URL来连接到WebSocket服务器端
 * 
 * @author root
 *
 */
@ServerEndpoint(value = "/websocket", configurator = GetHttpSessionConfigurator.class)
public class WebSocket {

	private static final Logger log = LoggerFactory.getLogger(WebSocket.class);

	// concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
	private static Map<String, WebSocket> items = new LinkedHashMap<>();

	private String message;

	// 与某个客户端的连接会话，需要通过它来给客户端发送数据
	private Session session;

	private HttpSession httpSession;

	/**
	 * 
	 * 连接建立成功调用的方法
	 * 
	 * @param session 可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
	 * @param config  端点配置
	 */
	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
		this.session = session;
		items.put(httpSession.getId(), this);
		log.debug("有新连接加入！当前在线人数为 {}", items.size());
		log.debug("sessionId: {}", httpSession.getId());
	}

	/**
	 * 连接关闭调用的方法
	 */
	@OnClose
	public void onClose() {
		items.remove(httpSession.getId()); // 从map中删除
		log.debug("有一连接关闭！当前在线人数为 {}", items.size());
	}

	/**
	 * 收到客户端消息后调用的方法
	 * 
	 * @param message 客户端发送过来的消息
	 * @param session 可选的参数
	 */
	@OnMessage
	public void onMessage(String message, Session session) {
		log.debug("来自客户端的消息:sessionId {}, message {}", session.getId(), message);

		// 群发消息
		for (String key : items.keySet()) {
			if (!items.get(key).sendMessage(message)) {
				log.info("消息发送错误信息： {}", this.getMessage());
			}
		}
	}

	/**
	 * 发生错误时调用
	 * 
	 * @param session 当前连接
	 * @param error   错误
	 */
	@OnError
	public void onError(Session session, Throwable error) {
		items.remove(httpSession.getId());
		log.error(error.getMessage());
	}

	public boolean sendMessage(String json) {
		boolean result = true;
		try {
			this.session.getBasicRemote().sendText(json);
			log.debug("sendMessage: {}", json);
		} catch (IOException e) {
			this.message = e.getMessage();
			result = false;
		}
		return result;
	}

	public static Map<String, WebSocket> getWebSocketSet() {
		return items;
	}

	public String getMessage() {
		return message;
	}
}