package com.golearning.training.spr.lesson.business;

import java.util.List;

import com.golearning.training.spr.lesson.dao.IBookDAO;
import com.golearning.training.spr.lesson.dao.entity.*;


public class BookSrv {
	
	private IBookDAO bookDao;
	

	public IBookDAO getBookDao() {
		return bookDao;
	}

	public void setBookDao(IBookDAO bookDao) {
		System.out.println("Execute setBookDao()");
		this.bookDao = bookDao;
	}



	public List<Book> findAll() {
		return bookDao.findAll();
	}

}
