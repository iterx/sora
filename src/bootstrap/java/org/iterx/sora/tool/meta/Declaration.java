package org.iterx.sora.tool.meta;


//TODO: make immutable & validate (problem: allows multiple instances???)
//TODO: => so make mutable state inner object???
//TODO: Class/Interface Declarations should also me Types!!
public abstract class Declaration<T extends Declaration<T>> {

    public interface Access {
        String name();
    }

    public interface Modifier {
        String name();
    }

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

    protected static <T extends Declaration<T>> T defineDeclaration(final MetaClassLoader metaClassLoader, final Type<?> type, final T declaration) {
        return metaClassLoader.defineDeclaration(type, declaration);
    }
}