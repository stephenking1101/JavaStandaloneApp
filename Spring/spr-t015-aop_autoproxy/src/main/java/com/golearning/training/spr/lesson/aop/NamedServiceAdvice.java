package com.golearning.training.spr.lesson.aop;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.annotation.*;

import com.golearning.training.spr.lesson.business.INamedService;
import com.golearning.training.spr.lesson.business.impl.NamedServiceImpl;


@Aspect
public class NamedServiceAdvice {

	@DeclareParents(value="com.golearning.training.spr.lesson.business.impl.*Srv",
			defaultImpl=NamedServiceImpl.class)
	public INamedService iNamedService;
	
}
