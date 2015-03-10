package com.xycode.crawlerdefender.counter;



/**
 * 计数器，用于实时记录单位时间内ip的访问次数<br/>
 * @author xiaoyang
 *
 */
public interface ICounter_bak {
	
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
	
	/**
	 * 小时级别计数器
	 * @param ip
	 */
	public int hourCount(String ip);
	
	/**
	 * 天级别计数器
	 * @param ip
	 */
	public int dayCount(String ip);
	

	public int getSecondCount(String ip);
	
	public int getMinuteCount(String ip);
	
	public int getHourCount(String ip);
	
	public int getDayCount(String ip);
	
	
}
