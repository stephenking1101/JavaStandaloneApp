package com.golearning.training.spr.lesson.main;

import java.util.List;
import java.util.Iterator;

import com.golearning.training.spr.lesson.business.*;
import com.golearning.training.spr.lesson.dao.entity.*;


public class Main1 {
	
	public static void main(String[] args) {
		BookSrv1 srv = new BookSrv1();
		List<Book> books = srv.findAll();
		for (Iterator<Book> it = books.iterator(); it.hasNext();) {
			Book book = it.next();
			System.out.println("Book name = " + book.getName());
		}
	}

}
