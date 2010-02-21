package org.iterx.sora.tool.meta.declaration;

import org.iterx.sora.tool.meta.Types;
import org.objectweb.asm.Type;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Declarations implements Types {
    
    public final Set<ConstructorDeclaration> constructorDeclarations;
    public final Set<MethodDeclaration> methodDeclarations;
    public final Set<FieldDeclaration> fieldDeclarations;

    public Declarations() {
        this.constructorDeclarations = new HashSet<ConstructorDeclaration>();
        this.methodDeclarations = new HashSet<MethodDeclaration>();
        this.fieldDeclarations = new HashSet<FieldDeclaration>();
    }

    public ConstructorDeclaration constructor(final Type... constructorTypes) {
        return store(constructorDeclarations, new ConstructorDeclaration(constructorTypes));
    }

    public MethodDeclaration method(final MethodDeclaration methodDeclaration) {
        return store(methodDeclarations, methodDeclaration);
    }
    
    public MethodDeclaration method(final String methodName, final Type returnType, final Type... argumentTypes) {
        return store(methodDeclarations, new MethodDeclaration(returnType, methodName, argumentTypes));
    }

    public FieldDeclaration field(final String fieldName, final Type fieldType) {
        return store(fieldDeclarations, new FieldDeclaration(fieldName, fieldType));
    }

    public void union(final Declarations declarations) {
        storeAll(fieldDeclarations, declarations.fieldDeclarations);
        storeAll(methodDeclarations, declarations.methodDeclarations);
        storeAll(constructorDeclarations, declarations.constructorDeclarations);
    }

    private static <T> T store(final Set<T> values, final T value) {
        values.add(value);
        return value;
    }

    private static <T> Collection<T> storeAll(final Set<T> values, final Collection<T> collection) {
        values.addAll(collection);
        return collection;
    }
}
