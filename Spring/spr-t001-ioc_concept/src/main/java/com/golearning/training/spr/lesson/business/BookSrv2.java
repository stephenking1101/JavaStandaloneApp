package com.golearning.training.spr.lesson.business;

import java.util.List;

import com.golearning.training.spr.lesson.dao.*;
import com.golearning.training.spr.lesson.dao.entity.*;


public class BookSrv2 {

	private DAOFactory factory = new DAOFactory();
	private IBookDAO bookDao;
	
	public BookSrv2() {
		bookDao = factory.getBookDAO();
	}
	
	public List<Book> findAll() {
		return bookDao.findAll();
	}

}
