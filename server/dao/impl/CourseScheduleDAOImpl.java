package server.dao.impl;

import common.vo.CourseScheduleVO;
import server.dao.CourseScheduleDAO;
import server.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 课程时间表数据访问实现类
 */
public class CourseScheduleDAOImpl implements CourseScheduleDAO {
    
    @Override
    public CourseScheduleVO findById(Integer id) {
        if (id == null) return null;
        
        String sql = "SELECT * FROM course_schedules WHERE schedule_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCourseScheduleVO(rs);
            }
        } catch (SQLException e) {
            System.err.println("查询课程时间表失败: " + e.getMessage());
        }
        
        return null;
    }
    
    @Override
    public List<CourseScheduleVO> findAll() {
        String sql = "SELECT * FROM course_schedules ORDER BY day_of_week, start_time";
        List<CourseScheduleVO> schedules = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                schedules.add(mapResultSetToCourseScheduleVO(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询所有课程时间表失败: " + e.getMessage());
        }
        
        return schedules;
    }
    
    @Override
    public Integer insert(CourseScheduleVO schedule) {
        if (schedule == null) return null;
        
        String sql = "INSERT INTO course_schedules (course_id, day_of_week, start_time, end_time, classroom, building, weeks) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, schedule.getCourseId());
            stmt.setInt(2, schedule.getDayOfWeek());
            stmt.setTime(3, schedule.getStartTime());
            stmt.setTime(4, schedule.getEndTime());
            stmt.setString(5, schedule.getClassroom());
            stmt.setString(6, schedule.getBuilding());
            stmt.setString(7, schedule.getWeeks());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("插入课程时间表失败: " + e.getMessage());
        }
        
        return null;
    }
    
    @Override
    public boolean update(CourseScheduleVO schedule) {
        if (schedule == null || schedule.getId() == null) return false;
        
        String sql = "UPDATE course_schedules SET course_id = ?, day_of_week = ?, start_time = ?, end_time = ?, classroom = ?, building = ?, weeks = ? WHERE schedule_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, schedule.getCourseId());
            stmt.setInt(2, schedule.getDayOfWeek());
            stmt.setTime(3, schedule.getStartTime());
            stmt.setTime(4, schedule.getEndTime());
            stmt.setString(5, schedule.getClassroom());
            stmt.setString(6, schedule.getBuilding());
            stmt.setString(7, schedule.getWeeks());
            stmt.setInt(8, schedule.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("更新课程时间表失败: " + e.getMessage());
        }
        
        return false;
    }
    
    public boolean delete(Integer id) {
        if (id == null) return false;
        
        String sql = "DELETE FROM course_schedules WHERE schedule_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("删除课程时间表失败: " + e.getMessage());
        }
        
        return false;
    }
    
    @Override
    public boolean deleteById(Integer id) {
        return delete(id);
    }
    
    @Override
    public boolean existsById(Integer id) {
        if (id == null) return false;
        
        String sql = "SELECT COUNT(*) FROM course_schedules WHERE schedule_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("检查课程时间表是否存在失败: " + e.getMessage());
        }
        
        return false;
    }
    
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM course_schedules";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("统计课程时间表数量失败: " + e.getMessage());
        }
        
        return 0;
    }
    
    @Override
    public List<CourseScheduleVO> findByCourseId(Integer courseId) {
        if (courseId == null) return new ArrayList<>();
        
        String sql = "SELECT cs.*, c.course_name, c.course_code, t.name AS teacher_name " +
                    "FROM course_schedules cs " +
                    "LEFT JOIN courses c ON cs.course_id = c.course_id " +
                    "LEFT JOIN teachers t ON c.teacher_id = t.teacher_id " +
                    "WHERE cs.course_id = ? " +
                    "ORDER BY cs.day_of_week, cs.start_time";
        
        List<CourseScheduleVO> schedules = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                CourseScheduleVO schedule = mapResultSetToCourseScheduleVO(rs);
                // 设置关联信息
                schedule.setCourseName(rs.getString("course_name"));
                schedule.setCourseCode(rs.getString("course_code"));
                schedule.setTeacherName(rs.getString("teacher_name"));
                schedules.add(schedule);
            }
        } catch (SQLException e) {
            System.err.println("根据课程ID查询课程时间表失败: " + e.getMessage());
        }
        
        return schedules;
    }
    
    @Override
    public List<CourseScheduleVO> findByCourseIds(List<Integer> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) return new ArrayList<>();
        
        // 构建IN子句
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT cs.*, c.course_name, c.course_code, t.name AS teacher_name ");
        sqlBuilder.append("FROM course_schedules cs ");
        sqlBuilder.append("LEFT JOIN courses c ON cs.course_id = c.course_id ");
        sqlBuilder.append("LEFT JOIN teachers t ON c.teacher_id = t.teacher_id ");
        sqlBuilder.append("WHERE cs.course_id IN (");
        
        for (int i = 0; i < courseIds.size(); i++) {
            if (i > 0) sqlBuilder.append(",");
            sqlBuilder.append("?");
        }
        sqlBuilder.append(") ORDER BY cs.day_of_week, cs.start_time");
        
        List<CourseScheduleVO> schedules = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
            
            for (int i = 0; i < courseIds.size(); i++) {
                stmt.setInt(i + 1, courseIds.get(i));
            }
            
            System.out.println("[CourseScheduleDAO] 执行SQL: " + sqlBuilder.toString());
            System.out.println("[CourseScheduleDAO] 课程ID参数: " + courseIds);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                CourseScheduleVO schedule = mapResultSetToCourseScheduleVO(rs);
                // 设置关联信息
                schedule.setCourseName(rs.getString("course_name"));
                schedule.setCourseCode(rs.getString("course_code"));
                schedule.setTeacherName(rs.getString("teacher_name"));
                schedules.add(schedule);
                
                System.out.println("[CourseScheduleDAO] 找到课程时间表: " + 
                    schedule.getCourseName() + " 星期" + schedule.getDayOfWeek() + 
                    " 时间:" + schedule.getStartTime() + "-" + schedule.getEndTime());
            }
            
            System.out.println("[CourseScheduleDAO] 总共找到 " + schedules.size() + " 条课程时间表");
        } catch (SQLException e) {
            System.err.println("根据课程ID列表查询课程时间表失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return schedules;
    }
    
    @Override
    public List<CourseScheduleVO> findByDayOfWeek(Integer dayOfWeek) {
        if (dayOfWeek == null) return new ArrayList<>();
        
        String sql = "SELECT cs.*, c.course_name, c.course_code, t.name AS teacher_name " +
                    "FROM course_schedules cs " +
                    "LEFT JOIN courses c ON cs.course_id = c.course_id " +
                    "LEFT JOIN teachers t ON c.teacher_id = t.teacher_id " +
                    "WHERE cs.day_of_week = ? " +
                    "ORDER BY cs.start_time";
        
        List<CourseScheduleVO> schedules = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, dayOfWeek);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                CourseScheduleVO schedule = mapResultSetToCourseScheduleVO(rs);
                // 设置关联信息
                schedule.setCourseName(rs.getString("course_name"));
                schedule.setCourseCode(rs.getString("course_code"));
                schedule.setTeacherName(rs.getString("teacher_name"));
                schedules.add(schedule);
            }
        } catch (SQLException e) {
            System.err.println("根据星期几查询课程时间表失败: " + e.getMessage());
        }
        
        return schedules;
    }
    
    @Override
    public List<CourseScheduleVO> findByClassroom(String classroom) {
        if (classroom == null || classroom.trim().isEmpty()) return new ArrayList<>();
        
        String sql = "SELECT cs.*, c.course_name, c.course_code, t.name AS teacher_name " +
                    "FROM course_schedules cs " +
                    "LEFT JOIN courses c ON cs.course_id = c.course_id " +
                    "LEFT JOIN teachers t ON c.teacher_id = t.teacher_id " +
                    "WHERE cs.classroom = ? " +
                    "ORDER BY cs.day_of_week, cs.start_time";
        
        List<CourseScheduleVO> schedules = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, classroom);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                CourseScheduleVO schedule = mapResultSetToCourseScheduleVO(rs);
                // 设置关联信息
                schedule.setCourseName(rs.getString("course_name"));
                schedule.setCourseCode(rs.getString("course_code"));
                schedule.setTeacherName(rs.getString("teacher_name"));
                schedules.add(schedule);
            }
        } catch (SQLException e) {
            System.err.println("根据教室查询课程时间表失败: " + e.getMessage());
        }
        
        return schedules;
    }
    
    @Override
    public boolean hasTimeConflict(Integer dayOfWeek, Time startTime, Time endTime, 
                                 String classroom, Integer excludeScheduleId) {
        if (dayOfWeek == null || startTime == null || endTime == null) return false;
        
        String sql = "SELECT COUNT(*) FROM course_schedules " +
                    "WHERE day_of_week = ? AND classroom = ? " +
                    "AND ((start_time < ? AND end_time > ?) OR " +
                    "(start_time < ? AND end_time > ?) OR " +
                    "(start_time >= ? AND end_time <= ?))";
        
        if (excludeScheduleId != null) {
            sql += " AND schedule_id != ?";
        }
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, dayOfWeek);
            stmt.setString(2, classroom);
            stmt.setTime(3, endTime);
            stmt.setTime(4, startTime);
            stmt.setTime(5, endTime);
            stmt.setTime(6, startTime);
            stmt.setTime(7, startTime);
            stmt.setTime(8, endTime);
            
            if (excludeScheduleId != null) {
                stmt.setInt(9, excludeScheduleId);
            }
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("检查时间冲突失败: " + e.getMessage());
        }
        
        return false;
    }
    
    @Override
    public boolean deleteByCourseId(Integer courseId) {
        if (courseId == null) return false;
        
        String sql = "DELETE FROM course_schedules WHERE course_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, courseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("根据课程ID删除课程时间表失败: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * 将ResultSet映射为CourseScheduleVO对象
     */
    private CourseScheduleVO mapResultSetToCourseScheduleVO(ResultSet rs) throws SQLException {
        CourseScheduleVO schedule = new CourseScheduleVO();
        schedule.setId(rs.getInt("schedule_id"));
        schedule.setCourseId(rs.getInt("course_id"));
        schedule.setDayOfWeek(rs.getInt("day_of_week"));
        schedule.setStartTime(rs.getTime("start_time"));
        schedule.setEndTime(rs.getTime("end_time"));
        schedule.setClassroom(rs.getString("classroom"));
        schedule.setBuilding(rs.getString("building"));
        schedule.setWeeks(rs.getString("weeks"));
        return schedule;
    }
}
