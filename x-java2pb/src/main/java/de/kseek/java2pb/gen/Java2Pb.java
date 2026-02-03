package de.kseek.java2pb.gen;

import io.protostuff.Schema;
import io.protostuff.WireFormat;
import io.protostuff.runtime.EnumIO;
import io.protostuff.runtime.Field;
import io.protostuff.runtime.HasSchema;
import io.protostuff.runtime.RuntimeSchema;
import de.kseek.java2pb.data.Pair;
import de.kseek.java2pb.enums.RuntimeFieldType;
import de.kseek.java2pb.util.ReflectionUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: kseek
 * @date: 2024/7/23
 */
public class Java2Pb {
    /* 协议*/
    public final Schema<?> schema;
    /* 本类message*/
    public StringBuilder message = new StringBuilder();

    public Class<? extends Annotation> protobufCommentClass;

    public Map<String, java.lang.reflect.Field> fieldMap = new HashMap<>();

    public Java2Pb(Schema<?> schema, String pkg) {
        if (!(schema instanceof RuntimeSchema)) {
            throw new IllegalArgumentException("schema instance must be a RuntimeSchema");
        }
        this.schema = schema;
    }

    public Java2Pb(Schema<?> schema, String pkg, Class<? extends Annotation> protobufCommentClass, java.lang.reflect.Field[] fields) {
        this(schema, pkg);
        this.protobufCommentClass = protobufCommentClass;
        if (fields != null) {
            for (java.lang.reflect.Field field : fields) {
                fieldMap.put(field.getName(), field);
            }
        }
    }

    public Java2Pb gen() {
        generateInternal();
        return this;
    }

    public String toMessage() {
        return message.toString();
    }

    protected void generateInternal() {
        if (schema.typeClass().isEnum()) {
            doGenerateEnum(schema.typeClass());
        } else {
            doGenerateMessage(schema);
        }
    }

    protected void doGenerateEnum(Class<?> enumClass) {

        message.append("enum ").append(enumClass.getSimpleName()).append(" {").append("\n");

        for (Object val : enumClass.getEnumConstants()) {
            Enum<?> v = (Enum<?>) val;
            message.append("  ").append(val).append(" = ").append(v.ordinal()).append(";\n");
        }

        message.append("}").append("\n\n");

    }

