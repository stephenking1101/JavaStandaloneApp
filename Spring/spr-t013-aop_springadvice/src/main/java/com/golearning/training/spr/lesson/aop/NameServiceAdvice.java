package com.golearning.training.spr.lesson.aop;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.IntroductionInterceptor;

import com.golearning.training.spr.lesson.business.INamedService;

public class NameServiceAdvice implements IntroductionInterceptor,
		INamedService {
	
	private static final Log LOG = LogFactory.getLog(NameServiceAdvice.class);
	
	private String name;

	@Override
	public Object invoke(MethodInvocation m) throws Throwable {
		Object rtn = null;
		
		if (implementsInterface(m.getMethod().getDeclaringClass())) {
			rtn = m.getMethod().invoke(this, m.getArguments());
		} else {
			rtn = m.proceed();
		}
		
		return rtn;
	}

	@Override
	public boolean implementsInterface(Class clz) {
		return clz.isAssignableFrom(INamedService.class);
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		this.name = "[Register Service]" + name;
		LOG.debug("setName = " + name);
	}

}
