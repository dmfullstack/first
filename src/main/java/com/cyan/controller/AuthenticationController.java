package com.cyan.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.cyan.util.JwtUtil;

@RestController
public class AuthenticationController {

	@RequestMapping("getToken")
	public String getJWTAuthToken() {
		String jwtToken = JwtUtil.createJWT("id", "jwtDemo", "Test", 100000L);
		
		JSONObject ret = new JSONObject(true);
		ret.put("jwt", jwtToken);
		return ret.toJSONString();
	}
}
