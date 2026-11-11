package com.library.servlet;

import com.library.dao.BookDao;
import com.library.dao.CategoryDao;
import com.library.entity.Book;
import com.library.entity.Page;
import com.library.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 全馆搜索。
 */
public class SearchServlet extends HttpServlet {

    private static final int PAGE_SIZE = 12;

    private final BookDao bookDao = new BookDao();
    private final CategoryDao categoryDao = new CategoryDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = WebUtil.trim(req.getParameter("keyword"));
        String sort = WebUtil.trim(req.getParameter("sort"));
        int pageNo = WebUtil.intValue(req.getParameter("page"), 1);

        Page<Book> bookPage = bookDao.search(null, keyword, sort, pageNo, PAGE_SIZE);

        req.setAttribute("bookPage", bookPage);
        req.setAttribute("categories", categoryDao.findAllWithCount());
        req.setAttribute("keyword", keyword);
        req.setAttribute("sort", sort);
        req.getRequestDispatcher("/WEB-INF/views/search.jsp").forward(req, resp);
    }
}
