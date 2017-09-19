package com.golearning.training.spr.lesson.aop;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import com.golearning.training.spr.lesson.dao.entity.Book;

public class TraceAdvice {
	
	private static final Log LOG = LogFactory.getLog(TraceAdvice.class);
	
	
	public void execute(List<Book> books) {
		LOG.debug("Execute before method...." + books);
	}
	
	public void after(List<Book> books) {
		LOG.debug("Execute after method...." + books);
	}
	
	public void handleThrowing(NullPointerException e) {
		LOG.error("Execute handleThrowing method, ", e);
	}
	
	public Object aroundInterceptor(ProceedingJoinPoint invocation) throws Throwable {
		LOG.debug("Around before..");
		
		Object o = invocation.proceed();
		
		LOG.debug("Around after..");
		return o;
	}

}
