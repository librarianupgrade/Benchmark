package webfx.platform.shared.services.buscall.spi;

import webfx.platform.shared.util.async.AsyncFunction;

/**
 * @author Bruno Salmon
 */
public class AsyncFunctionBusCallEndpoint<A, R> extends BusCallEndPointBase<A, R> {

	public AsyncFunctionBusCallEndpoint(String address, AsyncFunction<A, R> asyncFunction) {
		super(address, asyncFunction);
	}
}
