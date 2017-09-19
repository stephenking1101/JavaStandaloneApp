package com.golearning.training.spr.lesson.aop;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.AfterReturningAdvice;

public class TraceAfterReturningAdvice implements AfterReturningAdvice {
	
	private static final Log LOG = LogFactory.getLog(TraceAfterReturningAdvice.class);
	

	@Override
	public void afterReturning(Object target, Method method, Object[] args,
			Object arg) throws Throwable {

		LOG.debug("[afterReturning method] execute...");
	}

}
