package org.iterx.sora.tool.meta;

import org.iterx.sora.tool.meta.type.ClassType;
import org.iterx.sora.tool.meta.type.PrimitiveType;


public interface Type<T extends Type<T>> {

    public static final PrimitiveType VOID_TYPE = PrimitiveType.newType("void");
    public static final PrimitiveType BYTE_TYPE = PrimitiveType.newType("byte");
    public static final PrimitiveType BOOLEAN_TYPE = PrimitiveType.newType("boolean");
    public static final PrimitiveType CHAR_TYPE = PrimitiveType.newType("char");
    public static final PrimitiveType SHORT_TYPE = PrimitiveType.newType("short");
    public static final PrimitiveType INT_TYPE = PrimitiveType.newType("int");
    public static final PrimitiveType LONG_TYPE = PrimitiveType.newType("long");
    public static final PrimitiveType FLOAT_TYPE = PrimitiveType.newType("float");
    public static final PrimitiveType DOUBLE_TYPE = PrimitiveType.newType("double");

    public static final ClassType OBJECT_TYPE = ClassType.newType("java.lang.Object");
    public static final ClassType STRING_TYPE = ClassType.newType("java.lang.String");

    MetaClassLoader getMetaClassLoader();

    String getName();

    boolean isAnnotation();

    boolean isArray();

    boolean isClass();

    boolean isEnum();

    boolean isInterface();

    boolean isPrimitive();
}
