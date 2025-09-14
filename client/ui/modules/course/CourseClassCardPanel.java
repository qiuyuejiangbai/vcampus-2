package client.ui.modules.course;

import common.vo.CourseVO;
import common.vo.UserVO;

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
    private UserVO currentUser;
    
    public CourseClassCardPanel() {
        this.classCards = new ArrayList<>();
        this.courseClassesMap = new HashMap<>();
        initComponents();
        setupLayout();
    }
    
    public CourseClassCardPanel(UserVO currentUser) {
        this.classCards = new ArrayList<>();
        this.courseClassesMap = new HashMap<>();
        this.currentUser = currentUser;
        initComponents();
        setupLayout();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UITheme.WHITE);
        setPreferredSize(new Dimension(0, 360)); // 增加高度以适应新的卡片高度
        setMinimumSize(new Dimension(0, 360));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));
        
        // 创建标题面板
        JPanel titlePanel = createTitlePanel();
        
        // 创建卡片容器
        cardContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));
        cardContainer.setOpaque(false);
        cardContainer.setPreferredSize(new Dimension(0, 320)); // 增加高度以适应新的卡片高度
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
     * 设置当前用户
     * @param currentUser 当前用户
     */
    public void setCurrentUser(UserVO currentUser) {
        this.currentUser = currentUser;
        // 更新所有现有卡片的用户信息
        for (CourseClassCard card : classCards) {
            card.setCurrentUser(currentUser);
        }
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
            CourseClassCard card = new CourseClassCard(course, this, currentUser);
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
    
    /**
     * 获取卡片列表
     * @return 卡片列表
     */
    public List<CourseClassCard> getClassCards() {
        return classCards;
    }
    
    /**
     * 刷新卡片显示
     * 当课程数据更新后调用此方法刷新卡片内容
     */
    public void refreshCards() {
        for (CourseClassCard card : classCards) {
            // 从最新的课程数据中查找对应的课程信息
            String courseCode = card.getCourse().getCourseCode();
            List<CourseVO> courseClasses = courseClassesMap.get(courseCode);
            if (courseClasses != null) {
                for (CourseVO course : courseClasses) {
                    if (course.getCourseId().equals(card.getCourse().getCourseId())) {
                        card.updateCourse(course);
                        break;
                    }
                }
            }
        }
        cardContainer.revalidate();
        cardContainer.repaint();
    }
    
    /**
     * 删除指定的教学班卡片
     * @param courseId 要删除的课程ID
     */
    public void removeCourseCard(Integer courseId) {
        // 从卡片列表中移除
        CourseClassCard cardToRemove = null;
        for (CourseClassCard card : classCards) {
            if (card.getCourse().getCourseId().equals(courseId)) {
                cardToRemove = card;
                break;
            }
        }
        
        if (cardToRemove != null) {
            classCards.remove(cardToRemove);
            cardContainer.remove(cardToRemove);
            
            // 从课程分组数据中移除
            String courseCode = cardToRemove.getCourse().getCourseCode();
            List<CourseVO> courseClasses = courseClassesMap.get(courseCode);
            if (courseClasses != null) {
                courseClasses.removeIf(course -> course.getCourseId().equals(courseId));
                
                // 如果该课程代码下没有其他教学班，从分组数据中移除整个课程
                if (courseClasses.isEmpty()) {
                    courseClassesMap.remove(courseCode);
                }
            }
            
            // 刷新显示
            cardContainer.revalidate();
            cardContainer.repaint();
            
            // 如果当前没有卡片了，隐藏面板
            if (classCards.isEmpty()) {
                hideCourseClasses();
            }
        }
    }
    
    /**
     * 刷新选课记录表格
     * 这个方法会被CourseClassCard调用，用于在选课/退选成功后刷新选课记录表格
     */
    public void refreshEnrollmentTable() {
        // 通过反射或事件机制通知父组件刷新选课记录表格
        // 这里我们通过查找父组件中的StudentEnrollmentTablePanel来实现
        Container parent = getParent();
        while (parent != null) {
            if (parent instanceof javax.swing.JTabbedPane) {
                javax.swing.JTabbedPane tabbedPane = (javax.swing.JTabbedPane) parent;
                // 查找选课记录选项卡
                for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                    if ("选课记录".equals(tabbedPane.getTitleAt(i))) {
                        java.awt.Component tabComponent = tabbedPane.getComponentAt(i);
                        if (tabComponent instanceof Container) {
                            refreshEnrollmentTableInContainer((Container) tabComponent);
                        }
                        return;
                    }
                }
            }
            parent = parent.getParent();
        }
    }
    
    /**
     * 在容器中查找并刷新选课记录表格
     * @param container 容器
     */
    private void refreshEnrollmentTableInContainer(Container container) {
        for (java.awt.Component component : container.getComponents()) {
            if (component instanceof StudentEnrollmentTablePanel) {
                ((StudentEnrollmentTablePanel) component).refreshData();
                return;
            } else if (component instanceof Container) {
                refreshEnrollmentTableInContainer((Container) component);
            }
        }
    }
}
