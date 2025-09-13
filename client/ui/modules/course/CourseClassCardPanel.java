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
        setBorder(BorderFactory.createTitledBorder("教学班信息"));
        setPreferredSize(new Dimension(0, 300));
        setMinimumSize(new Dimension(0, 300));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        
        // 创建卡片容器
        cardContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        cardContainer.setOpaque(false);
        cardContainer.setPreferredSize(new Dimension(0, 250)); // 确保容器有足够高度
        
        // 创建滚动面板
        scrollPane = new JScrollPane(cardContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        
        // 初始状态隐藏面板
        setVisible(false);
    }
    
    private void setupLayout() {
        add(scrollPane, BorderLayout.CENTER);
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
