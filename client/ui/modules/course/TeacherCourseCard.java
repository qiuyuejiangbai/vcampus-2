package client.ui.modules.course;

import common.vo.CourseVO;
import common.vo.UserVO;
import client.net.ServerConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 教师课程卡片组件
 * 用于显示单个课程的详细信息，点击后跳转到学生名单界面
 */
public class TeacherCourseCard extends JPanel {
    private CourseVO course;
    private TeacherCourseCardPanel parentPanel;
    private JLabel courseNameLabel;
    private JLabel courseCodeLabel;
    private JLabel creditsLabel;
    private JLabel departmentLabel;
    private JLabel semesterLabel;
    private JLabel classTimeLabel;
    private JLabel locationLabel;
    private JLabel capacityLabel;
    private JLabel enrolledCountLabel;
    private JLabel statusLabel;
    private JButton viewStudentsButton;

    public TeacherCourseCard(CourseVO course, TeacherCourseCardPanel parentPanel, UserVO currentUser, ServerConnection connection) {
        this.course = course;
        this.parentPanel = parentPanel;
        
        initComponents();
        setupLayout();
        setupEventHandlers();
        updateCardContent();
        setupHoverEffects();
    }

    private void initComponents() {
        setBorder(UITheme.createCardBorder());
        setBackground(UITheme.WHITE);
        setPreferredSize(new Dimension(380, 280));
        setMinimumSize(new Dimension(380, 280));
        setMaximumSize(new Dimension(380, 280));
        
        // 创建标签
        courseNameLabel = new JLabel();
        courseCodeLabel = new JLabel();
        creditsLabel = new JLabel();
        departmentLabel = new JLabel();
        semesterLabel = new JLabel();
        classTimeLabel = new JLabel();
        locationLabel = new JLabel();
        capacityLabel = new JLabel();
        enrolledCountLabel = new JLabel();
        statusLabel = new JLabel();
        
        // 创建查看学生名单按钮
        viewStudentsButton = new JButton("查看学生名单");
        UITheme.styleButton(viewStudentsButton);
        viewStudentsButton.setPreferredSize(new Dimension(120, 32));
        viewStudentsButton.setBackground(UITheme.PRIMARY_GREEN);
        viewStudentsButton.setForeground(UITheme.WHITE);
        viewStudentsButton.setFont(UITheme.CONTENT_FONT);
        
        // 设置字体和颜色
        courseNameLabel.setFont(UITheme.SUBTITLE_FONT);
        courseNameLabel.setForeground(UITheme.PRIMARY_GREEN);
        
        courseCodeLabel.setFont(UITheme.CONTENT_FONT);
        courseCodeLabel.setForeground(UITheme.DARK_GRAY);
        
        creditsLabel.setFont(UITheme.CONTENT_FONT);
        creditsLabel.setForeground(UITheme.DARK_GRAY);
        
        departmentLabel.setFont(UITheme.CONTENT_FONT);
        departmentLabel.setForeground(UITheme.DARK_GRAY);
        
        semesterLabel.setFont(UITheme.CONTENT_FONT);
        semesterLabel.setForeground(UITheme.DARK_GRAY);
        
        classTimeLabel.setFont(UITheme.CONTENT_FONT);
        classTimeLabel.setForeground(UITheme.MEDIUM_GRAY);
        
        locationLabel.setFont(UITheme.CONTENT_FONT);
        locationLabel.setForeground(UITheme.MEDIUM_GRAY);
        
        capacityLabel.setFont(UITheme.CONTENT_FONT);
        capacityLabel.setForeground(UITheme.MEDIUM_GRAY);
        
        enrolledCountLabel.setFont(UITheme.CONTENT_FONT);
        enrolledCountLabel.setForeground(UITheme.MEDIUM_GRAY);
        
        statusLabel.setFont(UITheme.CONTENT_FONT);
        statusLabel.setForeground(UITheme.SUCCESS_GREEN);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // 主内容面板
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UITheme.WHITE);
        contentPanel.setBorder(UITheme.createEmptyBorder(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));
        
        // 课程名称
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        namePanel.setBackground(UITheme.WHITE);
        namePanel.add(courseNameLabel);
        contentPanel.add(namePanel);
        
