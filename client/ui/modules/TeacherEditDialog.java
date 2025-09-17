package client.ui.modules;

import client.ui.modules.course.UITheme;
import client.ui.util.ValidationUtil;
import common.vo.TeacherVO;

import javax.swing.*;
import java.awt.*;

/**
 * 教师编辑对话框
 * 用于添加和编辑教师信息
 */
public class TeacherEditDialog extends JDialog {
    private JTextField teacherNoField;
    private JTextField nameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField departmentField;
    private JTextField titleField;
    private JTextField officeField;
    private JTextField researchAreaField;
    private JTextField balanceField;
    
    private JButton okButton;
    private JButton cancelButton;
    
    private TeacherVO teacher;
    private boolean confirmed = false;

    public TeacherEditDialog(JFrame parent, String title, TeacherVO teacher) {
        super(parent, title, true);
        this.teacher = teacher;
        
        initComponents();
        setupLayout();
        setupEventHandlers();
        
        if (teacher != null) {
            loadTeacherData();
        }
        
        setSize(500, 650);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UITheme.WHITE);
        
        // 创建输入字段
        teacherNoField = new JTextField(20);
        styleTextField(teacherNoField);
        setupTeacherNoFieldValidation(teacherNoField);
        
        nameField = new JTextField(20);
        styleTextField(nameField);
        setupNameFieldValidation(nameField);
        
        phoneField = new JTextField(20);
        styleTextField(phoneField);
        setupPhoneFieldValidation(phoneField);
        
        emailField = new JTextField(20);
        styleTextField(emailField);
        setupEmailFieldValidation(emailField);
        
        departmentField = new JTextField(20);
        styleTextField(departmentField);
        
        titleField = new JTextField(20);
        styleTextField(titleField);
        
        officeField = new JTextField(20);
        styleTextField(officeField);
        
        researchAreaField = new JTextField(20);
        styleTextField(researchAreaField);
        
        balanceField = new JTextField(20);
        styleTextField(balanceField);
        balanceField.setEnabled(false); // 账户余额不可编辑
        balanceField.setEditable(false);
        
        // 创建按钮
        okButton = new JButton("确定");
        styleButton(okButton);
        okButton.setPreferredSize(new Dimension(80, UITheme.BUTTON_HEIGHT));
        
