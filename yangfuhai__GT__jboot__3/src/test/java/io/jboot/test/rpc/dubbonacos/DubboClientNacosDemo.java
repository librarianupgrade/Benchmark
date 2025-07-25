package io.jboot.test.rpc.dubbonacos;

import io.jboot.app.JbootApplication;
import io.jboot.components.rpc.annotation.RPCInject;
import io.jboot.test.rpc.commons.BlogService;
import io.jboot.test.rpc.commons.BookService;
import io.jboot.web.controller.JbootController;
import io.jboot.web.controller.annotation.RequestMapping;

@RequestMapping("/dubbonacos")
public class DubboClientNacosDemo extends JbootController {

	public static void main(String[] args) {

		//jboot端口号配置
		JbootApplication.setBootArg("undertow.port", "9999");

		JbootApplication.setBootArg("jboot.rpc.type", "dubbo");

		// dubbo 的注册中心的协议，支持的类型有 dubbo, multicast, zookeeper, redis, consul(2.7.1), sofa(2.7.2), etcd(2.7.2), nacos(2.7.2)
		JbootApplication.setBootArg("jboot.rpc.dubbo.registry.protocol", "nacos");
		//注册中心地址，即 nacos 的地址
		JbootApplication.setBootArg("jboot.rpc.dubbo.registry.address", "127.0.0.1:8848");

		JbootApplication.run(args);
	}

	@RPCInject
	private BlogService blogService;

	@RPCInject
	private BookService bookService;

	public void index() {

		System.out.println("DubboClientNacosDemo.index()");

		System.out.println(blogService.findById());
		System.out.println(bookService.findById());

		renderText("ok");
	}
}
