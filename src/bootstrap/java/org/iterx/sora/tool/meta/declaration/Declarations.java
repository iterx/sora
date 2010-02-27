package org.iterx.sora.tool.meta.declaration;


import org.iterx.sora.tool.meta.Type;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Declarations implements Iterable<Declaration> {

    //TODO: fix typing... -> maintain seperate sets by type -> allow iteration by type???
    //TODO: or make friend of Declaration...
    public final Set<Declaration> declarations;

    public Declarations() {
        //TODO: should check to see if this has been extended -> if so extract inline declarations
        this.declarations = new HashSet<Declaration>();
    }

    public FieldDeclaration field(final FieldDeclaration fieldDeclaration) {
        return store(declarations, fieldDeclaration);
    }

    public FieldDeclaration field(final String fieldName, final Type fieldType) {
        return store(declarations, FieldDeclaration.newFieldDeclaration(fieldName, fieldType));
    }

    public ConstructorDeclaration constructor(final ConstructorDeclaration constructorDeclaration) {
        return store(declarations, constructorDeclaration);
    }

    public ConstructorDeclaration constructor(final Type... constructorTypes) {
        return store(declarations, ConstructorDeclaration.newConstructorDeclaration(constructorTypes));
    }

    public MethodDeclaration method(final MethodDeclaration methodDeclaration) {
        return store(declarations, methodDeclaration);
    }
    
    public MethodDeclaration method(final String methodName, final Type... argumentTypes) {
        return store(declarations, MethodDeclaration.newMethodDeclaration(methodName, argumentTypes));
    }

    public Iterator<Declaration> iterator() {
        return declarations.iterator();
    }

    private static <T> T store(final Set<? super T> values, final T value) {
        values.add(value);
        return value;
    }
}
