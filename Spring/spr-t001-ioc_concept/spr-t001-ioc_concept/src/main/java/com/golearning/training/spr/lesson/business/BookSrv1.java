package com.golearning.training.spr.lesson.business;

import java.util.List;

import com.golearning.training.spr.lesson.dao.IBookDAO;
import com.golearning.training.spr.lesson.dao.entity.*;


public class BookSrv1 {
	
	private IBookDAO bookDao = new com.golearning.training.spr.lesson.dao.impl.hibernate.BookDAOHibernateImpl();

	//private IBookDAO bookDao = new com.golearning.training.spr.lesson.dao.impl.jdbc.BookDAOJDBCImpl();
	
	
	public List<Book> findAll() {
		return bookDao.findAll();
	}

}
