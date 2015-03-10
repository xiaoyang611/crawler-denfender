package com.xycode.crawlerdefender.blocker.simple;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xycode.crawlerdefender.blocker.AbstractBlocker;
import com.xycode.crawlerdefender.config.ConfigItem;

public class SimpleBlocker extends AbstractBlocker {

	@Override
	public boolean buildWhiteBlackListFromPersistence() {
		return false;
	}

	@Override
	public boolean block(String key, int type) {
		return false;
	}

	@Override
	public boolean blockAndValidate(HttpServletRequest request ,HttpServletResponse response) {
		
		boolean ret=false;
		
		int secondConut= super.couter.getSecondCount(request.getRemoteAddr());
		int minuteConut= super.couter.getMinuteCount(request.getRemoteAddr());
		
		for(ConfigItem.Threshold ts :config.getConfigItem().getThresholds()){
			int countsLimit=ts.getValue();
			if(ConfigItem.Threshold.SECONDS.equals(ts.getLevel().toLowerCase())){
				ret= secondConut >=countsLimit;
				if(ret) break;
			}
			if(ConfigItem.Threshold.MINUTES.equals(ts.getLevel().toLowerCase())){
				ret= minuteConut >=countsLimit;
				if(ret) break;
			}
		}
		
		//返回校验页面
		if(ret){
			
			try {
				//response.setContentType("text/html;charset=utf-8");
				//response.sendRedirect(request.getContextPath()+"/crawler-defender/validator?action=page");
				request.getRequestDispatcher("/crawler-defender/validator?action=page").forward(request, response);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ServletException e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}
	
	@Override
	protected boolean blockIpBlackList(List<String> blackList) {
		return false;
	}

	@Override
	protected boolean blockUserAgentBlackList(List<String> blackList) {
		return false;
	}

}
