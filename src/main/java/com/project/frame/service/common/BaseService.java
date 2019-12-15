package com.project.frame.service.common;

import com.github.pagehelper.PageInfo;

import java.io.Serializable;
import java.util.List;

/**
 * Service基础接口
 *
 * @author mxy
 * @date 2019/12/15
 */
public interface BaseService<T, ID extends Serializable> extends Serializable {

    /**
     * 动态查询T集合-分页
     *
     * @param entity T
     * @return T集合
     */
    PageInfo<T> selectList(T entity, PageInfo<T> pageInfo);

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
    T getById(ID id);

    /**
     * 保存T
     *
     * @param entity T
     * @return T
     */
    T save(T entity);

    /**
     * 更新T
     *
     * @param entity T
     * @return T
     */
    T update(T entity);

    /**
     * 根据ID删除T
     *
     * @param id 主键ID
     * @return 被影响的数据行数
     */
    int delete(ID id);
}
