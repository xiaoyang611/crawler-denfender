package com.xycode.crawlerdefender.blocker;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xycode.crawlerdefender.config.Config;
import com.xycode.crawlerdefender.counter.ICounter;
import com.xycode.crawlerdefender.counter.simple.SimpleCounter;
import com.xycode.crawlerdefender.persistence.IPersistence;
import com.xycode.crawlerdefender.persistence.mongo.MongoPersistence;

/**
 * 拦截者<br/>
 * 职责：<br/>
 * 1：实时拦截，根据计数器获取访问次数，如果单个ip在单位时间内超过设定阀值，则拦截并返回验证页面，若超过10次未填写验证页面则将此ip放入黑名单<br/>
 * 2：拦截黑名单中的ip<br/>
 * 3：维护黑、白名单：分析访问日志，生成黑名单<br/>
 * @author xiaoyang
 */
public abstract class AbstractBlocker {
	
	protected ICounter couter;
	protected IPersistence pt;
	protected Config config;
	
	protected List<String> ipWhiteList=new ArrayList<String>();
	protected List<String> ipBlackList=new ArrayList<String>();
	protected List<String> userAgentWhiteList=new ArrayList<String>();
	protected List<String> userAgentBlackList=new ArrayList<String>();
	
	public AbstractBlocker(){
		
		//初始化
		couter=SimpleCounter.getInstance();
		pt=MongoPersistence.getInstance();
		config=Config.getInstance();
		
		//从配置文件中读取用户自定义的黑白名单
		ipWhiteList.addAll(config.getConfigItem().getIpWhiteList());
		ipBlackList.addAll(config.getConfigItem().getIpBlackList());
		userAgentWhiteList.addAll(config.getConfigItem().getUserAgentWhiteList());
		userAgentBlackList.addAll(config.getConfigItem().getUserAgentBlackList());
		
		//从持久化容器中获取程序生成的黑白名单
		
	}
	
	
	/**
	 * 分析持久化容器中的日志，构建黑白名单
	 */
	protected abstract boolean buildWhiteBlackListFromPersistence();
		
	
	/**
	 * 直接拦截单个ip或者user-agent
	 * @param key  	ip or user-agent
	 * @param type 	0：ip，1：user-agent
	 */
	protected abstract boolean block(String key,int type);
	
	/**
	 * 拦截ip黑名单
	 * @param key  ip or user-agent
	 * @param type 0：ip，1：user-agent
	 */
	protected abstract boolean blockIpBlackList(List<String> blackList);
	
	/**
	 * 拦截userAgent黑名单
	 * @param key  ip or user-agent
	 * @param type 0：ip，1：user-agent
	 */
	protected abstract boolean blockUserAgentBlackList(List<String> blackList);
	
	/**
	 * <p>根据计数器以及持久化容器拦截并返回验证页面</p>
	 * <p>计数器实时进行秒、分级别的计数</p>
	 * <p>持久化容器离线分析数据并生成小时、天级别计数</p>
	 * @return boolean true:拦截并返回校验  false:不需要拦截
	 */
	protected abstract boolean blockAndValidate(HttpServletRequest request ,HttpServletResponse response);
	
	/**
	 * @return true：表示访问ip在黑名单中需要拦截，false：表示访问ip不在黑名单中
	 */
	public boolean block(HttpServletRequest request ,HttpServletResponse resonse){
		
		boolean b1,b2,b3;
		
		if(b1=blockAndValidate(request,resonse)){
			return b1;
		}else{
			if((b2=blockIpBlackList(ipBlackList))){
				return b2;
			}else{
				if(b3=blockUserAgentBlackList(userAgentBlackList)){
					return b3;
				}
			}
		}
		
		return false;
	}
	
}
