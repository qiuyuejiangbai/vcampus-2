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
    }
    
    private void initComponents() {
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(350, 250));
        setMinimumSize(new Dimension(350, 250));
        setMaximumSize(new Dimension(350, 250));
        
        // 创建标签
        classInfoLabel = new JLabel();
        teacherLabel = new JLabel();
        classTimeLabel = new JLabel();
        locationLabel = new JLabel();
        capacityLabel = new JLabel();
        enrolledCountLabel = new JLabel();
        
        // 设置字体
        Font titleFont = new Font("微软雅黑", Font.BOLD, 16);
        Font contentFont = new Font("微软雅黑", Font.PLAIN, 14);
        
        classInfoLabel.setFont(titleFont);
        classInfoLabel.setForeground(new Color(51, 51, 51));
        
        teacherLabel.setFont(contentFont);
        classTimeLabel.setFont(contentFont);
        locationLabel.setFont(contentFont);
        capacityLabel.setFont(contentFont);
        enrolledCountLabel.setFont(contentFont);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(0, 6));
        
        // 顶部：教学班信息
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);
        topPanel.add(classInfoLabel);
        
        // 中间：详细信息
        JPanel detailPanel = new JPanel(new GridLayout(2, 2, 10, 6));
        detailPanel.setOpaque(false);
        
        detailPanel.add(teacherLabel);
        detailPanel.add(classTimeLabel);
        detailPanel.add(locationLabel);
        detailPanel.add(capacityLabel);
        
        // 底部：选课信息
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(enrolledCountLabel);
        
        add(topPanel, BorderLayout.NORTH);
        add(detailPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
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
