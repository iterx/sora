package org.iterx.sora.tool.meta.util;

import org.iterx.sora.tool.meta.Declaration;
import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.iterx.sora.tool.meta.declaration.ConstructorDeclaration;
import org.iterx.sora.tool.meta.declaration.FieldDeclaration;
import org.iterx.sora.tool.meta.declaration.InterfaceDeclaration;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;

public final class DeclarationReader {

    private final Declaration<?> declaration;

    public DeclarationReader(final Declaration<?> declaration) {
        this.declaration = declaration;
    }

    public void accept(final DeclarationVisitor declarationVisitor) {
        final Class cls = declaration.getClass();
        if(ClassDeclaration.class == cls) accept(declarationVisitor, (ClassDeclaration) declaration);
        else if(InterfaceDeclaration.class == cls) accept(declarationVisitor, (InterfaceDeclaration) declaration);
        else if(FieldDeclaration.class == cls) accept(declarationVisitor, (FieldDeclaration) declaration);
        else if(ConstructorDeclaration.class == cls) accept(declarationVisitor, (ConstructorDeclaration) declaration);
        else if(MethodDeclaration.class == cls) accept(declarationVisitor, (MethodDeclaration) declaration);
    }

    private void accept(final DeclarationVisitor declarationVisitor, final ClassDeclaration classDeclaration) {
        declarationVisitor.startClass(classDeclaration);
        for(FieldDeclaration fieldDeclaration : classDeclaration.getFieldDeclarations()) accept(declarationVisitor, fieldDeclaration);
        for(ConstructorDeclaration constructorDeclaration : classDeclaration.getConstructorDeclarations()) accept(declarationVisitor, constructorDeclaration);
        for(MethodDeclaration methodDeclaration : classDeclaration.getMethodDeclarations()) accept(declarationVisitor, methodDeclaration);
        declarationVisitor.endClass();
    }

    private void accept(final DeclarationVisitor declarationVisitor, final InterfaceDeclaration interfaceDeclaration) {
        declarationVisitor.startInterface(interfaceDeclaration);
        for(FieldDeclaration fieldDeclaration : interfaceDeclaration.getFieldDeclarations()) accept(declarationVisitor, fieldDeclaration);
        for(MethodDeclaration methodDeclaration : interfaceDeclaration.getMethodDeclarations()) accept(declarationVisitor, methodDeclaration);
        declarationVisitor.endInterface();
    }

    private void accept(final DeclarationVisitor declarationVisitor, final FieldDeclaration fieldDeclaration) {
        declarationVisitor.field(fieldDeclaration);
    }

    private void accept(final DeclarationVisitor declarationVisitor, final ConstructorDeclaration constructorDeclaration) {
        declarationVisitor.constructor(constructorDeclaration);
    }

    private void accept(final DeclarationVisitor declarationVisitor, final MethodDeclaration methodDeclaration) {
        declarationVisitor.method(methodDeclaration);
    }
}
