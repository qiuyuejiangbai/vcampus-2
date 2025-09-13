package client.ui.modules.course;

import common.vo.CourseVO;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * 课程教学班卡片面板
 * 用于管理多个教学班卡片的横向滚动显示
 */
public class CourseClassCardPanel extends JPanel {
    private JPanel cardContainer;
    private JScrollPane scrollPane;
    private List<CourseClassCard> classCards;
    private Map<String, List<CourseVO>> courseClassesMap;
    
    public CourseClassCardPanel() {
        this.classCards = new ArrayList<>();
        this.courseClassesMap = new HashMap<>();
        initComponents();
        setupLayout();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UITheme.WHITE);
        setPreferredSize(new Dimension(0, 320));
        setMinimumSize(new Dimension(0, 320));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        
        // 创建标题面板
        JPanel titlePanel = createTitlePanel();
        
        // 创建卡片容器
        cardContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));
        cardContainer.setOpaque(false);
        cardContainer.setPreferredSize(new Dimension(0, 280));
        cardContainer.setBackground(UITheme.WHITE);
        
        // 创建滚动面板
        scrollPane = new JScrollPane(cardContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(UITheme.createEmptyBorder(0, 0, 0, 0));
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.setBackground(UITheme.WHITE);
        
        // 设置滚动条样式
        JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();
        horizontalScrollBar.setPreferredSize(new Dimension(0, 8));
        horizontalScrollBar.setBackground(UITheme.LIGHT_GRAY);
        horizontalScrollBar.setForeground(UITheme.PRIMARY_GREEN);
        
        // 添加组件
        add(titlePanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // 初始状态隐藏面板
        setVisible(false);
    }
    
    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setBackground(UITheme.WHITE);
        titlePanel.setBorder(UITheme.createEmptyBorder(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM, UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM));
        
        JLabel titleLabel = new JLabel("教学班信息");
        titleLabel.setFont(UITheme.SUBTITLE_FONT);
        titleLabel.setForeground(UITheme.PRIMARY_GREEN);
        
        // 添加装饰性图标
        JLabel iconLabel = new JLabel("📚");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        iconLabel.setBorder(UITheme.createEmptyBorder(0, 0, 0, UITheme.PADDING_SMALL));
        
        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);
        
        return titlePanel;
    }
    
    private void setupLayout() {
        // 布局已在initComponents中设置
    }
    
    /**
     * 显示指定课程的所有教学班
     * @param courseCode 课程代码
     * @param courses 该课程的所有教学班列表
     */
    public void showCourseClasses(String courseCode, List<CourseVO> courses) {
        if (courseCode == null || courses == null || courses.isEmpty()) {
            hideCourseClasses();
            return;
        }
        
        // 清除现有卡片
        clearCards();
        
        // 创建新的教学班卡片
        for (CourseVO course : courses) {
            CourseClassCard card = new CourseClassCard(course);
            classCards.add(card);
            cardContainer.add(card);
        }
        
        // 刷新容器
        cardContainer.revalidate();
        cardContainer.repaint();
        
        // 显示面板
        setVisible(true);
        
        // 滚动到最左边
        SwingUtilities.invokeLater(() -> {
            scrollPane.getHorizontalScrollBar().setValue(0);
        });
    }
    
    /**
     * 隐藏教学班信息
     */
    public void hideCourseClasses() {
        clearCards();
        setVisible(false);
    }
    
    /**
     * 清除所有卡片
     */
    private void clearCards() {
        for (CourseClassCard card : classCards) {
            cardContainer.remove(card);
        }
        classCards.clear();
        cardContainer.revalidate();
        cardContainer.repaint();
    }
    
    /**
     * 根据课程代码分组课程数据
     * @param allCourses 所有课程数据
     */
    public void groupCoursesByCode(List<CourseVO> allCourses) {
        courseClassesMap.clear();
        
        if (allCourses != null) {
            for (CourseVO course : allCourses) {
                String courseCode = course.getCourseCode();
                if (courseCode != null) {
                    courseClassesMap.computeIfAbsent(courseCode, k -> new ArrayList<>()).add(course);
                }
            }
        }
    }
    
    /**
     * 获取指定课程代码的所有教学班
     * @param courseCode 课程代码
     * @return 教学班列表
     */
    public List<CourseVO> getCourseClasses(String courseCode) {
        return courseClassesMap.getOrDefault(courseCode, new ArrayList<>());
    }
    
    /**
     * 检查是否有教学班信息
     * @param courseCode 课程代码
     * @return 是否有教学班
     */
    public boolean hasCourseClasses(String courseCode) {
        List<CourseVO> classes = courseClassesMap.get(courseCode);
        return classes != null && !classes.isEmpty();
    }
    
    /**
     * 获取当前显示的教学班数量
     * @return 教学班数量
     */
    public int getDisplayedClassCount() {
        return classCards.size();
    }
    
    /**
     * 获取滚动面板
     * @return JScrollPane对象
     */
    public JScrollPane getScrollPane() {
        return scrollPane;
    }
}
