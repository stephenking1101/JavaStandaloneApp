package com.golearning.training.spr.lesson.main;

import java.util.List;
import java.util.Iterator;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.golearning.training.spr.lesson.business.*;
import com.golearning.training.spr.lesson.dao.entity.*;


public class Main {
	
	public static void main(String[] args) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("bookcity-context.xml");
		
		BookSrv srv = (BookSrv)ctx.getBean("bookSrv");
		List<Book> books = srv.findAll();
		for (Iterator<Book> it = books.iterator(); it.hasNext();) {
			Book book = it.next();
			System.out.println("Book name = " + book.getName());
		}
	}

}
