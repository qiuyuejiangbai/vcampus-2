package client.ui.modules;

import com.formdev.flatlaf.FlatLightLaf;
import client.ui.dashboard.components.GradientButton;
import client.controller.LibraryController;
import common.vo.UserVO;

import javax.swing.*;
import java.awt.*;

public class LibraryMainFrameModule extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPanel;

    private final UserVO currentUser;
    private final LibraryController controller;

    // === 保存各个面板引用 ===
    private LibraryBookSearchModule libraryBookSearchModule;
    private LibraryBorrowHistoryModule libraryBorrowHistoryModule;
    private LibraryBookManageModule libraryBookManageModule;
    private LibraryBookAddModule libraryBookAddModule;

    public LibraryMainFrameModule(UserVO currentUser) {
        this.currentUser = currentUser;
        this.controller = new LibraryController(currentUser.getUserId()); // 统一创建一个实例
        initUI(currentUser);
    }

    public void initUI(UserVO user) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // 顶部菜单
        JPanel topBar = new JPanel();
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.X_AXIS));
        topBar.setPreferredSize(new Dimension(getWidth(), 50));
        topBar.setBackground(new Color(40, 40, 40));

        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);

        if (user.getRole() == 0 || user.getRole() == 1) {
            // === 普通用户 ===
            GradientButton btnSearch = new GradientButton("图书检索");
            GradientButton btnHistory = new GradientButton("借阅记录");

            // 初始化面板并保存引用
            libraryBookSearchModule = new LibraryBookSearchModule(controller, currentUser);
            libraryBorrowHistoryModule = new LibraryBorrowHistoryModule(user.getUserId());

            contentPanel.add(libraryBookSearchModule, "search");
            contentPanel.add(libraryBorrowHistoryModule, "history");

            btnSearch.addActionListener(e -> {
                libraryBookSearchModule.refreshTable(); // ✅ 自动刷新
                cardLayout.show(contentPanel, "search");
                btnSearch.setActive(true);
                btnHistory.setActive(false);
            });

            btnHistory.addActionListener(e -> {
                libraryBorrowHistoryModule.refreshTable(); // ✅ 自动刷新
                cardLayout.show(contentPanel, "history");
                btnHistory.setActive(true);
                btnSearch.setActive(false);
            });

            topBar.add(btnSearch);
            topBar.add(Box.createHorizontalStrut(10));
            topBar.add(btnHistory);

            btnSearch.setActive(true);

        } else if (user.getRole() == 2) {
            // === 管理员 ===
            GradientButton btnManage = new GradientButton("图书管理");
            GradientButton btnAdd = new GradientButton("新增书籍");

            // 初始化面板并保存引用
            libraryBookManageModule = new LibraryBookManageModule(controller);
            libraryBookAddModule = new LibraryBookAddModule(controller);

            contentPanel.add(libraryBookManageModule, "manage");
            contentPanel.add(libraryBookAddModule, "add");

            btnManage.addActionListener(e -> {
                libraryBookManageModule.refreshTable(); // ✅ 自动刷新
                cardLayout.show(contentPanel, "manage");
                btnManage.setActive(true);
                btnAdd.setActive(false);
            });

            btnAdd.addActionListener(e -> {
                cardLayout.show(contentPanel, "add");
                btnAdd.setActive(true);
                btnManage.setActive(false);
            });

            topBar.add(btnManage);
            topBar.add(Box.createHorizontalStrut(10));
            topBar.add(btnAdd);

            btnManage.setActive(true);
        }

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topBar, BorderLayout.NORTH);
        getContentPane().add(contentPanel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 测试：role=0 普通用户，role=2 管理员
        SwingUtilities.invokeLater(() ->
                new LibraryMainFrameModule(new UserVO(5, "name", "pass", 2)).setVisible(true));
    }
}
