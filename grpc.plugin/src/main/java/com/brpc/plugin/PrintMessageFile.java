package com.brpc.plugin;

import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.protobuf.DescriptorProtos;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label;

/**
 * @author shimingliu 2016年12月21日 下午3:42:47
 * @version PrintMessageFile.java, v 0.0.1 2016年12月21日 下午3:42:47 shimingliu
 */
public final class PrintMessageFile extends AbstractPrint {

    private static final Logger        logger = LoggerFactory.getLogger(PrintMessageFile.class);

    private List<FieldDescriptorProto> messageFields;

    private Map<String, String>        pojoTypeCache;

    private DescriptorProto            sourceMessageDesc;

    public PrintMessageFile(String fileRootPath, String sourcePackageName, String className){
        super(fileRootPath, sourcePackageName, className);
    }

    public void setMessageFields(List<FieldDescriptorProto> messageFields) {
        this.messageFields = messageFields;
    }

    public void setPojoTypeCache(Map<String, String> pojoTypeCache) {
        this.pojoTypeCache = pojoTypeCache;
    }

    public void setSourceMessageDesc(DescriptorProto sourceMessageDesc) {
        this.sourceMessageDesc = sourceMessageDesc;
    }

    @Override
    protected List<String> collectFileData() {
        String sourePackageName = super.getSourcePackageName();
        String className = super.getClassName();
        String packageName = sourePackageName.toLowerCase();
        List<String> fileData = new ArrayList<>();
        fileData.add("package " + packageName + ";");
        fileData.add(System.getProperty("line.separator"));
        fileData.add("import java.util.*;");
        fileData.add(System.getProperty("line.separator"));
        fileData.add("public class " + className + " implements com.brpc.client.BindMessage<" + sourePackageName + "."
                     + className + "," + className + ">{");
        List<String> pojoData = new ArrayList<>();
        List<String> sets = new ArrayList<>();
        List<String> setInstances = new ArrayList<>();

        for (int i = 0; i < messageFields.size(); i++) {
            FieldDescriptorProto messageField = messageFields.get(i);
            String javaType = findJavaType(packageName, sourceMessageDesc, messageField);
            boolean isList = false;
            boolean isEnum = messageField.getType().equals(TYPE_ENUM);
            if (messageField.getLabel() == Label.LABEL_REPEATED && javaType != null) {
                if (!javaType.contains("Map<")) {
                    javaType = "List<" + javaType + ">";
                    isList = true;
                }
            }
            boolean isObject = false;
            boolean isMap = false;
            if (javaType.startsWith("Map<")) {
                isMap = true;
            } else if (messageField.getType().equals(FieldDescriptorProto.Type.TYPE_MESSAGE)) {
                isObject = true;
            }
            String fieldName = messageField.getName();
            if (StringUtils.contains(fieldName, '_')) {
                fieldName = StringUtils.replaceAll(WordUtils.capitalizeFully(fieldName, '_'), "_", "");
                fieldName = StringUtils.uncapitalize(fieldName);
            } else {
                fieldName = StringUtils.uncapitalize(fieldName);
            }

            fileData.add("    private " + javaType + " " + fieldName + ";");
            pojoData.add("    public " + javaType + " get" + captureName(fieldName) + "(){");
            pojoData.add("        return this." + fieldName + ";");
            pojoData.add("    }");
            pojoData.add(System.getProperty("line.separator"));
            pojoData.add("    public void set" + captureName(fieldName) + "(" + javaType + " " + fieldName + "){");
            pojoData.add("        this." + fieldName + "=" + fieldName + ";");
            pojoData.add("    }");
            pojoData.add(System.getProperty("line.separator"));
            initConvert(sets, setInstances, isEnum, isMap, isObject, isList, fieldName, javaType, sourePackageName,
                        messageField.getTypeName());
        }

        fileData.add(System.getProperty("line.separator"));
        fileData.addAll(pojoData);
        fileData.add("    public " + sourePackageName + "." + className + " convert(){");
        fileData.add("       " + sourePackageName + "." + className + ".Builder build = " + sourePackageName + "."
                     + className + ".newBuilder();");
        fileData.addAll(sets);
        fileData.add("        return build.build();");
        fileData.add("    }");

        fileData.add(System.getProperty("line.separator"));

        fileData.add("    public  " + className + " getInstance(" + sourePackageName + "." + className + " param){");
        fileData.add("        " + className + " build = new " + className + "();");
        fileData.addAll(setInstances);
        fileData.addAll(handleOneofDecls());
        fileData.add("        return build;");
        fileData.add("    }");

        fileData.add("}");
        return fileData;
    }

