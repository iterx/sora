package org.iterx.sora.tool.meta.declaration;

import org.iterx.sora.collection.Set;
import org.iterx.sora.collection.set.HashSet;
import org.iterx.sora.tool.meta.Declaration;
import org.iterx.sora.tool.meta.Declarations;
import org.iterx.sora.tool.meta.MetaClassLoader;
import org.iterx.sora.tool.meta.Type;
import org.iterx.sora.tool.meta.type.ClassMetaType;
import org.iterx.sora.tool.meta.type.InterfaceMetaType;

import java.util.Arrays;

//TODO: should implement Type<ClassMetaType>...
public final class ClassDeclaration extends Declaration<ClassDeclaration> {

    public static final InterfaceMetaType[] EMPTY_INTERFACES = new InterfaceMetaType[0];
    public static final Modifier[] EMPTY_MODIFIERS = new Modifier[0];

    public enum Access implements Declaration.Access {  PUBLIC, PROTECTED, PRIVATE, DEFAULT }
    public enum Modifier implements Declaration.Modifier { ABSTRACT, FINAL }

    private final Set<FieldDeclaration> fieldDeclarations;
    private final Set<ConstructorDeclaration> constructorDeclarations;
    private final Set<MethodDeclaration> methodDeclarations;
    private final ClassMetaType classType;

    private final transient MetaClassLoader metaClassLoader;

    private Access access;
    private Modifier[] modifiers;
    private ClassMetaType superType;
    private InterfaceMetaType[] interfaceTypes;

    private ClassDeclaration(final MetaClassLoader metaClassLoader, final ClassMetaType classType) {
        this.fieldDeclarations = new HashSet<FieldDeclaration>();
        this.constructorDeclarations = new HashSet<ConstructorDeclaration>();
        this.methodDeclarations = new HashSet<MethodDeclaration>();
        this.superType = Type.OBJECT_TYPE;
        this.interfaceTypes = EMPTY_INTERFACES;
        this.access = Access.PUBLIC;
        this.modifiers = EMPTY_MODIFIERS;
        this.classType = classType;
        this.metaClassLoader = metaClassLoader;
    }

    public static ClassDeclaration newClassDeclaration(final ClassMetaType classType) {
        return newClassDeclaration(MetaClassLoader.getSystemMetaClassLoader(), classType);
    }

    public static ClassDeclaration newClassDeclaration(final MetaClassLoader metaClassLoader, final ClassMetaType classType) {
        assertType(classType);
        return defineDeclaration(metaClassLoader, classType, new ClassDeclaration(metaClassLoader, classType));
    }

    @Override
    public boolean isClassDeclaration() {
        return true;
    }

    public MetaClassLoader getMetaClassLoader() {
        return metaClassLoader;
    }
        
    public ClassMetaType getClassType() {
        return classType;
    }

    public ClassMetaType getSuperType() {
        return superType;
    }

    public ClassDeclaration setSuperType(final ClassMetaType superType) {
        assertType(superType);
        this.superType = superType;
        return this;
    }

    public InterfaceMetaType[] getInterfaceTypes() {
        return interfaceTypes;
    }

    public ClassDeclaration setInterfaceTypes(final InterfaceMetaType... interfaceTypes) {
        assertType(interfaceTypes);
        this.interfaceTypes = interfaceTypes;
        return this;
    }

    public Access getAccess() {
        return access;
    }

    public ClassDeclaration setAccess(final Access access) {
        assertAccess(access);
        this.access = access;
        return this;
    }

    public Modifier[] getModifiers() {
        return modifiers;
    }

    public ClassDeclaration setModifiers(final Modifier... modifiers) {
        assertModifiers(modifiers);
        this.modifiers = modifiers;
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

    public ConstructorDeclaration[] getConstructorDeclarations() {
        return constructorDeclarations.toArray(new ConstructorDeclaration[constructorDeclarations.size()]);
    }

    public ConstructorDeclaration getConstructorDeclaration(final Type... constructorTypes) {
        for(final ConstructorDeclaration constructorDeclaration : constructorDeclarations) {
            if(Arrays.equals(constructorDeclaration.getConstructorTypes(), constructorTypes)) return constructorDeclaration;
        }
        throw new RuntimeException(new NoSuchMethodException());
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

    public ClassDeclaration add(final FieldDeclaration fieldDeclaration) {
        assertFieldDeclaration(fieldDeclaration);
        add(fieldDeclarations, fieldDeclaration);
        return this;
    }

    public ClassDeclaration remove(final FieldDeclaration fieldDeclaration) {
        remove(fieldDeclarations, fieldDeclaration);
        return this;
    }

    public ClassDeclaration add(final ConstructorDeclaration constructorDeclaration) {
        assertConstructorDeclaration(constructorDeclaration);
        add(constructorDeclarations, constructorDeclaration);
        return this;
    }

    public ClassDeclaration remove(final ConstructorDeclaration constructorDeclaration) {
        remove(constructorDeclarations, constructorDeclaration);
        return this;
    }
    
    public ClassDeclaration add(final MethodDeclaration methodDeclaration) {
        assertMethodDeclaration(methodDeclaration);
        add(methodDeclarations, methodDeclaration);
        return this;
    }

    public ClassDeclaration remove(final MethodDeclaration methodDeclaration) {
        remove(methodDeclarations, methodDeclaration);
        return this;
    }

    public ClassDeclaration add(final Declarations declarations) {
        assertDeclarations(declarations);
        for(final FieldDeclaration fieldDeclaration : declarations.getFieldDeclarations()) fieldDeclarations.add(fieldDeclaration);
        for(final ConstructorDeclaration constructorDeclaration : declarations.getConstructorDeclarations()) constructorDeclarations.add(constructorDeclaration);
        for(final MethodDeclaration methodDeclaration : declarations.getMethodDeclarations()) methodDeclarations.add(methodDeclaration);
        return this;
    }

    @Override
    public int hashCode() {
        return classType.hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        return (this ==  object) ||
               (object != null && object.getClass() == getClass() && classType.equals(((ClassDeclaration) object).classType));
    }

    @Override
    public String toString() {
        return new StringBuilder().
                append("ClassDeclaration: ").
                append(classType).
                toString();
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
        if(types == null) throw new IllegalArgumentException("classType == null");
        for(Type type : types) if(type == null) throw new IllegalArgumentException("classType == null");
    }

    private static void assertAccess(final Access access) {
        if(access == null) throw new IllegalArgumentException("access == null");
    }

    private static void assertModifiers(final Modifier... modifiers) {
        if(modifiers == null) throw new IllegalArgumentException("modifiers == null");
    }

    private static void assertDeclarations(final Declarations declarations) {
        if(declarations == null) throw new IllegalArgumentException("declarations == null");
    }

    private static void assertFieldDeclaration(final FieldDeclaration fieldDeclaration) {
        if(fieldDeclaration == null) throw new IllegalArgumentException("fieldDeclaration == null");
        //TODO: check that newField is static final
    }

    private static void assertConstructorDeclaration(final ConstructorDeclaration constructorDeclaration) {
        if(constructorDeclaration == null) throw new IllegalArgumentException("constructorDeclaration == null");
        //TODO: check that method is abstract or static
    }
    
    private static void assertMethodDeclaration(final MethodDeclaration methodDeclaration) {
        if(methodDeclaration == null) throw new IllegalArgumentException("methodDeclaration == null");
        //TODO: check that method is abstract or static
    }
}