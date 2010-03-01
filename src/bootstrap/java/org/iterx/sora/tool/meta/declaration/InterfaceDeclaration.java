package org.iterx.sora.tool.meta.declaration;

import org.iterx.sora.collection.Set;
import org.iterx.sora.collection.set.HashSet;
import org.iterx.sora.tool.meta.type.Type;

import java.util.Arrays;

public final class InterfaceDeclaration implements Declaration<InterfaceDeclaration> {

    public static final Type[] EMPTY_INTERFACES = new Type[0];

    public enum Access implements Declaration.Access {  PUBLIC, PROTECTED, PRIVATE, DEFAULT }
    public enum Modifier implements Declaration.Modifier { ABSTRACT }

    private final Set<FieldDeclaration> fieldDeclarations;
    private final Set<MethodDeclaration> methodDeclarations;
    private final Type type;
    private Access access;
    private Type[] interfaceTypes;

    private InterfaceDeclaration(final Type type) {
        this.fieldDeclarations = new HashSet<FieldDeclaration>();
        this.methodDeclarations = new HashSet<MethodDeclaration>();
        this.access = Access.PUBLIC;
        this.interfaceTypes = EMPTY_INTERFACES;
        this.type = type;
    }

    public static InterfaceDeclaration newInterfaceDeclaration(final Type<Type.InterfaceMetaType> type) {
        assertType(type);
        return new InterfaceDeclaration(type);
    }

    public Type getType() {
        return type;
    }

    public Type[] getInterfaceTypes() {
        return interfaceTypes;
    }

    public InterfaceDeclaration setInterfaceTypes(final Type... interfaceTypes) {
        assertType(interfaceTypes);
        this.interfaceTypes = interfaceTypes;
        return this;
    }

    public Access getAccess() {
        return access;
    }

    public InterfaceDeclaration setAccess(final Access access) {
        assertAccess(access);
        this.access = access;
        return this;
    }

    public Modifier[] getModifiers() {
        return new Modifier[] { Modifier.ABSTRACT };
    }

    public FieldDeclaration[] getFieldDeclarations() {
        return fieldDeclarations.toArray(new FieldDeclaration[fieldDeclarations.size()]);
    }

    public FieldDeclaration getFieldDeclaration(final String fieldName) {
        for(final FieldDeclaration fieldDeclaration : fieldDeclarations) {
            if(fieldDeclaration.getFieldName().equals(fieldName)) return fieldDeclaration;
        }
        throw new RuntimeException(new NoSuchFieldException());
    }

    public MethodDeclaration[] getMethodDeclarations() {
        return methodDeclarations.toArray(new MethodDeclaration[methodDeclarations.size()]);
    }

    public MethodDeclaration getMethodDeclaration(final String methodName, final Type... argumentTypes) {
        for(final MethodDeclaration methodDeclaration : methodDeclarations) {
            if(methodDeclaration.getMethodName().equals(methodName) &&
               Arrays.equals(methodDeclaration.getArgumentTypes(), argumentTypes)) return methodDeclaration;
        }
        throw new RuntimeException(new NoSuchMethodException());
    }

    public InterfaceDeclaration add(final FieldDeclaration fieldDeclaration) {
        assertFieldDeclaration(fieldDeclaration);
        add(fieldDeclarations, fieldDeclaration);
        return this;
    }

    public InterfaceDeclaration remove(final FieldDeclaration fieldDeclaration) {
        remove(fieldDeclarations, fieldDeclaration);
        return this;
    }

    public InterfaceDeclaration add(final MethodDeclaration methodDeclaration) {
        assertMethodDeclaration(methodDeclaration);
        add(methodDeclarations, methodDeclaration);
        return this;
    }

    public InterfaceDeclaration remove(final MethodDeclaration methodDeclaration) {
        remove(methodDeclarations, methodDeclaration);
        return this;
    }

    public InterfaceDeclaration add(final Declarations declarations) {
        assertDeclarations(declarations);
        fieldDeclarations.addAll(declarations.fieldDeclarations);
        methodDeclarations.addAll(declarations.methodDeclarations);
        return this;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        return (this ==  object) ||
               (object != null && object.getClass() == getClass() && type.equals(((InterfaceDeclaration) object).type));
    }

    @Override
    public String toString() {
        return new StringBuilder().
                append("InterfaceDeclaration\n{\n").
                append("access = ").append(access).append(",\n").
                append("type = ").append(type).append(",\n").
                append("interfaceTypes = ").append(Arrays.toString(interfaceTypes)).append(",\n").
                append("fields = ").append(fieldDeclarations).append(",\n").
                append("methods = ").append(methodDeclarations).append("\n").
                append("}").toString();
    }


    private static <T> void add(final Set<T> declarations, final T declaration) {
        if(declarations.contains(declaration)) throw new IllegalStateException();
        declarations.add(declaration);
    }


    private static <T> void remove(final Set<T> declarations, final T declaration) {
        if(!declarations.contains(declaration)) throw new IllegalStateException();
        declarations.remove(declaration);
    }

    private static void assertType(final Type... types) {
        if(types == null) throw new IllegalArgumentException("type == null");        
        for(Type type : types) if(type == null) throw new IllegalArgumentException("type == null");
    }

    private static void assertAccess(final Access access) {
        if(access != Access.DEFAULT && access != Access.PUBLIC)
            throw new IllegalArgumentException("Unsupported access '" + access + "'");
    }

    private static void assertDeclarations(final Declarations declarations) {
        if(declarations == null) throw new IllegalArgumentException("declarations == null");
        if(!declarations.constructorDeclarations.isEmpty()) throw new IllegalArgumentException("Unsupported declarations '" + declarations.constructorDeclarations +"'");
    }

    private static void assertFieldDeclaration(final FieldDeclaration fieldDeclaration) {
        if(fieldDeclaration == null) throw new IllegalArgumentException("fieldDeclaration == null");
        //TODO: check that newField is static final
    }

    private static void assertMethodDeclaration(final MethodDeclaration methodDeclaration) {
        if(methodDeclaration == null) throw new IllegalArgumentException("methodDeclaration == null");
        //TODO: check that method is abstract or static
    }
}
