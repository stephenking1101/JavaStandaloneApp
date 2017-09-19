package com.golearning.training.spr.lesson.factory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

public class BeanFactory {
	
	private static final Map<String, Bean> beanMap = new HashMap<String, Bean>();
	private static final Map<String, Class> classMap = new HashMap<String, Class>();
	
	static {
		List<Bean> beans = Configuration.getBeans();
		for(Iterator<Bean> it = beans.iterator(); it.hasNext();) {
			Bean bean = it.next();
			beanMap.put(bean.getId(), bean);
		}
	}
	
	public static Object getBean(String id) {
		Object o = null;
		Bean bean = beanMap.get(id);
		if (bean != null) {
			try {
				Class clz = getClz(id);
				o = clz.newInstance();
				for(Prop prop: bean.getProps()) {
					String name = prop.getName();
					name = genMethodName(name);
					//Method method = clz.getMethod(name, getClz(prop.getValue()).getInterfaces()[0]);
					Method method = getMethod(clz, name);
					Object param = getBean(prop.getValue());
					if (prop.getFactoryMethod() != null) {
						Method factoryMethod = param.getClass().getMethod(prop.getFactoryMethod(), null);
						param = factoryMethod.invoke(param, null);
					}
					method.invoke(o, param);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return o;
	}
	
	public static Class getClz(String id) {
		Class clz = classMap.get(id);
		if (clz == null) {
			Bean b = beanMap.get(id);
			try {
				clz = Class.forName(b.getClzName());
				classMap.put(id, clz);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return clz;
	}
	
	private static Method getMethod(Class clz, String name) {
		Method method = null;
		Method[] methods = clz.getMethods();
		for(Method m: methods) {
			if (m.getName().equals(name)) {
				method = m;
				break;
			}
		}
		return method;
	}
	
	private static String genMethodName(String name) {
		StringBuffer buf = new StringBuffer("set").append(name);
		String f = name.substring(0, 1);
		buf.replace(3, 4, f.toUpperCase());
		return buf.toString();
	}
	
}
