package com.project.frame.mapper.common;

import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.List;

/**
 * Mapper基础接口
 *
 * @author mxy
 * @date 2019/12/15
 */
public interface BaseMapper<T, ID extends Serializable> extends Serializable {

    /**
     * 动态查询T集合
     *
     * @param entity T
     * @return T集合
     */
    List<T> selectList(T entity);

    /**
     * 根据ID获取T
     *
     * @param id 主键ID
     * @return T
     */
    T getById(@Param("id") ID id);

    /**
     * 保存T
     *
     * @param entity T
     * @return 被影响的数据行数
     */
    int save(T entity);

    /**
     * 更新T
     *
     * @param entity T
     * @return 被影响的数据行数
     */
    int update(T entity);

    /**
     * 根据ID删除T
     *
     * @param id 主键ID
     * @return 被影响的数据行数
     */
    int delete(@Param("id") ID id);
}
