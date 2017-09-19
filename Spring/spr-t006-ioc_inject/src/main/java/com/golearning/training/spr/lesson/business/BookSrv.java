package com.golearning.training.spr.lesson.business;

import java.util.List;

import com.golearning.training.spr.lesson.dao.IBookDAO;
import com.golearning.training.spr.lesson.dao.entity.Book;

public class BookSrv implements IBookSrv {
	
	private IBookDAO bookDao;
	private int count;

	public void setCount(int count) {
		this.count = count;
	}
	
	public void setBookDao(IBookDAO bookDao) {
		this.bookDao = bookDao;
	}

	public List<Book> findAll() {
		System.out.println("execute findAll method, count = " + count);
		return bookDao.findAll();
	}

}
