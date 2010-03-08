package org.iterx.sora.tool.meta;

import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.type.ClassMetaType;
import org.iterx.sora.tool.meta.type.PrimitiveMetaType;


public abstract class Type<T extends Type<T>> {

    public static final PrimitiveMetaType VOID_TYPE = PrimitiveMetaType.newType("void");
    public static final PrimitiveMetaType BYTE_TYPE = PrimitiveMetaType.newType("byte");
    public static final PrimitiveMetaType BOOLEAN_TYPE = PrimitiveMetaType.newType("boolean");
    public static final PrimitiveMetaType CHAR_TYPE = PrimitiveMetaType.newType("char");
    public static final PrimitiveMetaType SHORT_TYPE = PrimitiveMetaType.newType("short");
    public static final PrimitiveMetaType INT_TYPE = PrimitiveMetaType.newType("int");
    public static final PrimitiveMetaType LONG_TYPE = PrimitiveMetaType.newType("long");
    public static final PrimitiveMetaType FLOAT_TYPE = PrimitiveMetaType.newType("float");
    public static final PrimitiveMetaType DOUBLE_TYPE = PrimitiveMetaType.newType("double");

    public static final ClassMetaType OBJECT_TYPE = ClassMetaType.newType("java.lang.Object");

    private final transient MetaClassLoader metaClassLoader;
    private final String name;

    protected Type(final MetaClassLoader metaClassLoader, final String name) {
        this.metaClassLoader = metaClassLoader;
        this.name = name;
    }

    public MetaClassLoader getMetaClassLoader() {
        return metaClassLoader;
    }

    public String getName() {
        return name;
    }

    public boolean isAnnotation() {
        return false;
    }

    public boolean isArray() {
        return false;
    }

    public boolean isClass() {
        return false;
    }

    public boolean isEnum() {
        return false;
    }

    public boolean isInterface() {
        return false;
    }

    public boolean isPrimitive() {
        return false;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        return ((this == object) ||
                (object != null && getClass().equals(object.getClass()) && getName().equals(((Type) object).getName())));
    }

    @Override
    public String toString() {
        return new StringBuilder().
                append('<').
                append(getClass().getSimpleName()).
                append(':').
                append(getName()).
                append('>').
                toString();
    }

    protected static <T extends Type<T>> T defineType(final MetaClassLoader metaClassLoader, final T type) {
        return metaClassLoader.defineType(type);
    }
}
