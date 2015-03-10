package com.xycode.crawlerdefender.logcache;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.xycode.crawlerdefender.persistence.IPersistence;
import com.xycode.crawlerdefender.persistence.mongo.MongoPersistence;

/**
 * 日志缓存管理类
 * @author xiaoyang
 */
public class CacheManagement {
	
	private static LogCachePool cachePool=new LogCachePool(); 
	
	private static IPersistence pc=MongoPersistence.getInstance();
	
	private static ScheduledExecutorService ses=Executors.newScheduledThreadPool(1);
	
	static{
		
		//定时将缓存的日志输出到持久化容器
		ses.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				
				try{
					pc.cacheToPersistence(cachePool);
				}catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}, 0,1, TimeUnit.SECONDS);
		
		
		
	}
	
	public static LogCachePool getLogCachePool(){
		return cachePool;
	}
	
}
