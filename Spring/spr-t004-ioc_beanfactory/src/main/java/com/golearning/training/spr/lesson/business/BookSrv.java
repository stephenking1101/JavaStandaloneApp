package com.golearning.training.spr.lesson.business;

import java.util.List;

import com.golearning.training.spr.lesson.dao.IBookDAO;
import com.golearning.training.spr.lesson.dao.entity.Book;

public class BookSrv {
	
	private IBookDAO bookDao;
	

	public IBookDAO getBookDao() {
		return bookDao;
	}

	public void setBookDao(IBookDAO bookDao) {
		this.bookDao = bookDao;
	}

	public List<Book> findAll() {
		return bookDao.findAll();
	}	

}
