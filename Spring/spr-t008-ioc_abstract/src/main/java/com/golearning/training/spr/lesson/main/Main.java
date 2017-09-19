package com.golearning.training.spr.lesson.main;

import java.util.List;
import java.util.Iterator;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import com.golearning.training.spr.lesson.business.*;
import com.golearning.training.spr.lesson.dao.entity.*;


public class Main {
	
	private static final String CONFIG_FILE = "bookcity-context.xml";
	private static final String CONFIG_FILE_ABSTRACT = "bookcity-context-abstract.xml";
	
	private static ApplicationContext getApplicationFactory(String fileName) {
		
		return new ClassPathXmlApplicationContext(fileName);
	}
	
	public static void main(String[] args) {
		
		String fileName = CONFIG_FILE;
		
		ApplicationContext ctx = getApplicationFactory(fileName);
		
		IBookSrv srv = (IBookSrv)ctx.getBean("bookSrv");
		List<Book> books = srv.findAll();
		for (Iterator<Book> it = books.iterator(); it.hasNext();) {
			Book book = it.next();
			System.out.println("Book name = " + book.getName());
		}
	}

}
