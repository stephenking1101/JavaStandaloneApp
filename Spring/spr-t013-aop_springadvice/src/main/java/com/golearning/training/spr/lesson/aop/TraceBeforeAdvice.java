package com.golearning.training.spr.lesson.aop;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.apache.commons.logging.*;

import org.springframework.aop.MethodBeforeAdvice;;

public class TraceBeforeAdvice implements MethodBeforeAdvice {
	
	private static final Log LOG = LogFactory.getLog(TraceBeforeAdvice.class);
	
	@Override
	public void before(Method method, Object[] args, Object target) throws Throwable {
		
		LOG.debug("[before method] execute...");
	}

}
