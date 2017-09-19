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

public class ConstructorSrv implements IBookSrv {
	
	private IBookDAO bookDao;
	private int count;

	public ConstructorSrv(IBookDAO bookDao, int count) {
		this.bookDao = bookDao;
		this.count = count;
	}
	
	public List<Book> findAll() {
		System.out.println("execute findAll method, count = " + count);
		return bookDao.findAll();
	}
	
}
