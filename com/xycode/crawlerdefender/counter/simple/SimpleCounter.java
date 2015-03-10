package com.xycode.crawlerdefender.counter.simple;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.xycode.crawlerdefender.config.Config;
import com.xycode.crawlerdefender.config.ConfigItem;
import com.xycode.crawlerdefender.counter.ICounter;

/**
 * 简单计数器<br/>
 * <p>秒、分级别的计数写入内存</P>
 * @author xiaoyang
 */
public class SimpleCounter implements ICounter{
	
	private Config config=Config.getInstance();
	
	private static SimpleCounter instance=new SimpleCounter();
	
	private ConcurrentMap<String, AtomicInteger> map=new ConcurrentHashMap<String, AtomicInteger>();
	private DelayQueue<DelayItem<Pair>> queue=new DelayQueue<DelayItem<Pair>>();
	
	private Thread daemonThread;
	
	private SimpleCounter(){
		
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
	
	public static SimpleCounter getInstance() {
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
		int duration=1;
		for (ConfigItem.Threshold threshold : config.getConfigItem().getThresholds()) {
			if(ConfigItem.Threshold.SECONDS.equals(threshold.getLevel())){
				duration=threshold.getDuration();
				break;
			}
		} 
		return increment(ip,TimeUnit.SECONDS,duration);
	}

	@Override
	public int minuteCount(String ip) {
		int duration=1;
		for (ConfigItem.Threshold threshold : config.getConfigItem().getThresholds()) {
			if(ConfigItem.Threshold.MINUTES.equals(threshold.getLevel())){
				duration=threshold.getDuration();
				break;
			}
		} 
		return increment(ip,TimeUnit.MINUTES,duration);
	}
	
	@Override
	public int getSecondCount(String ip) {
		return getCount(ip, TimeUnit.SECONDS);
	}

	@Override
	public int getMinuteCount(String ip) {
		return getCount(ip, TimeUnit.MINUTES);
	}

	
	private int getCount (String ip,TimeUnit timeUnit){
		
		String key=ip+"_"+timeUnit.name();
		
		AtomicInteger count= map.get(key);
		if(count!=null){
			return count.get();
		}
		
		return 0;
	}
	
	private int increment(String ip,TimeUnit timeUnit,int duration){
		
		String key=ip+"_"+timeUnit.name();
		
		AtomicInteger count= map.get(key);
		
		if(count==null){
			count=new AtomicInteger(1);
			map.put(key, count);
			
			queue.add(new DelayItem<Pair>(new Pair(key,count), TimeUnit.NANOSECONDS.convert(duration,timeUnit)));
			
		}else{
			count.incrementAndGet();
		}
		
		return count.get();
	}
	
	public static void main(String[] args) {
		
		SimpleCounter couter=SimpleCounter.getInstance();
		//couter.increment("192.168.1.101",TimeUnit.SECONDS);
		//couter.increment("192.168.1.101",TimeUnit.SECONDS);
		//couter.increment("192.168.1.101",TimeUnit.SECONDS);
		
		couter.secondCount("192.168.1.101");
		couter.secondCount("192.168.1.101");
		couter.secondCount("192.168.1.101");

		
		System.out.println(couter.getSecondCount("192.168.1.101"));
		
		try {
			Thread.sleep(3050);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println(couter.getCount("192.168.1.101",TimeUnit.SECONDS));

	}

	
}
