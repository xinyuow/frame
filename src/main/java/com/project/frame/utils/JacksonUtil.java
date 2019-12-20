package com.project.frame.utils;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

/**
 * jackson工具类
 */
public class JacksonUtil {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 设置默认日期格式
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        //提供其它默认设置
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setFilters(new SimpleFilterProvider().setFailOnUnknownId(false));
    }

    /**
     * 使用泛型方法，把json字符串转换为相应的JavaBean对象。
     * (1)转换为普通JavaBean：readValue(json,Student.class)
     * (2)转换为List,如List<Student>,将第二个参数传递为Student
     * [].class.然后使用Arrays.asList();方法把得到的数组转换为特定类型的List
     *
     * @param jsonStr
     * @param valueType
     * @return
     */
    public static <T> T readValue(String jsonStr, Class<T> valueType) {

        try {
            return objectMapper.readValue(jsonStr, valueType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * json数组转List
     *
     * @param jsonStr
     * @param valueTypeRef
     * @return
     */
    public static <T> T readValue(String jsonStr, TypeReference<T> valueTypeRef) {

        try {
            return objectMapper.readValue(jsonStr, valueTypeRef);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 将对象转换为字符串类型
     *
     * @param value
     * @return
     * @throws IOException
     */
    public static String toJsonStr(Object value) throws IOException {
        return objectMapper.writeValueAsString(value);
    }


    /**
     * 将对象转化为字符串类型，只包括对应的属性类型
     *
     * @param value
     * @param properties
     * @return
     * @throws IOException
     */
    public static String toJsonStr(Object value, String[] properties) throws IOException {

        return objectMapper.writer(
                new SimpleFilterProvider().addFilter(
                        AnnotationUtils.getValue(
                                AnnotationUtils.findAnnotation(value.getClass(), JsonFilter.class))
                                .toString(), SimpleBeanPropertyFilter
                                .filterOutAllExcept(properties)))
                .writeValueAsString(value);

    }


    /**
     * 将对象转化为字符串类型，并忽略部分属性
     *
     * @param value
     * @param properties2Exclude
     * @return
     * @throws IOException
     */
    public static String toJsonStrWithExcludeProperties(Object value, String[] properties2Exclude) throws IOException {
        return objectMapper.writer(
                new SimpleFilterProvider().addFilter(
                        AnnotationUtils.getValue(
                                AnnotationUtils.findAnnotation(value.getClass(), JsonFilter.class))
                                .toString(), SimpleBeanPropertyFilter
                                .serializeAllExcept(properties2Exclude)))
                .writeValueAsString(value);

    }

    /**
     * @param out
     * @param value
     * @throws IOException
     */
    public static void writeJsonStr(OutputStream out, Object value)
            throws IOException {
        objectMapper.writeValue(out, value);
    }


    /**
     * 将流数据写入对象value的对应属性properties中
     *
     * @param out
     * @param value
     * @param properties
     * @throws IOException
     */
    public static void writeJsonStr(OutputStream out, Object value, String[] properties)
            throws IOException {

        objectMapper.writer(
                new SimpleFilterProvider().addFilter(
                        AnnotationUtils.getValue(AnnotationUtils.findAnnotation(
                                value.getClass(), JsonFilter.class))
                                .toString(), SimpleBeanPropertyFilter
                                .filterOutAllExcept(properties)))
                .writeValue(out, value);

    }

    /**
     * 将流数据写入对象value中并排除部分属性
     *
     * @param out
     * @param value
     * @param properties2Exclude
     * @throws IOException
     */
    public static void writeJsonStrWithExcludeProperties(OutputStream out, Object value, String[] properties2Exclude) throws IOException {
        objectMapper.writer(
                new SimpleFilterProvider().addFilter(
                        AnnotationUtils.getValue(
                                AnnotationUtils.findAnnotation(value.getClass(), JsonFilter.class))
                                .toString(), SimpleBeanPropertyFilter
                                .serializeAllExcept(properties2Exclude)))
                .writeValue(out, value);

    }

}

