package com.golearning.training.spr.lesson.business;

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.golearning.training.spr.lesson.dao.IBookDAO;
import com.golearning.training.spr.lesson.dao.entity.Book;

public class NonPOJOSrv implements BeanNameAware, BeanFactoryAware,
		InitializingBean, DisposableBean, IBookSrv {
	
	private String beanName;
	private BeanFactory beanFactory;
	private IBookDAO bookDao;


	@Override
	public void destroy() throws Exception {
		System.out.println("execute destroy mehtod(from DisposableBean)");

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("execute afterPropertiesSet method");

	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
		System.out.println("execute setBeanFactory method");
	}

	@Override
	public void setBeanName(String name) {
		this.beanName = name;
		System.out.println("execute setBeanName method, beanName = " + beanName);
	}
	
	
	public IBookDAO getBookDao() {
		return bookDao;
	}

	public void setBookDao(IBookDAO bookDao) {
		System.out.println("Execute y-inject method");
		this.bookDao = bookDao;
	}
	
	public List<Book> findAll() {
		return bookDao.findAll();
	}
	
	// life cycle method
	
	public void yInit() {
		System.out.println("execute y-init method");
	}
	
	public void yDestroy() {
		System.out.println("execute y-destroy method");
	}
	


}
