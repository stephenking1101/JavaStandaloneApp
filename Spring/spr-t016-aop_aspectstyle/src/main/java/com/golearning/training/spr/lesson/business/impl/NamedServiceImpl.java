package com.golearning.training.spr.lesson.business.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.golearning.training.spr.lesson.business.INamedService;

public class NamedServiceImpl implements INamedService {
	
	private static final Log LOG = LogFactory.getLog(NamedServiceImpl.class);
	private String name;
	
	@Override
	public String getName() {
		LOG.debug("getName = " + name);
		return name;
	}

	@Override
	public void setName(String name) {
		LOG.debug("setName = " + name);
		this.name = name;
	}
	
}
