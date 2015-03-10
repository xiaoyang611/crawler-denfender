package com.xycode.crawlerdefender.counter.simple;

import java.util.concurrent.atomic.AtomicInteger;

public class Pair {
	
	private String ip;
	private AtomicInteger count;
	
	public Pair(String ip, AtomicInteger count) {
		super();
		this.ip = ip;
		this.count = count;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public AtomicInteger getCount() {
		return count;
	}
	public void setCount(AtomicInteger count) {
		this.count = count;
	}
	
	
	
	
}
