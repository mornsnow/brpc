package com.brpc.plugin;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto;

/**
 * 生成服务提供者代码
 * 
 * @author joe
 */
public final class PrintServiceServerImplFile extends AbstractPrint {

    private Map<String, String>         pojoTypeCache;

    private List<MethodDescriptorProto> serviceMethods;

    public PrintServiceServerImplFile(String fileRootPath, String sourcePackageName, String className){
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
        String souceClassName = className.substring(0, className.length() - 8);

        String packageName = super.getSourcePackageName().toLowerCase();
        List<String> fileData = Lists.newArrayList();
        fileData.add("package " + packageName + ";");
        fileData.add("import org.slf4j.Logger;");
        fileData.add("import org.slf4j.LoggerFactory;");

        fileData.add("import org.springframework.beans.factory.annotation.Autowired;");
        fileData.add("import com.brpc.client.*;");
        fileData.add("import io.grpc.stub.StreamObserver;");

        List<String> imports = Lists.newArrayList();
        List<String> methods = Lists.newArrayList();
        for (MethodDescriptorProto method : serviceMethods) {
            String outPutType = method.getOutputType();
            String inputType = method.getInputType();
            String methodName = StringUtils.uncapitalize(method.getName());

            String sourceIntputType = StringUtils.substringBeforeLast(inputType.substring(1), ".");

            String sourceoutPutType = StringUtils.substringBeforeLast(outPutType.substring(1), ".");

            inputType = CommonUtils.findPojoTypeFromCache(inputType, pojoTypeCache);
            outPutType = CommonUtils.findPojoTypeFromCache(outPutType, pojoTypeCache);
            String inputValue = CommonUtils.findNotIncludePackageType(inputType).toLowerCase();

            imports.add("import " + inputType + ";");
            imports.add("import " + outPutType + ";");

            String inputJavaType = StringUtils.substringAfterLast(inputType, ".");
            String outJavaType = StringUtils.substringAfterLast(outPutType, ".");

            String inputProto = pojoTypeCache.get(inputJavaType + "_outclass");
            String outProto = pojoTypeCache.get(outJavaType + "_outclass");

            String methodStr = "\n    public void " + methodName + "(" + sourceIntputType + "." + inputProto + "."
                               + inputJavaType + " request" + "," + "StreamObserver<" + sourceoutPutType + "."
                               + outProto + "." + outJavaType + "> responseObserver" + "){";
            methods.add(methodStr);
            // methods.add(" return invoker.invoke(\""+packageName+"."+souceClassName+":"+methodName+"\"," + inputValue
            // + ");");
            // methods.add(" return null;");
            methods.add("        try {");
            methods.add("            " + outJavaType + " result = invoker." + methodName + "( (new " + inputJavaType
                        + "()).getInstance(request));");
            methods.add("            responseObserver.onNext(result.convert());");
            methods.add("        } catch (Exception e) {");
            methods.add("            logger.error(\"brpc invoke error:\", e);");
            methods.add("            responseObserver.onError(new RuntimeException(\"brpc invoke error by \" + Utils.getLocal(), e));");
            methods.add("        }");
            methods.add("        responseObserver.onCompleted();\n");
            methods.add("    }");

        }
        fileData.addAll(imports);
        fileData.add("\n");
        fileData.add("public class " + className + " extends " + souceClassName + "Grpc." + souceClassName
                     + "ImplBase {");
        fileData.add("\n    private static final Logger logger = LoggerFactory.getLogger(" + souceClassName
                     + ".class);");

        fileData.add("\n    @Autowired");
        fileData.add("    private " + souceClassName + " invoker;");
        fileData.addAll(methods);

        fileData.add("\n    public void setInvoker(" + souceClassName + " invoker){ this.invoker = invoker;}");

        fileData.add("}");
        return fileData;
    }

}
