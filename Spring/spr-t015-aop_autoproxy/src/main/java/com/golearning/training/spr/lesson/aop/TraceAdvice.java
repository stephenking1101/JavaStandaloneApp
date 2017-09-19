package com.golearning.training.spr.lesson.aop;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.Before;
import com.golearning.training.spr.lesson.dao.entity.Book;

@Aspect
public class TraceAdvice {
	
	private static final Log LOG = LogFactory.getLog(TraceAdvice.class);
	
	@Pointcut("execution(* buy(..))")
	private void pointcutFind() {};
	
	@Before("pointcutFind() && args(books)")
	public void before(List<Book> books) {
		LOG.debug("Execute before method...." + books);
	}
	
	@AfterReturning(pointcut="execution(* com.golearning.training.spr.lesson.business.*.get*(..))",
			returning="books")
	public void after(List<Book> books) {
		LOG.debug("Execute after method...." + books);
	}
	
	@AfterThrowing(pointcut="execution(* com.golearning.training.spr.lesson.business.*.throwing(..))",
			throwing="e")
	public void handleThrowing(NullPointerException e) {
		LOG.error("Execute handleThrowing method, ", e);
	}
	
	@Around("execution(* com.golearning.training.spr.lesson.business.*.delete*(..))")
	public Object aroundInterceptor(ProceedingJoinPoint invocation) throws Throwable {
		LOG.debug("Around before..");
		
		Object o = invocation.proceed();
		
		LOG.debug("Around after..");
		return o;
	}
	
	

}
