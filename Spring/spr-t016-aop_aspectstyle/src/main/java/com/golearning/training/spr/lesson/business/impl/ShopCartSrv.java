package com.golearning.training.spr.lesson.business.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.golearning.training.spr.lesson.business.IShopCartSrv;
import com.golearning.training.spr.lesson.dao.entity.Book;

public class ShopCartSrv implements IShopCartSrv {
	
	private static final Log LOG = LogFactory.getLog(ShopCartSrv.class);
	
	public void buy(List<Book> books) {
		
		LOG.debug("[buy method] start execute...");
	}
	
	public List<Book> getBooks() {
		LOG.debug("[getBooks method] start execute...");
		return new ArrayList<Book>();
	}
	
	@Override
	public boolean deleteBook(Book book) {
		LOG.debug("[deleteBook method] start execute...");
		return false;
	}

	@Override
	public void throwing() {
		throw new NullPointerException();
	}

}
