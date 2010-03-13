package org.iterx.sora.tool.meta.util;

import org.iterx.sora.tool.meta.declaration.ClassDeclaration;
import org.iterx.sora.tool.meta.declaration.ConstructorDeclaration;
import org.iterx.sora.tool.meta.declaration.FieldDeclaration;
import org.iterx.sora.tool.meta.declaration.InterfaceDeclaration;
import org.iterx.sora.tool.meta.declaration.MethodDeclaration;

public interface DeclarationVisitor {
    
    void startClass(ClassDeclaration classDeclaration);

    void startInterface(InterfaceDeclaration interfaceDeclaration);

    void field(FieldDeclaration fieldDeclaration);

    void constructor(ConstructorDeclaration constructorDeclaration);

    void method(MethodDeclaration methodDeclaration);

    void endClass();

    void endInterface();
}


