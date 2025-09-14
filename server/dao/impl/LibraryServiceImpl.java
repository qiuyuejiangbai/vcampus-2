package server.dao.impl;

import server.service.LibraryService;
import common.vo.BookVO;
import common.vo.BorrowRecordVO;
import server.util.DatabaseUtil;
import common.vo.DocumentVO;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class LibraryServiceImpl implements LibraryService {

    private static String URL;
    private static String USER;
    private static String PASSWORD;
    private static String DRIVER;

    static {
        Properties props = new Properties();
        try {
            // 从 resources/config.properties 加载
            String configPath = System.getProperty("user.dir") + File.separator
                    + "resources" + File.separator + "config.properties";
            try (InputStream in = new FileInputStream(configPath)) {
                props.load(in);
            }

            URL = props.getProperty("db.url");
            USER = props.getProperty("db.username");
            PASSWORD = props.getProperty("db.password");
            DRIVER = props.getProperty("db.driver");

            // 加载数据库驱动
            Class.forName(DRIVER);

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("加载数据库配置失败", e);
        }
    }

    // ✅ 项目根目录下的 resources 文件夹
    private static final String BASE_PATH =
            System.getProperty("user.dir") + File.separator + "resources" + File.separator;

    private Connection getConnection() throws SQLException {
        return DatabaseUtil.getConnection();
    }

    /** 按关键字搜索图书 */
    @Override
    public List<BookVO> searchBooks(String keyword) {
        List<BookVO> list = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
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
                book.setStatus(rs.getString("status"));
                list.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** 借书 */
    @Override
    public boolean borrowBook(Integer userId, Integer bookId) {
        String checkSql = "SELECT available_stock FROM books WHERE book_id=?";
        String insertSql = "INSERT INTO borrow_records (book_id, user_id, borrow_time, due_time, status) VALUES (?, ?, ?, ?, ?)";
        String updateSql = "UPDATE books SET available_stock=available_stock-1 WHERE book_id=?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement ps = conn.prepareStatement(checkSql);
            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt("available_stock") > 0) {
                Timestamp now = new Timestamp(System.currentTimeMillis());
                Timestamp due = new Timestamp(System.currentTimeMillis() + 14L * 24 * 60 * 60 * 1000);

                PreparedStatement ps2 = conn.prepareStatement(insertSql);
                ps2.setInt(1, bookId);
                ps2.setInt(2, userId);
                ps2.setTimestamp(3, now);
                ps2.setTimestamp(4, due);
                ps2.setInt(5, 1); // 1=借出中
                ps2.executeUpdate();

                PreparedStatement ps3 = conn.prepareStatement(updateSql);
                ps3.setInt(1, bookId);
                ps3.executeUpdate();

                conn.commit();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 还书 */
    @Override
    public boolean returnBook(Integer borrowId) {
        String selectSql = "SELECT book_id FROM borrow_records WHERE record_id=? AND status IN (1,3)";
        String updateBorrow = "UPDATE borrow_records SET return_time=?, status=2 WHERE record_id=?";
        String updateBook = "UPDATE books SET available_stock=available_stock+1 WHERE book_id=?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement ps = conn.prepareStatement(selectSql);
            ps.setInt(1, borrowId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int bookId = rs.getInt("book_id");
                Timestamp now = new Timestamp(System.currentTimeMillis());

                // 设置为已归还
                PreparedStatement ps2 = conn.prepareStatement(updateBorrow);
                ps2.setTimestamp(1, now);
                ps2.setInt(2, borrowId);
                ps2.executeUpdate();

                // 库存+1
                PreparedStatement ps3 = conn.prepareStatement(updateBook);
                ps3.setInt(1, bookId);
                ps3.executeUpdate();

                conn.commit();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 查询用户借阅历史 */
    @Override
    public List<BorrowRecordVO> getBorrowHistory(Integer userId) {
        List<BorrowRecordVO> list = new ArrayList<>();
        String sql = "SELECT br.record_id, br.book_id, b.title, br.borrow_time, br.due_time, br.return_time, br.status, br.user_id "
                + "FROM borrow_records br JOIN books b ON br.book_id = b.book_id WHERE br.user_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BorrowRecordVO record = new BorrowRecordVO();
                record.setRecordId(rs.getInt("record_id"));
                record.setUserId(rs.getInt("user_id"));
                record.setBookId(rs.getInt("book_id"));
                record.setBorrowTime(rs.getTimestamp("borrow_time"));
                record.setBookTitle(rs.getString("title"));
                record.setDueTime(rs.getTimestamp("due_time"));
                record.setReturnTime(rs.getTimestamp("return_time"));
                record.setStatus(rs.getInt("status"));
                list.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** 新增图书 */
    @Override
    public boolean addBook(BookVO book) {
        String sqlWithId = "INSERT INTO books (book_id, isbn, title, author, publisher, category, publication_date, total_stock, available_stock, location, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlWithoutId = "INSERT INTO books (isbn, title, author, publisher, category, publication_date, total_stock, available_stock, location, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection()) {
            PreparedStatement ps;
            if (book.getBookId() != null) {
                ps = conn.prepareStatement(sqlWithId);
                ps.setInt(1, book.getBookId());
                ps.setString(2, book.getIsbn());
                ps.setString(3, book.getTitle());
                ps.setString(4, book.getAuthor());
                ps.setString(5, book.getPublisher());
                ps.setString(6, book.getCategory());
                if (book.getPublicationDate() != null) {
                    ps.setDate(7, book.getPublicationDate());
                } else {
                    ps.setNull(7, java.sql.Types.DATE);
                }
                ps.setInt(8, book.getTotalStock() == null ? 0 : book.getTotalStock());
                ps.setInt(9, book.getAvailableStock() == null ? 0 : book.getAvailableStock());
                ps.setString(10, book.getLocation());
                ps.setString(11, book.getStatus() == null ? "available" : book.getStatus());
            } else {
                ps = conn.prepareStatement(sqlWithoutId);
                ps.setString(1, book.getIsbn());
                ps.setString(2, book.getTitle());
                ps.setString(3, book.getAuthor());
                ps.setString(4, book.getPublisher());
                ps.setString(5, book.getCategory());
                if (book.getPublicationDate() != null) {
                    ps.setDate(6, book.getPublicationDate());
                } else {
                    ps.setNull(6, java.sql.Types.DATE);
                }
                ps.setInt(7, book.getTotalStock() == null ? 0 : book.getTotalStock());
                ps.setInt(8, book.getAvailableStock() == null ? 0 : book.getAvailableStock());
                ps.setString(9, book.getLocation());
                ps.setString(10, book.getStatus() == null ? "available" : book.getStatus());
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** 更新图书信息 */
    @Override
    public boolean updateBook(BookVO book) {
        String sql = "UPDATE books SET isbn=?, title=?, author=?, publisher=?, category=?, publication_date=?, "
                + "total_stock=?, available_stock=?, location=?, status=? WHERE book_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getPublisher());
            ps.setString(5, book.getCategory());
            if (book.getPublicationDate() != null) {
                ps.setDate(6, book.getPublicationDate());
            } else {
                ps.setNull(6, java.sql.Types.DATE);
            }
            ps.setInt(7, book.getTotalStock() == null ? 0 : book.getTotalStock());
            ps.setInt(8, book.getAvailableStock() == null ? 0 : book.getAvailableStock());
            ps.setString(9, book.getLocation());
            ps.setString(10, book.getStatus() == null ? "available" : book.getStatus());
            ps.setInt(11, book.getBookId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** 删除图书 */
    @Override
    public boolean deleteBook(Integer bookId) {
        String checkSql = "SELECT COUNT(*) FROM borrow_records WHERE book_id=? AND return_time IS NULL";
        String deleteSql = "DELETE FROM books WHERE book_id=?";
        try (Connection conn = getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, bookId);
            ResultSet rs = checkPs.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return false; // 还有未归还的借阅记录，不能删除
            }
            try (PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
                deletePs.setInt(1, bookId);
                return deletePs.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** 续借 */
    @Override
    public boolean renewBook(Integer borrowId) {
        String sql = "UPDATE borrow_records " +
                "SET due_time = DATE_ADD(due_time, INTERVAL 7 DAY), " +
                "    status = CASE " +
                "        WHEN DATE_ADD(due_time, INTERVAL 7 DAY) < NOW() THEN 3 " +  // 逾期
                "        ELSE 1 " +  // 正常借出
                "    END " +
                "WHERE record_id = ? AND status IN (1,3)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, borrowId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** 按 ID 获取单本图书 */
    @Override
    public BookVO getBookById(Integer bookId) {
        String sql = "SELECT * FROM books WHERE book_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BookVO book = new BookVO();
                book.setBookId(rs.getInt("book_id"));
                book.setIsbn(rs.getString("isbn"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setPublisher(rs.getString("publisher"));
                book.setCategory(rs.getString("category"));
                book.setPublicationDate(rs.getDate("publication_date"));
                book.setTotalStock(rs.getInt("total_stock"));
                book.setAvailableStock(rs.getInt("available_stock"));
                book.setLocation(rs.getString("location"));
                book.setStatus(rs.getString("status"));
                return book;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<DocumentVO> searchDocuments(String keyword, String subject, String category, Integer startYear, Integer endYear) {
        List<DocumentVO> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM library_documents WHERE is_public = 1");

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (title LIKE ? OR authors LIKE ? OR keywords LIKE ?)");
        }
        if (subject != null && !subject.isEmpty()) {
            sql.append(" AND subject = ?");
        }
        if (category != null && !category.isEmpty()) {
            sql.append(" AND category = ?");
        }
        if (startYear != null) {
            sql.append(" AND year >= ?");
        }
        if (endYear != null) {
            sql.append(" AND year <= ?");
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (keyword != null && !keyword.isEmpty()) {
                String like = "%" + keyword + "%";
                stmt.setString(idx++, like);
                stmt.setString(idx++, like);
                stmt.setString(idx++, like);
            }
            if (subject != null && !subject.isEmpty()) stmt.setString(idx++, subject);
            if (category != null && !category.isEmpty()) stmt.setString(idx++, category);
            if (startYear != null) stmt.setInt(idx++, startYear);
            if (endYear != null) stmt.setInt(idx++, endYear);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(mapResultSetToDocument(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public DocumentVO getDocumentById(int docId) {
        String sql = "SELECT * FROM library_documents WHERE doc_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, docId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToDocument(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] downloadDocument(int docId) {
        DocumentVO doc = getDocumentById(docId);
        if (doc == null) return null;

        File file = new File(BASE_PATH + doc.getStoragePath());
        if (!file.exists()) return null;

        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean uploadDocument(DocumentVO doc, InputStream fileStream) {
        String relativePath = String.format("docs/%s/%d/%s.%s",
                doc.getSubject(),
                doc.getYear(),
                doc.getTitle().replaceAll("\\s+", "_"),
                doc.getFileType());

        String sql = "INSERT INTO library_documents " +
                "(title, authors, year, category, subject, keywords, abstract_txt, file_type, file_size, storage_path, uploader_id, is_public) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, doc.getTitle());
            stmt.setString(2, doc.getAuthors());
            stmt.setInt(3, doc.getYear());
            stmt.setString(4, doc.getCategory());
            stmt.setString(5, doc.getSubject());
            stmt.setString(6, doc.getKeywords());
            stmt.setString(7, doc.getAbstractTxt());
            stmt.setString(8, doc.getFileType());
            stmt.setLong(9, doc.getFileSize());
            stmt.setString(10, relativePath);
            stmt.setInt(11, doc.getUploaderId());
            stmt.setBoolean(12, doc.isPublic());

            int affected = stmt.executeUpdate();
            if (affected == 0) return false;

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int newId = keys.getInt(1);

                String finalPath = String.format("docs/%s/%d/%d_%s.%s",
                        doc.getSubject(),
                        doc.getYear(),
                        newId,
                        doc.getTitle().replaceAll("\\s+", "_"),
                        doc.getFileType());

                File outFile = new File(BASE_PATH + finalPath);
                outFile.getParentFile().mkdirs();

                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = fileStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                }

                String updateSql = "UPDATE library_documents SET storage_path = ? WHERE doc_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, finalPath);
                    updateStmt.setInt(2, newId);
                    updateStmt.executeUpdate();
                }
            }
            return true;
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateDocument(DocumentVO doc) {
        String sql = "UPDATE library_documents SET title=?, authors=?, year=?, category=?, subject=?, keywords=?, abstract_txt=?, is_public=? WHERE doc_id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, doc.getTitle());
            stmt.setString(2, doc.getAuthors());
            stmt.setInt(3, doc.getYear());
            stmt.setString(4, doc.getCategory());
            stmt.setString(5, doc.getSubject());
            stmt.setString(6, doc.getKeywords());
            stmt.setString(7, doc.getAbstractTxt());
            stmt.setBoolean(8, doc.isPublic());
            stmt.setInt(9, doc.getDocId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteDocument(int docId) {
        DocumentVO doc = getDocumentById(docId);
        if (doc == null) return false;

        File file = new File(BASE_PATH + doc.getStoragePath());

        String sql = "DELETE FROM library_documents WHERE doc_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, docId);
            int rows = stmt.executeUpdate();
            if (rows > 0 && file.exists()) {
                file.delete();
            }
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private DocumentVO mapResultSetToDocument(ResultSet rs) throws SQLException {
        DocumentVO doc = new DocumentVO();
        doc.setDocId(rs.getInt("doc_id"));
        doc.setTitle(rs.getString("title"));
        doc.setAuthors(rs.getString("authors"));
        doc.setYear(rs.getInt("year"));
        doc.setCategory(rs.getString("category"));
        doc.setSubject(rs.getString("subject"));
        doc.setKeywords(rs.getString("keywords"));
        doc.setAbstractTxt(rs.getString("abstract_txt"));
        doc.setFileType(rs.getString("file_type"));
        doc.setFileSize(rs.getLong("file_size"));
        doc.setStoragePath(rs.getString("storage_path"));
        doc.setUploaderId(rs.getInt("uploader_id"));
        doc.setUploadTime(rs.getString("upload_time"));
        doc.setPublic(rs.getBoolean("is_public"));
        return doc;
    }

}