        contentPanel.add(Box.createVerticalStrut(UITheme.PADDING_SMALL));
        
        // 课程代码和学分
        JPanel codeCreditsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        codeCreditsPanel.setBackground(UITheme.WHITE);
        codeCreditsPanel.add(courseCodeLabel);
        codeCreditsPanel.add(Box.createHorizontalStrut(UITheme.PADDING_MEDIUM));
        codeCreditsPanel.add(creditsLabel);
        contentPanel.add(codeCreditsPanel);
        
        contentPanel.add(Box.createVerticalStrut(UITheme.PADDING_SMALL));
        
        // 院系和学期
        JPanel deptSemesterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        deptSemesterPanel.setBackground(UITheme.WHITE);
        deptSemesterPanel.add(departmentLabel);
        deptSemesterPanel.add(Box.createHorizontalStrut(UITheme.PADDING_MEDIUM));
        deptSemesterPanel.add(semesterLabel);
        contentPanel.add(deptSemesterPanel);
        
        contentPanel.add(Box.createVerticalStrut(UITheme.PADDING_SMALL));
        
        // 上课时间和地点
        JPanel timeLocationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        timeLocationPanel.setBackground(UITheme.WHITE);
        timeLocationPanel.add(classTimeLabel);
        timeLocationPanel.add(Box.createHorizontalStrut(UITheme.PADDING_MEDIUM));
        timeLocationPanel.add(locationLabel);
        contentPanel.add(timeLocationPanel);
        
        contentPanel.add(Box.createVerticalStrut(UITheme.PADDING_SMALL));
        
        // 容量和已选人数
        JPanel capacityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        capacityPanel.setBackground(UITheme.WHITE);
        capacityPanel.add(capacityLabel);
        capacityPanel.add(Box.createHorizontalStrut(UITheme.PADDING_MEDIUM));
        capacityPanel.add(enrolledCountLabel);
        contentPanel.add(capacityPanel);
        
        contentPanel.add(Box.createVerticalStrut(UITheme.PADDING_SMALL));
        
        // 状态
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusPanel.setBackground(UITheme.WHITE);
        statusPanel.add(statusLabel);
        contentPanel.add(statusPanel);
        
        contentPanel.add(Box.createVerticalStrut(UITheme.PADDING_MEDIUM));
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonPanel.setBackground(UITheme.WHITE);
        buttonPanel.add(viewStudentsButton);
        contentPanel.add(buttonPanel);
        
        add(contentPanel, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        // 卡片点击事件
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (parentPanel != null) {
                    parentPanel.onCourseCardClicked(course);
                }
            }
        });
        
        // 查看学生名单按钮点击事件
        viewStudentsButton.addActionListener(e -> {
            if (parentPanel != null) {
                parentPanel.onCourseCardClicked(course);
            }
        });
    }

    private void setupHoverEffects() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(UITheme.VERY_LIGHT_GREEN);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(UITheme.WHITE);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

    private void updateCardContent() {
        if (course != null) {
            courseNameLabel.setText(course.getCourseName());
            courseCodeLabel.setText("课程代码: " + course.getCourseCode());
            creditsLabel.setText("学分: " + course.getCredits());
            departmentLabel.setText("院系: " + course.getDepartment());
            semesterLabel.setText("学期: " + course.getSemester());
            classTimeLabel.setText("上课时间: " + (course.getClassTime() != null ? course.getClassTime() : "未设置"));
            locationLabel.setText("上课地点: " + (course.getLocation() != null ? course.getLocation() : "未设置"));
            capacityLabel.setText("容量: " + course.getCapacity());
            enrolledCountLabel.setText("已选: " + course.getEnrolledCount());
            statusLabel.setText("状态: " + course.getStatusName());
        }
    }

    /**
     * 获取课程信息
     */
    public CourseVO getCourse() {
        return course;
    }

    /**
     * 设置课程信息
     */
    public void setCourse(CourseVO course) {
        this.course = course;
        updateCardContent();
    }

    /**
     * 更新卡片内容
     */
    public void updateCardContent(CourseVO course) {
        this.course = course;
        updateCardContent();
    }
}
