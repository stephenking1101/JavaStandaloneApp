package com.example;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringScheduleDemo {
	 public static void main(String[] args) {
		 ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		 String scheduledAnnotationProcessor = "org.springframework.context.annotation.internalScheduledAnnotationProcessor";
		 System.out.println("Contains " + scheduledAnnotationProcessor + ": "
		         + context.containsBean(scheduledAnnotationProcessor));
		 try {
		     Thread.sleep(60000);
		 } catch (InterruptedException e) {
		     e.printStackTrace();
		 } finally {
		     context.close();
		 }
	 }
}
