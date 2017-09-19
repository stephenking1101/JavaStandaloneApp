package com.golearning.training.spr.lesson.main;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.golearning.training.spr.lesson.business.IShopCartSrv;
import com.golearning.training.spr.lesson.business.ShopCartSrv;

public class ThrowMain {

	private static final Log LOG = LogFactory.getLog(ThrowMain.class);
	
	public static void main(String[] args) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("bookcity-throws-context.xml"); 
		IShopCartSrv srv = (IShopCartSrv)ctx.getBean("shopCartSrv");
		try {
			srv.buy(null);
			LOG.debug("buy Completed");
		} catch(NullPointerException e) {
			LOG.error("In ThrowMain", e);
		}
		LOG.debug("Main Completed");
	}

}
