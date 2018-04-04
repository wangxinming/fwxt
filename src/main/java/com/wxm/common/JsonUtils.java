package com.wxm.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

public class JsonUtils {
    // fastjson 的序列化配置
    public final static SerializeConfig fastjson_serializeConfig_noYear = new SerializeConfig();
    public final static SerializeConfig      fastjson_serializeConfig_time   = new SerializeConfig();
    public final static SerializeConfig      fastjson_free_datetime          = new SerializeConfig();

    // 默认打出所有属性(即使属性值为null)|属性排序输出,为了配合历史记录
    private final static SerializerFeature[] fastJsonFeatures                = { SerializerFeature.WriteMapNullValue,
            SerializerFeature.WriteEnumUsingToString, SerializerFeature.SortField };
    private final static SerializerFeature[] fastJsonFeatures_dateFormat             = { SerializerFeature.WriteMapNullValue,
            SerializerFeature.WriteEnumUsingToString, SerializerFeature.SortField,
            SerializerFeature.WriteDateUseDateFormat,SerializerFeature.WriteNullStringAsEmpty,SerializerFeature.WriteNullNumberAsZero };

    public static <T> T parseObject(String item, Class<T> clazz) {
        if (StringUtils.isBlank(item)) {
            return null;
        }
        return JSON.parseObject(item, clazz);
    }

    public static final <T> List<T> parseArray(String text, Class<T> clazz) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        return JSON.parseArray(text, clazz);
    }

    public static String toJsonString(Object object) {
        return toJsonString(object, fastjson_serializeConfig_noYear);
    }

    /**
     * Date format: yyyy-MM-dd HH:mm:ss
     * @param object
     * @return
     */
    public static String toJsonStringFormatDate(Object object) {
        return JSON.toJSONString(object, fastJsonFeatures_dateFormat);
    }

    /**
     *
     * @param object
     * @param format
     * @return
     */
    public static String toJsonStringFormatDate(Object object,String format) {
        return JSON.toJSONStringWithDateFormat(object, format, fastJsonFeatures_dateFormat);
    }


    public static String toJsonString(Object object, SerializeConfig serializeConfig) {
        if (null == object) {
            return "";
        }
        return JSON.toJSONString(object, serializeConfig, fastJsonFeatures);
    }

    public static Map<?, ?> jsonToMap(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        return (Map<?, ?>) JSON.parse(json);
    }

    public static Map<?, ?> objectToMap(Object object) {

        String json = toJsonString(object);
        if (StringUtils.isBlank(json)) {
            return null;
        }

        return (Map<?, ?>) JSON.parse(json);
    }

    public static String mapToJson(Map<?, ?> params) {
        if (null == params || params.isEmpty()) {
            return null;
        }
        return JSON.toJSONString(params, true);
    }
}

