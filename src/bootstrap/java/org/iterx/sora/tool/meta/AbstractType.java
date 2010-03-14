package org.iterx.sora.tool.meta;


public abstract class AbstractType<T extends Type<T>> implements Type<T> {

    private final transient MetaClassLoader metaClassLoader;
    private final String name;

    protected AbstractType(final MetaClassLoader metaClassLoader, final String name) {
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
                (object != null && getClass().equals(object.getClass()) && getName().equals(((AbstractType) object).getName())));
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

    protected static <T extends AbstractType<T>> T defineType(final MetaClassLoader metaClassLoader, final T type) {
        return metaClassLoader.defineType(type);
    }
}