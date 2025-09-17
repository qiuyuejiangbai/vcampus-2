package client.ui.modules.course;

import common.vo.EnrollmentVO;
import common.vo.CourseScheduleVO;
import common.vo.CourseVO;
import client.net.ServerConnection;
import common.protocol.Message;
import common.protocol.MessageType;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.sql.Time;

/**
 * 学生课程表面板
 * 用于显示学生的图形化课程表
 */
public class StudentSchedulePanel extends JPanel {
    private JPanel scheduleGridPanel;  // 课程表网格面板
    private JLabel titleLabel;         // 标题标签
    private JScrollPane scrollPane;    // 滚动面板
    private List<EnrollmentVO> enrollmentList;   // 选课记录列表
    private List<CourseScheduleVO> scheduleList; // 课程时间表列表
    private final ServerConnection serverConnection; // 服务器连接
    private Map<Integer, CourseVO> courseMap; // 课程ID到课程对象的映射
    private StudentEnrollmentTablePanel enrollmentTablePanel; // 选课记录表格面板
    
    // 课程表网格参数
    private static final int DAYS_PER_WEEK = 7;  // 一周7天
    private static final int PERIOD_SLOTS = 6;   // 6个节次时间段
    private static final int CELL_WIDTH = 120;   // 单元格宽度
    private static final int CELL_HEIGHT = 80;   // 单元格高度（增加高度以显示更多信息）
    private static final int HEADER_HEIGHT = 30; // 表头高度
    
    // 节次定义（根据实际课程安排）
    private static final String[] PERIOD_LABELS = {
        "1-2节", "3-4节", "5-6节", "7-8节", "9-10节", "其他时间"
    };
    
    // 节次对应的时间范围（用于匹配数据库中的时间）
    private static final int[] PERIOD_START_HOURS = {8, 10, 14, 16, 19, 0};
    private static final int[] PERIOD_END_HOURS = {10, 12, 16, 18, 21, 24};
    
    private static final String[] DAY_LABELS = {
        "周一", "周二", "周三", "周四", "周五", "周六", "周日"
    };

    public StudentSchedulePanel() {
        this.serverConnection = ServerConnection.getInstance();
        this.enrollmentList = new ArrayList<>();
        this.scheduleList = new ArrayList<>();
        this.courseMap = new HashMap<>();
        this.enrollmentTablePanel = null;
        initComponents();
        setupLayout();
        setupEventHandlers();
        setupMessageListener();
        loadScheduleData();
    }
    
    public StudentSchedulePanel(StudentEnrollmentTablePanel enrollmentTablePanel) {
        this.serverConnection = ServerConnection.getInstance();
        this.enrollmentList = new ArrayList<>();
        this.scheduleList = new ArrayList<>();
        this.courseMap = new HashMap<>();
        this.enrollmentTablePanel = enrollmentTablePanel;
        initComponents();
        setupLayout();
        setupEventHandlers();
        setupMessageListener();
        
        // 延迟加载数据，确保选课记录表格已经加载完成
        SwingUtilities.invokeLater(() -> {
            loadScheduleData();
        });
    }

