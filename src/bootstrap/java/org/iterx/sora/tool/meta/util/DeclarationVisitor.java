package org.iterx.sora.tool.meta.util;

import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.iterx.sora.tool.meta.declaration.ConstructorDeclaration;
import org.iterx.sora.tool.meta.declaration.FieldDeclaration;
import org.iterx.sora.tool.meta.declaration.InterfaceDeclaration;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;

public interface DeclarationVisitor {

    void startClassDeclaration(ClassDeclaration classDeclaration);

    void startInterfaceDeclaration(InterfaceDeclaration interfaceDeclaration);

    void fieldDeclaration(FieldDeclaration fieldDeclaration);

    void constructorDeclaration(ConstructorDeclaration constructorDeclaration);

    void methodDeclaration(MethodDeclaration methodDeclaration);

    void endClassDeclaration();

    void endInterfaceDeclaration();
}


