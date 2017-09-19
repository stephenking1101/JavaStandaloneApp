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
	private static final String CONFIG_EXTEND_FILE = "bookcity-context_extend.xml";
	
	private static BeanFactory getBeanFactory(String fileName) {
		
		return new XmlBeanFactory(new ClassPathResource(fileName));
	}
	
	private static ApplicationContext getApplicationFactory(String fileName) {
		
		return new ClassPathXmlApplicationContext(fileName);
	}
	
	public static void main(String[] args) {
		
		String fileName = CONFIG_EXTEND_FILE;
		
		//BeanFactory ctx = getBeanFactory(fileName);
		ApplicationContext ctx = getApplicationFactory(fileName);
		
		BookSrv srv = (BookSrv)ctx.getBean("bookSrv");
		List<Book> books = srv.findAll();
		for (Iterator<Book> it = books.iterator(); it.hasNext();) {
			Book book = it.next();
			System.out.println("Book name = " + book.getName());
		}
	}

}
