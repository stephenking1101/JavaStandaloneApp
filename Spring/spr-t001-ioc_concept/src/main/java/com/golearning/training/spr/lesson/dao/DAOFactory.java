package com.golearning.training.spr.lesson.dao;

import java.io.*;
import java.util.Properties;

public class DAOFactory {
	
	private static final String PROPS_FILE = "target/classes/beans.properites";
	private static final Properties PROPS = new Properties();
	
	static {
		try {
			PROPS.load(new BufferedInputStream(new FileInputStream(PROPS_FILE)));
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public IBookDAO getBookDAO() {
		return (IBookDAO)initInstance("bookDAO");
	}
	
	private Object initInstance(String daoName) {
		Object dao = null;
		
		try {
			String className = PROPS.getProperty(daoName);
			Class clz = Class.forName(className);
			dao = clz.newInstance();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return dao;
	}

}
