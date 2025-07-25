package cn.edu.upc.yb.integrate.deliverwater.model;

import javax.persistence.*;

/**
 * Created by 陈子枫 on 2016/9/29.
 */
@Entity
@Table(name = "deliverwater")
public class DeliverWater {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private int yibanid;
	private String yibanName;
	private boolean isdeal;
	private String blockNumber;
	private String dormitory;
	private int num;
	private int ticket;
	private long createAt;
	private long upAt;
	private long deleteAt;

	public DeliverWater(String blockNumber, String dormitory, int num) {
		this.blockNumber = blockNumber;
		this.dormitory = dormitory;
		this.num = num;
		this.createAt = System.currentTimeMillis();
	}

	public DeliverWater(String blockNumber, String dormitory) {
		this.blockNumber = blockNumber;
		this.dormitory = dormitory;

		this.createAt = System.currentTimeMillis();
	}

	public DeliverWater(int yibanid, String blockNumber, String dormitory) {
		this.yibanid = yibanid;
		this.blockNumber = blockNumber;
		this.dormitory = dormitory;

		this.createAt = System.currentTimeMillis();
	}

	public DeliverWater(int yibanid, String yibanName, String blockNumber, String dormitory, int num, int ticket) {
		this.yibanid = yibanid;
		this.yibanName = yibanName;
		this.blockNumber = blockNumber;
		this.dormitory = dormitory;
		this.num = num;
		this.ticket = ticket;
		this.createAt = System.currentTimeMillis();
	}

	public DeliverWater() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getBlockNumber() {
		return blockNumber;
	}

	public void setBlockNumber(String blockNumber) {
		this.blockNumber = blockNumber;
	}

	public String getDormitory() {
		return dormitory;
	}

	public void setDormitory(String dormitory) {
		this.dormitory = dormitory;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public int getYibanid() {
		return yibanid;
	}

	public void setYibanid(int yibanid) {
		this.yibanid = yibanid;
	}

	public boolean isdeal() {
		return isdeal;
	}

	public void setIsdeal(boolean isdeal) {
		this.isdeal = isdeal;
	}

	public long getCreateAt() {
		return this.createAt;
	}

	public int getTicket() {
		return ticket;
	}

	public void setTicket(int ticket) {
		this.ticket = ticket;
	}
}
