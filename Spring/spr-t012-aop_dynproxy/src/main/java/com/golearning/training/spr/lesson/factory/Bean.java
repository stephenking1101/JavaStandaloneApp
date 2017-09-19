package com.golearning.training.spr.lesson.factory;

import java.util.*;

public class Bean {
	
	private String id;
	private String clzName;
	private String factory;
	private String factoryMethod;
	private List<Prop> props = new ArrayList<Prop>();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getClzName() {
		return clzName;
	}
	public void setClzName(String clzName) {
		this.clzName = clzName;
	}
	public List<Prop> getProps() {
		return props;
	}
	public void setProps(List<Prop> props) {
		this.props = props;
	}
	
	public void addProp(Prop p) {
		props.add(p);
	}
	public String getFactory() {
		return factory;
	}
	public void setFactory(String factory) {
		this.factory = factory;
	}
	public String getFactoryMethod() {
		return factoryMethod;
	}
	public void setFactoryMethod(String factoryMethod) {
		this.factoryMethod = factoryMethod;
	}
	
}
