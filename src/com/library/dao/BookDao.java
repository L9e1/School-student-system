package com.library.dao;

import com.library.entity.Book;
import com.library.entity.Page;
import com.library.util.DBUtil;
import com.library.util.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 图书数据访问。所有用户输入均走 PreparedStatement，排序字段使用白名单。
 */
public class BookDao {

    private static final String SELECT_FIELDS =
            "SELECT b.id, b.category_id, b.name, b.author, b.isbn, b.publisher, b.price, b.stock, "
                    + "b.description, b.create_time, b.update_time, "
                    + "c.name AS category_name, c.icon AS category_icon, c.color AS category_color "
                    + "FROM book b JOIN category c ON b.category_id = c.id ";

    /**
     * 分页查询。categoryId 为 null 时表示全部板块。
     */
    public Page<Book> search(Integer categoryId, String keyword, String sort, int page, int pageSize) {
        String cleanKeyword = keyword == null ? "" : keyword.trim();
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        if (categoryId != null) {
            where.append(" AND b.category_id = ? ");
            params.add(categoryId);
        }
        if (!cleanKeyword.isEmpty()) {
            where.append(" AND (b.name LIKE ? OR b.author LIKE ? OR b.isbn LIKE ? OR b.publisher LIKE ?) ");
            String like = "%" + cleanKeyword + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        int total = count(where.toString(), params);
        int totalPages = Math.max(1, (total + pageSize - 1) / pageSize);
        int currentPage = Math.min(Math.max(1, page), totalPages);
        int offset = (currentPage - 1) * pageSize;

        String sql = SELECT_FIELDS + where + " ORDER BY " + safeSort(sort) + " LIMIT ? OFFSET ?";
        List<Book> books = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            for (Object param : params) {
                ps.setObject(index++, param);
            }
            ps.setInt(index++, pageSize);
            ps.setInt(index, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询图书失败", e);
        }
        return new Page<>(books, currentPage, pageSize, total);
    }

    public Book findById(int id) {
        String sql = SELECT_FIELDS + " WHERE b.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询图书失败", e);
        }
    }

    public void insert(Book book) {
        String sql = "INSERT INTO book(category_id, name, author, isbn, publisher, price, stock, description) "
                + "VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, book);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    book.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("新增图书失败", e);
        }
    }

    public boolean update(Book book) {
        String sql = "UPDATE book SET category_id=?, name=?, author=?, isbn=?, publisher=?, price=?, stock=?, "
                + "description=?, update_time=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, book);
            ps.setInt(9, book.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("修改图书失败", e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM book WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("删除图书失败", e);
        }
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM book";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DataAccessException("统计图书失败", e);
        }
    }

    public int sumStock() {
        String sql = "SELECT COALESCE(SUM(stock), 0) FROM book";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DataAccessException("统计库存失败", e);
        }
    }

    public List<Book> findRecent(int limit) {
        String sql = SELECT_FIELDS + " ORDER BY b.create_time DESC, b.id DESC LIMIT ?";
        List<Book> books = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询最近上架失败", e);
        }
        return books;
    }

    private int count(String where, List<Object> params) {
        String sql = "SELECT COUNT(*) FROM book b JOIN category c ON b.category_id = c.id " + where;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("统计图书失败", e);
        }
    }

    private String safeSort(String sort) {
        if (sort == null) {
            return "b.create_time DESC, b.id DESC";
        }
        switch (sort) {
            case "priceAsc":
                return "b.price ASC, b.id DESC";
            case "priceDesc":
                return "b.price DESC, b.id DESC";
            case "stockDesc":
                return "b.stock DESC, b.id DESC";
            case "nameAsc":
                return "b.name ASC";
            case "oldest":
                return "b.create_time ASC, b.id ASC";
            default:
                return "b.create_time DESC, b.id DESC";
        }
    }

    private void bind(PreparedStatement ps, Book book) throws SQLException {
        ps.setInt(1, book.getCategoryId());
        ps.setString(2, book.getName());
        ps.setString(3, book.getAuthor());
        ps.setString(4, book.getIsbn());
        ps.setString(5, book.getPublisher());
        ps.setBigDecimal(6, book.getPrice());
        ps.setInt(7, book.getStock());
        ps.setString(8, book.getDescription());
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setCategoryId(rs.getInt("category_id"));
        book.setCategoryName(rs.getString("category_name"));
        book.setCategoryIcon(rs.getString("category_icon"));
        book.setCategoryColor(rs.getString("category_color"));
        book.setName(rs.getString("name"));
        book.setAuthor(rs.getString("author"));
        book.setIsbn(rs.getString("isbn"));
        book.setPublisher(rs.getString("publisher"));
        book.setPrice(rs.getBigDecimal("price"));
        book.setStock(rs.getInt("stock"));
        book.setDescription(rs.getString("description"));
        book.setCreateTime(rs.getTimestamp("create_time"));
        book.setUpdateTime(rs.getTimestamp("update_time"));
        return book;
    }
}
