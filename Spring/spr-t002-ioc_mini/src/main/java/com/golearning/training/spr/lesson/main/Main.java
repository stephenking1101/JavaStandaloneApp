package com.golearning.training.spr.lesson.main;

import java.util.List;
import java.util.Iterator;

import com.golearning.training.spr.lesson.business.*;
import com.golearning.training.spr.lesson.dao.IBookDAO;
import com.golearning.training.spr.lesson.dao.entity.*;
import com.golearning.training.spr.lesson.factory.BeanFactory;


public class Main {
	
	public static void main(String[] args) {
		
		BookSrv srv = (BookSrv)BeanFactory.getBean("bookSrv");
		List<Book> books = srv.findAll();
		for (Iterator<Book> it = books.iterator(); it.hasNext();) {
			Book book = it.next();
			System.out.println("Book name = " + book.getName());
		}
	}

}
