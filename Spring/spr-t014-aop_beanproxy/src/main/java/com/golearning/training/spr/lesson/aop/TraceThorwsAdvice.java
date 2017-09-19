package com.golearning.training.spr.lesson.aop;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.ThrowsAdvice;

public class TraceThorwsAdvice implements ThrowsAdvice {
	
	private static final Log LOG = LogFactory.getLog(TraceThorwsAdvice.class);
	
	public void afterThrowing(NullPointerException e) {
		LOG.error("Throw Null PointerException", e);
	}

}
