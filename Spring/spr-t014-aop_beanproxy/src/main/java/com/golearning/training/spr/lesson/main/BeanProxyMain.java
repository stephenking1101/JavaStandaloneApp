package com.golearning.training.spr.lesson.main;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.golearning.training.spr.lesson.business.BookSrv;
import com.golearning.training.spr.lesson.business.IShopCartSrv;
import com.golearning.training.spr.lesson.business.ShopCartSrv;
import com.golearning.training.spr.lesson.dao.entity.Book;

public class BeanProxyMain {

	private static final Log LOG = LogFactory.getLog(BeanProxyMain.class);
	
	public static void main(String[] args) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("bookcity-beanproxy-context.xml"); 
		IShopCartSrv srv = (IShopCartSrv)ctx.getBean("shopCartSrv");
		srv.buy(null);
		
		BookSrv bookSrv = (BookSrv)ctx.getBean("bookSrv");
		List<Book> books = bookSrv.findAll();
		for (Iterator<Book> it = books.iterator(); it.hasNext();) {
			Book book = it.next();
			System.out.println("Book name = " + book.getName());
		}
		
	}

}
