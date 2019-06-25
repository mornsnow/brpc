package com.brpc.plugin;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * @author shimingliu 2016年12月21日 下午3:40:44
 * @version CommonUtils.java, v 0.0.1 2016年12月21日 下午3:40:44 shimingliu
 */
public final class CommonUtils {

    private CommonUtils(){

    }

    public static String findPojoTypeFromCache(String sourceType, Map<String, String> pojoTypes) {
        String type = StringUtils.substring(sourceType, StringUtils.lastIndexOf(sourceType, ".") + 1);
        return pojoTypes.get(type);
    }

    public static String findNotIncludePackageType(String sourceType) {
        String type = StringUtils.substring(sourceType, StringUtils.lastIndexOf(sourceType, ".") + 1);
        return type;
    }

}
