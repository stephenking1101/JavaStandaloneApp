package com.golearning.training.spr.lesson.aop;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;

public class AllTraceAdvice implements MethodBeforeAdvice,
		AfterReturningAdvice, MethodInterceptor {
	
	private static final Log LOG = LogFactory.getLog(AllTraceAdvice.class);

	@Override
	public void before(Method arg0, Object[] arg1, Object arg2)
			throws Throwable {
		LOG.debug("Execute before");

	}

	@Override
	public void afterReturning(Object arg0, Method arg1, Object[] arg2,
			Object arg3) throws Throwable {
		LOG.debug("Execute after");

	}

	@Override
	public Object invoke(MethodInvocation arg0) throws Throwable {

		LOG.debug("Around before");
		
		Object rtn = arg0.proceed();
		
		LOG.debug("Around after");
		

		return rtn;
	}

}
