package com.golearning.training.spr.lesson.business;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.golearning.training.spr.lesson.dao.entity.Book;

public class ShopCartSrv implements IShopCartSrv {
	
	private static final Log LOG = LogFactory.getLog(ShopCartSrv.class);
	
	public void buy(List<Book> books) {
		
		LOG.debug("[buy method] start execute...");
		throw new NullPointerException();
	}

}