    private void initComponents() {
        // 创建标题
        titleLabel = new JLabel("我的课程表");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(UITheme.DARK_GRAY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(UITheme.createEmptyBorder(UITheme.PADDING_LARGE, 0, UITheme.PADDING_LARGE, 0));
        
        // 创建课程表网格面板
        scheduleGridPanel = new JPanel(new GridBagLayout());
        scheduleGridPanel.setBackground(UITheme.WHITE);
        scheduleGridPanel.setBorder(UITheme.createCardBorder());
        
        // 创建滚动面板
        scrollPane = new JScrollPane(scheduleGridPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.setBackground(UITheme.WHITE);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        setBackground(UITheme.WHITE);
        
        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        // 暂时不需要事件处理
    }

    /**
     * 设置消息监听器
     */
    private void setupMessageListener() {
        System.out.println("设置课程表消息监听器");
        
        // 监听选课记录数据
        serverConnection.setMessageListener(MessageType.GET_STUDENT_ENROLLMENTS_SUCCESS, message -> {
            SwingUtilities.invokeLater(() -> {
                System.out.println("收到选课记录成功消息");
                if (message.getData() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<EnrollmentVO> enrollments = (List<EnrollmentVO>) message.getData();
                    enrollmentList.clear();
                    enrollmentList.addAll(enrollments);
                    System.out.println("成功加载 " + enrollments.size() + " 条选课记录用于课程表");
                    
                    // 打印选课记录详情
                    for (EnrollmentVO enrollment : enrollments) {
                        System.out.println("选课记录: " + enrollment.getCourseName() + 
                            " 状态:" + enrollment.getStatus() + 
                            " 课程ID:" + enrollment.getCourseId());
                    }
                    
                    loadCourseSchedules();
                } else {
                    System.out.println("选课记录数据不是List类型: " + message.getData());
                }
            });
        });
        
        // 监听课程时间表数据
        serverConnection.setMessageListener(MessageType.GET_COURSE_SCHEDULES_SUCCESS, message -> {
            SwingUtilities.invokeLater(() -> {
                System.out.println("收到课程时间表成功消息");
                if (message.getData() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<CourseScheduleVO> schedules = (List<CourseScheduleVO>) message.getData();
                    scheduleList.clear();
                    scheduleList.addAll(schedules);
                    System.out.println("成功加载 " + schedules.size() + " 条课程时间表");
                    
                    // 打印每个课程时间表的详细信息
                    for (CourseScheduleVO schedule : schedules) {
                        System.out.println("课程时间表: " + schedule.getCourseName() + 
                            " 星期" + schedule.getDayOfWeek() + 
                            " 时间:" + schedule.getStartTime() + "-" + schedule.getEndTime() + 
                            " 教室:" + schedule.getClassroom());
                    }
                    
                    updateScheduleDisplay();
                } else {
                    System.out.println("课程时间表数据不是List类型: " + message.getData());
                }
            });
        });
        
        // 监听课程时间表失败
        serverConnection.setMessageListener(MessageType.GET_COURSE_SCHEDULES_FAIL, message -> {
            SwingUtilities.invokeLater(() -> {
                System.err.println("获取课程时间表失败: " + message.getData());
                updateScheduleDisplay();
            });
        });
    }

    /**
     * 加载课程表数据
     */
    private void loadScheduleData() {
        try {
            if (!serverConnection.isConnected()) {
                System.out.println("正在连接到服务器...");
                if (!serverConnection.connect()) {
                    System.err.println("无法连接到服务器");
                    return;
                }
            }
            
            // 如果有选课记录表格面板，从中获取数据
            if (enrollmentTablePanel != null) {
                System.out.println("从选课记录表格面板获取数据");
                enrollmentList.clear();
                enrollmentList.addAll(enrollmentTablePanel.getEnrollmentList());
                System.out.println("从选课记录表格面板获取到 " + enrollmentList.size() + " 条选课记录");
                
                // 打印选课记录详情
                for (EnrollmentVO enrollment : enrollmentList) {
                    System.out.println("选课记录: " + enrollment.getCourseName() + 
                        " 状态:" + enrollment.getStatus() + 
                        " 课程ID:" + enrollment.getCourseId());
                }
                
                // 如果选课记录为空，等待一下再重试
                if (enrollmentList.isEmpty()) {
                    System.out.println("选课记录为空，等待500ms后重试...");
                    Timer timer = new Timer(500, e -> {
                        enrollmentList.clear();
                        enrollmentList.addAll(enrollmentTablePanel.getEnrollmentList());
                        System.out.println("重试后获取到 " + enrollmentList.size() + " 条选课记录");
                        if (!enrollmentList.isEmpty()) {
                            loadCourseSchedules();
                        } else {
                            System.out.println("仍然没有选课记录，显示空课程表");
                            updateScheduleDisplay();
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    return;
                }
                
                loadCourseSchedules();
            } else {
                loadEnrollmentDataDirectly();
            }
        } catch (Exception e) {
            System.err.println("加载课程表数据时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 直接获取选课记录数据
     */
    private void loadEnrollmentDataDirectly() {
        try {
            // 创建获取学生选课记录列表的消息
            Message request = new Message();
            request.setType(MessageType.GET_STUDENT_ENROLLMENTS_REQUEST);
            request.setData(null);
            
            if (serverConnection.sendMessage(request)) {
                System.out.println("已发送获取选课记录请求");
                
                // 等待响应
                Thread.sleep(1000); // 等待1秒让服务器响应
                
                // 直接调用服务器方法获取数据（临时解决方案）
                // 这里我们需要一个更好的方法来获取数据
                System.out.println("尝试直接获取选课记录数据...");
            } else {
                System.err.println("发送获取选课记录请求失败");
            }
        } catch (Exception e) {
            System.err.println("直接获取选课记录数据时发生错误: " + e.getMessage());
        }
    }

    /**
     * 根据选课记录加载课程时间表
     */
    private void loadCourseSchedules() {
        if (enrollmentList.isEmpty()) {
            System.out.println("没有选课记录，显示空课程表");
            updateScheduleDisplay();
            return;
        }
        
        // 获取所有已选课程的ID
        List<Integer> courseIds = new ArrayList<>();
        for (EnrollmentVO enrollment : enrollmentList) {
            if ("enrolled".equals(enrollment.getStatus()) && enrollment.getCourseId() != null) {
                courseIds.add(enrollment.getCourseId());
                System.out.println("找到已选课程: " + enrollment.getCourseName() + " (ID: " + enrollment.getCourseId() + ")");
                
                // 同时保存课程信息到映射中
                if (enrollment.getCourse() != null) {
                    courseMap.put(enrollment.getCourseId(), enrollment.getCourse());
                } else {
                    // 如果没有课程对象，创建一个基本的课程信息
                    CourseVO course = new CourseVO();
                    course.setCourseId(enrollment.getCourseId());
                    course.setCourseName(enrollment.getCourseName());
                    course.setCourseCode(enrollment.getCourseCode());
                    courseMap.put(enrollment.getCourseId(), course);
                }
            }
        }
        
        System.out.println("总共找到 " + courseIds.size() + " 门已选课程");
        
        if (courseIds.isEmpty()) {
            System.out.println("没有已选课程，显示空课程表");
            updateScheduleDisplay();
            return;
        }
        
        // 请求课程时间表数据
        try {
            Message request = new Message();
            request.setType(MessageType.GET_COURSE_SCHEDULES_REQUEST);
            request.setData(courseIds);
            
            if (serverConnection.sendMessage(request)) {
                System.out.println("已发送获取课程时间表请求，课程数量: " + courseIds.size());
            } else {
                System.err.println("发送获取课程时间表请求失败");
                updateScheduleDisplay();
            }
        } catch (Exception e) {
            System.err.println("加载课程时间表时发生错误: " + e.getMessage());
            updateScheduleDisplay();
        }
    }

    /**
     * 更新课程表显示
     */
    private void updateScheduleDisplay() {
        System.out.println("=== 更新课程表显示 ===");
        System.out.println("选课记录数量: " + enrollmentList.size());
        System.out.println("课程时间表数量: " + scheduleList.size());
        System.out.println("课程映射数量: " + courseMap.size());
        
        // 打印所有课程时间表信息
        for (CourseScheduleVO schedule : scheduleList) {
            System.out.println("课程时间表: " + schedule.getCourseName() + 
                " 星期" + schedule.getDayOfWeek() + 
                " 时间:" + schedule.getStartTime() + "-" + schedule.getEndTime() + 
                " 教室:" + schedule.getClassroom());
        }
        
        scheduleGridPanel.removeAll();
        
        // 检查是否有选课记录
        if (enrollmentList.isEmpty()) {
            showNoEnrollmentMessage();
        } else if (scheduleList.isEmpty()) {
            showNoScheduleMessage();
        } else {
            // 创建表头
            createScheduleHeader();
            
            // 创建节次网格
            createScheduleGrid();
        }
        
        // 刷新显示
        scheduleGridPanel.revalidate();
        scheduleGridPanel.repaint();
        
        System.out.println("课程表显示更新完成");
    }
    
    /**
     * 显示没有选课记录的提示
     */
    private void showNoEnrollmentMessage() {
        JLabel messageLabel = new JLabel("<html><div style='text-align: center; font-size: 16px; color: #666;'>" +
            "您还没有选课记录<br/>请先在课程管理中选择课程</div></html>");
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setVerticalAlignment(SwingConstants.CENTER);
        messageLabel.setPreferredSize(new Dimension(800, 400));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        scheduleGridPanel.add(messageLabel, gbc);
    }
    
    /**
     * 显示没有课程时间表的提示
     */
    private void showNoScheduleMessage() {
        JLabel messageLabel = new JLabel("<html><div style='text-align: center; font-size: 16px; color: #666;'>" +
            "您已选课程暂无时间安排<br/>请联系任课教师确认上课时间</div></html>");
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setVerticalAlignment(SwingConstants.CENTER);
        messageLabel.setPreferredSize(new Dimension(800, 400));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        scheduleGridPanel.add(messageLabel, gbc);
    }

    /**
     * 创建课程表表头
     */
    private void createScheduleHeader() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        
        // 左上角空白单元格
        JLabel emptyLabel = new JLabel("");
        emptyLabel.setPreferredSize(new Dimension(80, HEADER_HEIGHT));
        emptyLabel.setBackground(UITheme.LIGHT_GRAY);
        emptyLabel.setOpaque(true);
            emptyLabel.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        gbc.gridx = 0;
        gbc.gridy = 0;
        scheduleGridPanel.add(emptyLabel, gbc);
        
        // 星期标签
        for (int day = 0; day < DAYS_PER_WEEK; day++) {
            JLabel dayLabel = new JLabel(DAY_LABELS[day]);
            dayLabel.setHorizontalAlignment(SwingConstants.CENTER);
            dayLabel.setVerticalAlignment(SwingConstants.CENTER);
            dayLabel.setFont(UITheme.SUBTITLE_FONT);
            dayLabel.setForeground(UITheme.WHITE);
            dayLabel.setBackground(UITheme.PRIMARY_GREEN);
            dayLabel.setOpaque(true);
            dayLabel.setPreferredSize(new Dimension(CELL_WIDTH, HEADER_HEIGHT));
            dayLabel.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
            
            gbc.gridx = day + 1;
            gbc.gridy = 0;
            scheduleGridPanel.add(dayLabel, gbc);
        }
    }

    /**
     * 创建课程表网格
     */
    private void createScheduleGrid() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        
        // 创建节次网格
        for (int periodSlot = 0; periodSlot < PERIOD_SLOTS; periodSlot++) {
            // 节次标签
            JLabel periodLabel = new JLabel(PERIOD_LABELS[periodSlot]);
            periodLabel.setHorizontalAlignment(SwingConstants.CENTER);
            periodLabel.setVerticalAlignment(SwingConstants.CENTER);
            periodLabel.setFont(UITheme.CONTENT_FONT);
            periodLabel.setForeground(UITheme.DARK_GRAY);
            periodLabel.setBackground(UITheme.LIGHT_GRAY);
            periodLabel.setOpaque(true);
            periodLabel.setPreferredSize(new Dimension(80, CELL_HEIGHT));
            periodLabel.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
            
            gbc.gridx = 0;
            gbc.gridy = periodSlot + 1;
            scheduleGridPanel.add(periodLabel, gbc);
            
            // 创建每天的课程单元格
            for (int day = 0; day < DAYS_PER_WEEK; day++) {
                JPanel cellPanel = createScheduleCell(day + 1, periodSlot);
                gbc.gridx = day + 1;
                gbc.gridy = periodSlot + 1;
                scheduleGridPanel.add(cellPanel, gbc);
            }
        }
    }

    /**
     * 创建课程表单元格
     */
    private JPanel createScheduleCell(int dayOfWeek, int periodSlot) {
        JPanel cellPanel = new JPanel(new BorderLayout());
        cellPanel.setPreferredSize(new Dimension(CELL_WIDTH, CELL_HEIGHT));
        cellPanel.setBackground(UITheme.WHITE);
        cellPanel.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        
        // 查找该节次的课程
        List<CourseScheduleVO> coursesInPeriod = findCoursesInPeriod(dayOfWeek, periodSlot);
        
        if (coursesInPeriod.isEmpty()) {
            // 空单元格
            cellPanel.setBackground(UITheme.WHITE);
        } else {
            // 有课程的单元格
            cellPanel.setBackground(UITheme.VERY_LIGHT_GREEN);
            
            // 创建课程信息标签
            JLabel courseLabel = new JLabel();
            courseLabel.setHorizontalAlignment(SwingConstants.CENTER);
            courseLabel.setVerticalAlignment(SwingConstants.CENTER);
            courseLabel.setFont(UITheme.SMALL_FONT);
            courseLabel.setForeground(UITheme.DARK_GRAY);
            
            if (coursesInPeriod.size() == 1) {
                // 单个课程
                CourseScheduleVO schedule = coursesInPeriod.get(0);
                CourseVO course = courseMap.get(schedule.getCourseId());
                String courseName = course != null ? course.getCourseName() : "未知课程";
                String location = schedule.getLocation();
                String teacherName = schedule.getTeacherName();
                courseLabel.setText("<html><div style='text-align: center;'>" + 
                    "<b>" + courseName + "</b><br/>" + 
                    location + "<br/>" + 
                    (teacherName != null ? teacherName : "") + "</div></html>");
            } else {
                // 多个课程（时间冲突）
                StringBuilder text = new StringBuilder("<html><div style='text-align: center;'>");
                for (int i = 0; i < Math.min(coursesInPeriod.size(), 2); i++) {
                    CourseScheduleVO schedule = coursesInPeriod.get(i);
                    CourseVO course = courseMap.get(schedule.getCourseId());
                    String courseName = course != null ? course.getCourseName() : "未知课程";
                    text.append("<b>").append(courseName).append("</b>");
                    if (i < Math.min(coursesInPeriod.size(), 2) - 1) {
                        text.append("<br/>");
                    }
                }
                if (coursesInPeriod.size() > 2) {
                    text.append("<br/>...");
                }
                text.append("</div></html>");
                courseLabel.setText(text.toString());
                cellPanel.setBackground(new Color(255, 200, 200)); // 冲突颜色
            }
            
            cellPanel.add(courseLabel, BorderLayout.CENTER);
        }
        
        return cellPanel;
    }

    /**
     * 查找指定节次的所有课程
     */
    private List<CourseScheduleVO> findCoursesInPeriod(int dayOfWeek, int periodSlot) {
        List<CourseScheduleVO> result = new ArrayList<>();
        
        System.out.println("查找星期" + dayOfWeek + " " + PERIOD_LABELS[periodSlot] + " 的课程，总课程数: " + scheduleList.size());
        
        for (CourseScheduleVO schedule : scheduleList) {
            System.out.println("检查课程: " + schedule.getCourseName() + 
                " 星期" + schedule.getDayOfWeek() + 
                " 时间:" + schedule.getStartTime());
            
            if (schedule.getDayOfWeek() == dayOfWeek && 
                isPeriodMatch(schedule, periodSlot)) {
                result.add(schedule);
                System.out.println("找到课程: " + schedule.getCourseName() + 
                    " 星期" + dayOfWeek + " " + PERIOD_LABELS[periodSlot] + 
                    " 教室:" + schedule.getClassroom());
            }
        }
        
        System.out.println("星期" + dayOfWeek + " " + PERIOD_LABELS[periodSlot] + " 找到 " + result.size() + " 门课程");
        return result;
    }

    /**
     * 检查课程时间是否匹配指定节次
     */
    private boolean isPeriodMatch(CourseScheduleVO schedule, int periodSlot) {
        if (schedule.getStartTime() == null) {
            System.out.println("课程 " + schedule.getCourseName() + " 没有开始时间");
            return false;
        }
        
        Time startTime = schedule.getStartTime();
        int hour = startTime.getHours();
        
        System.out.println("检查课程 " + schedule.getCourseName() + 
            " 时间:" + startTime + " 小时:" + hour + 
            " 节次:" + periodSlot + " (" + PERIOD_LABELS[periodSlot] + ")");
        
        // 根据节次判断
        if (periodSlot >= 0 && periodSlot < PERIOD_START_HOURS.length) {
            int startHour = PERIOD_START_HOURS[periodSlot];
            int endHour = PERIOD_END_HOURS[periodSlot];
            
            System.out.println("节次 " + periodSlot + " 时间范围: " + startHour + "-" + endHour);
            
            // 特殊处理"其他时间"（最后一个节次）
            if (periodSlot == PERIOD_START_HOURS.length - 1) {
                // 检查是否不在任何标准节次内
                for (int i = 0; i < PERIOD_START_HOURS.length - 1; i++) {
                    if (hour >= PERIOD_START_HOURS[i] && hour < PERIOD_END_HOURS[i]) {
                        System.out.println("课程在标准节次 " + i + " 内，不属于其他时间");
                        return false; // 在标准节次内，不属于"其他时间"
                    }
                }
                System.out.println("课程属于其他时间");
                return true; // 不在任何标准节次内，属于"其他时间"
            } else {
                boolean match = hour >= startHour && hour < endHour;
                System.out.println("节次匹配结果: " + match);
                return match;
            }
        }
        
        System.out.println("节次索引超出范围");
        return false;
    }

    /**
     * 刷新课程表数据
     */
    public void refreshData() {
        loadScheduleData();
    }
}
