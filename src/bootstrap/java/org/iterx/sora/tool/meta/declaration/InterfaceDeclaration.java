package org.iterx.sora.tool.meta.declaration;

import org.iterx.sora.collection.Set;
import org.iterx.sora.collection.set.HashSet;
import org.iterx.sora.tool.meta.Declaration;
import org.iterx.sora.tool.meta.Declarations;
import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.type.InterfaceMetaType;

import java.util.Arrays;

public final class InterfaceDeclaration extends Declaration<InterfaceDeclaration> {

    public static final InterfaceMetaType[] EMPTY_INTERFACES = new InterfaceMetaType[0];

    public enum Access implements Declaration.Access {  PUBLIC, PROTECTED, PRIVATE, DEFAULT }
    public enum Modifier implements Declaration.Modifier { ABSTRACT }

    private final Set<FieldDeclaration> fieldDeclarations;
    private final Set<MethodDeclaration> methodDeclarations;
    private final InterfaceMetaType interfaceType;
    private final transient MetaClassLoader metaClassLoader;

    private Access access;
    private InterfaceMetaType[] interfaceTypes;

    private InterfaceDeclaration(final MetaClassLoader metaClassLoader, final InterfaceMetaType interfaceType) {
        this.fieldDeclarations = new HashSet<FieldDeclaration>();
        this.methodDeclarations = new HashSet<MethodDeclaration>();
        this.access = Access.PUBLIC;
        this.interfaceTypes = EMPTY_INTERFACES;
        this.interfaceType = interfaceType;
        this.metaClassLoader = metaClassLoader;
    }

    public static InterfaceDeclaration newInterfaceDeclaration(final InterfaceMetaType interfaceType) {
        return newInterfaceDeclaration(MetaClassLoader.getSystemMetaClassLoader(), interfaceType);
    }

    public static InterfaceDeclaration newInterfaceDeclaration(final MetaClassLoader metaClassLoader, final InterfaceMetaType interfaceType) {
        assertType(interfaceType);
        return defineDeclaration(metaClassLoader, interfaceType, new InterfaceDeclaration(metaClassLoader, interfaceType));
    }

    @Override
    public boolean isInterfaceDeclaration() {
        return true;
    }

    public MetaClassLoader getMetaClassLoader() {
        return metaClassLoader;
    }

    public InterfaceMetaType getInterfaceType() {
        return interfaceType;
    }

    public InterfaceMetaType[] getInterfaceTypes() {
        return interfaceTypes;
    }

    public InterfaceDeclaration setInterfaceTypes(final InterfaceMetaType... interfaceTypes) {
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
        for(final FieldDeclaration fieldDeclaration : declarations.getFieldDeclarations()) fieldDeclarations.add(fieldDeclaration);
        for(final MethodDeclaration methodDeclaration : declarations.getMethodDeclarations()) methodDeclarations.add(methodDeclaration);
        return this;
    }

    @Override
    public int hashCode() {
        return interfaceType.hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        return (this ==  object) ||
               (object != null && object.getClass() == getClass() && interfaceType.equals(((InterfaceDeclaration) object).interfaceType));
    }

    @Override
    public String toString() {
        return new StringBuilder().
                append("InterfaceDeclaration\n{\n").
                append("access = ").append(access).append(",\n").
                append("interfaceType = ").append(interfaceType).append(",\n").
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
        if(types == null) throw new IllegalArgumentException("interfaceType == null");
        for(Type type : types) if(type == null) throw new IllegalArgumentException("interfaceType == null");
    }

    private static void assertAccess(final Access access) {
        if(access != Access.DEFAULT && access != Access.PUBLIC)
            throw new IllegalArgumentException("Unsupported access '" + access + "'");
    }

    private static void assertDeclarations(final Declarations declarations) {
        if(declarations == null) throw new IllegalArgumentException("declarations == null");
        if(declarations.getConstructorDeclarations().length != 0) throw new IllegalArgumentException("Unsupported declarations '" + declarations.getConstructorDeclarations() +"'");
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
