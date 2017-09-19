package com.golearning.training.spr.lesson.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.golearning.training.spr.lesson.business.BookSrv;
import com.golearning.training.spr.lesson.dao.entity.Book;

/**
 * Servlet implementation class BookServlet
 */
public class BookServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public BookServlet() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		ServletContext appContext;
		WebApplicationContext webContext;
		PrintWriter out = response.getWriter();
		
		appContext = this.getServletContext();
		webContext = WebApplicationContextUtils.getWebApplicationContext(appContext);
		BookSrv bookSrv = (BookSrv)webContext.getBean("bookSrv");
		List<Book> books = bookSrv.findAll();
		for (Book book: books) {
			out.println(book.getName());
		}
	}

}
