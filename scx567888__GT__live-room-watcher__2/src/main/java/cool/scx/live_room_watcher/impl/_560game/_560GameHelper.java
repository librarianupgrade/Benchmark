package cool.scx.live_room_watcher.impl._560game;

import cool.scx.util.URIBuilder;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cool.scx.constant.ScxDateTimeFormatter.yyyy_MM_dd;
import static cool.scx.util.HashUtils.md5Hex;
import static cool.scx.util.HashUtils.sha256Hex;

public class _560GameHelper {

	private static final HashedWheelTimer HASHED_WHEEL_TIMER = new HashedWheelTimer(Thread.ofVirtual().factory());

	public static Timeout setTimeout(Runnable task, long delay) {
		return HASHED_WHEEL_TIMER.newTimeout((v) -> {
			task.run();
		}, delay, TimeUnit.MILLISECONDS);
	}

	public static String getWsUrl(String baseUrl, String roomid) {
		String data = yyyy_MM_dd.format(LocalDate.now()) + ":" + roomid;
		URIBuilder uriBuilder = URIBuilder.of(baseUrl).addParam("client_token", sha256Hex(data).toLowerCase())
				.addParam("roomId", roomid);
		return uriBuilder.toString();
	}

	public static String getSign(Map<String, String> map, String secret) {
		String urlParams = map.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.map(k -> k.getKey() + "=" + k.getValue()).collect(Collectors.joining("&"));
		String s = urlParams + "&secret=" + secret;
		return md5Hex(s).toUpperCase();
	}

}
