package com.golearning.training.spr.lesson.dao.impl.jdbc;

import java.util.ArrayList;
import java.util.List;

import com.golearning.training.spr.lesson.dao.IBookDAO;
import com.golearning.training.spr.lesson.dao.entity.Book;


public class BookDAOJDBCImpl implements IBookDAO {
	
	private static final List<Book> books = new ArrayList<Book>();
	
	public BookDAOJDBCImpl() {
		for(int i = 0; i < 5; i++) {
			Book book = new Book();
			book.setName("JDBC" + i);
			book.setIsbn(Integer.toString(i));
			books.add(book);
		}
	}

	public List<Book> findAll() {
		return books;
	}

}
