package org.iterx.sora.tool.meta;

//TODO: rename as MetaType???
public final class Type {
    //TODO: define native types
    //TODO: work on generics... -> extrace parameter types...

    public static final Type VOID_TYPE = Type.getType(void.class);
    public static final Type OBJECT_TYPE = Type.getType("java.lang.Object");

    private final String name;

    private Type(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Type getType(final Class cls) {
        return new Type(cls.getName());
    }

    public static Type getType(final String name) {
        //TODO: validate name
        //TODO: check if interned predefined types... ->
        return new Type(name);
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
        return "<" + name + ">";
    }
}
