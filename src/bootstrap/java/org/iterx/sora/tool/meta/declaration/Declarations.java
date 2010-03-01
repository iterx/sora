package org.iterx.sora.tool.meta.declaration;


import org.iterx.sora.tool.meta.type.Type;

import java.util.HashSet;
import java.util.Set;

public class Declarations {

    final Set<FieldDeclaration> fieldDeclarations;
    final Set<ConstructorDeclaration> constructorDeclarations;
    final Set<MethodDeclaration> methodDeclarations;

    public Declarations() {
        //TODO: should check to see if this has been extended -> if so extract inline declarations
        this.fieldDeclarations = new HashSet<FieldDeclaration>();
        this.constructorDeclarations = new HashSet<ConstructorDeclaration>();
        this.methodDeclarations = new HashSet<MethodDeclaration>();
    }

    public FieldDeclaration field(final FieldDeclaration fieldDeclaration) {
        return store(fieldDeclarations, fieldDeclaration);
    }

    public FieldDeclaration field(final String fieldName, final Type fieldType) {
        return store(fieldDeclarations, FieldDeclaration.newFieldDeclaration(fieldName, fieldType));
    }

    public ConstructorDeclaration constructor(final ConstructorDeclaration constructorDeclaration) {
        return store(constructorDeclarations, constructorDeclaration);
    }

    public ConstructorDeclaration constructor(final Type... constructorTypes) {
        return store(constructorDeclarations, ConstructorDeclaration.newConstructorDeclaration(constructorTypes));
    }

    public MethodDeclaration method(final MethodDeclaration methodDeclaration) {
        return store(methodDeclarations, methodDeclaration);
    }
    
    public MethodDeclaration method(final String methodName, final Type... argumentTypes) {
        return store(methodDeclarations, MethodDeclaration.newMethodDeclaration(methodName, argumentTypes));
    }

    private static <T> T store(final Set<? super T> values, final T value) {
        values.add(value);
        return value;
    }
}
