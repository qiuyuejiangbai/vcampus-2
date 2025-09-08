package server.service;

import java.io.InputStream;
import java.util.List;
import common.vo.BookVO;
import common.vo.BorrowRecordVO;
import common.vo.DocumentVO;

public interface LibraryService {
    /** 按关键字搜索图书（书名、作者、ISBN） */
    List<BookVO> searchBooks(String keyword);

    /** 按 ID 获取单本图书 */
    BookVO getBookById(Integer bookId);

    /** 借书 */
    boolean borrowBook(Integer userId, Integer bookId);

    /** 还书 */
    boolean returnBook(Integer borrowId);

    /** 续借 */
    boolean renewBook(Integer borrowId);

    /** 新增图书 */
    boolean addBook(BookVO book);

    /** 更新图书信息 */
    boolean updateBook(BookVO book);

    /** 删除图书 */
    boolean deleteBook(Integer bookId);

    /** 查询用户的借阅历史 */
    List<BorrowRecordVO> getBorrowHistory(Integer userId);

    // 文献检索：根据关键字和筛选条件查找文献
    List<DocumentVO> searchDocuments(String keyword, String subject, String category, Integer startYear, Integer endYear);

    // 根据文献ID获取详情
    DocumentVO getDocumentById(int docId);

    // 下载文献：返回文件字节流
    byte[] downloadDocument(int docId);

    // 上传文献（管理员）：插入记录 + 保存文件
    boolean uploadDocument(DocumentVO doc, InputStream fileStream);

    // 编辑文献信息（不涉及文件）
    boolean updateDocument(DocumentVO doc);

    // 删除文献（数据库 + 磁盘文件）
    boolean deleteDocument(int docId);

}