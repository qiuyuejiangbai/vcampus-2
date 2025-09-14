package server.net;

import common.protocol.Message;
import common.protocol.MessageType;
import common.protocol.StatusCode;
import common.vo.UserVO;
import common.vo.StudentVO;
import common.vo.TeacherVO;
import common.vo.BookVO;
import common.vo.BorrowRecordVO;
import server.service.UserService;
import server.dao.impl.LibraryServiceImpl;
import server.service.ForumService;

import java.util.List;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * 客户端处理器
 * 每个连接的客户端对应一个ClientHandler实例
 * 负责处理客户端请求并返回响应
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final VCampusServer server;
    private ObjectInputStream objectIn;
    private ObjectOutputStream objectOut;
    private Integer currentUserId; // 当前登录用户ID
    private UserVO currentUser;    // 当前登录用户信息
    private boolean isConnected = true;
    
    // 业务服务
    private final UserService userService;
    
    public ClientHandler(Socket clientSocket, VCampusServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.userService = new UserService();
        
        try {
            // 创建输入输出流
            this.objectOut = new ObjectOutputStream(clientSocket.getOutputStream());
            this.objectIn = new ObjectInputStream(clientSocket.getInputStream());

        } catch (IOException e) {
            System.err.println("创建客户端流失败: " + e.getMessage());
            disconnect();
        }
    }
    
    @Override
    public void run() {
        String clientInfo = clientSocket.getRemoteSocketAddress().toString();
        System.out.println("客户端处理线程启动: " + clientInfo);
        
        try {
            // 主循环：处理客户端消息
            while (isConnected && !clientSocket.isClosed()) {
                try {
                    Object receivedObject = objectIn.readObject();
                    
                    if (receivedObject instanceof Message) {
                        Message request = (Message) receivedObject;
                        handleMessage(request);
                    } else {
                        System.err.println("收到无效消息类型: " + receivedObject.getClass().getName());
                        sendErrorMessage("无效的消息格式");
                    }
                    
                } catch (SocketException e) {
                    // 客户端正常断开连接
                    System.out.println("客户端断开连接: " + clientInfo);
                    break;
                } catch (EOFException e) {
                    // 客户端关闭连接
                    System.out.println("客户端关闭连接: " + clientInfo);
                    break;
                } catch (ClassNotFoundException e) {
                    System.err.println("反序列化消息失败: " + e.getMessage());
                    sendErrorMessage("消息格式错误");
                } catch (IOException e) {
                    System.err.println("读取客户端消息失败: " + e.getMessage());
                    break;
                }
            }
            
        } finally {
            disconnect();
        }
    }
    
    /**
     * 处理客户端消息
     * @param request 请求消息
     */
    private void handleMessage(Message request) {
        if (request == null || request.getType() == null) {
            sendErrorMessage("无效的请求");
            return;
        }
        
        System.out.println("处理消息: " + request.getType() + " from " + 
                          (currentUser != null ? currentUser.getLoginId() : "未登录用户"));
        
        try {
            switch (request.getType()) {
                case LOGIN_REQUEST:
                    handleLogin(request);
                    break;
                    
                case REGISTER_REQUEST:
                    handleRegister(request);
                    break;
                    
                case LOGOUT_REQUEST:
                    handleLogout(request);
                    break;
                    
                case GET_USER_INFO_REQUEST:
                    handleGetUserInfo(request);
                    break;
                    
                case UPDATE_USER_REQUEST:
                    handleUpdateUser(request);
                    break;
                    
                case GET_ALL_USERS_REQUEST:
                    handleGetAllUsers(request);
                    break;
                    
                case GET_STUDENT_INFO_REQUEST:
                    handleGetStudentInfo(request);
                    break;
                    
                case GET_TEACHER_INFO_REQUEST:
                    handleGetTeacherInfo(request);
                    break;
                    
                // 课程模块
                case GET_ALL_COURSES_REQUEST:
                    System.out.println("[Course][Server] 收到请求: GET_ALL_COURSES_REQUEST");
                    handleGetAllCourses(request);
                    break;
                    
                case UPDATE_COURSE_REQUEST:
                    System.out.println("[Course][Server] 收到请求: UPDATE_COURSE_REQUEST");
                    handleUpdateCourse(request);
                    break;
                    
                case DELETE_COURSE_REQUEST:
                    System.out.println("[Course][Server] 收到请求: DELETE_COURSE_REQUEST");
                    handleDeleteCourse(request);
                    break;
                    
                case GET_ALL_ENROLLMENTS_REQUEST:
                    System.out.println("[Enrollment][Server] 收到请求: GET_ALL_ENROLLMENTS_REQUEST");
                    handleGetAllEnrollments(request);
                    break;
                    
                case GET_STUDENT_ENROLLMENTS_REQUEST:
                    System.out.println("[Enrollment][Server] 收到请求: GET_STUDENT_ENROLLMENTS_REQUEST");
                    handleGetStudentEnrollments(request);
                    break;
                    
                case ENROLL_COURSE_REQUEST:
                    System.out.println("[Enrollment][Server] 收到请求: ENROLL_COURSE_REQUEST");
                    handleEnrollCourse(request);
                    break;
                    
                case DROP_COURSE_REQUEST:
                    System.out.println("[Enrollment][Server] 收到请求: DROP_COURSE_REQUEST");
                    handleDropCourse(request);
                    break;
                    
                case GET_ENROLLMENTS_BY_COURSE_REQUEST:
                    System.out.println("[Enrollment][Server] 收到请求: GET_ENROLLMENTS_BY_COURSE_REQUEST");
                    handleGetEnrollmentsByCourse(request);
                    break;
                    
                case HEARTBEAT:
                    handleHeartbeat(request);
                    break;

                // 图书馆模块请求
                case SEARCH_BOOK_REQUEST:
                    handleSearchBooks(request);
                    break;
                case BORROW_BOOK_REQUEST:
                    handleBorrowBook(request);
                    break;
                case RETURN_BOOK_REQUEST:
                    handleReturnBook(request);
                    break;
                case RENEW_BOOK_REQUEST:
                    handleRenewBook(request);
                    break;
                case GET_BORROW_RECORDS_REQUEST:
                    handleGetBorrowRecords(request);
                    break;
                case ADD_BOOK_REQUEST:
                    handleAddBook(request);
                    break;
                case UPDATE_BOOK_REQUEST:
                    handleUpdateBook(request);
                    break;
                case DELETE_BOOK_REQUEST:
                    handleDeleteBook(request);
                    break;
                case GET_BOOK_BY_ID_REQUEST:
                    handleGetBookById(request);
                    break;
                case SEARCH_DOCUMENTS_REQUEST:
                    handleSearchDocuments(request);
                    break;
                case GET_DOCUMENT_REQUEST:
                    handleGetDocument(request);
                    break;
                case DOWNLOAD_DOCUMENT_REQUEST:
                    handleDownloadDocument(request);
                    break;
                case UPLOAD_DOCUMENT_REQUEST:
                    handleUploadDocument(request);
                    break;
                case UPDATE_DOCUMENT_REQUEST:
                    handleUpdateDocument(request);
                    break;
                case DELETE_DOCUMENT_REQUEST:
                    handleDeleteDocument(request);
                    break;
                case SEARCH_BORROW_HISTORY_REQUEST:
                    handleSearchBorrowHistory(request);
                    break;
                // 论坛模块
                case GET_ALL_THREADS_REQUEST:
                    System.out.println("[Forum][Server] 收到请求: GET_ALL_THREADS_REQUEST");
                    handleGetAllThreads(request);
                    break;
                case GET_FORUM_SECTIONS_REQUEST:
                    System.out.println("[Forum][Server] 收到请求: GET_FORUM_SECTIONS_REQUEST");
                    handleGetForumSections(request);
                    break;
                case GET_POSTS_REQUEST:
                    System.out.println("[Forum][Server] 收到请求: GET_POSTS_REQUEST, threadId=" + request.getData());
                    handleGetPosts(request);
                    break;
                case CREATE_THREAD_REQUEST:
                    System.out.println("[Forum][Server] 收到请求: CREATE_THREAD_REQUEST");
                    handleCreateThread(request);
                    break;
                case CREATE_POST_REQUEST:
                    System.out.println("[Forum][Server] 收到请求: CREATE_POST_REQUEST");
                    handleCreatePost(request);
                    break;
                case TOGGLE_THREAD_LIKE_REQUEST:
                    System.out.println("[Forum][Server] 收到请求: TOGGLE_THREAD_LIKE_REQUEST");
                    handleToggleThreadLike(request);
                    break;
                case TOGGLE_POST_LIKE_REQUEST:
                    System.out.println("[Forum][Server] 收到请求: TOGGLE_POST_LIKE_REQUEST");
                    handleTogglePostLike(request);
                    break;
                case SEARCH_THREADS_REQUEST:
                    System.out.println("[Forum][Server] 收到请求: SEARCH_THREADS_REQUEST");
                    handleSearchThreads(request);
                    break;
                case DELETE_THREAD_REQUEST:
                    System.out.println("[Forum][Server] 收到请求: DELETE_THREAD_REQUEST");
                    handleDeleteThread(request);
                    break;
                case SET_THREAD_ESSENCE_REQUEST:
                    System.out.println("[Forum][Server] 收到请求: SET_THREAD_ESSENCE_REQUEST");
                    handleSetThreadEssence(request);
                    break;
                case CREATE_SUB_REPLY_REQUEST:
                    System.out.println("[Forum][Server] 收到请求: CREATE_SUB_REPLY_REQUEST");
                    handleCreateSubReply(request);
                    break;
                case CREATE_QUOTE_REPLY_REQUEST:
                    System.out.println("[Forum][Server] 收到请求: CREATE_QUOTE_REPLY_REQUEST");
                    handleCreateQuoteReply(request);
                    break;





                default:
                    handleUnsupportedRequest(request);
                    break;
            }
        } catch (Exception e) {
            System.err.println("处理消息异常: " + e.getMessage());
            e.printStackTrace();
            sendErrorMessage("服务器内部错误: " + e.getMessage());
        }
    }

    // ================= 图书馆模块 =================

    private void handleSearchBooks(Message request) {
        try {
            String keyword = (String) request.getData();
            server.service.LibraryService libraryService = new server.dao.impl.LibraryServiceImpl();
            List<BookVO> books = libraryService.searchBooks(keyword);

            Message response = new Message(MessageType.SEARCH_BOOK_SUCCESS, StatusCode.SUCCESS, books, "搜索成功");
            sendMessage(response);
        } catch (Exception e) {
            sendErrorMessage("搜索书籍失败: " + e.getMessage());
        }
    }

    private void handleBorrowBook(Message request) {
        try {
            Object[] params = (Object[]) request.getData();
            Integer userId = (Integer) params[0];
            Integer bookId = (Integer) params[1];

            server.service.LibraryService libraryService = new server.dao.impl.LibraryServiceImpl();
            boolean success = libraryService.borrowBook(userId, bookId);

            Message response = new Message(
                    success ? MessageType.BORROW_BOOK_SUCCESS : MessageType.BORROW_BOOK_FAIL,
                    success ? StatusCode.SUCCESS : StatusCode.BAD_REQUEST,
                    null,
                    success ? "借书成功" : "借书失败"
            );
            sendMessage(response);
        } catch (Exception e) {
            sendErrorMessage("借书失败: " + e.getMessage());
        }
    }

    private void handleReturnBook(Message request) {
        try {
            Integer borrowId = (Integer) request.getData();
            server.service.LibraryService libraryService = new server.dao.impl.LibraryServiceImpl();
            boolean success = libraryService.returnBook(borrowId);

            Message response = new Message(
                    success ? MessageType.RETURN_BOOK_SUCCESS : MessageType.RETURN_BOOK_FAIL,
                    success ? StatusCode.SUCCESS : StatusCode.BAD_REQUEST,
                    null,
                    success ? "还书成功" : "还书失败"
            );
            sendMessage(response);
        } catch (Exception e) {
            sendErrorMessage("还书失败: " + e.getMessage());
        }
    }

    private void handleRenewBook(Message request) {
        try {
            Integer borrowId = (Integer) request.getData();
            server.service.LibraryService libraryService = new server.dao.impl.LibraryServiceImpl();
            boolean success = libraryService.renewBook(borrowId);

            Message response = new Message(
                    success ? MessageType.RENEW_BOOK_SUCCESS : MessageType.RENEW_BOOK_FAIL,
                    success ? StatusCode.SUCCESS : StatusCode.BAD_REQUEST,
                    null,
                    success ? "续借成功" : "续借失败"
            );
            sendMessage(response);
        } catch (Exception e) {
            sendErrorMessage("续借失败: " + e.getMessage());
        }
    }

    private void handleGetBorrowRecords(Message request) {
        try {
            Integer userId = (Integer) request.getData();
            server.service.LibraryService libraryService = new server.dao.impl.LibraryServiceImpl();
            List<BorrowRecordVO> records = libraryService.getBorrowHistory(userId);

            Message response = new Message(MessageType.GET_BORROW_RECORDS_SUCCESS, StatusCode.SUCCESS, records, "查询借阅记录成功");
            sendMessage(response);
        } catch (Exception e) {
            sendErrorMessage("获取借阅记录失败: " + e.getMessage());
        }
    }

    private void handleAddBook(Message request) {
        try {
            BookVO book = (BookVO) request.getData();
            server.service.LibraryService libraryService = new server.dao.impl.LibraryServiceImpl();
            boolean success = libraryService.addBook(book);

            Message response = new Message(
                    success ? MessageType.ADD_BOOK_SUCCESS : MessageType.ADD_BOOK_FAIL,
                    success ? StatusCode.SUCCESS : StatusCode.BAD_REQUEST,
                    null,
                    success ? "添加图书成功" : "添加图书失败"
            );
            sendMessage(response);
        } catch (Exception e) {
            sendErrorMessage("添加图书失败: " + e.getMessage());
        }
    }

    private void handleUpdateBook(Message request) {
        try {
            BookVO book = (BookVO) request.getData();
            server.service.LibraryService libraryService = new server.dao.impl.LibraryServiceImpl();
            boolean success = libraryService.updateBook(book);

            Message response = new Message(
                    success ? MessageType.UPDATE_BOOK_SUCCESS : MessageType.UPDATE_BOOK_FAIL,
                    success ? StatusCode.SUCCESS : StatusCode.BAD_REQUEST,
                    null,
                    success ? "更新图书成功" : "更新图书失败"
            );
            sendMessage(response);
        } catch (Exception e) {
            sendErrorMessage("更新图书失败: " + e.getMessage());
        }
    }

    private void handleDeleteBook(Message request) {
        try {
            Integer bookId = (Integer) request.getData();
            server.service.LibraryService libraryService = new server.dao.impl.LibraryServiceImpl();
            boolean success = libraryService.deleteBook(bookId);

            Message response = new Message(
                    success ? MessageType.DELETE_BOOK_SUCCESS : MessageType.DELETE_BOOK_FAIL,
                    success ? StatusCode.SUCCESS : StatusCode.BAD_REQUEST,
                    null,
                    success ? "删除图书成功" : "删除图书失败"
            );
            sendMessage(response);
        } catch (Exception e) {
            sendErrorMessage("删除图书失败: " + e.getMessage());
        }
    }

    private void handleGetBookById(Message request) {
        try {
            Integer bookId = (Integer) request.getData();
            server.service.LibraryService libraryService = new server.dao.impl.LibraryServiceImpl();
            BookVO book = libraryService.getBookById(bookId);

            if (book != null) {
                Message response = new Message(MessageType.GET_BOOK_BY_ID_SUCCESS, StatusCode.SUCCESS, book, "查询成功");
                sendMessage(response);
            } else {
                Message response = new Message(MessageType.GET_BOOK_BY_ID_FAIL, StatusCode.NOT_FOUND, null, "未找到图书");
                sendMessage(response);
            }
        } catch (Exception e) {
            sendErrorMessage("查询图书失败: " + e.getMessage());
        }
    }

    private void handleSearchBorrowHistory(Message request) {
        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> params = (java.util.Map<String, Object>) request.getData();
            Integer userId = (Integer) params.get("userId");
            String keyword = (String) params.get("keyword");

            LibraryServiceImpl libraryService = new server.dao.impl.LibraryServiceImpl();
            List<BorrowRecordVO> records = libraryService.searchBorrowHistory(userId, keyword);

            Message response = new Message(
                    MessageType.SEARCH_BORROW_HISTORY_SUCCESS,
                    StatusCode.SUCCESS,
                    records,
                    "搜索借阅记录成功"
            );
            sendMessage(response);
        } catch (Exception e) {
            sendErrorMessage("搜索借阅历史失败: " + e.getMessage());
        }
    }


    // ================= 文献模块 =================

    private void handleSearchDocuments(Message request) {
        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> params = (java.util.Map<String, Object>) request.getData();
            String keyword = (String) params.get("keyword");
            String subject = (String) params.get("subject");
            String category = (String) params.get("category");
            Integer startYear = (Integer) params.get("startYear");
            Integer endYear = (Integer) params.get("endYear");

            server.service.LibraryService libraryService = new server.dao.impl.LibraryServiceImpl();
            java.util.List<common.vo.DocumentVO> docs =
                    libraryService.searchDocuments(keyword, subject, category, startYear, endYear);

            Message response = new Message(MessageType.SEARCH_DOCUMENTS_RESPONSE,
                    StatusCode.SUCCESS, docs, "搜索文献成功");
            sendMessage(response);
        } catch (Exception e) {
            sendErrorMessage("搜索文献失败: " + e.getMessage());
        }
    }

    private void handleGetDocument(Message request) {
        try {
            Integer docId = (Integer) request.getData();
            server.service.LibraryService libraryService = new server.dao.impl.LibraryServiceImpl();
            common.vo.DocumentVO doc = libraryService.getDocumentById(docId);

            Message response;
            if (doc != null) {
                response = new Message(MessageType.GET_DOCUMENT_RESPONSE,
                        StatusCode.SUCCESS, doc, "获取文献成功");
            } else {
                response = new Message(MessageType.GET_DOCUMENT_RESPONSE,
                        StatusCode.NOT_FOUND, null, "文献不存在");
            }
            sendMessage(response);
        } catch (Exception e) {
            sendErrorMessage("获取文献失败: " + e.getMessage());
        }
    }

    private void handleDownloadDocument(Message request) {
        try {
            Integer docId = (Integer) request.getData();
            server.service.LibraryService libraryService = new server.dao.impl.LibraryServiceImpl();
            byte[] fileData = libraryService.downloadDocument(docId);

            Message response;
            if (fileData != null) {
                response = new Message(MessageType.DOWNLOAD_DOCUMENT_RESPONSE,
                        StatusCode.SUCCESS, fileData, "下载成功");
            } else {
                response = new Message(MessageType.DOWNLOAD_DOCUMENT_RESPONSE,
                        StatusCode.NOT_FOUND, null, "文件不存在");
            }
            sendMessage(response);
        } catch (Exception e) {
            sendErrorMessage("下载文献失败: " + e.getMessage());
        }
    }

    private void handleUploadDocument(Message request) {
        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> uploadData = (java.util.Map<String, Object>) request.getData();
            common.vo.DocumentVO doc = (common.vo.DocumentVO) uploadData.get("doc");
            byte[] fileBytes = (byte[]) uploadData.get("file");

            boolean ok = false;
            server.service.LibraryService libraryService = new server.dao.impl.LibraryServiceImpl();
            try (java.io.InputStream is = new java.io.ByteArrayInputStream(fileBytes)) {
                ok = libraryService.uploadDocument(doc, is);
            }

            Message response = new Message(MessageType.UPLOAD_DOCUMENT_RESPONSE,
                    ok ? StatusCode.SUCCESS : StatusCode.BAD_REQUEST, ok, ok ? "上传成功" : "上传失败");
            sendMessage(response);
        } catch (Exception e) {
            sendErrorMessage("上传文献失败: " + e.getMessage());
        }
    }

    private void handleUpdateDocument(Message request) {
        try {
            common.vo.DocumentVO doc = (common.vo.DocumentVO) request.getData();
            server.service.LibraryService libraryService = new server.dao.impl.LibraryServiceImpl();
            boolean ok = libraryService.updateDocument(doc);

            Message response = new Message(MessageType.UPDATE_DOCUMENT_RESPONSE,
                    ok ? StatusCode.SUCCESS : StatusCode.BAD_REQUEST, ok, ok ? "更新成功" : "更新失败");
            sendMessage(response);
        } catch (Exception e) {
            sendErrorMessage("更新文献失败: " + e.getMessage());
        }
    }

    private void handleDeleteDocument(Message request) {
        try {
            Integer docId = (Integer) request.getData();
            server.service.LibraryService libraryService = new server.dao.impl.LibraryServiceImpl();
            boolean ok = libraryService.deleteDocument(docId);

            Message response = new Message(MessageType.DELETE_DOCUMENT_RESPONSE,
                    ok ? StatusCode.SUCCESS : StatusCode.BAD_REQUEST, ok, ok ? "删除成功" : "删除失败");
            sendMessage(response);
        } catch (Exception e) {
            sendErrorMessage("删除文献失败: " + e.getMessage());
        }
    }


    //==========================================================================================

    // ================= 论坛模块 =================

    private void handleGetAllThreads(Message request) {
        try {
            System.out.println("[Forum][Server] 开始查询所有主题");
            ForumService forumService = new ForumService();
            java.util.List<common.vo.ThreadVO> threads = forumService.getAllThreads(currentUserId);
            System.out.println("[Forum][Server] 查询完成，返回条数=" + (threads != null ? threads.size() : -1));
            Message response = new Message(MessageType.GET_ALL_THREADS_SUCCESS, StatusCode.SUCCESS, threads, "获取主题成功");
            sendMessage(response);
            System.out.println("[Forum][Server] 已发送响应: GET_ALL_THREADS_SUCCESS");
        } catch (Exception e) {
            System.err.println("处理获取主题请求时发生异常: " + e.getMessage());
            sendErrorMessage("获取主题失败: " + e.getMessage());
        }
    }

    private void handleGetForumSections(Message request) {
        try {
            System.out.println("[Forum][Server] 开始查询分区列表");
            ForumService forumService = new ForumService();
            java.util.List<common.vo.ForumSectionVO> sections = forumService.getAllSections();
            System.out.println("[Forum][Server] 查询分区完成，返回条数=" + (sections != null ? sections.size() : -1));
            Message response = new Message(MessageType.GET_FORUM_SECTIONS_SUCCESS, StatusCode.SUCCESS, sections, "获取分区成功");
            sendMessage(response);
        } catch (Exception e) {
            sendErrorMessage("获取分区失败: " + e.getMessage());
        }
    }

    private void handleSearchThreads(Message request) {
        try {
            String keyword = (String) request.getData();
            System.out.println("[Forum][Server] 开始搜索帖子，关键词: " + keyword);
            
            if (keyword == null || keyword.trim().isEmpty()) {
                Message response = new Message(MessageType.SEARCH_THREADS_SUCCESS, StatusCode.SUCCESS, new java.util.ArrayList<>(), "搜索关键词为空");
                sendMessage(response);
                return;
            }
            
            ForumService forumService = new ForumService();
            java.util.List<common.vo.ThreadVO> threads = forumService.searchThreads(keyword, currentUserId);
            System.out.println("[Forum][Server] 搜索完成，返回结果数=" + (threads != null ? threads.size() : -1));
            
            String message = threads != null && threads.size() > 0 ? 
                "搜索成功，找到 " + threads.size() + " 个结果" : "未找到匹配的帖子";
            
            Message response = new Message(MessageType.SEARCH_THREADS_SUCCESS, StatusCode.SUCCESS, threads, message);
            sendMessage(response);
            System.out.println("[Forum][Server] 已发送搜索响应: SEARCH_THREADS_SUCCESS");
        } catch (Exception e) {
            System.err.println("处理搜索帖子请求时发生异常: " + e.getMessage());
            e.printStackTrace();
            Message response = new Message(MessageType.SEARCH_THREADS_FAIL, StatusCode.INTERNAL_ERROR, null, "搜索失败: " + e.getMessage());
            sendMessage(response);
        }
    }

    private void handleGetPosts(Message request) {
        try {
            Integer threadId = (Integer) request.getData();
            if (threadId == null) {
                sendErrorMessage("缺少threadId");
                return;
            }
            System.out.println("[Forum][Server] 开始查询回复，threadId=" + threadId);
            ForumService forumService = new ForumService();
            java.util.List<common.vo.PostVO> posts = forumService.getPostsByThreadId(threadId, currentUserId);
            System.out.println("[Forum][Server] 查询回复完成，返回条数=" + (posts != null ? posts.size() : -1));
            Message response = new Message(MessageType.GET_POSTS_SUCCESS, StatusCode.SUCCESS, posts, "获取回复成功");
            sendMessage(response);
        } catch (Exception e) {
            sendErrorMessage("获取回复失败: " + e.getMessage());
        }
    }

    private void handleCreateThread(Message request) {
        try {
            if (!isLoggedIn()) {
                sendUnauthorizedMessage();
                return;
            }
            common.vo.ThreadVO thread = (common.vo.ThreadVO) request.getData();
            if (thread == null) {
                sendErrorMessage("无效的主题数据");
                return;
            }
            ForumService forumService = new ForumService();
            Integer newId = forumService.createThread(thread, currentUserId);
            if (newId != null) {
                thread.setThreadId(newId);
                Message response = new Message(MessageType.CREATE_THREAD_SUCCESS, StatusCode.CREATED, thread, "创建主题成功");
                sendMessage(response);
            } else {
                Message response = new Message(MessageType.ERROR, StatusCode.INTERNAL_ERROR, null, "创建主题失败");
                sendMessage(response);
            }
        } catch (Exception e) {
            sendErrorMessage("创建主题异常: " + e.getMessage());
        }
    }

    private void handleCreatePost(Message request) {
        try {
            if (!isLoggedIn()) {
                sendUnauthorizedMessage();
                return;
            }
            common.vo.PostVO post = (common.vo.PostVO) request.getData();
            if (post == null || post.getThreadId() == null) {
                sendErrorMessage("无效的回复数据");
                return;
            }
            ForumService forumService = new ForumService();
            Integer newId = forumService.createPost(post, currentUserId);
            if (newId != null) {
                post.setPostId(newId);
                Message response = new Message(MessageType.CREATE_POST_SUCCESS, StatusCode.CREATED, post, "创建回复成功");
                sendMessage(response);
            } else {
                Message response = new Message(MessageType.ERROR, StatusCode.INTERNAL_ERROR, null, "创建回复失败");
                sendMessage(response);
            }
        } catch (Exception e) {
            sendErrorMessage("创建回复异常: " + e.getMessage());
        }
    }
    
    private void handleToggleThreadLike(Message request) {
        try {
            if (!isLoggedIn()) {
                sendUnauthorizedMessage();
                return;
            }
            Integer threadId = (Integer) request.getData();
            if (threadId == null) {
                sendErrorMessage("缺少threadId");
                return;
            }
            System.out.println("[Forum][Server] 处理主题点赞切换: threadId=" + threadId + ", userId=" + currentUserId);
            ForumService forumService = new ForumService();
            Boolean result = forumService.toggleThreadLike(threadId, currentUserId);
            if (result != null) {
                // 返回点赞结果和新的点赞数量
                int newLikeCount = forumService.getThreadLikeCount(threadId);
                System.out.println("[Forum][Server] 获取到新的点赞数量: " + newLikeCount + " for threadId=" + threadId);
                
                java.util.Map<String, Object> responseData = new java.util.HashMap<>();
                responseData.put("isLiked", result);
                responseData.put("likeCount", newLikeCount);
                responseData.put("threadId", threadId);
                
                String message = result ? "点赞成功" : "取消点赞成功";
                Message response = new Message(MessageType.TOGGLE_THREAD_LIKE_SUCCESS, StatusCode.SUCCESS, responseData, message);
                sendMessage(response);
                System.out.println("[Forum][Server] 主题点赞切换成功: " + message + ", 返回likeCount=" + newLikeCount);
            } else {
                sendErrorMessage("点赞操作失败");
            }
        } catch (Exception e) {
            System.err.println("处理主题点赞异常: " + e.getMessage());
            sendErrorMessage("点赞操作异常: " + e.getMessage());
        }
    }
    
    private void handleTogglePostLike(Message request) {
        try {
            if (!isLoggedIn()) {
                sendUnauthorizedMessage();
                return;
            }
            Integer postId = (Integer) request.getData();
            if (postId == null) {
                sendErrorMessage("缺少postId");
                return;
            }
            System.out.println("[Forum][Server] 处理回复点赞切换: postId=" + postId + ", userId=" + currentUserId);
            ForumService forumService = new ForumService();
            Boolean result = forumService.togglePostLike(postId, currentUserId);
            if (result != null) {
                // 返回点赞结果和新的点赞数量
                java.util.Map<String, Object> responseData = new java.util.HashMap<>();
                responseData.put("isLiked", result);
                responseData.put("likeCount", forumService.getPostLikeCount(postId));
                responseData.put("postId", postId);
                
                String message = result ? "点赞成功" : "取消点赞成功";
                Message response = new Message(MessageType.TOGGLE_POST_LIKE_SUCCESS, StatusCode.SUCCESS, responseData, message);
                sendMessage(response);
                System.out.println("[Forum][Server] 回复点赞切换成功: " + message);
            } else {
                sendErrorMessage("点赞操作失败");
            }
        } catch (Exception e) {
            System.err.println("处理回复点赞异常: " + e.getMessage());
            sendErrorMessage("点赞操作异常: " + e.getMessage());
        }
    }

    private void handleCreateSubReply(Message request) {
        try {
            if (!isLoggedIn()) {
                sendUnauthorizedMessage();
                return;
            }
            
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> requestData = (java.util.Map<String, Object>) request.getData();
            common.vo.PostVO post = (common.vo.PostVO) requestData.get("post");
            Integer parentPostId = (Integer) requestData.get("parentPostId");
            
            if (post == null || parentPostId == null) {
                sendErrorMessage("无效的子回复数据");
                return;
            }
            
            System.out.println("[Forum][Server] 处理创建子回复: parentPostId=" + parentPostId + ", userId=" + currentUserId);
            ForumService forumService = new ForumService();
            Integer newId = forumService.createSubReply(post, parentPostId, currentUserId);
            
            if (newId != null) {
                post.setPostId(newId);
                Message response = new Message(MessageType.CREATE_SUB_REPLY_SUCCESS, StatusCode.CREATED, post, "创建子回复成功");
                sendMessage(response);
                System.out.println("[Forum][Server] 子回复创建成功: postId=" + newId);
            } else {
                Message response = new Message(MessageType.ERROR, StatusCode.INTERNAL_ERROR, null, "创建子回复失败");
                sendMessage(response);
            }
        } catch (Exception e) {
            System.err.println("处理创建子回复异常: " + e.getMessage());
            sendErrorMessage("创建子回复异常: " + e.getMessage());
        }
    }

    private void handleCreateQuoteReply(Message request) {
        try {
            if (!isLoggedIn()) {
                sendUnauthorizedMessage();
                return;
            }
            
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> requestData = (java.util.Map<String, Object>) request.getData();
            common.vo.PostVO post = (common.vo.PostVO) requestData.get("post");
            Integer quotePostId = (Integer) requestData.get("quotePostId");
            
            if (post == null || quotePostId == null) {
                sendErrorMessage("无效的引用回复数据");
                return;
            }
            
            System.out.println("[Forum][Server] 处理创建引用回复: quotePostId=" + quotePostId + ", userId=" + currentUserId);
            ForumService forumService = new ForumService();
            Integer newId = forumService.createQuoteReply(post, quotePostId, currentUserId);
            
            if (newId != null) {
                post.setPostId(newId);
                Message response = new Message(MessageType.CREATE_QUOTE_REPLY_SUCCESS, StatusCode.CREATED, post, "创建引用回复成功");
                sendMessage(response);
                System.out.println("[Forum][Server] 引用回复创建成功: postId=" + newId);
            } else {
                Message response = new Message(MessageType.ERROR, StatusCode.INTERNAL_ERROR, null, "创建引用回复失败");
                sendMessage(response);
            }
        } catch (Exception e) {
            System.err.println("处理创建引用回复异常: " + e.getMessage());
            sendErrorMessage("创建引用回复异常: " + e.getMessage());
        }
    }

    /**
     * 处理删除帖子请求
     */
    private void handleDeleteThread(Message request) {
        try {
            if (!isLoggedIn()) {
                sendUnauthorizedMessage();
                return;
            }
            
            // 检查管理员权限
            if (currentUser == null || currentUser.getRole() == null || currentUser.getRole() != 2) {
                sendErrorMessage("权限不足，只有管理员可以删除帖子");
                return;
            }
            
            Integer threadId = (Integer) request.getData();
            if (threadId == null) {
                sendErrorMessage("缺少threadId");
                return;
            }
            
            System.out.println("[Forum][Server] 处理删除帖子: threadId=" + threadId + ", adminId=" + currentUserId);
            ForumService forumService = new ForumService();
            boolean result = forumService.deleteThread(threadId, currentUserId);
            
            if (result) {
                Message response = new Message(MessageType.DELETE_THREAD_SUCCESS, StatusCode.SUCCESS, threadId, "删除帖子成功");
                sendMessage(response);
                System.out.println("[Forum][Server] 帖子删除成功: threadId=" + threadId);
            } else {
                Message response = new Message(MessageType.ERROR, StatusCode.INTERNAL_ERROR, null, "删除帖子失败");
                sendMessage(response);
                System.err.println("[Forum][Server] 帖子删除失败: threadId=" + threadId);
            }
        } catch (Exception e) {
            System.err.println("处理删除帖子异常: " + e.getMessage());
            sendErrorMessage("删除帖子异常: " + e.getMessage());
        }
    }

    /**
     * 处理设置精华帖请求
     */
    private void handleSetThreadEssence(Message request) {
        try {
            if (!isLoggedIn()) {
                sendUnauthorizedMessage();
                return;
            }
            
            // 检查管理员权限
            if (currentUser == null || currentUser.getRole() == null || currentUser.getRole() != 2) {
                sendErrorMessage("权限不足，只有管理员可以设置精华帖");
                return;
            }
            
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> requestData = (java.util.Map<String, Object>) request.getData();
            Integer threadId = (Integer) requestData.get("threadId");
            Boolean isEssence = (Boolean) requestData.get("isEssence");
            
            if (threadId == null || isEssence == null) {
                sendErrorMessage("缺少threadId或isEssence参数");
                return;
            }
            
            String action = isEssence ? "设为精华" : "取消精华";
            System.out.println("[Forum][Server] 处理" + action + ": threadId=" + threadId + ", adminId=" + currentUserId);
            
            ForumService forumService = new ForumService();
            boolean result = forumService.setThreadEssence(threadId, isEssence, currentUserId);
            
            if (result) {
                java.util.Map<String, Object> responseData = new java.util.HashMap<>();
                responseData.put("threadId", threadId);
                responseData.put("isEssence", isEssence);
                
                Message response = new Message(MessageType.SET_THREAD_ESSENCE_SUCCESS, StatusCode.SUCCESS, responseData, action + "成功");
                sendMessage(response);
                System.out.println("[Forum][Server] " + action + "成功: threadId=" + threadId);
            } else {
                Message response = new Message(MessageType.ERROR, StatusCode.INTERNAL_ERROR, null, action + "失败");
                sendMessage(response);
                System.err.println("[Forum][Server] " + action + "失败: threadId=" + threadId);
            }
        } catch (Exception e) {
            System.err.println("处理设置精华帖异常: " + e.getMessage());
            sendErrorMessage("设置精华帖异常: " + e.getMessage());
        }
    }

    /**
     * 处理登录请求
     */
    private void handleLogin(Message request) {
        System.out.println("=== 处理登录请求 ===");
        if (request.getData() instanceof UserVO) {
            UserVO loginUser = (UserVO) request.getData();
            System.out.println("收到登录请求 - ID: " + loginUser.getId());
            
            UserVO user = userService.login(loginUser.getId(), loginUser.getPassword());
            if (user != null) {
                // 登录成功
                this.currentUserId = user.getUserId(); // 使用数据库的user_id
                this.currentUser = user;
                System.out.println("登录成功，保存会话：currentUserId=" + this.currentUserId + ", loginId=" + this.currentUser.getLoginId() + ", role=" + this.currentUser.getRoleName());
                
                // 添加到在线用户列表
                server.addOnlineUser(currentUserId, this);
                
                // 清除密码信息（安全考虑）
                user.setPassword(null);
                
                Message response = new Message(MessageType.LOGIN_SUCCESS, StatusCode.SUCCESS, user, "登录成功");
                sendMessage(response);
                
                System.out.println("用户登录成功: " + user.getId() + " (" + user.getRoleName() + ")");
            } else {
                // 登录失败
                System.out.println("登录失败，发送失败响应");
                Message response = new Message(MessageType.LOGIN_FAIL, StatusCode.INVALID_PASSWORD, null, "用户名或密码错误");
                sendMessage(response);
            }
        } else {
            System.out.println("登录数据格式错误");
            sendErrorMessage("登录数据格式错误");
        }
        System.out.println("=== 登录请求处理完成 ===");
    }
    
    /**
     * 处理注册请求
     */
    private void handleRegister(Message request) {
        try {
            if (request.getData() instanceof UserVO) {
                // 简单注册（只有基础用户信息）
                UserVO newUser = (UserVO) request.getData();
                
                Integer userId = userService.register(newUser);
                if (userId != null) {
                    // 注册成功，账户自动激活
                    Message response = new Message(MessageType.REGISTER_SUCCESS, StatusCode.CREATED, userId, "注册成功，账户已激活");
                    sendMessage(response);
                    
                    System.out.println("新用户注册: " + newUser.getId());
                } else {
                    // 注册失败
                    String errorMsg = "注册失败";
                    if (userService.loginIdExists(newUser.getId())) {
                        errorMsg = "登录ID已存在";
                    }
                    Message response = new Message(MessageType.REGISTER_FAIL, StatusCode.USER_EXISTS, null, errorMsg);
                    sendMessage(response);
                }
            } else if (request.getData() instanceof java.util.Map) {
                // 详细注册（包含学生/教师信息）
                handleDetailedRegister(request);
            } else {
                sendErrorMessage("注册数据格式错误");
            }
        } catch (Exception e) {
            System.err.println("处理注册请求失败: " + e.getMessage());
            e.printStackTrace();
            sendErrorMessage("注册处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理详细注册请求
     */
    private void handleDetailedRegister(Message request) {
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> details = (java.util.Map<String, Object>) request.getData();
        
        UserVO user = (UserVO) details.get("user");
        String name = (String) details.get("name");
        String phone = (String) details.get("phone");
        String email = (String) details.get("email");
        String department = (String) details.get("department");
        String major = (String) details.get("major");
        String title = (String) details.get("title");
        
        // 创建学生或教师信息对象
        StudentVO studentInfo = null;
        TeacherVO teacherInfo = null;
        
        if (user.isStudent()) {
            studentInfo = new StudentVO();
            studentInfo.setName(name);
            studentInfo.setPhone(phone);
            studentInfo.setEmail(email);
            studentInfo.setDepartment(department);
            studentInfo.setMajor(major);
            // 其他字段为空，账户余额默认为0在数据库中设置
        } else if (user.isTeacher()) {
            teacherInfo = new TeacherVO();
            teacherInfo.setName(name);
            teacherInfo.setPhone(phone);
            teacherInfo.setEmail(email);
            teacherInfo.setDepartment(department);
            teacherInfo.setTitle(title);
        }
        
        // 执行注册
        Integer userId = userService.register(user, studentInfo, teacherInfo);
        if (userId != null) {
            // 注册成功，账户自动激活
            Message response = new Message(MessageType.REGISTER_SUCCESS, StatusCode.CREATED, userId, "注册成功，账户已激活");
            sendMessage(response);
            
            System.out.println("新用户详细注册: " + user.getId() + " (" + user.getRoleName() + ")");
        } else {
            // 注册失败
            String errorMsg = "注册失败";
            if (userService.loginIdExists(user.getId())) {
                errorMsg = "登录ID已存在";
            }
            Message response = new Message(MessageType.REGISTER_FAIL, StatusCode.USER_EXISTS, null, errorMsg);
            sendMessage(response);
        }
    }
    
    /**
     * 处理登出请求
     */
    private void handleLogout(Message request) {
        if (currentUserId != null) {
            server.removeOnlineUser(currentUserId);
            System.out.println("用户登出: " + (currentUser != null ? currentUser.getId() : currentUserId));
        }
        
        currentUserId = null;
        currentUser = null;
        
        Message response = new Message(MessageType.LOGOUT_SUCCESS, StatusCode.SUCCESS, null, "登出成功");
        sendMessage(response);
    }
    
    /**
     * 处理获取用户信息请求
     */
    private void handleGetUserInfo(Message request) {
        if (!isLoggedIn()) {
            sendUnauthorizedMessage();
            return;
        }
        
        UserVO user = userService.getUserById(currentUserId);
        if (user != null) {
            // 清除密码信息
            user.setPassword(null);
            Message response = new Message(MessageType.GET_USER_INFO_SUCCESS, StatusCode.SUCCESS, user);
            sendMessage(response);
        } else {
            sendErrorMessage("获取用户信息失败");
        }
    }
    
    /**
     * 处理获取学生信息请求
     */
    private void handleGetStudentInfo(Message request) {
        if (!isLoggedIn()) {
            sendUnauthorizedMessage();
            return;
        }
        
        // 检查当前用户是否为学生
        if (!currentUser.isStudent()) {
            sendErrorMessage("只有学生用户才能获取学生信息");
            return;
        }
        
        // 获取学生详细信息
        System.out.println("准备查询学生信息，userId=" + currentUserId + ", loginId=" + currentUser.getLoginId());
        server.service.StudentService studentService = new server.service.StudentService();
        StudentVO student = studentService.getStudentByUserId(currentUserId);
        
        if (student != null) {
            // 设置用户信息
            student.setUserInfo(currentUser);
            System.out.println("学生信息查询成功：姓名=" + student.getName() + ", 专业=" + student.getMajor());
            Message response = new Message(MessageType.GET_STUDENT_INFO_SUCCESS, StatusCode.SUCCESS, student, "获取学生信息成功");
            sendMessage(response);
        } else {
            System.out.println("学生信息查询结果为空，userId=" + currentUserId);
            Message response = new Message(MessageType.GET_STUDENT_INFO_SUCCESS, StatusCode.NOT_FOUND, null, "学生信息不存在");
            sendMessage(response);
        }
    }
    
    private void handleGetTeacherInfo(Message request) {
        System.out.println("[DEBUG][ClientHandler] 收到GET_TEACHER_INFO_REQUEST请求");
        
        if (!isLoggedIn()) {
            System.err.println("[DEBUG][ClientHandler] 用户未登录，拒绝请求");
            sendUnauthorizedMessage();
            return;
        }
        
        System.out.println("[DEBUG][ClientHandler] 用户已登录，当前用户：userId=" + currentUserId + 
            ", loginId=" + currentUser.getLoginId() + ", role=" + currentUser.getRoleName());
        
        // 检查当前用户是否为教师
        if (!currentUser.isTeacher()) {
            System.err.println("[DEBUG][ClientHandler] 当前用户非教师角色：role=" + currentUser.getRole());
            sendErrorMessage("只有教师用户才能获取教师信息");
            return;
        }
        
        System.out.println("[DEBUG][ClientHandler] 用户角色验证通过，开始查询教师信息");
        
        // 获取教师详细信息
        System.out.println("[DEBUG][ClientHandler] 准备查询教师信息，userId=" + currentUserId + ", loginId=" + currentUser.getLoginId());
        
        try {
            server.service.TeacherService teacherService = new server.service.TeacherService();
            System.out.println("[DEBUG][ClientHandler] TeacherService创建成功，调用getTeacherByUserId");
            
            TeacherVO teacher = teacherService.getTeacherByUserId(currentUserId);
            System.out.println("[DEBUG][ClientHandler] 数据库查询完成，结果：" + (teacher != null ? "找到教师信息" : "未找到教师信息"));
            
            if (teacher != null) {
                // 设置用户信息
                teacher.setUser(currentUser);
                System.out.println("[DEBUG][ClientHandler] 教师信息查询成功：姓名=" + teacher.getName() + 
                    ", 学院=" + teacher.getDepartment() + ", 职称=" + teacher.getTitle() + ", 工号=" + teacher.getTeacherNo());
                
                Message response = new Message(MessageType.GET_TEACHER_INFO_SUCCESS, StatusCode.SUCCESS, teacher, "获取教师信息成功");
                System.out.println("[DEBUG][ClientHandler] 准备发送成功响应");
                sendMessage(response);
                System.out.println("[DEBUG][ClientHandler] 成功响应已发送");
            } else {
                System.err.println("[DEBUG][ClientHandler] 教师信息查询结果为空，userId=" + currentUserId);
                Message response = new Message(MessageType.GET_TEACHER_INFO_FAIL, StatusCode.NOT_FOUND, null, "教师信息不存在");
                System.out.println("[DEBUG][ClientHandler] 准备发送失败响应");
                sendMessage(response);
                System.out.println("[DEBUG][ClientHandler] 失败响应已发送");
            }
        } catch (Exception e) {
            System.err.println("[DEBUG][ClientHandler] 处理教师信息请求时发生异常：" + e.getMessage());
            e.printStackTrace();
            sendErrorMessage("服务器内部错误：" + e.getMessage());
        }
    }
    
    /**
     * 处理更新用户信息请求
     */
    private void handleUpdateUser(Message request) {
        if (!isLoggedIn()) {
            sendUnauthorizedMessage();
            return;
        }
        
        if (request.getData() instanceof UserVO) {
            UserVO updateUser = (UserVO) request.getData();
            
            // 安全检查：只能更新自己的信息（除非是管理员）
            if (!currentUser.isAdmin() && !currentUserId.equals(updateUser.getUserId())) {
                Message response = new Message(MessageType.UPDATE_USER_FAIL, StatusCode.FORBIDDEN, null, "无权限修改他人信息");
                sendMessage(response);
                return;
            }
            
            // 如果没有设置userId，则设置为当前用户的ID
            if (updateUser.getUserId() == null) {
                updateUser.setUserId(currentUserId);
            }
            
            boolean success = userService.updateUser(updateUser);
            if (success) {
                // 如果更新的是当前用户，刷新当前用户信息
                if (currentUserId.equals(updateUser.getUserId())) {
                    currentUser = userService.getUserById(currentUserId);
                }
                
                Message response = new Message(MessageType.UPDATE_USER_SUCCESS, StatusCode.SUCCESS, null, "更新成功");
                sendMessage(response);
            } else {
                Message response = new Message(MessageType.UPDATE_USER_FAIL, StatusCode.INTERNAL_ERROR, null, "更新失败");
                sendMessage(response);
            }
        } else {
            sendErrorMessage("更新数据格式错误");
        }
    }
    
    /**
     * 处理获取所有用户请求（管理员功能）
     */
    private void handleGetAllUsers(Message request) {
        if (!isLoggedIn()) {
            sendUnauthorizedMessage();
            return;
        }
        
        // 检查管理员权限
        if (!currentUser.isAdmin()) {
            Message response = new Message(MessageType.GET_ALL_USERS_SUCCESS, StatusCode.FORBIDDEN, null, "需要管理员权限");
            sendMessage(response);
            return;
        }
        
        List<UserVO> users = userService.getAllUsers();
        // 清除所有用户的密码信息
        users.forEach(user -> user.setPassword(null));
        
        Message response = new Message(MessageType.GET_ALL_USERS_SUCCESS, StatusCode.SUCCESS, users);
        sendMessage(response);
    }
    
    /**
     * 处理心跳请求
     */
    private void handleHeartbeat(Message request) {
        Message response = new Message(MessageType.HEARTBEAT, StatusCode.SUCCESS, System.currentTimeMillis());
        sendMessage(response);
    }
    
    /**
     * 处理不支持的请求
     */
    private void handleUnsupportedRequest(Message request) {
        System.err.println("不支持的请求类型: " + request.getType());
        Message response = new Message(MessageType.INVALID_REQUEST, StatusCode.BAD_REQUEST, null, "不支持的请求类型");
        sendMessage(response);
    }
    
    /**
     * 发送消息到客户端
     * @param message 消息对象
     */
    public void sendMessage(Object message) {
        if (!isConnected || objectOut == null) {
            return;
        }
        
        try {
            objectOut.writeObject(message);
            objectOut.flush();
        } catch (IOException e) {
            System.err.println("发送消息失败: " + e.getMessage());
            disconnect();
        }
    }
    
    /**
     * 发送错误消息
     * @param errorMsg 错误信息
     */
    private void sendErrorMessage(String errorMsg) {
        Message response = new Message(MessageType.ERROR, StatusCode.BAD_REQUEST, null, errorMsg);
        sendMessage(response);
    }
    
    /**
     * 发送未授权消息
     */
    private void sendUnauthorizedMessage() {
        Message response = new Message(MessageType.ERROR, StatusCode.UNAUTHORIZED, null, "请先登录");
        sendMessage(response);
    }
    
    /**
     * 检查用户是否已登录
     * @return 已登录返回true，未登录返回false
     */
    private boolean isLoggedIn() {
        return currentUserId != null && currentUser != null;
    }
    
    /**
     * 断开连接
     */
    public void disconnect() {
        if (!isConnected) {
            return;
        }
        
        isConnected = false;
        
        // 从服务器移除
        server.removeClientHandler(clientSocket);
        
        // 关闭流
        try {
            if (objectIn != null) {
                objectIn.close();
            }
            if (objectOut != null) {
                objectOut.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("关闭连接失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前登录用户ID
     * @return 用户ID，未登录返回null
     */
    public Integer getCurrentUserId() {
        return currentUserId;
    }
    
    /**
     * 获取当前登录用户信息
     * @return 用户信息，未登录返回null
     */
    public UserVO getCurrentUser() {
        return currentUser;
    }
    
    // ================= 课程模块 =================
    
    private void handleGetAllCourses(Message request) {
        try {
            System.out.println("[Course][Server] 开始查询所有课程");
            server.service.CourseService courseService = new server.service.CourseService();
            java.util.List<common.vo.CourseVO> courses = courseService.getAllCourses();
            System.out.println("[Course][Server] 查询完成，返回条数=" + (courses != null ? courses.size() : -1));
            Message response = new Message(MessageType.GET_ALL_COURSES_SUCCESS, StatusCode.SUCCESS, courses, "获取课程成功");
            sendMessage(response);
            System.out.println("[Course][Server] 已发送响应: GET_ALL_COURSES_SUCCESS");
        } catch (Exception e) {
            System.err.println("处理获取课程请求时发生异常: " + e.getMessage());
            sendErrorMessage("获取课程失败: " + e.getMessage());
        }
    }
    
    private void handleGetAllEnrollments(Message request) {
        try {
            System.out.println("[Enrollment][Server] 开始查询所有选课记录");
            server.service.EnrollmentService enrollmentService = new server.service.EnrollmentService();
            java.util.List<common.vo.EnrollmentVO> enrollments = enrollmentService.getAllEnrollments();
            System.out.println("[Enrollment][Server] 查询完成，返回条数=" + (enrollments != null ? enrollments.size() : -1));
            Message response = new Message(MessageType.GET_ALL_ENROLLMENTS_SUCCESS, StatusCode.SUCCESS, enrollments, "获取选课记录成功");
            sendMessage(response);
            System.out.println("[Enrollment][Server] 已发送响应: GET_ALL_ENROLLMENTS_SUCCESS");
        } catch (Exception e) {
            System.err.println("处理获取选课记录请求时发生异常: " + e.getMessage());
            sendErrorMessage("获取选课记录失败: " + e.getMessage());
        }
    }
    
    private void handleGetStudentEnrollments(Message request) {
        try {
            System.out.println("[Enrollment][Server] 开始查询学生选课记录");
            
            // 检查用户是否已登录
            if (currentUser == null) {
                sendErrorMessage("用户未登录");
                return;
            }
            
            // 获取学生ID
            Integer studentId = null;
            if (request.getData() instanceof Integer) {
                studentId = (Integer) request.getData();
            } else if (currentUser.getRole() == 0) { // 学生角色
                // 如果是学生，通过用户ID查找学生ID
                server.dao.StudentDAO studentDAO = new server.dao.impl.StudentDAOImpl();
                common.vo.StudentVO student = studentDAO.findByUserId(currentUser.getUserId());
                if (student != null) {
                    studentId = student.getStudentId();
                }
            }
            
            if (studentId == null) {
                sendErrorMessage("无法获取学生ID");
                return;
            }
            
            server.service.EnrollmentService enrollmentService = new server.service.EnrollmentService();
            java.util.List<common.vo.EnrollmentVO> enrollments = enrollmentService.getEnrollmentsByStudentId(studentId);
            System.out.println("[Enrollment][Server] 查询完成，返回条数=" + (enrollments != null ? enrollments.size() : -1));
            Message response = new Message(MessageType.GET_STUDENT_ENROLLMENTS_SUCCESS, StatusCode.SUCCESS, enrollments, "获取学生选课记录成功");
            sendMessage(response);
            System.out.println("[Enrollment][Server] 已发送响应: GET_STUDENT_ENROLLMENTS_SUCCESS");
        } catch (Exception e) {
            System.err.println("处理获取学生选课记录请求时发生异常: " + e.getMessage());
            sendErrorMessage("获取学生选课记录失败: " + e.getMessage());
        }
    }
    
    private void handleGetEnrollmentsByCourse(Message request) {
        try {
            System.out.println("[Enrollment][Server] 开始查询指定课程的选课记录");
            
            // 检查用户是否已登录
            if (currentUser == null) {
                sendErrorMessage("用户未登录");
                return;
            }
            
            // 获取课程代码
            String courseCode = null;
            if (request.getData() instanceof String) {
                courseCode = (String) request.getData();
            }
            
            if (courseCode == null || courseCode.trim().isEmpty()) {
                sendErrorMessage("课程代码不能为空");
                return;
            }
            
            // 先根据课程代码获取课程ID
            server.service.CourseService courseService = new server.service.CourseService();
            common.vo.CourseVO course = courseService.getCourseByCode(courseCode);
            if (course == null) {
                sendErrorMessage("课程不存在");
                return;
            }
            
            // 获取该课程的所有选课记录
            server.service.EnrollmentService enrollmentService = new server.service.EnrollmentService();
            java.util.List<common.vo.EnrollmentVO> enrollments = enrollmentService.getStudentListByCourseId(course.getCourseId());
            System.out.println("[Enrollment][Server] 查询完成，返回条数=" + (enrollments != null ? enrollments.size() : -1));
            Message response = new Message(MessageType.GET_ENROLLMENTS_BY_COURSE_SUCCESS, StatusCode.SUCCESS, enrollments, "获取课程选课记录成功");
            sendMessage(response);
            System.out.println("[Enrollment][Server] 已发送响应: GET_ENROLLMENTS_BY_COURSE_SUCCESS");
        } catch (Exception e) {
            System.err.println("处理获取课程选课记录请求时发生异常: " + e.getMessage());
            sendErrorMessage("获取课程选课记录失败: " + e.getMessage());
        }
    }
    
    private void handleUpdateCourse(Message request) {
        try {
            if (request.getData() instanceof common.vo.CourseVO) {
                common.vo.CourseVO course = (common.vo.CourseVO) request.getData();
                System.out.println("[Course][Server] 开始更新课程: " + course.getCourseName());
                
                server.service.CourseService courseService = new server.service.CourseService();
                boolean success = courseService.updateCourse(course);
                
                if (success) {
                    System.out.println("[Course][Server] 课程更新成功");
                    Message response = new Message(MessageType.UPDATE_COURSE_SUCCESS, StatusCode.SUCCESS, course, "课程更新成功");
                    sendMessage(response);
                    System.out.println("[Course][Server] 已发送响应: UPDATE_COURSE_SUCCESS");
                } else {
                    System.err.println("[Course][Server] 课程更新失败");
                    sendErrorMessage("课程更新失败");
                }
            } else {
                System.err.println("[Course][Server] 无效的课程数据");
                sendErrorMessage("无效的课程数据");
            }
        } catch (Exception e) {
            System.err.println("处理更新课程请求时发生异常: " + e.getMessage());
            sendErrorMessage("更新课程失败: " + e.getMessage());
        }
    }
    
    private void handleDeleteCourse(Message request) {
        try {
            if (request.getData() instanceof Integer) {
                Integer courseId = (Integer) request.getData();
                System.out.println("[Course][Server] 开始删除课程ID: " + courseId);
                
                server.service.CourseService courseService = new server.service.CourseService();
                boolean success = courseService.deleteCourse(courseId);
                
                if (success) {
                    System.out.println("[Course][Server] 课程删除成功");
                    Message response = new Message(MessageType.DELETE_COURSE_SUCCESS, StatusCode.SUCCESS, courseId, "课程删除成功");
                    sendMessage(response);
                    System.out.println("[Course][Server] 已发送响应: DELETE_COURSE_SUCCESS");
                } else {
                    System.err.println("[Course][Server] 课程删除失败");
                    sendErrorMessage("课程删除失败");
                }
            } else {
                System.err.println("[Course][Server] 无效的课程ID");
                sendErrorMessage("无效的课程ID");
            }
        } catch (Exception e) {
            System.err.println("处理删除课程请求时发生异常: " + e.getMessage());
            sendErrorMessage("删除课程失败: " + e.getMessage());
        }
    }
    
    private void handleEnrollCourse(Message request) {
        try {
            // 检查用户是否已登录
            if (currentUser == null) {
                sendErrorMessage("用户未登录");
                return;
            }
            
            // 检查用户是否为学生
            if (currentUser.getRole() != 0) {
                sendErrorMessage("只有学生可以选课");
                return;
            }
            
            // 获取课程ID
            Integer courseId = null;
            if (request.getData() instanceof Integer) {
                courseId = (Integer) request.getData();
            } else {
                sendErrorMessage("无效的课程ID");
                return;
            }
            
            // 获取学生ID
            server.dao.StudentDAO studentDAO = new server.dao.impl.StudentDAOImpl();
            common.vo.StudentVO student = studentDAO.findByUserId(currentUser.getUserId());
            if (student == null) {
                sendErrorMessage("无法获取学生信息");
                return;
            }
            
            Integer studentId = student.getStudentId();
            System.out.println("[Enrollment][Server] 学生 " + studentId + " 尝试选课 " + courseId);
            
            // 调用选课服务
            server.service.EnrollmentService enrollmentService = new server.service.EnrollmentService();
            boolean success = enrollmentService.enrollCourse(studentId, courseId);
            
            if (success) {
                System.out.println("[Enrollment][Server] 选课成功");
                Message response = new Message(MessageType.ENROLL_COURSE_SUCCESS, StatusCode.SUCCESS, courseId, "选课成功");
                sendMessage(response);
            } else {
                System.out.println("[Enrollment][Server] 选课失败");
                Message response = new Message(MessageType.ENROLL_COURSE_FAIL, StatusCode.BAD_REQUEST, null, "选课失败，可能已经选过该课程");
                sendMessage(response);
            }
        } catch (Exception e) {
            System.err.println("处理选课请求时发生异常: " + e.getMessage());
            sendErrorMessage("选课失败: " + e.getMessage());
        }
    }
    
    private void handleDropCourse(Message request) {
        try {
            // 检查用户是否已登录
            if (currentUser == null) {
                sendErrorMessage("用户未登录");
                return;
            }
            
            // 检查用户是否为学生
            if (currentUser.getRole() != 0) {
                sendErrorMessage("只有学生可以退选");
                return;
            }
            
            // 获取课程ID
            Integer courseId = null;
            if (request.getData() instanceof Integer) {
                courseId = (Integer) request.getData();
            } else {
                sendErrorMessage("无效的课程ID");
                return;
            }
            
            // 获取学生ID
            server.dao.StudentDAO studentDAO = new server.dao.impl.StudentDAOImpl();
            common.vo.StudentVO student = studentDAO.findByUserId(currentUser.getUserId());
            if (student == null) {
                sendErrorMessage("无法获取学生信息");
                return;
            }
            
            Integer studentId = student.getStudentId();
            System.out.println("[Enrollment][Server] 学生 " + studentId + " 尝试退选课程 " + courseId);
            
            // 调用退选服务
            server.service.EnrollmentService enrollmentService = new server.service.EnrollmentService();
            boolean success = enrollmentService.dropCourse(studentId, courseId);
            
            if (success) {
                System.out.println("[Enrollment][Server] 退选成功");
                Message response = new Message(MessageType.DROP_COURSE_SUCCESS, StatusCode.SUCCESS, courseId, "退选成功");
                sendMessage(response);
            } else {
                System.out.println("[Enrollment][Server] 退选失败");
                Message response = new Message(MessageType.DROP_COURSE_FAIL, StatusCode.BAD_REQUEST, null, "退选失败，可能未选该课程");
                sendMessage(response);
            }
        } catch (Exception e) {
            System.err.println("处理退选请求时发生异常: " + e.getMessage());
            sendErrorMessage("退选失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查连接是否活跃
     * @return 连接活跃返回true，已断开返回false
     */
    public boolean isConnected() {
        return isConnected && clientSocket != null && !clientSocket.isClosed();
    }
}
