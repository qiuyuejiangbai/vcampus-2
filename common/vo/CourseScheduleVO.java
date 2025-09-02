package common.vo;

import java.io.Serializable;
import java.sql.Time;

/**
 * 课程时间表值对象
 * 用于封装课程时间安排信息在客户端和服务器端之间传输
 */
public class CourseScheduleVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer id;             // 时间表ID
    private Integer courseId;       // 课程ID
    private Integer dayOfWeek;      // 星期几(1-7, 1=周一)
    private Time startTime;         // 开始时间
    private Time endTime;           // 结束时间
    private String classroom;       // 教室
    private String building;        // 教学楼
    private String weeks;           // 授课周次（如：1-16周）
    
    // 关联信息（用于显示）
    private String courseName;      // 课程名称
    private String courseCode;      // 课程代码
    private String teacherName;     // 教师姓名
    
    // 关联对象
    private CourseVO course;
    
    // 构造函数
    public CourseScheduleVO() {}
    
    public CourseScheduleVO(Integer courseId, Integer dayOfWeek, Time startTime, Time endTime, 
                           String classroom, String building) {
        this.courseId = courseId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.classroom = classroom;
        this.building = building;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getCourseId() {
        return courseId;
    }
    
    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }
    
    public Integer getDayOfWeek() {
        return dayOfWeek;
    }
    
    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
    
    public Time getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }
    
    public Time getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }
    
    public String getClassroom() {
        return classroom;
    }
    
    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }
    
    public String getBuilding() {
        return building;
    }
    
    public void setBuilding(String building) {
        this.building = building;
    }
    
    public String getWeeks() {
        return weeks;
    }
    
    public void setWeeks(String weeks) {
        this.weeks = weeks;
    }
    
    public String getCourseName() {
        return courseName;
    }
    
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    
    public String getCourseCode() {
        return courseCode;
    }
    
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }
    
    public String getTeacherName() {
        return teacherName;
    }
    
    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
    
    public CourseVO getCourse() {
        return course;
    }
    
    public void setCourse(CourseVO course) {
        this.course = course;
    }
    
    // 业务方法
    
    /**
     * 获取星期几的中文名称
     * @return 星期几的中文名称
     */
    public String getDayOfWeekName() {
        if (dayOfWeek == null) return "未知";
        switch (dayOfWeek) {
            case 1: return "周一";
            case 2: return "周二";
            case 3: return "周三";
            case 4: return "周四";
            case 5: return "周五";
            case 6: return "周六";
            case 7: return "周日";
            default: return "未知";
        }
    }
    
    /**
     * 获取时间范围字符串
     * @return 时间范围（如：08:00-09:50）
     */
    public String getTimeRange() {
        if (startTime != null && endTime != null) {
            return startTime.toString() + "-" + endTime.toString();
        }
        return "";
    }
    
    /**
     * 获取完整地点信息
     * @return 完整地点（如：教学楼A101）
     */
    public String getLocation() {
        if (building != null && classroom != null) {
            return building + classroom;
        } else if (classroom != null) {
            return classroom;
        } else if (building != null) {
            return building;
        }
        return "";
    }
    
    /**
     * 获取课程时间的完整描述
     * @return 完整描述（如：周一 08:00-09:50 教学楼A101）
     */
    public String getFullScheduleDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDayOfWeekName());
        if (startTime != null && endTime != null) {
            sb.append(" ").append(getTimeRange());
        }
        String location = getLocation();
        if (!location.isEmpty()) {
            sb.append(" ").append(location);
        }
        if (weeks != null && !weeks.isEmpty()) {
            sb.append(" (").append(weeks).append(")");
        }
        return sb.toString();
    }
    
    /**
     * 检查时间是否有冲突
     * @param other 另一个课程时间安排
     * @return true表示有冲突，false表示无冲突
     */
    public boolean hasTimeConflict(CourseScheduleVO other) {
        if (other == null || !this.dayOfWeek.equals(other.dayOfWeek)) {
            return false;
        }
        
        if (this.startTime == null || this.endTime == null || 
            other.startTime == null || other.endTime == null) {
            return false;
        }
        
        // 检查时间段是否重叠
        return !(this.endTime.before(other.startTime) || this.startTime.after(other.endTime));
    }
    
    /**
     * 检查教室是否有冲突
     * @param other 另一个课程时间安排
     * @return true表示有冲突，false表示无冲突
     */
    public boolean hasRoomConflict(CourseScheduleVO other) {
        if (!hasTimeConflict(other)) {
            return false;
        }
        
        String thisLocation = this.getLocation();
        String otherLocation = other.getLocation();
        
        return thisLocation.equals(otherLocation) && !thisLocation.isEmpty();
    }
    
    /**
     * 获取上课节次（根据时间计算）
     * @return 节次描述
     */
    public String getClassPeriod() {
        if (startTime == null) return "未知";
        
        int hour = startTime.getHours();
        if (hour >= 8 && hour < 10) {
            return "1-2节";
        } else if (hour >= 10 && hour < 12) {
            return "3-4节";
        } else if (hour >= 14 && hour < 16) {
            return "5-6节";
        } else if (hour >= 16 && hour < 18) {
            return "7-8节";
        } else if (hour >= 19 && hour < 21) {
            return "9-10节";
        } else {
            return "其他时间";
        }
    }
    
    /**
     * 计算课程时长（分钟）
     * @return 课程时长
     */
    public int getDurationMinutes() {
        if (startTime == null || endTime == null) return 0;
        
        long startMillis = startTime.getTime();
        long endMillis = endTime.getTime();
        return (int) ((endMillis - startMillis) / (1000 * 60));
    }
    
    /**
     * 检查是否为工作日
     * @return true表示是工作日，false表示是周末
     */
    public boolean isWeekday() {
        return dayOfWeek != null && dayOfWeek >= 1 && dayOfWeek <= 5;
    }
    
    /**
     * 检查是否为周末
     * @return true表示是周末，false表示是工作日
     */
    public boolean isWeekend() {
        return dayOfWeek != null && (dayOfWeek == 6 || dayOfWeek == 7);
    }
    
    @Override
    public String toString() {
        return "CourseScheduleVO{" +
                "id=" + id +
                ", courseId=" + courseId +
                ", courseName='" + courseName + '\'' +
                ", courseCode='" + courseCode + '\'' +
                ", dayOfWeek=" + dayOfWeek +
                ", timeRange='" + getTimeRange() + '\'' +
                ", location='" + getLocation() + '\'' +
                ", weeks='" + weeks + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CourseScheduleVO that = (CourseScheduleVO) obj;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
