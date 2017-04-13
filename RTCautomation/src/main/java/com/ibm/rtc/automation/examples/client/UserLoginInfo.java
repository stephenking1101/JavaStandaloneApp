package com.ibm.rtc.automation.examples.client;

import com.ibm.team.repository.client.ITeamRepository.ILoginHandler.ILoginInfo;

public class UserLoginInfo implements ILoginInfo{
	private String userId;
	private String password;
	
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserId() {
		return userId;
	}

	public String getPassword() {
		return password;
	}



}
