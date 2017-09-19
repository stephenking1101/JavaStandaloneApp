package com.golearning.training.spr.lesson.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class PerformanceLogHandler implements InvocationHandler {
	
	private Object target;
	
	public PerformanceLogHandler(Object target) {
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Object rtn = null;
		PerformanceLog log = null;;
		
		if (method.getName().indexOf("find") == 0) {
			log = new PerformanceLog(target.toString() + "." + method.getName());
			log.start();
		}

		rtn = method.invoke(target, args);
		
		if (method.getName().indexOf("find") == 0) {
			log.end();
		}
		
		return rtn;
	}

}
