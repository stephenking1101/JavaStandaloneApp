package com.golearning.training.spr.lesson.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TraceAroundAdvice implements MethodInterceptor {
	
	private static final Log LOG = LogFactory.getLog(TraceAroundAdvice.class);

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		
		LOG.debug("Around start....");
		
		long start = System.currentTimeMillis();
		
		Object rtn = invocation.proceed();
		
		LOG.debug("Around Completed. execute time = " + (System.currentTimeMillis() - start));
		
		return rtn;
	}

}
