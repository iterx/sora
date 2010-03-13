package org.iterx.sora.tool.meta;

import org.iterx.sora.tool.meta.declaration.ConstructorDeclaration;
import org.iterx.sora.tool.meta.declaration.FieldDeclaration;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;

import java.util.HashSet;
import java.util.Set;

public abstract class Declarations {

    private final Set<FieldDeclaration> fieldDeclarations;
    private final Set<ConstructorDeclaration> constructorDeclarations;
    private final Set<MethodDeclaration> methodDeclarations;

    //TODO: should check to see if this has been extended -> if so extract inline declarations
    public Declarations() {
        this.fieldDeclarations = new HashSet<FieldDeclaration>();
        this.constructorDeclarations = new HashSet<ConstructorDeclaration>();
        this.methodDeclarations = new HashSet<MethodDeclaration>();
    }

    public FieldDeclaration[] getFieldDeclarations() {
        return fieldDeclarations.toArray(new FieldDeclaration[fieldDeclarations.size()]);
    }

    public ConstructorDeclaration[] getConstructorDeclarations() {
        return constructorDeclarations.toArray(new ConstructorDeclaration[constructorDeclarations.size()]);
    }

    public MethodDeclaration[] getMethodDeclarations() {
        return methodDeclarations.toArray(new MethodDeclaration[methodDeclarations.size()]);
    }

    protected FieldDeclaration field(final FieldDeclaration fieldDeclaration) {
        return store(fieldDeclarations, fieldDeclaration);
    }

    protected FieldDeclaration field(final String fieldName, final Type fieldType) {
        return store(fieldDeclarations, FieldDeclaration.newFieldDeclaration(fieldName, fieldType));
    }

    protected ConstructorDeclaration constructor(final ConstructorDeclaration constructorDeclaration) {
        return store(constructorDeclarations, constructorDeclaration);
    }

    protected ConstructorDeclaration constructor(final Type... constructorTypes) {
        return store(constructorDeclarations, ConstructorDeclaration.newConstructorDeclaration(constructorTypes));
    }

    protected MethodDeclaration method(final MethodDeclaration methodDeclaration) {
        return store(methodDeclarations, methodDeclaration);
    }
    
    protected MethodDeclaration method(final String methodName, final Type... argumentTypes) {
        return store(methodDeclarations, MethodDeclaration.newMethodDeclaration(methodName, argumentTypes));
    }

    private static <T> T store(final Set<? super T> values, final T value) {
        values.add(value);
        return value;
    }
}
