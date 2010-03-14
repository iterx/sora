package org.iterx.sora.tool.meta;


//TODO: make immutable & validate (problem: allows multiple instances???)
//TODO: => so make mutable state inner object???
public abstract class AbstractDeclaration<T extends Declaration<T>> implements Declaration<T> {

    public abstract Access getAccess();

    public abstract Modifier[] getModifiers();

    public boolean isClassDeclaration() {
        return false;
    }

    public boolean isInterfaceDeclaration() {
        return false;
    }

    public boolean isFieldDeclaration() {
        return false;
    }

    public boolean isConstructorDeclaration() {
        return false;
    }

    public boolean isMethodDeclaration() {
        return false;
    }

    protected static <T extends AbstractDeclaration<T>> T defineDeclaration(final MetaClassLoader metaClassLoader, final Type<?> type, final T declaration) {
        return metaClassLoader.defineDeclaration(type, declaration);
    }
}