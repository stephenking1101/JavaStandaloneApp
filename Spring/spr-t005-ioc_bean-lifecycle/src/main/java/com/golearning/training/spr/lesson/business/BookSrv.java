package com.golearning.training.spr.lesson.business;

import java.util.List;

import com.golearning.training.spr.lesson.dao.IBookDAO;
import com.golearning.training.spr.lesson.dao.entity.Book;

public class BookSrv implements IBookSrv {
	
	private IBookDAO bookDao;
	

	public IBookDAO getBookDao() {
		return bookDao;
	}

	public void setBookDao(IBookDAO bookDao) {
		System.out.println("Execute x-inject method");
		this.bookDao = bookDao;
	}

	public List<Book> findAll() {
		return bookDao.findAll();
	}
	
	// life cycle method
	
	public void xInit() {
		System.out.println("execute x-init method");
	}
	
	public void xDestroy() {
		System.out.println("execute x-destroy method");
	}

}
