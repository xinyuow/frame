package com.project.frame.service.common.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.project.frame.mapper.common.BaseMapper;
import com.project.frame.service.common.BaseService;
import com.project.frame.utils.id.IDUtil;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Service基础实现类
 *
 * @author mxy
 * @date 2019/12/15
 */
@SuppressWarnings({"unchecked"})
public class BaseServiceImpl<T, ID extends Serializable> implements BaseService<T, ID> {
    private static final long serialVersionUID = -246325674843801562L;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected BaseMapper<T, ID> baseMapper;

    /**
     * 动态查询T集合-分页
     *
     * @param entity T
     * @return T集合
     */
    @Override
    public PageInfo<T> selectList(T entity, PageInfo<T> pageInfo) {
        if (pageInfo != null) {
            PageHelper.startPage(pageInfo.getPageNum(), pageInfo.getPageSize());
        }
        return new PageInfo(baseMapper.selectList(entity));
    }

    /**
     * 动态查询T集合
     *
     * @param entity T
     * @return T集合
     */
    @Override
    public List<T> selectList(T entity) {
        return baseMapper.selectList(entity);
    }

    /**
     * 根据ID获取T
     *
     * @param id 主键ID
     * @return T
     */
    @Override
    public T getById(ID id) {
        if (id == null) {
            return null;
        } else {
            return baseMapper.getById(id);
        }
    }

    /**
     * 保存T
     *
     * @param entity T
     * @return T
     */
    @Override
    public T save(T entity) {
        if (entity == null) {
            return null;
        }
        try {
            //id为字符串类型时需要设置默认值，否则认为id是数据库自动生成
            Class<?> idType = PropertyUtils.getPropertyType(entity, "id");
            if (idType == Long.class && PropertyUtils.getProperty(entity, "id") == null) {
                setPropertyValue(entity, "id", IDUtil.getId());
            }

            // 判断是否存在创建时间、修改时间两个字段，如果存在就设置值
            PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(entity);
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                if (descriptor.getName().equals("createDate")) {
                    setPropertyValue(entity, "createDate", new Date());
                } else if (descriptor.getName().equals("modifyDate")) {
                    setPropertyValue(entity, "modifyDate", new Date());
                }
            }
        } catch (Exception e) {
            log.info("pojo类：" + entity.getClass() + "无id属性");
        }
        return baseMapper.save(entity) > 0 ? entity : null;
    }

    /**
     * 更新T
     *
     * @param entity T
     * @return T
     */
    @Override
    public T update(T entity) {
        if (entity == null) {
            return null;
        }
        // 判断是否存在修改时间字段，如果存在就更新为当前值
        PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(entity);
        for (PropertyDescriptor descriptor : propertyDescriptors) {
            if (descriptor.getName().equals("modifyDate")) {
                setPropertyValue(entity, "modifyDate", new Date());
            }
        }
        return baseMapper.update(entity) > 0 ? entity : null;
    }

    /**
     * 根据ID删除T
     *
     * @param id 主键ID
     * @return 被影响的数据行数
     */
    @Override
    public int delete(ID id) {
        if (id == null) {
            return 0;
        } else {
            return baseMapper.delete(id);
        }
    }


    /* ********************************** 以下为私有方法 **********************************/

    /**
     * 设置对象的属性值,若属性值为空则进行设置
     *
     * @param entity   对象T
     * @param property 属性名称
     * @param value    属性值
     */
    private void setPropertyValue(T entity, String property, Object value) {
        setPropertyValue(entity, property, value, false);
    }

    /**
     * 设置对象的值
     *
     * @param entity   对象T
     * @param property 属性名称
     * @param value    属性值
     * @param isForced 是否强制设置
     */
    private void setPropertyValue(T entity, String property, Object value, boolean isForced) {
        try {
            Assert.notNull(entity, "实体类为空");
            Assert.hasText(property, "实体属性不存在");
            Assert.notNull(value, "实体属性值为空");
            Class propertyType = PropertyUtils.getPropertyType(entity, property);

            if (propertyType != value.getClass()) {
                return;
            }

            if (isForced) {
                PropertyUtils.setProperty(entity, property, value);
            } else {
                Object propValue = PropertyUtils.getProperty(entity, property);
                if (propValue == null) {
                    PropertyUtils.setProperty(entity, property, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
