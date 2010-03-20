package org.iterx.sora.tool.meta;

public abstract class AbstractTypeDeclaration<T extends Type<T>, S extends Declaration<S>> extends AbstractDeclaration<S> implements TypeDeclaration<T, S> {

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
}
