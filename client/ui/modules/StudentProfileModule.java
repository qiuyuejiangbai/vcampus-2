package client.ui.modules;

import client.ui.api.IModuleView;
import client.ui.integration.ModuleKeys;
import client.controller.StudentController;
import common.vo.StudentVO;
import common.vo.UserVO;
import client.ui.dashboard.components.CircularAvatar;
import client.ui.util.AnimationUtil;
import client.ui.util.ForumStyleConstants;
import client.ui.util.AvatarManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.text.SimpleDateFormat;

/**
 * 学生学籍管理模块
 * 实现学生个人档案的查询和修改功能
 */
public class StudentProfileModule implements IModuleView, client.ui.dashboard.layout.SideNav.AvatarUpdateListener {
    private JPanel root;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // 当前用户和连接
    private UserVO currentUser;

    // 控制器
    private StudentController studentController;

    // 数据
    private StudentVO currentStudent;

    // 查看模式组件
    private JPanel viewPanel;
    private CircularAvatar avatarLabel;
    private InfoLabel nameLabel;
    private InfoLabel studentNoLabel;
    private InfoLabel genderLabel;
    private InfoLabel birthDateLabel;
    private InfoLabel phoneLabel;
    private InfoLabel emailLabel;
    private InfoLabel addressLabel;
    private InfoLabel departmentLabel;
    private InfoLabel classNameLabel;
    private InfoLabel majorLabel;
    private InfoLabel gradeLabel;
    private InfoLabel enrollmentYearLabel;
    private InfoLabel balanceLabel;
    private JButton editButton;

    // 编辑模式组件
    private JPanel editPanel;
    private JTextField nameField;
    private JTextField studentNoField;
    private JComboBox<String> genderComboBox;
    private JTextField birthDateField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextArea addressArea;
    private JTextField departmentField;
    private JTextField classNameField;
    private JTextField majorField;
    private JTextField gradeField;
    private JTextField enrollmentYearField;
    private JTextField balanceField;
    private JButton saveButton;
    private JButton cancelButton;

    // 状态标签
    private JLabel statusLabel;

