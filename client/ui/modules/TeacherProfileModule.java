package client.ui.modules;

import client.ui.api.IModuleView;
import client.ui.integration.ModuleKeys;
import client.controller.TeacherController;
import common.vo.TeacherVO;
import common.vo.UserVO;
import client.ui.dashboard.components.CircularAvatar;
import client.ui.util.AnimationUtil;
import client.ui.util.ForumStyleConstants;
import client.ui.util.AvatarManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.SwingUtilities;
import java.awt.*;

/**
 * 教师个人信息管理模块
 * 实现教师个人档案的查询和修改功能
 */
public class TeacherProfileModule implements IModuleView, client.ui.dashboard.layout.SideNav.AvatarUpdateListener {
    private JPanel root;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // 当前用户和连接
    private UserVO currentUser;

    // 控制器
    private TeacherController teacherController;

    // 数据
    private TeacherVO currentTeacher;

    // 查看模式组件
    private JPanel viewPanel;
    private CircularAvatar avatarLabel;
    private InfoLabel nameLabel;
    private InfoLabel teacherNoLabel;
    private InfoLabel phoneLabel;
    private InfoLabel emailLabel;
    private InfoLabel departmentLabel;
    private InfoLabel titleLabel;
    private InfoLabel officeLabel;
    private InfoLabel researchAreaLabel;
    private InfoLabel balanceLabel;
    private JButton editButton;

    // 编辑模式组件
    private JPanel editPanel;
    private JTextField nameField;
    private JTextField teacherNoField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField departmentField;
    private JTextField titleField;
    private JTextField officeField;
    private JTextArea researchAreaArea;
    private JTextField balanceField;
    private JButton saveButton;
    private JButton cancelButton;

    // 状态标签
    private JLabel statusLabel;

    public TeacherProfileModule() {
        this.teacherController = new TeacherController();
        buildUI();
    }
    
    /**
     * 设置共享的TeacherController实例（避免监听器冲突）
     */
    public void setTeacherController(TeacherController teacherController) {
        this.teacherController = teacherController;
        System.out.println("[DEBUG][TeacherProfileModule] 已设置共享的TeacherController实例");
    }

    private void buildUI() {
        root = new JPanel(new BorderLayout());
        root.setBackground(ForumStyleConstants.BACKGROUND_WHITE);

        // 创建卡片布局
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 创建查看面板
        createViewPanel();

        // 创建编辑面板
        createEditPanel();

        // 创建状态栏
        createStatusBar();

        mainPanel.add(viewPanel, "view");
        mainPanel.add(editPanel, "edit");

        root.add(mainPanel, BorderLayout.CENTER);
        root.add(statusLabel, BorderLayout.SOUTH);

        // 默认显示查看模式
        cardLayout.show(mainPanel, "view");
    }

    private void createViewPanel() {
        viewPanel = new JPanel(new BorderLayout());
        viewPanel.setBackground(ForumStyleConstants.BACKGROUND_WHITE);
        viewPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // 创建主卡片容器
        JPanel cardContainer = createCardContainer();

        viewPanel.add(cardContainer, BorderLayout.CENTER);
    }

    private JPanel createCardContainer() {
        // 简化的卡片容器，使用简单的边框阴影
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 简单的白色背景和圆角
                g2d.setColor(ForumStyleConstants.BACKGROUND_WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // 简单的边框
                g2d.setColor(ForumStyleConstants.BORDER_LIGHT);
                g2d.setStroke(new BasicStroke(1f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                
                g2d.dispose();
            }
        };

        // 设置边框和尺寸
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        card.setPreferredSize(new Dimension(900, 700));

        // 创建上半部分墨绿色区域
        JPanel topSection = createTopSection();

        // 创建下半部分白色区域
        JPanel bottomSection = createBottomSection();

        card.add(topSection, BorderLayout.NORTH);
        card.add(bottomSection, BorderLayout.CENTER);

        return card;
    }

    private JPanel createTopSection() {
        JPanel topSection = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制墨绿色背景，只圆角上半部分
                g2d.setColor(new Color(0x1B, 0x3A, 0x2A)); // 使用窗口标题栏的绿色
                g2d.fillRoundRect(0, 0, getWidth(), getHeight() + 10, 20, 20);

                g2d.dispose();
            }
        };
        topSection.setOpaque(false);
        topSection.setPreferredSize(new Dimension(0, 150)); // 四分之一高度