    private List<String> handleOneofDecls() {
        List<String> oneofDeslData = new ArrayList<>();
        for (FieldDescriptorProto messageField : messageFields) {
            List<DescriptorProtos.OneofDescriptorProto> oneofDeclList = this.sourceMessageDesc.getOneofDeclList();
            if (messageField.hasOneofIndex()) {
                String oneofData = "        if (param.get" + captureName(oneofDeclList.get(messageField.getOneofIndex()).getName()) + "Case().getNumber() == 0) {\n"
                        + "        " +  "    build.set" + captureName(messageField.getName()) + "(null);\n"
                        + "        }";
                oneofDeslData.add(oneofData);
            }
        }
        return oneofDeslData;
    }

    private void initConvert(List<String> sets, List<String> setInstances, boolean isEnum, boolean isMap,
                             boolean isObject, boolean isList, String fieldName, String javaType,
                             String sourePackageName, String typeName) {

        typeName = StringUtils.substring(typeName, 1);
        String sourceoutPutType = StringUtils.substringBeforeLast(typeName, ".");
        typeName = StringUtils.substringAfterLast(typeName, ".");
        boolean curIsEnum = pojoTypeCache.containsKey(sourePackageName.toLowerCase()+"."+typeName + "_enum");

        if (isList) {
            sets.add("        if(get" + captureName(fieldName) + "()!=null){");
            sets.add("          for(int i=0;i<get" + captureName(fieldName) + "().size();i++){");
            boolean curIsObject = StringUtils.contains(javaType, '.');
            String opt = "";
            if (curIsObject) {
                opt = ".convert()";
            }
            String pbType = CommonUtils.findNotIncludePackageType(javaType);

            pbType = StringUtils.replaceAll(pbType, ">", "");

            // 是枚举
            if (curIsEnum) {
                sets.add("            build.add" + captureName(fieldName) + "(" + sourceoutPutType + "."
                         + pojoTypeCache.get(typeName + "_outclass") + "." + pbType + ".forNumber(get"
                         + captureName(fieldName) + "().get(i).getNumber()));");
            } else {
                sets.add("            build.add" + captureName(fieldName) + "(get" + captureName(fieldName)
                         + "().get(i)" + opt + ");");
            }

            sets.add("          }");
            sets.add("        }");

            setInstances.add("        if(param.get" + captureName(fieldName) + "Count()>0){");
            if (curIsObject) {
                setInstances.add("          build.set" + captureName(fieldName) + "(new ArrayList<>());");
                setInstances.add("          for(int i=0;i<param.get" + captureName(fieldName) + "Count();i++){");
                String type = javaType.replaceAll("List<", "").replaceAll(">", "");
                if (curIsEnum) {
                    setInstances.add("        build.get" + captureName(fieldName) + "().add(" + type
                                     + ".forNumber(param.get" + captureName(fieldName) + "(i).getNumber()));");
                } else {
                    setInstances.add("            " + type + " ins = new " + type + "();");
                    setInstances.add("            build.get" + captureName(fieldName)
                                     + "().add(ins.getInstance(param.get" + captureName(fieldName) + "(i)));");
                }
                setInstances.add("        }");
            } else {
                setInstances.add("            build.set" + captureName(fieldName) + "(param.get"
                                 + captureName(fieldName) + "List().subList(0,param.get" + captureName(fieldName)
                                 + "Count()));");
            }
            setInstances.add("        }");
        } else if (isMap) {
            String type = javaType.replaceAll("Map<", "").replaceAll(">", "");
            String entryType = type.split(",")[1];
            String pbType = CommonUtils.findNotIncludePackageType(entryType);
            if (StringUtils.equals("String", pbType) || StringUtils.equals("Integer", pbType)
                || StringUtils.equals("Long", pbType) || StringUtils.equals("Double", pbType)
                || StringUtils.equals("Float", pbType) || StringUtils.equals("Boolean", pbType)) {
                pbType = null;
            }
            sets.add("        if(get" + captureName(fieldName) + "()!=null){");
            if (pbType == null) {
                sets.add("          build.putAll" + captureName(fieldName) + "(get" + captureName(fieldName) + "());");
            } else {
                sets.add("          get" + captureName(fieldName) + "().forEach((key, value) -> {");
                sets.add("            build.get" + captureName(fieldName) + "().put(key, value.convert());");
                sets.add("          });");
            }
            sets.add("        }");
            if (pbType == null) {
                setInstances.add("        build.set" + captureName(fieldName) + "(param.get" + captureName(fieldName)
                                 + "());");
            } else {
                setInstances.add("        build.set" + captureName(fieldName) + "(new HashMap());");
                setInstances.add("        param.get" + captureName(fieldName) + "().forEach((key, value) -> {");
                setInstances.add("          build.get" + captureName(fieldName) + "().put(key, (new " + pbType
                                 + "()).getInstance(value));");
                setInstances.add("        });");
            }
        } else if (isObject) {
            sets.add("        if(get" + captureName(fieldName) + "()!=null){");
            sets.add("            build.set" + captureName(fieldName) + "(get" + captureName(fieldName)
                     + "().convert());");
            sets.add("        }");

            setInstances.add("        if(param.has" + captureName(fieldName) + "()){");
            setInstances.add("            build.set" + captureName(fieldName) + "((new " + javaType + "()).getInstance("
                             + "param.get" + captureName(fieldName) + "()));");
            setInstances.add("        }");
        } else if (isEnum) {
            String pbType = CommonUtils.findNotIncludePackageType(javaType);
            sets.add("        if(get" + captureName(fieldName) + "()!=null){");

            sets.add("          build.set" + captureName(fieldName) + "(" + sourceoutPutType + "."
                     + pojoTypeCache.get(typeName + "_outclass") + "." + pbType + ".forNumber(get"
                     + captureName(fieldName) + "().getNumber()));");
            sets.add("        }");

            // setInstances.add(" if(param.get" + captureName(fieldName) + "()!=null){");

            setInstances.add("        build.set" + captureName(fieldName) + "(" + javaType + ".forNumber(param.get"
                             + captureName(fieldName) + "().getNumber()));");
        } else {
            sets.add("        if(get" + captureName(fieldName) + "()!=null){");
            sets.add("          build.set" + captureName(fieldName) + "(get" + captureName(fieldName) + "());");
            sets.add("        }");

            // setInstances.add(" if(param.get" + captureName(fieldName) + "()!=null){");

            setInstances.add("        build.set" + captureName(fieldName) + "(param.get" + captureName(fieldName)
                             + "());");
            // setInstances.add(" }");

        }
    }

