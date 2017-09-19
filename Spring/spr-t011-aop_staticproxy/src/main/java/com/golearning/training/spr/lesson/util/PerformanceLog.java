package com.golearning.training.spr.lesson.util;

public class PerformanceLog {
	
	private String name;
	private long startTime = 0;
	
	private PerformanceLog() {}
	
	public PerformanceLog(String name) {
		this.name = name;
	}
	
	public void start() {
		startTime = System.currentTimeMillis();
		try {
			Thread.currentThread().sleep(300);
		} catch(Exception e) {}
	}
	
	public void end() {
		long endTime = System.currentTimeMillis();
		System.out.println(name + " execute in " + (endTime - startTime) + "ms");
	}

}
