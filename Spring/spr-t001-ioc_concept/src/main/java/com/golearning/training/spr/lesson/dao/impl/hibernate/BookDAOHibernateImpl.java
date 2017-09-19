package com.golearning.training.spr.lesson.dao.impl.hibernate;

import java.util.List;
import java.util.ArrayList;

import com.golearning.training.spr.lesson.dao.IBookDAO;
import com.golearning.training.spr.lesson.dao.entity.Book;


public class BookDAOHibernateImpl implements IBookDAO {
	
	private static final List<Book> books = new ArrayList<Book>();

	public BookDAOHibernateImpl() {
		for(int i = 0; i < 5; i++) {
			Book book = new Book();
			book.setName("Hibernate" + i);
			book.setIsbn(Integer.toString(i));
			books.add(book);
		}
	}
	
	public List<Book> findAll() {
		return books;
	}

}
