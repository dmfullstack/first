package com.cyan.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.cyan.util.JwtUtil;

import io.jsonwebtoken.Claims;

public class AuthenticationFilter implements Filter {
	
	private static Logger logger = LogManager.getLogger();

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		AuthenticationEnum authenticationEnum=checkJwtAuthorize(request);
		if(!authenticationEnum.equals(AuthenticationEnum.PERMISSION_OK)){
			 HttpServletResponse httpResponse = (HttpServletResponse) response;  
	            httpResponse.setCharacterEncoding("UTF-8");    
	            httpResponse.setContentType("application/json; charset=utf-8");   
	            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED); 
	            JSONObject jobj=new JSONObject(true);
	            jobj.put("errorMsg", "Failed to authenticate");
	            httpResponse.getWriter().write(jobj.toJSONString());;  
	            return;  
		}else{
			chain.doFilter(request, response);
		}
	}
	
	private AuthenticationEnum checkJwtAuthorize(ServletRequest request){
		try {
			 HttpServletRequest httpRequest = (HttpServletRequest)request;  
	         String jwt = httpRequest.getHeader("Authorization");
	         //if(httpRequest.getRequestURI().equals("/getToken")){
				 System.out.println("/////////Branch: Master   /// REQUESTUI: "+httpRequest.getRequestURI().toString());
			 if(httpRequest.getRequestURI().endsWith("/getToken")){	 
	        	 return AuthenticationEnum.PERMISSION_OK;
	         }
	         Claims claims =JwtUtil.parseJWT(jwt);
	         //do other Authorize
	         logger.debug("claims:{}",claims);
		} catch (Exception e) {
			logger.debug(e.getMessage(),e);
			return AuthenticationEnum.PERMISSION_DENIED;
		}
		return AuthenticationEnum.PERMISSION_OK;
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		logger.debug("==============Init filter");
	}

}
