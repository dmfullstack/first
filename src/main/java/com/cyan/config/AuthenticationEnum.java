package com.cyan.config;

public enum AuthenticationEnum {
	PERMISSION_DENIED("0", "Authentication failure"), PERMISSION_OK("0", "Authentication success");

	public String value;
	public String msg;

	private AuthenticationEnum(String value, String msg) {
		this.value = value;
		this.msg = msg;
	}
}
