package com.golearning.training.spr.lesson.dao;

import java.util.List;

import com.golearning.training.spr.lesson.dao.entity.Book;

public interface IBookDAO {
	
	public List<Book> findAll();
	
}
