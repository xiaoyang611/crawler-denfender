package com.xycode.crawlerdefender.logcache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.xycode.crawlerdefender.persistence.LogObject;
import com.xycode.crawlerdefender.persistence.mongo.MongoPersistence;

/**
 * 日志对象缓存池
 * @author xiaoyang
 */
public class LogCachePool_bak {
	
	public static final int FIFO_POLICY=1;//先入先出策略
	public static final int LRU_POLICY=2;//最近最少使用(Least Recently Used)
	private static final int DEFAULT_SIZE=100;//缓存池默认大小
	
	private Map<String,LogObject> cacheOjects;
	
	public LogCachePool_bak(){
		this(DEFAULT_SIZE,FIFO_POLICY);
	}
	
	public LogCachePool_bak(final int size,final int policy){
		
		switch(policy){
			case  FIFO_POLICY:
				
				cacheOjects=new LinkedHashMap<String,LogObject>(size){

					private static final long serialVersionUID = 1L;

					@Override
					protected boolean removeEldestEntry(Entry<String,LogObject> eldest) {
						if(size()>size) logPersistence(eldest.getValue());
						return size()>size;
					}
					
				};
				break;
			case LRU_POLICY:
				
				cacheOjects=new LinkedHashMap<String,LogObject>(size,0.75f,true){

					private static final long serialVersionUID = 1L;

					@Override
					protected boolean removeEldestEntry(Entry<String,LogObject> eldest) {
						if(size()>size) logPersistence(eldest.getValue());
						return size()>size;
					}
					
				};
				break;
		}
		
		
	}
	
	
	public void put(String key,LogObject value){
		this.cacheOjects.put(key, value);
	}
	
	public LogObject get(String key){
		return this.cacheOjects.get(key);
	}
	
	public LogObject remove(String key){
		return this.cacheOjects.remove(key);
	}
	
	public void clear(){
		this.cacheOjects.clear();
	}
	
	public Set<String> getKeySet(){
		return this.cacheOjects.keySet();
	}
	
	public boolean isEmpty(){
		return cacheOjects.isEmpty();
	}
	
	
	private void logPersistence(LogObject log){
		MongoPersistence.getInstance().insertVisitLog(log);
	}
	
	
}
