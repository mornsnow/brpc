package com.brpc.plugin;

import com.google.common.collect.Lists;
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public final class PrintServiceFile extends AbstractPrint {

    private Map<String, String>         pojoTypeCache;

    private List<MethodDescriptorProto> serviceMethods;

    public PrintServiceFile(String fileRootPath, String sourcePackageName, String className){
        super(fileRootPath, sourcePackageName, className);
    }

    public void setPojoTypeCache(Map<String, String> pojoTypeCache) {
        this.pojoTypeCache = pojoTypeCache;
    }

    public void setServiceMethods(List<MethodDescriptorProto> serviceMethods) {
        this.serviceMethods = serviceMethods;
    }

    @Override
    protected List<String> collectFileData() {
        String className = super.getClassName();
        String packageName = super.getSourcePackageName().toLowerCase();
        List<String> fileData = Lists.newArrayList();
        fileData.add("package " + packageName + ";");

        List<String> imports = Lists.newArrayList();
        List<String> methods = Lists.newArrayList();
        for (MethodDescriptorProto method : serviceMethods) {
            String outPutType = method.getOutputType();
            String inputType = method.getInputType();
            String methodName = method.getName();
            inputType = CommonUtils.findPojoTypeFromCache(inputType, pojoTypeCache);
            outPutType = CommonUtils.findPojoTypeFromCache(outPutType, pojoTypeCache);
            String inputValue = CommonUtils.findNotIncludePackageType(inputType).toLowerCase();

            imports.add("import " + inputType + ";");
            imports.add("import " + outPutType + ";");

            String methodStr = "    public " + StringUtils.substringAfterLast(outPutType, ".") + " " + StringUtils.uncapitalize(methodName) + "("
                               + StringUtils.substringAfterLast(inputType, ".") + " " + inputValue + ");";
            methods.add(methodStr);

        }
        fileData.addAll(imports);
        fileData.add("\n");
        fileData.add("public interface " + className + "{");
        fileData.addAll(methods);
        fileData.add("}");
        return fileData;
    }

}