        // 在交接线上添加头像 - 使用绝对定位使其中心位于交界线
        JPanel avatarContainer = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                // 不绘制背景，保持透明
            }
        };
        avatarContainer.setOpaque(false);
        avatarContainer.setPreferredSize(new Dimension(0, 100)); // 增加高度以完全显示头像

        avatarLabel = new CircularAvatar(100);
        // 移除白色边框，设置为无边框
        avatarLabel.setBorder(null);
        // 设置头像位置使其中心位于交界线（向上偏移50px，这样头像中心就在交界线上）
        avatarLabel.setBounds(40, 0, 100, 100);

        avatarContainer.add(avatarLabel);
        topSection.add(avatarContainer, BorderLayout.SOUTH);

        return topSection;
    }

    private JPanel createBottomSection() {
        JPanel bottomSection = new JPanel(new BorderLayout());
        bottomSection.setBackground(ForumStyleConstants.BACKGROUND_WHITE);
        bottomSection.setBorder(new EmptyBorder(60, 40, 30, 40)); // 调整顶部留白，为头像留出足够空间

        // 创建个人信息展示区域
        JPanel infoPanel = createInfoDisplayPanel();

        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();

        bottomSection.add(infoPanel, BorderLayout.CENTER);
        bottomSection.add(buttonPanel, BorderLayout.SOUTH);

        return bottomSection;
    }

    private JPanel createInfoDisplayPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ForumStyleConstants.BACKGROUND_WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;

        // 创建信息标签，使用更大的字体
        nameLabel = createInfoLabel("姓名", "", 16);
        teacherNoLabel = createInfoLabel("工号", "", 14);
        phoneLabel = createInfoLabel("联系电话", "", 14);
        emailLabel = createInfoLabel("邮箱", "", 14);
        departmentLabel = createInfoLabel("院系", "", 14);
        titleLabel = createInfoLabel("职称", "", 14);
        officeLabel = createInfoLabel("办公室", "", 14);
        researchAreaLabel = createInfoLabel("研究方向", "", 14);
        balanceLabel = createInfoLabel("账户余额", "", 14);

        // 添加标签到面板
        int row = 0;
        addInfoRow(panel, gbc, nameLabel, teacherNoLabel, row++);
        addInfoRow(panel, gbc, phoneLabel, emailLabel, row++);
        addInfoRow(panel, gbc, departmentLabel, titleLabel, row++);
        addInfoRow(panel, gbc, officeLabel, researchAreaLabel, row++);
        
        // 账户余额单独一行
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(balanceLabel, gbc);

        return panel;
    }

    private InfoLabel createInfoLabel(String label, String value, int fontSize) {
        return new InfoLabel(label, value, fontSize);
    }
    
    // 自定义信息标签类
    private class InfoLabel extends JPanel {
        private JLabel labelComponent;
        private JLabel valueComponent;
        
        public InfoLabel(String label, String value, int fontSize) {
            setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
            setBackground(ForumStyleConstants.BACKGROUND_WHITE);

            labelComponent = new JLabel(label + ": ");
            labelComponent.setFont(new Font("微软雅黑", Font.BOLD, fontSize));
            labelComponent.setForeground(ForumStyleConstants.TEXT_SECONDARY);

            valueComponent = new JLabel(value);
            valueComponent.setFont(new Font("微软雅黑", Font.PLAIN, fontSize));
            valueComponent.setForeground(ForumStyleConstants.TEXT_SECONDARY);

            add(labelComponent);
            add(valueComponent);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
        }
        
        public void setText(String text) {
            valueComponent.setText(text);
            // 强制刷新
            revalidate();
            repaint();
        }
        
    }

    private void addInfoRow(JPanel panel, GridBagConstraints gbc, InfoLabel left, InfoLabel right, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(left, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(right, gbc);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(ForumStyleConstants.BACKGROUND_WHITE);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));

        editButton = new JButton("编辑信息");
        editButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        editButton.setPreferredSize(new Dimension(110, 40));
        editButton.setBackground(new Color(0x1B, 0x3A, 0x2A)); // 使用窗口标题栏的绿色
        editButton.setForeground(Color.WHITE);
        editButton.setBorderPainted(false);
        editButton.addActionListener(e -> switchToEditMode());

        panel.add(editButton);

        return panel;
    }

    private void createEditPanel() {
        editPanel = new JPanel(new BorderLayout());
        editPanel.setBackground(ForumStyleConstants.BACKGROUND_WHITE);
        editPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 创建标题
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(ForumStyleConstants.BACKGROUND_WHITE);
        JLabel titleLabel = new JLabel("编辑个人信息");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0x1B, 0x3A, 0x2A)); // 使用窗口标题栏的绿色
        titlePanel.add(titleLabel);

        // 创建表单面板
        JPanel formPanel = createFormPanel();

        // 创建按钮面板
        JPanel buttonPanel = createEditButtonPanel();

        editPanel.add(titlePanel, BorderLayout.NORTH);
        editPanel.add(formPanel, BorderLayout.CENTER);
        editPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ForumStyleConstants.BACKGROUND_WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // 创建可编辑的表单字段（只允许修改的字段）
        nameField = createTextField("姓名", 20);
        phoneField = createTextField("联系电话", 20);
        emailField = createTextField("邮箱", 20);
        officeField = createTextField("办公室", 20);
        researchAreaArea = createTextArea("研究方向", 3, 20);

        // 创建只读字段（不可修改的字段）
        teacherNoField = createReadOnlyTextField("工号", 20);
        departmentField = createReadOnlyTextField("院系", 20);
        titleField = createReadOnlyTextField("职称", 20);
        balanceField = createReadOnlyTextField("账户余额", 20);

        // 添加字段到面板
        int row = 0;
        addFormRow(panel, gbc, nameField, phoneField, row++);
        addFormRow(panel, gbc, emailField, officeField, row++);

        // 研究方向单独一行
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 为研究方向字段创建包装组件
        JPanel researchAreaWrapper = new JPanel(new BorderLayout());
        researchAreaWrapper.setBackground(ForumStyleConstants.BACKGROUND_WHITE);
        
        JLabel researchAreaLabel = new JLabel("研究方向:");
        researchAreaLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        researchAreaLabel.setForeground(ForumStyleConstants.TEXT_SECONDARY);
        
        JPanel researchAreaContainer = new JPanel(new BorderLayout());
        researchAreaContainer.setBackground(ForumStyleConstants.BACKGROUND_WHITE);
        researchAreaContainer.add(researchAreaLabel, BorderLayout.NORTH);
        researchAreaContainer.add(researchAreaArea, BorderLayout.CENTER);
        
        researchAreaWrapper.add(researchAreaContainer, BorderLayout.CENTER);
        panel.add(researchAreaWrapper, gbc);
        row++;

        // 添加只读字段显示
        addFormRow(panel, gbc, teacherNoField, departmentField, row++);
        addFormRow(panel, gbc, titleField, balanceField, row++);

        return panel;
    }

    private JTextField createTextField(String label, int columns) {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(ForumStyleConstants.BACKGROUND_WHITE);

        JLabel labelComponent = new JLabel(label + ":");
        labelComponent.setFont(new Font("微软雅黑", Font.BOLD, 12));
        labelComponent.setForeground(ForumStyleConstants.TEXT_SECONDARY);

        JTextField field = new JTextField(columns);
        field.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ForumStyleConstants.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        container.add(labelComponent, BorderLayout.NORTH);
        container.add(field, BorderLayout.CENTER);

        // 创建一个包装组件
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(container, BorderLayout.CENTER);

        // 将字段存储到容器中以便访问
        field.putClientProperty("wrapper", wrapper);

        return field;
    }

    private JTextField createReadOnlyTextField(String label, int columns) {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(ForumStyleConstants.BACKGROUND_WHITE);

        JLabel labelComponent = new JLabel(label + ":");
        labelComponent.setFont(new Font("微软雅黑", Font.BOLD, 12));
        labelComponent.setForeground(ForumStyleConstants.TEXT_SECONDARY);

        JTextField field = new JTextField(columns);
        field.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        field.setEditable(false); // 设置为只读
        field.setBackground(ForumStyleConstants.BACKGROUND_HOVER); // 设置只读背景色
        field.setForeground(ForumStyleConstants.TEXT_MUTED); // 设置只读文字颜色
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ForumStyleConstants.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        container.add(labelComponent, BorderLayout.NORTH);
        container.add(field, BorderLayout.CENTER);

        // 创建一个包装组件
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(container, BorderLayout.CENTER);

        // 将字段存储到容器中以便访问
        field.putClientProperty("wrapper", wrapper);

        return field;
    }

    private JTextArea createTextArea(String label, int rows, int columns) {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(ForumStyleConstants.BACKGROUND_WHITE);

        JLabel labelComponent = new JLabel(label + ":");
        labelComponent.setFont(new Font("微软雅黑", Font.BOLD, 12));
        labelComponent.setForeground(ForumStyleConstants.TEXT_SECONDARY);

        JTextArea area = new JTextArea(rows, columns);
        area.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ForumStyleConstants.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        container.add(labelComponent, BorderLayout.NORTH);
        container.add(area, BorderLayout.CENTER);

        return area;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, JComponent left, JComponent right, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // 获取包装组件（包含标签的容器）
        JPanel leftWrapper = (JPanel) left.getClientProperty("wrapper");
        if (leftWrapper != null) {
            panel.add(leftWrapper, gbc);
        } else {
            panel.add(left, gbc);
        }

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        
        // 获取包装组件（包含标签的容器）
        JPanel rightWrapper = (JPanel) right.getClientProperty("wrapper");
        if (rightWrapper != null) {
            panel.add(rightWrapper, gbc);
        } else {
            panel.add(right, gbc);
        }
    }

    private JPanel createEditButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(ForumStyleConstants.BACKGROUND_WHITE);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));

        cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        cancelButton.setPreferredSize(new Dimension(80, 35));
        cancelButton.addActionListener(e -> switchToViewMode());

        saveButton = new JButton("保存");
        saveButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        saveButton.setPreferredSize(new Dimension(80, 35));
        saveButton.setBackground(new Color(0x1B, 0x3A, 0x2A)); // 使用窗口标题栏的绿色
        saveButton.setForeground(ForumStyleConstants.BACKGROUND_WHITE);
        saveButton.addActionListener(e -> saveTeacherInfo());

        panel.add(cancelButton);
        panel.add(saveButton);

        return panel;
    }

    private void createStatusBar() {
        statusLabel = new JLabel("就绪");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(ForumStyleConstants.TEXT_SECONDARY);
        statusLabel.setBorder(new EmptyBorder(8, 20, 8, 20));
        statusLabel.setBackground(ForumStyleConstants.BACKGROUND_HOVER);
        statusLabel.setOpaque(true);
    }

    private void switchToEditMode() {
        if (currentTeacher == null) {
            showStatus("请先加载教师信息", true);
            return;
        }

        // 填充表单数据
        fillEditForm();

        // 切换到编辑模式
        cardLayout.show(mainPanel, "edit");
        showStatus("编辑模式", false);
    }

    private void switchToViewMode() {
        // 切换到查看模式
        cardLayout.show(mainPanel, "view");
        showStatus("查看模式", false);
    }

    private void fillEditForm() {
        if (currentTeacher == null) return;

        nameField.setText(currentTeacher.getName());
        teacherNoField.setText(currentTeacher.getTeacherNo());
        phoneField.setText(currentTeacher.getPhone());
        emailField.setText(currentTeacher.getEmail());
        departmentField.setText(currentTeacher.getDepartment());
        titleField.setText(currentTeacher.getTitle());
        officeField.setText(currentTeacher.getOffice());
        researchAreaArea.setText(currentTeacher.getResearchArea());
        
        // 填充账户余额
        if (currentTeacher.getBalance() != null) {
            balanceField.setText("¥" + currentTeacher.getBalance().toString());
        } else {
            balanceField.setText("¥0.00");
        }
    }

    private void refreshTeacherInfo() {
        if (currentUser == null) {
            showStatus("用户信息未初始化", true);
            return;
        }
        
        showStatus("正在加载教师信息...", false);

        teacherController.getTeacherInfo(currentUser.getUserId(), new TeacherController.GetTeacherInfoCallback() {
            @Override
            public void onSuccess(TeacherVO teacher) {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("[DEBUG][TeacherProfileModule] 教师信息加载成功回调");
                    System.out.println("  - teacher = " + (teacher != null ? "存在" : "null"));
                    if (teacher != null) {
                        System.out.println("  - teacher.getUser() = " + (teacher.getUser() != null ? "存在" : "null"));
                        if (teacher.getUser() != null) {
                        }
                    }
                    
                    currentTeacher = teacher;
                    
                    // 重要：更新当前用户对象的头像路径，确保头像信息同步
                    if (teacher != null && teacher.getUser() != null && teacher.getUser().getAvatarPath() != null) {
                        currentUser.setAvatarPath(teacher.getUser().getAvatarPath());
                        
                        // 更新头像显示
                        updateAvatarDisplay(teacher.getUser().getAvatarPath());
                    }
                    
                    updateViewDisplay();
                    showStatus("教师信息加载成功", false);
                });
            }

            @Override
            public void onFailure(String error) {
                SwingUtilities.invokeLater(() -> {
                    showStatus("加载失败: " + error, true);
                });
            }
        });
    }

    private void saveTeacherInfo() {
        System.out.println("[DEBUG][TeacherProfileModule] ========== 开始保存教师信息 ==========");
        
        if (currentTeacher == null) {
            System.err.println("[DEBUG][TeacherProfileModule] 教师信息未加载");
            showStatus("教师信息未加载", true);
            return;
        }

        System.out.println("[DEBUG][TeacherProfileModule] 当前教师信息：ID=" + currentTeacher.getId() + ", userId=" + currentTeacher.getUserId());

        // 验证必填字段（只验证可编辑的字段）
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String office = officeField.getText().trim();
        String researchArea = researchAreaArea.getText().trim();

        System.out.println("[DEBUG][TeacherProfileModule] 表单数据：");
        System.out.println("  - 姓名=" + name);
        System.out.println("  - 电话=" + phone);
        System.out.println("  - 邮箱=" + email);
        System.out.println("  - 办公室=" + office);
        System.out.println("  - 研究方向=" + researchArea);

        // 验证姓名
        if (name.isEmpty()) {
            System.err.println("[DEBUG][TeacherProfileModule] 姓名不能为空");
            JOptionPane.showMessageDialog(root, "姓名不能为空", "输入错误", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }
        
        String nameError = client.ui.util.ValidationUtil.getNameValidationError(name);
        if (nameError != null) {
            System.err.println("[DEBUG][TeacherProfileModule] 姓名格式错误: " + nameError);
            JOptionPane.showMessageDialog(root, nameError, "姓名格式错误", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        // 验证联系电话
        if (phone.isEmpty()) {
            System.err.println("[DEBUG][TeacherProfileModule] 联系电话不能为空");
            JOptionPane.showMessageDialog(root, "联系电话不能为空", "输入错误", JOptionPane.WARNING_MESSAGE);
            phoneField.requestFocus();
            return;
        }
        
        String phoneError = client.ui.util.ValidationUtil.getPhoneValidationError(phone);
        if (phoneError != null) {
            System.err.println("[DEBUG][TeacherProfileModule] 联系电话格式错误: " + phoneError);
            JOptionPane.showMessageDialog(root, phoneError, "联系电话格式错误", JOptionPane.WARNING_MESSAGE);
            phoneField.requestFocus();
            return;
        }

        // 验证邮箱
        if (email.isEmpty()) {
            System.err.println("[DEBUG][TeacherProfileModule] 邮箱不能为空");
            JOptionPane.showMessageDialog(root, "邮箱不能为空", "输入错误", JOptionPane.WARNING_MESSAGE);
            emailField.requestFocus();
            return;
        }
        
        String emailError = client.ui.util.ValidationUtil.getEmailValidationError(email);
        if (emailError != null) {
            System.err.println("[DEBUG][TeacherProfileModule] 邮箱格式错误: " + emailError);
            JOptionPane.showMessageDialog(root, emailError, "邮箱格式错误", JOptionPane.WARNING_MESSAGE);
            emailField.requestFocus();
            return;
        }

        System.out.println("[DEBUG][TeacherProfileModule] 字段验证通过，开始构建更新对象");
        showStatus("正在保存信息...", false);

        // 更新教师信息（只更新允许修改的字段）
        TeacherVO updatedTeacher = new TeacherVO();
        updatedTeacher.setId(currentTeacher.getId());
        updatedTeacher.setUserId(currentTeacher.getUserId());
        
        // 设置可修改的字段
        updatedTeacher.setName(name);
        updatedTeacher.setPhone(phone);
        updatedTeacher.setEmail(email);
        updatedTeacher.setOffice(office);
        updatedTeacher.setResearchArea(researchArea);
        
        // 设置只读字段（保持原有值）
        updatedTeacher.setTeacherNo(currentTeacher.getTeacherNo());
        updatedTeacher.setTitle(currentTeacher.getTitle());
        updatedTeacher.setDepartment(currentTeacher.getDepartment());

        System.out.println("[DEBUG][TeacherProfileModule] 构建的更新对象：");
        System.out.println("  - ID=" + updatedTeacher.getId());
        System.out.println("  - userId=" + updatedTeacher.getUserId());
        System.out.println("  - name=" + updatedTeacher.getName());
        System.out.println("  - phone=" + updatedTeacher.getPhone());
        System.out.println("  - email=" + updatedTeacher.getEmail());
        System.out.println("  - office=" + updatedTeacher.getOffice());
        System.out.println("  - researchArea=" + updatedTeacher.getResearchArea());
        System.out.println("  - teacherNo=" + updatedTeacher.getTeacherNo());
        System.out.println("  - title=" + updatedTeacher.getTitle());
        System.out.println("  - department=" + updatedTeacher.getDepartment());

        System.out.println("[DEBUG][TeacherProfileModule] 调用teacherController.updateTeacher");
        // 更新教师信息
        teacherController.updateTeacher(updatedTeacher, new TeacherController.UpdateTeacherCallback() {
            @Override
            public void onSuccess(String message) {
                System.out.println("[DEBUG][TeacherProfileModule] 更新成功回调：" + message);
                SwingUtilities.invokeLater(() -> {
                    // 刷新教师信息
                    refreshTeacherInfo();
                    switchToViewMode();
                    showStatus("保存成功", false);
                    
                    // 通知侧边栏更新头像（如果头像路径有变化）
                    notifySideNavAvatarUpdate();
                });
            }

            @Override
            public void onFailure(String error) {
                System.err.println("[DEBUG][TeacherProfileModule] 更新失败回调：" + error);
                SwingUtilities.invokeLater(() -> {
                    showStatus("保存失败: " + error, true);
                });
            }
        });
    }

    private void updateViewDisplay() {
        if (currentTeacher == null) {
            return;
        }

        try {
            nameLabel.setText(currentTeacher.getName());
            teacherNoLabel.setText(currentTeacher.getTeacherNo());
            phoneLabel.setText(currentTeacher.getPhone());
            emailLabel.setText(currentTeacher.getEmail());
            departmentLabel.setText(currentTeacher.getDepartment());
            titleLabel.setText(currentTeacher.getTitle());
            officeLabel.setText(currentTeacher.getOffice());
            researchAreaLabel.setText(currentTeacher.getResearchArea());
            
            // 显示账户余额
            if (currentTeacher.getBalance() != null) {
                balanceLabel.setText("¥" + currentTeacher.getBalance().toString());
            } else {
                balanceLabel.setText("¥0.00");
            }

            // 更新头像 - 强制从服务器获取最新头像
            System.out.println("  - currentTeacher.getUser() = " + (currentTeacher.getUser() != null ? "存在" : "null"));
            if (currentTeacher.getUser() != null) {
            }
            
            // 使用统一的头像管理器更新头像
            String avatarPath = null;
            if (currentTeacher.getUser() != null) {
                avatarPath = currentTeacher.getUser().getAvatarPath();
            }
            
            AvatarManager.updateAvatar(avatarLabel, avatarPath, currentTeacher.getName(), new AvatarManager.AvatarUpdateCallback() {
                @Override
                public void onAvatarUpdated(Image avatarImage) {
                    // 强制刷新UI
                    avatarLabel.revalidate();
                    avatarLabel.repaint();
                }
                
                @Override
                public void onUpdateFailed(String errorMessage) {
                    System.err.println("[TeacherProfileModule] 头像更新失败: " + errorMessage);
                    // 强制刷新UI
                    avatarLabel.revalidate();
                    avatarLabel.repaint();
                }
            });
            
            // 强制刷新UI
            viewPanel.revalidate();
            viewPanel.repaint();
            root.revalidate();
            root.repaint();
            
            // 强制刷新头像组件
            if (avatarLabel != null) {
                avatarLabel.revalidate();
                avatarLabel.repaint();
            }
            
            // 启动淡入动画
            startFadeInAnimation();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        if (isError) {
            statusLabel.setForeground(ForumStyleConstants.ERROR_RED);
        } else {
            statusLabel.setForeground(ForumStyleConstants.TEXT_SECONDARY);
        }
    }
    
    /**
     * 启动淡入动画效果
     */
    private void startFadeInAnimation() {
        // 头像淡入动画（延迟0ms，持续800ms）
        AnimationUtil.fadeIn(avatarLabel, 800, 0);
        
        // 信息标签淡入动画（延迟200ms，持续600ms）
        AnimationUtil.fadeIn(nameLabel, 600, 200);
        AnimationUtil.fadeIn(teacherNoLabel, 600, 250);
        AnimationUtil.fadeIn(phoneLabel, 600, 300);
        AnimationUtil.fadeIn(emailLabel, 600, 350);
        AnimationUtil.fadeIn(departmentLabel, 600, 400);
        AnimationUtil.fadeIn(titleLabel, 600, 450);
        AnimationUtil.fadeIn(officeLabel, 600, 500);
        AnimationUtil.fadeIn(researchAreaLabel, 600, 550);
        AnimationUtil.fadeIn(balanceLabel, 600, 600);
        
        // 编辑按钮淡入动画（延迟700ms，持续400ms）
        AnimationUtil.fadeIn(editButton, 400, 700);
    }
    

    @Override
    public String getKey() {
        return ModuleKeys.TEACHER_PROFILE;
    }

    @Override
    public String getDisplayName() {
        return "个人信息";
    }

    @Override
    public String getIconPath() {
        return "icons/学籍.png"; // 使用学籍图标
    }

    @Override
    public JComponent getComponent() {
        return root;
    }

    @Override
    public void initContext(UserVO currentUser, client.net.ServerConnection connection) {
        this.currentUser = currentUser;

        // 初始化时加载教师信息
        SwingUtilities.invokeLater(this::refreshTeacherInfo);
    }
    
    
    /**
     * 头像更新回调方法
     * 当SideNav中的头像更新后，会调用此方法刷新本模块的头像显示
     */
    @Override
    public void onAvatarUpdated(String avatarPath) {
        
        // 在EDT线程中更新头像显示
        SwingUtilities.invokeLater(() -> {
            try {
                // 更新当前用户对象的头像路径
                if (currentUser != null && avatarPath != null) {
                    currentUser.setAvatarPath(avatarPath);
                }
                
                // 同时更新当前教师对象的头像路径
                if (currentTeacher != null && currentTeacher.getUser() != null && avatarPath != null) {
                    currentTeacher.getUser().setAvatarPath(avatarPath);
                }
                
                // 直接更新头像显示，无论当前处于什么模式
                updateAvatarDisplay(avatarPath);
                
            } catch (Exception e) {
                System.err.println("[TeacherProfileModule] 更新头像显示失败: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 通知侧边栏更新头像
     * 通过查找父窗口中的SideNav组件来通知头像更新
     */
    private void notifySideNavAvatarUpdate() {
        try {
            // 查找父窗口中的SideNav组件
            Window parentWindow = SwingUtilities.getWindowAncestor(root);
            if (parentWindow != null) {
                client.ui.dashboard.layout.SideNav sideNav = findSideNavInWindow(parentWindow);
                if (sideNav != null && currentUser != null && currentUser.getAvatarPath() != null) {
                    sideNav.updateUserAvatarPath(currentUser.getAvatarPath());
                }
            }
        } catch (Exception e) {
            System.err.println("[TeacherProfileModule] 通知侧边栏更新头像失败: " + e.getMessage());
        }
    }
    
    /**
     * 在窗口中查找SideNav组件
     */
    private client.ui.dashboard.layout.SideNav findSideNavInWindow(Container container) {
        if (container instanceof client.ui.dashboard.layout.SideNav) {
            return (client.ui.dashboard.layout.SideNav) container;
        }
        
        for (Component component : container.getComponents()) {
            if (component instanceof Container) {
                client.ui.dashboard.layout.SideNav result = findSideNavInWindow((Container) component);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * 刷新头像显示（公开方法，供外部调用）
     */
    public void refreshAvatarDisplay() {
        
        SwingUtilities.invokeLater(() -> {
            try {
                String avatarPath = null;
                String userName = null;
                
                if (currentTeacher != null && currentTeacher.getUser() != null) {
                    avatarPath = currentTeacher.getUser().getAvatarPath();
                    userName = currentTeacher.getName();
                } else if (currentUser != null) {
                    avatarPath = currentUser.getAvatarPath();
                    userName = currentUser.getName();
                }
                
                // 使用统一的头像管理器强制刷新头像
                AvatarManager.refreshAvatar(avatarLabel, avatarPath, userName, new AvatarManager.AvatarUpdateCallback() {
                    @Override
                    public void onAvatarUpdated(Image avatarImage) {
                        // 强制刷新UI
                        avatarLabel.revalidate();
                        avatarLabel.repaint();
                    }
                    
                    @Override
                    public void onUpdateFailed(String errorMessage) {
                        System.err.println("[TeacherProfileModule] 头像刷新失败: " + errorMessage);
                        // 强制刷新UI
                        avatarLabel.revalidate();
                        avatarLabel.repaint();
                    }
                });
            } catch (Exception e) {
                System.err.println("[TeacherProfileModule] 刷新头像显示失败: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 更新头像显示
     */
    private void updateAvatarDisplay(String avatarPath) {
        if (avatarLabel == null) {
            return;
        }
        
        // 使用统一的头像管理器更新头像
        AvatarManager.updateAvatar(avatarLabel, avatarPath, currentTeacher != null ? currentTeacher.getName() : null, new AvatarManager.AvatarUpdateCallback() {
            @Override
            public void onAvatarUpdated(Image avatarImage) {
                // 强制刷新UI
                avatarLabel.revalidate();
                avatarLabel.repaint();
            }
            
            @Override
            public void onUpdateFailed(String errorMessage) {
                System.err.println("[TeacherProfileModule] 头像显示更新失败: " + errorMessage);
                // 强制刷新UI
                avatarLabel.revalidate();
                avatarLabel.repaint();
            }
        });
    }
    
}
