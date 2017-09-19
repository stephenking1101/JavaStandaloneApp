package com.golearning.training.spr.lesson.iocextend;

import java.lang.reflect.Method;
import com.golearning.training.spr.lesson.dao.IBookDAO;
import org.springframework.beans.factory.support.MethodReplacer;

public class FindMethodReplacer implements MethodReplacer {
	
	private IBookDAO bookDao;
	
	public void setBookDao(IBookDAO bookDao) {
		this.bookDao = bookDao;
	}

	@Override
	public Object reimplement(Object o, Method method, Object[] args)
			throws Throwable {
		Object rtn = null;
		
		long start = System.currentTimeMillis();
		System.out.println("start...method = " + method.getName() + ", class = " + o.getClass().getName());
		
		//rtn = method.invoke(bookDao, args);
		
		System.out.println("end.. time = " + (System.currentTimeMillis() - start));
		
		return rtn;
	}

}
