package com.xycode.crawlerdefender.filter;

import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xycode.crawlerdefender.blocker.AbstractBlocker;
import com.xycode.crawlerdefender.blocker.simple.SimpleBlocker;
import com.xycode.crawlerdefender.config.Config;
import com.xycode.crawlerdefender.counter.ICounter;
import com.xycode.crawlerdefender.counter.simple.SimpleCounter;
import com.xycode.crawlerdefender.logcache.CacheManagement;
import com.xycode.crawlerdefender.persistence.IPersistence;
import com.xycode.crawlerdefender.persistence.LogObject;
import com.xycode.crawlerdefender.persistence.mongo.MongoPersistence;

/**
 * 拦截需要应用反爬虫的页面的filter
 * <p>此filter的作用：</p>
 * 1.会在页面受访之前记录访问日志<br/>
 * 2.向受访页面注入ajax脚本<br/>
 * 3.阻止黑名单内ip的访问。<br/>
 * 4.向可疑的ip返回验证页面<br/>
 * 5.向单位时间内访问超过指定阀值的ip返回验证页面<br/>
 * 
 * <p>如何判定爬虫：</p>
 *  
 * 1.在发起页面访问时，浏览器会正确执行注入的ajax，但大多数爬虫程序不会执行js。
 * 因此对比filter中的访问记录与ajax发起的访问记录可辨别出一部分爬虫。<br/>
 * 2.访问超过设置的阀值，没有完成校验页面但依然在频繁访问，可以判定为爬虫。<br/>
 * 3.多次访问Bot Trap(爬虫陷阱)的也可以判定为爬虫。<br/>
 * @author xiaoyang
 */
public class GlobalFilter implements Filter {
	
	private IPersistence pc;
	
	private ICounter counter;
	
	private AbstractBlocker blocker;
	
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
		//解析configfile
		{
			String configPath= arg0.getInitParameter("configFile");
			
			if(configPath!=null && !"".endsWith(configPath)){
				Config.setConfigFilePath(configPath);
			}
			
			Config.getInstance();
		}
		
		
		//初始化持久化容器、计数器、拦截器
		try {
			
			pc=MongoPersistence.getInstance();
			
			counter=SimpleCounter.getInstance();
			
			blocker=new SimpleBlocker();
			
			pc.init();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain arg2) throws IOException, ServletException {
		
		HttpServletRequest  request=(HttpServletRequest)arg0;
		HttpServletResponse resonse=(HttpServletResponse)arg1;
		
		
		String ip=request.getRemoteAddr();//.subSequence(beginIndex, endIndex);
		
		String userAgent=request.getHeader("User-Agent");
		String url=request.getRequestURL().toString();
		
		if(url.contains("?")){//去掉参数
			url=url.substring(0, url.indexOf("?"));
		}
		
		Date time=new Date();
		
		//拦截
		if(blocker.block(request,resonse)) return;
		
		//记录filter拦截日志，先写入缓存
		{
			LogObject  log= new LogObject(url, ip, userAgent, time,0);
			CacheManagement.getLogCachePool().add(log);
		}
		
		//写计数器
		{
			counter.secondCount(ip);
			counter.minuteCount(ip);
		}
		
		
		//向受访页面输出ajax脚本
		resonse.getWriter().print("<script> (function(){ var xmlhttp= window.XMLHttpRequest  ? new XMLHttpRequest() : new ActiveXObject('Microsoft.XMLHTTP'); if (xmlhttp) { xmlhttp.open('GET', '"+request.getContextPath()+"/crawler-defender/logger', true ); xmlhttp.send( null ); } })(); </script>\r\n");
		
		arg2.doFilter(arg0, arg1);
		
	}

	@Override
	public void destroy() {
		pc.destroy();
	}

}
