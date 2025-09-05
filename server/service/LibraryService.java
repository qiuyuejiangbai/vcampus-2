package server.service;

import java.util.List;
import common.vo.BookVO;
import common.vo.BorrowRecordVO;

public interface LibraryService {
    /** 按关键字搜索图书（书名、作者、ISBN） */
    List<BookVO> searchBooks(String keyword);

    /** 获取所有图书 */
    List<BookVO> getAllBooks();

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
}
