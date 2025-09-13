 package client.ui.modules.course;

import common.vo.CourseVO;

import javax.swing.*;
import java.awt.*;

/**
 * 课程教学班卡片组件
 * 用于显示单个教学班的详细信息
 */
public class CourseClassCard extends JPanel {
    private CourseVO course;
    private JLabel teacherLabel;
    private JLabel classTimeLabel;
    private JLabel locationLabel;
    private JLabel capacityLabel;
    private JLabel enrolledCountLabel;
    private JLabel classInfoLabel;
    
    public CourseClassCard(CourseVO course) {
        this.course = course;
        initComponents();
        setupLayout();
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
        classInfoLabel = new JLabel();
        teacherLabel = new JLabel();
        classTimeLabel = new JLabel();
        locationLabel = new JLabel();
        capacityLabel = new JLabel();
        enrolledCountLabel = new JLabel();
        
        // 设置字体和颜色
        classInfoLabel.setFont(UITheme.SUBTITLE_FONT);
        classInfoLabel.setForeground(UITheme.PRIMARY_GREEN);
        
        teacherLabel.setFont(UITheme.CONTENT_FONT);
        teacherLabel.setForeground(UITheme.DARK_GRAY);
        
        classTimeLabel.setFont(UITheme.CONTENT_FONT);
        classTimeLabel.setForeground(UITheme.DARK_GRAY);
        
        locationLabel.setFont(UITheme.CONTENT_FONT);
        locationLabel.setForeground(UITheme.DARK_GRAY);
        
        capacityLabel.setFont(UITheme.CONTENT_FONT);
        capacityLabel.setForeground(UITheme.DARK_GRAY);
        
        enrolledCountLabel.setFont(UITheme.CONTENT_FONT);
        enrolledCountLabel.setForeground(UITheme.MEDIUM_GRAY);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(0, UITheme.PADDING_MEDIUM));
        
        // 顶部：教学班信息
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topPanel.setOpaque(false);
        topPanel.setBorder(UITheme.createEmptyBorder(0, 0, UITheme.PADDING_SMALL, 0));
        topPanel.add(classInfoLabel);
        
        // 中间：详细信息
        JPanel detailPanel = new JPanel(new GridLayout(2, 2, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL));
        detailPanel.setOpaque(false);
        detailPanel.setBorder(UITheme.createEmptyBorder(UITheme.PADDING_SMALL, 0, UITheme.PADDING_SMALL, 0));
        
        detailPanel.add(teacherLabel);
        detailPanel.add(classTimeLabel);
        detailPanel.add(locationLabel);
        detailPanel.add(capacityLabel);
        
        // 底部：选课信息
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(UITheme.createEmptyBorder(UITheme.PADDING_SMALL, 0, 0, 0));
        bottomPanel.add(enrolledCountLabel);
        
        add(topPanel, BorderLayout.NORTH);
        add(detailPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setupHoverEffects() {
        // 添加鼠标悬停效果
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                setBorder(UITheme.createRoundedBorder(UITheme.PRIMARY_GREEN, 2, UITheme.RADIUS_MEDIUM));
                setBackground(UITheme.VERY_LIGHT_GREEN);
                repaint();
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                setBorder(UITheme.createCardBorder());
                setBackground(UITheme.WHITE);
                repaint();
            }
        });
        
        // 设置鼠标指针
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private void updateCardContent() {
        if (course != null) {
            // 教学班标识（教师+时间）
            String classInfo = String.format("教学班 - %s", 
                course.getTeacherName() != null ? course.getTeacherName() : "未设置");
            classInfoLabel.setText(classInfo);
            
            // 详细信息
            teacherLabel.setText("教师: " + (course.getTeacherName() != null ? course.getTeacherName() : "未设置"));
            classTimeLabel.setText("时间: " + (course.getClassTime() != null ? course.getClassTime() : "未设置"));
            locationLabel.setText("地点: " + (course.getLocation() != null ? course.getLocation() : "未设置"));
            capacityLabel.setText("容量: " + (course.getCapacity() != null ? course.getCapacity().toString() : "未设置"));
            
            // 选课信息
            int enrolled = course.getEnrolledCount() != null ? course.getEnrolledCount() : 0;
            int capacity = course.getCapacity() != null ? course.getCapacity() : 0;
            String statusText = String.format("已选: %d/%d", enrolled, capacity);
            enrolledCountLabel.setText(statusText);
        }
    }
    
    /**
     * 获取课程信息
     * @return CourseVO对象
     */
    public CourseVO getCourse() {
        return course;
    }
    
    /**
     * 更新课程信息
     * @param course 新的课程信息
     */
    public void updateCourse(CourseVO course) {
        this.course = course;
        updateCardContent();
    }
}
