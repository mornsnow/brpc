package com.brpc.plugin;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

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
