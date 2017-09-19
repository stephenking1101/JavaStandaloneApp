package com.golearning.training.spr.lesson.proxy;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;

import com.golearning.training.spr.lesson.util.PerformanceLogHandler;

public class PerformanceLogProxyFactory {
	
	private Object target;
	
	
	
	public Object getTarget() {
		return target;
	}



	public void setTarget(Object target) {
		this.target = target;
	}



	public Object createInstance() {
		Object rtn = null;
		
		InvocationHandler handler = new PerformanceLogHandler(target);
		
		rtn = Proxy.newProxyInstance(target.getClass().getClassLoader()
				,target.getClass().getInterfaces(), handler);
		
		return rtn;
	}

}