    public StudentProfileModule() {
        this.studentController = new StudentController();
        buildUI();
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
        studentNoLabel = createInfoLabel("学号", "", 14);
        genderLabel = createInfoLabel("性别", "", 14);
        birthDateLabel = createInfoLabel("出生日期", "", 14);
        phoneLabel = createInfoLabel("联系电话", "", 14);
        emailLabel = createInfoLabel("邮箱", "", 14);
        addressLabel = createInfoLabel("家庭地址", "", 14);
        departmentLabel = createInfoLabel("院系", "", 14);
        classNameLabel = createInfoLabel("班级", "", 14);
        majorLabel = createInfoLabel("专业", "", 14);
        gradeLabel = createInfoLabel("年级", "", 14);
        enrollmentYearLabel = createInfoLabel("入学年份", "", 14);
        balanceLabel = createInfoLabel("账户余额", "", 14);

        // 添加标签到面板
        int row = 0;
        addInfoRow(panel, gbc, nameLabel, studentNoLabel, row++);
        addInfoRow(panel, gbc, genderLabel, birthDateLabel, row++);
        addInfoRow(panel, gbc, phoneLabel, emailLabel, row++);
        addInfoRow(panel, gbc, departmentLabel, classNameLabel, row++);
        addInfoRow(panel, gbc, majorLabel, gradeLabel, row++);
        addInfoRow(panel, gbc, enrollmentYearLabel, balanceLabel, row++);

        // 地址单独一行
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(addressLabel, gbc);

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
        editButton.setForeground(ForumStyleConstants.BACKGROUND_WHITE);
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
        birthDateField = createTextField("出生日期 (yyyy-MM-dd)", 20);
        phoneField = createTextField("联系电话", 20);
        emailField = createTextField("邮箱", 20);
        addressArea = createTextArea("家庭地址", 3, 20);

        // 创建只读字段（不可修改的字段）
        studentNoField = createReadOnlyTextField("学号", 20);
        genderComboBox = createReadOnlyGenderComboBox();
        departmentField = createReadOnlyTextField("院系", 20);
        classNameField = createReadOnlyTextField("班级", 20);
        majorField = createReadOnlyTextField("专业", 20);
        gradeField = createReadOnlyTextField("年级", 20);
        enrollmentYearField = createReadOnlyTextField("入学年份", 20);
        balanceField = createReadOnlyTextField("账户余额", 20);

        // 添加字段到面板
        int row = 0;
        addFormRow(panel, gbc, nameField, birthDateField, row++);
        addFormRow(panel, gbc, phoneField, emailField, row++);

        // 地址单独一行
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 为地址字段创建包装组件
        JPanel addressWrapper = new JPanel(new BorderLayout());
        addressWrapper.setBackground(ForumStyleConstants.BACKGROUND_WHITE);
        
        JLabel addressLabel = new JLabel("家庭地址:");
        addressLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        addressLabel.setForeground(ForumStyleConstants.TEXT_SECONDARY);
        
        JPanel addressContainer = new JPanel(new BorderLayout());
        addressContainer.setBackground(ForumStyleConstants.BACKGROUND_WHITE);
        addressContainer.add(addressLabel, BorderLayout.NORTH);
        addressContainer.add(addressArea, BorderLayout.CENTER);
        
        addressWrapper.add(addressContainer, BorderLayout.CENTER);
        panel.add(addressWrapper, gbc);
        row++;

        // 添加只读字段显示
        addFormRow(panel, gbc, studentNoField, genderComboBox, row++);
        addFormRow(panel, gbc, departmentField, classNameField, row++);
        addFormRow(panel, gbc, majorField, gradeField, row++);
        addFormRow(panel, gbc, enrollmentYearField, balanceField, row++);

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



    private JComboBox<String> createReadOnlyGenderComboBox() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(ForumStyleConstants.BACKGROUND_WHITE);

        JLabel labelComponent = new JLabel("性别:");
        labelComponent.setFont(new Font("微软雅黑", Font.BOLD, 12));
        labelComponent.setForeground(ForumStyleConstants.TEXT_SECONDARY);

        JComboBox<String> comboBox = new JComboBox<>(new String[]{"男", "女"});
        comboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        comboBox.setEnabled(false); // 设置为不可编辑
        comboBox.setBackground(ForumStyleConstants.BACKGROUND_HOVER); // 设置只读背景色
        comboBox.setForeground(ForumStyleConstants.TEXT_MUTED); // 设置只读文字颜色
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ForumStyleConstants.BORDER_LIGHT),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        container.add(labelComponent, BorderLayout.NORTH);
        container.add(comboBox, BorderLayout.CENTER);

        // 创建一个包装组件
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(container, BorderLayout.CENTER);

        // 将字段存储到容器中以便访问
        comboBox.putClientProperty("wrapper", wrapper);

