package com.golearning.training.spr.lesson.aop;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.annotation.*;

import com.golearning.training.spr.lesson.business.INamedService;
import com.golearning.training.spr.lesson.business.impl.NamedServiceImpl;



public class NamedServiceAdvice {

	public INamedService iNamedService;
	
}
