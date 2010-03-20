package org.iterx.sora.tool.meta;

public interface Declaration<T extends Declaration<T>> {

    public interface Access {
        String name();
    }

    public interface Modifier {
        String name();
    }

    abstract Access getAccess();

    abstract Modifier[] getModifiers();

    boolean isClassTypeDeclaration();

    boolean isInterfaceTypeDeclaration();

    boolean isFieldDeclaration();

    boolean isConstructorDeclaration();

    boolean isMethodDeclaration();

}