        return comboBox;
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
        saveButton.addActionListener(e -> saveStudentInfo());

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
        if (currentStudent == null) {
            showStatus("请先加载学生信息", true);
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
        if (currentStudent == null) return;

        nameField.setText(currentStudent.getName());
        studentNoField.setText(currentStudent.getStudentNo());
        
        // 设置性别
        if (currentStudent.getGender() != null) {
            if ("male".equals(currentStudent.getGender())) {
                genderComboBox.setSelectedItem("男");
            } else if ("female".equals(currentStudent.getGender())) {
                genderComboBox.setSelectedItem("女");
            }
        }
        
        // 设置出生日期
        if (currentStudent.getBirthDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            birthDateField.setText(sdf.format(currentStudent.getBirthDate()));
        }
        
        phoneField.setText(currentStudent.getPhone() != null ? currentStudent.getPhone() : "");
        emailField.setText(currentStudent.getEmail() != null ? currentStudent.getEmail() : "");
        addressArea.setText(currentStudent.getAddress() != null ? currentStudent.getAddress() : "");
        departmentField.setText(currentStudent.getDepartment() != null ? currentStudent.getDepartment() : "");
        classNameField.setText(currentStudent.getClassName() != null ? currentStudent.getClassName() : "");
        majorField.setText(currentStudent.getMajor() != null ? currentStudent.getMajor() : "");
        gradeField.setText(currentStudent.getGrade() != null ? currentStudent.getGrade() : "");
        
        if (currentStudent.getEnrollmentYear() != null) {
            enrollmentYearField.setText(currentStudent.getEnrollmentYear().toString());
        }
        
        if (currentStudent.getBalance() != null) {
            balanceField.setText(currentStudent.getBalance().toString());
        }
    }

