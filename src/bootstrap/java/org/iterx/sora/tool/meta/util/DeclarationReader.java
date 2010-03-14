package org.iterx.sora.tool.meta.util;

import org.iterx.sora.tool.meta.Declaration;
import org.iterx.sora.tool.meta.declaration.ClassTypeDeclaration;
import org.iterx.sora.tool.meta.declaration.ConstructorDeclaration;
import org.iterx.sora.tool.meta.declaration.FieldDeclaration;
import org.iterx.sora.tool.meta.declaration.InterfaceTypeDeclaration;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;

public final class DeclarationReader {

    private final Declaration<?> declaration;

    public DeclarationReader(final Declaration<?> declaration) {
        this.declaration = declaration;
    }

    public void accept(final DeclarationVisitor declarationVisitor) {
        final Class cls = declaration.getClass();
        if(ClassTypeDeclaration.class == cls) accept(declarationVisitor, (ClassTypeDeclaration) declaration);
        else if(InterfaceTypeDeclaration.class == cls) accept(declarationVisitor, (InterfaceTypeDeclaration) declaration);
        else if(FieldDeclaration.class == cls) accept(declarationVisitor, (FieldDeclaration) declaration);
        else if(ConstructorDeclaration.class == cls) accept(declarationVisitor, (ConstructorDeclaration) declaration);
        else if(MethodDeclaration.class == cls) accept(declarationVisitor, (MethodDeclaration) declaration);
    }

    private void accept(final DeclarationVisitor declarationVisitor, final ClassTypeDeclaration classTypeDeclaration) {
        declarationVisitor.startClass(classTypeDeclaration);
        for(FieldDeclaration fieldDeclaration : classTypeDeclaration.getFieldDeclarations()) accept(declarationVisitor, fieldDeclaration);
        for(ConstructorDeclaration constructorDeclaration : classTypeDeclaration.getConstructorDeclarations()) accept(declarationVisitor, constructorDeclaration);
        for(MethodDeclaration methodDeclaration : classTypeDeclaration.getMethodDeclarations()) accept(declarationVisitor, methodDeclaration);
        declarationVisitor.endClass();
    }

    private void accept(final DeclarationVisitor declarationVisitor, final InterfaceTypeDeclaration interfaceTypeDeclaration) {
        declarationVisitor.startInterface(interfaceTypeDeclaration);
        for(FieldDeclaration fieldDeclaration : interfaceTypeDeclaration.getFieldDeclarations()) accept(declarationVisitor, fieldDeclaration);
        for(MethodDeclaration methodDeclaration : interfaceTypeDeclaration.getMethodDeclarations()) accept(declarationVisitor, methodDeclaration);
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
