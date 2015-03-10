package com.xycode.crawlerdefender.counter.simple;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.xycode.crawlerdefender.counter.ICounter_bak;

/**
 * 简单计数器<br/>
 * <p>秒、分级别的计数写入内存</P>
 * <p>小时、天级别的计数写入持久化容器（避免内存占用过多）</P>
 * @author xiaoyang
 */
public class SimpleCounter_bak implements ICounter_bak{
	
	private static SimpleCounter_bak instance=new SimpleCounter_bak();
	
	private ConcurrentMap<String, AtomicInteger> map=new ConcurrentHashMap<String, AtomicInteger>();
	private DelayQueue<DelayItem<Pair>> queue=new DelayQueue<DelayItem<Pair>>();
	
	private Thread daemonThread;
	
	private SimpleCounter_bak(){
		
		Runnable daemonTask = new Runnable() {
			public void run() {
				daemonCheck();
			}
		};

		daemonThread = new Thread(daemonTask);
		daemonThread.setDaemon(true);
		daemonThread.setName("Visit Counter Daemon Thread");
		daemonThread.start();
	}
	
	public static SimpleCounter_bak getInstance() {
		return instance;
	}

	//清除过期计数器
	private void daemonCheck(){
		for (;;) {
			try {
				DelayItem<Pair> delayItem = queue.take();
				if (delayItem != null) {
					// 超时对象处理
					Pair pair = delayItem.getItem();
					map.remove(pair.getIp()); 
				}
			} catch (InterruptedException e) {
				break;
			}
		}
	}
	

	@Override
	public int secondCount(String ip) {
		return increment(ip,TimeUnit.SECONDS);
	}

	@Override
	public int minuteCount(String ip) {
		return increment(ip,TimeUnit.MINUTES);
	}

	@Override
	public int hourCount(String ip) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int dayCount(String ip) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int getSecondCount(String ip) {
		return getCount(ip, TimeUnit.SECONDS);
	}

	@Override
	public int getMinuteCount(String ip) {
		return getCount(ip, TimeUnit.MINUTES);
	}

	@Override
	public int getHourCount(String ip) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDayCount(String ip) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private int getCount (String ip,TimeUnit timeUnit){
		
		String key=ip+"_"+timeUnit.name();
		
		AtomicInteger count= map.get(key);
		if(count!=null){
			return count.get();
		}
		
		return 0;
	}
	
	private int increment(String ip,TimeUnit timeUnit){
		
		String key=ip+"_"+timeUnit.name();
		
		AtomicInteger count= map.get(key);
		
		if(count==null){
			count=new AtomicInteger(1);
			map.put(key, count);
			
			queue.add(new DelayItem<Pair>(new Pair(key,count), TimeUnit.NANOSECONDS.convert(1,timeUnit)));
			
		}else{
			count.incrementAndGet();
		}
		
		return count.get();
	}
	
	public static void main(String[] args) {
		
		SimpleCounter_bak couter=SimpleCounter_bak.getInstance();
		//couter.increment("192.168.1.101",TimeUnit.SECONDS);
		//couter.increment("192.168.1.101",TimeUnit.SECONDS);
		//couter.increment("192.168.1.101",TimeUnit.SECONDS);
		
		couter.secondCount("192.168.1.101");
		couter.secondCount("192.168.1.101");
		couter.secondCount("192.168.1.101");

		
		System.out.println(couter.getSecondCount("192.168.1.101"));
		
		try {
			Thread.sleep(1010);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println(couter.getCount("192.168.1.101",TimeUnit.SECONDS));

	}

	
}
