package com.golearning.training.spr.lesson.main;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.golearning.training.spr.lesson.business.INamedService;
import com.golearning.training.spr.lesson.business.IShopCartSrv;
import com.golearning.training.spr.lesson.business.ShopCartSrv;

public class IntroductionMain {
	
	private static final Log LOG = LogFactory.getLog(IntroductionMain.class);
	
	public static void main(String[] args) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("bookcity-introduction-context.xml"); 
		
		IShopCartSrv srv = (IShopCartSrv)ctx.getBean("shopCartSrv");
		((INamedService)srv).setName("Test");
		LOG.debug("Name[service] = " + ((INamedService)srv).getName());
		srv.buy(null);
	}

}
