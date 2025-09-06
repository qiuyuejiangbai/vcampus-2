package server.dao.impl;

import server.service.LibraryService;
import common.vo.BookVO;
import common.vo.BorrowRecordVO;
import server.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 图书馆服务实现类
 * 负责数据库操作
 */
public class LibraryServiceImpl implements LibraryService {

    private Connection getConnection() throws SQLException {
        return DatabaseUtil.getConnection();
    }

    @Override
    public List<BookVO> searchBooks(String keyword) {
        List<BookVO> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                books.add(extractBook(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    @Override
    public List<BookVO> getAllBooks() {
        List<BookVO> books = new ArrayList<>();
        String sql = "SELECT * FROM books";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                books.add(extractBook(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    @Override
    public BookVO getBookById(Integer bookId) {
        String sql = "SELECT * FROM books WHERE book_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return extractBook(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean borrowBook(Integer userId, Integer bookId) {
        String sql = "INSERT INTO borrow_records(user_id, book_id, borrow_date, status) VALUES(?, ?, NOW(), 'BORROWED')";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean returnBook(Integer borrowId) {
        String sql = "UPDATE borrow_records SET status = 'RETURNED', return_date = NOW() WHERE borrow_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, borrowId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean renewBook(Integer borrowId) {
        String sql = "UPDATE borrow_records SET due_date = DATE_ADD(due_date, INTERVAL 30 DAY) WHERE borrow_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, borrowId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean addBook(BookVO book) {
        String sql = "INSERT INTO books(title, author, isbn, publisher, category, total_stock, available_stock, location, publication_date) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            fillBookParams(ps, book);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateBook(BookVO book) {
        String sql = "UPDATE books SET title=?, author=?, isbn=?, publisher=?, category=?, total_stock=?, available_stock=?, location=?, publication_date=? " +
                "WHERE book_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            fillBookParams(ps, book);
            ps.setInt(10, book.getBookId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteBook(Integer bookId) {
        String sql = "DELETE FROM books WHERE book_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<BorrowRecordVO> getBorrowHistory(Integer userId) {
        List<BorrowRecordVO> records = new ArrayList<>();
        String sql = "SELECT * FROM borrow_records WHERE user_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BorrowRecordVO record = new BorrowRecordVO();
                record.setRecordId(rs.getInt("borrow_id"));
                record.setUserId(rs.getInt("user_id"));
                record.setBookId(rs.getInt("book_id"));
                record.setBorrowTime(rs.getTimestamp("borrow_date"));
                record.setDueTime(rs.getTimestamp("due_date"));
                record.setReturnTime(rs.getTimestamp("return_date"));
                record.setStatus(rs.getInt("status"));
                records.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    // ================== 辅助方法 ==================
    private BookVO extractBook(ResultSet rs) throws SQLException {
        BookVO book = new BookVO();
        book.setBookId(rs.getInt("book_id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setIsbn(rs.getString("isbn"));
        book.setPublisher(rs.getString("publisher"));
        book.setCategory(rs.getString("category"));
        book.setTotalStock(rs.getInt("total_stock"));
        book.setAvailableStock(rs.getInt("available_stock"));
        book.setLocation(rs.getString("location"));
        book.setPublicationDate(rs.getDate("publication_date"));
        return book;
    }

    private void fillBookParams(PreparedStatement ps, BookVO book) throws SQLException {
        ps.setString(1, book.getTitle());
        ps.setString(2, book.getAuthor());
        ps.setString(3, book.getIsbn());
        ps.setString(4, book.getPublisher());
        ps.setString(5, book.getCategory());
        ps.setInt(6, book.getTotalStock());
        ps.setInt(7, book.getAvailableStock());
        ps.setString(8, book.getLocation());
        ps.setDate(9, new java.sql.Date(book.getPublicationDate().getTime()));
    }
}