    private void refreshStudentInfo() {
        if (currentUser == null) {
            showStatus("用户信息未初始化", true);
            return;
        }
        
        showStatus("正在加载学生信息...", false);

        studentController.getStudentInfo(currentUser.getUserId(), new StudentController.GetStudentInfoCallback() {
            @Override
            public void onSuccess(StudentVO student) {
                SwingUtilities.invokeLater(() -> {
                    currentStudent = student;
                    
                    // 重要：更新当前用户对象的头像路径，确保头像信息同步
                    if (student != null && student.getUser() != null && student.getUser().getAvatarPath() != null) {
                        currentUser.setAvatarPath(student.getUser().getAvatarPath());
                        
                        // 更新头像显示
                        updateAvatarDisplay(student.getUser().getAvatarPath());
                    }
                    
                    updateViewDisplay();
                    showStatus("学生信息加载成功", false);
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

    private void saveStudentInfo() {
        if (currentStudent == null) {
            showStatus("学生信息未加载", true);
            return;
        }

        // 验证必填字段（只验证可编辑的字段）
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String birthDateStr = birthDateField.getText().trim();

        // 验证姓名
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(root, "姓名不能为空", "输入错误", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }
        
        String nameError = client.ui.util.ValidationUtil.getNameValidationError(name);
        if (nameError != null) {
            JOptionPane.showMessageDialog(root, nameError, "姓名格式错误", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        // 验证联系电话
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(root, "联系电话不能为空", "输入错误", JOptionPane.WARNING_MESSAGE);
            phoneField.requestFocus();
            return;
        }
        
        String phoneError = client.ui.util.ValidationUtil.getPhoneValidationError(phone);
        if (phoneError != null) {
            JOptionPane.showMessageDialog(root, phoneError, "联系电话格式错误", JOptionPane.WARNING_MESSAGE);
            phoneField.requestFocus();
            return;
        }

        // 验证邮箱
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(root, "邮箱不能为空", "输入错误", JOptionPane.WARNING_MESSAGE);
            emailField.requestFocus();
            return;
        }
        
        String emailError = client.ui.util.ValidationUtil.getEmailValidationError(email);
        if (emailError != null) {
            JOptionPane.showMessageDialog(root, emailError, "邮箱格式错误", JOptionPane.WARNING_MESSAGE);
            emailField.requestFocus();
            return;
        }

        // 验证出生日期格式（如果填写了的话）
        if (!birthDateStr.isEmpty()) {
            String dateError = client.ui.util.ValidationUtil.getDateValidationError(birthDateStr);
            if (dateError != null) {
                JOptionPane.showMessageDialog(root, dateError, "出生日期格式错误", JOptionPane.WARNING_MESSAGE);
                birthDateField.requestFocus();
                return;
            }
        }

        showStatus("正在保存信息...", false);

        // 更新学生信息（只更新允许修改的字段）
        StudentVO updatedStudent = new StudentVO();
        updatedStudent.setStudentId(currentStudent.getStudentId());
        updatedStudent.setUserId(currentStudent.getUserId());
        
        // 设置学号（从当前学生信息中获取）
        updatedStudent.setStudentNo(currentStudent.getStudentNo());
        
        // 只设置可修改的字段
        updatedStudent.setName(name);
        
        // 设置出生日期
        if (!birthDateStr.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                java.util.Date utilDate = sdf.parse(birthDateStr);
                updatedStudent.setBirthDate(new java.sql.Date(utilDate.getTime()));
            } catch (Exception e) {
                showStatus("出生日期格式错误，请使用 yyyy-MM-dd 格式", true);
                return;
            }
        }
        
        updatedStudent.setPhone(phone);
        updatedStudent.setEmail(email);
        updatedStudent.setAddress(addressArea.getText().trim());
        
        // 重要：保留不可修改的字段，防止被清空
        updatedStudent.setDepartment(currentStudent.getDepartment());
        updatedStudent.setClassName(currentStudent.getClassName());
        updatedStudent.setMajor(currentStudent.getMajor());
        updatedStudent.setGrade(currentStudent.getGrade());
        updatedStudent.setEnrollmentYear(currentStudent.getEnrollmentYear());
        updatedStudent.setBalance(currentStudent.getBalance());
        updatedStudent.setGender(currentStudent.getGender());

        // 更新学生信息
        studentController.updateStudent(updatedStudent, new StudentController.UpdateStudentCallback() {
            @Override
            public void onSuccess(String message) {
                SwingUtilities.invokeLater(() -> {
                    // 刷新学生信息
                    refreshStudentInfo();
                    switchToViewMode();
                    showStatus("保存成功", false);
                    
                    // 通知侧边栏更新头像（如果头像路径有变化）
                    notifySideNavAvatarUpdate();
                });
            }

            @Override
            public void onFailure(String error) {
                SwingUtilities.invokeLater(() -> {
                    showStatus("保存失败: " + error, true);
                });
            }
        });
    }


    private void updateViewDisplay() {
        if (currentStudent == null) {
            return;
        }

        try {
            nameLabel.setText(currentStudent.getName());
            studentNoLabel.setText(currentStudent.getStudentNo());
            genderLabel.setText(currentStudent.getGenderName());

            if (currentStudent.getBirthDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                birthDateLabel.setText(sdf.format(currentStudent.getBirthDate()));
            } else {
                birthDateLabel.setText("未设置");
            }

            phoneLabel.setText(currentStudent.getPhone() != null ? currentStudent.getPhone() : "未设置");
            emailLabel.setText(currentStudent.getEmail() != null ? currentStudent.getEmail() : "未设置");
            addressLabel.setText(currentStudent.getAddress() != null ? currentStudent.getAddress() : "未设置");
            departmentLabel.setText(currentStudent.getDepartment() != null ? currentStudent.getDepartment() : "未设置");
            classNameLabel.setText(currentStudent.getClassName() != null ? currentStudent.getClassName() : "未设置");
            majorLabel.setText(currentStudent.getMajor() != null ? currentStudent.getMajor() : "未设置");
            gradeLabel.setText(currentStudent.getGrade() != null ? currentStudent.getGrade() : "未设置");

            if (currentStudent.getEnrollmentYear() != null) {
                enrollmentYearLabel.setText(currentStudent.getEnrollmentYear().toString());
            } else {
                enrollmentYearLabel.setText("未设置");
            }

            if (currentStudent.getBalance() != null) {
                balanceLabel.setText("¥" + currentStudent.getBalance().toString());
            } else {
                balanceLabel.setText("¥0.00");
            }

            // 更新头像 - 使用统一的头像管理器
            System.out.println("  - currentStudent.getUser() = " + (currentStudent.getUser() != null ? "存在" : "null"));
            if (currentStudent.getUser() != null) {
            }
            
            String avatarPath = null;
            if (currentStudent.getUser() != null) {
                avatarPath = currentStudent.getUser().getAvatarPath();
            }
            
            // 使用统一的头像管理器更新头像
            AvatarManager.updateAvatar(avatarLabel, avatarPath, currentStudent.getName(), new AvatarManager.AvatarUpdateCallback() {
                @Override
                public void onAvatarUpdated(Image avatarImage) {
                    // 强制刷新UI
                    avatarLabel.revalidate();
                    avatarLabel.repaint();
                }
                
                @Override
                public void onUpdateFailed(String errorMessage) {
                    System.err.println("[StudentProfileModule] 头像更新失败: " + errorMessage);
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
     * 启动淡入动画效果（优化版本）
     */
    private void startFadeInAnimation() {
        // 确保头像组件处于正确的初始状态
        avatarLabel.clearAnimationState();
        
        // 头像和文字同时开始动画，更加丝滑
        // 头像淡入动画（延迟0ms，持续600ms，稍微减慢动画速度）
        AnimationUtil.fadeIn(avatarLabel, 600, 0);
        
        // 使用批量动画优化信息标签的淡入效果
        JComponent[] infoLabels = {
            nameLabel, studentNoLabel, genderLabel, birthDateLabel,
            phoneLabel, emailLabel, addressLabel, departmentLabel,
            classNameLabel, majorLabel, gradeLabel, enrollmentYearLabel, balanceLabel
        };
        
        // 批量淡入动画：延迟0ms开始，持续600ms，每个组件间隔25ms（增加间隔，使动画更舒缓）
        AnimationUtil.fadeInBatch(infoLabels, 600, 0, 25);
        
        // 编辑按钮淡入动画（延迟400ms，持续350ms，稍微减慢动画速度）
        AnimationUtil.fadeIn(editButton, 350, 400);
    }
    

    @Override
    public String getKey() {
        return ModuleKeys.STUDENT_PROFILE;
    }

    @Override
    public String getDisplayName() {
        return "学籍管理";
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

        // 初始化时加载学生信息
        SwingUtilities.invokeLater(this::refreshStudentInfo);
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
                
                // 同时更新当前学生对象的头像路径
                if (currentStudent != null && currentStudent.getUser() != null && avatarPath != null) {
                    currentStudent.getUser().setAvatarPath(avatarPath);
                }
                
                // 直接更新头像显示，无论当前处于什么模式
                updateAvatarDisplay(avatarPath);
                
            } catch (Exception e) {
                System.err.println("[StudentProfileModule] 更新头像显示失败: " + e.getMessage());
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
            System.err.println("[StudentProfileModule] 通知侧边栏更新头像失败: " + e.getMessage());
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
                
                if (currentStudent != null && currentStudent.getUser() != null) {
                    avatarPath = currentStudent.getUser().getAvatarPath();
                    userName = currentStudent.getName();
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
                        System.err.println("[StudentProfileModule] 头像刷新失败: " + errorMessage);
                        // 强制刷新UI
                        avatarLabel.revalidate();
                        avatarLabel.repaint();
                    }
                });
            } catch (Exception e) {
                System.err.println("[StudentProfileModule] 刷新头像显示失败: " + e.getMessage());
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
        AvatarManager.updateAvatar(avatarLabel, avatarPath, currentStudent != null ? currentStudent.getName() : null, new AvatarManager.AvatarUpdateCallback() {
            @Override
            public void onAvatarUpdated(Image avatarImage) {
                // 强制刷新UI
                avatarLabel.revalidate();
                avatarLabel.repaint();
            }
            
            @Override
            public void onUpdateFailed(String errorMessage) {
                System.err.println("[StudentProfileModule] 头像显示更新失败: " + errorMessage);
                // 强制刷新UI
                avatarLabel.revalidate();
                avatarLabel.repaint();
            }
        });
    }
    
}
