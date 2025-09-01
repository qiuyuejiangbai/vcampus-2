package server.dao;

import java.util.List;

/**
 * 基础DAO接口
 * 定义通用的CRUD操作方法
 * @param <T> 实体类型
 * @param <ID> 主键类型
 */
public interface BaseDAO<T, ID> {
    
    /**
     * 插入实体
     * @param entity 实体对象
     * @return 插入成功返回生成的主键，失败返回null
     */
    ID insert(T entity);
    
    /**
     * 根据主键删除实体
     * @param id 主键
     * @return 删除成功返回true，失败返回false
     */
    boolean deleteById(ID id);
    
    /**
     * 更新实体
     * @param entity 实体对象
     * @return 更新成功返回true，失败返回false
     */
    boolean update(T entity);
    
    /**
     * 根据主键查询实体
     * @param id 主键
     * @return 实体对象，不存在返回null
     */
    T findById(ID id);
    
    /**
     * 查询所有实体
     * @return 实体列表
     */
    List<T> findAll();
    
    /**
     * 统计实体总数
     * @return 实体总数
     */
    long count();
    
    /**
     * 检查实体是否存在
     * @param id 主键
     * @return 存在返回true，不存在返回false
     */
    boolean existsById(ID id);
}
