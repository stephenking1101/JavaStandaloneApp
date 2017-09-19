package com.golearning.training.spr.lesson.main;

import java.util.Iterator;
import java.util.List;

import com.golearning.training.spr.lesson.business.BookSrv2;
import com.golearning.training.spr.lesson.dao.entity.Book;


public class Main2 {

	public static void main(String[] args) {
		BookSrv2 srv = new BookSrv2();
		List<Book> books = srv.findAll();
		for (Iterator<Book> it = books.iterator(); it.hasNext();) {
			Book book = it.next();
			System.out.println("Book name = " + book.getName());
		}
	}
	
}