    private String findJavaType(String packageName, DescriptorProto sourceMessageDesc, FieldDescriptorProto field) {
        switch (field.getType()) {
            case TYPE_ENUM:
                return getMessageJavaType(packageName, sourceMessageDesc, field);
            case TYPE_MESSAGE:
                String javaType = getMessageJavaType(packageName, sourceMessageDesc, field);
                return javaType;
            case TYPE_GROUP:
                logger.info("group have not support yet");
                return null;
            case TYPE_STRING:
                return "String";
            case TYPE_INT64:
                return "Long";
            case TYPE_INT32:
                return "Integer";
            case TYPE_FIXED32:
                return "Integer";
            case TYPE_FIXED64:
                return "Long";
            case TYPE_BOOL:
                return "Boolean";
            case TYPE_DOUBLE:
                return "Double";
            case TYPE_FLOAT:
                return "Float";
            default:
                logger.info("have not support this type " + field.getType()
                            + ",please contact 297442500@qq.com for support");
                return null;
        }
    }

    private String getMessageJavaType(String packageName, DescriptorProto sourceMessageDesc,
                                      FieldDescriptorProto field) {
        String fieldType = CommonUtils.findNotIncludePackageType(field.getTypeName());
        Map<String, Pair<DescriptorProto, List<FieldDescriptorProto>>> nestedFieldType = transform(sourceMessageDesc);
        // isMap
        if (nestedFieldType.containsKey(fieldType)) {
            Pair<DescriptorProto, List<FieldDescriptorProto>> nestedFieldPair = nestedFieldType.get(fieldType);
            if (nestedFieldPair.getRight().size() == 2) {
                DescriptorProto mapSourceMessageDesc = nestedFieldPair.getLeft();
                List<FieldDescriptorProto> mapFieldList = nestedFieldPair.getRight();
                String nestedJavaType = "Map<" + findJavaType(packageName, mapSourceMessageDesc, mapFieldList.get(0))
                                        + "," + findJavaType(packageName, mapSourceMessageDesc, mapFieldList.get(1))
                                        + ">";
                return nestedJavaType;
            } else {
                return null;
            }
        } else {
            String result = CommonUtils.findPojoTypeFromCache(field.getTypeName(), pojoTypeCache);
            if (result != null) {
                return result;
            }
            return fieldType;//
        }
    }

    private Map<String, Pair<DescriptorProto, List<FieldDescriptorProto>>> transform(DescriptorProto sourceMessageDesc) {
        Map<String, Pair<DescriptorProto, List<FieldDescriptorProto>>> nestedFieldMap = new HashMap<>();
        sourceMessageDesc.getNestedTypeList().forEach(new Consumer<DescriptorProto>() {

            @Override
            public void accept(DescriptorProto t) {
                nestedFieldMap.put(t.getName(),
                                   new ImmutablePair<DescriptorProto, List<FieldDescriptorProto>>(t, t.getFieldList()));
            }

        });
        return nestedFieldMap;
    }

    private String captureName(String name) {
        char[] cs = name.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);

    }
}
