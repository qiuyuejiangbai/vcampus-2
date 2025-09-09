package client.controller;

import common.protocol.Message;
import common.protocol.MessageType;
import common.protocol.StatusCode;
import common.vo.BookVO;
import common.vo.BorrowRecordVO;
import common.vo.DocumentVO;

import java.io.*;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户端图书馆模块控制器
 * 负责 UI 与服务器的通信
 */
public class LibraryController {
    private final Integer currentUserId;   // 当前登录用户ID
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public LibraryController(Integer userId) {
        this.currentUserId = userId;
        try {
            // 连接服务器
            this.socket = new Socket("127.0.0.1", 8888);
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 统一请求方法
     */
    private Message sendRequest(Message request) {
        try {
            out.writeObject(request);
            out.flush();
            return (Message) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(MessageType.ERROR, StatusCode.INTERNAL_ERROR, null, "请求失败");
        }
    }

    /**
     * 搜索书籍
     */
    public List<BookVO> searchBooks(String keyword) {
        Message request = new Message(MessageType.SEARCH_BOOK_REQUEST, StatusCode.SUCCESS, keyword);
        Message response = sendRequest(request);
        if (response.getStatusCode() == StatusCode.SUCCESS) {
            return (List<BookVO>) response.getData();
        }
        return Collections.emptyList();
    }

    /**
     * 借书
     */
    public boolean requestBorrow(Integer bookId) {
        Message request = new Message(MessageType.BORROW_BOOK_REQUEST, StatusCode.SUCCESS,
                new Object[]{currentUserId, bookId});
        Message response = sendRequest(request);
        return response.getStatusCode() == StatusCode.SUCCESS;
    }

    /**
     * 还书
     */
    public boolean requestReturn(int borrowId) {
        Message request = new Message(MessageType.RETURN_BOOK_REQUEST, StatusCode.SUCCESS, borrowId);
        Message response = sendRequest(request);
        return response.getStatusCode() == StatusCode.SUCCESS;
    }

    /**
     * 续借
     */
    public boolean renewBook(int borrowId) {
        Message request = new Message(MessageType.RENEW_BOOK_REQUEST, StatusCode.SUCCESS, borrowId);
        Message response = sendRequest(request);
        return response.getStatusCode() == StatusCode.SUCCESS;
    }

    /**
     * 获取借阅记录
     */
    public List<BorrowRecordVO> getBorrowingsByUser() {
        Message request = new Message(MessageType.GET_BORROW_RECORDS_REQUEST, StatusCode.SUCCESS, currentUserId);
        Message response = sendRequest(request);
        if (response.getStatusCode() == StatusCode.SUCCESS) {
            return (List<BorrowRecordVO>) response.getData();
        }
        return Collections.emptyList();
    }

    /**
     * 管理员：添加图书
     */
    public boolean submitAddBook(BookVO book) {
        Message request = new Message(MessageType.ADD_BOOK_REQUEST, StatusCode.SUCCESS, book);
        Message response = sendRequest(request);
        return response.getStatusCode() == StatusCode.SUCCESS;
    }

    /**
     * 管理员：更新图书
     */
    public boolean submitUpdateBook(BookVO book) {
        Message request = new Message(MessageType.UPDATE_BOOK_REQUEST, StatusCode.SUCCESS, book);
        Message response = sendRequest(request);
        return response.getStatusCode() == StatusCode.SUCCESS;
    }

    /**
     * 管理员：删除图书
     */
    public boolean submitDeleteBook(Integer bookId) {
        Message request = new Message(MessageType.DELETE_BOOK_REQUEST, StatusCode.SUCCESS, bookId);
        Message response = sendRequest(request);
        return response.getStatusCode() == StatusCode.SUCCESS;
    }

    public Integer getCurrentUserId() {
        return currentUserId;
    }

    /**
     * 关闭连接
     */
    public void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BookVO getBookById(int bookId) {
        Message request = new Message(MessageType.GET_BOOK_BY_ID_REQUEST, StatusCode.SUCCESS, bookId);
        Message response = sendRequest(request);

        // 根据服务端返回的状态判断
        if (response.getStatusCode() == StatusCode.SUCCESS) {
            return (BookVO) response.getData();
        }
        return null;
    }

    // 文献检索
    public List<DocumentVO> searchDocuments(String keyword, String subject, String category, Integer startYear, Integer endYear) {
        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("subject", subject);
        params.put("category", category);
        params.put("startYear", startYear);
        params.put("endYear", endYear);

        Message req = new Message(MessageType.SEARCH_DOCUMENTS_REQUEST, StatusCode.SUCCESS, params);
        Message resp = sendRequest(req);
        if (resp != null && resp.getStatusCode() == StatusCode.SUCCESS) {
            return (List<DocumentVO>) resp.getData();
        }
        return Collections.emptyList();
    }

    // 获取文献详情
    public DocumentVO getDocumentById(int docId) {
        Message req = new Message(MessageType.GET_DOCUMENT_REQUEST, StatusCode.SUCCESS, docId);
        Message resp = sendRequest(req);
        if (resp != null && resp.getStatusCode() == StatusCode.SUCCESS) {
            return (DocumentVO) resp.getData();
        }
        return null;
    }

    // 下载文献（返回文件字节流）
    public byte[] downloadDocument(int docId) {
        Message req = new Message(MessageType.DOWNLOAD_DOCUMENT_REQUEST, StatusCode.SUCCESS, docId);
        Message resp = sendRequest(req);
        if (resp != null && resp.getStatusCode() == StatusCode.SUCCESS) {
            return (byte[]) resp.getData();
        }
        return null;
    }

    // 上传文献（管理员）
    public boolean uploadDocument(DocumentVO doc, byte[] fileBytes) {
        Map<String, Object> data = new HashMap<>();
        data.put("doc", doc);
        data.put("file", fileBytes);
        Message req = new Message(MessageType.UPLOAD_DOCUMENT_REQUEST, StatusCode.SUCCESS, data);
        Message resp = sendRequest(req);
        return resp != null && resp.getStatusCode() == StatusCode.SUCCESS;
    }

    // 更新文献（管理员）
    public boolean updateDocument(DocumentVO doc) {
        Message req = new Message(MessageType.UPDATE_DOCUMENT_REQUEST, StatusCode.SUCCESS, doc);
        Message resp = sendRequest(req);
        return resp != null && resp.getStatusCode() == StatusCode.SUCCESS;
    }

    // 删除文献（管理员）
    public boolean deleteDocument(int docId) {
        Message req = new Message(MessageType.DELETE_DOCUMENT_REQUEST, StatusCode.SUCCESS, docId);
        Message resp = sendRequest(req);
        return resp != null && resp.getStatusCode() == StatusCode.SUCCESS;
    }

    public List<BorrowRecordVO> searchBorrowHistory(String keyword) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", currentUserId);
        params.put("keyword", keyword);
        Message request = new Message(MessageType.SEARCH_BORROW_HISTORY_REQUEST, StatusCode.SUCCESS, params);
        Message response = sendRequest(request);
        if (response.getStatusCode() == StatusCode.SUCCESS) {
            return (List<BorrowRecordVO>) response.getData();
        }
        return Collections.emptyList();
    }


}
