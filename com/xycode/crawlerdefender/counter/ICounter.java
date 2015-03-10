package com.xycode.crawlerdefender.counter;

import com.xycode.crawlerdefender.config.Config;



/**
 * 计数器，用于实时记录单位时间数内ip的访问次数<br/>
 * @author xiaoyang
 *
 */
public interface ICounter {
	
	/**
	 * 秒级别计数器
	 * @param ip
	 */
	public int secondCount(String ip);
	
	/**
	 * 分级别计数器
	 * @param ip
	 */
	public int minuteCount(String ip);
	

	public int getSecondCount(String ip);
	
	public int getMinuteCount(String ip);
	
	
	
}
