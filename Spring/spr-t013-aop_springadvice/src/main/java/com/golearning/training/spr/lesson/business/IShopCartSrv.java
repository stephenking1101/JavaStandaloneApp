package com.golearning.training.spr.lesson.business;

import java.util.List;

import com.golearning.training.spr.lesson.dao.entity.Book;

public interface IShopCartSrv {
	
	public void buy(List<Book> books);

}
