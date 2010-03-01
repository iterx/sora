package org.iterx.sora.tool.meta.type;

import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.iterx.sora.tool.meta.declaration.Declaration;
import org.iterx.sora.tool.meta.declaration.InterfaceDeclaration;


//TODO: collapse as just Type (class, interface primative, etc).
public final class Type<T extends Type.MetaType> {
    //TODO: define native types
    //TODO: work on generics... -> extract parameter types...

    public static final Type VOID_TYPE = Type.getType(void.class);
    public static final Type OBJECT_TYPE = Type.getType(Object.class);

    public static final ClassMetaType CLASS_META_TYPE = new ClassMetaType();
    public static final InterfaceMetaType INTERFACE_META_TYPE = new InterfaceMetaType();

    public static final class ClassMetaType implements MetaType<ClassDeclaration> {}
    public static final class InterfaceMetaType implements MetaType<InterfaceDeclaration> {}
    public interface MetaType<T extends Declaration<T>> {}

    private final String name;
    private final T metaType;

    private Type(final String name, final T metaType) {
        this.name = name;
        this.metaType = metaType;
    }

    public String getName() {
        return name;
    }

    public T getMetaType() {
        return metaType;
    }

    @SuppressWarnings("unchecked")
    public static <T extends MetaType<?>> Type<T> getType(final Class cls) {
        return new Type<T>(cls.getName(), (T) toMetaType(cls));
    }

    public static <T extends MetaType<?>> Type<T> getType(final String name, final T metaType) {
        return new Type<T>(name, metaType);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        return (this == object ||
                object != null && getClass() == object.getClass() && name.equals(((Type) object).name));
    }

    @Override
    public String toString() {
        return new StringBuilder().
                append("<").
                append(metaType.getClass().getSimpleName()).
                append(":").
                append(name).
                append(">").
                toString();
    }
    private static MetaType toMetaType(final Class cls){
        if(cls.isInterface()) return INTERFACE_META_TYPE;
            //else if(cls.isPrimitive()) return MetaType.PRIMITIVE;
            //else if(cls.isEnum()) return MetaType.ENUM;
            //else if(cls.isAnnotation()) return MetaType.ANNOTATION;
        else return CLASS_META_TYPE;
    }
}
