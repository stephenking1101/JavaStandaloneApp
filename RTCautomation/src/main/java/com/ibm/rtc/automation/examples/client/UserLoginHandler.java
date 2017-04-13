package com.ibm.rtc.automation.examples.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jconfig.Configuration;

import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler;

public class UserLoginHandler implements ILoginHandler {
	private Configuration configuration;
	private static Log logger = LogFactory.getLog(UserLoginHandler.class);

	public UserLoginHandler(Configuration conf) {
		this.configuration = conf;
	}
	
	public ILoginInfo challenge(ITeamRepository repository) {
    	UserLoginInfo uli = new UserLoginInfo();
    	uli.setUserId(configuration.getProperty("userId",null,"LoginInfo"));
    	if(configuration.getProperty("userId",null,"LoginInfo").equals("bldfge")) 
    		uli.setPassword("69kWLxv");
    	else
    		uli.setPassword(configuration.getProperty("password",null,"LoginInfo"));
    	logger.debug("userId: " + uli.getUserId());
		return uli;
	}

}