        cancelButton = new JButton("取消");
        styleButton(cancelButton);
        cancelButton.setPreferredSize(new Dimension(80, UITheme.BUTTON_HEIGHT));
    }

    private void setupLayout() {
        // 创建表单面板
        JPanel formPanel = createFormPanel();
        
        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        
        // 添加到对话框
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * 创建表单面板
     */
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_LARGE, UITheme.PADDING_LARGE, UITheme.PADDING_LARGE, UITheme.PADDING_LARGE));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(UITheme.PADDING_SMALL, UITheme.PADDING_SMALL, UITheme.PADDING_SMALL, UITheme.PADDING_SMALL);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 工号
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("工号*:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(teacherNoField, gbc);
        
        // 姓名
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("姓名*:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(nameField, gbc);
        
        // 联系电话
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("联系电话:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(phoneField, gbc);
        
        // 邮箱
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("邮箱:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(emailField, gbc);
        
        // 院系
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("院系:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(departmentField, gbc);
        
        // 职称
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("职称:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(titleField, gbc);
        
        // 办公室
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("办公室:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(officeField, gbc);
        
        // 研究方向
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("研究方向:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(researchAreaField, gbc);
        
        // 账户余额
        gbc.gridx = 0; gbc.gridy = 8;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("账户余额(只读):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(balanceField, gbc);
        
        return formPanel;
    }

    /**
     * 创建标签
     */
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.CONTENT_FONT);
        label.setForeground(UITheme.DARK_GRAY);
        label.setPreferredSize(new Dimension(120, UITheme.INPUT_HEIGHT));
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }

    /**
     * 创建按钮面板
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));
        buttonPanel.setBackground(UITheme.WHITE);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(UITheme.PADDING_MEDIUM, 0, UITheme.PADDING_MEDIUM, 0)
        ));
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        return buttonPanel;
    }

    private void setupEventHandlers() {
        okButton.addActionListener(e -> {
            if (validateInput()) {
                confirmed = true;
                dispose();
            }
        });
        
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
    }

    /**
     * 验证输入
     */
    private boolean validateInput() {
        // 验证工号
        String teacherNoError = ValidationUtil.getTeacherNoValidationError(teacherNoField.getText());
        if (teacherNoError != null) {
            JOptionPane.showMessageDialog(this, teacherNoError, "验证错误", JOptionPane.ERROR_MESSAGE);
            teacherNoField.requestFocus();
            return false;
        }
        
        // 验证姓名
        String nameError = ValidationUtil.getNameValidationError(nameField.getText());
        if (nameError != null) {
            JOptionPane.showMessageDialog(this, nameError, "验证错误", JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return false;
        }
        
        // 验证电话号码
        String phoneError = ValidationUtil.getPhoneValidationError(phoneField.getText());
        if (phoneError != null) {
            JOptionPane.showMessageDialog(this, phoneError, "验证错误", JOptionPane.ERROR_MESSAGE);
            phoneField.requestFocus();
            return false;
        }
        
        // 验证邮箱
        String emailError = ValidationUtil.getEmailValidationError(emailField.getText());
        if (emailError != null) {
            JOptionPane.showMessageDialog(this, emailError, "验证错误", JOptionPane.ERROR_MESSAGE);
            emailField.requestFocus();
            return false;
        }
        
        return true;
    }

    /**
     * 加载教师数据
     */
    private void loadTeacherData() {
        if (teacher != null) {
            teacherNoField.setText(teacher.getTeacherNo());
            nameField.setText(teacher.getName());
            phoneField.setText(teacher.getPhone());
            emailField.setText(teacher.getEmail());
            departmentField.setText(teacher.getDepartment());
            titleField.setText(teacher.getTitle());
            officeField.setText(teacher.getOffice());
            researchAreaField.setText(teacher.getResearchArea());
            
            // 加载账户余额
            if (teacher.getBalance() != null) {
                balanceField.setText("¥" + teacher.getBalance().toString());
            } else {
                balanceField.setText("¥0.00");
            }
        }
    }

    /**
     * 获取教师对象
     */
    public TeacherVO getTeacher() {
        System.out.println("[DEBUG][TeacherEditDialog] ========== 开始构建教师对象 ==========");
        System.out.println("[DEBUG][TeacherEditDialog] 确认状态: " + confirmed);
        
        if (!confirmed) {
            System.out.println("[DEBUG][TeacherEditDialog] 用户未确认，返回null");
            return null;
        }
        
        TeacherVO result = new TeacherVO();
        
        // 如果是编辑模式，保留原有ID
        if (teacher != null) {
            System.out.println("[DEBUG][TeacherEditDialog] 编辑模式，保留原有ID: " + teacher.getId() + ", 用户ID: " + teacher.getUserId());
            result.setId(teacher.getId());
            result.setUserId(teacher.getUserId());
        } else {
            System.out.println("[DEBUG][TeacherEditDialog] 新增模式");
        }
        
        // 设置基本信息
        String teacherNo = teacherNoField.getText().trim();
        String name = nameField.getText().trim();
        result.setTeacherNo(teacherNo);
        result.setName(name);
        System.out.println("[DEBUG][TeacherEditDialog] 工号: " + teacherNo + ", 姓名: " + name);
        
        // 设置联系信息
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        result.setPhone(phone);
        result.setEmail(email);
        System.out.println("[DEBUG][TeacherEditDialog] 联系电话: " + phone + ", 邮箱: " + email);
        
        // 设置工作信息
        String department = departmentField.getText().trim();
        String title = titleField.getText().trim();
        String office = officeField.getText().trim();
        String researchArea = researchAreaField.getText().trim();
        result.setDepartment(department);
        result.setTitle(title);
        result.setOffice(office);
        result.setResearchArea(researchArea);
        System.out.println("[DEBUG][TeacherEditDialog] 院系: " + department + ", 职称: " + title + ", 办公室: " + office + ", 研究方向: " + researchArea);
        
        System.out.println("[DEBUG][TeacherEditDialog] ========== 教师对象构建完成 ==========");
        return result;
    }

    /**
     * 是否确认
     */
    public boolean isConfirmed() {
        return confirmed;
    }
    
    /**
     * 样式化输入框
     */
    private void styleTextField(JTextField textField) {
        textField.setFont(UITheme.CONTENT_FONT);
        textField.setBackground(UITheme.WHITE);
        textField.setForeground(UITheme.DARK_GRAY);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM)
        ));
        textField.setPreferredSize(new Dimension(200, UITheme.INPUT_HEIGHT));
        textField.setEnabled(true);
        textField.setEditable(true);
    }
    
    /**
     * 样式化按钮
     */
    private void styleButton(JButton button) {
        button.setFont(UITheme.CONTENT_FONT);
        button.setBackground(UITheme.PRIMARY_GREEN);
        button.setForeground(UITheme.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_LARGE, UITheme.PADDING_SMALL, UITheme.PADDING_LARGE));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(UITheme.HOVER_GREEN);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(UITheme.PRIMARY_GREEN);
            }
        });
    }
    
    /**
     * 设置电话字段验证
     */
    private void setupPhoneFieldValidation(JTextField phoneField) {
        phoneField.setToolTipText("请输入有效的手机号码（支持中国手机号和国际格式）");
        
        // 添加输入限制：只允许数字、+、-、空格
        phoneField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                // 允许数字、+、-、空格、退格、删除等控制字符
                if (!Character.isDigit(c) && c != '+' && c != '-' && c != ' ' && 
                    !Character.isISOControl(c)) {
                    e.consume(); // 阻止输入
                }
            }
        });
        
        // 添加实时验证
        phoneField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validatePhoneField();
            }
            
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validatePhoneField();
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validatePhoneField();
            }
        });
    }
    
    /**
     * 设置邮箱字段验证
     */
    private void setupEmailFieldValidation(JTextField emailField) {
        emailField.setToolTipText("请输入有效的邮箱地址（格式：user@domain.com）");
        
        // 添加实时验证
        emailField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validateEmailField();
            }
            
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validateEmailField();
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validateEmailField();
            }
        });
    }
    
    /**
     * 验证电话字段
     */
    private void validatePhoneField() {
        String phone = phoneField.getText().trim();
        if (phone.isEmpty()) {
            phoneField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM)
            ));
            return;
        }
        
        // 使用新的验证工具类
        if (ValidationUtil.isValidPhone(phone)) {
            phoneField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.PRIMARY_GREEN, 2),
                BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM)
            ));
        } else {
            phoneField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.RED, 2),
                BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM)
            ));
        }
    }
    
    /**
     * 验证邮箱字段
     */
    private void validateEmailField() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM)
            ));
            return;
        }
        
        // 使用新的验证工具类
        if (ValidationUtil.isValidEmail(email)) {
            emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.PRIMARY_GREEN, 2),
                BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM)
            ));
        } else {
            emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.RED, 2),
                BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM)
            ));
        }
    }
    
    /**
     * 设置工号字段验证
     */
    private void setupTeacherNoFieldValidation(JTextField teacherNoField) {
        teacherNoField.setToolTipText("请输入4-20位字母数字组合");
        
        // 添加实时验证
        teacherNoField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validateTeacherNoField();
            }
            
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validateTeacherNoField();
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validateTeacherNoField();
            }
        });
    }
    
    /**
     * 设置姓名字段验证
     */
    private void setupNameFieldValidation(JTextField nameField) {
        nameField.setToolTipText("请输入2-20位中英文字符");
        
        // 添加实时验证
        nameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validateNameField();
            }
            
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validateNameField();
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validateNameField();
            }
        });
    }
    
    /**
     * 验证工号字段
     */
    private void validateTeacherNoField() {
        String teacherNo = teacherNoField.getText().trim();
        if (teacherNo.isEmpty()) {
            teacherNoField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM)
            ));
            return;
        }
        
        // 使用验证工具类
        if (ValidationUtil.isValidTeacherNo(teacherNo)) {
            teacherNoField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.PRIMARY_GREEN, 2),
                BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM)
            ));
        } else {
            teacherNoField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.RED, 2),
                BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM)
            ));
        }
    }
    
    /**
     * 验证姓名字段
     */
    private void validateNameField() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM)
            ));
            return;
        }
        
        // 使用验证工具类
        if (ValidationUtil.isValidName(name)) {
            nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.PRIMARY_GREEN, 2),
                BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM)
            ));
        } else {
            nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.RED, 2),
                BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM)
            ));
        }
    }
}
