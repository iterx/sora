package org.iterx.sora.tool.meta;

import org.iterx.sora.tool.meta.type.ClassType;
import org.iterx.sora.tool.meta.type.PrimitiveType;


public interface Type<T extends Type<T>> {

    public static final PrimitiveType VOID_TYPE = PrimitiveType.VOID_TYPE;
    public static final PrimitiveType BYTE_TYPE = PrimitiveType.BYTE_TYPE;
    public static final PrimitiveType BOOLEAN_TYPE = PrimitiveType.BOOLEAN_TYPE;
    public static final PrimitiveType CHAR_TYPE = PrimitiveType.CHAR_TYPE;
    public static final PrimitiveType SHORT_TYPE = PrimitiveType.SHORT_TYPE;
    public static final PrimitiveType INT_TYPE = PrimitiveType.INT_TYPE;
    public static final PrimitiveType LONG_TYPE = PrimitiveType.LONG_TYPE;
    public static final PrimitiveType FLOAT_TYPE = PrimitiveType.FLOAT_TYPE;
    public static final PrimitiveType DOUBLE_TYPE = PrimitiveType.DOUBLE_TYPE;

    public static final ClassType OBJECT_TYPE = ClassType.OBJECT_TYPE;
    public static final ClassType STRING_TYPE = ClassType.STRING_TYPE;


    MetaClassLoader getMetaClassLoader();

    String getName();

    boolean isAnnotation();

    boolean isArray();

    boolean isClass();

    boolean isEnum();

    boolean isInterface();

    boolean isPrimitive();
}
