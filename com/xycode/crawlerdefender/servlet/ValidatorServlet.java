package com.xycode.crawlerdefender.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 校验servlet
 * @author xiaoyang
 */
public class ValidatorServlet extends HttpServlet {
	
	
	private static final long serialVersionUID = -222187131522796998L;
	
	private static final String PAGE="page";
	private static final String VALIDATE="validate";
	private static final String CODEGEN="codegen";
	
	char[] pageBuf;

	@Override
	public void init(ServletConfig arg0) throws ServletException {
		
		InputStream pageInStream= ValidatorServlet.class.getClassLoader().getResourceAsStream("com/xycode/crawlerdefender/servlet/validatePage.template");
		InputStreamReader isr=new InputStreamReader(pageInStream);
		try {
			
			char[] buf=new char[1024];
			int len=isr.read(buf);
			
			pageBuf=new char[len];
			
			System.arraycopy(buf, 0, pageBuf, 0, len);
			
			buf=null;
			 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
			
		HttpServletRequest request=(HttpServletRequest)arg0;
		HttpServletResponse response=(HttpServletResponse)arg1;
		
		response.setContentType("text/html;charset=utf-8");
		
		String action= request.getParameter("action");
		
		if(PAGE.equals(action)){//返回校验页面
			
			response.getWriter().write(pageBuf);
			response.getWriter().flush();
		
			return;
		}
		if(CODEGEN.equals(action)){//生成校验码
			
			// 设置响应的类型格式为图片格式
			response.setContentType("image/jpeg");
			//禁止图像缓存
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);
			ValidateCode vCode = new ValidateCode(80,30,5,15);
			HttpSession session = request.getSession();
			session.setAttribute(CODEGEN, vCode.getCode().toLowerCase());
			vCode.write(response.getOutputStream());
			
			return;
		}
		if(VALIDATE.equals(action)){//校验
			return;
		}
		
		
	}


}
