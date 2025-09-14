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
import server.dao.impl.StoreServiceImpl;
import common.vo.ProductVO;
import common.vo.ShoppingCartItemVO;
import common.vo.OrderVO;
import common.vo.OrderItemVO;
import java.util.Map;

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
    
    /**
     * 检查连接是否活跃
     * @return 连接活跃返回true，已断开返回false
     */
    public boolean isConnected() {
        return isConnected && clientSocket != null && !clientSocket.isClosed();
    }
}
