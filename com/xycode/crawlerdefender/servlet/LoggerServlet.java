package com.xycode.crawlerdefender.servlet;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import com.mongodb.DBCollection;
import com.xycode.crawlerdefender.logcache.CacheManagement;
import com.xycode.crawlerdefender.persistence.LogObject;
import com.xycode.crawlerdefender.persistence.mongo.MongoPersistence;

/**
 * 接受filter向页面注入的ajax请求的servlet
 * @author xiaoyang
 */
public class LoggerServlet extends HttpServlet {
	
	private static final long serialVersionUID = 6382849513645000078L;
	
	DBCollection visitLog;

	@Override
	public void init(ServletConfig arg0) throws ServletException {
		visitLog= MongoPersistence.getInstance().getVisitLogCollection();
	}

	@Override
	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
			
		HttpServletRequest request=(HttpServletRequest)arg0;
		
		String userAgent=request.getHeader("User-Agent");
		String url=request.getHeader("Referer");
		
		if(url.contains("?")){//去掉参数
			url=url.substring(0, url.indexOf("?"));
		}
		
		String ip=request.getRemoteAddr();
		
		Date time=new Date();
		
		LogObject  log= new LogObject(url, ip, userAgent, time,1);
		CacheManagement.getLogCachePool().add(log);
		
	}


}
