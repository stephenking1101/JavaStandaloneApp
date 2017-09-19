package com.golearning.training.spr.lesson.factory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.digester.Digester;

public class Configuration {
	
	private static final Configuration instance = new Configuration();
	private static final String BEANS_FILE = "beans.xml";
	private static final List<Bean> beans = new ArrayList<Bean>();
	
	private Configuration() {
	}
	
	static {
		Digester digester = new Digester();
		digester.push(instance);
		
		digester.addObjectCreate("beans/bean", Bean.class);
		digester.addSetProperties("beans/bean", "id", "id");
		digester.addSetProperties("beans/bean", "class", "clzName");
		digester.addSetNext("beans/bean", "addBean");
		
		digester.addObjectCreate("beans/bean/prop", Prop.class);
		digester.addSetProperties("beans/bean/prop", "name", "name");
		digester.addSetProperties("beans/bean/prop", "value", "value");
		digester.addSetNext("beans/bean/prop", "addProp");
		
		try {
			digester.parse(instance.getClass().getClassLoader().getResourceAsStream(BEANS_FILE));
			for(Iterator<Bean> it = instance.getBeans().iterator(); it.hasNext();) {
				Bean bean = it.next();
				for(Iterator<Prop> itp = bean.getProps().iterator(); itp.hasNext();) {
					Prop prop = itp.next();
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	
	public void addBean(Bean bean) {
		beans.add(bean);
	}
	
	public static List<Bean> getBeans() {
		return beans;
	}
	

}