    protected void doGenerateMessage(Schema<?> schema) {

        if (!(schema instanceof RuntimeSchema)) {
            throw new IllegalStateException("invalid schema type " + schema.getClass());
        }

        RuntimeSchema<?> runtimeSchema = (RuntimeSchema<?>) schema;

        message.append("message ").append(runtimeSchema.messageName()).append(" {").append("\n");

        try {
            java.util.List<? extends Field<?>> fields = runtimeSchema.getFields();

            for (int i = 0; i != fields.size(); ++i) {
                Field<?> field = fields.get(i);
                String fieldType = null;

                if (field.type == WireFormat.FieldType.ENUM) {
                    EnumIO<?> enumIO = getEnumIOFromField(field);
                    fieldType = enumIO.enumClass.getSimpleName();
                } else if (field.type == WireFormat.FieldType.MESSAGE) {
                    if (field.repeated) {
                        java.lang.reflect.Field typeClassField = field.getClass().getDeclaredField("typeClass");
                        typeClassField.setAccessible(true);
                        Class<?> tmpClass = (Class<?>) typeClassField.get(field);
                        fieldType = tmpClass.getSimpleName();
                    } else {
                        Pair<RuntimeFieldType, Class<?>> normField = ReflectionUtil.normalizeFieldClass(field);
                        if (normField == null) {
                            throw new IllegalStateException("unknown fieldClass " + field.getClass());
                        }

                        Class<?> fieldClass = normField.getSecond();
                        if (normField.getFirst() == RuntimeFieldType.RuntimeRepeatedField) {

                        } else if (normField.getFirst() == RuntimeFieldType.RuntimeMessageField) {

                            java.lang.reflect.Field typeClassField = fieldClass.getDeclaredField("typeClass");
                            typeClassField.setAccessible(true);
                            Class<?> typeClass = (Class<?>) typeClassField.get(field);
                            java.lang.reflect.Field hasSchemaField = fieldClass.getDeclaredField("hasSchema");
                            hasSchemaField.setAccessible(true);

                            HasSchema<?> hasSchema = (HasSchema<?>) hasSchemaField.get(field);
                            Schema<?> fieldSchema = hasSchema.getSchema();
                            fieldType = fieldSchema.messageName();
//                        } else if (normField.getFirst() == RuntimeFieldType.RuntimeMapField) {
//                            Field schemaField = fieldClass.getDeclaredField("schema");
//                            schemaField.setAccessible(true);
//                            Schema<?> fieldSchema = (Schema<?>) schemaField.get(field);
//                            Pair<RuntimeSchemaType, Class<?>> normSchema = ReflectionUtil.normalizeSchemaClass(fieldSchema.getClass());
//                            System.out.println("1、"+fieldSchema.messageName());
//                            System.out.println("2、"+fieldSchema.typeClass());
//                            System.out.println("3、"+fieldSchema.getClass().getName());
//                            System.out.println("4、"+fieldSchema.messageFullName());
//                            if (normSchema == null) {
//                                throw new IllegalStateException("unknown schema type " + fieldSchema.getClass());
//                            }
//                            switch (normSchema.getFirst()) {
//                                case ArraySchema:
//                                    fieldType = "ArrayObject";
//                                    break;
//                                case ObjectSchema:
//                                    fieldType = "DynamicObject";
//                                    break;
//                                case MapSchema:
//                                    Field reflectionField = field.getClass().getDeclaredField("val$f");
//                                    reflectionField.setAccessible(true);
//                                    Field pojoField = (Field) reflectionField.get(field);
//                                    Pair<Type, Type> keyValue = ReflectionUtil.getMapGenericTypes(pojoField.getGenericType());
//                                    fieldType = getMapFieldType(keyValue);
//                                    break;
//                                case PolymorphicEnumSchema:
//                                    fieldType = "EnumObject";
//                                    break;
//                            }
//                        } else if (normField.getFirst() == RuntimeFieldType.RuntimeMapField ||
//                                normField.getFirst() == RuntimeFieldType.RuntimeObjectField) {
//                            Field schemaField = fieldClass.getDeclaredField("schema");
//                            schemaField.setAccessible(true);
//                            Schema<?> fieldSchema = (Schema<?>) schemaField.get(field);
//
//                            if ("Array".equals(fieldSchema.messageName())){
//                                Field hsField= fieldSchema.getClass().getDeclaredField("hs");
//                                hsField.setAccessible(true);
//                                HasSchema hasSchema = (HasSchema) hsField.get(fieldSchema);
//                                Field typeClassField= hasSchema.getClass().getDeclaredField("typeClass");
//                                typeClassField.setAccessible(true);
//                                Class clazz = (Class) typeClassField.get(hasSchema);
//                                System.out.println("");
//                            }
//
//                            Pair<RuntimeSchemaType, Class<?>> normSchema = ReflectionUtil.normalizeSchemaClass(fieldSchema.getClass());
//                            System.out.println("1、"+fieldSchema.messageName());
//                            System.out.println("2、"+fieldSchema.typeClass());
//                            System.out.println("3、"+fieldSchema.getClass().getName());
//                            System.out.println("4、"+fieldSchema.messageFullName());

//
//                            if (normSchema == null) {
//                                throw new IllegalStateException("unknown schema type " + fieldSchema.getClass());
//                            }
//
//                            switch (normSchema.getFirst()) {
//                                case ArraySchema:
//                                    fieldType = "ArrayObject";
//                                    break;
//                                case ObjectSchema:
//                                    fieldType = "DynamicObject";
//                                    break;
//                                case MapSchema:
//
//                                    Field reflectionField = field.getClass().getDeclaredField("val$f");
//                                    reflectionField.setAccessible(true);
//                                    Field pojoField = (Field) reflectionField.get(field);
//
//                                    Pair<Type, Type> keyValue = ReflectionUtil.getMapGenericTypes(pojoField.getGenericType());
//
//                                    fieldType = getMapFieldType(keyValue);
//                                    break;
//
//                                case PolymorphicEnumSchema:
//                                    fieldType = "EnumObject";
//                                    break;
//                            }

                            //System.out.println(getClassHierarchy(normSchema.getSecond()));

                        } else {
                            throw new IllegalStateException("field type not support, typeclass=" + schema.typeClass() + ",fieldName=" + field.name);
                        }
                    }
                } else {
                    fieldType = field.type.toString().toLowerCase();
                }

                if (protobufCommentClass != null) {
                    java.lang.reflect.Field field1 = fieldMap.get(field.name);
                    if (field1 != null) {
                        Annotation commentAnn = field1.getAnnotation(protobufCommentClass);
                        if (commentAnn != null) {
                            Class commentAnnClass = commentAnn.getClass();
                            Object comment = commentAnnClass.getMethod("value").invoke(commentAnn);
                            message.append("    // ").append(comment).append("\n");
                        }
                    }
                }
                message.append("    ");
                if (field.repeated) {
                    message.append("repeated ");
                } else {
                    message.append("optional ");
                }
                message.append(fieldType).append(" ").append(field.name).append(" = ").append(field.number).append(";\n");
            }
        } catch (Exception e) {
            throw new RuntimeException("generate proto fail", e);
        }

        message.append("}").append("\n\n");

    }

    @SuppressWarnings("unchecked")
    private static EnumIO<?> getEnumIOFromField(Field<?> field) {
        try {
            for (String name : new String[]{"val$eio", "eio"}) {
                try {
                    java.lang.reflect.Field eioField = field.getClass().getDeclaredField(name);
                    eioField.setAccessible(true);
                    Object v = eioField.get(field);
                    if (v instanceof EnumIO) {
                        return (EnumIO<?>) v;
                    }
                } catch (NoSuchFieldException ignored) {
                }
            }
            for (java.lang.reflect.Field f : field.getClass().getDeclaredFields()) {
                if (EnumIO.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    return (EnumIO<?>) f.get(field);
                }
            }
            throw new IllegalStateException("Cannot find EnumIO in enum field: " + field.getClass().getName());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access EnumIO in enum field", e);
        }
    }

    private static String getMapFieldType(Pair<Type, Type> keyValue) {
        if (keyValue.getFirst() == String.class) {
            if (keyValue.getSecond() == String.class) {
                return "map<string,string>";
            } else {
                return "map<String,Object>";
            }
        }
        return "map<Object,Object>";
    }
}
