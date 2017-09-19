package com.golearning.training.spr.lesson.iocextend;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class BookCityBeanFactoryPostProcesser implements
		BeanFactoryPostProcessor {

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory factory)
			throws BeansException {
		
		BookCityPostProcessor post = new BookCityPostProcessor();
		factory.addBeanPostProcessor(post);
		
		System.out.println("bean count = " + factory.getBeanDefinitionCount());
	}

}
