package client.ui.modules;

import client.ui.modules.course.UITheme;
import client.ui.util.ValidationUtil;
import common.vo.StudentVO;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * 学生编辑对话框
 * 用于添加和编辑学生信息
 */
public class StudentEditDialog extends JDialog {
    private JTextField studentNoField;
    private JTextField nameField;
    private JComboBox<String> genderComboBox;
    private JTextField birthDateField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField addressField;
    private JTextField departmentField;
    private JTextField classNameField;
    private JTextField majorField;
    private JTextField gradeField;
    private JTextField enrollmentYearField;
    private JTextField balanceField;
    
    private JButton okButton;
    private JButton cancelButton;
    
    private StudentVO student;
    private boolean confirmed = false;

    public StudentEditDialog(JFrame parent, String title, StudentVO student) {
        super(parent, title, true);
        this.student = student;
        
        initComponents();
        setupLayout();
        setupEventHandlers();
        
        if (student != null) {
            loadStudentData();
        }
        
        setSize(650, 750);
        setLocationRelativeTo(parent);
        setResizable(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UITheme.WHITE);
        
        // 创建输入字段
        studentNoField = new JTextField(20);
        styleTextField(studentNoField);
        
        nameField = new JTextField(20);
        styleTextField(nameField);
        
        genderComboBox = new JComboBox<>(new String[]{"男", "女"});
        styleComboBox(genderComboBox);
        
        birthDateField = new JTextField(20);
        styleTextField(birthDateField);
        birthDateField.setToolTipText("格式: yyyy-MM-dd");
        setupDateFieldValidation(birthDateField);
        
        phoneField = new JTextField(20);
        styleTextField(phoneField);
        setupPhoneFieldValidation(phoneField);
        
        emailField = new JTextField(20);
        styleTextField(emailField);
        setupEmailFieldValidation(emailField);
        
        addressField = new JTextField(20);
        styleTextField(addressField);
        
        departmentField = new JTextField(20);
        styleTextField(departmentField);
        
        classNameField = new JTextField(20);
        styleTextField(classNameField);
        
        majorField = new JTextField(20);
        styleTextField(majorField);
        
        gradeField = new JTextField(20);
        styleTextField(gradeField);
        
        enrollmentYearField = new JTextField(20);
        styleTextField(enrollmentYearField);
        setupYearFieldValidation(enrollmentYearField);
        
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
        
        // 创建滚动面板包装表单
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        
        // 添加到对话框
        add(scrollPane, BorderLayout.CENTER);
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
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        
        // 学号
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("学号*:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(studentNoField, gbc);
        
        // 姓名
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("姓名*:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(nameField, gbc);
        
        // 性别
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("性别:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(genderComboBox, gbc);
        
        // 出生日期
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("出生日期:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(birthDateField, gbc);
        
        // 联系电话
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("联系电话:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(phoneField, gbc);
        
        // 邮箱
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("邮箱:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(emailField, gbc);
        
        // 地址
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("地址:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(addressField, gbc);
        
        // 院系
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("院系:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(departmentField, gbc);
        
        // 班级
        gbc.gridx = 0; gbc.gridy = 8;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("班级:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(classNameField, gbc);
        
        // 专业
        gbc.gridx = 0; gbc.gridy = 9;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("专业:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(majorField, gbc);
        
        // 年级
        gbc.gridx = 0; gbc.gridy = 10;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("年级:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(gradeField, gbc);
        
        // 入学年份
        gbc.gridx = 0; gbc.gridy = 11;
        gbc.weightx = 0.3;
        formPanel.add(createLabel("入学年份:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(enrollmentYearField, gbc);
        
        // 账户余额
        gbc.gridx = 0; gbc.gridy = 12;
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
        // 验证学号
        String studentNoError = ValidationUtil.getStudentNoValidationError(studentNoField.getText());
        if (studentNoError != null) {
            JOptionPane.showMessageDialog(this, studentNoError, "验证错误", JOptionPane.ERROR_MESSAGE);
            studentNoField.requestFocus();
            return false;
        }
        
        // 验证姓名
        String nameError = ValidationUtil.getNameValidationError(nameField.getText());
        if (nameError != null) {
            JOptionPane.showMessageDialog(this, nameError, "验证错误", JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return false;
        }
        
        // 验证出生日期格式
        String birthDateError = ValidationUtil.getDateValidationError(birthDateField.getText());
        if (birthDateError != null) {
            JOptionPane.showMessageDialog(this, birthDateError, "验证错误", JOptionPane.ERROR_MESSAGE);
            birthDateField.requestFocus();
            return false;
        }
        
        // 验证入学年份
        String enrollmentYearError = ValidationUtil.getYearValidationError(enrollmentYearField.getText());
        if (enrollmentYearError != null) {
            JOptionPane.showMessageDialog(this, enrollmentYearError, "验证错误", JOptionPane.ERROR_MESSAGE);
            enrollmentYearField.requestFocus();
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
        
        // 账户余额字段不可编辑，无需验证
        
        return true;
    }

    /**
     * 加载学生数据
     */
    private void loadStudentData() {
        if (student != null) {
            studentNoField.setText(student.getStudentNo());
            nameField.setText(student.getName());
            // 将英文性别转换为中文显示
            String genderDisplay = student.getGenderName();
            genderComboBox.setSelectedItem(genderDisplay);
            
            if (student.getBirthDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                birthDateField.setText(sdf.format(student.getBirthDate()));
            }
            
            phoneField.setText(student.getPhone());
            emailField.setText(student.getEmail());
            addressField.setText(student.getAddress());
            departmentField.setText(student.getDepartment());
            classNameField.setText(student.getClassName());
            majorField.setText(student.getMajor());
            gradeField.setText(student.getGrade());
            
            if (student.getEnrollmentYear() != null) {
                enrollmentYearField.setText(student.getEnrollmentYear().toString());
            }
            
            if (student.getBalance() != null) {
                balanceField.setText(student.getBalance().toString());
            }
        }
    }

    /**
     * 获取学生对象
     */
    public StudentVO getStudent() {
        System.out.println("[DEBUG][StudentEditDialog] ========== 开始构建学生对象 ==========");
        System.out.println("[DEBUG][StudentEditDialog] 确认状态: " + confirmed);
        
        if (!confirmed) {
            System.out.println("[DEBUG][StudentEditDialog] 用户未确认，返回null");
            return null;
        }
        
        StudentVO result = new StudentVO();
        
        // 如果是编辑模式，保留原有ID
        if (student != null) {
            System.out.println("[DEBUG][StudentEditDialog] 编辑模式，保留原有ID: " + student.getId() + ", 用户ID: " + student.getUserId());
            result.setId(student.getId());
            result.setUserId(student.getUserId());
        } else {
            System.out.println("[DEBUG][StudentEditDialog] 新增模式");
        }
        
        // 设置基本信息
        String studentNo = studentNoField.getText().trim();
        String name = nameField.getText().trim();
        result.setStudentNo(studentNo);
        result.setName(name);
        System.out.println("[DEBUG][StudentEditDialog] 学号: " + studentNo + ", 姓名: " + name);
        
        // 将中文性别转换为英文存储
        String genderDisplay = (String) genderComboBox.getSelectedItem();
        String genderValue = convertGenderToEnglish(genderDisplay);
        result.setGender(genderValue);
        System.out.println("[DEBUG][StudentEditDialog] 性别显示: " + genderDisplay + ", 存储值: " + genderValue);
        
        // 处理出生日期
        String birthDateText = birthDateField.getText().trim();
        if (!birthDateText.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                result.setBirthDate(new Date(sdf.parse(birthDateText).getTime()));
                System.out.println("[DEBUG][StudentEditDialog] 出生日期: " + birthDateText);
            } catch (ParseException e) {
                System.err.println("[DEBUG][StudentEditDialog] 出生日期解析失败: " + birthDateText);
            }
        } else {
            System.out.println("[DEBUG][StudentEditDialog] 出生日期为空");
        }
        
        // 设置联系信息
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        result.setPhone(phone);
        result.setEmail(email);
        result.setAddress(address);
        System.out.println("[DEBUG][StudentEditDialog] 联系电话: " + phone + ", 邮箱: " + email + ", 地址: " + address);
        
        // 设置学术信息
        String department = departmentField.getText().trim();
        String className = classNameField.getText().trim();
        String major = majorField.getText().trim();
        String grade = gradeField.getText().trim();
        result.setDepartment(department);
        result.setClassName(className);
        result.setMajor(major);
        result.setGrade(grade);
        System.out.println("[DEBUG][StudentEditDialog] 院系: " + department + ", 班级: " + className + ", 专业: " + major + ", 年级: " + grade);
        
        // 处理入学年份
        String enrollmentYearText = enrollmentYearField.getText().trim();
        if (!enrollmentYearText.isEmpty()) {
            try {
                Integer enrollmentYear = Integer.parseInt(enrollmentYearText);
                result.setEnrollmentYear(enrollmentYear);
                System.out.println("[DEBUG][StudentEditDialog] 入学年份: " + enrollmentYear);
            } catch (NumberFormatException e) {
                System.err.println("[DEBUG][StudentEditDialog] 入学年份解析失败: " + enrollmentYearText);
            }
        } else {
            System.out.println("[DEBUG][StudentEditDialog] 入学年份为空");
        }
        
        // 处理账户余额 - 保持原有值不变
        if (student != null && student.getBalance() != null) {
            result.setBalance(student.getBalance());
            System.out.println("[DEBUG][StudentEditDialog] 账户余额保持不变: " + student.getBalance());
        } else {
            result.setBalance(BigDecimal.ZERO);
            System.out.println("[DEBUG][StudentEditDialog] 账户余额设置为0");
        }
        
        System.out.println("[DEBUG][StudentEditDialog] ========== 学生对象构建完成 ==========");
        return result;
    }

    /**
     * 是否确认
     */
    public boolean isConfirmed() {
        return confirmed;
    }
    
    /**
     * 将中文性别转换为英文
     */
    private String convertGenderToEnglish(String chineseGender) {
        if (chineseGender == null) return "male"; // 默认为男性
        switch (chineseGender) {
            case "男": return "male";
            case "女": return "female";
            default: return "male"; // 默认为男性
        }
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
     * 样式化下拉框
     */
    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(UITheme.CONTENT_FONT);
        comboBox.setBackground(UITheme.WHITE);
        comboBox.setForeground(UITheme.DARK_GRAY);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM)
        ));
        comboBox.setPreferredSize(new Dimension(200, UITheme.INPUT_HEIGHT));
        comboBox.setEnabled(true);
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
     * 设置日期字段验证
     */
    private void setupDateFieldValidation(JTextField dateField) {
        dateField.setToolTipText("请输入正确的日期格式（yyyy-MM-dd）");
        
        // 添加实时验证
        dateField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validateDateField();
            }
            
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validateDateField();
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validateDateField();
            }
        });
    }
    
    /**
     * 设置年份字段验证
     */
    private void setupYearFieldValidation(JTextField yearField) {
        yearField.setToolTipText("请输入1900-2100之间的有效年份");
        
        // 添加输入限制：只允许数字
        yearField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                // 只允许数字、退格、删除等控制字符
                if (!Character.isDigit(c) && !Character.isISOControl(c)) {
                    e.consume(); // 阻止输入
                }
            }
        });
        
        // 添加实时验证
        yearField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validateYearField();
            }
            
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validateYearField();
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validateYearField();
            }
        });
    }
    
    /**
     * 验证日期字段
     */
    private void validateDateField() {
        String date = birthDateField.getText().trim();
        if (date.isEmpty()) {
            birthDateField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM)
            ));
            return;
        }
        
        // 使用验证工具类
        if (ValidationUtil.isValidDateFormat(date)) {
            birthDateField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.PRIMARY_GREEN, 2),
                BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM)
            ));
        } else {
            birthDateField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.RED, 2),
                BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM)
            ));
        }
    }
    
    /**
     * 验证年份字段
     */
    private void validateYearField() {
        String year = enrollmentYearField.getText().trim();
        if (year.isEmpty()) {
            enrollmentYearField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM)
            ));
            return;
        }
        
        // 使用验证工具类
        if (ValidationUtil.isValidYear(year)) {
            enrollmentYearField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.PRIMARY_GREEN, 2),
                BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM)
            ));
        } else {
            enrollmentYearField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.RED, 2),
                BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM)
            ));
        }
    }
    
}
