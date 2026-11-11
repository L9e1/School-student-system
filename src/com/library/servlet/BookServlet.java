package com.library.servlet;

import com.library.dao.BookDao;
import com.library.dao.CategoryDao;
import com.library.entity.Book;
import com.library.entity.Category;
import com.library.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 图书：书架中的新增、修改、删除、详情。
 */
public class BookServlet extends HttpServlet {

    private final BookDao bookDao = new BookDao();
    private final CategoryDao categoryDao = new CategoryDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = WebUtil.trim(req.getParameter("action"));
        switch (action) {
            case "toAdd":
                showForm(req, resp, null, WebUtil.intValue(req.getParameter("categoryId"), 0));
                break;
            case "toEdit": {
                Book book = findBook(req);
                if (book == null) {
                    WebUtil.redirect(req, resp, "/home");
                    return;
                }
                showForm(req, resp, book, book.getCategoryId());
                break;
            }
            case "detail": {
                Book book = findBook(req);
                if (book == null) {
                    WebUtil.flash(req, "error", "图书不存在或已被删除");
                    WebUtil.redirect(req, resp, "/home");
                    return;
                }
                req.setAttribute("book", book);
                req.getRequestDispatcher("/WEB-INF/views/book-detail.jsp").forward(req, resp);
                break;
            }
            default:
                WebUtil.redirect(req, resp, "/home");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if ("delete".equals(WebUtil.trim(req.getParameter("action")))) {
            delete(req, resp);
        } else {
            save(req, resp);
        }
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, Book book, int categoryId)
            throws ServletException, IOException {
        if (book == null) {
            book = new Book();
            book.setCategoryId(categoryId);
            book.setPrice(BigDecimal.ZERO);
            req.setAttribute("mode", "add");
        } else {
            req.setAttribute("mode", "edit");
        }
        req.setAttribute("book", book);
        req.setAttribute("categories", categoryDao.findAllWithCount());
        req.getRequestDispatcher("/WEB-INF/views/book-form.jsp").forward(req, resp);
    }

    private void save(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = WebUtil.intValue(req.getParameter("id"), 0);
        Book book = new Book();
        book.setId(id);
        book.setCategoryId(WebUtil.intValue(req.getParameter("categoryId"), 0));
        book.setName(WebUtil.trim(req.getParameter("name")));
        book.setAuthor(WebUtil.trim(req.getParameter("author")));
        book.setIsbn(WebUtil.trim(req.getParameter("isbn")));
        book.setPublisher(WebUtil.trim(req.getParameter("publisher")));
        book.setDescription(WebUtil.trim(req.getParameter("description")));

        Map<String, String> errors = new LinkedHashMap<>();
        if (book.getName().isEmpty()) {
            errors.put("name", "请填写书名");
        } else if (book.getName().length() > 100) {
            errors.put("name", "书名不能超过 100 个字符");
        }
        if (book.getAuthor().isEmpty()) {
            errors.put("author", "请填写作者");
        } else if (book.getAuthor().length() > 50) {
            errors.put("author", "作者不能超过 50 个字符");
        }
        if (book.getIsbn().length() > 30) {
            errors.put("isbn", "ISBN 不能超过 30 个字符");
        }
        if (book.getPublisher().length() > 80) {
            errors.put("publisher", "出版社不能超过 80 个字符");
        }
        if (book.getDescription().length() > 2000) {
            errors.put("description", "简介不能超过 2000 个字符");
        }

        Category category = book.getCategoryId() > 0 ? categoryDao.findById(book.getCategoryId()) : null;
        if (category == null) {
            errors.put("categoryId", "请选择有效的板块");
        } else {
            book.setCategoryName(category.getName());
        }

        try {
            BigDecimal price = new BigDecimal(WebUtil.trim(req.getParameter("price")));
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                errors.put("price", "价格不能为负数");
            } else if (price.scale() > 2) {
                errors.put("price", "价格最多保留两位小数");
            } else {
                book.setPrice(price);
            }
        } catch (NumberFormatException e) {
            errors.put("price", "请输入正确的价格");
            book.setPrice(BigDecimal.ZERO);
        }

        try {
            int stock = Integer.parseInt(WebUtil.trim(req.getParameter("stock")));
            if (stock < 0) {
                errors.put("stock", "库存不能为负数");
            } else {
                book.setStock(stock);
            }
        } catch (NumberFormatException e) {
            errors.put("stock", "请输入正确的库存数量");
        }

        if (id > 0 && bookDao.findById(id) == null) {
            errors.put("name", "图书不存在或已被删除");
        }

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            req.setAttribute("book", book);
            req.setAttribute("categories", categoryDao.findAllWithCount());
            req.setAttribute("mode", id > 0 ? "edit" : "add");
            req.getRequestDispatcher("/WEB-INF/views/book-form.jsp").forward(req, resp);
            return;
        }

        if (id > 0) {
            bookDao.update(book);
            WebUtil.flash(req, "success", "《" + book.getName() + "》已更新");
        } else {
            bookDao.insert(book);
            WebUtil.flash(req, "success", "《" + book.getName() + "》已上架");
        }
        WebUtil.redirect(req, resp, "/category?id=" + book.getCategoryId());
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = WebUtil.intValue(req.getParameter("id"), 0);
        Book book = id > 0 ? bookDao.findById(id) : null;
        if (book == null) {
            WebUtil.flash(req, "error", "图书不存在或已被删除");
            WebUtil.redirect(req, resp, "/home");
            return;
        }
        bookDao.delete(id);
        WebUtil.flash(req, "success", "《" + book.getName() + "》已下架删除");
        WebUtil.redirect(req, resp, "/category?id=" + book.getCategoryId());
    }

    private Book findBook(HttpServletRequest req) {
        int id = WebUtil.intValue(req.getParameter("id"), 0);
        return id > 0 ? bookDao.findById(id) : null;
    }
}
