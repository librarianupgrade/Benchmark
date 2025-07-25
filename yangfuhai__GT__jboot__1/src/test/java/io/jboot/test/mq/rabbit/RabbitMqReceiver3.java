package io.jboot.test.mq.rabbit;

import io.jboot.Jboot;
import io.jboot.app.JbootApplication;
import io.jboot.components.mq.MessageContext;
import io.jboot.components.mq.JbootmqMessageListener;

/**
 * 开始之前 先通过通过 docker 把 rabbitmq 运行起来
 * docker run -d  -p 15672:15672 -p 5672:5672 rabbitmq:management
 */
public class RabbitMqReceiver3 {

	public static void main(String[] args) {

		//Undertow端口号配置
		JbootApplication.setBootArg("undertow.port", "8003");

		//设置 mq 的相关信息
		JbootApplication.setBootArg("jboot.mq.type", "rabbitmq");
		JbootApplication.setBootArg("jboot.mq.channel", "channel1,channel2,myChannel");

		//以下可以不用配置，是默认信息
		//        JbootApplication.setBootArg("jboot.mq.rabbitmq.username", "guest");
		//        JbootApplication.setBootArg("jboot.mq.rabbitmq.password", "guest");
		//        JbootApplication.setBootArg("jboot.mq.rabbitmq.host", "127.0.0.1");
		//        JbootApplication.setBootArg("jboot.mq.rabbitmq.port", "5672");
		//        JbootApplication.setBootArg("jboot.mq.rabbitmq.virtualHost", "");

		//非常重要，多个应用如果同时接受同一个 channel 的广播，必须配置此项，而且必须不能相同，否则广播的时候只有一个应用能够接受到
		JbootApplication.setBootArg("jboot.mq.rabbitmq.broadcastChannelPrefix", "app3");

		//启动应用程序
		JbootApplication.run(args);

		//添加监听，不指定通道，则监听所有通道
		Jboot.getMq().addMessageListener(new JbootmqMessageListener() {
			@Override
			public void onMessage(String channel, Object message, MessageContext context) {
				System.out.println("Receive msg: " + message + ", from channel: " + channel);
			}
		});

		Jboot.getMq().startListening();

		System.out.println("RabbitMqReceiver3 started.");
	}
}
