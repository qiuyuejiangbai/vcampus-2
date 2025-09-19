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
import server.service.GradeService;
import server.dao.impl.LibraryServiceImpl;
import server.service.ForumService;
import common.vo.ProductVO;
import common.vo.ShoppingCartItemVO;
import common.vo.OrderVO;
import common.vo.OrderItemVO;
import java.util.Map;
import java.util.HashMap;
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
    private final GradeService gradeService;
    
    public ClientHandler(Socket clientSocket, VCampusServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.userService = new UserService();
        this.gradeService = new GradeService();
        
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
                    
                case UPLOAD_AVATAR_REQUEST:
                    handleUploadAvatar(request);
                    break;
                    
                case DOWNLOAD_AVATAR_REQUEST:
                    handleDownloadAvatar(request);
                    break;
                    
                case GET_ALL_USERS_REQUEST:
                    handleGetAllUsers(request);
                    break;
                    
                case RESET_PASSWORD_REQUEST:
                    handleResetPassword(request);
                    break;
                    
                case CHANGE_PASSWORD_REQUEST:
                    handleChangePassword(request);
                    break;
                    
                case GET_STUDENT_INFO_REQUEST:
                    handleGetStudentInfo(request);
                    break;
                    
                case UPDATE_STUDENT_REQUEST:
                    handleUpdateStudent(request);
                    break;
                    
                case GET_TEACHER_INFO_REQUEST:
                    handleGetTeacherInfo(request);
                    break;
                    
                case UPDATE_TEACHER_REQUEST:
                    handleUpdateTeacher(request);
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
                    
                case GET_COURSE_SCHEDULES_REQUEST:
                    System.out.println("[CourseSchedule][Server] 收到请求: GET_COURSE_SCHEDULES_REQUEST");
                    handleGetCourseSchedules(request);
                    break;
                    
                case DELETE_CONFLICT_CLASS_REQUEST:
                    System.out.println("[Course][Server] 收到请求: DELETE_CONFLICT_CLASS_REQUEST");
                    handleDeleteConflictClass(request);
                    break;
                    
                // 成绩管理模块
                case GET_ALL_GRADES_REQUEST:
                    System.out.println("[Grade][Server] 收到请求: GET_ALL_GRADES_REQUEST");
                    handleGetAllGrades(request);
                    break;
                    
                case GET_GRADES_BY_STUDENT_REQUEST:
                    System.out.println("[Grade][Server] 收到请求: GET_GRADES_BY_STUDENT_REQUEST");
                    handleGetGradesByStudent(request);
                    break;
                    
                case GET_GRADES_BY_COURSE_REQUEST:
                    System.out.println("[Grade][Server] 收到请求: GET_GRADES_BY_COURSE_REQUEST");
                    handleGetGradesByCourse(request);
                    break;
                    
                case ADD_GRADE_REQUEST:
                    System.out.println("[Grade][Server] 收到请求: ADD_GRADE_REQUEST");
                    handleAddGrade(request);
                    break;
                    
                case UPDATE_GRADE_REQUEST:
                    System.out.println("[Grade][Server] 收到请求: UPDATE_GRADE_REQUEST");
                    handleUpdateGrade(request);
                    break;
                    
                case DELETE_GRADE_REQUEST:
                    System.out.println("[Grade][Server] 收到请求: DELETE_GRADE_REQUEST");
                    handleDeleteGrade(request);
                    break;
                    
                case GET_ALL_STUDENTS_REQUEST:
                    System.out.println("[Student][Server] 收到请求: GET_ALL_STUDENTS_REQUEST");
                    handleGetAllStudents(request);
                    break;
                    
                case GET_ALL_TEACHERS_REQUEST:
                    System.out.println("[Teacher][Server] 收到请求: GET_ALL_TEACHERS_REQUEST");
                    handleGetAllTeachers(request);
                    break;
                    
                // 管理员学生管理
                case ADD_STUDENT:
                    System.out.println("[Student][Server] 收到请求: ADD_STUDENT");
                    handleAddStudent(request);
                    break;
                    
                case UPDATE_STUDENT:
                    System.out.println("[Student][Server] 收到请求: UPDATE_STUDENT");
                    handleUpdateStudentAdmin(request);
                    break;
                    
                case DELETE_STUDENT:
                    System.out.println("[Student][Server] 收到请求: DELETE_STUDENT");
                    handleDeleteStudent(request);
                    break;
                    
                // 管理员教师管理
                case ADD_TEACHER:
                    System.out.println("[Teacher][Server] 收到请求: ADD_TEACHER");
                    handleAddTeacher(request);
                    break;
                    
                case UPDATE_TEACHER:
                    System.out.println("[Teacher][Server] 收到请求: UPDATE_TEACHER");
                    handleUpdateTeacherAdmin(request);
                    break;
                    
                case DELETE_TEACHER:
                    System.out.println("[Teacher][Server] 收到请求: DELETE_TEACHER");
                    handleDeleteTeacher(request);
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


                      // ================== 商店模块 ==================

                      // ===== 商品管理 =====
                case SEARCH_PRODUCTS_REQUEST:
                    handleSearchProducts(request);
                    break;
                case ADD_PRODUCT_REQUEST:
                    handleAddProduct(request);
                    break;
                case UPDATE_PRODUCT_REQUEST:
                    handleUpdateProduct(request);
                    break;
                case DELETE_PRODUCT_REQUEST:
                    handleDeleteProduct(request);
                    break;
                case GET_PRODUCT_BY_ID_REQUEST:
                    handleGetProductById(request);
                    break;
                    // ===== 购物车管理 =====
                case GET_SHOPPING_CART_REQUEST:
                    handleGetShoppingCart(request);
                    break;
                case ADD_TO_CART_REQUEST:
                    handleAddToCart(request);
                    break;
                case UPDATE_CART_ITEM_REQUEST:
                    handleUpdateCartItem(request);
                    break;
                case REMOVE_FROM_CART_REQUEST:
                    handleRemoveFromCart(request);
                    break;
                case CLEAR_CART_REQUEST:
                    handleClearCart(request);
                    break;
// ===== 订单管理 =====
                case CREATE_ORDER_REQUEST:
                    handlePlaceOrder(request);
                    break;
                case GET_USER_ORDER_HISTORY_REQUEST:
                    handleGetOrdersByUser(request);
                    break;
case GET_ADMIN_ORDER_DETAIL_REQUEST:
    handleGetAdminOrderDetail(request);
    break;
case CANCEL_ORDER_REQUEST:
    handleCancelOrder(request);
    break;
case GET_ORDER_ITEMS_REQUEST:
    handleGetOrderItems(request);
    break;
case PAY_ORDER_REQUEST:
    handlePayOrder(request);
    break;
case SHIP_ORDER_REQUEST:
    handleShipOrder(request);
    break;
    case GET_ALL_USER_ORDERS_REQUEST:
    handleGetAllUserOrders(request);
    break;
//库存管理
case ADJUST_STOCK_REQUEST:
    handleAdjustStock(request);
    break;
    // ===== 余额管理 =====
case GET_USER_BALANCE_REQUEST:
    handleGetUserBalance(request);
    break;
case RECHARGE_BALANCE_REQUEST:
    handleRechargeBalance(request);
    break;
case PAY_WITH_BALANCE_REQUEST:
    handlePayWithBalance(request);
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
            System.out.println("[DEBUG] ClientHandler.handleSearchBooks() 开始执行");
            String keyword = (String) request.getData();
            System.out.println("[DEBUG] 收到搜索关键词: '" + keyword + "'");
            
            server.service.LibraryService libraryService = new server.dao.impl.LibraryServiceImpl();
            System.out.println("[DEBUG] 创建LibraryService实例成功");
            
            List<BookVO> books = libraryService.searchBooks(keyword);
            System.out.println("[DEBUG] LibraryService.searchBooks() 返回 " + (books != null ? books.size() : "null") + " 本书");

            Message response = new Message(MessageType.SEARCH_BOOK_SUCCESS, StatusCode.SUCCESS, books, "搜索成功");
            System.out.println("[DEBUG] 准备发送响应给客户端");
            sendMessage(response);
            System.out.println("[DEBUG] 响应已发送");
        } catch (Exception e) {
            System.out.println("[DEBUG] 搜索书籍异常: " + e.getMessage());
            e.printStackTrace();
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

    private void handleSearchProducts(Message request) {
    try {
        String keyword = (String) request.getData();
        server.service.StoreService storeService = new server.dao.impl.StoreServiceImpl();
        List<ProductVO> products = storeService.searchProducts(keyword);
        Message response = new Message(MessageType.SEARCH_PRODUCTS_SUCCESS, StatusCode.SUCCESS, products, "搜索成功");
        sendMessage(response);
    } catch (Exception e) {
        sendErrorMessage("搜索商品失败: " + e.getMessage());
    }
}

private void handleAddProduct(Message request) {
    try {
        ProductVO product = (ProductVO) request.getData();
        server.service.StoreService storeService = new server.dao.impl.StoreServiceImpl();
        boolean success = storeService.addProduct(product);
        Message response = new Message(
                success ? MessageType.ADD_PRODUCT_SUCCESS : MessageType.ADD_PRODUCT_FAIL,
                success ? StatusCode.SUCCESS : StatusCode.BAD_REQUEST,
                null,
                success ? "新增商品成功" : "新增商品失败"
        );
        sendMessage(response);
    } catch (Exception e) {
        sendErrorMessage("新增商品失败: " + e.getMessage());
    }
}

private void handleUpdateProduct(Message request) {
    try {
        ProductVO product = (ProductVO) request.getData();
        server.service.StoreService storeService = new server.dao.impl.StoreServiceImpl();
        boolean success = storeService.updateProduct(product);
        Message response = new Message(
                success ? MessageType.UPDATE_PRODUCT_SUCCESS : MessageType.UPDATE_PRODUCT_FAIL,
                success ? StatusCode.SUCCESS : StatusCode.BAD_REQUEST,
                null,
                success ? "更新商品成功" : "更新商品失败"
        );
        sendMessage(response);
    } catch (Exception e) {
        sendErrorMessage("更新商品失败: " + e.getMessage());
    }
}

private void handleDeleteProduct(Message request) {
    try {
        Integer productId = (Integer) request.getData();
        server.service.StoreService storeService = new server.dao.impl.StoreServiceImpl();
        boolean success = storeService.deleteProduct(productId);
        Message response = new Message(
                success ? MessageType.DELETE_PRODUCT_SUCCESS : MessageType.DELETE_PRODUCT_FAIL,
                success ? StatusCode.SUCCESS : StatusCode.BAD_REQUEST,
                null,
                success ? "删除商品成功" : "删除商品失败"
        );
        sendMessage(response);
    } catch (Exception e) {
        sendErrorMessage("删除商品失败: " + e.getMessage());
    }
}

private void handleGetShoppingCart(Message request) {
    try {
        Integer userId = (Integer) request.getData();
        server.service.StoreService storeService = new server.dao.impl.StoreServiceImpl();
        List<ShoppingCartItemVO> cart = storeService.getShoppingCart(userId);
        Message response = new Message(MessageType.GET_SHOPPING_CART_SUCCESS, StatusCode.SUCCESS, cart, "获取购物车成功");
        sendMessage(response);
    } catch (Exception e) {
        sendErrorMessage("获取购物车失败: " + e.getMessage());
    }
}

private void handleAddToCart(Message request) {
    try {
        Object[] params = (Object[]) request.getData();
        Integer userId = (Integer) params[0];
        Integer productId = (Integer) params[1];
        int quantity = (int) params[2];
        server.service.StoreService storeService = new server.dao.impl.StoreServiceImpl();
        boolean success = storeService.addToCart(userId, productId, quantity);
        Message response = new Message(
                success ? MessageType.ADD_TO_CART_SUCCESS : MessageType.ADD_TO_CART_FAIL,
                success ? StatusCode.SUCCESS : StatusCode.BAD_REQUEST,
                null,
                success ? "加入购物车成功" : "加入购物车失败"
        );
        sendMessage(response);
    } catch (Exception e) {
        sendErrorMessage("加入购物车失败: " + e.getMessage());
    }
}

private void handleUpdateCartItem(Message request) {
    try {
        Object[] params = (Object[]) request.getData();
        Integer userId = (Integer) params[0];
        Integer productId = (Integer) params[1];
        int newQuantity = (int) params[2];
        server.service.StoreService storeService = new server.dao.impl.StoreServiceImpl();
        boolean success = storeService.updateCartItem(userId, productId, newQuantity);
        Message response = new Message(
                success ? MessageType.UPDATE_CART_ITEM_SUCCESS : MessageType.UPDATE_CART_ITEM_FAIL,
                success ? StatusCode.SUCCESS : StatusCode.BAD_REQUEST,
                null,
                success ? "更新购物车成功" : "更新购物车失败"
        );
        sendMessage(response);
    } catch (Exception e) {
        sendErrorMessage("更新购物车失败: " + e.getMessage());
    }
}

private void handleRemoveFromCart(Message request) {
    try {
        Object[] params = (Object[]) request.getData();
        Integer userId = (Integer) params[0];
        Integer productId = (Integer) params[1];
        server.service.StoreService storeService = new server.dao.impl.StoreServiceImpl();
        boolean success = storeService.removeFromCart(userId, productId);
        Message response = new Message(
                success ? MessageType.REMOVE_FROM_CART_SUCCESS : MessageType.REMOVE_FROM_CART_FAIL,
                success ? StatusCode.SUCCESS : StatusCode.BAD_REQUEST,
                null,
                success ? "移除购物车商品成功" : "移除失败"
        );
        sendMessage(response);
    } catch (Exception e) {
        sendErrorMessage("移除购物车商品失败: " + e.getMessage());
    }
}

private void handleClearCart(Message request) {
    try {
        Integer userId = (Integer) request.getData();
        server.service.StoreService storeService = new server.dao.impl.StoreServiceImpl();
        boolean success = storeService.clearCart(userId);
        Message response = new Message(
                success ? MessageType.CLEAR_CART_SUCCESS : MessageType.CLEAR_CART_FAIL,
                success ? StatusCode.SUCCESS : StatusCode.BAD_REQUEST,
                null,
                success ? "清空购物车成功" : "清空购物车失败"
        );
        sendMessage(response);
    } catch (Exception e) {
        sendErrorMessage("清空购物车失败: " + e.getMessage());
    }
}

private void handlePlaceOrder(Message request) {
    try {
        Map<String, Object> params = (Map<String, Object>) request.getData();
        Integer userId = (Integer) params.get("userId");
        @SuppressWarnings("unchecked")
        List<Integer> productIds = (List<Integer>) params.get("productIds");
        @SuppressWarnings("unchecked")
        List<Integer> quantities = (List<Integer>) params.get("quantities");
        server.service.StoreService storeService = new server.dao.impl.StoreServiceImpl();
        OrderVO order = storeService.createOrder(userId, productIds, quantities);
        boolean success = order != null;
        Message response = new Message(
                success ? MessageType.CREATE_ORDER_SUCCESS : MessageType.CREATE_ORDER_FAIL,
                success ? StatusCode.SUCCESS : StatusCode.BAD_REQUEST,
                order,
                success ? "下单成功" : "下单失败"
        );
        sendMessage(response);
    } catch (Exception e) {
        sendErrorMessage("下单失败: " + e.getMessage());
    }
}

private void handleGetOrdersByUser(Message request) {
    try {
        Integer userId = (Integer) request.getData();
        server.service.StoreService storeService = new server.dao.impl.StoreServiceImpl();
        List<OrderItemVO> orders = storeService.getUserOrderHistory(userId);
        Message response = new Message(MessageType.GET_USER_ORDER_HISTORY_SUCCESS, StatusCode.SUCCESS, orders, "获取订单历史成功");
        sendMessage(response);
    } catch (Exception e) {
        sendErrorMessage("获取订单历史失败: " + e.getMessage());
    }
}

private void handleGetAllOrders(Message request) {
    try {
        server.service.StoreService storeService = new server.dao.impl.StoreServiceImpl();
        List<OrderVO> orders = storeService.getAllUserOrders();
        Message response = new Message(MessageType.GET_ALL_USER_ORDERS_SUCCESS, StatusCode.SUCCESS, orders, "获取全部订单成功");
        sendMessage(response);
    } catch (Exception e) {
        sendErrorMessage("获取全部订单失败: " + e.getMessage());
    }
}

private void handleCancelOrder(Message request) {
    try {
        Integer orderId = (Integer) request.getData();
        server.service.StoreService storeService = new server.dao.impl.StoreServiceImpl();
        boolean success = storeService.cancelOrder(orderId, currentUserId);
        Message response = new Message(
                success ? MessageType.CANCEL_ORDER_SUCCESS : MessageType.CANCEL_ORDER_FAIL,
                success ? StatusCode.SUCCESS : StatusCode.BAD_REQUEST,
                null,
                success ? "订单已取消" : "取消订单失败"
        );
        sendMessage(response);
    } catch (Exception e) {
        sendErrorMessage("取消订单失败: " + e.getMessage());
    }
}

private void handleGetUserBalance(Message request) {
    try {
        Integer userId = (Integer) request.getData();
        server.service.StoreService storeService = new server.dao.impl.StoreServiceImpl();
        Double balance = storeService.getUserBalance(userId);
        Message response = new Message(
                MessageType.GET_USER_BALANCE_SUCCESS,
                StatusCode.SUCCESS,
                balance,
                "获取余额成功"
        );
        sendMessage(response);
    } catch (Exception e) {
        sendErrorMessage("获取余额失败: " + e.getMessage());
    }
}

private void handleRechargeBalance(Message request) {
    try {
        Object[] params = (Object[]) request.getData();
        Integer userId = (Integer) params[0];
        Double amount = (Double) params[1];
        server.service.StoreService storeService = new server.dao.impl.StoreServiceImpl();
        boolean success = storeService.rechargeBalance(userId, amount);
        Message response = new Message(
                success ? MessageType.RECHARGE_BALANCE_SUCCESS : MessageType.RECHARGE_BALANCE_FAIL,
                success ? StatusCode.SUCCESS : StatusCode.BAD_REQUEST,
                null,
                success ? "充值成功" : "充值失败"
        );
        sendMessage(response);
    } catch (Exception e) {
        sendErrorMessage("充值失败: " + e.getMessage());
    }
}

private void handlePayWithBalance(Message request) {
    try {
        Object[] params = (Object[]) request.getData();
        Integer userId = (Integer) params[0];
        Double amount = (Double) params[1];
        server.service.StoreService storeService = new server.dao.impl.StoreServiceImpl();
        boolean success = storeService.payWithBalance(userId, amount.intValue());
        Message response = new Message(
                success ? MessageType.PAY_WITH_BALANCE_SUCCESS : MessageType.PAY_WITH_BALANCE_FAIL,
                success ? StatusCode.SUCCESS : StatusCode.BAD_REQUEST,
                null,
                success ? "支付成功" : "支付失败，余额不足"
        );
        sendMessage(response);
    } catch (Exception e) {
        sendErrorMessage("支付失败: " + e.getMessage());
    }
}

private void handleGetProductById(Message request) {
    try {
        Integer productId = (Integer) request.getData();
        server.service.StoreService storeService = new server.dao.impl.StoreServiceImpl();
        ProductVO product = storeService.getProductById(productId);
        Message response;
        if (product != null) {
            response = new Message(MessageType.GET_PRODUCT_BY_ID_SUCCESS, StatusCode.SUCCESS, product, "查询成功");
        } else {
            response = new Message(MessageType.GET_PRODUCT_BY_ID_FAIL, StatusCode.NOT_FOUND, null, "未找到商品");
        }
        sendMessage(response);
    } catch (Exception e) {
        sendErrorMessage("查询商品失败: " + e.getMessage());
    }
}

private void handleAdjustStock(Message request) {
    try {
        Object[] params = (Object[]) request.getData();
        Integer productId = (Integer) params[0];
        int quantityDelta = (int) params[1];
        server.service.StoreService storeService = new server.dao.impl.StoreServiceImpl();
        boolean success = storeService.adjustStock(productId, quantityDelta);
        Message response = new Message(
                success ? MessageType.ADJUST_STOCK_SUCCESS : MessageType.ADJUST_STOCK_FAIL,
                success ? StatusCode.SUCCESS : StatusCode.BAD_REQUEST,
                null,
                success ? "库存调整成功" : "库存调整失败"
        );
        sendMessage(response);
    } catch (Exception e) {
        sendErrorMessage("库存调整失败: " + e.getMessage());
    }
}

private void handleGetOrderItems(Message request) {
    try {
        Object[] params = (Object[]) request.getData();
        Integer orderId = (Integer) params[0];
        Integer userId = (Integer) params[1];
        server.service.StoreService storeService = new server.dao.impl.StoreServiceImpl();
        List<OrderItemVO> items = storeService.getOrderItems(orderId, userId);
        Message response = new Message(MessageType.GET_ORDER_ITEMS_SUCCESS, StatusCode.SUCCESS, items, "获取订单项成功");
        sendMessage(response);
    } catch (Exception e) {
        sendErrorMessage("获取订单项失败: " + e.getMessage());
    }
}

private void handleGetAdminOrderDetail(Message request) {
    try {
        Integer orderId = (Integer) request.getData();
        server.service.StoreService storeService = new server.dao.impl.StoreServiceImpl();
        OrderVO order = storeService.getAdminOrderDetail(orderId);
        Message response;
        if (order != null) {
            response = new Message(MessageType.GET_ADMIN_ORDER_DETAIL_SUCCESS, StatusCode.SUCCESS, order, "获取订单详情成功");
        } else {
            response = new Message(MessageType.GET_ADMIN_ORDER_DETAIL_FAIL, StatusCode.NOT_FOUND, null, "订单不存在");
        }
        sendMessage(response);
    } catch (Exception e) {
        sendErrorMessage("获取订单详情失败: " + e.getMessage());
    }
}

private void handlePayOrder(Message request) {
    try {
        Integer orderId = (Integer) request.getData();
        server.service.StoreService storeService = new server.dao.impl.StoreServiceImpl();
        boolean success = storeService.payOrder(orderId);
        Message response = new Message(
                success ? MessageType.PAY_ORDER_SUCCESS : MessageType.PAY_ORDER_FAIL,
                success ? StatusCode.SUCCESS : StatusCode.BAD_REQUEST,
                null,
                success ? "支付成功" : "支付失败"
        );
        sendMessage(response);
    } catch (Exception e) {
        sendErrorMessage("支付失败: " + e.getMessage());
    }
}

private void handleGetAllUserOrders(Message request) {
    try {
        server.service.StoreService storeService = new server.dao.impl.StoreServiceImpl();
        List<OrderVO> orders = storeService.getAllUserOrders();
        Message response = new Message(
                MessageType.GET_ALL_USER_ORDERS_SUCCESS,
                StatusCode.SUCCESS,
                orders,
                "获取所有用户订单成功"
        );
        sendMessage(response);
    } catch (Exception e) {
        sendErrorMessage("获取所有用户订单失败: " + e.getMessage());
    }
}

private void handleShipOrder(Message request) {
    try {
        Integer orderId = (Integer) request.getData();
        server.service.StoreService storeService = new server.dao.impl.StoreServiceImpl();
        boolean success = storeService.shipOrder(orderId);
        Message response = new Message(
                success ? MessageType.SHIP_ORDER_SUCCESS : MessageType.SHIP_ORDER_FAIL,
                success ? StatusCode.SUCCESS : StatusCode.BAD_REQUEST,
                null,
                success ? "发货成功" : "发货失败"
        );
        sendMessage(response);
    } catch (Exception e) {
        sendErrorMessage("发货失败: " + e.getMessage());
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
            
            // 先获取点赞前的数量用于对比
            int beforeLikeCount = forumService.getThreadLikeCount(threadId);
            System.out.println("[Forum][Server] 点赞前数量: " + beforeLikeCount + " for threadId=" + threadId);
            
            Boolean result = forumService.toggleThreadLike(threadId, currentUserId);
            if (result != null) {
                // 返回点赞结果和新的点赞数量
                int newLikeCount = forumService.getThreadLikeCount(threadId);
                System.out.println("[Forum][Server] 点赞后数量: " + newLikeCount + " for threadId=" + threadId);
                
                // 验证数量变化是否符合预期
                int expectedChange = result ? 1 : -1;
                int actualChange = newLikeCount - beforeLikeCount;
                System.out.println("[Forum][Server] 预期变化: " + expectedChange + ", 实际变化: " + actualChange);
                
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
        
        try {
            // 获取学生详细信息
            server.service.StudentService studentService = new server.service.StudentService();
            StudentVO student = studentService.getStudentByUserId(currentUserId);
            
            if (student != null) {
                // 设置用户信息
                student.setUserInfo(currentUser);
                
                Message response = new Message(MessageType.GET_STUDENT_INFO_SUCCESS, StatusCode.SUCCESS, student, "获取学生信息成功");
                sendMessage(response);
            } else {
                Message response = new Message(MessageType.GET_STUDENT_INFO_FAIL, StatusCode.NOT_FOUND, null, "学生信息不存在");
                sendMessage(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorMessage("服务器内部错误：" + e.getMessage());
        }
    }
    
    /**
     * 处理更新学生信息请求
     */
    private void handleUpdateStudent(Message request) {
        if (!isLoggedIn()) {
            sendUnauthorizedMessage();
            return;
        }
        
        // 检查当前用户是否为学生
        if (!currentUser.isStudent()) {
            sendErrorMessage("只有学生用户才能更新学生信息");
            return;
        }
        
        if (request.getData() instanceof StudentVO) {
            StudentVO updateStudent = (StudentVO) request.getData();
            
            // 安全检查：只能更新自己的信息
            if (!currentUserId.equals(updateStudent.getUserId())) {
                Message response = new Message(MessageType.UPDATE_STUDENT_FAIL, StatusCode.FORBIDDEN, null, "无权限修改他人信息");
                sendMessage(response);
                return;
            }
            
            // 如果没有设置userId，则设置为当前用户的ID
            if (updateStudent.getUserId() == null) {
                updateStudent.setUserId(currentUserId);
            }
            
            // 更新学生信息
            server.service.StudentService studentService = new server.service.StudentService();
            boolean success = studentService.updateStudent(updateStudent);
            
            if (success) {
                Message response = new Message(MessageType.UPDATE_STUDENT_SUCCESS, StatusCode.SUCCESS, null, "更新成功");
                sendMessage(response);
            } else {
                Message response = new Message(MessageType.UPDATE_STUDENT_FAIL, StatusCode.INTERNAL_ERROR, null, "更新失败");
                sendMessage(response);
            }
        } else {
            sendErrorMessage("更新数据格式错误");
        }
    }
    
    private void handleGetTeacherInfo(Message request) {
        System.out.println("[DEBUG][ClientHandler] ========== 收到GET_TEACHER_INFO_REQUEST请求 ==========");
        System.out.println("[DEBUG][ClientHandler] 请求数据：" + request.getData());
        
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
            System.out.println("[DEBUG][ClientHandler] 创建TeacherService实例");
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
        System.out.println("[DEBUG][ClientHandler] ========== GET_TEACHER_INFO_REQUEST处理完成 ==========");
    }
    
    /**
     * 处理更新教师信息请求
     */
    private void handleUpdateTeacher(Message request) {
        System.out.println("[DEBUG][ClientHandler] 收到UPDATE_TEACHER_REQUEST请求");
        
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
            sendErrorMessage("只有教师用户才能更新教师信息");
            return;
        }
        
        if (request.getData() instanceof TeacherVO) {
            TeacherVO updateTeacher = (TeacherVO) request.getData();
            System.out.println("[DEBUG][ClientHandler] 收到教师更新数据：teacherId=" + updateTeacher.getId() + ", userId=" + updateTeacher.getUserId());
            System.out.println("[DEBUG][ClientHandler] 更新字段：name=" + updateTeacher.getName() + ", phone=" + updateTeacher.getPhone() + ", email=" + updateTeacher.getEmail());
            
            // 安全检查：只能更新自己的信息
            if (!currentUserId.equals(updateTeacher.getUserId())) {
                System.err.println("[DEBUG][ClientHandler] 权限检查失败：当前用户ID=" + currentUserId + ", 更新用户ID=" + updateTeacher.getUserId());
                Message response = new Message(MessageType.UPDATE_TEACHER_FAIL, StatusCode.FORBIDDEN, null, "无权限修改他人信息");
                sendMessage(response);
                return;
            }
            
            // 如果没有设置userId，则设置为当前用户的ID
            if (updateTeacher.getUserId() == null) {
                updateTeacher.setUserId(currentUserId);
                System.out.println("[DEBUG][ClientHandler] 设置userId为当前用户ID：" + currentUserId);
            }
            
            // 更新教师信息
            server.service.TeacherService teacherService = new server.service.TeacherService();
            System.out.println("[DEBUG][ClientHandler] 开始调用TeacherService.updateTeacher");
            boolean success = teacherService.updateTeacher(updateTeacher);
            System.out.println("[DEBUG][ClientHandler] TeacherService.updateTeacher返回结果：" + success);
            
            if (success) {
                Message response = new Message(MessageType.UPDATE_TEACHER_SUCCESS, StatusCode.SUCCESS, null, "更新成功");
                sendMessage(response);
                System.out.println("[DEBUG][ClientHandler] 发送成功响应");
            } else {
                Message response = new Message(MessageType.UPDATE_TEACHER_FAIL, StatusCode.INTERNAL_ERROR, null, "更新失败");
                sendMessage(response);
                System.err.println("[DEBUG][ClientHandler] 发送失败响应");
            }
        } else {
            sendErrorMessage("更新数据格式错误");
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
     * 处理头像上传请求
     */
    private void handleUploadAvatar(Message request) {
        if (!isLoggedIn()) {
            sendUnauthorizedMessage();
            return;
        }
        
        try {
            // 检查请求数据格式
            if (request.getData() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) request.getData();
                
                byte[] fileData = (byte[]) data.get("fileData");
                String fileName = (String) data.get("fileName");
                
                if (fileData == null || fileName == null) {
                    Message response = new Message(MessageType.UPLOAD_AVATAR_FAIL, StatusCode.BAD_REQUEST, null, "头像数据不能为空");
                    sendMessage(response);
                    return;
                }
                
                // 上传头像
                String avatarPath = userService.uploadAvatar(currentUserId, fileData, fileName);
                
                if (avatarPath != null) {
                    // 更新当前用户信息
                    currentUser = userService.getUserById(currentUserId);
                    
                    Message response = new Message(MessageType.UPLOAD_AVATAR_SUCCESS, StatusCode.SUCCESS, avatarPath, "头像上传成功");
                    sendMessage(response);
                } else {
                    Message response = new Message(MessageType.UPLOAD_AVATAR_FAIL, StatusCode.INTERNAL_ERROR, null, "头像上传失败");
                    sendMessage(response);
                }
            } else {
                Message response = new Message(MessageType.UPLOAD_AVATAR_FAIL, StatusCode.BAD_REQUEST, null, "无效的头像数据格式");
                sendMessage(response);
            }
        } catch (Exception e) {
            System.err.println("处理头像上传请求时发生异常: " + e.getMessage());
            e.printStackTrace();
            Message response = new Message(MessageType.UPLOAD_AVATAR_FAIL, StatusCode.INTERNAL_ERROR, null, "服务器内部错误: " + e.getMessage());
            sendMessage(response);
        }
    }
    
    /**
     * 处理头像下载请求
     */
    private void handleDownloadAvatar(Message request) {
        if (!isLoggedIn()) {
            sendUnauthorizedMessage();
            return;
        }
        
        try {
            // 检查请求数据格式
            if (request.getData() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) request.getData();
                
                String avatarPath = (String) data.get("avatarPath");
                
                if (avatarPath == null || avatarPath.trim().isEmpty()) {
                    Message response = new Message(MessageType.DOWNLOAD_AVATAR_FAIL, StatusCode.BAD_REQUEST, null, "头像路径不能为空");
                    sendMessage(response);
                    return;
                }
                
                // 下载头像文件数据
                byte[] avatarData = userService.downloadAvatar(avatarPath);
                
                if (avatarData != null) {
                    // 创建响应数据
                    Map<String, Object> responseData = new HashMap<>();
                    responseData.put("avatarData", avatarData);
                    responseData.put("avatarPath", avatarPath);
                    
                    Message response = new Message(MessageType.DOWNLOAD_AVATAR_SUCCESS, StatusCode.SUCCESS, responseData, "头像下载成功");
                    sendMessage(response);
                } else {
                    Message response = new Message(MessageType.DOWNLOAD_AVATAR_FAIL, StatusCode.NOT_FOUND, null, "头像文件不存在");
                    sendMessage(response);
                }
            } else {
                Message response = new Message(MessageType.DOWNLOAD_AVATAR_FAIL, StatusCode.BAD_REQUEST, null, "无效的头像下载数据格式");
                sendMessage(response);
            }
        } catch (Exception e) {
            System.err.println("处理头像下载请求时发生异常: " + e.getMessage());
            e.printStackTrace();
            Message response = new Message(MessageType.DOWNLOAD_AVATAR_FAIL, StatusCode.INTERNAL_ERROR, null, "服务器内部错误: " + e.getMessage());
            sendMessage(response);
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
     * 处理密码重置请求
     */
    private void handleResetPassword(Message request) {
        if (!isLoggedIn()) {
            sendUnauthorizedMessage();
            return;
        }
        
        // 检查管理员权限
        if (!currentUser.isAdmin()) {
            Message response = new Message(MessageType.RESET_PASSWORD_FAILURE, StatusCode.FORBIDDEN, null, "需要管理员权限");
            sendMessage(response);
            return;
        }
        
        try {
            Integer userId = (Integer) request.getData();
            if (userId == null) {
                Message response = new Message(MessageType.RESET_PASSWORD_FAILURE, StatusCode.BAD_REQUEST, null, "用户ID不能为空");
                sendMessage(response);
                return;
            }
            
            System.out.println("管理员 " + currentUser.getLoginId() + " 请求重置用户 " + userId + " 的密码");
            
            boolean success = userService.resetPassword(userId);
            
            if (success) {
                Message response = new Message(MessageType.RESET_PASSWORD_SUCCESS, StatusCode.SUCCESS, null, "密码重置成功");
                sendMessage(response);
                System.out.println("用户 " + userId + " 密码重置成功");
            } else {
                Message response = new Message(MessageType.RESET_PASSWORD_FAILURE, StatusCode.INTERNAL_ERROR, null, "密码重置失败");
                sendMessage(response);
                System.err.println("用户 " + userId + " 密码重置失败");
            }
        } catch (Exception e) {
            System.err.println("处理密码重置请求异常: " + e.getMessage());
            e.printStackTrace();
            Message response = new Message(MessageType.RESET_PASSWORD_FAILURE, StatusCode.INTERNAL_ERROR, null, "密码重置异常: " + e.getMessage());
            sendMessage(response);
        }
    }
    
    /**
     * 处理修改密码请求
     */
    private void handleChangePassword(Message request) {
        if (!isLoggedIn()) {
            sendUnauthorizedMessage();
            return;
        }
        
        try {
            Object[] data = (Object[]) request.getData();
            if (data == null || data.length != 3) {
                Message response = new Message(MessageType.CHANGE_PASSWORD_FAILURE, StatusCode.BAD_REQUEST, null, "请求数据格式错误");
                sendMessage(response);
                return;
            }
            
            Integer userId = (Integer) data[0];
            String oldPassword = (String) data[1];
            String newPassword = (String) data[2];
            
            // 安全检查：只能修改自己的密码
            if (!currentUserId.equals(userId)) {
                Message response = new Message(MessageType.CHANGE_PASSWORD_FAILURE, StatusCode.FORBIDDEN, null, "无权限修改他人密码");
                sendMessage(response);
                return;
            }
            
            System.out.println("用户 " + currentUser.getLoginId() + " 请求修改密码");
            
            boolean success = userService.changePassword(userId, oldPassword, newPassword);
            
            if (success) {
                Message response = new Message(MessageType.CHANGE_PASSWORD_SUCCESS, StatusCode.SUCCESS, null, "密码修改成功");
                sendMessage(response);
                System.out.println("用户 " + userId + " 密码修改成功");
            } else {
                Message response = new Message(MessageType.CHANGE_PASSWORD_FAILURE, StatusCode.INTERNAL_ERROR, null, "密码修改失败，请检查当前密码是否正确");
                sendMessage(response);
                System.err.println("用户 " + userId + " 密码修改失败");
            }
        } catch (Exception e) {
            System.err.println("处理修改密码请求异常: " + e.getMessage());
            e.printStackTrace();
            Message response = new Message(MessageType.CHANGE_PASSWORD_FAILURE, StatusCode.INTERNAL_ERROR, null, "密码修改异常: " + e.getMessage());
            sendMessage(response);
        }
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
            
            // 创建响应数据，包含课程列表和冲突课程删除状态
            java.util.Map<String, Object> responseData = new java.util.HashMap<>();
            responseData.put("courses", courses);
            responseData.put("conflictClassDeleted", courseService.isConflictClassDeleted(999));
            
            Message response = new Message(MessageType.GET_ALL_COURSES_SUCCESS, StatusCode.SUCCESS, responseData, "获取课程成功");
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
    
    // ================= 成绩管理模块 =================
    
    /**
     * 处理获取所有成绩请求
     */
    private void handleGetAllGrades(Message request) {
        try {
            System.out.println("[Grade][Server] 开始处理获取所有成绩请求");
            
            List<common.vo.GradeVO> grades = gradeService.getAllGrades();
            System.out.println("[Grade][Server] 查询到 " + grades.size() + " 条成绩记录");
            
            Message response = new Message(MessageType.GET_ALL_GRADES_SUCCESS, StatusCode.SUCCESS, grades, "获取成绩列表成功");
            sendMessage(response);
            
        } catch (Exception e) {
            System.err.println("处理获取所有成绩请求时发生异常: " + e.getMessage());
            e.printStackTrace();
            Message response = new Message(MessageType.GET_ALL_GRADES_FAIL, StatusCode.INTERNAL_ERROR, null, "获取成绩列表失败: " + e.getMessage());
            sendMessage(response);
        }
    }
    
    /**
     * 处理根据学生ID获取成绩请求
     */
    private void handleGetGradesByStudent(Message request) {
        try {
            System.out.println("[Grade][Server] 开始处理根据学生ID获取成绩请求");
            
            if (request.getData() instanceof Integer) {
                Integer userId = (Integer) request.getData();
                
                // 先根据用户ID获取学生信息
                server.service.StudentService studentService = new server.service.StudentService();
                common.vo.StudentVO student = studentService.getStudentByUserId(userId);
                
                if (student == null) {
                    System.out.println("[Grade][Server] 未找到用户ID " + userId + " 对应的学生信息");
                    Message response = new Message(MessageType.GET_GRADES_BY_STUDENT_SUCCESS, StatusCode.NOT_FOUND, new java.util.ArrayList<>(), "学生信息不存在");
                    sendMessage(response);
                    return;
                }
                
                // 使用学生ID查询成绩
                Integer studentId = student.getStudentId();
                List<common.vo.GradeVO> grades = gradeService.getGradesByStudentId(studentId);
                
                System.out.println("[Grade][Server] 查询到学生 " + studentId + " (用户ID: " + userId + ") 的 " + grades.size() + " 条成绩记录");
                
                Message response = new Message(MessageType.GET_GRADES_BY_STUDENT_SUCCESS, StatusCode.SUCCESS, grades, "获取学生成绩成功");
                sendMessage(response);
            } else {
                sendErrorMessage("用户ID格式错误");
            }
            
        } catch (Exception e) {
            System.err.println("处理根据学生ID获取成绩请求时发生异常: " + e.getMessage());
            e.printStackTrace();
            sendErrorMessage("获取学生成绩失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理根据课程ID获取成绩请求
     */
    private void handleGetGradesByCourse(Message request) {
        try {
            System.out.println("[Grade][Server] 开始处理根据课程ID获取成绩请求");
            
            if (request.getData() instanceof Integer) {
                Integer courseId = (Integer) request.getData();
                List<common.vo.GradeVO> grades = gradeService.getGradesByCourseId(courseId);
                
                System.out.println("[Grade][Server] 查询到课程 " + courseId + " 的 " + grades.size() + " 条成绩记录");
                
                Message response = new Message(MessageType.GET_GRADES_BY_COURSE_SUCCESS, StatusCode.SUCCESS, grades, "获取课程成绩成功");
                sendMessage(response);
            } else {
                sendErrorMessage("课程ID格式错误");
            }
            
        } catch (Exception e) {
            System.err.println("处理根据课程ID获取成绩请求时发生异常: " + e.getMessage());
            e.printStackTrace();
            sendErrorMessage("获取课程成绩失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理添加成绩请求
     */
    private void handleAddGrade(Message request) {
        try {
            System.out.println("[Grade][Server] 开始处理添加成绩请求");
            
            if (request.getData() instanceof common.vo.GradeVO) {
                common.vo.GradeVO grade = (common.vo.GradeVO) request.getData();
                boolean success = gradeService.addGrade(grade);
                
                if (success) {
                    System.out.println("[Grade][Server] 添加成绩成功");
                    Message response = new Message(MessageType.ADD_GRADE_SUCCESS, StatusCode.SUCCESS, grade, "添加成绩成功");
                    sendMessage(response);
                } else {
                    System.out.println("[Grade][Server] 添加成绩失败");
                    Message response = new Message(MessageType.ADD_GRADE_FAIL, StatusCode.BAD_REQUEST, null, "添加成绩失败");
                    sendMessage(response);
                }
            } else {
                sendErrorMessage("成绩数据格式错误");
            }
            
        } catch (Exception e) {
            System.err.println("处理添加成绩请求时发生异常: " + e.getMessage());
            e.printStackTrace();
            Message response = new Message(MessageType.ADD_GRADE_FAIL, StatusCode.INTERNAL_ERROR, null, "添加成绩失败: " + e.getMessage());
            sendMessage(response);
        }
    }
    
    /**
     * 处理更新成绩请求
     */
    private void handleUpdateGrade(Message request) {
        try {
            System.out.println("[Grade][Server] 开始处理更新成绩请求");
            
            if (request.getData() instanceof common.vo.GradeVO) {
                common.vo.GradeVO grade = (common.vo.GradeVO) request.getData();
                boolean success = gradeService.updateGrade(grade);
                
                if (success) {
                    System.out.println("[Grade][Server] 更新成绩成功");
                    Message response = new Message(MessageType.UPDATE_GRADE_SUCCESS, StatusCode.SUCCESS, grade, "更新成绩成功");
                    sendMessage(response);
                } else {
                    System.out.println("[Grade][Server] 更新成绩失败");
                    Message response = new Message(MessageType.UPDATE_GRADE_FAIL, StatusCode.BAD_REQUEST, null, "更新成绩失败");
                    sendMessage(response);
                }
            } else {
                sendErrorMessage("成绩数据格式错误");
            }
            
        } catch (Exception e) {
            System.err.println("处理更新成绩请求时发生异常: " + e.getMessage());
            e.printStackTrace();
            Message response = new Message(MessageType.UPDATE_GRADE_FAIL, StatusCode.INTERNAL_ERROR, null, "更新成绩失败: " + e.getMessage());
            sendMessage(response);
        }
    }
    
    /**
     * 处理删除成绩请求
     */
    private void handleDeleteGrade(Message request) {
        try {
            System.out.println("[Grade][Server] 开始处理删除成绩请求");
            
            if (request.getData() instanceof Integer) {
                Integer gradeId = (Integer) request.getData();
                boolean success = gradeService.deleteGrade(gradeId);
                
                if (success) {
                    System.out.println("[Grade][Server] 删除成绩成功");
                    Message response = new Message(MessageType.DELETE_GRADE_SUCCESS, StatusCode.SUCCESS, gradeId, "删除成绩成功");
                    sendMessage(response);
                } else {
                    System.out.println("[Grade][Server] 删除成绩失败");
                    Message response = new Message(MessageType.DELETE_GRADE_FAIL, StatusCode.BAD_REQUEST, null, "删除成绩失败");
                    sendMessage(response);
                }
            } else {
                sendErrorMessage("成绩ID格式错误");
            }
            
        } catch (Exception e) {
            System.err.println("处理删除成绩请求时发生异常: " + e.getMessage());
            e.printStackTrace();
            Message response = new Message(MessageType.DELETE_GRADE_FAIL, StatusCode.INTERNAL_ERROR, null, "删除成绩失败: " + e.getMessage());
            sendMessage(response);
        }
    }
    
    /**
     * 处理获取课程时间表请求
     */
    private void handleGetCourseSchedules(Message request) {
        try {
            System.out.println("[CourseSchedule][Server] 开始查询课程时间表");
            
            // 检查用户是否已登录
            if (currentUser == null) {
                sendErrorMessage("用户未登录");
                return;
            }
            
            server.service.CourseScheduleService courseScheduleService = new server.service.CourseScheduleService();
            java.util.List<common.vo.CourseScheduleVO> schedules = null;
            
            if (request.getData() instanceof java.util.List) {
                // 根据课程ID列表查询
                @SuppressWarnings("unchecked")
                java.util.List<Integer> courseIds = (java.util.List<Integer>) request.getData();
                System.out.println("[CourseSchedule][Server] 查询课程ID列表: " + courseIds);
                schedules = courseScheduleService.getSchedulesByCourseIds(courseIds);
                System.out.println("[CourseSchedule][Server] 查询结果数量: " + (schedules != null ? schedules.size() : 0));
            } else if (request.getData() instanceof Integer) {
                // 根据单个课程ID查询
                Integer courseId = (Integer) request.getData();
                System.out.println("[CourseSchedule][Server] 查询单个课程ID: " + courseId);
                schedules = courseScheduleService.getSchedulesByCourseId(courseId);
            } else {
                // 查询所有课程时间表
                System.out.println("[CourseSchedule][Server] 查询所有课程时间表");
                schedules = courseScheduleService.getAllSchedules();
            }
            
            System.out.println("[CourseSchedule][Server] 查询完成，返回条数=" + (schedules != null ? schedules.size() : -1));
            Message response = new Message(MessageType.GET_COURSE_SCHEDULES_SUCCESS, StatusCode.SUCCESS, schedules, "获取课程时间表成功");
            sendMessage(response);
            System.out.println("[CourseSchedule][Server] 已发送响应: GET_COURSE_SCHEDULES_SUCCESS");
        } catch (Exception e) {
            System.err.println("处理获取课程时间表请求时发生异常: " + e.getMessage());
            sendErrorMessage("获取课程时间表失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理删除冲突课程请求
     * @param request 请求消息
     */
    private void handleDeleteConflictClass(Message request) {
        try {
            System.out.println("[Course][Server] 开始处理删除冲突课程请求");
            
            // 检查用户是否已登录
            if (currentUser == null) {
                sendErrorMessage("用户未登录");
                return;
            }
            
            // 检查用户是否为管理员
            if (currentUser.getRole() != 2) {
                sendErrorMessage("只有管理员可以删除冲突课程");
                return;
            }
            
            if (request.getData() instanceof Integer) {
                Integer courseId = (Integer) request.getData();
                System.out.println("[Course][Server] 删除冲突课程ID: " + courseId);
                
                // 调用课程服务删除冲突课程
                server.service.CourseService courseService = new server.service.CourseService();
                boolean success = courseService.deleteConflictClass(courseId);
                
                if (success) {
                    System.out.println("[Course][Server] 冲突课程删除成功");
                    
                    // 发送成功响应给请求的管理员
                    Message response = new Message(MessageType.DELETE_CONFLICT_CLASS_SUCCESS, StatusCode.SUCCESS, courseId, "删除冲突课程成功");
                    sendMessage(response);
                    
                    // 广播删除消息给所有客户端
                    broadcastMessage(new Message(MessageType.DELETE_CONFLICT_CLASS_SUCCESS, StatusCode.SUCCESS, courseId, "冲突课程已被管理员删除"));
                    
                    System.out.println("[Course][Server] 冲突课程删除成功，已广播给所有客户端");
                } else {
                    sendErrorMessage("删除冲突课程失败");
                }
            } else {
                sendErrorMessage("无效的课程ID");
            }
        } catch (Exception e) {
            System.err.println("处理删除冲突课程请求时发生异常: " + e.getMessage());
            sendErrorMessage("删除冲突课程失败: " + e.getMessage());
        }
    }
    
    /**
     * 广播消息给所有客户端
     * @param message 要广播的消息
     */
    private void broadcastMessage(Message message) {
        try {
            // 通过服务器实例广播消息
            // 注意：这里需要从ClientHandler中获取服务器实例的引用
            // 由于VCampusServer没有单例模式，我们需要通过其他方式获取服务器实例
            System.out.println("广播删除冲突课程消息: " + message.getData());
            // 这里暂时只打印日志，实际的广播功能需要在VCampusServer中实现
        } catch (Exception e) {
            System.err.println("广播消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查连接是否活跃
     * @return 连接活跃返回true，已断开返回false
     */
    public boolean isConnected() {
        return isConnected && clientSocket != null && !clientSocket.isClosed();
    }
    
    /**
     * 处理获取所有学生请求（管理员功能）
     */
    private void handleGetAllStudents(Message request) {
        System.out.println("[Student][Server] 开始处理获取所有学生请求");
        
        if (!isLoggedIn()) {
            System.err.println("[Student][Server] 用户未登录，拒绝请求");
            sendUnauthorizedMessage();
            return;
        }
        
        // 检查管理员权限
        if (!currentUser.isAdmin()) {
            System.err.println("[Student][Server] 用户不是管理员，拒绝请求");
            Message response = new Message(MessageType.GET_ALL_STUDENTS_FAILURE, StatusCode.FORBIDDEN, null, "需要管理员权限");
            sendMessage(response);
            return;
        }
        
        try {
            System.out.println("[Student][Server] 管理员权限验证通过，开始查询学生数据");
            
            // 调用学生服务获取所有学生信息
            server.service.StudentService studentService = new server.service.StudentService();
            List<common.vo.StudentVO> students = studentService.getAllStudents();
            
            System.out.println("[Student][Server] 查询完成，找到 " + students.size() + " 个学生");
            
            if (students != null) {
                Message response = new Message(MessageType.GET_ALL_STUDENTS_SUCCESS, StatusCode.SUCCESS, students, "获取学生信息成功");
                sendMessage(response);
                System.out.println("[Student][Server] 成功响应已发送");
            } else {
                Message response = new Message(MessageType.GET_ALL_STUDENTS_FAILURE, StatusCode.INTERNAL_ERROR, null, "获取学生信息失败");
                sendMessage(response);
                System.err.println("[Student][Server] 学生数据为空");
            }
        } catch (Exception e) {
            System.err.println("[Student][Server] 处理获取所有学生请求时发生异常: " + e.getMessage());
            e.printStackTrace();
            Message response = new Message(MessageType.GET_ALL_STUDENTS_FAILURE, StatusCode.INTERNAL_ERROR, null, "服务器内部错误: " + e.getMessage());
            sendMessage(response);
        }
    }
    
    /**
     * 处理获取所有教师请求（管理员功能）
     */
    private void handleGetAllTeachers(Message request) {
        System.out.println("[Teacher][Server] 开始处理获取所有教师请求");
        
        if (!isLoggedIn()) {
            System.err.println("[Teacher][Server] 用户未登录，拒绝请求");
            sendUnauthorizedMessage();
            return;
        }
        
        // 检查管理员权限
        if (!currentUser.isAdmin()) {
            System.err.println("[Teacher][Server] 用户不是管理员，拒绝请求");
            Message response = new Message(MessageType.GET_ALL_TEACHERS_FAILURE, StatusCode.FORBIDDEN, null, "需要管理员权限");
            sendMessage(response);
            return;
        }
        
        try {
            System.out.println("[Teacher][Server] 管理员权限验证通过，开始查询教师数据");
            
            // 调用教师服务获取所有教师信息
            server.service.TeacherService teacherService = new server.service.TeacherService();
            List<common.vo.TeacherVO> teachers = teacherService.getAllTeachers();
            
            System.out.println("[Teacher][Server] 查询完成，找到 " + teachers.size() + " 个教师");
            
            if (teachers != null) {
                Message response = new Message(MessageType.GET_ALL_TEACHERS_SUCCESS, StatusCode.SUCCESS, teachers, "获取教师信息成功");
                sendMessage(response);
                System.out.println("[Teacher][Server] 成功响应已发送");
            } else {
                Message response = new Message(MessageType.GET_ALL_TEACHERS_FAILURE, StatusCode.INTERNAL_ERROR, null, "获取教师信息失败");
                sendMessage(response);
                System.err.println("[Teacher][Server] 教师数据为空");
            }
        } catch (Exception e) {
            System.err.println("[Teacher][Server] 处理获取所有教师请求时发生异常: " + e.getMessage());
            e.printStackTrace();
            Message response = new Message(MessageType.GET_ALL_TEACHERS_FAILURE, StatusCode.INTERNAL_ERROR, null, "服务器内部错误: " + e.getMessage());
            sendMessage(response);
        }
    }
    
    /**
     * 处理添加学生请求（管理员功能）
     */
    private void handleAddStudent(Message request) {
        System.out.println("[Student][Server] 开始处理添加学生请求");
        
        if (!isLoggedIn()) {
            System.err.println("[Student][Server] 用户未登录，拒绝请求");
            sendUnauthorizedMessage();
            return;
        }
        
        // 检查管理员权限
        if (!currentUser.isAdmin()) {
            System.err.println("[Student][Server] 用户不是管理员，拒绝请求");
            Message response = new Message(MessageType.ADD_STUDENT_FAILURE, StatusCode.FORBIDDEN, null, "需要管理员权限");
            sendMessage(response);
            return;
        }
        
        try {
            common.vo.StudentVO student = (common.vo.StudentVO) request.getData();
            if (student == null) {
                System.err.println("[Student][Server] 学生数据为空");
                Message response = new Message(MessageType.ADD_STUDENT_FAILURE, StatusCode.BAD_REQUEST, null, "学生数据不能为空");
                sendMessage(response);
                return;
            }
            
            System.out.println("[Student][Server] 管理员权限验证通过，开始添加学生: " + student.getName());
            
            // 调用学生服务添加学生
            server.service.StudentService studentService = new server.service.StudentService();
            boolean success = studentService.addStudent(student);
            
            if (success) {
                System.out.println("[Student][Server] 学生添加成功");
                Message response = new Message(MessageType.ADD_STUDENT_SUCCESS, StatusCode.SUCCESS, null, "学生添加成功");
                sendMessage(response);
            } else {
                System.err.println("[Student][Server] 学生添加失败");
                Message response = new Message(MessageType.ADD_STUDENT_FAILURE, StatusCode.INTERNAL_ERROR, null, "学生添加失败");
                sendMessage(response);
            }
        } catch (Exception e) {
            System.err.println("[Student][Server] 处理添加学生请求时发生异常: " + e.getMessage());
            e.printStackTrace();
            Message response = new Message(MessageType.ADD_STUDENT_FAILURE, StatusCode.INTERNAL_ERROR, null, "服务器内部错误: " + e.getMessage());
            sendMessage(response);
        }
    }
    
    /**
     * 处理更新学生请求（管理员功能）
     */
    private void handleUpdateStudentAdmin(Message request) {
        System.out.println("[Student][Server] 开始处理管理员更新学生请求");
        
        if (!isLoggedIn()) {
            System.err.println("[Student][Server] 用户未登录，拒绝请求");
            sendUnauthorizedMessage();
            return;
        }
        
        // 检查管理员权限
        if (!currentUser.isAdmin()) {
            System.err.println("[Student][Server] 用户不是管理员，拒绝请求");
            Message response = new Message(MessageType.UPDATE_STUDENT_FAILURE, StatusCode.FORBIDDEN, null, "需要管理员权限");
            sendMessage(response);
            return;
        }
        
        try {
            common.vo.StudentVO student = (common.vo.StudentVO) request.getData();
            if (student == null) {
                System.err.println("[Student][Server] 学生数据为空");
                Message response = new Message(MessageType.UPDATE_STUDENT_FAILURE, StatusCode.BAD_REQUEST, null, "学生数据不能为空");
                sendMessage(response);
                return;
            }
            
            System.out.println("[Student][Server] 管理员权限验证通过，开始更新学生: " + student.getName());
            
            // 调用学生服务更新学生
            server.service.StudentService studentService = new server.service.StudentService();
            boolean success = studentService.updateStudent(student);
            
            if (success) {
                System.out.println("[Student][Server] 学生更新成功");
                Message response = new Message(MessageType.UPDATE_STUDENT_SUCCESS, StatusCode.SUCCESS, null, "学生信息更新成功");
                sendMessage(response);
            } else {
                System.err.println("[Student][Server] 学生更新失败");
                Message response = new Message(MessageType.UPDATE_STUDENT_FAILURE, StatusCode.INTERNAL_ERROR, null, "学生信息更新失败");
                sendMessage(response);
            }
        } catch (Exception e) {
            System.err.println("[Student][Server] 处理更新学生请求时发生异常: " + e.getMessage());
            e.printStackTrace();
            Message response = new Message(MessageType.UPDATE_STUDENT_FAILURE, StatusCode.INTERNAL_ERROR, null, "服务器内部错误: " + e.getMessage());
            sendMessage(response);
        }
    }
    
    /**
     * 处理删除学生请求（管理员功能）
     */
    private void handleDeleteStudent(Message request) {
        System.out.println("[Student][Server] 开始处理删除学生请求");
        
        if (!isLoggedIn()) {
            System.err.println("[Student][Server] 用户未登录，拒绝请求");
            sendUnauthorizedMessage();
            return;
        }
        
        // 检查管理员权限
        if (!currentUser.isAdmin()) {
            System.err.println("[Student][Server] 用户不是管理员，拒绝请求");
            Message response = new Message(MessageType.DELETE_STUDENT_FAILURE, StatusCode.FORBIDDEN, null, "需要管理员权限");
            sendMessage(response);
            return;
        }
        
        try {
            Integer studentId = (Integer) request.getData();
            if (studentId == null) {
                System.err.println("[Student][Server] 学生ID为空");
                Message response = new Message(MessageType.DELETE_STUDENT_FAILURE, StatusCode.BAD_REQUEST, null, "学生ID不能为空");
                sendMessage(response);
                return;
            }
            
            System.out.println("[Student][Server] 管理员权限验证通过，开始删除学生ID: " + studentId);
            
            // 调用学生服务删除学生
            server.service.StudentService studentService = new server.service.StudentService();
            boolean success = studentService.deleteStudent(studentId);
            
            if (success) {
                System.out.println("[Student][Server] 学生删除成功");
                Message response = new Message(MessageType.DELETE_STUDENT_SUCCESS, StatusCode.SUCCESS, null, "学生删除成功");
                sendMessage(response);
            } else {
                System.err.println("[Student][Server] 学生删除失败");
                Message response = new Message(MessageType.DELETE_STUDENT_FAILURE, StatusCode.INTERNAL_ERROR, null, "学生删除失败");
                sendMessage(response);
            }
        } catch (Exception e) {
            System.err.println("[Student][Server] 处理删除学生请求时发生异常: " + e.getMessage());
            e.printStackTrace();
            Message response = new Message(MessageType.DELETE_STUDENT_FAILURE, StatusCode.INTERNAL_ERROR, null, "服务器内部错误: " + e.getMessage());
            sendMessage(response);
        }
    }
    
    /**
     * 处理添加教师请求（管理员功能）
     */
    private void handleAddTeacher(Message request) {
        System.out.println("[Teacher][Server] 开始处理添加教师请求");
        
        if (!isLoggedIn()) {
            System.err.println("[Teacher][Server] 用户未登录，拒绝请求");
            sendUnauthorizedMessage();
            return;
        }
        
        // 检查管理员权限
        if (!currentUser.isAdmin()) {
            System.err.println("[Teacher][Server] 用户不是管理员，拒绝请求");
            Message response = new Message(MessageType.ADD_TEACHER_FAILURE, StatusCode.FORBIDDEN, null, "需要管理员权限");
            sendMessage(response);
            return;
        }
        
        try {
            common.vo.TeacherVO teacher = (common.vo.TeacherVO) request.getData();
            if (teacher == null) {
                System.err.println("[Teacher][Server] 教师数据为空");
                Message response = new Message(MessageType.ADD_TEACHER_FAILURE, StatusCode.BAD_REQUEST, null, "教师数据不能为空");
                sendMessage(response);
                return;
            }
            
            System.out.println("[Teacher][Server] 管理员权限验证通过，开始添加教师: " + teacher.getName());
            
            // 调用教师服务添加教师
            server.service.TeacherService teacherService = new server.service.TeacherService();
            boolean success = teacherService.addTeacher(teacher);
            
            if (success) {
                System.out.println("[Teacher][Server] 教师添加成功");
                Message response = new Message(MessageType.ADD_TEACHER_SUCCESS, StatusCode.SUCCESS, null, "教师添加成功");
                sendMessage(response);
            } else {
                System.err.println("[Teacher][Server] 教师添加失败");
                Message response = new Message(MessageType.ADD_TEACHER_FAILURE, StatusCode.INTERNAL_ERROR, null, "教师添加失败");
                sendMessage(response);
            }
        } catch (Exception e) {
            System.err.println("[Teacher][Server] 处理添加教师请求时发生异常: " + e.getMessage());
            e.printStackTrace();
            Message response = new Message(MessageType.ADD_TEACHER_FAILURE, StatusCode.INTERNAL_ERROR, null, "服务器内部错误: " + e.getMessage());
            sendMessage(response);
        }
    }
    
    /**
     * 处理更新教师请求（管理员功能）
     */
    private void handleUpdateTeacherAdmin(Message request) {
        System.out.println("[Teacher][Server] 开始处理管理员更新教师请求");
        
        if (!isLoggedIn()) {
            System.err.println("[Teacher][Server] 用户未登录，拒绝请求");
            sendUnauthorizedMessage();
            return;
        }
        
        // 检查管理员权限
        if (!currentUser.isAdmin()) {
            System.err.println("[Teacher][Server] 用户不是管理员，拒绝请求");
            Message response = new Message(MessageType.UPDATE_TEACHER_FAILURE, StatusCode.FORBIDDEN, null, "需要管理员权限");
            sendMessage(response);
            return;
        }
        
        try {
            common.vo.TeacherVO teacher = (common.vo.TeacherVO) request.getData();
            if (teacher == null) {
                System.err.println("[Teacher][Server] 教师数据为空");
                Message response = new Message(MessageType.UPDATE_TEACHER_FAILURE, StatusCode.BAD_REQUEST, null, "教师数据不能为空");
                sendMessage(response);
                return;
            }
            
            System.out.println("[Teacher][Server] 管理员权限验证通过，开始更新教师: " + teacher.getName());
            
            // 调用教师服务更新教师
            server.service.TeacherService teacherService = new server.service.TeacherService();
            boolean success = teacherService.updateTeacher(teacher);
            
            if (success) {
                System.out.println("[Teacher][Server] 教师更新成功");
                Message response = new Message(MessageType.UPDATE_TEACHER_SUCCESS, StatusCode.SUCCESS, null, "教师信息更新成功");
                sendMessage(response);
            } else {
                System.err.println("[Teacher][Server] 教师更新失败");
                Message response = new Message(MessageType.UPDATE_TEACHER_FAILURE, StatusCode.INTERNAL_ERROR, null, "教师信息更新失败");
                sendMessage(response);
            }
        } catch (Exception e) {
            System.err.println("[Teacher][Server] 处理更新教师请求时发生异常: " + e.getMessage());
            e.printStackTrace();
            Message response = new Message(MessageType.UPDATE_TEACHER_FAILURE, StatusCode.INTERNAL_ERROR, null, "服务器内部错误: " + e.getMessage());
            sendMessage(response);
        }
    }
    
    /**
     * 处理删除教师请求（管理员功能）
     */
    private void handleDeleteTeacher(Message request) {
        System.out.println("[Teacher][Server] 开始处理删除教师请求");
        
        if (!isLoggedIn()) {
            System.err.println("[Teacher][Server] 用户未登录，拒绝请求");
            sendUnauthorizedMessage();
            return;
        }
        
        // 检查管理员权限
        if (!currentUser.isAdmin()) {
            System.err.println("[Teacher][Server] 用户不是管理员，拒绝请求");
            Message response = new Message(MessageType.DELETE_TEACHER_FAILURE, StatusCode.FORBIDDEN, null, "需要管理员权限");
            sendMessage(response);
            return;
        }
        
        try {
            Integer teacherId = (Integer) request.getData();
            if (teacherId == null) {
                System.err.println("[Teacher][Server] 教师ID为空");
                Message response = new Message(MessageType.DELETE_TEACHER_FAILURE, StatusCode.BAD_REQUEST, null, "教师ID不能为空");
                sendMessage(response);
                return;
            }
            
            System.out.println("[Teacher][Server] 管理员权限验证通过，开始删除教师ID: " + teacherId);
            
            // 调用教师服务删除教师
            server.service.TeacherService teacherService = new server.service.TeacherService();
            boolean success = teacherService.deleteTeacher(teacherId);
            
            if (success) {
                System.out.println("[Teacher][Server] 教师删除成功");
                Message response = new Message(MessageType.DELETE_TEACHER_SUCCESS, StatusCode.SUCCESS, null, "教师删除成功");
                sendMessage(response);
            } else {
                System.err.println("[Teacher][Server] 教师删除失败");
                Message response = new Message(MessageType.DELETE_TEACHER_FAILURE, StatusCode.INTERNAL_ERROR, null, "教师删除失败");
                sendMessage(response);
            }
        } catch (Exception e) {
            System.err.println("[Teacher][Server] 处理删除教师请求时发生异常: " + e.getMessage());
            e.printStackTrace();
            Message response = new Message(MessageType.DELETE_TEACHER_FAILURE, StatusCode.INTERNAL_ERROR, null, "服务器内部错误: " + e.getMessage());
            sendMessage(response);
        }
    }
}