package org.iterx.sora.tool.meta.declaration;

import org.iterx.sora.tool.meta.AbstractTypeDeclaration;
import org.iterx.sora.tool.meta.Declaration;
import org.iterx.sora.tool.meta.Declarations;
import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.type.InterfaceType;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public final class InterfaceTypeDeclaration extends AbstractTypeDeclaration<InterfaceType, InterfaceTypeDeclaration> {

    public static final InterfaceType[] EMPTY_INTERFACES = new InterfaceType[0];

    public enum Access implements Declaration.Access {  PUBLIC, PROTECTED, PRIVATE, DEFAULT }
    public enum Modifier implements Declaration.Modifier { ABSTRACT }

    private final Set<FieldDeclaration> fieldDeclarations;
    private final Set<MethodDeclaration> methodDeclarations;
    private final Set<Type<?>> innerTypes;
    private final InterfaceType interfaceType;
    private final transient MetaClassLoader metaClassLoader;

    private Access access;
    private Type<?> outerType;
    private InterfaceType[] interfaceTypes;

    private InterfaceTypeDeclaration(final MetaClassLoader metaClassLoader, final InterfaceType interfaceType) {
        this.fieldDeclarations = new LinkedHashSet<FieldDeclaration>();
        this.methodDeclarations = new LinkedHashSet<MethodDeclaration>();
        this.innerTypes = new LinkedHashSet<Type<?>>();
        this.interfaceType = interfaceType;
        this.interfaceTypes = EMPTY_INTERFACES;
        this.access = Access.PUBLIC;
        this.metaClassLoader = metaClassLoader;
    }

    public static InterfaceTypeDeclaration newInterfaceDeclaration(final InterfaceType interfaceType) {
        return newInterfaceDeclaration(MetaClassLoader.getSystemMetaClassLoader(), interfaceType);
    }

    public static InterfaceTypeDeclaration newInterfaceDeclaration(final MetaClassLoader metaClassLoader, final InterfaceType interfaceType) {
        assertType(interfaceType);
        return defineDeclaration(metaClassLoader, interfaceType, new InterfaceTypeDeclaration(metaClassLoader, interfaceType));
    }

    @Override
    public boolean isInterface() {
        return true;
    }

    @Override
    public boolean isInterfaceTypeDeclaration() {
        return true;
    }

    public MetaClassLoader getMetaClassLoader() {
        return metaClassLoader;
    }

    public String getName() {
        return interfaceType.getName();
    }

    public InterfaceType getInterfaceType() {
        return interfaceType;
    }

    public Type<?> getOuterType() {
        return outerType;
    }

    public InterfaceTypeDeclaration setOuterType(final Type<?> outerType) {
        assertType(outerType);
        this.outerType = outerType;
        return this;
    }

    public InterfaceType[] getInterfaceTypes() {
        return interfaceTypes;
    }

    public InterfaceTypeDeclaration setInterfaceTypes(final InterfaceType... interfaceTypes) {
        assertType(interfaceTypes);
        this.interfaceTypes = interfaceTypes;
        return this;
    }

    public Access getAccess() {
        return access;
    }

    public InterfaceTypeDeclaration setAccess(final Access access) {
        assertAccess(access);
        this.access = access;
        return this;
    }

    public Modifier[] getModifiers() {
        return new Modifier[] { Modifier.ABSTRACT };
    }

    public Type<?>[] getInnerTypes() {
        return innerTypes.toArray(new Type<?>[innerTypes.size()]);
    }

    public InterfaceTypeDeclaration addInnerType(final Type<?> type) {
        innerTypes.add(type);
        return this;
    }
    
    public InterfaceTypeDeclaration removeInnerType(final Type<?> type) {
        innerTypes.remove(type);
        return this;
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

    public InterfaceTypeDeclaration add(final FieldDeclaration fieldDeclaration) {
        assertFieldDeclaration(fieldDeclaration);
        add(fieldDeclarations, fieldDeclaration);
        return this;
    }

    public InterfaceTypeDeclaration remove(final FieldDeclaration fieldDeclaration) {
        remove(fieldDeclarations, fieldDeclaration);
        return this;
    }

    public InterfaceTypeDeclaration add(final MethodDeclaration methodDeclaration) {
        assertMethodDeclaration(methodDeclaration);
        add(methodDeclarations, methodDeclaration);
        return this;
    }

    public InterfaceTypeDeclaration remove(final MethodDeclaration methodDeclaration) {
        remove(methodDeclarations, methodDeclaration);
        return this;
    }

    public InterfaceTypeDeclaration add(final Declarations declarations) {
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
               (object != null && object.getClass() == getClass() && interfaceType.equals(((InterfaceTypeDeclaration) object).interfaceType));
    }

    @Override
    public String toString() {
        return new StringBuilder().
                append("InterfaceTypeDeclaration\n{\n").
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
        //TODO: check that startMethod is abstract or static
    }
}
