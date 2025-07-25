package com.paritytrading.parity.match;

import java.util.ArrayList;

class Level {

	private Orders parent;

	private long price;

	private ArrayList<Order> orders;

	private ArrayList<Order> toDelete;

	public Level(Orders parent, long price) {
		this.parent = parent;
		this.price = price;
		this.orders = new ArrayList<>();
		this.toDelete = new ArrayList<>();
	}

	public long getPrice() {
		return price;
	}

	public Order add(long orderId, long size) {
		Order order = new Order(this, orderId, size);

		orders.add(order);

		return order;
	}

	public long match(long orderId, Side side, long quantity, MarketListener listener) {
		for (int i = 0; quantity > 0 && i < orders.size(); i++) {
			Order order = orders.get(i);

			long orderQuantity = order.getRemainingQuantity();

			if (orderQuantity > quantity) {
				order.reduce(quantity);

				listener.match(order.getId(), orderId, side, price, quantity, order.getRemainingQuantity());

				quantity = 0;
			} else {
				toDelete.add(order);

				listener.match(order.getId(), orderId, side, price, orderQuantity, 0);

				quantity -= orderQuantity;
			}
		}

		if (!toDelete.isEmpty()) {
			for (int i = 0; i < toDelete.size(); i++)
				toDelete.get(i).delete();

			toDelete.clear();
		}

		return quantity;
	}

	public void delete(Order order) {
		orders.remove(order);

		if (orders.isEmpty())
			parent.delete(this);
	}

}
