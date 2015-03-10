package com.xycode.crawlerdefender.persistence.mongo.dbobject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.mongodb.BasicDBObject;
import com.xycode.crawlerdefender.persistence.LogObject;

public class LogDBObject extends BasicDBObject {
	
	private static final long serialVersionUID = -7989410499925554880L;

	public LogDBObject(LogObject log){
		
		this.append("url", log.getUrl())
		.append("ip", log.getIp() )
		.append("user_agent", log.getUserAgent())
		.append("time", log.getTime())
		.append("type", log.getType())//0:从filter中记录下，1：从servlet中记录下
		;
		
	}
	
	protected Date getFormatedTime(Date time,String timePattern,String replace){
		
		SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
	    String timeStr = sf.format(time);
	    
	    Date timestamp;
	    try {
	    	String timestempStr= timeStr.replaceFirst(timePattern, replace);
	    	timestamp=sf.parse(timestempStr);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	    
	    return timestamp;
	}
}
