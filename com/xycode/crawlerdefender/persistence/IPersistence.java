package com.xycode.crawlerdefender.persistence;

import com.xycode.crawlerdefender.logcache.LogCachePool;


/**
 * 持久化容器，可以是db、filesystem等<br/>
 * <p>职责：</p>
 * 1.负责日志的持久化<br/>
 * 2.负责小时、天级别的访问计数
 * @author xiaoyang
 *
 */
public interface IPersistence {
	
	/**
	 * 初始化
	 */
	public abstract void init()  ;
	
	/**
	 * 销毁
	 */
	public abstract void destroy()  ;
	
	/**
	 * 将缓存中的日志写入持久化容器
	 */
	public abstract void cacheToPersistence(LogCachePool cachePool) ; 
	
	/**
	 * 写访问日志
	 * @param log
	 */
	public abstract void insertVisitLog(LogObject log) ;
	
	
	/**
	 * 获取最近1小时内指定ip的访问次数
	 */
	public abstract int getHourVisitCount(String ip);

	
	/**
	 * 获取最近1天内指定ip的访问次数
	 */
	public abstract int getDayVisitConut(String ip);
	
}
