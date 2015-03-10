package com.xycode.crawlerdefender.persistence;

import java.util.Date;

public class LogObject {
	
	private String url;
	
	private String ip;

    private	String userAgent;
    
    private Date time;
    
    private int type;//0:从filter中记录下，1：从servlet中记录下
    
    private int count;//各维度表需要用到的统计字段
    
    public LogObject(){}

	public LogObject(String url, String ip, String userAgent, Date time,int type) {
		this.url = url;
		this.ip = ip;
		this.userAgent = userAgent;
		this.time = time;
		this.type=type;
	}
	
	public LogObject(String url, String ip, String userAgent, Date time,int type,int count) {
		this.url = url;
		this.ip = ip;
		this.userAgent = userAgent;
		this.time = time;
		this.type=type;
		this.count=count;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LogObject other = (LogObject) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "["+url+","+ip+","+userAgent+","+time+","+type+"]";
	}
	
}
