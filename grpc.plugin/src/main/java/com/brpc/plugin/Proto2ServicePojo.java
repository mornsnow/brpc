package com.brpc.plugin;

import com.google.protobuf.DescriptorProtos.*;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.ProtocolStringList;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Proto2ServicePojo {

    private final String        discoveryRoot;

    private final String        generatePath;

    private final CommondProtoc commondProtoc;

    private Map<String, String> pojoTypes;

    private Proto2ServicePojo(String discoveryRoot, String generatePath){
        this.discoveryRoot = discoveryRoot;
        this.generatePath = generatePath;
        this.commondProtoc = CommondProtoc.configProtoPath(discoveryRoot);
    }

    public static Proto2ServicePojo forConfig(String discoveryRoot, String generatePath) {
        return new Proto2ServicePojo(discoveryRoot, generatePath);
    }

    public void generateFile(String protoPath) {
        try {
            if (pojoTypes == null) {
                pojoTypes = new HashMap<>();
            }
        } finally {
            FileDescriptorSet fileDescriptorSet = commondProtoc.invoke(protoPath);
            for (FileDescriptorProto fdp : fileDescriptorSet.getFileList()) {
                Pair<String, String> packageClassName = this.packageClassName(fdp.getOptions());
                if (packageClassName == null) {
                    continue;
                }
                ProtocolStringList dependencyList = fdp.getDependencyList();
                for (Iterator<String> it = dependencyList.iterator(); it.hasNext();) {
                    String dependencyPath = discoveryRoot + "/" + it.next();
                    generateFile(dependencyPath);
                }
                doPrint(fdp, packageClassName.getLeft(), packageClassName.getRight());
            }
        }
    }

    private Pair<String, String> packageClassName(FileOptions options) {
        String packageName = null;
        String className = null;
        for (Map.Entry<FieldDescriptor, Object> entry : options.getAllFields().entrySet()) {
            if (entry.getKey().getName().equals("java_package")) {
                packageName = entry.getValue().toString();
            }
            if (entry.getKey().getName().equals("java_outer_classname")) {
                className = entry.getValue().toString();
            }
        }
        if (packageName != null && className != null) {
            return new ImmutablePair<String, String>(packageName, className);
        }
        return null;
    }

    private void doPrint(FileDescriptorProto fdp, String javaPackage, String outerClassName) {
        List<DescriptorProto> messageDescList = fdp.getMessageTypeList();
        List<ServiceDescriptorProto> serviceDescList = fdp.getServiceList();
        List<EnumDescriptorProto> enumDescList = fdp.getEnumTypeList();
        printEnum(enumDescList, javaPackage, outerClassName);
        printMessage(messageDescList, javaPackage, outerClassName);
        printService(serviceDescList, javaPackage);
    }

    private void printService(List<ServiceDescriptorProto> serviceDescList, String javaPackage) {
        for (ServiceDescriptorProto serviceDesc : serviceDescList) {
            PrintServiceFile serviceFile = new PrintServiceFile(generatePath, javaPackage, serviceDesc.getName());
            try {
                serviceFile.setServiceMethods(serviceDesc.getMethodList());
                serviceFile.setPojoTypeCache(pojoTypes);
            } finally {
                serviceFile.print();
            }

            PrintServiceImplFile serviceImplFile = new PrintServiceImplFile(generatePath, javaPackage, serviceDesc.getName()+"_impl");
            try {
                serviceImplFile.setServiceMethods(serviceDesc.getMethodList());
                serviceImplFile.setPojoTypeCache(pojoTypes);
            } finally {
                serviceImplFile.print();
            }

            PrintServiceServerImplFile serviceServerImplFile = new PrintServiceServerImplFile(generatePath, javaPackage, serviceDesc.getName()+"_service");
            try {
                serviceServerImplFile.setServiceMethods(serviceDesc.getMethodList());
                serviceServerImplFile.setPojoTypeCache(pojoTypes);
            } finally {
                serviceServerImplFile.print();
            }
        }
    }

    private void printMessage(List<DescriptorProto> messageDescList, String javaPackage, String outerClassName) {
        for (DescriptorProto messageDesc : messageDescList) {
            String pojoClassType = messageDesc.getName();
            String pojoPackageName = javaPackage + "." + outerClassName;
            String fullpojoType = pojoPackageName.toLowerCase() + "." + pojoClassType;
            pojoTypes.put(pojoClassType, fullpojoType);
            pojoTypes.put(pojoClassType+"_outclass", outerClassName);
            pojoTypes.put(fullpojoType, fullpojoType);

            PrintMessageFile messageFile = new PrintMessageFile(generatePath, pojoPackageName, pojoClassType);
            try {
                messageFile.setMessageFields(messageDesc.getFieldList());
                messageFile.setPojoTypeCache(pojoTypes);
                messageFile.setSourceMessageDesc(messageDesc);
            } finally {
                messageFile.print();
            }
        }
    }

    private void printEnum(List<EnumDescriptorProto> enumDescList, String javaPackage, String outerClassName) {
        for (EnumDescriptorProto enumDesc : enumDescList) {
            String enumClassType = enumDesc.getName();
            String enumPackageName = javaPackage + "." + outerClassName;
            String fullpojoType = enumPackageName.toLowerCase() + "." + enumClassType;
            pojoTypes.put(enumClassType, fullpojoType);
            pojoTypes.put(enumClassType+"_outclass", outerClassName);
            pojoTypes.put(enumClassType+"_enum", outerClassName);
            pojoTypes.put(fullpojoType+"_enum", outerClassName);


            PrintEnumFile enumFile = new PrintEnumFile(generatePath, enumPackageName, enumClassType);
            try {
                enumFile.setEnumFields(enumDesc.getValueList());
            } finally {
                enumFile.print();
            }
        }
    }

}
