package com.golearning.training.spr.lesson.sproxy;

import java.util.List;

import com.golearning.training.spr.lesson.dao.IBookDAO;
import com.golearning.training.spr.lesson.dao.entity.Book;
import com.golearning.training.spr.lesson.util.PerformanceLog;

public class BookDAOProxy implements IBookDAO {

	private IBookDAO dao;
	
	public IBookDAO getDao() {
		return dao;
	}

	public void setDao(IBookDAO dao) {
		this.dao = dao;
	}

	@Override
	public List<Book> findAll() {
		
		PerformanceLog pl = new PerformanceLog("IBookDAO findAll");
		pl.start();
		
		List<Book> books = dao.findAll();
		
		pl.end();
		
		return books;
	}

}
