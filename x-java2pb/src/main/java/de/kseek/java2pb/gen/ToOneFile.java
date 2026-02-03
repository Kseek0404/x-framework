package de.kseek.java2pb.gen;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: kseek
 * @date: 2024/7/23
 */
@Slf4j
public class ToOneFile {

    /**
     * 通用入口：不依赖任何业务模块，注解类由调用方通过参数传入（classpath 需包含对应模块）。
     * 参数：searchPackage gen2dir filename [importFiles] protoAnnotationClassFullName [commentAnnotationClassFullName]
     * 示例：de.kseek.core gen_proto x-core.proto "" com.xxx.ProtobufMessage com.xxx.ProtobufComment
     */
    public static void main(String[] args) throws ClassNotFoundException {
        if (args.length < 5) {
            System.err.println("Usage: searchPackage gen2dir filename [importFiles] protoAnnotationClassFullName [commentAnnotationClassFullName]");
            System.err.println("Example: de.kseek.core gen_proto x-core.proto \"\" com.xxx.ProtobufMessage com.xxx.ProtobufComment");
            return;
        }
        String searchPackage = args[0];
        String gen2dir = args[1];
        String filename = args[2];
        String importFiles = args.length > 3 && !args[3].isEmpty() ? args[3] : null;
        String protoClazzName = args[4];
        String commentClazzName = args.length > 5 ? args[5] : null;

        Class<? extends Annotation> protoClazz = Class.forName(protoClazzName).asSubclass(Annotation.class);
        Class<? extends Annotation> commentClazz = commentClazzName != null
                ? Class.forName(commentClazzName).asSubclass(Annotation.class)
                : null;
        java2PbMessage(searchPackage, importFiles, null, gen2dir, filename, protoClazz, commentClazz);
    }

    /**
     * 由调用方传入注解 Class，x-java2pb 不依赖具体业务模块（如 x-core）。
     * commentClazz 为 null 时不处理注释注解。
     */
    public static void java2PbMessage(String searchPackage, String importFiles, String pkg, String dir, String filename,
                                      Class<? extends Annotation> protoClazz, Class<? extends Annotation> commentClazz) {
        String[] sps = searchPackage.split(";");
        List<Class<?>> classList = new ArrayList<>();
        for (String pk : sps) {
            Set<Class<?>> classes = ClassUtil.scanPackage(pk);
            classes = classes.stream().filter(c -> c.getAnnotation(protoClazz) != null).collect(Collectors.toSet());
            classList.addAll(classes);
        }
        Class<?>[] classes1 = classList.toArray(new Class[0]);
        List<Class<?>> classListTmp = Arrays.stream(classes1).sorted(Comparator.comparingInt(clazz -> {
            int i = 0;
            try {
                Annotation annotation = clazz.getAnnotation(protoClazz);
                Class<?> c = annotation.getClass();
                Object cmd = c.getMethod("cmd").invoke(annotation);
                Object messageType = c.getMethod("messageType").invoke(annotation);
                if (messageType instanceof Integer && cmd instanceof Integer) {
                    int messageType1 = (Integer) messageType;
                    int cmd1 = (Integer) cmd;
                    i += messageType1 * 10000;
                    i += cmd1;
                    if (messageType1 == 0 && cmd1 == 0) {
                        String simpleName = clazz.getSimpleName();
                        char[] charArray = simpleName.toCharArray();
                        for (int j = 0; j < charArray.length; j++) {
                            char c1 = charArray[j];
                            i += c1 * (charArray.length - j);
                        }
                    }
                }
                return i;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return 0;
        })).collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        sb.append("syntax = \"proto3\";\n\n");
        if (importFiles != null) {
            String[] imports = importFiles.split(";");
            for (String imp : imports) {
                sb.append("import \"").append(imp).append("\";\n");
//                csCode.append("using ").append(imp).append(";\n");
            }
            sb.append("\n\n");
        }

        //String fileName = dir + "/Protocol.proto";
        classListTmp.forEach(clazz -> {
            System.out.println(clazz);
            Schema<?> schema = RuntimeSchema.getSchema(clazz);
            Annotation annotation = clazz.getAnnotation(protoClazz);
            try {
                Class c = annotation.getClass();
                Object obj1 = c.getMethod("resp").invoke(annotation);
                Object obj2 = c.getMethod("messageType").invoke(annotation);
                Object obj3 = c.getMethod("cmd").invoke(annotation);

                if (commentClazz != null) {
                    Annotation commentAnn = clazz.getAnnotation(commentClazz);
                    if (commentAnn != null) {
                        Class<?> commentAnnClass = commentAnn.getClass();
                        Object comment = commentAnnClass.getMethod("value").invoke(commentAnn);
                        sb.append("// ").append(comment).append("\n");
                    }
                }

                if (!"0".equals(obj2.toString())) {
                    String note = String.format("//%s,messageType=%s,cmd=%s", (boolean) obj1 ? "响应" : "请求", obj2, obj3);
                    sb.append(note).append("\n");

                    String attribute = String.format("\t[PFMessage(%s,%s,%s)]", obj2, obj3, obj1);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            if ("ReqSelfRegister".equals(clazz.getSimpleName())) {
                System.out.println("==========");
            }

            Java2Pb pbGen = new Java2Pb(schema, pkg, commentClazz, clazz.getDeclaredFields()).gen();
            String content = pbGen.toMessage();
            sb.append(content);

        });
        if (!StrUtil.isEmpty(dir)){
            String fileName = dir + "/" + filename;
            FileUtil.touch(fileName);
            FileWriter writer = new FileWriter(fileName);
            writer.write(sb.toString(), false);
        }
    }
}


