package org.iterx.sora.tool.meta.declaration;


//TODO: refactor...
public final class DeclarationType<T extends Declaration> {

    public static final DeclarationType<InterfaceDeclaration> INTERFACE = new DeclarationType<InterfaceDeclaration>(InterfaceDeclaration.class);
    public static final DeclarationType<ClassDeclaration> CLASS = new DeclarationType<ClassDeclaration>(ClassDeclaration.class);
    public static final DeclarationType<FieldDeclaration> FIELD = new DeclarationType<FieldDeclaration>(FieldDeclaration.class);
    public static final DeclarationType<ConstructorDeclaration> CONSTRUCTOR = new DeclarationType<ConstructorDeclaration>(ConstructorDeclaration.class);
    public static final DeclarationType<MethodDeclaration> METHOD = new DeclarationType<MethodDeclaration>(MethodDeclaration.class);

    private final Class cls;

    private DeclarationType(final Class cls) {
        this.cls = cls;
    }

    public boolean isa(final Declaration declaration) {
        return (declaration != null && declaration.getClass() == cls);
    }

    @SuppressWarnings("unchecked")
    public T cast(final Declaration declaration) {
        return (T) declaration;
    }
}
