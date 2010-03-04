package org.iterx.sora.tool.meta.type;

import org.iterx.sora.tool.meta.MetaClassLoader;


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

    private final String name;

    protected Type(final String name) {
        this.name = name;
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

    @Deprecated //TODO: use MetaClassLoader
    public static <T extends Type<T>> T getType(final String name) throws ClassNotFoundException {
        return getType(name, MetaClassLoader.getMetaClassLoader());
    }

    @SuppressWarnings("unchecked")
    @Deprecated //TODO: use MetaClassLoader
    public static <T extends Type<T>> T getType(final String name, final MetaClassLoader metaClassLoader) throws ClassNotFoundException {
        //TODO: check to see if primative and/or array...
        if(name.endsWith("[]")) {
            return (T) (Type<?>) ArrayMetaType.newType(name.substring(0, name.length() - 2));
        }
        else if("|void|byte|boolean|char|short|int|long|float|double|".contains("|" + name + "|")) {
            return (T) (Type<?>) PrimitiveMetaType.newType(name);
        }
        return (T) (Type<?>) metaClassLoader.loadType(name);
    }

    @SuppressWarnings("unchecked")
    @Deprecated //TODO: use MetaClassLoader
    public static <T extends Type<T>> T getType(final Class cls) throws ClassNotFoundException {
        final String name = cls.getName();
        return (cls.isInterface())? (T) (Type<?>) InterfaceMetaType.newType(name) :
               (cls.isPrimitive())? (T) (Type<?>) PrimitiveMetaType.newType(name) :
               (cls.isAnnotation())? (T) (Type<?>) AnnotationMetaType.newType(name) :
               (cls.isEnum())? (T) (Type<?>) EnumMetaType.newType(name) :
               (cls.isArray())? (T) (Type<?>) ArrayMetaType.newType(name) :
               (T) (Type<?>) ClassMetaType.newType(name);
    }


}
